import request from '@/utils/request'

export function listSaleReturnItem(query) {
  return request({
    url: '/business/saleReturnItem/list',
    method: 'get',
    params: query
  })
}

export function getSaleReturnItem(saleReturnItemId) {
  return request({
    url: '/business/saleReturnItem/' + saleReturnItemId,
    method: 'get'
  })
}

export function addSaleReturnItem(data) {
  return request({
    url: '/business/saleReturnItem',
    method: 'post',
    data: data
  })
}

export function updateSaleReturnItem(data) {
  return request({
    url: '/business/saleReturnItem',
    method: 'put',
    data: data
  })
}

export function delSaleReturnItem(saleReturnItemId) {
  return request({
    url: '/business/saleReturnItem/' + saleReturnItemId,
    method: 'delete'
  })
}
