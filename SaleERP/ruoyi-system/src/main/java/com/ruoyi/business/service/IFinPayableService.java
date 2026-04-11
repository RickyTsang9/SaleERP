package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.FinPayable;

/**
 * 应付账款Service接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface IFinPayableService 
{
    /**
     * 查询应付账款
     * 
     * @param payableId 应付账款主键
     * @return 应付账款
     */
    public FinPayable selectFinPayableByPayableId(Long payableId);

    /**
     * 查询应付账款列表
     * 
     * @param finPayable 应付账款
     * @return 应付账款集合
     */
    public List<FinPayable> selectFinPayableList(FinPayable finPayable);

    /**
     * 新增应付账款
     * 
     * @param finPayable 应付账款
     * @return 结果
     */
    public int insertFinPayable(FinPayable finPayable);

    /**
     * 调整应付账款
     * 
     * @param finPayable 应付账款
     * @return 结果
     */
    public int updateFinPayable(FinPayable finPayable);

    /**
     * 批量删除应付账款
     * 
     * @param payableIds 需要删除的应付账款主键集合
     * @return 结果
     */
    public int deleteFinPayableByPayableIds(Long[] payableIds);

    /**
     * 删除应付账款信息
     * 
     * @param payableId 应付账款主键
     * @return 结果
     */
    public int deleteFinPayableByPayableId(Long payableId);
}
