package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.util.Date;
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
     * 多单合并付款核销（按所选应付台账顺序依次核销）
     */
    public int batchInsertFinPayment(List<Long> payableIds, BigDecimal totalAmount, String paymentMethod,
                                     Long accountId, Date paymentTime, String remark);

    public int voidFinPayment(Long paymentId, String voidReason);

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