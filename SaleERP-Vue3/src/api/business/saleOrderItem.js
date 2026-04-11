import request from '@/utils/request'

export function listSaleOrderItem(query) {
  return request({
    url: '/business/saleOrderItem/list',
    method: 'get',
    params: query
  })
}

export function getSaleOrderItem(saleOrderItemId) {
  return request({
    url: '/business/saleOrderItem/' + saleOrderItemId,
    method: 'get'
  })
}

export function addSaleOrderItem(data) {
  return request({
    url: '/business/saleOrderItem',
    method: 'post',
    data: data
  })
}

export function updateSaleOrderItem(data) {
  return request({
    url: '/business/saleOrderItem',
    method: 'put',
    data: data
  })
}

export function delSaleOrderItem(saleOrderItemId) {
  return request({
    url: '/business/saleOrderItem/' + saleOrderItemId,
    method: 'delete'
  })
}
