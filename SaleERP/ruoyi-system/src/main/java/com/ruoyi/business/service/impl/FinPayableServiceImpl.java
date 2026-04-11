package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.FinPayableMapper;
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.business.service.IFinPayableService;
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
}
