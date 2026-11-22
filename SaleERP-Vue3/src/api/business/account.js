import request from '@/utils/request'

export function listAccount(query) {
  return request({
    url: '/business/account/list',
    method: 'get',
    params: query
  })
}

export function getAccountSummary() {
  return request({
    url: '/business/account/summary',
    method: 'get'
  })
}

export function getAccount(accountId) {
  return request({
    url: '/business/account/' + accountId,
    method: 'get'
  })
}

export function addAccount(data) {
  return request({
    url: '/business/account',
    method: 'post',
    data: data
  })
}

export function updateAccount(data) {
  return request({
    url: '/business/account',
    method: 'put',
    data: data
  })
}

export function listAccountJournal(query) {
  return request({
    url: '/business/account/journal/list',
    method: 'get',
    params: query
  })
}

export function transferAccount(data) {
  return request({
    url: '/business/account/transfer',
    method: 'post',
    data: data
  })
}

export function recordOtherFund(data) {
  return request({
    url: '/business/account/otherFund',
    method: 'post',
    data: data
  })
}

export function listPeriodClose(query) {
  return request({
    url: '/business/account/period/list',
    method: 'get',
    params: query
  })
}

export function closePeriod(data) {
  return request({
    url: '/business/account/period/close',
    method: 'post',
    data: data
  })
}

export function reopenPeriod(data) {
  return request({
    url: '/business/account/period/reopen',
    method: 'post',
    data: data
  })
}
