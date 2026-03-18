<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="应收编号" prop="receivableId">
        <el-input v-model="queryParams.receivableId" placeholder="请输入应收编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="销售单编号" prop="saleOrderId">
        <el-input v-model="queryParams.saleOrderId" placeholder="请输入销售单编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户编号" prop="customerId">
        <el-input v-model="queryParams.customerId" placeholder="请输入客户编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="回款方式" prop="paymentMethod">
        <el-input v-model="queryParams.paymentMethod" placeholder="请输入回款方式" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="回款时间" style="width: 360px">
        <el-date-picker
          v-model="paymentTimeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:receipt:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:receipt:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:receipt:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:receipt:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="receiptList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="回款编号" align="center" prop="receiptId" />
      <el-table-column label="应收编号" align="center" prop="receivableId" />
      <el-table-column label="销售单编号" align="center" prop="saleOrderId" />
      <el-table-column label="客户编号" align="center" prop="customerId" />
      <el-table-column label="回款金额" align="center" prop="amount" />
      <el-table-column label="回款时间" align="center" prop="paymentTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.paymentTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="回款方式" align="center" prop="paymentMethod" />
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:receipt:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:receipt:remove']">删除</el-button>
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
      <el-form ref="receiptRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="应收编号" prop="receivableId">
              <el-input-number v-model="form.receivableId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售单编号" prop="saleOrderId">
              <el-input-number v-model="form.saleOrderId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户编号" prop="customerId">
              <el-input-number v-model="form.customerId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="回款金额" prop="amount">
              <el-input-number v-model="form.amount" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="回款时间" prop="paymentTime">
              <el-date-picker v-model="form.paymentTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择回款时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="回款方式" prop="paymentMethod">
              <el-input v-model="form.paymentMethod" placeholder="请输入回款方式" />
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

<script setup name="BusinessReceipt">
import { listReceipt, getReceipt, addReceipt, updateReceipt, delReceipt } from "@/api/business/receipt"

const { proxy } = getCurrentInstance()

const receiptList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const paymentTimeRange = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    receivableId: undefined,
    saleOrderId: undefined,
    customerId: undefined,
    paymentMethod: undefined
  },
  rules: {
    receivableId: [{ required: true, message: "应收编号不能为空", trigger: "blur" }],
    amount: [{ required: true, message: "回款金额不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  const requestParams = {
    ...queryParams.value,
    beginPaymentTime: paymentTimeRange.value && paymentTimeRange.value.length > 0 ? paymentTimeRange.value[0] : undefined,
    endPaymentTime: paymentTimeRange.value && paymentTimeRange.value.length > 1 ? paymentTimeRange.value[1] : undefined
  }
  listReceipt(requestParams).then(response => {
    receiptList.value = response.rows
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
    receiptId: undefined,
    receivableId: undefined,
    saleOrderId: undefined,
    customerId: undefined,
    amount: undefined,
    paymentTime: undefined,
    paymentMethod: "bank_transfer",
    remark: undefined
  }
  proxy.resetForm("receiptRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  paymentTimeRange.value = []
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.receiptId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增回款登记"
}

function handleUpdate(row) {
  reset()
  const receiptId = row.receiptId || ids.value[0]
  getReceipt(receiptId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改回款登记"
  })
}

function submitForm() {
  proxy.$refs["receiptRef"].validate(valid => {
    if (valid) {
      if (form.value.receiptId !== undefined) {
        updateReceipt(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addReceipt(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const receiptIds = row.receiptId || ids.value
  proxy.$modal.confirm('是否确认删除回款编号为"' + receiptIds + '"的数据项？').then(function () {
    return delReceipt(receiptIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  const requestParams = {
    ...queryParams.value,
    beginPaymentTime: paymentTimeRange.value && paymentTimeRange.value.length > 0 ? paymentTimeRange.value[0] : undefined,
    endPaymentTime: paymentTimeRange.value && paymentTimeRange.value.length > 1 ? paymentTimeRange.value[1] : undefined
  }
  proxy.download("business/receipt/export", {
    ...requestParams
  }, `receipt_${new Date().getTime()}.xlsx`)
}

getList()
</script>
