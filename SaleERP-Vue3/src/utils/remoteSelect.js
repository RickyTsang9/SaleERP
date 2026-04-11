// 将远程查询到的选项合并到本地选项列表，避免重复数据导致下拉展示混乱。
export function appendUniqueSelectOption(optionList, optionItem, optionIdFieldName) {
  if (!optionItem || optionItem[optionIdFieldName] === undefined || optionItem[optionIdFieldName] === null || optionItem[optionIdFieldName] === "") {
    return optionList
  }
  const existingOption = optionList.find(existingOptionItem => existingOptionItem[optionIdFieldName] === optionItem[optionIdFieldName])
  if (existingOption) {
    Object.assign(existingOption, optionItem)
    return optionList
  }
  optionList.unshift(optionItem)
  return optionList
}

// 按当前值构造下拉选项列表，当前值未命中远程结果时补充历史编号占位，保证回显稳定。
export function buildSelectOptionList(optionList, currentIdList, optionIdFieldName, optionLabelFieldName, fallbackLabelPrefix) {
  const selectOptionList = [...optionList]
  currentIdList.forEach(currentId => {
    if (currentId === undefined || currentId === null || currentId === "") {
      return
    }
    if (selectOptionList.some(optionItem => optionItem[optionIdFieldName] === currentId)) {
      return
    }
    selectOptionList.unshift({
      [optionIdFieldName]: currentId,
      [optionLabelFieldName]: `${fallbackLabelPrefix}${currentId}`
    })
  })
  return selectOptionList
}

// 规范远程搜索关键字，避免把纯空格当成有效查询条件提交给后端接口。
export function normalizeRemoteKeyword(searchKeyword) {
  if (typeof searchKeyword !== "string") {
    return undefined
  }
  const normalizedKeyword = searchKeyword.trim()
  return normalizedKeyword === "" ? undefined : normalizedKeyword
}
