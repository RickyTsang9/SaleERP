// 判断当前消息正文是否像历史编码异常后遗留的 base64 文本，避免对正常正文重复解码。
export function looksLikeEncodedDashboardMessageContent(messageContent) {
  return typeof messageContent === 'string' && /^[A-Za-z0-9+/=]+$/.test(messageContent) && messageContent.length % 4 === 0
}

// 尝试修复历史公告正文的编码问题，优先还原 base64 与乱码叠加后的中文内容。
export function decodeDashboardMessageContent(messageContent) {
  if (!looksLikeEncodedDashboardMessageContent(messageContent)) {
    return messageContent
  }
  try {
    const base64DecodedText = atob(messageContent)
    const utf8ByteArray = Uint8Array.from(base64DecodedText, messageCharacter => messageCharacter.charCodeAt(0))
    const firstDecodedText = new TextDecoder().decode(utf8ByteArray)
    const latinByteArray = Uint8Array.from(firstDecodedText, messageCharacter => messageCharacter.charCodeAt(0))
    const repairedText = new TextDecoder().decode(latinByteArray)
    if (/[\u4e00-\u9fa5]/.test(repairedText)) {
      return repairedText
    }
    return firstDecodedText
  } catch (error) {
    return messageContent
  }
}

// 统一隐藏消息正文里的历史对象内部编号，避免管理层直接看到技术字段。
export function normalizeDashboardMessageContent(messageContent) {
  if (typeof messageContent !== 'string') {
    return messageContent
  }
  return messageContent
    .replace(/历史客户id：\d+/gi, '客户资料缺失')
    .replace(/历史供应商id：\d+/gi, '供应商资料缺失')
}

// 统一组装首页和经营看板的消息详情正文，保证公告预览内容稳定可读。
export function buildDashboardMessagePreviewContent(messageItem) {
  if (!messageItem) {
    return '当前消息暂无更多详情'
  }
  if (messageItem.message_content) {
    return normalizeDashboardMessageContent(decodeDashboardMessageContent(messageItem.message_content))
  }
  return messageItem.message_title || '当前消息暂无更多详情'
}
