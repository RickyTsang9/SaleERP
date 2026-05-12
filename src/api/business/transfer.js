import request from '@/utils/request'

// 查询库存调拨列表
export function listTransfer(query) {
  return request({
    url: '/business/transfer/list',
    method: 'get',
    params: query
  })
}

// 查询库存调拨详细
export function getTransfer(transferId) {
  return request({
    url: '/business/transfer/' + transferId,
    method: 'get'
  })
}

// 新增库存调拨
export function addTransfer(data) {
  return request({
    url: '/business/transfer',
    method: 'post',
    data: data
  })
}

// 修改库存调拨
export function updateTransfer(data) {
  return request({
    url: '/business/transfer',
    method: 'put',
    data: data
  })
}

// 提交库存调拨
export function submitTransfer(transferId) {
  return request({
    url: '/business/transfer/submit/' + transferId,
    method: 'post'
  })
}

// 审核库存调拨
export function auditTransfer(transferId) {
  return request({
    url: '/business/transfer/audit/' + transferId,
    method: 'post'
  })
}

// 作废库存调拨
export function cancelTransfer(transferId) {
  return request({
    url: '/business/transfer/cancel/' + transferId,
    method: 'post'
  })
}

// 删除库存调拨
export function delTransfer(transferId) {
  return request({
    url: '/business/transfer/' + transferId,
    method: 'delete'
  })
}

// 导出库存调拨
export function exportTransfer(query) {
  return request({
    url: '/business/transfer/export',
    method: 'post',
    params: query
  })
}
