import request from '@/utils/request'

export function listInboundItem(query) {
  return request({
    url: '/business/inboundItem/list',
    method: 'get',
    params: query
  })
}

export function getInboundItem(inboundItemId) {
  return request({
    url: '/business/inboundItem/' + inboundItemId,
    method: 'get'
  })
}

export function addInboundItem(data) {
  return request({
    url: '/business/inboundItem',
    method: 'post',
    data: data
  })
}

export function updateInboundItem(data) {
  return request({
    url: '/business/inboundItem',
    method: 'put',
    data: data
  })
}

export function delInboundItem(inboundItemId) {
  return request({
    url: '/business/inboundItem/' + inboundItemId,
    method: 'delete'
  })
}
