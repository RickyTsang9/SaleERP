<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="盘点单编号" prop="checkId">
        <el-input v-model="queryParams.checkId" placeholder="请输入盘点单编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商品编号" prop="productId">
        <el-input v-model="queryParams.productId" placeholder="请输入商品编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:inventoryCheckItem:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:inventoryCheckItem:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:inventoryCheckItem:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:inventoryCheckItem:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="inventoryCheckItemList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="明细编号" align="center" prop="checkItemId" />
      <el-table-column label="盘点单编号" align="center" prop="checkId" />
      <el-table-column label="商品编号" align="center" prop="productId" />
      <el-table-column label="库位编号" align="center" prop="locationId" />
      <el-table-column label="批次号" align="center" prop="batchNo" />
      <el-table-column label="账面数量" align="center" prop="stockQty" />
      <el-table-column label="实盘数量" align="center" prop="actualQty" />
      <el-table-column label="差异数量" align="center" prop="diffQty" />
      <el-table-column label="单价" align="center" prop="price" />
      <el-table-column label="差异金额" align="center" prop="diffAmount" />
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:inventoryCheckItem:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:inventoryCheckItem:remove']">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="860px" append-to-body>
      <el-form ref="inventoryCheckItemRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="盘点单编号" prop="checkId">
              <el-input-number v-model="form.checkId" :min="1" controls-position="right" style="width: 100%" />
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
            <el-form-item label="账面数量" prop="stockQty">
              <el-input-number v-model="form.stockQty" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实盘数量" prop="actualQty">
              <el-input-number v-model="form.actualQty" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="差异数量" prop="diffQty">
              <el-input-number v-model="form.diffQty" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="form.price" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="差异金额" prop="diffAmount">
              <el-input-number v-model="form.diffAmount" :precision="2" controls-position="right" style="width: 100%" />
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

<script setup name="BusinessInventoryCheckItem">
import { listInventoryCheckItem, getInventoryCheckItem, addInventoryCheckItem, updateInventoryCheckItem, delInventoryCheckItem } from "@/api/business/inventoryCheckItem"

const { proxy } = getCurrentInstance()

const inventoryCheckItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    checkId: undefined,
    productId: undefined
  },
  rules: {
    checkId: [{ required: true, message: "盘点单编号不能为空", trigger: "blur" }],
    productId: [{ required: true, message: "商品编号不能为空", trigger: "blur" }],
    locationId: [{ required: true, message: "库位编号不能为空", trigger: "blur" }],
    batchNo: [{ required: true, message: "批次号不能为空", trigger: "blur" }],
    actualQty: [{ required: true, message: "实盘数量不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listInventoryCheckItem(queryParams.value).then(response => {
    inventoryCheckItemList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    checkItemId: undefined,
    checkId: undefined,
    productId: undefined,
    locationId: undefined,
    batchNo: undefined,
    stockQty: 0,
    actualQty: 0,
    diffQty: 0,
    price: 0,
    diffAmount: 0
  }
  proxy.resetForm("inventoryCheckItemRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.checkItemId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增库存盘点明细"
}

function handleUpdate(row) {
  reset()
  const checkItemId = row.checkItemId || ids.value
  getInventoryCheckItem(checkItemId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改库存盘点明细"
  })
}

function submitForm() {
  proxy.$refs["inventoryCheckItemRef"].validate(valid => {
    if (valid) {
      if (form.value.checkItemId !== undefined) {
        updateInventoryCheckItem(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addInventoryCheckItem(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const checkItemIds = row.checkItemId || ids.value
  proxy.$modal.confirm('是否确认删除库存盘点明细编号为"' + checkItemIds + '"的数据项？').then(function () {
    return delInventoryCheckItem(checkItemIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/inventoryCheckItem/export", {
    ...queryParams.value
  }, `inventoryCheckItem_${new Date().getTime()}.xlsx`)
}

getList()
</script>
