package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
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
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;

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

    @Autowired
    private FinReceiptMapper finReceiptMapper;

    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFinReceipt(FinReceipt finReceipt)
    {
        if (finReceipt.getAmount() == null || finReceipt.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("回款金额必须大于0");
        }
        FinReceivable finReceivable = finReceivableMapper.selectFinReceivableById(finReceipt.getReceivableId());
        if (finReceivable == null)
        {
            throw new ServiceException("应收记录不存在");
        }
        if (finReceipt.getSaleOrderId() == null)
        {
            finReceipt.setSaleOrderId(finReceivable.getSaleOrderId());
        }
        if (finReceipt.getCustomerId() == null)
        {
            finReceipt.setCustomerId(finReceivable.getCustomerId());
        }
        BigDecimal currentPaidAmount = finReceivable.getAmountPaid() == null ? BigDecimal.ZERO : finReceivable.getAmountPaid();
        BigDecimal newPaidAmount = currentPaidAmount.add(finReceipt.getAmount());
        if (newPaidAmount.compareTo(finReceivable.getAmountDue()) > 0)
        {
            throw new ServiceException("回款金额超过应收余额");
        }
        String receivableStatus = newPaidAmount.compareTo(finReceivable.getAmountDue()) == 0 ? "paid" : "partial";
        finReceivableMapper.updateFinReceivablePaidAmount(finReceivable.getReceivableId(), newPaidAmount, receivableStatus);
        String saleOrderPaymentStatus = "partial";
        if ("paid".equals(receivableStatus))
        {
            saleOrderPaymentStatus = "paid";
        }
        wmsSaleOrderMapper.updateWmsSaleOrderPaymentStatus(finReceivable.getSaleOrderId(), saleOrderPaymentStatus);
        // 老版本 fin_receipt 表只保留回款日期，不具备回款方式字段时走兼容写入。
        if (useLegacyDateFinReceiptSchema())
        {
            return finReceiptMapper.insertFinReceiptLegacy(finReceipt);
        }
        // 兼容仅缺少回款方式字段的半升级结构，避免登记回款时写入失败。
        if (useWithoutMethodFinReceiptSchema())
        {
            return finReceiptMapper.insertFinReceiptWithoutMethod(finReceipt);
        }
        return finReceiptMapper.insertFinReceipt(finReceipt);
    }

    @Override
    public int updateFinReceipt(FinReceipt finReceipt)
    {
        throw new ServiceException("回款记录不允许修改，请删除后重建");
    }

    @Override
    public int deleteFinReceiptById(Long receiptId)
    {
        throw new ServiceException("回款记录不允许删除");
    }

    @Override
    public int deleteFinReceiptByIds(Long[] receiptIds)
    {
        throw new ServiceException("回款记录不允许删除");
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
        String countSql =
            "select count(1) from information_schema.columns where table_schema = (select database()) and table_name = ? and column_name = ?";
        Integer columnCount = jdbcTemplate.queryForObject(countSql, Integer.class, FIN_RECEIPT_TABLE_NAME, columnName);
        return columnCount != null && columnCount.intValue() > 0;
    }
}
