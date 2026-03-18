<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="仓库编号" prop="warehouseId">
        <el-input v-model="queryParams.warehouseId" placeholder="请输入仓库编号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商品编号" prop="productId">
        <el-input v-model="queryParams.productId" placeholder="请输入商品编号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="单据类型" prop="billType">
        <el-input v-model="queryParams.billType" placeholder="请输入单据类型" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="单据号" prop="billNo">
        <el-input v-model="queryParams.billNo" placeholder="请输入单据号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="出入库方向" prop="inOut">
        <el-input v-model="queryParams.inOut" placeholder="请输入出入库方向" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:stockLog:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stockLogList">
      <el-table-column label="流水编号" align="center" prop="stockLogId" />
      <el-table-column label="仓库编号" align="center" prop="warehouseId" />
      <el-table-column label="商品编号" align="center" prop="productId" />
      <el-table-column label="库位编号" align="center" prop="locationId" />
      <el-table-column label="批次号" align="center" prop="batchNo" />
      <el-table-column label="单据类型" align="center" prop="billType" />
      <el-table-column label="单据号" align="center" prop="billNo" />
      <el-table-column label="单据编号" align="center" prop="billId" />
      <el-table-column label="出入库方向" align="center" prop="inOut" />
      <el-table-column label="变动数量" align="center" prop="quantity" />
      <el-table-column label="单价" align="center" prop="price" />
      <el-table-column label="金额" align="center" prop="amount" />
      <el-table-column label="变动前数量" align="center" prop="beforeQty" />
      <el-table-column label="变动后数量" align="center" prop="afterQty" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
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
  </div>
</template>

<script setup name="BusinessStockLog">
import { listStockLog } from "@/api/business/stockLog"

const { proxy } = getCurrentInstance()

const stockLogList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    warehouseId: undefined,
    productId: undefined,
    billType: undefined,
    billNo: undefined,
    inOut: undefined
  }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listStockLog(queryParams.value).then(response => {
    stockLogList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleExport() {
  proxy.download("business/stockLog/export", {
    ...queryParams.value
  }, `stock_log_${new Date().getTime()}.xlsx`)
}

getList()
</script>
