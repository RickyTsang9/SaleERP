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

export function rejectInventoryCheck(checkId, rejectReason) {
  return request({
    url: '/business/inventoryCheck/reject/' + checkId,
    method: 'post',
    data: { rejectReason }
  })
}

export function loadInventoryCheckStockSnapshot(checkId, locationId, blindMode = false) {
  const params = {}
  if (locationId) params.locationId = locationId
  if (blindMode) params.blindMode = true
  return request({
    url: '/business/inventoryCheck/' + checkId + '/loadStockSnapshot',
    method: 'post',
    params: Object.keys(params).length ? params : undefined
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

export function batchSubmitInventoryCheck(ids) {
  return request({
    url: '/business/inventoryCheck/batchSubmit',
    method: 'post',
    data: ids
  })
}

export function batchAuditInventoryCheck(ids) {
  return request({
    url: '/business/inventoryCheck/batchAudit',
    method: 'post',
    data: ids
  })
}
