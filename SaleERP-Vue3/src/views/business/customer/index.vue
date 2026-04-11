<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户编码" prop="customerCode">
        <el-input
          v-model="queryParams.customerCode"
          placeholder="请输入客户编码"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input
          v-model="queryParams.customerName"
          placeholder="请输入客户名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系人" prop="contactPerson">
        <el-input
          v-model="queryParams.contactPerson"
          placeholder="请输入联系人"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="付款方式" prop="paymentMethod">
        <el-select v-model="queryParams.paymentMethod" clearable style="width: 200px" placeholder="请选择付款方式">
          <el-option
            v-for="option in paymentMethodOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="客户等级" prop="customerLevel">
        <el-select v-model="queryParams.customerLevel" clearable style="width: 200px" placeholder="请选择客户等级">
          <el-option
            v-for="option in customerLevelOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 200px" placeholder="请选择状态">
          <el-option
            v-for="dict in sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:customer:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:customer:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="ChatDotRound" :disabled="single" @click="handleViewCustomerFollow" v-hasPermi="['business:customerFollow:list']">客户跟进</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:customer:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['business:customer:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:customer:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="customerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="客户编码" align="center" prop="customerCode" />
      <el-table-column label="客户名称" align="center" prop="customerName" />
      <el-table-column label="联系人" align="center" prop="contactPerson" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" />
      <el-table-column label="邮箱" align="center" prop="contactEmail" />
      <el-table-column label="地址" align="center" prop="address" min-width="180" show-overflow-tooltip />
      <el-table-column label="信用额度" align="center" prop="creditLimit" />
      <el-table-column label="付款方式" align="center" prop="paymentMethod">
        <template #default="scope">
          <span>{{ formatPaymentMethod(scope.row.paymentMethod) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="客户等级" align="center" prop="customerLevel">
        <template #default="scope">
          <span>{{ formatCustomerLevel(scope.row.customerLevel) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="120">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="ChatDotRound" @click="handleViewCustomerFollow(scope.row)" v-hasPermi="['business:customerFollow:list']">跟进</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:customer:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:customer:remove']">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="customerRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户编码" prop="customerCode">
              <el-input v-model="form.customerCode" placeholder="请输入客户编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="contactEmail">
              <el-input v-model="form.contactEmail" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="信用额度" prop="creditLimit">
              <el-input-number v-model="form.creditLimit" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款方式" prop="paymentMethod">
              <el-select v-model="form.paymentMethod" placeholder="请选择付款方式" style="width: 100%">
                <el-option
                  v-for="option in paymentMethodOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="客户等级" prop="customerLevel">
          <el-select v-model="form.customerLevel" placeholder="请选择客户等级" style="width: 100%">
            <el-option
              v-for="option in customerLevelOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 客户导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport" /> 是否更新已经存在的客户数据
            </div>
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessCustomer">
import { listCustomer, getCustomer, addCustomer, updateCustomer, delCustomer, changeCustomerStatus, importTemplate as downloadImportTemplate } from "@/api/business/customer"
import { getToken } from "@/utils/auth"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")

const customerList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

/*** 客户导入参数 */
const upload = reactive({
  // 是否显示弹出层
  open: false,
  // 弹出层标题
  title: "",
  // 是否禁用上传
  isUploading: false,
  // 是否更新已经存在的数据
  updateSupport: 0,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/business/customer/importData"
})

const paymentMethodOptions = ref([
  { label: "现金", value: "cash" },
  { label: "银行转账", value: "bank_transfer" },
  { label: "赊销", value: "credit" },
  { label: "月结", value: "month" },
  { label: "银行转账", value: "bank" }
])
const customerLevelOptions = ref([
  { label: "普通", value: "normal" },
  { label: "银牌", value: "silver" },
  { label: "金牌", value: "gold" },
  { label: "VIP", value: "vip" },
  { label: "A级客户", value: "a" },
  { label: "B级客户", value: "b" },
  { label: "C级客户", value: "c" }
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    customerCode: undefined,
    customerName: undefined,
    contactPerson: undefined,
    paymentMethod: undefined,
    customerLevel: undefined,
    status: undefined
  },
  rules: {
    customerCode: [{ required: true, message: "客户编码不能为空", trigger: "blur" }],
    customerName: [{ required: true, message: "客户名称不能为空", trigger: "blur" }],
    paymentMethod: [{ required: true, message: "付款方式不能为空", trigger: "change" }],
    customerLevel: [{ required: true, message: "客户等级不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCustomer(queryParams.value).then(response => {
    customerList.value = response.rows
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
    customerId: undefined,
    customerCode: undefined,
    customerName: undefined,
    contactPerson: undefined,
    contactPhone: undefined,
    contactEmail: undefined,
    address: undefined,
    creditLimit: 0,
    paymentMethod: "cash",
    customerLevel: "normal",
    status: "0",
    remark: undefined
  }
  proxy.resetForm("customerRef")
}

function formatPaymentMethod(paymentMethod) {
  const targetOption = paymentMethodOptions.value.find(option => option.value === paymentMethod)
  return targetOption ? targetOption.label : paymentMethod
}

// 将客户等级编码统一转换为业务人员可读的中文名称，兼容历史 a/b/c 级别数据。
function formatCustomerLevel(customerLevel) {
  const targetOption = customerLevelOptions.value.find(option => option.value === customerLevel)
  return targetOption ? targetOption.label : customerLevel
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
  ids.value = selection.map(item => item.customerId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// 打开客户跟进页，并自动带上当前客户筛选，减少销售二次查找。
function handleViewCustomerFollow(row) {
  const customerRow = row || customerList.value.find(customerItem => customerItem.customerId === ids.value[0])
  if (!customerRow?.customerId) {
    return
  }
  proxy.$router.push({
    path: "/base/customerFollow",
    query: {
      customerId: customerRow.customerId
    }
  })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增客户"
}

function handleUpdate(row) {
  reset()
  const customerId = row.customerId || ids.value
  getCustomer(customerId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改客户"
  })
}

function submitForm() {
  proxy.$refs["customerRef"].validate(valid => {
    if (valid) {
      if (form.value.customerId !== undefined) {
        updateCustomer(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCustomer(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const customerIds = row.customerId || ids.value
  const displayCustomerText = row?.customerId
    ? `${row.customerName || row.customerCode || row.customerId}`
    : `已选中的 ${ids.value.length} 个客户`
  proxy.$modal.confirm(`是否确认删除“${displayCustomerText}”？`).then(function () {
    return delCustomer(customerIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleStatusChange(row) {
  const targetStatusText = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + targetStatusText + '""' + row.customerName + '"客户吗？').then(function () {
    return changeCustomerStatus({ customerId: row.customerId, status: row.status })
  }).then(() => {
    proxy.$modal.msgSuccess(targetStatusText + "成功")
  }).catch(() => {
    row.status = row.status === "0" ? "1" : "0"
  })
}

function handleExport() {
  proxy.download("business/customer/export", {
    ...queryParams.value
  }, `customer_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
function handleImport() {
  upload.title = "客户导入";
  upload.open = true;
}

/** 下载模板操作 */
function importTemplate() {
  downloadImportTemplate().then(response => {
    proxy.download.saveAs(response, `customer_template_${new Date().getTime()}.xlsx`)
  })
}

/**文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
};

/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].handleRemove(file);
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  getList();
};

/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
}

getList()
</script>
