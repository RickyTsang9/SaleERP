package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.FinPayableMapper;
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.business.service.IFinPayableService;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 应付账款Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@Service
public class FinPayableServiceImpl implements IFinPayableService 
{
    @Autowired
    private FinPayableMapper finPayableMapper;

    @Autowired(required = false)
    private com.ruoyi.business.mapper.FinPaymentMapper finPaymentMapper;

    @Autowired(required = false)
    private com.ruoyi.business.service.support.BizComplianceHelper complianceHelper;

    /**
     * 查询应付账款
     * 
     * @param payableId 应付账款主键
     * @return 应付账款
     */
    @Override
    public FinPayable selectFinPayableByPayableId(Long payableId)
    {
        return finPayableMapper.selectFinPayableByPayableId(payableId);
    }

    /**
     * 查询应付账款列表
     * 
     * @param finPayable 应付账款
     * @return 应付账款
     */
    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:payable:list")
    public List<FinPayable> selectFinPayableList(FinPayable finPayable)
    {
        return finPayableMapper.selectFinPayableList(finPayable);
    }

    /**
     * 应付台账由业务单据自动生成，禁止手工新增主台账。
     * 
     * @param finPayable 应付账款
     * @return 结果
     */
    @Override
    public int insertFinPayable(FinPayable finPayable)
    {
        throw new ServiceException("应付台账由采购订单和付款记录自动维护，不支持手工新增");
    }

    /**
     * 仅允许调整应付台账的应付日期和备注，核心金额与状态由业务流程自动维护。
     * 
     * @param finPayable 应付账款
     * @return 结果
     */
    @Override
    public int updateFinPayable(FinPayable finPayable)
    {
        if (finPayable.getPayableId() == null)
        {
            throw new ServiceException("应付台账不存在");
        }
        FinPayable databasePayable = finPayableMapper.selectFinPayableByPayableId(finPayable.getPayableId());
        if (databasePayable == null)
        {
            throw new ServiceException("应付台账不存在");
        }
        FinPayable updatePayable = new FinPayable();
        updatePayable.setPayableId(databasePayable.getPayableId());
        updatePayable.setDueDate(finPayable.getDueDate());
        updatePayable.setRemark(finPayable.getRemark());
        try
        {
            updatePayable.setUpdateBy(SecurityUtils.getUsername());
        }
        catch (Exception exception)
        {
            // ignore
        }
        return finPayableMapper.updateFinPayableAdjustInfo(updatePayable);
    }

    /**
     * 应付台账由业务单据自动维护，禁止手工批量删除主台账。
     * 
     * @param payableIds 需要删除的应付账款主键
     * @return 结果
     */
    @Override
    public int deleteFinPayableByPayableIds(Long[] payableIds)
    {
        throw new ServiceException("应付台账由采购订单和付款记录自动维护，不支持手工删除");
    }

    /**
     * 应付台账由业务单据自动维护，禁止手工删除单条主台账。
     * 
     * @param payableId 应付账款主键
     * @return 结果
     */
    @Override
    public int deleteFinPayableByPayableId(Long payableId)
    {
        throw new ServiceException("应付台账由采购订单和付款记录自动维护，不支持手工删除");
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public java.math.BigDecimal offsetPayable(Long positivePayableId, Long negativePayableId, String remark, String operator)
    {
        if (positivePayableId == null || negativePayableId == null || positivePayableId.equals(negativePayableId))
        {
            throw new ServiceException("请选择一条正向待付台账与一条红字待退台账进行对冲");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new java.util.Date(), "应付红字对冲核销");
        }
        Long firstId = Math.min(positivePayableId, negativePayableId);
        Long secondId = Math.max(positivePayableId, negativePayableId);
        FinPayable firstLocked = finPayableMapper.selectFinPayableByPayableIdForUpdate(firstId);
        FinPayable secondLocked = finPayableMapper.selectFinPayableByPayableIdForUpdate(secondId);
        FinPayable pos = positivePayableId.equals(firstId) ? firstLocked : secondLocked;
        FinPayable neg = negativePayableId.equals(firstId) ? firstLocked : secondLocked;
        if (pos == null || neg == null)
        {
            throw new ServiceException("所选应付台账记录不存在");
        }
        if (pos.getSupplierId() == null || !pos.getSupplierId().equals(neg.getSupplierId()))
        {
            throw new ServiceException("仅同一供应商的应付与红字待退台账允许内部对冲核销");
        }
        java.math.BigDecimal posDue = pos.getAmountDue() == null ? java.math.BigDecimal.ZERO : pos.getAmountDue();
        java.math.BigDecimal posPaid = pos.getAmountPaid() == null ? java.math.BigDecimal.ZERO : pos.getAmountPaid();
        java.math.BigDecimal posRemain = posDue.subtract(posPaid);
        java.math.BigDecimal negDue = neg.getAmountDue() == null ? java.math.BigDecimal.ZERO : neg.getAmountDue();
        java.math.BigDecimal negPaid = neg.getAmountPaid() == null ? java.math.BigDecimal.ZERO : neg.getAmountPaid();
        java.math.BigDecimal negRemain = negDue.subtract(negPaid);
        if (posRemain.compareTo(java.math.BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("正向应付台账已结清，无需对冲");
        }
        if (negRemain.compareTo(java.math.BigDecimal.ZERO) >= 0)
        {
            throw new ServiceException("所选红字台账无可冲抵负额余款");
        }
        java.math.BigDecimal offsetAmt = posRemain.min(negRemain.abs()).setScale(2, java.math.RoundingMode.HALF_UP);
        java.util.Date now = new java.util.Date();
        String note = com.ruoyi.common.utils.StringUtils.isEmpty(remark)
            ? ("红字对冲核销(AP#" + positivePayableId + " <-> AP#" + negativePayableId + ")")
            : remark;

        if (finPaymentMapper != null)
        {
            com.ruoyi.business.domain.FinPayment posPm = new com.ruoyi.business.domain.FinPayment();
            posPm.setPayableId(pos.getPayableId());
            posPm.setPurchaseOrderId(pos.getPurchaseOrderId());
            posPm.setSupplierId(pos.getSupplierId());
            posPm.setAmount(offsetAmt);
            posPm.setPaymentTime(now);
            posPm.setPaymentMethod("offset");
            posPm.setStatus("normal");
            posPm.setCreateBy(operator);
            posPm.setCreateTime(now);
            posPm.setRemark(note);
            finPaymentMapper.insertFinPayment(posPm);

            com.ruoyi.business.domain.FinPayment negPm = new com.ruoyi.business.domain.FinPayment();
            negPm.setPayableId(neg.getPayableId());
            negPm.setPurchaseOrderId(neg.getPurchaseOrderId());
            negPm.setSupplierId(neg.getSupplierId());
            negPm.setAmount(offsetAmt.negate());
            negPm.setPaymentTime(now);
            negPm.setPaymentMethod("offset");
            negPm.setStatus("normal");
            negPm.setCreateBy(operator);
            negPm.setCreateTime(now);
            negPm.setRemark(note);
            finPaymentMapper.insertFinPayment(negPm);
        }

        java.math.BigDecimal newPosPaid = posPaid.add(offsetAmt);
        java.math.BigDecimal newPosRemain = posDue.subtract(newPosPaid);
        pos.setAmountPaid(newPosPaid);
        pos.setRemainAmount(newPosRemain);
        pos.setStatus(newPosRemain.compareTo(java.math.BigDecimal.ZERO) <= 0 ? "2" : "1");
        pos.setUpdateBy(operator);
        finPayableMapper.updateFinPayable(pos);

        java.math.BigDecimal newNegPaid = negPaid.subtract(offsetAmt);
        java.math.BigDecimal newNegRemain = negDue.subtract(newNegPaid);
        neg.setAmountPaid(newNegPaid);
        neg.setRemainAmount(newNegRemain);
        neg.setStatus(newNegRemain.compareTo(java.math.BigDecimal.ZERO) >= 0 ? "2" : "1");
        neg.setUpdateBy(operator);
        finPayableMapper.updateFinPayable(neg);
        return offsetAmt;
    }
}
