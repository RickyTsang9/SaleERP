import request from '@/utils/request'

export function listMessage(query) {
  return request({
    url: '/business/message/list',
    method: 'get',
    params: query
  })
}

export function getUnreadMessageCount(messageType) {
  return request({
    url: '/business/message/unreadCount',
    method: 'get',
    params: { messageType: messageType }
  })
}

export function listPopupMessage(messageType, limitCount) {
  return request({
    url: '/business/message/popupList',
    method: 'get',
    params: { messageType: messageType, limitCount: limitCount }
  })
}

export function readMessage(messageId) {
  return request({
    url: '/business/message/read/' + messageId,
    method: 'post'
  })
}

export function readAllMessage(messageType) {
  return request({
    url: '/business/message/readAll',
    method: 'post',
    params: { messageType: messageType }
  })
}
