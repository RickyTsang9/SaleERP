import request from '@/utils/request'

export function listPurchaseReturnItem(query) {
  return request({
    url: '/business/purchaseReturnItem/list',
    method: 'get',
    params: query
  })
}

export function getPurchaseReturnItem(purchaseReturnItemId) {
  return request({
    url: '/business/purchaseReturnItem/' + purchaseReturnItemId,
    method: 'get'
  })
}

export function addPurchaseReturnItem(data) {
  return request({
    url: '/business/purchaseReturnItem',
    method: 'post',
    data: data
  })
}

export function updatePurchaseReturnItem(data) {
  return request({
    url: '/business/purchaseReturnItem',
    method: 'put',
    data: data
  })
}

export function delPurchaseReturnItem(purchaseReturnItemId) {
  return request({
    url: '/business/purchaseReturnItem/' + purchaseReturnItemId,
    method: 'delete'
  })
}
