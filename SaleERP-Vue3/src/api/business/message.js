import request from '@/utils/request'

export function listMessage(query) {
  return request({
    url: '/business/message/list',
    method: 'get',
    params: query
  })
}

export function popupList(query) {
  return request({
    url: '/business/message/popupList',
    method: 'get',
    params: query
  })
}

export function unreadCount(query) {
  return request({
    url: '/business/message/unreadCount',
    method: 'get',
    params: query
  })
}

export function getUnreadMessageCount(query) {
  return unreadCount(query)
}

export function listPopupMessage(query) {
  return popupList(query)
}

export function readMessage(messageId) {
  return request({
    url: '/business/message/read/' + messageId,
    method: 'post'
  })
}

export function readAllMessage(data) {
  return request({
    url: '/business/message/readAll',
    method: 'post',
    params: data
  })
}
