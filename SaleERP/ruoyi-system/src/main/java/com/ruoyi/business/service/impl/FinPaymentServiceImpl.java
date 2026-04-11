package com.ruoyi.business.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import com.ruoyi.business.mapper.FinPaymentMapper;
import com.ruoyi.business.domain.FinPayment;
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.business.mapper.FinPayableMapper;
import com.ruoyi.business.service.IFinPaymentService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 付款记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@Service
public class FinPaymentServiceImpl implements IFinPaymentService 
{
    @Autowired
    private FinPaymentMapper finPaymentMapper;
    
    @Autowired
    private FinPayableMapper finPayableMapper;

    /**
     * 查询付款记录
     * 
     * @param paymentId 付款记录主键
     * @return 付款记录
     */
    @Override
    public FinPayment selectFinPaymentByPaymentId(Long paymentId)
    {
        return finPaymentMapper.selectFinPaymentByPaymentId(paymentId);
    }

    /**
     * 查询付款记录列表
     * 
     * @param finPayment 付款记录
     * @return 付款记录
     */
    @Override
    public List<FinPayment> selectFinPaymentList(FinPayment finPayment)
    {
        return finPaymentMapper.selectFinPaymentList(finPayment);
    }

    /**
     * 新增付款记录
     * 
     * @param finPayment 付款记录
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertFinPayment(FinPayment finPayment)
    {
        if (finPayment.getAmount() == null || finPayment.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("付款金额必须大于0");
        }
        FinPayable finPayable = finPayableMapper.selectFinPayableByPayableId(finPayment.getPayableId());
        if (finPayable == null)
        {
            throw new ServiceException("应付账款不存在");
        }
        if (finPayment.getPurchaseOrderId() == null)
        {
            finPayment.setPurchaseOrderId(finPayable.getPurchaseOrderId());
        }
        if (finPayment.getSupplierId() == null)
        {
            finPayment.setSupplierId(finPayable.getSupplierId());
        }
        BigDecimal currentPaidAmount = finPayable.getAmountPaid() == null ? BigDecimal.ZERO : finPayable.getAmountPaid();
        BigDecimal payableAmount = finPayable.getAmountDue() == null ? BigDecimal.ZERO : finPayable.getAmountDue();
        BigDecimal newPaidAmount = currentPaidAmount.add(finPayment.getAmount());
        if (newPaidAmount.compareTo(payableAmount) > 0)
        {
            throw new ServiceException("付款金额超过应付余额");
        }
        finPayment.setCreateTime(DateUtils.getNowDate());
        try
        {
            finPayment.setCreateBy(SecurityUtils.getUsername());
        }
        catch (Exception exception)
        {
            // 当前场景下创建人获取失败不影响付款登记主流程。
        }
        
        int insertRows = finPaymentMapper.insertFinPayment(finPayment);
        
        // 付款登记成功后立即重算应付账款的金额和状态，保证台账口径一致。
        updatePayableStatus(finPayment.getPayableId());
        
        return insertRows;
    }

    /**
     * 修改付款记录
     * 
     * @param finPayment 付款记录
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateFinPayment(FinPayment finPayment)
    {
        throw new ServiceException("付款记录不允许修改，请通过应付台账补充新的付款登记");
    }

    /**
     * 批量删除付款记录
     * 
     * @param paymentIds 需要删除的付款记录主键
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteFinPaymentByPaymentIds(Long[] paymentIds)
    {
        throw new ServiceException("付款记录不允许删除");
    }

    /**
     * 删除付款记录信息
     * 
     * @param paymentId 付款记录主键
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteFinPaymentByPaymentId(Long paymentId)
    {
        throw new ServiceException("付款记录不允许删除");
    }
    
    /**
     * 重新计算并更新应付账款的金额和状态
     */
    private void updatePayableStatus(Long payableId)
    {
        if (payableId == null)
        {
            return;
        }
        
        FinPayable finPayable = finPayableMapper.selectFinPayableByPayableId(payableId);
        if (finPayable == null)
        {
            return;
        }
        
        FinPayment paymentQuery = new FinPayment();
        paymentQuery.setPayableId(payableId);
        List<FinPayment> paymentRecordList = finPaymentMapper.selectFinPaymentList(paymentQuery);
        
        BigDecimal totalPaidAmount = BigDecimal.ZERO;
        for (FinPayment paymentRecord : paymentRecordList)
        {
            if (paymentRecord.getAmount() != null)
            {
                totalPaidAmount = totalPaidAmount.add(paymentRecord.getAmount());
            }
        }
        
        finPayable.setAmountPaid(totalPaidAmount);
        BigDecimal remainAmount = finPayable.getAmountDue().subtract(totalPaidAmount);
        finPayable.setRemainAmount(remainAmount);
        
        if (remainAmount.compareTo(BigDecimal.ZERO) <= 0)
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
        
        finPayableMapper.updateFinPayable(finPayable);
    }
}
