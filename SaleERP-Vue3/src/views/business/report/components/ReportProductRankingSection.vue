<template>
  <el-row :gutter="12" style="margin-top: 12px">
    <el-col :span="24">
      <el-card shadow="never">
        <template #header>
          <div class="product-ranking-header">
            <div class="product-ranking-title-group">
              <span class="product-ranking-title">商品经营排行分析</span>
              <el-tag size="small" type="primary" effect="plain">共 {{ rankingList.length }} 款商品</el-tag>
              <span class="product-ranking-subtitle">按销量、销售额、预估毛利、退货率与库存周转综合洞察单品表现</span>
            </div>
            <div class="product-ranking-actions">
              <el-radio-group v-model="sortBy" size="small" @change="loadRankingList">
                <el-radio-button value="saleAmount">销售额</el-radio-button>
                <el-radio-button value="saleQty">销量</el-radio-button>
                <el-radio-button value="grossProfitAmount">预估毛利</el-radio-button>
                <el-radio-button value="returnRate">退货率</el-radio-button>
                <el-radio-button value="turnoverRate">库存周转</el-radio-button>
              </el-radio-group>
              <el-input
                v-model="keyword"
                placeholder="搜商品名称或编码"
                clearable
                size="small"
                style="width: 170px"
                @keyup.enter="loadRankingList"
                @clear="loadRankingList"
              />
              <el-button size="small" icon="Refresh" :loading="loading" @click="loadRankingList">刷新</el-button>
            </div>
          </div>
        </template>

        <div class="product-ranking-kpi-row">
          <div class="product-ranking-kpi-card">
            <div class="kpi-label">统计销售额合计</div>
            <div class="kpi-value">¥{{ formatMoney(summaryMetrics.totalSaleAmount) }}</div>
            <div class="kpi-sub">累计销量 {{ formatNumber(summaryMetrics.totalSaleQty) }} 件</div>
          </div>
          <div class="product-ranking-kpi-card">
            <div class="kpi-label">预估毛利合计</div>
            <div class="kpi-value kpi-value--success">¥{{ formatMoney(summaryMetrics.totalGrossProfit) }}</div>
            <div class="kpi-sub">综合毛利率 {{ formatRate(summaryMetrics.overallGrossMarginRate) }}%</div>
          </div>
          <div class="product-ranking-kpi-card">
            <div class="kpi-label">退货风险监控</div>
            <div class="kpi-value" :class="summaryMetrics.highReturnCount > 0 ? 'kpi-value--danger' : ''">
              {{ summaryMetrics.highReturnCount }} 款
            </div>
            <div class="kpi-sub">退货率 ≥ 5% 商品 · 退货额 ¥{{ formatMoney(summaryMetrics.totalReturnAmount) }}</div>
          </div>
          <div class="product-ranking-kpi-card">
            <div class="kpi-label">当前在库总量</div>
            <div class="kpi-value">{{ formatNumber(summaryMetrics.totalStockQty) }}</div>
            <div class="kpi-sub">高周转单品（周转 ≥ 1.0）{{ summaryMetrics.fastTurnoverCount }} 款</div>
          </div>
        </div>

        <el-table v-loading="loading" :data="rankingList" border size="small" max-height="420">
          <el-table-column label="排名" width="54" align="center">
            <template #default="scope">
              <span class="rank-badge" :class="getRankBadgeClass(scope.$index)">
                {{ scope.$index + 1 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="商品编码" prop="productCode" width="112" align="center" show-overflow-tooltip />
          <el-table-column label="商品名称" prop="productName" min-width="136" show-overflow-tooltip />
          <el-table-column label="分类" prop="categoryName" width="84" align="center" show-overflow-tooltip />
          <el-table-column label="销量" prop="saleQty" width="66" align="right">
            <template #default="scope">{{ formatNumber(scope.row.saleQty) }}</template>
          </el-table-column>
          <el-table-column label="销售额" prop="saleAmount" width="104" align="right">
            <template #default="scope">¥{{ formatMoney(scope.row.saleAmount) }}</template>
          </el-table-column>
          <el-table-column label="预估毛利" prop="grossProfitAmount" width="104" align="right">
            <template #default="scope">
              <span :class="Number(scope.row.grossProfitAmount || 0) >= 0 ? 'text-profit-pos' : 'text-profit-neg'">
                ¥{{ formatMoney(scope.row.grossProfitAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="毛利率" prop="grossMarginRate" width="78" align="center">
            <template #default="scope">
              <el-tag size="small" :type="getMarginTagType(scope.row.grossMarginRate)" effect="plain">
                {{ formatRate(scope.row.grossMarginRate) }}%
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="退货率" prop="returnRate" width="76" align="center">
            <template #default="scope">
              <el-tag size="small" :type="getReturnTagType(scope.row.returnRate)" effect="plain">
                {{ formatRate(scope.row.returnRate) }}%
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="库存" prop="stockQty" width="72" align="right">
            <template #default="scope">{{ formatNumber(scope.row.stockQty) }}</template>
          </el-table-column>
          <el-table-column label="周转率" prop="turnoverRate" width="76" align="right">
            <template #default="scope">{{ formatRate(scope.row.turnoverRate) }}次</template>
          </el-table-column>
          <el-table-column label="操作" width="76" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="DataAnalysis" @click="openProductProfile(scope.row)">画像</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>

    <el-drawer v-model="profileOpen" title="商品经营画像" size="720px" append-to-body>
      <div v-loading="profileLoading" class="ranking-profile-body">
        <template v-if="profileData && profileData.product">
          <div class="ranking-profile-header">
            <div>
              <div class="ranking-profile-name">{{ profileData.product.productName }}</div>
              <div class="ranking-profile-meta">
                编码：{{ profileData.product.productCode || "-" }} · 规格：{{ profileData.product.productSpec || "-" }} · 分类：{{ profileData.product.categoryName || "-" }}
              </div>
              <div class="ranking-profile-meta">
                成本价：¥{{ formatMoney(profileData.product.costPrice) }} · 销售价：¥{{ formatMoney(profileData.product.salePrice) }}
              </div>
            </div>
            <el-tag :type="getWarningTagType(profileData.summary?.warningStatus)" effect="dark">
              库存：{{ profileData.summary?.warningStatus || "正常" }}
            </el-tag>
          </div>

          <div class="ranking-profile-kpis">
            <div class="ranking-profile-kpi">
              <div class="kpi-label">累计审核销售</div>
              <div class="kpi-value">¥{{ formatMoney(profileData.summary?.totalSaleAmount) }}</div>
              <div class="kpi-sub">销量 {{ formatNumber(profileData.summary?.totalSaleQty) }}（{{ profileData.summary?.saleOrderCount || 0 }} 单）</div>
            </div>
            <div class="ranking-profile-kpi">
              <div class="kpi-label">预估毛利</div>
              <div class="kpi-value kpi-value--success">¥{{ formatMoney(profileData.summary?.grossProfitAmount) }}</div>
              <div class="kpi-sub">毛利率 {{ formatRate(profileData.summary?.grossMarginRate) }}%</div>
            </div>
            <div class="ranking-profile-kpi">
              <div class="kpi-label">退货率</div>
              <div class="kpi-value">{{ formatRate(profileData.summary?.returnRate) }}%</div>
              <div class="kpi-sub">退货 {{ formatNumber(profileData.summary?.returnQty) }} 件</div>
            </div>
            <div class="ranking-profile-kpi">
              <div class="kpi-label">当前库存 / 可用</div>
              <div class="kpi-value">{{ formatNumber(profileData.summary?.totalStockQty) }}</div>
              <div class="kpi-sub">可用 {{ formatNumber(profileData.summary?.totalAvailableQty) }}</div>
            </div>
          </div>

          <div class="ranking-profile-section-title">分仓分库位库存分布</div>
          <el-table :data="profileData.warehouseStockList || []" size="small" border>
            <el-table-column label="仓库" prop="warehouseName" min-width="130" show-overflow-tooltip />
            <el-table-column label="库位" prop="locationName" min-width="110" show-overflow-tooltip />
            <el-table-column label="批次号" prop="batchNo" min-width="110" />
            <el-table-column label="库存" prop="quantity" align="right" width="80" />
            <el-table-column label="可用" prop="availableQuantity" align="right" width="80" />
            <el-table-column label="预警区间" align="center" width="115">
              <template #default="scope">
                {{ scope.row.warningMinQty ?? "-" }} ~ {{ scope.row.warningMaxQty ?? "-" }}
              </template>
            </el-table-column>
          </el-table>
        </template>
      </div>
    </el-drawer>
  </el-row>
</template>

<script setup>
import { listProductRanking } from "@/api/business/report"
import { getProductProfile } from "@/api/business/product"

const loading = ref(false)
const sortBy = ref("saleAmount")
const keyword = ref("")
const rankingList = ref([])

const profileOpen = ref(false)
const profileLoading = ref(false)
const profileData = ref(null)

const summaryMetrics = computed(() => {
  const list = rankingList.value || []
  let totalSaleAmount = 0
  let totalSaleQty = 0
  let totalGrossProfit = 0
  let totalReturnAmount = 0
  let totalStockQty = 0
  let highReturnCount = 0
  let fastTurnoverCount = 0
  for (const row of list) {
    totalSaleAmount += Number(row.saleAmount || 0)
    totalSaleQty += Number(row.saleQty || 0)
    totalGrossProfit += Number(row.grossProfitAmount || 0)
    totalReturnAmount += Number(row.returnAmount || 0)
    totalStockQty += Number(row.stockQty || 0)
    if (Number(row.returnRate || 0) >= 5) {
      highReturnCount += 1
    }
    if (Number(row.turnoverRate || 0) >= 1) {
      fastTurnoverCount += 1
    }
  }
  const overallGrossMarginRate = totalSaleAmount > 0 ? (totalGrossProfit * 100) / totalSaleAmount : 0
  return {
    totalSaleAmount,
    totalSaleQty,
    totalGrossProfit,
    overallGrossMarginRate,
    totalReturnAmount,
    totalStockQty,
    highReturnCount,
    fastTurnoverCount
  }
})

function formatMoney(val) {
  const num = Number(val || 0)
  return Number.isNaN(num) ? "0.00" : num.toFixed(2)
}

function formatRate(val) {
  const num = Number(val || 0)
  return Number.isNaN(num) ? "0.00" : num.toFixed(2)
}

function formatNumber(val) {
  const num = Number(val || 0)
  if (Number.isNaN(num)) return "0"
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

function getRankBadgeClass(index) {
  if (index === 0) return "rank-badge--gold"
  if (index === 1) return "rank-badge--silver"
  if (index === 2) return "rank-badge--bronze"
  return ""
}

function getMarginTagType(rate) {
  const val = Number(rate || 0)
  if (val >= 25) return "success"
  if (val >= 10) return "primary"
  if (val > 0) return "warning"
  return "info"
}

function getReturnTagType(rate) {
  const val = Number(rate || 0)
  if (val >= 10) return "danger"
  if (val >= 5) return "warning"
  return "success"
}

function getWarningTagType(status) {
  if (status === "缺货预警") return "danger"
  if (status === "积压预警") return "warning"
  return "success"
}

async function loadRankingList() {
  loading.value = true
  try {
    const res = await listProductRanking({
      sortBy: sortBy.value,
      keyword: keyword.value ? keyword.value.trim() : undefined
    })
    rankingList.value = Array.isArray(res.data) ? res.data : []
  } catch {
    rankingList.value = []
  } finally {
    loading.value = false
  }
}

async function openProductProfile(row) {
  if (!row?.productId) return
  profileOpen.value = true
  profileLoading.value = true
  profileData.value = null
  try {
    const res = await getProductProfile(row.productId)
    profileData.value = res.data || null
  } finally {
    profileLoading.value = false
  }
}

defineExpose({
  refresh: loadRankingList
})

onMounted(() => {
  loadRankingList()
})
</script>

<style scoped lang="scss">
.product-ranking-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.product-ranking-title-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.product-ranking-title {
  font-weight: 600;
  color: var(--app-text-primary);
}

.product-ranking-subtitle {
  font-size: 12px;
  color: var(--app-text-muted);
}

.product-ranking-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.product-ranking-kpi-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.product-ranking-kpi-card {
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid var(--erp-border);
  background: var(--erp-surface-soft);
}

.kpi-label {
  font-size: 12px;
  color: var(--erp-text-muted);
}

.kpi-value {
  margin-top: 4px;
  font-size: 19px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--erp-heading);
}

.kpi-value--success {
  color: var(--el-color-success);
}

.kpi-value--danger {
  color: var(--el-color-danger);
}

.kpi-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--app-text-secondary);
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  background: var(--app-surface-subtle);
  color: var(--app-text-secondary);
}

.rank-badge--gold {
  background: rgba(245, 158, 11, 0.18);
  color: #d97706;
}

.rank-badge--silver {
  background: rgba(148, 163, 184, 0.22);
  color: var(--app-text-primary);
}

.rank-badge--bronze {
  background: rgba(180, 83, 9, 0.16);
  color: #b45309;
}

.text-profit-pos {
  color: var(--el-color-success);
  font-weight: 600;
}

.text-profit-neg {
  color: var(--el-color-danger);
  font-weight: 600;
}

.ranking-profile-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ranking-profile-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid var(--app-border-soft);
  background: var(--app-surface-subtle);
}

.ranking-profile-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-text-primary);
}

.ranking-profile-meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--app-text-secondary);
}

.ranking-profile-kpis {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.ranking-profile-kpi {
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid var(--app-border-soft);
  background: var(--app-surface);
}

.ranking-profile-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-primary);
}

@media (max-width: 960px) {
  .product-ranking-kpi-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
