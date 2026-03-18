package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private FinReceiptMapper finReceiptMapper;

    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Override
    public FinReceipt selectFinReceiptById(Long receiptId)
    {
        return finReceiptMapper.selectFinReceiptById(receiptId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:receipt:list")
    public List<FinReceipt> selectFinReceiptList(FinReceipt finReceipt)
    {
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
}
