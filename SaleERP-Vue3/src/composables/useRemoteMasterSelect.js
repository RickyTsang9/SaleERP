import { ref } from "vue"
import { listCustomer, getCustomer } from "@/api/business/customer"
import { listSupplier, getSupplier } from "@/api/business/supplier"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { appendUniqueSelectOption, buildSelectOptionList } from "@/utils/remoteSelect"

const ENTITY_CONFIG = {
  customer: {
    idKey: "customerId",
    nameKey: "customerName",
    codeKey: "customerCode",
    listFn: listCustomer,
    getFn: getCustomer
  },
  supplier: {
    idKey: "supplierId",
    nameKey: "supplierName",
    codeKey: "supplierCode",
    listFn: listSupplier,
    getFn: getSupplier
  },
  warehouse: {
    idKey: "warehouseId",
    nameKey: "warehouseName",
    codeKey: "warehouseCode",
    listFn: listWarehouse,
    getFn: getWarehouse
  },
  product: {
    idKey: "productId",
    nameKey: "productName",
    codeKey: "productCode",
    listFn: listProduct,
    getFn: getProduct
  },
  location: {
    idKey: "locationId",
    nameKey: "locationName",
    codeKey: "locationCode",
    listFn: listLocation,
    getFn: getLocation
  }
}

/**
 * 统一主数据远程检索、启用过滤与按主键兜底回显 Composable
 */
export function useRemoteMasterSelect(entityType, extraDefaultParams = {}) {
  const cfg = ENTITY_CONFIG[entityType]
  if (!cfg) {
    throw new Error(`Unsupported master entity type: ${entityType}`)
  }

  const options = ref([])
  const cacheMap = ref({})
  const loading = ref(false)

  function cacheItems(rows) {
    if (!Array.isArray(rows)) return
    const nextMap = { ...cacheMap.value }
    rows.forEach(item => {
      if (item && item[cfg.idKey] != null) {
        nextMap[item[cfg.idKey]] = item
      }
    })
    cacheMap.value = nextMap
  }

  async function search(keyword = "", extraParams = {}) {
    loading.value = true
    try {
      const params = {
        pageNum: 1,
        pageSize: 50,
        status: "0",
        ...extraDefaultParams,
        ...extraParams
      }
      const trimmed = String(keyword || "").trim()
      if (trimmed) {
        params[cfg.nameKey] = trimmed
      }
      const res = await cfg.listFn(params)
      const rows = res?.rows || []
      cacheItems(rows)
      options.value = rows
      return rows
    } finally {
      loading.value = false
    }
  }

  async function ensureLoaded(id) {
    if (!id) return null
    if (cacheMap.value[id]) {
      options.value = appendUniqueSelectOption(options.value, cacheMap.value[id], cfg.idKey)
      return cacheMap.value[id]
    }
    try {
      const res = await cfg.getFn(id)
      const data = res?.data
      if (data && data[cfg.idKey] != null) {
        cacheItems([data])
        options.value = appendUniqueSelectOption(options.value, data, cfg.idKey)
        return data
      }
    } catch {
      // ignore missing or unauthorized detail lookup
    }
    return null
  }

  function buildOptions(...selectedIds) {
    const extras = selectedIds
      .filter(id => id != null && cacheMap.value[id])
      .map(id => cacheMap.value[id])
    let merged = buildSelectOptionList(options.value, null, cfg.idKey)
    extras.forEach(item => {
      merged = appendUniqueSelectOption(merged, item, cfg.idKey)
    })
    return merged
  }

  function getName(id, fallbackPrefix = "#") {
    if (!id) return "-"
    return cacheMap.value[id]?.[cfg.nameKey] || `${fallbackPrefix}${id}`
  }

  return {
    options,
    cacheMap,
    loading,
    search,
    ensureLoaded,
    buildOptions,
    getName
  }
}
