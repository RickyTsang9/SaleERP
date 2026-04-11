package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.CrmCustomerFollow;

/**
 * 客户跟进记录Service接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface ICrmCustomerFollowService 
{
    /**
     * 查询客户跟进记录
     * 
     * @param followId 客户跟进记录主键
     * @return 客户跟进记录
     */
    public CrmCustomerFollow selectCrmCustomerFollowByFollowId(Long followId);

    /**
     * 查询客户跟进记录列表
     * 
     * @param crmCustomerFollow 客户跟进记录
     * @return 客户跟进记录集合
     */
    public List<CrmCustomerFollow> selectCrmCustomerFollowList(CrmCustomerFollow crmCustomerFollow);

    /**
     * 查询客户跟进待办摘要
     * 
     * @param crmCustomerFollow 客户跟进记录筛选条件
     * @return 客户跟进待办摘要
     */
    public Map<String, Object> selectCrmCustomerFollowTodoSummary(CrmCustomerFollow crmCustomerFollow);

    /**
     * 新增客户跟进记录
     * 
     * @param crmCustomerFollow 客户跟进记录
     * @return 结果
     */
    public int insertCrmCustomerFollow(CrmCustomerFollow crmCustomerFollow);

    /**
     * 修改客户跟进记录
     * 
     * @param crmCustomerFollow 客户跟进记录
     * @return 结果
     */
    public int updateCrmCustomerFollow(CrmCustomerFollow crmCustomerFollow);

    /**
     * 批量删除客户跟进记录
     * 
     * @param followIds 需要删除的客户跟进记录主键集合
     * @return 结果
     */
    public int deleteCrmCustomerFollowByFollowIds(Long[] followIds);

    /**
     * 删除客户跟进记录信息
     * 
     * @param followId 客户跟进记录主键
     * @return 结果
     */
    public int deleteCrmCustomerFollowByFollowId(Long followId);
}
