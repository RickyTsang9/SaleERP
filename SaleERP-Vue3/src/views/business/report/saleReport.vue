<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="销售日期" style="width: 308px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable style="width: 200px">
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
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类" prop="categoryName">
        <el-input
          v-model="queryParams.categoryName"
          placeholder="请输入分类"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reportList">
      <el-table-column label="销售单号" align="center" prop="orderNo" width="180" />
      <el-table-column label="销售日期" align="center" prop="saleDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.saleDate, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="客户名称" align="center" prop="customerName" />
      <el-table-column label="商品编码" align="center" prop="productCode" />
      <el-table-column label="商品名称" align="center" prop="productName" />
      <el-table-column label="分类" align="center" prop="categoryName" />
      <el-table-column label="数量" align="center" prop="quantity" />
      <el-table-column label="单价(元)" align="center" prop="price" />
      <el-table-column label="金额(元)" align="center" prop="amount" />
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="SaleReport">
import { listSaleReport } from "@/api/business/report"
import { listCustomer } from "@/api/business/customer"

const { proxy } = getCurrentInstance()

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

getCustomerOptions()
getList()
</script>