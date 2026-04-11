import request from '@/utils/request'

export function listStockLog(query) {
  return request({
    url: '/business/stockLog/list',
    method: 'get',
    params: query
  })
}

export function getStockLog(stockLogId) {
  return request({
    url: '/business/stockLog/' + stockLogId,
    method: 'get'
  })
}
