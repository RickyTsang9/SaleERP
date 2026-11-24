/**
 * SaleERP 通用格式化、路由参数解析与快捷日期范围工具
 */
export function formatMoney(val, digits = 2, maskedFallback = "权限脱敏") {
  if (val === null || val === undefined || val === "") {
    return maskedFallback
  }
  const num = Number(val)
  if (!Number.isFinite(num)) {
    return maskedFallback
  }
  return num.toLocaleString("zh-CN", {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

export function convertRouteNumberValue(rawVal) {
  if (rawVal === undefined || rawVal === null || rawVal === "") {
    return undefined
  }
  const parsed = Number(rawVal)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined
}

export function buildTodayDateString() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, "0")
  const d = String(now.getDate()).padStart(2, "0")
  return `${y}-${m}-${d}`
}

function formatDateYmd(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, "0")
  const d = String(date.getDate()).padStart(2, "0")
  return `${y}-${m}-${d}`
}

export const ERP_DATE_SHORTCUTS = [
  {
    text: "今日",
    value: () => {
      const today = formatDateYmd(new Date())
      return [today, today]
    }
  },
  {
    text: "近7天",
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 6)
      return [formatDateYmd(start), formatDateYmd(end)]
    }
  },
  {
    text: "近30天",
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 29)
      return [formatDateYmd(start), formatDateYmd(end)]
    }
  },
  {
    text: "本月",
    value: () => {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth(), 1)
      return [formatDateYmd(start), formatDateYmd(now)]
    }
  },
  {
    text: "上月",
    value: () => {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth() - 1, 1)
      const end = new Date(now.getFullYear(), now.getMonth(), 0)
      return [formatDateYmd(start), formatDateYmd(end)]
    }
  }
]
