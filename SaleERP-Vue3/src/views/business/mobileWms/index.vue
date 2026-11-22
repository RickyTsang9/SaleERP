<template>
  <div class="app-container erp-page mobile-wms-page">
    <erp-page-header
      eyebrow="仓储现场移动作业"
      title="移动仓配 PDA 作业台"
      subtitle="专为无线扫码枪与移动端触屏打造：扫码收货点验、FEFO 效期拣货指引与现场盲盘一键协同。"
      icon="Iphone"
      tone="indigo"
      badge="无线扫码枪就绪"
    >
      <template #actions>
        <el-switch v-model="narrowPreview" active-text="PDA 手持窄屏预览" />
        <el-button type="primary" plain icon="Refresh" @click="loadAllDraftTasks">刷新待办单据</el-button>
      </template>
    </erp-page-header>

    <div class="pda-shell" :class="{ 'is-narrow': narrowPreview }">
      <div class="pda-mode-tabs">
        <button
          v-for="tab in modeTabs"
          :key="tab.key"
          type="button"
          class="pda-mode-btn"
          :class="{ active: activeMode === tab.key }"
          @click="switchMode(tab.key)"
        >
          <span class="mode-title">{{ tab.label }}</span>
          <span class="mode-badge">{{ tab.badge }}</span>
        </button>
      </div>

      <!-- 模式 1：扫码收货入库 -->
      <div v-if="activeMode === 'inbound'" class="pda-panel">
        <div class="field-label">1. 选择草稿收货入库单</div>
        <el-select v-model="selectedInboundId" placeholder="请选择待收货草稿入库单" style="width: 100%" size="large" @change="loadInboundItems">
          <el-option
            v-for="ib in draftInboundList"
            :key="ib.inboundId"
            :label="`${ib.inboundNo} | ${ib.warehouseName || '主仓'} (共${ib.totalQty || 0}件)`"
            :value="ib.inboundId"
          />
        </el-select>

        <div class="field-label mt12">2. 扫描商品条码 / 输入编码回车（实收自动 +1）</div>
        <div class="scan-input-box">
          <el-input
            ref="scanInputRef"
            v-model="scanCode"
            size="large"
            clearable
            placeholder="对准条码扫描或输入编码按回车..."
            @keyup.enter="handleExecuteScan"
          >
            <template #append>
              <el-button type="primary" :loading="scanning" @click="handleExecuteScan">确认扫码</el-button>
            </template>
          </el-input>
        </div>

        <div v-if="quickProducts.length" class="quick-sku-row">
          <span class="quick-tip">快捷演练条码：</span>
          <el-tag
            v-for="p in quickProducts"
            :key="p.productId"
            class="quick-tag"
            effect="plain"
            @click="triggerQuickScan(p.barCode || p.productCode)"
          >
            {{ p.productName }} ({{ p.barCode || p.productCode }})
          </el-tag>
        </div>

        <div class="pda-list-header">
          <span>当前入库明细（{{ inboundItems.length }} 项）</span>
          <el-button
            v-if="selectedInboundId && inboundItems.length"
            type="success"
            size="small"
            icon="Upload"
            @click="handleSubmitInboundFromPda"
          >完成收货并提交审核</el-button>
        </div>

        <div v-if="inboundItems.length" class="pda-card-list">
          <div v-for="item in inboundItems" :key="item.inboundItemId" class="pda-item-card">
            <div class="item-main">
              <div class="item-title">
                {{ getProductName(item.productId) }}
                <el-tag v-if="inboundScanCountMap[item.inboundItemId]" size="small" type="success" style="margin-left: 6px">
                  已扫码点验 {{ inboundScanCountMap[item.inboundItemId] }}
                </el-tag>
              </div>
              <div class="item-meta">
                批次：{{ item.batchNo || '-' }} | 效期：{{ item.expireDate ? parseTime(item.expireDate, '{y}-{m}-{d}') : '长期' }}
                <template v-if="item.purchaseOrderItemId && item.remainQuantity != null"> | 订单待收上限：{{ item.remainQuantity }}</template>
              </div>
            </div>
            <div class="item-qty-box">
              <span class="qty-num">{{ item.quantity }}</span>
              <span class="qty-unit">实收</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="请先选择草稿入库单或前往采购入库新建单据" :image-size="72" />
      </div>

      <!-- 模式 2：FEFO 拣货出库 -->
      <div v-else-if="activeMode === 'outbound'" class="pda-panel">
        <div class="field-label">1. 选择发货仓库与待拣货出库单</div>
        <el-row :gutter="8">
          <el-col :span="10">
            <el-select v-model="pickWarehouseId" placeholder="选择仓库" size="large" style="width: 100%">
              <el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" />
            </el-select>
          </el-col>
          <el-col :span="14">
            <el-select v-model="selectedOutboundId" placeholder="可选：关联草稿出库单" clearable size="large" style="width: 100%" @change="handlePickOutboundChange">
              <el-option
                v-for="ob in draftOutboundList"
                :key="ob.outboundId"
                :label="`${ob.outboundNo} (${ob.customerName || '客户'})`"
                :value="ob.outboundId"
              />
            </el-select>
          </el-col>
        </el-row>

        <div class="field-label mt12">2. 扫描商品条码查询 FEFO 最优拣货库位与批次</div>
        <el-input
          v-model="scanCode"
          size="large"
          clearable
          placeholder="扫描商品条码，自动计算近效期优先拣货位..."
          @keyup.enter="handleExecuteScan"
        >
          <template #append>
            <el-button type="primary" :loading="scanning" @click="handleExecuteScan">FEFO 指引</el-button>
          </template>
        </el-input>

        <div v-if="quickProducts.length" class="quick-sku-row">
          <span class="quick-tip">点击测试 FEFO 指引：</span>
          <el-tag
            v-for="p in quickProducts"
            :key="p.productId"
            class="quick-tag"
            type="warning"
            effect="plain"
            @click="triggerQuickScan(p.barCode || p.productCode)"
          >
            {{ p.productName }}
          </el-tag>
        </div>

        <div v-if="fefoResultProduct" class="fefo-guide-box">
          <div class="guide-header">
            <strong>FEFO 拣货推荐：{{ fefoResultProduct.productName }}</strong>
            <el-tag size="small" type="primary">先到期先出库</el-tag>
          </div>
          <div v-if="fefoBatches.length" class="pda-card-list">
            <div v-for="(b, idx) in fefoBatches" :key="b.stockId" class="pda-item-card" :class="{ 'is-top-fefo': idx === 0 }">
              <div class="item-main">
                <div class="item-title">
                  <el-tag v-if="idx === 0" size="small" type="danger" style="margin-right: 6px">首选拣货位</el-tag>
                  库位：{{ b.locationName || ('库位#' + (b.locationId || '-')) }}
                </div>
                <div class="item-meta">批次：{{ b.batchNo || '-' }} | 到期日：{{ b.expireDate ? parseTime(b.expireDate, '{y}-{m}-{d}') : '无限制' }}</div>
              </div>
              <div class="item-qty-box">
                <span class="qty-num">{{ Math.max(0, Number(b.quantity || 0) - Number(b.lockedQuantity || 0) - Number(b.frozenQuantity || 0)) }}</span>
                <span class="qty-unit">可拣数</span>
              </div>
            </div>
          </div>
          <el-alert v-else title="当前仓库暂无该商品可用批次库存" type="warning" :closable="false" />
        </div>

        <div v-if="selectedOutboundId && outboundItems.length" class="mt12">
          <div class="pda-list-header">
            <span>出库单待发清单（{{ outboundItems.length }} 项）</span>
            <el-button type="success" size="small" icon="Upload" @click="handleSubmitOutboundFromPda">拣货完毕提交审核</el-button>
          </div>
          <div class="pda-card-list">
            <div v-for="it in outboundItems" :key="it.outboundItemId" class="pda-item-card">
              <div class="item-main">
                <div class="item-title">
                  {{ getProductName(it.productId) }}
                  <el-tag
                    v-if="outboundPickCountMap[it.outboundItemId]"
                    size="small"
                    :type="outboundPickCountMap[it.outboundItemId] >= Number(it.quantity || 0) ? 'success' : 'warning'"
                    style="margin-left: 6px"
                  >
                    已拣 {{ outboundPickCountMap[it.outboundItemId] }} / {{ it.quantity }}
                  </el-tag>
                </div>
                <div class="item-meta">库位：{{ it.locationId ? ('库位#' + it.locationId) : '未指定' }} | 批次：{{ it.batchNo || '-' }}</div>
              </div>
              <div class="item-qty-box">
                <span class="qty-num">{{ it.quantity }}</span>
                <span class="qty-unit">实发</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 模式 3：移动连续盲盘 -->
      <div v-else class="pda-panel">
        <div class="field-label">1. 选择草稿盘点单与默认库位（现场默认开启真盲盘，初始实盘归零）</div>
        <div class="check-select-row">
          <el-select v-model="selectedCheckId" placeholder="请选择草稿盘点单" size="large" style="flex: 1" @change="handleCheckOrderChange">
            <el-option
              v-for="chk in draftCheckList"
              :key="chk.checkId"
              :label="`${chk.checkNo} | ${chk.warehouseName || '仓库'}`"
              :value="chk.checkId"
            />
          </el-select>
          <el-select v-model="selectedCheckLocationId" placeholder="默认库位(可选)" clearable size="large" style="width: 165px">
            <el-option
              v-for="loc in checkLocationOptions"
              :key="loc.locationId"
              :label="loc.locationName"
              :value="loc.locationId"
            />
          </el-select>
          <el-button
            v-if="selectedCheckId"
            type="warning"
            plain
            size="large"
            icon="MagicStick"
            @click="handleLoadSnapshotFromPda"
          >载入盲盘快照</el-button>
        </div>

        <div class="field-label mt12">2. 连续扫描实物条码（每扫一次实盘数自动 +1）</div>
        <el-input
          v-model="scanCode"
          size="large"
          clearable
          placeholder="扫描货架商品条码累加实盘数量..."
          @keyup.enter="handleExecuteScan"
        >
          <template #append>
            <el-button type="primary" :loading="scanning" @click="handleExecuteScan">实盘 +1</el-button>
          </template>
        </el-input>

        <div v-if="quickProducts.length" class="quick-sku-row">
          <span class="quick-tip">快捷点按模拟扫码：</span>
          <el-tag
            v-for="p in quickProducts"
            :key="p.productId"
            class="quick-tag"
            type="success"
            effect="plain"
            @click="triggerQuickScan(p.barCode || p.productCode)"
          >
            {{ p.productName }} (+1)
          </el-tag>
        </div>

        <div class="pda-list-header">
          <span>现场实盘记录（{{ checkItems.length }} 项 | 盲盘保护中）</span>
          <el-button
            v-if="selectedCheckId && checkItems.length"
            type="success"
            size="small"
            icon="Upload"
            @click="handleSubmitCheckFromPda"
          >完成盲盘提交复核</el-button>
        </div>

        <div v-if="checkItems.length" class="pda-card-list">
          <div v-for="ci in checkItems" :key="ci.checkItemId" class="pda-item-card">
            <div class="item-main">
              <div class="item-title">{{ getProductName(ci.productId) }}</div>
              <div class="item-meta">批次：{{ ci.batchNo || '-' }} | 账面数：***（盲盘隐藏）</div>
            </div>
            <div class="item-qty-box">
              <span class="qty-num">{{ ci.actualQty ?? 0 }}</span>
              <span class="qty-unit">实盘数</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="请选择草稿盘点单并点击【载入快照】开始盲盘" :image-size="72" />
      </div>
    </div>
  </div>
</template>

<script setup name="BusinessMobileWms">
import { Iphone } from "@element-plus/icons-vue"
import { listInbound, submitInbound } from "@/api/business/inbound"
import { listInboundItem, updateInboundItem } from "@/api/business/inboundItem"
import { listOutbound, submitOutbound } from "@/api/business/outbound"
import { listOutboundItem, updateOutboundItem } from "@/api/business/outboundItem"
import { listInventoryCheck, submitInventoryCheck, loadInventoryCheckStockSnapshot } from "@/api/business/inventoryCheck"
import { listInventoryCheckItem, addInventoryCheckItem, updateInventoryCheckItem } from "@/api/business/inventoryCheckItem"
import { listProduct, scanProductByCode } from "@/api/business/product"
import { listWarehouse } from "@/api/business/warehouse"
import { listLocation } from "@/api/business/location"
import { recommendStockBatch } from "@/api/business/stock"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const narrowPreview = ref(false)
const activeMode = ref("outbound")
const scanCode = ref("")
const scanning = ref(false)

const warehouseOptions = ref([])
const productMap = ref({})
const quickProducts = ref([])

const draftInboundList = ref([])
const selectedInboundId = ref(undefined)
const inboundItems = ref([])
const inboundScanCountMap = ref({})

const draftOutboundList = ref([])
const pickWarehouseId = ref(undefined)
const selectedOutboundId = ref(undefined)
const outboundItems = ref([])
const outboundPickCountMap = ref({})
const fefoResultProduct = ref(null)
const fefoBatches = ref([])

const draftCheckList = ref([])
const selectedCheckId = ref(undefined)
const selectedCheckLocationId = ref(undefined)
const checkLocationOptions = ref([])
const checkItems = ref([])

const modeTabs = computed(() => [
  { key: "inbound", label: "扫码收货入库", badge: `${draftInboundList.value.length} 待收` },
  { key: "outbound", label: "FEFO 拣货出库", badge: `${draftOutboundList.value.length} 待发` },
  { key: "check", label: "移动连续盲盘", badge: `${draftCheckList.value.length} 待盘` }
])

function getProductName(productId) {
  return productMap.value[productId]?.productName || `商品#${productId}`
}

function switchMode(mode) {
  activeMode.value = mode
  scanCode.value = ""
}

async function initBaseResources() {
  const [whRes, prodRes] = await Promise.all([
    listWarehouse({ pageNum: 1, pageSize: 50 }),
    listProduct({ pageNum: 1, pageSize: 100 })
  ])
  warehouseOptions.value = whRes.rows || []
  if (warehouseOptions.value.length && !pickWarehouseId.value) {
    pickWarehouseId.value = warehouseOptions.value[0].warehouseId
  }
  const map = {}
  ;(prodRes.rows || []).forEach(p => {
    map[p.productId] = p
  })
  productMap.value = map
  quickProducts.value = (prodRes.rows || []).slice(0, 4)
}

async function loadAllDraftTasks() {
  const [ibRes, obRes, chkRes] = await Promise.all([
    listInbound({ pageNum: 1, pageSize: 30, status: "draft" }),
    listOutbound({ pageNum: 1, pageSize: 30, status: "draft" }),
    listInventoryCheck({ pageNum: 1, pageSize: 30, status: "draft" })
  ])
  draftInboundList.value = ibRes.rows || []
  draftOutboundList.value = obRes.rows || []
  draftCheckList.value = chkRes.rows || []

  if (draftInboundList.value.length && !selectedInboundId.value) {
    selectedInboundId.value = draftInboundList.value[0].inboundId
    loadInboundItems()
  }
  if (draftCheckList.value.length && !selectedCheckId.value) {
    selectedCheckId.value = draftCheckList.value[0].checkId
    handleCheckOrderChange(selectedCheckId.value)
  }
}

async function handleCheckOrderChange(checkId) {
  selectedCheckLocationId.value = undefined
  checkLocationOptions.value = []
  if (!checkId) {
    checkItems.value = []
    return
  }
  const chk = draftCheckList.value.find(c => c.checkId === checkId)
  if (chk?.warehouseId) {
    const locRes = await listLocation({ pageNum: 1, pageSize: 100, warehouseId: chk.warehouseId, status: "0" })
    checkLocationOptions.value = locRes.rows || []
    if (checkLocationOptions.value.length) {
      selectedCheckLocationId.value = checkLocationOptions.value[0].locationId
    }
  }
  await loadCheckItems()
}

async function loadInboundItems() {
  if (!selectedInboundId.value) {
    inboundItems.value = []
    return
  }
  const res = await listInboundItem({ pageNum: 1, pageSize: 200, inboundId: selectedInboundId.value })
  inboundItems.value = res.rows || []
}

async function handlePickOutboundChange(outboundId) {
  if (!outboundId) {
    outboundItems.value = []
    return
  }
  const picked = draftOutboundList.value.find(o => o.outboundId === outboundId)
  if (picked?.warehouseId) {
    pickWarehouseId.value = picked.warehouseId
  }
  const res = await listOutboundItem({ pageNum: 1, pageSize: 200, outboundId })
  outboundItems.value = res.rows || []
}

async function loadCheckItems() {
  if (!selectedCheckId.value) {
    checkItems.value = []
    return
  }
  const res = await listInventoryCheckItem({ pageNum: 1, pageSize: 500, checkId: selectedCheckId.value })
  checkItems.value = res.rows || []
}

function triggerQuickScan(code) {
  scanCode.value = code
  handleExecuteScan()
}

async function handleExecuteScan() {
  const code = String(scanCode.value || "").trim()
  if (!code) {
    proxy.$modal.msgWarning("请扫描条码或输入商品编码")
    return
  }
  scanning.value = true
  try {
    const prodRes = await scanProductByCode(code)
    const product = prodRes.data
    if (!product?.productId) {
      proxy.$modal.msgError(`未识别条码：${code}`)
      return
    }
    productMap.value[product.productId] = product

    if (activeMode.value === "outbound") {
      fefoResultProduct.value = product
      const batchRes = await recommendStockBatch(pickWarehouseId.value, product.productId)
      fefoBatches.value = batchRes.data || []
      if (selectedOutboundId.value && outboundItems.value.length) {
        const matchedLine = outboundItems.value.find(it => it.productId === product.productId)
        if (matchedLine) {
          const prevPick = Number(outboundPickCountMap.value[matchedLine.outboundItemId] || 0)
          const targetQty = Number(matchedLine.quantity || 0)
          if (prevPick >= targetQty) {
            proxy.$modal.msgWarning(`商品【${product.productName}】已拣满（${prevPick} / ${targetQty}），请勿重复拣货`)
          } else {
            outboundPickCountMap.value[matchedLine.outboundItemId] = prevPick + 1
            // 手工无关联销售单出库时，若明细尚未指定库位/批次，自动回填首选 FEFO 批次
            const topFefo = fefoBatches.value[0]
            if (!matchedLine.saleOrderItemId && topFefo && (!matchedLine.locationId || !matchedLine.batchNo)) {
              await updateOutboundItem({
                ...matchedLine,
                locationId: topFefo.locationId,
                batchNo: topFefo.batchNo
              })
              await handlePickOutboundChange(selectedOutboundId.value)
            }
            proxy.$modal.msgSuccess(`FEFO 拣货复核：${product.productName} (${prevPick + 1} / ${targetQty})`)
          }
        } else {
          proxy.$modal.msgWarning(`出库单不包含商品【${product.productName}】，仅展示 FEFO 推荐位`)
        }
      }
      scanCode.value = ""
      return
    }

    if (activeMode.value === "inbound") {
      if (!selectedInboundId.value) {
        proxy.$modal.msgWarning("请先选择草稿收货入库单")
        return
      }
      const target = inboundItems.value.find(it => it.productId === product.productId)
      if (!target) {
        proxy.$modal.msgWarning(`当前入库单不包含商品【${product.productName}】`)
        return
      }
      const currentScanned = Number(inboundScanCountMap.value[target.inboundItemId] || 0) + 1
      inboundScanCountMap.value[target.inboundItemId] = currentScanned
      const currentQty = Number(target.quantity || 0)
      const maxAllowed = target.purchaseOrderItemId && target.remainQuantity != null ? Number(target.remainQuantity) : null
      if (maxAllowed !== null && currentQty >= maxAllowed) {
        if (currentScanned > maxAllowed) {
          inboundScanCountMap.value[target.inboundItemId] = maxAllowed
          proxy.$modal.msgWarning(`商品【${product.productName}】已达采购单待收上限(${maxAllowed})，点验已完成`)
        } else {
          proxy.$modal.msgSuccess(`收货点验核对：${product.productName} 已点验 ${currentScanned} / 计划 ${currentQty}`)
        }
      } else {
        const nextQty = currentQty + 1
        const price = Number(target.price || 0)
        await updateInboundItem({
          ...target,
          quantity: nextQty,
          amount: (nextQty * price).toFixed(2)
        })
        proxy.$modal.msgSuccess(`收货点验：${product.productName} 实收 -> ${nextQty}`)
        loadInboundItems()
      }
      scanCode.value = ""
      return
    }

    if (activeMode.value === "check") {
      if (!selectedCheckId.value) {
        proxy.$modal.msgWarning("请先选择草稿盘点单")
        return
      }
      const existing = checkItems.value.find(it =>
        it.productId === product.productId &&
        (!selectedCheckLocationId.value || !it.locationId || it.locationId === selectedCheckLocationId.value)
      )
      if (existing) {
        const nextActual = Number(existing.actualQty || 0) + 1
        const stockQty = Number(existing.stockQty || 0)
        const price = Number(existing.price || 0)
        const diffQty = nextActual - stockQty
        await updateInventoryCheckItem({
          ...existing,
          actualQty: nextActual,
          diffQty,
          diffAmount: (diffQty * price).toFixed(2)
        })
        proxy.$modal.msgSuccess(`盲盘点验：${product.productName} 实盘 -> ${nextActual}`)
      } else {
        const targetLocId = selectedCheckLocationId.value || checkLocationOptions.value[0]?.locationId
        await addInventoryCheckItem({
          checkId: selectedCheckId.value,
          productId: product.productId,
          locationId: targetLocId,
          batchNo: "",
          stockQty: 0,
          actualQty: 1,
          diffQty: 1,
          price: Number(product.costPrice || 0),
          diffAmount: Number(product.costPrice || 0)
        })
        proxy.$modal.msgSuccess(`新增盘盈实盘商品：${product.productName} (实盘 1)`)
      }
      scanCode.value = ""
      loadCheckItems()
    }
  } finally {
    scanning.value = false
  }
}

async function handleLoadSnapshotFromPda() {
  if (!selectedCheckId.value) return
  const res = await loadInventoryCheckStockSnapshot(selectedCheckId.value, selectedCheckLocationId.value, true)
  proxy.$modal.msgSuccess(res.msg || "已载入仓库真盲盘快照（实盘初始化为0）")
  loadCheckItems()
}

async function handleSubmitInboundFromPda() {
  await submitInbound(selectedInboundId.value)
  proxy.$modal.msgSuccess("入库单已提交主管审核")
  selectedInboundId.value = undefined
  inboundItems.value = []
  loadAllDraftTasks()
}

async function handleSubmitOutboundFromPda() {
  await submitOutbound(selectedOutboundId.value)
  proxy.$modal.msgSuccess("出库单已提交审核")
  selectedOutboundId.value = undefined
  outboundItems.value = []
  loadAllDraftTasks()
}

async function handleSubmitCheckFromPda() {
  await submitInventoryCheck(selectedCheckId.value)
  proxy.$modal.msgSuccess("盲盘单已提交主管复核")
  selectedCheckId.value = undefined
  checkItems.value = []
  loadAllDraftTasks()
}

Promise.all([initBaseResources(), loadAllDraftTasks()])
</script>

<style scoped lang="scss">
.pda-shell {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
  transition: max-width 0.28s cubic-bezier(0.22, 1, 0.36, 1);

  &.is-narrow {
    max-width: 430px;
    border: 2px solid var(--erp-border-strong);
    border-radius: 26px;
    padding: 16px;
    background: var(--erp-surface);
    box-shadow: var(--erp-shadow-lg);
  }
}

.pda-mode-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.pda-mode-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 15px 12px;
  border-radius: 14px;
  border: 1px solid var(--erp-border);
  background: var(--erp-surface);
  color: var(--erp-text-secondary);
  cursor: pointer;
  box-shadow: var(--erp-shadow-xs);
  transition: all 0.2s ease;

  &:hover:not(.active) {
    border-color: var(--erp-border-strong);
    transform: translateY(-1px);
  }

  .mode-title {
    font-size: 14.5px;
    font-weight: 700;
  }

  .mode-badge {
    margin-top: 5px;
    font-size: 11.5px;
    padding: 2px 10px;
    border-radius: 999px;
    background: var(--erp-surface-soft);
    color: var(--erp-text-muted);
  }

  &.active {
    border-color: var(--erp-primary);
    background: linear-gradient(135deg, rgba(var(--erp-primary-rgb), 0.12) 0%, rgba(var(--erp-primary-rgb), 0.04) 100%);
    color: var(--erp-primary);
    box-shadow: 0 0 0 3px rgba(var(--erp-primary-rgb), 0.12);

    .mode-badge {
      background: var(--erp-primary);
      color: #ffffff;
      font-weight: 600;
    }
  }
}

.pda-panel {
  padding: 22px;
  border-radius: 18px;
  background: var(--erp-surface);
  border: 1px solid var(--erp-border);
  box-shadow: var(--erp-shadow-sm);
}

.field-label {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--erp-heading);
  margin-bottom: 8px;
}

.mt12 {
  margin-top: 16px;
}

.check-select-row {
  display: flex;
  gap: 8px;
}

.quick-sku-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;

  .quick-tip {
    font-size: 12px;
    color: var(--erp-text-muted);
  }

  .quick-tag {
    cursor: pointer;
    user-select: none;
    transition: transform 0.15s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }
}

.fefo-guide-box {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--erp-surface-soft);
  border: 1px solid var(--erp-border);

  .guide-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    font-size: 14px;
    color: var(--erp-heading);
  }
}

.pda-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 18px 0 10px;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--erp-heading);
}

.pda-card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pda-item-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 13px 16px;
  border-radius: 12px;
  border: 1px solid var(--erp-border);
  background: var(--erp-surface);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;

  &:hover {
    border-color: var(--erp-border-strong);
    box-shadow: var(--erp-shadow-xs);
  }

  &.is-top-fefo {
    border-color: rgba(225, 29, 72, 0.45);
    background: rgba(225, 29, 72, 0.06);
  }

  .item-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--erp-heading);
  }

  .item-meta {
    margin-top: 4px;
    font-size: 12px;
    color: var(--erp-text-muted);
  }

  .item-qty-box {
    text-align: right;
    min-width: 68px;

    .qty-num {
      display: block;
      font-size: 22px;
      font-weight: 800;
      font-variant-numeric: tabular-nums;
      color: var(--erp-primary);
    }

    .qty-unit {
      font-size: 11px;
      color: var(--erp-text-muted);
    }
  }
}
</style>
