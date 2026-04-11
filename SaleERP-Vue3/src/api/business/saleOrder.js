import request from '@/utils/request'

export function listSaleOrder(query) {
  return request({
    url: '/business/saleOrder/list',
    method: 'get',
    params: query
  })
}

export function getSaleOrder(saleOrderId) {
  return request({
    url: '/business/saleOrder/' + saleOrderId,
    method: 'get'
  })
}

export function addSaleOrder(data) {
  return request({
    url: '/business/saleOrder',
    method: 'post',
    data: data
  })
}

export function updateSaleOrder(data) {
  return request({
    url: '/business/saleOrder',
    method: 'put',
    data: data
  })
}

export function delSaleOrder(saleOrderId) {
  return request({
    url: '/business/saleOrder/' + saleOrderId,
    method: 'delete'
  })
}

export function submitSaleOrder(saleOrderId) {
  return request({
    url: '/business/saleOrder/submit/' + saleOrderId,
    method: 'post'
  })
}

export function managerAuditSaleOrder(saleOrderId, data) {
  return request({
    url: '/business/saleOrder/managerAudit/' + saleOrderId,
    method: 'post',
    data: data
  })
}

export function managerRollbackSaleOrder(saleOrderId, data) {
  return request({
    url: '/business/saleOrder/managerRollback/' + saleOrderId,
    method: 'post',
    data: data
  })
}

export function financeAuditSaleOrder(saleOrderId, data) {
  return request({
    url: '/business/saleOrder/financeAudit/' + saleOrderId,
    method: 'post',
    data: data
  })
}

export function financeRollbackSaleOrder(saleOrderId, data) {
  return request({
    url: '/business/saleOrder/financeRollback/' + saleOrderId,
    method: 'post',
    data: data
  })
}

export function cancelSaleOrder(saleOrderId) {
  return request({
    url: '/business/saleOrder/cancel/' + saleOrderId,
    method: 'post'
  })
}

export function getSaleOrderPrintTemplate(saleOrderId) {
  return request({
    url: '/business/saleOrder/printTemplate/' + saleOrderId,
    method: 'get'
  })
}

export function listSaleOrderStatusHistory(saleOrderId) {
  return request({
    url: '/business/saleOrder/statusHistory/' + saleOrderId,
    method: 'get'
  })
}

export function importSaleOrderTemplate() {
  return request({
    url: '/business/saleOrder/importTemplate',
    method: 'post',
    responseType: 'blob'
  })
}
