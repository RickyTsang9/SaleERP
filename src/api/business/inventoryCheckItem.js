import request from '@/utils/request'

export function listInventoryCheckItem(query) {
  return request({
    url: '/business/inventoryCheckItem/list',
    method: 'get',
    params: query
  })
}

export function getInventoryCheckItem(checkItemId) {
  return request({
    url: '/business/inventoryCheckItem/' + checkItemId,
    method: 'get'
  })
}

export function addInventoryCheckItem(data) {
  return request({
    url: '/business/inventoryCheckItem',
    method: 'post',
    data: data
  })
}

export function updateInventoryCheckItem(data) {
  return request({
    url: '/business/inventoryCheckItem',
    method: 'put',
    data: data
  })
}

export function delInventoryCheckItem(checkItemId) {
  return request({
    url: '/business/inventoryCheckItem/' + checkItemId,
    method: 'delete'
  })
}
