import request from '@/utils/request'

// 查询供应商列表
export function listSupplier(query) {
  return request({
    url: '/business/supplier/list',
    method: 'get',
    params: query
  })
}

// 查询供应商详细
export function getSupplier(supplierId) {
  return request({
    url: '/business/supplier/' + supplierId,
    method: 'get'
  })
}

// 新增供应商
export function addSupplier(data) {
  return request({
    url: '/business/supplier',
    method: 'post',
    data: data
  })
}

// 修改供应商
export function updateSupplier(data) {
  return request({
    url: '/business/supplier',
    method: 'put',
    data: data
  })
}

// 删除供应商
export function delSupplier(supplierId) {
  return request({
    url: '/business/supplier/' + supplierId,
    method: 'delete'
  })
}

// 导出供应商
export function exportSupplier(query) {
  return request({
    url: '/business/supplier/export',
    method: 'post',
    params: query
  })
}