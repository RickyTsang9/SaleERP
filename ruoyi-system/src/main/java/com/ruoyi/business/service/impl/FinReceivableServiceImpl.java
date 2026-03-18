package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.service.IFinReceivableService;

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

    @Override
    public int insertFinReceivable(FinReceivable finReceivable)
    {
        return finReceivableMapper.insertFinReceivable(finReceivable);
    }

    @Override
    public int updateFinReceivable(FinReceivable finReceivable)
    {
        return finReceivableMapper.updateFinReceivable(finReceivable);
    }

    @Override
    public int deleteFinReceivableById(Long receivableId)
    {
        return finReceivableMapper.deleteFinReceivableById(receivableId);
    }

    @Override
    public int deleteFinReceivableByIds(Long[] receivableIds)
    {
        return finReceivableMapper.deleteFinReceivableByIds(receivableIds);
    }
}
