import request from '@/utils/request'

export function listInventoryCheck(query) {
  return request({
    url: '/business/inventoryCheck/list',
    method: 'get',
    params: query
  })
}

export function getInventoryCheck(checkId) {
  return request({
    url: '/business/inventoryCheck/' + checkId,
    method: 'get'
  })
}

export function addInventoryCheck(data) {
  return request({
    url: '/business/inventoryCheck',
    method: 'post',
    data: data
  })
}

export function updateInventoryCheck(data) {
  return request({
    url: '/business/inventoryCheck',
    method: 'put',
    data: data
  })
}

export function submitInventoryCheck(checkId) {
  return request({
    url: '/business/inventoryCheck/submit/' + checkId,
    method: 'post'
  })
}

export function auditInventoryCheck(checkId) {
  return request({
    url: '/business/inventoryCheck/audit/' + checkId,
    method: 'post'
  })
}

export function cancelInventoryCheck(checkId) {
  return request({
    url: '/business/inventoryCheck/cancel/' + checkId,
    method: 'post'
  })
}

export function delInventoryCheck(checkId) {
  return request({
    url: '/business/inventoryCheck/' + checkId,
    method: 'delete'
  })
}
