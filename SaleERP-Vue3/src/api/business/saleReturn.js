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
