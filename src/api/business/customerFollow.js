import request from '@/utils/request'

// 查询客户跟进记录列表
export function listCustomerFollow(query) {
  return request({
    url: '/business/customerFollow/list',
    method: 'get',
    params: query
  })
}

// 查询客户跟进待办摘要
export function getCustomerFollowTodoSummary(query) {
  return request({
    url: '/business/customerFollow/todoSummary',
    method: 'get',
    params: query
  })
}

// 查询客户跟进记录详细
export function getCustomerFollow(followId) {
  return request({
    url: '/business/customerFollow/' + followId,
    method: 'get'
  })
}

// 新增客户跟进记录
export function addCustomerFollow(data) {
  return request({
    url: '/business/customerFollow',
    method: 'post',
    data: data
  })
}

// 修改客户跟进记录
export function updateCustomerFollow(data) {
  return request({
    url: '/business/customerFollow',
    method: 'put',
    data: data
  })
}

// 删除客户跟进记录
export function delCustomerFollow(followId) {
  return request({
    url: '/business/customerFollow/' + followId,
    method: 'delete'
  })
}

// 导出客户跟进记录
export function exportCustomerFollow(query) {
  return request({
    url: '/business/customerFollow/export',
    method: 'post',
    params: query
  })
}
