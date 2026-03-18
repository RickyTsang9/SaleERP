<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="仓库编号" prop="warehouseId">
        <el-input v-model="queryParams.warehouseId" placeholder="请输入仓库编号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商品编号" prop="productId">
        <el-input v-model="queryParams.productId" placeholder="请输入商品编号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="库位编号" prop="locationId">
        <el-input v-model="queryParams.locationId" placeholder="请输入库位编号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="批次号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="warning" icon="Warning" @click="handleWarningQuery">预警查询</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:stock:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:stock:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:stock:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:stock:export']">导出</el-button>
      </el-col>
      <el-col :span="3">
        <el-button type="warning" plain icon="AlarmClock" @click="handleWarningRemind" v-hasPermi="['business:stock:list']">
          触发预警提醒
        </el-button>
      </el-col>
      <el-col :span="3">
        <el-button type="danger" plain icon="Bell" @click="handleReadWarningMessage" v-hasPermi="['business:stock:list']">
          预警消息已读({{ warningUnreadCount }})
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stockList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="库存编号" align="center" prop="stockId" />
      <el-table-column label="仓库编号" align="center" prop="warehouseId" />
      <el-table-column label="商品编号" align="center" prop="productId" />
      <el-table-column label="库位编号" align="center" prop="locationId" />
      <el-table-column label="批次号" align="center" prop="batchNo" />
      <el-table-column label="库存数量" align="center" prop="quantity" />
      <el-table-column label="锁定数量" align="center" prop="lockedQuantity" />
      <el-table-column label="冻结数量" align="center" prop="frozenQuantity" />
      <el-table-column label="最小预警值" align="center" prop="warningMinQty" />
      <el-table-column label="最大预警值" align="center" prop="warningMaxQty" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:stock:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:stock:remove']">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form ref="stockRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="仓库编号" prop="warehouseId">
              <el-input-number v-model="form.warehouseId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编号" prop="productId">
              <el-input-number v-model="form.productId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="库位编号" prop="locationId">
              <el-input-number v-model="form.locationId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="库存数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="锁定数量" prop="lockedQuantity">
              <el-input-number v-model="form.lockedQuantity" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="冻结数量" prop="frozenQuantity">
              <el-input-number v-model="form.frozenQuantity" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input-number v-model="form.version" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="最小预警值" prop="warningMinQty">
              <el-input-number v-model="form.warningMinQty" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大预警值" prop="warningMaxQty">
              <el-input-number v-model="form.warningMaxQty" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessStock">
import { listStock, listWarningStock, remindWarningStock, getStock, addStock, updateStock, delStock } from "@/api/business/stock"
import { getUnreadMessageCount, listPopupMessage, readAllMessage } from "@/api/business/message"

const { proxy } = getCurrentInstance()
const route = useRoute()

const stockList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const isWarningQuery = ref(false)
const warningUnreadCount = ref(0)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    warehouseId: undefined,
    productId: undefined,
    locationId: undefined,
    batchNo: undefined
  },
  rules: {
    warehouseId: [{ required: true, message: "仓库编号不能为空", trigger: "blur" }],
    productId: [{ required: true, message: "商品编号不能为空", trigger: "blur" }],
    locationId: [{ required: true, message: "库位编号不能为空", trigger: "blur" }],
    batchNo: [{ required: true, message: "批次号不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  const queryAction = isWarningQuery.value ? listWarningStock : listStock
  queryAction(queryParams.value).then(response => {
    stockList.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

function checkWarningReminder() {
  getUnreadMessageCount("stock_warning").then(unreadResponse => {
    warningUnreadCount.value = unreadResponse.unreadCount || 0
    return listPopupMessage("stock_warning", 5)
  }).then(popupResponse => {
    const popupMessageList = popupResponse.rows || []
    if (popupMessageList.length === 0) {
      return
    }
    const popupTitleList = popupMessageList.map(messageItem => messageItem.messageTitle)
    const popupContent = popupTitleList.join("；")
    proxy.$modal.confirm(`当前存在${warningUnreadCount.value}条库存预警消息：${popupContent}。是否立即标记为已读？`).then(() => {
      return readAllMessage("stock_warning")
    }).then(() => {
      warningUnreadCount.value = 0
      proxy.$modal.msgSuccess("库存预警消息已标记为已读")
    }).catch(() => {})
  }).catch(() => {})
}

function handleWarningRemind() {
  remindWarningStock().then(response => {
    const warningCount = response.warningCount || 0
    if (warningCount === 0) {
      proxy.$modal.msgSuccess("当前没有库存预警数据")
      return
    }
    proxy.$modal.msgSuccess(`触发成功，当前预警商品共${warningCount}条`)
    checkWarningReminder()
  })
}

function handleReadWarningMessage() {
  if (warningUnreadCount.value === 0) {
    proxy.$modal.msgSuccess("当前没有未读库存预警消息")
    return
  }
  proxy.$modal.confirm(`是否将${warningUnreadCount.value}条库存预警消息全部标记为已读？`).then(() => {
    return readAllMessage("stock_warning")
  }).then(() => {
    warningUnreadCount.value = 0
    proxy.$modal.msgSuccess("操作成功")
  })
    .catch(() => {})
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    stockId: undefined,
    warehouseId: undefined,
    productId: undefined,
    locationId: undefined,
    batchNo: undefined,
    quantity: 0,
    lockedQuantity: 0,
    frozenQuantity: 0,
    warningMinQty: 0,
    warningMaxQty: 0,
    version: 0
  }
  proxy.resetForm("stockRef")
}

function handleQuery() {
  isWarningQuery.value = false
  queryParams.value.pageNum = 1
  getList()
}

function handleWarningQuery() {
  isWarningQuery.value = true
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  isWarningQuery.value = false
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.stockId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增库存"
}

function handleUpdate(row) {
  reset()
  const stockId = row.stockId || ids.value
  getStock(stockId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改库存"
  })
}

function submitForm() {
  proxy.$refs["stockRef"].validate(valid => {
    if (valid) {
      if (form.value.stockId !== undefined) {
        updateStock(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addStock(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const stockIds = row.stockId || ids.value
  proxy.$modal.confirm('是否确认删除库存编号为"' + stockIds + '"的数据项？').then(function () {
    return delStock(stockIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/stock/export", {
    ...queryParams.value
  }, `stock_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  if (route.query.warningQuery === "1")
  {
    isWarningQuery.value = true
  }
  getList()
  checkWarningReminder()
})
</script>
