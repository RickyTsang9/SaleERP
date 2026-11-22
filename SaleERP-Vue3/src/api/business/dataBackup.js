import request from '@/utils/request'

export function listDataBackup(query) {
  return request({
    url: '/business/dataBackup/list',
    method: 'get',
    params: query
  })
}

export function getDataBackup(backupId) {
  return request({
    url: '/business/dataBackup/' + backupId,
    method: 'get'
  })
}

export function manualBackup() {
  return request({
    url: '/business/dataBackup/manual',
    method: 'post'
  })
}

export function restoreDataBackup(backupId) {
  return request({
    url: '/business/dataBackup/restore/' + backupId,
    method: 'post'
  })
}

export function delDataBackup(backupId) {
  return request({
    url: '/business/dataBackup/' + backupId,
    method: 'delete'
  })
}

export function downloadDataBackup(backupId) {
  return request({
    url: '/business/dataBackup/' + backupId + '/download',
    method: 'get',
    responseType: 'blob'
  })
}

export function uploadRestoreDataBackup(formData) {
  return request({
    url: '/business/dataBackup/uploadRestore',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
