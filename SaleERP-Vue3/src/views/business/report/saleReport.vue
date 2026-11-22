<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="经营报表"
      title="销售明细报表"
      subtitle="按销售日期、客户、商品及分类维度多维查询销售成交、退货冲减、成本快照与毛利明细。"
      icon="TrendCharts"
      tone="indigo"
      :badge="`共 ${total} 条记录`"
    >
      <template #actions>
        <el-button type="primary" plain icon="Download" @click="handleExport" v-hasPermi="['business:report:sale']">导出明细</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="statItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="销售日期">
          <el-date-picker
            v-model="dateRange"
            value-format="YYYY-MM-DD"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable style="width: 200px">
            <el-option
              v-for="item in customerOptions"
              :key="item.customerId"
              :label="item.customerName"
              :value="item.customerId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input
            v-model="queryParams.productName"
            placeholder="请输入商品名称"
            clearable
            style="width: 190px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="分类" prop="categoryName">
          <el-input
            v-model="queryParams.categoryName"
            placeholder="请输入分类"
            clearable
            style="width: 160px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="erp-table-card">
      <div class="erp-toolbar">
        <div class="erp-toolbar-left">
          <span class="erp-cell-sub">按当前筛选条件展示逐笔商品销售成交明细</span>
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="reportList">
        <el-table-column label="业务单号" align="center" prop="orderNo" width="215">
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.orderNo }}</span>
            <el-tag
              v-if="scope.row.billType === 'sale_return'"
              size="small"
              type="danger"
              effect="light"
              style="margin-left: 6px"
            >
              退货冲减
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="销售日期" align="center" prop="saleDate" width="175">
          <template #default="scope">
            <span>{{ parseTime(scope.row.saleDate, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户名称" align="left" prop="customerName" min-width="150" show-overflow-tooltip />
        <el-table-column label="商品信息" align="left" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.productName }}</div>
            <div class="erp-cell-sub">{{ scope.row.productCode }}</div>
          </template>
        </el-table-column>
        <el-table-column label="分类" align="center" prop="categoryName" width="110">
          <template #default="scope">
            <el-tag size="small" type="info" effect="plain">{{ scope.row.categoryName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="净销数量" align="right" prop="quantity" width="100">
          <template #default="scope">
            <span :class="Number(scope.row.quantity || 0) < 0 ? 'erp-amount is-danger' : 'erp-num'">{{ scope.row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价(元)" align="right" prop="price" width="110">
          <template #default="scope">
            <span>¥{{ Number(scope.row.price || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成交金额(元)" align="right" prop="amount" width="122">
          <template #default="scope">
            <span :class="['erp-amount', Number(scope.row.amount || 0) < 0 ? 'is-danger' : 'is-primary']">
              {{ Number(scope.row.amount || 0) < 0 ? '-' : '' }}¥{{ Math.abs(Number(scope.row.amount || 0)).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="成本金额(元)" align="right" prop="costAmount" width="118">
          <template #default="scope">
            <span v-if="scope.row.costAmount === null || scope.row.costAmount === undefined" class="erp-cell-sub">权限脱敏</span>
            <span v-else class="erp-num">{{ Number(scope.row.costAmount) < 0 ? '-' : '' }}¥{{ Math.abs(Number(scope.row.costAmount)).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="毛利额(元)" align="right" prop="grossProfit" width="118">
          <template #default="scope">
            <span v-if="scope.row.grossProfit === null || scope.row.grossProfit === undefined" class="erp-cell-sub">权限脱敏</span>
            <span v-else :class="['erp-amount', Number(scope.row.grossProfit) < 0 ? 'is-danger' : 'is-positive']">
              {{ Number(scope.row.grossProfit) < 0 ? '-' : '' }}¥{{ Math.abs(Number(scope.row.grossProfit)).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </section>
  </div>
</template>

<script setup name="SaleReport">
import { listSaleReport } from "@/api/business/report"
import { listCustomer } from "@/api/business/customer"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()

const reportList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])
const customerOptions = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    customerId: undefined,
    productName: undefined,
    categoryName: undefined
  }
})

const { queryParams } = toRefs(data)

const statItems = computed(() => {
  const rows = reportList.value || []
  const pageQty = rows.reduce((sum, r) => sum + Number(r.quantity || 0), 0)
  const pageAmount = rows.reduce((sum, r) => sum + Number(r.amount || 0), 0)
  const profitMasked = rows.length > 0 && rows.every(r => r.grossProfit === null || r.grossProfit === undefined)
  const pageProfit = rows.reduce((sum, r) => sum + Number(r.grossProfit || 0), 0)
  return [
    { label: '匹配明细总数', value: total.value, suffix: '笔', icon: 'Document', tone: 'indigo' },
    { label: '本页净销数量', value: pageQty, suffix: '件', icon: 'Goods', tone: 'blue' },
    { label: '本页净成交额', prefix: '¥', value: pageAmount.toFixed(2), icon: 'Money', tone: 'emerald' },
    { label: '本页净毛利额', prefix: profitMasked ? '' : '¥', value: profitMasked ? '权限脱敏' : pageProfit.toFixed(2), icon: 'TrendCharts', tone: 'violet' }
  ]
})

function getCustomerOptions() {
  listCustomer({ pageNum: 1, pageSize: 1000 }).then(response => {
    customerOptions.value = response.rows
  })
}

function getList() {
  loading.value = true
  let params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length > 0) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  listSaleReport(params).then(response => {
    reportList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleExport() {
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length > 0) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  proxy.download("business/report/saleExport", params, `sale_report_${Date.now()}.xlsx`)
}

if (route.query.productName) {
  queryParams.value.productName = String(route.query.productName)
}
getCustomerOptions()
getList()
</script>
