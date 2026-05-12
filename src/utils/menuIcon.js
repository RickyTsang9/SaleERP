const menuTitleIconMap = {
  "首页": "dashboard",
  "系统管理": "system",
  "基础资料": "tree",
  "商品管理": "list",
  "客户管理": "user",
  "供应商管理": "peoples",
  "仓库管理": "tree-table",
  "库位管理": "guide",
  "仓储管理": "guide",
  "采购管理": "shopping",
  "采购订单": "shopping",
  "采购入库": "download",
  "采购退货": "more-up",
  "销售管理": "chart",
  "销售订单": "clipboard",
  "销售出库": "upload",
  "销售退货": "more-up",
  "库存管理": "table",
  "库存台账": "table",
  "库存流水": "time",
  "库存盘点": "date",
  "库存调拨": "drag",
  "财务分析": "money",
  "应收台账": "money",
  "回款记录": "download",
  "回款流水": "download",
  "应付台账": "money",
  "付款流水": "upload",
  "经营看板": "chart",
  "销售明细报表": "chart",
  "数据备份": "server",
  "客户跟进": "people"
}

const menuPathIconMap = {
  "/index": "dashboard",
  "customer": "user",
  "supplier": "peoples",
  "product": "list",
  "warehouse": "tree-table",
  "location": "guide",
  "customerFollow": "people",
  "purchaseOrder": "shopping",
  "inbound": "download",
  "purchaseReturn": "more-up",
  "saleOrder": "clipboard",
  "outbound": "upload",
  "saleReturn": "more-up",
  "stock": "table",
  "stockLog": "time",
  "inventoryCheck": "date",
  "transfer": "drag",
  "receivable": "money",
  "receipt": "download",
  "payable": "money",
  "payment": "upload",
  "report": "chart",
  "saleReport": "chart",
  "dataBackup": "server"
}

// 标准化菜单路径，统一兼容完整路由和菜单相对路径两种场景。
function normalizeMenuPath(menuPath) {
  if (!menuPath) {
    return ""
  }
  return String(menuPath).replace(/^\//, "")
}

// 为缺少图标的业务菜单返回默认图标，避免侧边栏和顶部菜单出现纯文字入口。
export function resolveMenuIcon(menuItem, fallbackIcon) {
  const menuMeta = menuItem && menuItem.meta ? menuItem.meta : {}
  const currentIcon = menuMeta.icon || fallbackIcon
  if (currentIcon && currentIcon !== "#") {
    return currentIcon
  }

  const menuTitle = menuMeta.title
  if (menuTitle && menuTitleIconMap[menuTitle]) {
    return menuTitleIconMap[menuTitle]
  }

  const normalizedMenuPath = normalizeMenuPath(menuItem && menuItem.path)
  if (normalizedMenuPath && menuPathIconMap[normalizedMenuPath]) {
    return menuPathIconMap[normalizedMenuPath]
  }

  return "guide"
}
