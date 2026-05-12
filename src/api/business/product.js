import request from '@/utils/request'

export function listProduct(query) {
  return request({
    url: '/business/product/list',
    method: 'get',
    params: query
  })
}

export function getProduct(productId) {
  return request({
    url: '/business/product/' + productId,
    method: 'get'
  })
}

export function addProduct(data) {
  return request({
    url: '/business/product',
    method: 'post',
    data: data
  })
}

export function updateProduct(data) {
  return request({
    url: '/business/product',
    method: 'put',
    data: data
  })
}

export function changeProductStatus(data) {
  return request({
    url: '/business/product/changeStatus',
    method: 'put',
    data: data
  })
}

export function delProduct(productId) {
  return request({
    url: '/business/product/' + productId,
    method: 'delete'
  })
}

// 获取导入模板
export function importTemplate() {
  return request({
    url: '/business/product/importTemplate',
    method: 'post',
    responseType: 'blob'
  })
}
