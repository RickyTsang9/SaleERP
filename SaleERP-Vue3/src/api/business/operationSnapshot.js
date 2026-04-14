import request from '@/utils/request'

// 查询经营快照列表
export function listOperationSnapshot(query) {
  return request({
    url: '/business/operationSnapshot/list',
    method: 'get',
    params: query
  })
}

// 查询经营快照详情
export function getOperationSnapshot(snapshotId) {
  return request({
    url: '/business/operationSnapshot/' + snapshotId,
    method: 'get'
  })
}

// 新增经营快照
export function addOperationSnapshot(data) {
  return request({
    url: '/business/operationSnapshot',
    method: 'post',
    data: data
  })
}
