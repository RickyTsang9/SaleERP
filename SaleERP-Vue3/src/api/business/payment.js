import request from '@/utils/request'

// 查询付款记录列表
export function listPayment(query) {
  return request({
    url: '/business/payment/list',
    method: 'get',
    params: query
  })
}

// 查询付款记录详细
export function getPayment(paymentId) {
  return request({
    url: '/business/payment/' + paymentId,
    method: 'get'
  })
}

// 新增付款记录
export function addPayment(data) {
  return request({
    url: '/business/payment',
    method: 'post',
    data: data
  })
}

// 多单合并付款核销
export function batchAddPayment(data) {
  return request({
    url: '/business/payment/batch',
    method: 'post',
    data: data
  })
}

// 修改付款记录
export function updatePayment(data) {
  return request({
    url: '/business/payment',
    method: 'put',
    data: data
  })
}

// 删除付款记录
export function delPayment(paymentId) {
  return request({
    url: '/business/payment/' + paymentId,
    method: 'delete'
  })
}

// 导出付款记录
export function exportPayment(query) {
  return request({
    url: '/business/payment/export',
    method: 'post',
    params: query
  })
}

// 付款冲红作废
export function voidPayment(paymentId, voidReason) {
  return request({
    url: '/business/payment/' + paymentId + '/void',
    method: 'put',
    data: { voidReason }
  })
}