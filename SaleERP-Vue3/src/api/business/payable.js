import request from '@/utils/request'

// 查询应付账款列表
export function listPayable(query) {
  return request({
    url: '/business/payable/list',
    method: 'get',
    params: query
  })
}

// 查询应付账款详细
export function getPayable(payableId) {
  return request({
    url: '/business/payable/' + payableId,
    method: 'get'
  })
}

// 新增应付账款
export function addPayable(data) {
  return request({
    url: '/business/payable',
    method: 'post',
    data: data
  })
}

// 修改应付账款
export function updatePayable(data) {
  return request({
    url: '/business/payable',
    method: 'put',
    data: data
  })
}

// 删除应付账款
export function delPayable(payableId) {
  return request({
    url: '/business/payable/' + payableId,
    method: 'delete'
  })
}

// 导出应付账款
export function exportPayable(query) {
  return request({
    url: '/business/payable/export',
    method: 'post',
    params: query
  })
}