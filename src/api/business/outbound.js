import request from '@/utils/request'

export function listOutbound(query) {
  return request({
    url: '/business/outbound/list',
    method: 'get',
    params: query
  })
}

export function getOutbound(outboundId) {
  return request({
    url: '/business/outbound/' + outboundId,
    method: 'get'
  })
}

export function addOutbound(data) {
  return request({
    url: '/business/outbound',
    method: 'post',
    data: data
  })
}

export function updateOutbound(data) {
  return request({
    url: '/business/outbound',
    method: 'put',
    data: data
  })
}

export function submitOutbound(outboundId) {
  return request({
    url: '/business/outbound/submit/' + outboundId,
    method: 'post'
  })
}

export function auditOutbound(outboundId) {
  return request({
    url: '/business/outbound/audit/' + outboundId,
    method: 'post'
  })
}

export function cancelOutbound(outboundId) {
  return request({
    url: '/business/outbound/cancel/' + outboundId,
    method: 'post'
  })
}

export function delOutbound(outboundId) {
  return request({
    url: '/business/outbound/' + outboundId,
    method: 'delete'
  })
}
