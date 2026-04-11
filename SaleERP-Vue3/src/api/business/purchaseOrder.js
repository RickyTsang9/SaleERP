import request from '@/utils/request'

// 查询采购订单列表
export function listPurchaseOrder(query) {
  return request({
    url: '/business/purchaseOrder/list',
    method: 'get',
    params: query
  })
}

// 查询采购订单详细
export function getPurchaseOrder(purchaseOrderId) {
  return request({
    url: '/business/purchaseOrder/' + purchaseOrderId,
    method: 'get'
  })
}

// 新增采购订单
export function addPurchaseOrder(data) {
  return request({
    url: '/business/purchaseOrder',
    method: 'post',
    data: data
  })
}

// 修改采购订单
export function updatePurchaseOrder(data) {
  return request({
    url: '/business/purchaseOrder',
    method: 'put',
    data: data
  })
}

// 提交采购订单
export function submitPurchaseOrder(purchaseOrderId) {
  return request({
    url: '/business/purchaseOrder/submit/' + purchaseOrderId,
    method: 'post'
  })
}

// 审核采购订单
export function auditPurchaseOrder(purchaseOrderId) {
  return request({
    url: '/business/purchaseOrder/audit/' + purchaseOrderId,
    method: 'post'
  })
}

// 作废采购订单
export function cancelPurchaseOrder(purchaseOrderId) {
  return request({
    url: '/business/purchaseOrder/cancel/' + purchaseOrderId,
    method: 'post'
  })
}

// 删除采购订单
export function delPurchaseOrder(purchaseOrderId) {
  return request({
    url: '/business/purchaseOrder/' + purchaseOrderId,
    method: 'delete'
  })
}

// 导出采购订单
export function exportPurchaseOrder(query) {
  return request({
    url: '/business/purchaseOrder/export',
    method: 'post',
    params: query
  })
}
