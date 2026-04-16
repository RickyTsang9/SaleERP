package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizDecisionBudgetPlan;

/**
 * 管理层年度预算计划Service接口
 * 
 * @author ruoyi
 * @date 2026-04-11
 */
public interface IBizDecisionBudgetPlanService
{
    /**
     * 查询指定年度预算计划
     * 
     * @param budgetYear 预算年度
     * @return 年度预算计划
     */
    public BizDecisionBudgetPlan selectCurrentBizDecisionBudgetPlan(Integer budgetYear);

    /**
     * 查询指定年度预算版本列表
     * 
     * @param budgetYear 预算年度
     * @return 年度预算版本列表
     */
    public List<BizDecisionBudgetPlan> selectBizDecisionBudgetPlanVersionList(Integer budgetYear);

    /**
     * 新增年度预算计划
     * 
     * @param bizDecisionBudgetPlan 年度预算计划
     * @param operatorName 操作人
     * @return 结果
     */
    public int insertBizDecisionBudgetPlan(BizDecisionBudgetPlan bizDecisionBudgetPlan, String operatorName);

    /**
     * 修改年度预算计划
     * 
     * @param bizDecisionBudgetPlan 年度预算计划
     * @param operatorName 操作人
     * @return 结果
     */
    public int updateBizDecisionBudgetPlan(BizDecisionBudgetPlan bizDecisionBudgetPlan, String operatorName);

    /**
     * 基于现有预算计划创建新版本
     * 
     * @param sourcePlanId 来源预算计划ID
     * @param bizDecisionBudgetPlan 新版本信息
     * @param operatorName 操作人
     * @return 新版本预算计划
     */
    public BizDecisionBudgetPlan createVersionBizDecisionBudgetPlan(Long sourcePlanId,
        BizDecisionBudgetPlan bizDecisionBudgetPlan, String operatorName);

    /**
     * 提交年度预算计划审批
     * 
     * @param planId 预算计划ID
     * @param operatorName 操作人
     * @return 结果
     */
    public int submitBizDecisionBudgetPlan(Long planId, String operatorName);

    /**
     * 审批通过年度预算计划
     * 
     * @param planId 预算计划ID
     * @param operatorName 操作人
     * @param approveRemark 审批意见
     * @return 结果
     */
    public int approveBizDecisionBudgetPlan(Long planId, String operatorName, String approveRemark);

    /**
     * 驳回年度预算计划
     * 
     * @param planId 预算计划ID
     * @param operatorName 操作人
     * @param approveRemark 审批意见
     * @return 结果
     */
    public int rejectBizDecisionBudgetPlan(Long planId, String operatorName, String approveRemark);
}
