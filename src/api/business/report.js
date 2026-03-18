import request from '@/utils/request'

export function getDashboard() {
  return request({
    url: '/business/report/dashboard',
    method: 'get'
  })
}
