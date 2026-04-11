import request from '@/utils/request'

// 查询经营简报归档列表
export function listExecutiveBriefRecord(query) {
  return request({
    url: '/business/executiveBriefRecord/list',
    method: 'get',
    params: query
  })
}

// 查询经营简报归档详情
export function getExecutiveBriefRecord(briefId) {
  return request({
    url: '/business/executiveBriefRecord/' + briefId,
    method: 'get'
  })
}

// 新增经营简报归档
export function addExecutiveBriefRecord(data) {
  return request({
    url: '/business/executiveBriefRecord',
    method: 'post',
    data: data
  })
}
