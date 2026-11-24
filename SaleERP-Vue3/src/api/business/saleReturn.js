import request from '@/utils/request'

export function listSaleReturn(query) {
  return request({
    url: '/business/saleReturn/list',
    method: 'get',
    params: query
  })
}

export function getSaleReturn(saleReturnId) {
  return request({
    url: '/business/saleReturn/' + saleReturnId,
    method: 'get'
  })
}

export function addSaleReturn(data) {
  return request({
    url: '/business/saleReturn',
    method: 'post',
    data: data
  })
}

export function updateSaleReturn(data) {
  return request({
    url: '/business/saleReturn',
    method: 'put',
    data: data
  })
}

export function submitSaleReturn(saleReturnId) {
  return request({
    url: '/business/saleReturn/submit/' + saleReturnId,
    method: 'post'
  })
}

export function auditSaleReturn(saleReturnId) {
  return request({
    url: '/business/saleReturn/audit/' + saleReturnId,
    method: 'post'
  })
}

export function rejectSaleReturn(saleReturnId, rejectReason) {
  return request({
    url: '/business/saleReturn/reject/' + saleReturnId,
    method: 'post',
    data: { rejectReason }
  })
}

export function cancelSaleReturn(saleReturnId) {
  return request({
    url: '/business/saleReturn/cancel/' + saleReturnId,
    method: 'post'
  })
}

export function delSaleReturn(saleReturnId) {
  return request({
    url: '/business/saleReturn/' + saleReturnId,
    method: 'delete'
  })
}

export function batchSubmitSaleReturn(ids) {
  return request({
    url: '/business/saleReturn/batchSubmit',
    method: 'post',
    data: ids
  })
}

export function batchAuditSaleReturn(ids) {
  return request({
    url: '/business/saleReturn/batchAudit',
    method: 'post',
    data: ids
  })
}
