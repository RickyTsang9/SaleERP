package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.BizDecisionBudgetPlan;

/**
 * 管理层年度预算计划Mapper接口
 * 
 * @author ruoyi
 * @date 2026-04-11
 */
public interface BizDecisionBudgetPlanMapper
{
    /**
     * 查询年度预算计划
     * 
     * @param planId 预算计划ID
     * @return 年度预算计划
     */
    public BizDecisionBudgetPlan selectBizDecisionBudgetPlanByPlanId(Long planId);

    /**
     * 按年度查询预算计划
     * 
     * @param budgetYear 预算年度
     * @return 年度预算计划
     */
    public BizDecisionBudgetPlan selectBizDecisionBudgetPlanByBudgetYear(Integer budgetYear);

    /**
     * 新增年度预算计划
     * 
     * @param bizDecisionBudgetPlan 年度预算计划
     * @return 结果
     */
    public int insertBizDecisionBudgetPlan(BizDecisionBudgetPlan bizDecisionBudgetPlan);

    /**
     * 修改年度预算计划
     * 
     * @param bizDecisionBudgetPlan 年度预算计划
     * @return 结果
     */
    public int updateBizDecisionBudgetPlan(BizDecisionBudgetPlan bizDecisionBudgetPlan);
}
