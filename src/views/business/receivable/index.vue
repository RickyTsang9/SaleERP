<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="销售单编号" prop="saleOrderId">
        <el-input v-model="queryParams.saleOrderId" placeholder="请输入销售单编号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户编号" prop="customerId">
        <el-input v-model="queryParams.customerId" placeholder="请输入客户编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="应收状态" prop="status">
        <el-input v-model="queryParams.status" placeholder="请输入应收状态" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:receivable:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:receivable:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:receivable:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:receivable:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="receivableList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="应收编号" align="center" prop="receivableId" />
      <el-table-column label="销售单编号" align="center" prop="saleOrderId" />
      <el-table-column label="客户编号" align="center" prop="customerId" />
      <el-table-column label="应收金额" align="center" prop="amountDue" />
      <el-table-column label="已收金额" align="center" prop="amountPaid" />
      <el-table-column label="应收状态" align="center" prop="status" />
      <el-table-column label="到期日期" align="center" prop="dueDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.dueDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:receivable:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:receivable:remove']">删除</el-button>
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
      <el-form ref="receivableRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="销售单编号" prop="saleOrderId">
              <el-input-number v-model="form.saleOrderId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户编号" prop="customerId">
              <el-input-number v-model="form.customerId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="应收金额" prop="amountDue">
              <el-input-number v-model="form.amountDue" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="已收金额" prop="amountPaid">
              <el-input-number v-model="form.amountPaid" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="应收状态" prop="status">
              <el-input v-model="form.status" placeholder="请输入应收状态" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期" prop="dueDate">
              <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择到期日期" style="width: 100%" />
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

<script setup name="BusinessReceivable">
import { listReceivable, getReceivable, addReceivable, updateReceivable, delReceivable } from "@/api/business/receivable"

const { proxy } = getCurrentInstance()
const route = useRoute()

const receivableList = ref([])
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
    saleOrderId: undefined,
    customerId: undefined,
    status: undefined
  },
  rules: {
    saleOrderId: [{ required: true, message: "销售单编号不能为空", trigger: "blur" }],
    customerId: [{ required: true, message: "客户编号不能为空", trigger: "blur" }],
    amountDue: [{ required: true, message: "应收金额不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listReceivable(queryParams.value).then(response => {
    receivableList.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    receivableId: undefined,
    saleOrderId: undefined,
    customerId: undefined,
    amountDue: 0,
    amountPaid: 0,
    status: "unpaid",
    dueDate: undefined,
    remark: undefined
  }
  proxy.resetForm("receivableRef")
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
  ids.value = selection.map(item => item.receivableId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增应收台账"
}

function handleUpdate(row) {
  reset()
  const receivableId = row.receivableId || ids.value[0]
  getReceivable(receivableId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改应收台账"
  })
}

function submitForm() {
  proxy.$refs["receivableRef"].validate(valid => {
    if (valid) {
      if (form.value.receivableId !== undefined) {
        updateReceivable(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addReceivable(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const receivableIds = row.receivableId || ids.value
  proxy.$modal.confirm('是否确认删除应收编号为"' + receivableIds + '"的数据项？').then(function () {
    return delReceivable(receivableIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/receivable/export", {
    ...queryParams.value
  }, `receivable_${new Date().getTime()}.xlsx`)
}

if (route.query.saleOrderId)
{
  queryParams.value.saleOrderId = route.query.saleOrderId
}
getList()
</script>
