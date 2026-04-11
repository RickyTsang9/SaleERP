import request from '@/utils/request'

export function listLocation(query) {
  return request({
    url: '/business/location/list',
    method: 'get',
    params: query
  })
}

export function getLocation(locationId) {
  return request({
    url: '/business/location/' + locationId,
    method: 'get'
  })
}

export function addLocation(data) {
  return request({
    url: '/business/location',
    method: 'post',
    data: data
  })
}

export function updateLocation(data) {
  return request({
    url: '/business/location',
    method: 'put',
    data: data
  })
}

export function delLocation(locationId) {
  return request({
    url: '/business/location/' + locationId,
    method: 'delete'
  })
}
