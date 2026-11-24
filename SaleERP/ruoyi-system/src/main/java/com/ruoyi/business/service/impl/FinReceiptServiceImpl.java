package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.domain.FinReceipt;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.mapper.FinReceiptMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.service.IFinReceiptService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 回款登记Service业务层处理
 *
 * @author ruoyi
 * @date 2026-04-17
 */
@Service
public class FinReceiptServiceImpl implements IFinReceiptService
{
    /** 回款登记表名称 */
    private static final String FIN_RECEIPT_TABLE_NAME = "fin_receipt";

    /** 回款时间字段名称 */
    private static final String FIN_RECEIPT_PAYMENT_TIME_COLUMN_NAME = "payment_time";

    /** 旧版回款日期字段名称 */
    private static final String FIN_RECEIPT_RECEIPT_DATE_COLUMN_NAME = "receipt_date";

    /** 回款方式字段名称 */
    private static final String FIN_RECEIPT_PAYMENT_METHOD_COLUMN_NAME = "payment_method";

    /** 已确认存在的回款字段缓存 */
    private final Map<String, Boolean> finReceiptExistingColumnMap = new ConcurrentHashMap<String, Boolean>();

    @Autowired
    private FinReceiptMapper finReceiptMapper;

    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FinAccountPeriodService finAccountPeriodService;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    /**
     * 查询回款记录
     *
     * @param receiptId 回款记录主键
     * @return 回款记录
     */
    @Override
    public FinReceipt selectFinReceiptById(Long receiptId)
    {
        // 兼容现网仍使用 receipt_date 的旧版回款表结构，避免回款查询直接报错。
        if (useLegacyDateFinReceiptSchema())
        {
            return finReceiptMapper.selectFinReceiptByIdLegacy(receiptId);
        }
        // 兼容仅缺少回款方式字段的半升级结构，避免字段不一致导致查询失败。
        if (useWithoutMethodFinReceiptSchema())
        {
            return finReceiptMapper.selectFinReceiptByIdWithoutMethod(receiptId);
        }
        return finReceiptMapper.selectFinReceiptById(receiptId);
    }

    /**
     * 查询回款记录列表
     *
     * @param finReceipt 回款记录
     * @return 回款记录集合
     */
    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:receipt:list")
    public List<FinReceipt> selectFinReceiptList(FinReceipt finReceipt)
    {
        // 回款流水查询优先兼容旧版字段结构，保证老库也能正常打开回款页面。
        if (useLegacyDateFinReceiptSchema())
        {
            return finReceiptMapper.selectFinReceiptListLegacy(finReceipt);
        }
        // 兼容仅缺少回款方式字段的老数据结构，确保流水查询可正常分页展示。
        if (useWithoutMethodFinReceiptSchema())
        {
            return finReceiptMapper.selectFinReceiptListWithoutMethod(finReceipt);
        }
        return finReceiptMapper.selectFinReceiptList(finReceipt);
    }

    /**
     * 新增回款记录
     *
     * @param finReceipt 回款记录
     * @return 新增结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFinReceipt(FinReceipt finReceipt)
    {
        Long receivableId = finReceipt == null ? null : finReceipt.getReceivableId();
        FinReceivable lockedReceivable = receivableId == null ? null : finReceivableMapper.selectFinReceivableByIdForUpdate(receivableId);
        FinReceivable finReceivable = lockedReceivable != null
            ? finReceivableMapper.selectFinReceivableById(receivableId)
            : (receivableId == null ? null : finReceivableMapper.selectFinReceivableById(receivableId));
        if (lockedReceivable != null && finReceivable != null)
        {
            finReceivable.setAmountDue(lockedReceivable.getAmountDue());
            finReceivable.setAmountPaid(lockedReceivable.getAmountPaid());
            finReceivable.setStatus(lockedReceivable.getStatus());
        }
        validateReceiptBeforeSave(finReceipt, finReceivable);
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(finReceipt.getPaymentTime(), "登记回款/退款");
        }
        finReceipt.setSaleOrderId(finReceivable.getSaleOrderId());
        finReceipt.setCustomerId(finReceivable.getCustomerId());
        if (finReceipt.getAccountId() == null)
        {
            finReceipt.setAccountId(resolveDefaultAccountId());
        }
        if (finReceipt.getAccountId() == null)
        {
            throw new ServiceException("未找到可用的默认资金结算账户，请先在【资金账户】页面启用结算账户");
        }
        String operator = "system";
        try
        {
            operator = SecurityUtils.getUsername();
            finReceipt.setCreateBy(operator);
        }
        catch (Exception exception)
        {
            // ignore in non-web context
        }
        BigDecimal currentPaidAmount = finReceivable.getAmountPaid() == null ? BigDecimal.ZERO : finReceivable.getAmountPaid();
        BigDecimal receivableAmount = finReceivable.getAmountDue() == null ? BigDecimal.ZERO : finReceivable.getAmountDue();
        BigDecimal inputAmount = finReceipt.getAmount();
        boolean isRedInkRefund = receivableAmount.compareTo(BigDecimal.ZERO) < 0;
        BigDecimal signedAmount;
        BigDecimal newPaidAmount;
        String receivableStatus;
        if (isRedInkRefund)
        {
            BigDecimal maxRefundable = currentPaidAmount.subtract(receivableAmount);
            if (maxRefundable.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("该红字退款台账已全部退清，无需重复退款");
            }
            if (inputAmount.compareTo(maxRefundable) > 0)
            {
                throw new ServiceException("退款金额超过待退余额（待退余额：" + maxRefundable + "）");
            }
            signedAmount = inputAmount.negate();
            newPaidAmount = currentPaidAmount.add(signedAmount);
            receivableStatus = newPaidAmount.compareTo(receivableAmount) <= 0 ? "paid" : "partial";
            finReceipt.setAmount(signedAmount);
            if (StringUtils.isEmpty(finReceipt.getRemark()))
            {
                finReceipt.setRemark("红字应收退还客户款");
            }
        }
        else
        {
            signedAmount = inputAmount;
            newPaidAmount = currentPaidAmount.add(signedAmount);
            if (newPaidAmount.compareTo(receivableAmount) > 0)
            {
                throw new ServiceException("回款金额超过应收余额");
            }
            receivableStatus = newPaidAmount.compareTo(receivableAmount) == 0 ? "paid" : "partial";
        }
        finReceivableMapper.updateFinReceivablePaidAmount(finReceivable.getReceivableId(), newPaidAmount, receivableStatus);
        if (!isRedInkRefund && finReceivable.getSaleOrderId() != null)
        {
            String saleOrderPaymentStatus = "paid".equals(receivableStatus) ? "paid" : "partial";
            wmsSaleOrderMapper.updateWmsSaleOrderPaymentStatus(finReceivable.getSaleOrderId(), saleOrderPaymentStatus);
        }
        int insertRows;
        if (useLegacyDateFinReceiptSchema())
        {
            insertRows = finReceiptMapper.insertFinReceiptLegacy(finReceipt);
        }
        else if (useWithoutMethodFinReceiptSchema())
        {
            insertRows = finReceiptMapper.insertFinReceiptWithoutMethod(finReceipt);
        }
        else
        {
            insertRows = finReceiptMapper.insertFinReceipt(finReceipt);
        }
        String bizNo = StringUtils.isNotEmpty(finReceivable.getSaleOrderNo())
            ? finReceivable.getSaleOrderNo()
            : ("RC-" + finReceipt.getReceiptId());
        String targetName = StringUtils.isNotEmpty(finReceivable.getCustomerName())
            ? finReceivable.getCustomerName()
            : ("客户#" + finReceivable.getCustomerId());
        finAccountPeriodService.changeAccountBalanceWithJournal(
            finReceipt.getAccountId(),
            signedAmount,
            isRedInkRefund ? "sale_refund" : "receipt",
            finReceipt.getReceiptId(),
            bizNo,
            targetName,
            finReceipt.getPaymentTime(),
            finReceipt.getRemark(),
            operator
        );
        return insertRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertFinReceipt(List<Long> receivableIds, BigDecimal totalAmount, String paymentMethod,
                                     Long accountId, Date paymentTime, String remark)
    {
        if (receivableIds == null || receivableIds.isEmpty())
        {
            throw new ServiceException("请至少选择一条待收款应收台账");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("合并核销总金额必须大于0");
        }
        if (StringUtils.isEmpty(paymentMethod))
        {
            throw new ServiceException("回款方式不能为空");
        }
        Date payTime = paymentTime == null ? new Date() : paymentTime;
        List<FinReceivable> targets = new ArrayList<>();
        BigDecimal totalRemain = BigDecimal.ZERO;
        Long commonCustomerId = null;
        for (Long rid : receivableIds)
        {
            FinReceivable rec = finReceivableMapper.selectFinReceivableById(rid);
            if (rec == null)
            {
                throw new ServiceException("应收台账不存在（ID：" + rid + "）");
            }
            BigDecimal due = rec.getAmountDue() == null ? BigDecimal.ZERO : rec.getAmountDue();
            BigDecimal paid = rec.getAmountPaid() == null ? BigDecimal.ZERO : rec.getAmountPaid();
            if (due.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("批量合并核销仅支持正向应收台账，红字退款台账请单笔办理退款（ID：" + rid + "）");
            }
            BigDecimal remain = due.subtract(paid);
            if (remain.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            if (commonCustomerId == null)
            {
                commonCustomerId = rec.getCustomerId();
            }
            else if (!commonCustomerId.equals(rec.getCustomerId()))
            {
                throw new ServiceException("多单合并核销所选应收台账必须属于同一客户");
            }
            targets.add(rec);
            totalRemain = totalRemain.add(remain);
        }
        if (targets.isEmpty())
        {
            throw new ServiceException("所选应收台账均已结清，无需核销");
        }
        if (totalAmount.compareTo(totalRemain) > 0)
        {
            throw new ServiceException("核销总金额（" + totalAmount + "）超过所选单据合计未收余额（" + totalRemain + "）");
        }
        BigDecimal left = totalAmount;
        int count = 0;
        for (FinReceivable rec : targets)
        {
            if (left.compareTo(BigDecimal.ZERO) <= 0)
            {
                break;
            }
            BigDecimal remain = rec.getAmountDue().subtract(rec.getAmountPaid() == null ? BigDecimal.ZERO : rec.getAmountPaid());
            BigDecimal currentPay = left.min(remain);
            FinReceipt receipt = new FinReceipt();
            receipt.setReceivableId(rec.getReceivableId());
            receipt.setAmount(currentPay);
            receipt.setPaymentMethod(paymentMethod);
            receipt.setAccountId(accountId);
            receipt.setPaymentTime(payTime);
            receipt.setRemark(StringUtils.isEmpty(remark) ? "多单合并核销回款" : remark.trim());
            count += insertFinReceipt(receipt);
            left = left.subtract(currentPay);
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int voidFinReceipt(Long receiptId, String voidReason)
    {
        if (receiptId == null)
        {
            throw new ServiceException("回款记录编号不能为空");
        }
        FinReceipt receipt = selectFinReceiptById(receiptId);
        if (receipt == null)
        {
            throw new ServiceException("回款记录不存在");
        }
        if ("voided".equals(receipt.getStatus()))
        {
            throw new ServiceException("该回款记录已冲红作废，请勿重复操作");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(receipt.getPaymentTime(), "冲红回款记录");
            complianceHelper.assertPeriodOpen(new Date(), "冲红回款记录（当期资金流水）");
        }
        String reason = StringUtils.isEmpty(voidReason) ? "财务冲红纠错" : voidReason.trim();
        String operator = "system";
        try
        {
            operator = SecurityUtils.getUsername();
        }
        catch (Exception ignored) {}

        int rows = finReceiptMapper.voidFinReceiptStatus(receiptId, reason, operator);
        if (rows == 0)
        {
            throw new ServiceException("回款记录状态已变更，请刷新重试");
        }
        if (receipt.getAccountId() != null && receipt.getAmount() != null && receipt.getAmount().compareTo(BigDecimal.ZERO) != 0)
        {
            boolean wasRefund = receipt.getAmount().compareTo(BigDecimal.ZERO) < 0;
            String bizNo = StringUtils.isNotEmpty(receipt.getSaleOrderNo()) ? receipt.getSaleOrderNo() : ("RC-" + receiptId);
            String targetName = StringUtils.isNotEmpty(receipt.getCustomerName()) ? receipt.getCustomerName() : ("客户#" + receipt.getCustomerId());
            finAccountPeriodService.changeAccountBalanceWithJournal(
                receipt.getAccountId(),
                receipt.getAmount().negate(),
                wasRefund ? "sale_refund_void" : "receipt_void",
                receiptId,
                bizNo,
                targetName,
                new Date(),
                "冲红作废：" + reason,
                operator
            );
        }

        if (receipt.getReceivableId() != null)
        {
            FinReceivable lockedRec = finReceivableMapper.selectFinReceivableByIdForUpdate(receipt.getReceivableId());
            FinReceivable finReceivable = lockedRec != null ? lockedRec : finReceivableMapper.selectFinReceivableById(receipt.getReceivableId());
            if (finReceivable != null)
            {
                BigDecimal validPaid = finReceiptMapper.selectValidReceiptSumByReceivableId(receipt.getReceivableId());
                if (validPaid == null)
                {
                    validPaid = BigDecimal.ZERO;
                }
                BigDecimal due = finReceivable.getAmountDue() == null ? BigDecimal.ZERO : finReceivable.getAmountDue();
                String status;
                if (due.compareTo(BigDecimal.ZERO) < 0)
                {
                    if (validPaid.compareTo(due) <= 0)
                    {
                        status = "paid";
                    }
                    else if (validPaid.compareTo(BigDecimal.ZERO) < 0)
                    {
                        status = "partial";
                    }
                    else
                    {
                        status = "unpaid";
                    }
                }
                else
                {
                    if (validPaid.compareTo(due) >= 0 && due.compareTo(BigDecimal.ZERO) > 0)
                    {
                        status = "paid";
                    }
                    else if (validPaid.compareTo(BigDecimal.ZERO) > 0)
                    {
                        status = "partial";
                    }
                    else
                    {
                        status = "unpaid";
                    }
                }
                finReceivableMapper.updateFinReceivablePaidAmount(finReceivable.getReceivableId(), validPaid, status);
                if (due.compareTo(BigDecimal.ZERO) >= 0 && finReceivable.getSaleOrderId() != null)
                {
                    wmsSaleOrderMapper.updateWmsSaleOrderPaymentStatus(finReceivable.getSaleOrderId(), status);
                }
            }
        }
        return rows;
    }

    private Long resolveDefaultAccountId()
    {
        if (jdbcTemplate == null) return null;
        try
        {
            List<Long> ids = jdbcTemplate.queryForList(
                "SELECT account_id FROM fin_account WHERE status = '0' ORDER BY CASE WHEN is_default = 'Y' THEN 0 ELSE 1 END, account_id ASC LIMIT 1",
                Long.class
            );
            return ids != null && !ids.isEmpty() ? ids.get(0) : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private void adjustSettlementAccountBalance(Long accountId, BigDecimal deltaAmount)
    {
        if (jdbcTemplate == null || accountId == null || deltaAmount == null || deltaAmount.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        try
        {
            jdbcTemplate.update(
                "UPDATE fin_account SET current_balance = IFNULL(current_balance, 0) + ?, update_time = NOW() WHERE account_id = ?",
                deltaAmount, accountId
            );
        }
        catch (Exception ignored) {}
    }

    @Override
    public int updateFinReceipt(FinReceipt finReceipt)
    {
        throw new ServiceException("已入账回款不可直接修改，请使用【冲红作废】后重新登记");
    }

    @Override
    public int deleteFinReceiptById(Long receiptId)
    {
        throw new ServiceException("已入账回款不可物理删除，请使用【冲红作废】回退账款并保留审计留痕");
    }

    @Override
    public int deleteFinReceiptByIds(Long[] receiptIds)
    {
        throw new ServiceException("已入账回款不可物理删除，请使用【冲红作废】回退账款并保留审计留痕");
    }

    /**
     * 保存前校验回款记录与来源应收台账是否一致
     *
     * @param finReceipt 回款记录
     * @param finReceivable 应收台账
     */
    private void validateReceiptBeforeSave(FinReceipt finReceipt, FinReceivable finReceivable)
    {
        if (finReceipt == null)
        {
            throw new ServiceException("回款记录不能为空");
        }
        if (finReceipt.getReceivableId() == null)
        {
            throw new ServiceException("应收记录不能为空");
        }
        if (finReceipt.getAmount() == null || finReceipt.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("回款金额必须大于0");
        }
        if (finReceipt.getPaymentTime() == null)
        {
            throw new ServiceException("回款时间不能为空");
        }
        if (shouldValidateReceiptPaymentMethod() && StringUtils.isEmpty(finReceipt.getPaymentMethod()))
        {
            throw new ServiceException("回款方式不能为空");
        }
        if (finReceivable == null)
        {
            throw new ServiceException("应收记录不存在");
        }
        if (finReceipt.getSaleOrderId() != null && !finReceipt.getSaleOrderId().equals(finReceivable.getSaleOrderId()))
        {
            throw new ServiceException("回款记录销售单必须与应收台账保持一致");
        }
        if (finReceipt.getCustomerId() != null && !finReceipt.getCustomerId().equals(finReceivable.getCustomerId()))
        {
            throw new ServiceException("回款记录客户必须与应收台账保持一致");
        }
    }

    /**
     * 判断当前回款登记是否需要校验回款方式字段
     *
     * @return 是否需要校验回款方式
     */
    private boolean shouldValidateReceiptPaymentMethod()
    {
        return !useLegacyDateFinReceiptSchema() && !useWithoutMethodFinReceiptSchema();
    }

    /**
     * 判断当前回款登记表是否仍为旧版结构
     * @return 是否使用旧版结构
     */
    private boolean useLegacyDateFinReceiptSchema()
    {
        return !hasFinReceiptColumn(FIN_RECEIPT_PAYMENT_TIME_COLUMN_NAME)
            && hasFinReceiptColumn(FIN_RECEIPT_RECEIPT_DATE_COLUMN_NAME);
    }

    /**
     * 判断当前回款登记表是否缺少回款方式字段
     * @return 是否缺少回款方式字段
     */
    private boolean useWithoutMethodFinReceiptSchema()
    {
        return hasFinReceiptColumn(FIN_RECEIPT_PAYMENT_TIME_COLUMN_NAME)
            && !hasFinReceiptColumn(FIN_RECEIPT_PAYMENT_METHOD_COLUMN_NAME);
    }

    /**
     * 判断回款登记表是否包含指定字段
     * @param columnName 字段名称
     * @return 是否包含字段
     */
    private boolean hasFinReceiptColumn(String columnName)
    {
        if (jdbcTemplate == null)
        {
            return true;
        }
        if (finReceiptExistingColumnMap.containsKey(columnName))
        {
            return finReceiptExistingColumnMap.get(columnName);
        }
        String countSql =
            "select count(1) from information_schema.columns where table_schema = (select database()) and table_name = ? and column_name = ?";
        Integer columnCount = jdbcTemplate.queryForObject(countSql, Integer.class, FIN_RECEIPT_TABLE_NAME, columnName);
        boolean columnExists = columnCount != null && columnCount.intValue() > 0;
        finReceiptExistingColumnMap.put(columnName, Boolean.valueOf(columnExists));
        return columnExists;
    }
}
