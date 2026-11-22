import request from '@/utils/request'

export function listInvoice(query) {
  return request({
    url: '/business/invoice/list',
    method: 'get',
    params: query
  })
}

export function getInvoiceSummary(query) {
  return request({
    url: '/business/invoice/summary',
    method: 'get',
    params: query
  })
}

export function getInvoiceSourceOptions() {
  return request({
    url: '/business/invoice/sourceOptions',
    method: 'get'
  })
}

export function getInvoice(invoiceId) {
  return request({
    url: '/business/invoice/' + invoiceId,
    method: 'get'
  })
}

export function addInvoice(data) {
  return request({
    url: '/business/invoice',
    method: 'post',
    data: data
  })
}

export function updateInvoice(data) {
  return request({
    url: '/business/invoice',
    method: 'put',
    data: data
  })
}

export function voidInvoice(invoiceId, voidReason) {
  return request({
    url: '/business/invoice/' + invoiceId + '/void',
    method: 'put',
    data: { voidReason }
  })
}
