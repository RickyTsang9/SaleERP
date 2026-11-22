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

export function updateStockWarningThreshold(data) {
  return request({
    url: '/business/stock/warningThreshold',
    method: 'put',
    data: data
  })
}

export function recommendStockBatch(warehouseId, productId) {
  return request({
    url: '/business/stock/recommendBatch',
    method: 'get',
    params: { warehouseId, productId }
  })
}

export function freezeStock(data) {
  return request({
    url: '/business/stock/freeze',
    method: 'post',
    data: data
  })
}

export function unfreezeStock(data) {
  return request({
    url: '/business/stock/unfreeze',
    method: 'post',
    data: data
  })
}
