import request from '@/utils/request'

// 查询经营决议事项列表
export function listExecutiveActionItem(query) {
  return request({
    url: '/business/executiveActionItem/list',
    method: 'get',
    params: query
  })
}

// 查询经营决议事项详情
export function getExecutiveActionItem(actionItemId) {
  return request({
    url: '/business/executiveActionItem/' + actionItemId,
    method: 'get'
  })
}

// 新增经营决议事项
export function addExecutiveActionItem(data) {
  return request({
    url: '/business/executiveActionItem',
    method: 'post',
    data: data
  })
}

// 修改经营决议事项
export function updateExecutiveActionItem(data) {
  return request({
    url: '/business/executiveActionItem',
    method: 'put',
    data: data
  })
}

// 删除经营决议事项
export function delExecutiveActionItem(actionItemId) {
  return request({
    url: '/business/executiveActionItem/' + actionItemId,
    method: 'delete'
  })
}
