package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.FinPayment;

/**
 * 付款记录Service接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface IFinPaymentService 
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
     * 批量删除付款记录
     * 
     * @param paymentIds 需要删除的付款记录主键集合
     * @return 结果
     */
    public int deleteFinPaymentByPaymentIds(Long[] paymentIds);

    /**
     * 删除付款记录信息
     * 
     * @param paymentId 付款记录主键
     * @return 结果
     */
    public int deleteFinPaymentByPaymentId(Long paymentId);
}