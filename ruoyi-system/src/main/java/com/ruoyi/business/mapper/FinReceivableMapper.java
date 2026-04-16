package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.FinReceivable;

public interface FinReceivableMapper
{
    /**
     * 查询应收台账
     *
     * @param receivableId 应收台账主键
     * @return 应收台账
     */
    public FinReceivable selectFinReceivableById(Long receivableId);

    /**
     * 根据销售单查询应收台账
     *
     * @param saleOrderId 销售单主键
     * @return 应收台账
     */
    public FinReceivable selectFinReceivableBySaleOrderId(Long saleOrderId);

    /**
     * 查询应收台账列表
     *
     * @param finReceivable 应收台账查询条件
     * @return 应收台账集合
     */
    public List<FinReceivable> selectFinReceivableList(FinReceivable finReceivable);

    /**
     * 根据客户查询待冲减应收台账列表
     *
     * @param customerId 客户主键
     * @return 待冲减应收台账集合
     */
    public List<FinReceivable> selectPendingFinReceivableListByCustomerId(Long customerId);

    /**
     * 查询应收到期提醒列表
     *
     * @param remindDays 提醒天数
     * @return 应收到期提醒集合
     */
    public List<FinReceivable> selectFinReceivableDueReminderList(Integer remindDays);

    /**
     * 查询逾期应收台账列表
     *
     * @return 逾期应收台账集合
     */
    public List<FinReceivable> selectFinReceivableOverdueList();

    /**
     * 查询客户未收金额
     *
     * @param customerId 客户主键
     * @return 客户未收金额
     */
    public BigDecimal selectCustomerOutstandingAmount(Long customerId);

    /**
     * 新增应收台账
     *
     * @param finReceivable 应收台账
     * @return 结果
     */
    public int insertFinReceivable(FinReceivable finReceivable);

    /**
     * 修改应收台账
     *
     * @param finReceivable 应收台账
     * @return 结果
     */
    public int updateFinReceivable(FinReceivable finReceivable);

    /**
     * 调整应收台账基础信息
     *
     * @param finReceivable 应收台账
     * @return 结果
     */
    public int updateFinReceivableAdjustInfo(FinReceivable finReceivable);

    /**
     * 更新应收台账已收金额
     *
     * @param receivableId 应收台账主键
     * @param amountPaid 已收金额
     * @param status 应收状态
     * @return 结果
     */
    public int updateFinReceivablePaidAmount(Long receivableId, BigDecimal amountPaid, String status);

    /**
     * 更新应收台账金额信息
     *
     * @param receivableId 应收台账主键
     * @param amountDue 应收金额
     * @param amountPaid 已收金额
     * @param status 应收状态
     * @return 结果
     */
    public int updateFinReceivableAmounts(Long receivableId, BigDecimal amountDue, BigDecimal amountPaid, String status);

    /**
     * 删除应收台账
     *
     * @param receivableId 应收台账主键
     * @return 结果
     */
    public int deleteFinReceivableById(Long receivableId);

    /**
     * 批量删除应收台账
     *
     * @param receivableIds 应收台账主键集合
     * @return 结果
     */
    public int deleteFinReceivableByIds(Long[] receivableIds);
}
