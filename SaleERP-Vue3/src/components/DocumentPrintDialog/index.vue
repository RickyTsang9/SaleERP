<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="920px"
    append-to-body
    destroy-on-close
    class="doc-print-dialog"
  >
    <div class="print-toolbar">
      <el-radio-group v-model="paperMode" size="small">
        <el-radio-button value="a4">A4 标准单据</el-radio-button>
        <el-radio-button value="triplicate">241×140 三等分针式联单</el-radio-button>
      </el-radio-group>
      <div class="print-switches">
        <el-switch
          v-if="canToggleHidePrice"
          v-model="hidePrice"
          active-text="无价送货单（隐藏单价金额）"
        />
        <el-switch
          v-if="docData.docType === 'inventory_check'"
          v-model="blindCheckMode"
          active-text="盲盘打印（隐藏账面数与差异）"
        />
      </div>
    </div>

    <div ref="printAreaRef" class="print-sheet-wrapper" :class="{ 'is-triplicate': paperMode === 'triplicate' }">
      <div class="print-sheet">
        <div class="sheet-header">
          <div class="company-sub">{{ companyName }}</div>
          <h2 class="sheet-title">{{ docData.title || '业务单据' }}</h2>
          <div v-if="paperMode === 'triplicate' && copyBadgeText" class="copy-badge">{{ copyBadgeText }}</div>
        </div>

        <div class="meta-grid" :class="{ 'is-two-col': useTwoColMeta }">
          <div v-for="(meta, idx) in docData.metaFields || []" :key="idx" class="meta-cell">
            <span class="meta-label">{{ meta.label }}：</span>
            <span class="meta-val">{{ meta.value || '-' }}</span>
          </div>
        </div>

        <table class="sheet-table">
          <thead>
            <tr>
              <th style="width: 46px">序号</th>
              <th v-for="col in visibleColumns" :key="col.prop" :style="col.width ? { width: col.width } : {}">
                {{ col.label }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, rIdx) in docData.items || []" :key="rIdx">
              <td class="text-center">{{ rIdx + 1 }}</td>
              <td
                v-for="col in visibleColumns"
                :key="col.prop"
                :class="col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : ''"
              >
                <template v-if="blindCheckMode && (col.prop === 'stockQty' || col.prop === 'diffQty' || col.prop === 'actualQty')">
                  {{ col.prop === 'actualQty' ? '________' : '***' }}
                </template>
                <template v-else>
                  {{ formatCell(row[col.prop], col.type) }}
                </template>
              </td>
            </tr>
            <tr v-if="!docData.items || !docData.items.length">
              <td :colspan="visibleColumns.length + 1" class="text-center">暂无明细记录</td>
            </tr>
          </tbody>
          <tfoot v-if="docData.items && docData.items.length">
            <tr>
              <td class="text-center font-bold">合计</td>
              <td :colspan="Math.max(visibleColumns.length - (showAmountSummary ? 2 : 0), 1)">
                <span v-if="showAmountSummary">人民币大写：<strong>{{ amountUppercase }}</strong></span>
                <span v-else>总数量：<strong>{{ docData.totalQty ?? '-' }}</strong></span>
              </td>
              <td v-if="showAmountSummary" class="text-right font-bold">数量：{{ docData.totalQty ?? '-' }}</td>
              <td v-if="showAmountSummary" class="text-right font-bold">¥{{ formatMoney(docData.totalAmount) }}</td>
            </tr>
          </tfoot>
        </table>

        <div v-if="docData.remark" class="sheet-remark">
          <strong>备注说明：</strong>{{ docData.remark }}
        </div>

        <div class="signature-row">
          <div v-for="(sig, sIdx) in signatureLabels" :key="sIdx" class="sig-item">
            <span>{{ sig }}：__________________</span>
          </div>
        </div>

        <div class="sheet-footer">
          <span>打印时间：{{ printNow }}</span>
          <span>系统单号：{{ docData.docNo || '-' }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关 闭</el-button>
        <el-button type="primary" icon="Printer" @click="executeBrowserPrint">立即打印 / 导出 PDF</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="DocumentPrintDialog">
const visible = ref(false)
const paperMode = ref('a4')
const hidePrice = ref(false)
const blindCheckMode = ref(false)
const printAreaRef = ref(null)
const companyName = ref('SaleERP 数字化供应链经销管理平台')
const printNow = ref('')

const docData = ref({
  docType: 'generic',
  title: '',
  docNo: '',
  metaFields: [],
  columns: [],
  items: [],
  totalQty: 0,
  totalAmount: 0,
  remark: '',
  signatures: []
})

const dialogTitle = computed(() => `单据打印预览 — ${docData.value.title || ''}`)

const canToggleHidePrice = computed(() => {
  const noPriceSwitchTypes = ['inventory_check', 'transfer', 'receipt', 'payment', 'invoice']
  return !noPriceSwitchTypes.includes(docData.value.docType) && (docData.value.columns || []).some(c => c.isPrice)
})

const useTwoColMeta = computed(() => {
  const fields = docData.value.metaFields || []
  return fields.length >= 7 || fields.some(m => String(m.value || '').length > 13)
})

const copyBadgeText = computed(() => {
  if (docData.value.copyBadge) return docData.value.copyBadge
  const map = {
    outbound: '第一联：白联仓库存根 / 第二联：红联客户签收 / 第三联：黄联财务记账',
    sale_order: '第一联：白联业务存根 / 第二联：红联客户确认 / 第三联：黄联财务留存',
    saleReturn: '第一联：白联仓库存根 / 第二联：红联客户确认 / 第三联：黄联财务冲账',
    sale_return: '第一联：白联仓库存根 / 第二联：红联客户确认 / 第三联：黄联财务冲账',
    purchase_order: '第一联：白联采购存根 / 第二联：红联供应商确认 / 第三联：黄联财务留存',
    inbound: '第一联：白联仓库验收 / 第二联：红联采购核对 / 第三联：黄联财务入账',
    purchaseReturn: '第一联：白联仓库存根 / 第二联：红联供应商签收 / 第三联：黄联财务冲账',
    purchase_return: '第一联：白联仓库存根 / 第二联：红联供应商签收 / 第三联：黄联财务冲账',
    transfer: '第一联：白联调出仓存根 / 第二联：红联调入仓签收 / 第三联：黄联仓储复核',
    inventory_check: '第一联：白联仓库初盘 / 第二联：红联财务监盘 / 第三联：黄联归档留存',
    receipt: '第一联：白联财务记账 / 第二联：红联客户收据 / 第三联：黄联出纳留存',
    payment: '第一联：白联财务记账 / 第二联：红联出纳留存 / 第三联：黄联供应商核对',
    invoice: '第一联：白联税务记账 / 第二联：红联财务归档'
  }
  return map[docData.value.docType] || '第一联：白联存根 / 第二联：红联业务联 / 第三联：黄联财务记账'
})

const visibleColumns = computed(() => {
  const cols = docData.value.columns || []
  if (hidePrice.value && canToggleHidePrice.value) {
    return cols.filter(c => !c.isPrice)
  }
  return cols
})

const showAmountSummary = computed(() => {
  const noAmountTypes = ['inventory_check', 'transfer']
  return (
    !noAmountTypes.includes(docData.value.docType) &&
    docData.value.showAmount !== false &&
    !hidePrice.value &&
    docData.value.totalAmount !== undefined &&
    docData.value.totalAmount !== null
  )
})

const signatureLabels = computed(() => {
  if (docData.value.signatures && docData.value.signatures.length) {
    return docData.value.signatures
  }
  const map = {
    inventory_check: ['初盘人', '复盘监盘人', '仓库主管签字'],
    outbound: ['制单人', '拣货验货人', '送货司机', '客户收货签章'],
    transfer: ['制单人', '调出仓经办', '调入仓签收'],
    purchaseReturn: ['制单人', '仓库发货', '供应商签收'],
    purchase_return: ['制单人', '仓库发货', '供应商签收'],
    saleReturn: ['制单人', '仓库质检入库', '客户确认'],
    sale_return: ['制单人', '仓库质检入库', '客户确认'],
    inbound: ['制单人', '仓库验收', '采购复核'],
    receipt: ['财务审核', '出纳收款', '客户确认签章'],
    payment: ['财务审核', '出纳复核', '经办/收款人签章'],
    invoice: ['制单人', '财务审核', '税务复核']
  }
  return map[docData.value.docType] || ['制单人', '部门审核', '对方单位签章']
})

const amountUppercase = computed(() => digitUppercase(docData.value.totalAmount || 0))

function formatMoney(val) {
  const num = Number(val || 0)
  return Number.isFinite(num) ? num.toFixed(2) : '0.00'
}

function formatCell(val, type) {
  if (val === null || val === undefined || val === '') return '-'
  if (type === 'money') return '¥' + formatMoney(val)
  return val
}

function digitUppercase(n) {
  const fraction = ['角', '分']
  const digit = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
  const unit = [['元', '万', '亿'], ['', '拾', '佰', '仟']]
  let num = Math.abs(Number(n || 0))
  let s = ''
  for (let i = 0; i < fraction.length; i++) {
    s += (digit[Math.floor(Math.round(num * 100) / (10 * Math.pow(10, -i))) % 10] + fraction[i]).replace(/零./, '')
  }
  s = s || '整'
  num = Math.floor(num)
  for (let i = 0; i < unit[0].length && num > 0; i++) {
    let p = ''
    for (let j = 0; j < unit[1].length && num > 0; j++) {
      p = digit[num % 10] + unit[1][j] + p
      num = Math.floor(num / 10)
    }
    s = p.replace(/(零.)*零$/, '').replace(/^$/, '零') + unit[0][i] + s
  }
  const head = Number(n || 0) < 0 ? '负' : ''
  return head + s.replace(/(零.)*零元/, '元').replace(/(零.)+/g, '零').replace(/^整$/, '零元整')
}

function openPrint(payload = {}) {
  docData.value = {
    docType: payload.docType || 'generic',
    title: payload.title || '业务单据',
    docNo: payload.docNo || '',
    metaFields: payload.metaFields || [],
    columns: payload.columns || [],
    items: payload.items || [],
    totalQty: payload.totalQty ?? 0,
    totalAmount: payload.totalAmount,
    showAmount: payload.showAmount,
    copyBadge: payload.copyBadge || '',
    remark: payload.remark || '',
    signatures: payload.signatures || []
  }
  paperMode.value = payload.defaultPaperMode || (payload.docType === 'outbound' ? 'triplicate' : 'a4')
  hidePrice.value = Boolean(payload.defaultHidePrice)
  blindCheckMode.value = Boolean(payload.defaultBlindMode)
  const now = new Date()
  printNow.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  visible.value = true
}

function executeBrowserPrint() {
  if (!printAreaRef.value) return
  const htmlContent = printAreaRef.value.innerHTML
  const pageSizeCss = paperMode.value === 'triplicate'
    ? '@page { size: 241mm 140mm; margin: 6mm 8mm; }'
    : '@page { size: A4 portrait; margin: 12mm; }'
  const win = window.open('', '_blank', 'width=960,height=720')
  if (!win) {
    window.print()
    return
  }
  win.document.write(`<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>${docData.value.title || '单据打印'} - ${docData.value.docNo || ''}</title>
  <style>
    ${pageSizeCss}
    body { font-family: "Microsoft YaHei", "SimSun", sans-serif; color: #111; margin: 0; padding: 12px; font-size: 13px; }
    .sheet-header { text-align: center; margin-bottom: 10px; }
    .company-sub { font-size: 12px; color: #555; }
    .sheet-title { margin: 4px 0; font-size: 20px; letter-spacing: 2px; }
    .copy-badge { font-size: 11px; color: #666; margin-top: 2px; }
    .meta-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 6px 14px; margin-bottom: 10px; font-size: 12.5px; }
    .meta-grid.is-two-col { grid-template-columns: repeat(2, minmax(0, 1fr)); }
    .meta-cell { display: flex; align-items: baseline; min-width: 0; }
    .meta-label { font-weight: 600; flex-shrink: 0; }
    .meta-val { word-break: keep-all; overflow-wrap: anywhere; }
    .sheet-table { width: 100%; border-collapse: collapse; margin-bottom: 10px; font-size: 12.5px; }
    .sheet-table th, .sheet-table td { border: 1px solid #222; padding: 5px 7px; }
    .sheet-table th { background: #f3f4f6; font-weight: 700; }
    .text-center { text-align: center; }
    .text-right { text-align: right; }
    .font-bold { font-weight: 700; }
    .sheet-remark { margin: 8px 0; font-size: 12px; }
    .signature-row { display: flex; justify-content: space-between; margin-top: 18px; font-size: 12.5px; }
    .sheet-footer { display: flex; justify-content: space-between; margin-top: 12px; font-size: 11px; color: #666; border-top: 1px dashed #aaa; padding-top: 6px; }
  </style>
</head>
<body>${htmlContent}</body>
</html>`)
  win.document.close()
  win.focus()
  setTimeout(() => {
    win.print()
  }, 200)
}

defineExpose({
  openPrint
})
</script>

<style scoped lang="scss">
.print-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 14px;
  margin-bottom: 14px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.print-sheet-wrapper {
  background: #e5e7eb;
  padding: 18px;
  border-radius: 8px;
  max-height: 62vh;
  overflow-y: auto;

  &.is-triplicate .print-sheet {
    max-width: 820px;
    border-style: dashed;
  }
}

.print-sheet {
  background: #ffffff;
  color: #111827;
  padding: 22px 26px;
  margin: 0 auto;
  border: 1px solid #d1d5db;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
}

.sheet-header {
  text-align: center;
  margin-bottom: 14px;

  .company-sub {
    font-size: 12px;
    color: #6b7280;
  }

  .sheet-title {
    margin: 4px 0;
    font-size: 21px;
    font-weight: 700;
    letter-spacing: 2px;
    color: #111827;
  }

  .copy-badge {
    font-size: 11px;
    color: #dc2626;
    margin-top: 2px;
  }
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px 16px;
  margin-bottom: 12px;
  font-size: 13px;

  &.is-two-col {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .meta-cell {
    display: flex;
    align-items: baseline;
    min-width: 0;
  }

  .meta-label {
    font-weight: 600;
    color: #374151;
    flex-shrink: 0;
  }

  .meta-val {
    word-break: keep-all;
    overflow-wrap: anywhere;
  }
}

.sheet-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12.5px;
  margin-bottom: 12px;

  th,
  td {
    border: 1px solid #374151;
    padding: 6px 8px;
  }

  th {
    background: #f3f4f6;
    font-weight: 700;
    color: #111827;
  }
}

.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.font-bold {
  font-weight: 700;
}

.sheet-remark {
  margin: 8px 0;
  font-size: 12.5px;
}

.signature-row {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
  font-size: 13px;
}

.sheet-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 8px;
  border-top: 1px dashed #9ca3af;
  font-size: 11.5px;
  color: #6b7280;
}
</style>
