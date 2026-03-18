import request from '@/utils/request'

export function listReceivable(query) {
  return request({
    url: '/business/receivable/list',
    method: 'get',
    params: query
  })
}

export function getReceivable(receivableId) {
  return request({
    url: '/business/receivable/' + receivableId,
    method: 'get'
  })
}

export function addReceivable(data) {
  return request({
    url: '/business/receivable',
    method: 'post',
    data: data
  })
}

export function updateReceivable(data) {
  return request({
    url: '/business/receivable',
    method: 'put',
    data: data
  })
}

export function delReceivable(receivableId) {
  return request({
    url: '/business/receivable/' + receivableId,
    method: 'delete'
  })
}
