package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.service.IFinReceivableService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class FinReceivableServiceImpl implements IFinReceivableService
{
    @Autowired
    private FinReceivableMapper finReceivableMapper;

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
}
