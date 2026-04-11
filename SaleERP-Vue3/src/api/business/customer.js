import request from '@/utils/request'

export function listCustomer(query) {
  return request({
    url: '/business/customer/list',
    method: 'get',
    params: query
  })
}

export function getCustomer(customerId) {
  return request({
    url: '/business/customer/' + customerId,
    method: 'get'
  })
}

export function addCustomer(data) {
  return request({
    url: '/business/customer',
    method: 'post',
    data: data
  })
}

export function updateCustomer(data) {
  return request({
    url: '/business/customer',
    method: 'put',
    data: data
  })
}

export function changeCustomerStatus(data) {
  return request({
    url: '/business/customer/changeStatus',
    method: 'put',
    data: data
  })
}

export function delCustomer(customerId) {
  return request({
    url: '/business/customer/' + customerId,
    method: 'delete'
  })
}

// 获取导入模板
export function importTemplate() {
  return request({
    url: '/business/customer/importTemplate',
    method: 'post',
    responseType: 'blob'
  })
}
