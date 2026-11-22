// 根据当前勾选结果统一计算主表工具栏状态，避免每个单据页重复维护相同判断。
export function buildDocumentSelectionState(selectionList, idFieldName) {
  return {
    idList: selectionList.map(selectionItem => selectionItem[idFieldName]),
    single: selectionList.length !== 1,
    multiple: !selectionList.length,
    selectedStatus: selectionList.length === 1 ? selectionList[0].status : ''
  }
}

// 返回当前工具栏操作选中的单据编号，未选中时统一给出提示文案。
export function getSingleSelectedDocumentId(selectedIdList, actionText, documentName, showWarningMessage) {
  const documentId = selectedIdList[0]
  if (!documentId) {
    showWarningMessage(`请选择要${actionText}的${documentName}`)
    return undefined
  }
  return documentId
}

// 统一解析当前操作的目标单据，兼容行内操作和工具栏操作两种入口。
export function resolveDocumentActionRow(actionRow, selectedIdList, documentList, idFieldName, actionText, documentName, showWarningMessage) {
  if (actionRow?.[idFieldName]) {
    return actionRow
  }
  const documentId = getSingleSelectedDocumentId(selectedIdList, actionText, documentName, showWarningMessage)
  if (!documentId) {
    return null
  }
  return documentList.find(documentItem => documentItem[idFieldName] === documentId) || null
}
