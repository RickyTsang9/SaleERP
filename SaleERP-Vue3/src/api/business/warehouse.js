import request from '@/utils/request'

export function listWarehouse(query) {
  return request({
    url: '/business/warehouse/list',
    method: 'get',
    params: query
  })
}

export function getWarehouse(warehouseId) {
  return request({
    url: '/business/warehouse/' + warehouseId,
    method: 'get'
  })
}

export function addWarehouse(data) {
  return request({
    url: '/business/warehouse',
    method: 'post',
    data: data
  })
}

export function updateWarehouse(data) {
  return request({
    url: '/business/warehouse',
    method: 'put',
    data: data
  })
}

export function delWarehouse(warehouseId) {
  return request({
    url: '/business/warehouse/' + warehouseId,
    method: 'delete'
  })
}
