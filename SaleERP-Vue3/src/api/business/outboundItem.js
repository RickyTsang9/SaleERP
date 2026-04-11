import request from '@/utils/request'

export function listOutboundItem(query) {
  return request({
    url: '/business/outboundItem/list',
    method: 'get',
    params: query
  })
}

export function getOutboundItem(outboundItemId) {
  return request({
    url: '/business/outboundItem/' + outboundItemId,
    method: 'get'
  })
}

export function addOutboundItem(data) {
  return request({
    url: '/business/outboundItem',
    method: 'post',
    data: data
  })
}

export function updateOutboundItem(data) {
  return request({
    url: '/business/outboundItem',
    method: 'put',
    data: data
  })
}

export function delOutboundItem(outboundItemId) {
  return request({
    url: '/business/outboundItem/' + outboundItemId,
    method: 'delete'
  })
}
