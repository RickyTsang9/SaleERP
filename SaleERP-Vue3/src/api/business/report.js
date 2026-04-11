import request from '@/utils/request'

export function getDashboard() {
  return request({
    url: '/business/report/dashboard',
    method: 'get'
  })
}

export function listSaleReport(query) {
  return request({
    url: '/business/report/saleList',
    method: 'get',
    params: query
  })
}

export function listReconciliation(query) {
  return request({
    url: '/business/report/reconciliationList',
    method: 'get',
    params: query
  })
}

export function listInvoiceTax(query) {
  return request({
    url: '/business/report/invoiceTaxList',
    method: 'get',
    params: query
  })
}

export function getCashFlowForecast(query) {
  return request({
    url: '/business/report/cashFlowForecast',
    method: 'get',
    params: query
  })
}
