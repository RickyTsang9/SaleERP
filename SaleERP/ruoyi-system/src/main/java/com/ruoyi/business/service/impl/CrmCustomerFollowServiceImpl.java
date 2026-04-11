package com.ruoyi.business.service.impl;

import java.util.List;
import java.util.Map;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.CrmCustomerFollowMapper;
import com.ruoyi.business.domain.CrmCustomerFollow;
import com.ruoyi.business.service.ICrmCustomerFollowService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 客户跟进记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@Service
public class CrmCustomerFollowServiceImpl implements ICrmCustomerFollowService 
{
    @Autowired
    private CrmCustomerFollowMapper crmCustomerFollowMapper;

    /**
     * 查询客户跟进记录
     * 
     * @param followId 客户跟进记录主键
     * @return 客户跟进记录
     */
    @Override
    public CrmCustomerFollow selectCrmCustomerFollowByFollowId(Long followId)
    {
        return crmCustomerFollowMapper.selectCrmCustomerFollowByFollowId(followId);
    }

    /**
     * 查询客户跟进记录列表
     * 
     * @param crmCustomerFollow 客户跟进记录
     * @return 客户跟进记录
     */
    @Override
    public List<CrmCustomerFollow> selectCrmCustomerFollowList(CrmCustomerFollow crmCustomerFollow)
    {
        return crmCustomerFollowMapper.selectCrmCustomerFollowList(crmCustomerFollow);
    }

    /**
     * 查询客户跟进待办摘要
     * 
     * @param crmCustomerFollow 客户跟进记录筛选条件
     * @return 客户跟进待办摘要
     */
    @Override
    public Map<String, Object> selectCrmCustomerFollowTodoSummary(CrmCustomerFollow crmCustomerFollow)
    {
        return crmCustomerFollowMapper.selectCrmCustomerFollowTodoSummary(crmCustomerFollow);
    }

    /**
     * 新增客户跟进记录
     * 
     * @param crmCustomerFollow 客户跟进记录
     * @return 结果
     */
    @Override
    public int insertCrmCustomerFollow(CrmCustomerFollow crmCustomerFollow)
    {
        crmCustomerFollow.setCreateTime(DateUtils.getNowDate());
        try {
            crmCustomerFollow.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            // ignore
        }
        return crmCustomerFollowMapper.insertCrmCustomerFollow(crmCustomerFollow);
    }

    /**
     * 修改客户跟进记录
     * 
     * @param crmCustomerFollow 客户跟进记录
     * @return 结果
     */
    @Override
    public int updateCrmCustomerFollow(CrmCustomerFollow crmCustomerFollow)
    {
        crmCustomerFollow.setUpdateTime(DateUtils.getNowDate());
        try {
            crmCustomerFollow.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            // ignore
        }
        return crmCustomerFollowMapper.updateCrmCustomerFollow(crmCustomerFollow);
    }

    /**
     * 批量删除客户跟进记录
     * 
     * @param followIds 需要删除的客户跟进记录主键
     * @return 结果
     */
    @Override
    public int deleteCrmCustomerFollowByFollowIds(Long[] followIds)
    {
        return crmCustomerFollowMapper.deleteCrmCustomerFollowByFollowIds(followIds);
    }

    /**
     * 删除客户跟进记录信息
     * 
     * @param followId 客户跟进记录主键
     * @return 结果
     */
    @Override
    public int deleteCrmCustomerFollowByFollowId(Long followId)
    {
        return crmCustomerFollowMapper.deleteCrmCustomerFollowByFollowId(followId);
    }
}
