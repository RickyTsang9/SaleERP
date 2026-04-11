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

export function getStock(stockId) {
  return request({
    url: '/business/stock/' + stockId,
    method: 'get'
  })
}

export function addStock(data) {
  return request({
    url: '/business/stock',
    method: 'post',
    data: data
  })
}

export function updateStock(data) {
  return request({
    url: '/business/stock',
    method: 'put',
    data: data
  })
}

export function delStock(stockId) {
  return request({
    url: '/business/stock/' + stockId,
    method: 'delete'
  })
}
