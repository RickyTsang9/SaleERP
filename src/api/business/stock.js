import request from '@/utils/request'

export function listStock(query) {
  return request({
    url: '/business/stock/list',
    method: 'get',
    params: query
  })
}

export function listWarningStock(query) {
  return request({
    url: '/business/stock/warning/list',
    method: 'get',
    params: query
  })
}

export function remindWarningStock() {
  return request({
    url: '/business/stock/warning/remind',
    method: 'post'
  })
}
