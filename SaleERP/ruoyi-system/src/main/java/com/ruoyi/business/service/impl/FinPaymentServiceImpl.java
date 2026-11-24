package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.business.domain.FinPayment;
import com.ruoyi.business.mapper.FinPayableMapper;
import com.ruoyi.business.mapper.FinPaymentMapper;
import com.ruoyi.business.service.IFinPaymentService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class FinPaymentServiceImpl implements IFinPaymentService
{
    @Autowired
    private FinPaymentMapper finPaymentMapper;

    @Autowired
    private FinPayableMapper finPayableMapper;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FinAccountPeriodService finAccountPeriodService;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    @Override
    public FinPayment selectFinPaymentByPaymentId(Long paymentId)
    {
        return finPaymentMapper.selectFinPaymentByPaymentId(paymentId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:payment:list")
    public List<FinPayment> selectFinPaymentList(FinPayment finPayment)
    {
        return finPaymentMapper.selectFinPaymentList(finPayment);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertFinPayment(FinPayment finPayment)
    {
        Long payableId = finPayment == null ? null : finPayment.getPayableId();
        FinPayable lockedPayable = payableId == null ? null : finPayableMapper.selectFinPayableByPayableIdForUpdate(payableId);
        FinPayable finPayable = lockedPayable != null
            ? finPayableMapper.selectFinPayableByPayableId(payableId)
            : (payableId == null ? null : finPayableMapper.selectFinPayableByPayableId(payableId));
        if (lockedPayable != null && finPayable != null)
        {
            finPayable.setAmountDue(lockedPayable.getAmountDue());
            finPayable.setAmountPaid(lockedPayable.getAmountPaid());
            finPayable.setRemainAmount(lockedPayable.getRemainAmount());
            finPayable.setStatus(lockedPayable.getStatus());
        }
        validatePaymentBeforeSave(finPayment, finPayable);
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(finPayment.getPaymentTime(), "登记付款/供应商退款");
        }
        finPayment.setPurchaseOrderId(finPayable.getPurchaseOrderId());
        finPayment.setSupplierId(finPayable.getSupplierId());
        if (finPayment.getAccountId() == null)
        {
            finPayment.setAccountId(resolveDefaultAccountId());
        }
        if (finPayment.getAccountId() == null)
        {
            throw new ServiceException("未找到可用的默认资金结算账户，请先在【资金账户】页面启用结算账户");
        }
        BigDecimal currentPaidAmount = finPayable.getAmountPaid() == null ? BigDecimal.ZERO : finPayable.getAmountPaid();
        BigDecimal payableAmount = finPayable.getAmountDue() == null ? BigDecimal.ZERO : finPayable.getAmountDue();
        BigDecimal inputAmount = finPayment.getAmount();
        boolean isRedInkRefund = payableAmount.compareTo(BigDecimal.ZERO) < 0;
        BigDecimal accountDelta;
        if (isRedInkRefund)
        {
            BigDecimal maxRefundable = currentPaidAmount.subtract(payableAmount);
            if (maxRefundable.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("该红字供应商退款台账已全部收回，无需重复登记");
            }
            if (inputAmount.compareTo(maxRefundable) > 0)
            {
                throw new ServiceException("供应商退款金额超过待收退款余额（待收：" + maxRefundable + "）");
            }
            // 红字应付收回供应商退款：付款记录金额记负值，资金账户记正收入
            finPayment.setAmount(inputAmount.negate());
            accountDelta = inputAmount;
            if (StringUtils.isEmpty(finPayment.getRemark()))
            {
                finPayment.setRemark("红字应付收回供应商退款");
            }
        }
        else
        {
            BigDecimal newPaidAmount = currentPaidAmount.add(inputAmount);
            if (newPaidAmount.compareTo(payableAmount) > 0)
            {
                throw new ServiceException("付款金额超过应付余额");
            }
            accountDelta = inputAmount.negate();
        }
        finPayment.setCreateTime(DateUtils.getNowDate());
        String operator = "system";
        try
        {
            operator = SecurityUtils.getUsername();
            finPayment.setCreateBy(operator);
        }
        catch (Exception ignored) {}

        int insertRows = finPaymentMapper.insertFinPayment(finPayment);
        String bizNo = StringUtils.isNotEmpty(finPayable.getPurchaseOrderNo())
            ? finPayable.getPurchaseOrderNo()
            : ("PM-" + finPayment.getPaymentId());
        String targetName = StringUtils.isNotEmpty(finPayable.getSupplierName())
            ? finPayable.getSupplierName()
            : ("供应商#" + finPayable.getSupplierId());
        finAccountPeriodService.changeAccountBalanceWithJournal(
            finPayment.getAccountId(),
            accountDelta,
            isRedInkRefund ? "purchase_refund" : "payment",
            finPayment.getPaymentId(),
            bizNo,
            targetName,
            finPayment.getPaymentTime(),
            finPayment.getRemark(),
            operator
        );
        updatePayableStatus(finPayment.getPayableId());
        return insertRows;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchInsertFinPayment(List<Long> payableIds, BigDecimal totalAmount, String paymentMethod,
                                     Long accountId, Date paymentTime, String remark)
    {
        if (payableIds == null || payableIds.isEmpty())
        {
            throw new ServiceException("请至少选择一条待付款应付台账");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("合并付款核销总金额必须大于0");
        }
        if (StringUtils.isEmpty(paymentMethod))
        {
            throw new ServiceException("付款方式不能为空");
        }
        Date payTime = paymentTime == null ? new Date() : paymentTime;
        List<FinPayable> targets = new ArrayList<>();
        BigDecimal totalRemain = BigDecimal.ZERO;
        Long commonSupplierId = null;
        for (Long pid : payableIds)
        {
            FinPayable pay = finPayableMapper.selectFinPayableByPayableId(pid);
            if (pay == null)
            {
                throw new ServiceException("应付台账不存在（ID：" + pid + "）");
            }
            BigDecimal due = pay.getAmountDue() == null ? BigDecimal.ZERO : pay.getAmountDue();
            BigDecimal paid = pay.getAmountPaid() == null ? BigDecimal.ZERO : pay.getAmountPaid();
            if (due.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("批量合并付款核销仅支持正向应付台账，红字退款台账请单笔登记供应商退款（ID：" + pid + "）");
            }
            BigDecimal remain = due.subtract(paid);
            if (remain.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            if (commonSupplierId == null)
            {
                commonSupplierId = pay.getSupplierId();
            }
            else if (!commonSupplierId.equals(pay.getSupplierId()))
            {
                throw new ServiceException("多单合并付款核销所选应付台账必须属于同一供应商");
            }
            targets.add(pay);
            totalRemain = totalRemain.add(remain);
        }
        if (targets.isEmpty())
        {
            throw new ServiceException("所选应付台账均已结清，无需付款");
        }
        if (totalAmount.compareTo(totalRemain) > 0)
        {
            throw new ServiceException("核销总金额（" + totalAmount + "）超过所选单据合计未付余额（" + totalRemain + "）");
        }
        BigDecimal left = totalAmount;
        int count = 0;
        for (FinPayable pay : targets)
        {
            if (left.compareTo(BigDecimal.ZERO) <= 0)
            {
                break;
            }
            BigDecimal remain = pay.getAmountDue().subtract(pay.getAmountPaid() == null ? BigDecimal.ZERO : pay.getAmountPaid());
            BigDecimal currentPay = left.min(remain);
            FinPayment payment = new FinPayment();
            payment.setPayableId(pay.getPayableId());
            payment.setAmount(currentPay);
            payment.setPaymentMethod(paymentMethod);
            payment.setAccountId(accountId);
            payment.setPaymentTime(payTime);
            payment.setRemark(StringUtils.isEmpty(remark) ? "多单合并付款核销" : remark.trim());
            count += insertFinPayment(payment);
            left = left.subtract(currentPay);
        }
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int voidFinPayment(Long paymentId, String voidReason)
    {
        if (paymentId == null)
        {
            throw new ServiceException("付款记录编号不能为空");
        }
        FinPayment payment = finPaymentMapper.selectFinPaymentByPaymentId(paymentId);
        if (payment == null)
        {
            throw new ServiceException("付款记录不存在");
        }
        if ("voided".equals(payment.getStatus()))
        {
            throw new ServiceException("该付款记录已冲红作废，请勿重复操作");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(payment.getPaymentTime(), "冲红付款记录");
            complianceHelper.assertPeriodOpen(new Date(), "冲红付款记录（当期资金流水）");
        }
        String reason = StringUtils.isEmpty(voidReason) ? "财务付款冲红纠错" : voidReason.trim();
        String operator = "system";
        try
        {
            operator = SecurityUtils.getUsername();
        }
        catch (Exception ignored) {}

        int rows = finPaymentMapper.voidFinPaymentStatus(paymentId, reason, operator);
        if (rows == 0)
        {
            throw new ServiceException("付款记录状态已变更，请刷新重试");
        }
        if (payment.getAccountId() != null && payment.getAmount() != null && payment.getAmount().compareTo(BigDecimal.ZERO) != 0)
        {
            boolean wasRefund = payment.getAmount().compareTo(BigDecimal.ZERO) < 0;
            String bizNo = StringUtils.isNotEmpty(payment.getPurchaseOrderNo()) ? payment.getPurchaseOrderNo() : ("PM-" + paymentId);
            String targetName = StringUtils.isNotEmpty(payment.getSupplierName()) ? payment.getSupplierName() : ("供应商#" + payment.getSupplierId());
            finAccountPeriodService.changeAccountBalanceWithJournal(
                payment.getAccountId(),
                payment.getAmount(),
                wasRefund ? "purchase_refund_void" : "payment_void",
                paymentId,
                bizNo,
                targetName,
                new Date(),
                "冲红作废：" + reason,
                operator
            );
        }
        updatePayableStatus(payment.getPayableId());
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
    public int updateFinPayment(FinPayment finPayment)
    {
        throw new ServiceException("已入账付款不可直接修改，请使用【冲红作废】后重新登记");
    }

    @Override
    public int deleteFinPaymentByPaymentIds(Long[] paymentIds)
    {
        throw new ServiceException("已入账付款不可物理删除，请使用【冲红作废】回退账款并保留审计留痕");
    }

    @Override
    public int deleteFinPaymentByPaymentId(Long paymentId)
    {
        throw new ServiceException("已入账付款不可物理删除，请使用【冲红作废】回退账款并保留审计留痕");
    }

    private void validatePaymentBeforeSave(FinPayment finPayment, FinPayable finPayable)
    {
        if (finPayment == null)
        {
            throw new ServiceException("付款记录不能为空");
        }
        if (finPayment.getPayableId() == null)
        {
            throw new ServiceException("应付账款不能为空");
        }
        if (finPayment.getAmount() == null || finPayment.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("付款金额必须大于0");
        }
        if (finPayment.getPaymentTime() == null)
        {
            throw new ServiceException("付款时间不能为空");
        }
        if (StringUtils.isEmpty(finPayment.getPaymentMethod()))
        {
            throw new ServiceException("付款方式不能为空");
        }
        if (finPayable == null)
        {
            throw new ServiceException("应付账款不存在");
        }
        if (finPayment.getPurchaseOrderId() != null && !finPayment.getPurchaseOrderId().equals(finPayable.getPurchaseOrderId()))
        {
            throw new ServiceException("付款记录采购订单必须与应付台账保持一致");
        }
        if (finPayment.getSupplierId() != null && !finPayment.getSupplierId().equals(finPayable.getSupplierId()))
        {
            throw new ServiceException("付款记录供应商必须与应付台账保持一致");
        }
    }

    private void updatePayableStatus(Long payableId)
    {
        if (payableId == null)
        {
            return;
        }
        FinPayable locked = finPayableMapper.selectFinPayableByPayableIdForUpdate(payableId);
        FinPayable finPayable = locked != null ? locked : finPayableMapper.selectFinPayableByPayableId(payableId);
        if (finPayable == null)
        {
            return;
        }
        BigDecimal totalPaidAmount = finPaymentMapper.selectPaymentAmountSumByPayableId(payableId);
        if (totalPaidAmount == null)
        {
            totalPaidAmount = BigDecimal.ZERO;
        }
        finPayable.setAmountPaid(totalPaidAmount);
        BigDecimal payableAmount = finPayable.getAmountDue() == null ? BigDecimal.ZERO : finPayable.getAmountDue();
        BigDecimal remainAmount = payableAmount.subtract(totalPaidAmount);
        if (payableAmount.compareTo(BigDecimal.ZERO) < 0)
        {
            if (remainAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                remainAmount = BigDecimal.ZERO;
            }
            finPayable.setRemainAmount(remainAmount);
            if (remainAmount.compareTo(BigDecimal.ZERO) >= 0)
            {
                finPayable.setStatus("2");
            }
            else if (totalPaidAmount.compareTo(BigDecimal.ZERO) < 0)
            {
                finPayable.setStatus("1");
            }
            else
            {
                finPayable.setStatus("0");
            }
        }
        else
        {
            if (remainAmount.compareTo(BigDecimal.ZERO) < 0)
            {
                remainAmount = BigDecimal.ZERO;
            }
            finPayable.setRemainAmount(remainAmount);
            if (remainAmount.compareTo(BigDecimal.ZERO) <= 0 && payableAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                finPayable.setStatus("2");
            }
            else if (totalPaidAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                finPayable.setStatus("1");
            }
            else
            {
                finPayable.setStatus("0");
            }
        }
        finPayableMapper.updateFinPayable(finPayable);
    }
}
