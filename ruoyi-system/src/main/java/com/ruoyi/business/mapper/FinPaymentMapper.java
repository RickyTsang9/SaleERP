package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.FinPayment;

/**
 * 付款记录Mapper接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface FinPaymentMapper 
{
    /**
     * 查询付款记录
     * 
     * @param paymentId 付款记录主键
     * @return 付款记录
     */
    public FinPayment selectFinPaymentByPaymentId(Long paymentId);

    /**
     * 查询付款记录列表
     * 
     * @param finPayment 付款记录
     * @return 付款记录集合
     */
    public List<FinPayment> selectFinPaymentList(FinPayment finPayment);

    /**
     * 根据应付账款查询付款总金额
     * 
     * @param payableId 应付账款主键
     * @return 付款总金额
     */
    public BigDecimal selectPaymentAmountSumByPayableId(Long payableId);

    /**
     * 新增付款记录
     * 
     * @param finPayment 付款记录
     * @return 结果
     */
    public int insertFinPayment(FinPayment finPayment);

    /**
     * 修改付款记录
     * 
     * @param finPayment 付款记录
     * @return 结果
     */
    public int updateFinPayment(FinPayment finPayment);

    /**
     * 删除付款记录
     * 
     * @param paymentId 付款记录主键
     * @return 结果
     */
    public int deleteFinPaymentByPaymentId(Long paymentId);

    /**
     * 批量删除付款记录
     * 
     * @param paymentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFinPaymentByPaymentIds(Long[] paymentIds);
}
