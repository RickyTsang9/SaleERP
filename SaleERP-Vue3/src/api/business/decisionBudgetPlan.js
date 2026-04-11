import request from '@/utils/request'

// 查询指定年度预算计划
export function getDecisionBudgetPlanCurrent(budgetYear) {
  return request({
    url: '/business/decisionBudgetPlan/current/' + budgetYear,
    method: 'get'
  })
}

// 新增年度预算计划
export function addDecisionBudgetPlan(data) {
  return request({
    url: '/business/decisionBudgetPlan',
    method: 'post',
    data: data
  })
}

// 修改年度预算计划
export function updateDecisionBudgetPlan(data) {
  return request({
    url: '/business/decisionBudgetPlan',
    method: 'put',
    data: data
  })
}

// 提交年度预算计划审批
export function submitDecisionBudgetPlan(planId) {
  return request({
    url: '/business/decisionBudgetPlan/submit/' + planId,
    method: 'post'
  })
}

// 审批通过年度预算计划
export function approveDecisionBudgetPlan(planId, data) {
  return request({
    url: '/business/decisionBudgetPlan/approve/' + planId,
    method: 'post',
    data: data
  })
}

// 驳回年度预算计划
export function rejectDecisionBudgetPlan(planId, data) {
  return request({
    url: '/business/decisionBudgetPlan/reject/' + planId,
    method: 'post',
    data: data
  })
}
