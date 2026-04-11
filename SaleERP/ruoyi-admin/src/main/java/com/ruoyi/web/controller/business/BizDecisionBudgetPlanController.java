package com.ruoyi.web.controller.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizDecisionBudgetPlan;
import com.ruoyi.business.service.IBizDecisionBudgetPlanService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;

/**
 * 管理层年度预算计划Controller
 * 
 * @author ruoyi
 * @date 2026-04-11
 */
@RestController
@RequestMapping("/business/decisionBudgetPlan")
public class BizDecisionBudgetPlanController extends BaseController
{
    @Autowired
    private IBizDecisionBudgetPlanService bizDecisionBudgetPlanService;

    /**
     * 查询指定年度预算计划
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/current/{budgetYear}")
    public AjaxResult current(@PathVariable Integer budgetYear)
    {
        return AjaxResult.success(bizDecisionBudgetPlanService.selectCurrentBizDecisionBudgetPlan(budgetYear));
    }

    /**
     * 新增年度预算计划
     */
    @PreAuthorize("@ss.hasPermi('business:report:edit')")
    @Log(title = "年度预算计划", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizDecisionBudgetPlan bizDecisionBudgetPlan)
    {
        return toAjax(bizDecisionBudgetPlanService.insertBizDecisionBudgetPlan(bizDecisionBudgetPlan, getUsername()));
    }

    /**
     * 修改年度预算计划
     */
    @PreAuthorize("@ss.hasPermi('business:report:edit')")
    @Log(title = "年度预算计划", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizDecisionBudgetPlan bizDecisionBudgetPlan)
    {
        return toAjax(bizDecisionBudgetPlanService.updateBizDecisionBudgetPlan(bizDecisionBudgetPlan, getUsername()));
    }

    /**
     * 提交年度预算计划审批
     */
    @PreAuthorize("@ss.hasPermi('business:report:edit')")
    @Log(title = "年度预算计划", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{planId}")
    public AjaxResult submit(@PathVariable Long planId)
    {
        return toAjax(bizDecisionBudgetPlanService.submitBizDecisionBudgetPlan(planId, getUsername()));
    }

    /**
     * 审批通过年度预算计划
     */
    @PreAuthorize("@ss.hasPermi('business:report:approve')")
    @Log(title = "年度预算计划", businessType = BusinessType.UPDATE)
    @PostMapping("/approve/{planId}")
    public AjaxResult approve(@PathVariable Long planId, @RequestBody(required = false) BizDecisionBudgetPlan bizDecisionBudgetPlan)
    {
        String approveRemark = bizDecisionBudgetPlan == null ? null : bizDecisionBudgetPlan.getApproveRemark();
        return toAjax(bizDecisionBudgetPlanService.approveBizDecisionBudgetPlan(planId, getUsername(), approveRemark));
    }

    /**
     * 驳回年度预算计划
     */
    @PreAuthorize("@ss.hasPermi('business:report:approve')")
    @Log(title = "年度预算计划", businessType = BusinessType.UPDATE)
    @PostMapping("/reject/{planId}")
    public AjaxResult reject(@PathVariable Long planId, @RequestBody(required = false) BizDecisionBudgetPlan bizDecisionBudgetPlan)
    {
        String approveRemark = bizDecisionBudgetPlan == null ? null : bizDecisionBudgetPlan.getApproveRemark();
        return toAjax(bizDecisionBudgetPlanService.rejectBizDecisionBudgetPlan(planId, getUsername(), approveRemark));
    }
}
