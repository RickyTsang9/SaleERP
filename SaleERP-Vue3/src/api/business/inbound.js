import request from '@/utils/request'

export function listInbound(query) {
  return request({
    url: '/business/inbound/list',
    method: 'get',
    params: query
  })
}

export function getInbound(inboundId) {
  return request({
    url: '/business/inbound/' + inboundId,
    method: 'get'
  })
}

export function addInbound(data) {
  return request({
    url: '/business/inbound',
    method: 'post',
    data: data
  })
}

export function updateInbound(data) {
  return request({
    url: '/business/inbound',
    method: 'put',
    data: data
  })
}

export function submitInbound(inboundId) {
  return request({
    url: '/business/inbound/submit/' + inboundId,
    method: 'post'
  })
}

export function auditInbound(inboundId) {
  return request({
    url: '/business/inbound/audit/' + inboundId,
    method: 'post'
  })
}

export function cancelInbound(inboundId) {
  return request({
    url: '/business/inbound/cancel/' + inboundId,
    method: 'post'
  })
}

export function delInbound(inboundId) {
  return request({
    url: '/business/inbound/' + inboundId,
    method: 'delete'
  })
}
