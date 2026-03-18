import request from '@/utils/request'

export function listReceipt(query) {
  return request({
    url: '/business/receipt/list',
    method: 'get',
    params: query
  })
}

export function getReceipt(receiptId) {
  return request({
    url: '/business/receipt/' + receiptId,
    method: 'get'
  })
}

export function addReceipt(data) {
  return request({
    url: '/business/receipt',
    method: 'post',
    data: data
  })
}

export function updateReceipt(data) {
  return request({
    url: '/business/receipt',
    method: 'put',
    data: data
  })
}

export function delReceipt(receiptId) {
  return request({
    url: '/business/receipt/' + receiptId,
    method: 'delete'
  })
}
