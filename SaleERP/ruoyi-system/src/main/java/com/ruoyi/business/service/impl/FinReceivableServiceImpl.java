package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.service.IFinReceivableService;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;

@Service
public class FinReceivableServiceImpl implements IFinReceivableService
{
    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired(required = false)
    private com.ruoyi.business.mapper.FinReceiptMapper finReceiptMapper;

    @Autowired(required = false)
    private com.ruoyi.business.mapper.WmsSaleOrderMapper wmsSaleOrderMapper;

    @Autowired(required = false)
    private com.ruoyi.business.service.support.BizComplianceHelper complianceHelper;

    @Override
    public FinReceivable selectFinReceivableById(Long receivableId)
    {
        return finReceivableMapper.selectFinReceivableById(receivableId);
    }

    @Override
    public FinReceivable selectFinReceivableBySaleOrderId(Long saleOrderId)
    {
        return finReceivableMapper.selectFinReceivableBySaleOrderId(saleOrderId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:receivable:list")
    public List<FinReceivable> selectFinReceivableList(FinReceivable finReceivable)
    {
        return finReceivableMapper.selectFinReceivableList(finReceivable);
    }

    @Override
    public List<FinReceivable> selectFinReceivableDueReminderList(Integer remindDays)
    {
        return finReceivableMapper.selectFinReceivableDueReminderList(remindDays);
    }

    @Override
    public List<FinReceivable> selectFinReceivableOverdueList()
    {
        return finReceivableMapper.selectFinReceivableOverdueList();
    }

    /**
     * 应收台账由业务单据自动生成，禁止手工新增主台账。
     */
    @Override
    public int insertFinReceivable(FinReceivable finReceivable)
    {
        throw new ServiceException("应收台账由销售单和回款记录自动维护，不支持手工新增");
    }

    /**
     * 仅允许调整应收台账的到期日期和备注，核心金额与状态由业务流程自动维护。
     */
    @Override
    public int updateFinReceivable(FinReceivable finReceivable)
    {
        if (finReceivable.getReceivableId() == null)
        {
            throw new ServiceException("应收台账不存在");
        }
        FinReceivable databaseReceivable = finReceivableMapper.selectFinReceivableById(finReceivable.getReceivableId());
        if (databaseReceivable == null)
        {
            throw new ServiceException("应收台账不存在");
        }
        FinReceivable updateReceivable = new FinReceivable();
        updateReceivable.setReceivableId(databaseReceivable.getReceivableId());
        updateReceivable.setDueDate(finReceivable.getDueDate());
        updateReceivable.setRemark(finReceivable.getRemark());
        updateReceivable.setUpdateBy(finReceivable.getUpdateBy());
        return finReceivableMapper.updateFinReceivableAdjustInfo(updateReceivable);
    }

    /**
     * 应收台账由业务单据自动维护，禁止手工删除单条主台账。
     */
    @Override
    public int deleteFinReceivableById(Long receivableId)
    {
        throw new ServiceException("应收台账由销售单和回款记录自动维护，不支持手工删除");
    }

    /**
     * 应收台账由业务单据自动维护，禁止手工批量删除主台账。
     */
    @Override
    public int deleteFinReceivableByIds(Long[] receivableIds)
    {
        throw new ServiceException("应收台账由销售单和回款记录自动维护，不支持手工删除");
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public java.math.BigDecimal offsetReceivable(Long positiveReceivableId, Long negativeReceivableId, String remark, String operator)
    {
        if (positiveReceivableId == null || negativeReceivableId == null || positiveReceivableId.equals(negativeReceivableId))
        {
            throw new ServiceException("请选择一条正向待收台账与一条红字待退台账进行对冲");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new java.util.Date(), "应收红字对冲核销");
        }
        Long firstId = Math.min(positiveReceivableId, negativeReceivableId);
        Long secondId = Math.max(positiveReceivableId, negativeReceivableId);
        FinReceivable firstLocked = finReceivableMapper.selectFinReceivableByIdForUpdate(firstId);
        FinReceivable secondLocked = finReceivableMapper.selectFinReceivableByIdForUpdate(secondId);
        FinReceivable pos = positiveReceivableId.equals(firstId) ? firstLocked : secondLocked;
        FinReceivable neg = negativeReceivableId.equals(firstId) ? firstLocked : secondLocked;
        if (pos == null || neg == null)
        {
            throw new ServiceException("所选应收台账记录不存在");
        }
        if (pos.getCustomerId() == null || !pos.getCustomerId().equals(neg.getCustomerId()))
        {
            throw new ServiceException("仅同一客户的应收与红字待退台账允许内部对冲核销");
        }
        java.math.BigDecimal posDue = pos.getAmountDue() == null ? java.math.BigDecimal.ZERO : pos.getAmountDue();
        java.math.BigDecimal posPaid = pos.getAmountPaid() == null ? java.math.BigDecimal.ZERO : pos.getAmountPaid();
        java.math.BigDecimal posRemain = posDue.subtract(posPaid);
        java.math.BigDecimal negDue = neg.getAmountDue() == null ? java.math.BigDecimal.ZERO : neg.getAmountDue();
        java.math.BigDecimal negPaid = neg.getAmountPaid() == null ? java.math.BigDecimal.ZERO : neg.getAmountPaid();
        java.math.BigDecimal negRemain = negDue.subtract(negPaid);
        if (posRemain.compareTo(java.math.BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("正向应收台账已结清，无需对冲");
        }
        if (negRemain.compareTo(java.math.BigDecimal.ZERO) >= 0)
        {
            throw new ServiceException("所选红字台账无可冲抵负额余款");
        }
        java.math.BigDecimal offsetAmt = posRemain.min(negRemain.abs()).setScale(2, java.math.RoundingMode.HALF_UP);
        java.util.Date now = new java.util.Date();
        String note = com.ruoyi.common.utils.StringUtils.isEmpty(remark)
            ? ("红字对冲核销(AR#" + positiveReceivableId + " <-> AR#" + negativeReceivableId + ")")
            : remark;

        if (finReceiptMapper != null)
        {
            com.ruoyi.business.domain.FinReceipt posRec = new com.ruoyi.business.domain.FinReceipt();
            posRec.setReceivableId(pos.getReceivableId());
            posRec.setSaleOrderId(pos.getSaleOrderId());
            posRec.setCustomerId(pos.getCustomerId());
            posRec.setAmount(offsetAmt);
            posRec.setPaymentTime(now);
            posRec.setPaymentMethod("offset");
            posRec.setStatus("normal");
            posRec.setCreateBy(operator);
            posRec.setRemark(note);
            finReceiptMapper.insertFinReceipt(posRec);

            com.ruoyi.business.domain.FinReceipt negRec = new com.ruoyi.business.domain.FinReceipt();
            negRec.setReceivableId(neg.getReceivableId());
            negRec.setSaleOrderId(neg.getSaleOrderId());
            negRec.setCustomerId(neg.getCustomerId());
            negRec.setAmount(offsetAmt.negate());
            negRec.setPaymentTime(now);
            negRec.setPaymentMethod("offset");
            negRec.setStatus("normal");
            negRec.setCreateBy(operator);
            negRec.setRemark(note);
            finReceiptMapper.insertFinReceipt(negRec);
        }

        java.math.BigDecimal newPosPaid = posPaid.add(offsetAmt);
        String newPosStatus = newPosPaid.compareTo(posDue) >= 0 ? "paid" : "partial";
        finReceivableMapper.updateFinReceivablePaidAmount(pos.getReceivableId(), newPosPaid, newPosStatus);
        if (pos.getSaleOrderId() != null && wmsSaleOrderMapper != null)
        {
            wmsSaleOrderMapper.updateWmsSaleOrderPaymentStatus(pos.getSaleOrderId(), newPosStatus);
        }

        java.math.BigDecimal newNegPaid = negPaid.subtract(offsetAmt);
        String newNegStatus = newNegPaid.compareTo(negDue) <= 0 ? "paid" : "partial";
        finReceivableMapper.updateFinReceivablePaidAmount(neg.getReceivableId(), newNegPaid, newNegStatus);
        return offsetAmt;
    }
}
