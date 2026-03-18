import request from '@/utils/request'

export function listPurchaseReturn(query) {
  return request({
    url: '/business/purchaseReturn/list',
    method: 'get',
    params: query
  })
}

export function getPurchaseReturn(purchaseReturnId) {
  return request({
    url: '/business/purchaseReturn/' + purchaseReturnId,
    method: 'get'
  })
}

export function addPurchaseReturn(data) {
  return request({
    url: '/business/purchaseReturn',
    method: 'post',
    data: data
  })
}

export function updatePurchaseReturn(data) {
  return request({
    url: '/business/purchaseReturn',
    method: 'put',
    data: data
  })
}

export function delPurchaseReturn(purchaseReturnId) {
  return request({
    url: '/business/purchaseReturn/' + purchaseReturnId,
    method: 'delete'
  })
}
