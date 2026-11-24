<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="基础资料 · 客户管理"
      title="客户资料管理"
      subtitle="维护客户档案、授信额度、账期天数、客户等级与结算方式，查看往来统计与跟进记录。"
      icon="User"
      tone="blue"
      :badge="`共 ${total} 家客户`"
    >
      <template #actions>
        <el-button icon="Upload" plain @click="handleImport" v-hasPermi="['business:customer:import']">批量导入</el-button>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:customer:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:customer:add']">新增客户</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="customerStatItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="客户编码" prop="customerCode">
          <el-input v-model="queryParams.customerCode" placeholder="请输入客户编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable style="width: 190px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="queryParams.contactPerson" placeholder="请输入联系人" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="付款方式" prop="paymentMethod">
          <el-select v-model="queryParams.paymentMethod" clearable style="width: 150px" placeholder="全部方式">
            <el-option v-for="option in paymentMethodOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级" prop="customerLevel">
          <el-select v-model="queryParams.customerLevel" clearable style="width: 150px" placeholder="全部等级">
            <el-option v-for="option in customerLevelOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable style="width: 130px" placeholder="全部状态">
            <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
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
          <erp-status-tabs
            :model-value="queryParams.status ?? ''"
            :items="statusTabItems"
            @change="handleQuickStatus"
          />
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:customer:edit']">修改</el-button>
          <el-button type="primary" plain icon="ChatDotRound" :disabled="single" @click="handleViewCustomerFollow" v-hasPermi="['business:customerFollow:list']">客户跟进</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:customer:remove']">删除</el-button>
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="customerList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="客户编码" align="center" prop="customerCode" min-width="125" show-overflow-tooltip>
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.customerCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户名称 / 地址" align="left" prop="customerName" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.customerName }}</div>
            <div class="erp-cell-sub">{{ scope.row.address || '未填写地址' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="联系人 / 电话" align="left" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.contactPerson || '-' }}</div>
            <div class="erp-cell-sub">{{ scope.row.contactPhone || scope.row.contactEmail || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="授信额度" align="right" prop="creditLimit" min-width="115">
          <template #default="scope">
            <span class="erp-amount">¥{{ formatMoney(scope.row.creditLimit) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="账期天数" align="center" prop="creditDays" width="90">
          <template #default="scope">
            <span>{{ scope.row.creditDays != null ? `${scope.row.creditDays} 天` : (scope.row.paymentMethod === 'cash' ? '0 天' : '30 天') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="付款方式" align="center" prop="paymentMethod" min-width="96">
          <template #default="scope">
            <el-tag size="small" type="info" effect="plain">{{ formatPaymentMethod(scope.row.paymentMethod) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="客户等级" align="center" prop="customerLevel" min-width="96">
          <template #default="scope">
            <el-tag size="small" :type="['vip', 'gold', 'a'].includes(scope.row.customerLevel) ? 'warning' : 'primary'" effect="light">
              {{ formatCustomerLevel(scope.row.customerLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="84">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="0"
              inactive-value="1"
              :disabled="!checkPermi(['business:customer:edit'])"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="DataAnalysis" @click="handleViewProfile(scope.row)" v-hasPermi="['business:customer:query']">画像</el-button>
            <el-button link type="primary" icon="ChatDotRound" @click="handleViewCustomerFollow(scope.row)" v-hasPermi="['business:customerFollow:list']">跟进</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:customer:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:customer:remove']">删除</el-button>
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
              <el-select v-model="form.paymentMethod" placeholder="请选择付款方式" style="width: 100%" @change="handlePaymentMethodChange">
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
        <el-row>
          <el-col :span="12">
            <el-form-item label="账期天数" prop="creditDays">
              <el-input-number v-model="form.creditDays" :min="0" :max="365" :precision="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
          </el-col>
        </el-row>
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

    <!-- 客户360度经营画像抽屉 -->
    <el-drawer v-model="profileOpen" title="客户经营画像" size="740px" append-to-body>
      <div v-loading="profileLoading" class="profile-drawer-body">
        <template v-if="profileData && profileData.customer">
          <div class="profile-header-card">
            <div class="profile-header-top">
              <div>
                <div class="profile-title">{{ profileData.customer.customerName }}</div>
                <div class="profile-subtitle">
                  编码：{{ profileData.customer.customerCode || '-' }} · 等级：{{ formatCustomerLevel(profileData.customer.customerLevel) || '-' }} · 联系人：{{ profileData.customer.contactPerson || '-' }}（{{ profileData.customer.contactPhone || '-' }}）
                </div>
              </div>
              <el-tag :type="getProfileRiskTagType(profileData.summary?.riskLevel)" effect="dark" size="large">
                {{ profileData.summary?.riskLevel || '正常' }}
              </el-tag>
            </div>
            <div class="profile-risk-note">{{ profileData.summary?.riskReason }}</div>
          </div>

          <div class="profile-kpi-grid">
            <div class="profile-kpi-item">
              <div class="profile-kpi-label">审核销售额</div>
              <div class="profile-kpi-value">¥{{ formatMoney(profileData.summary?.totalSaleAmount) }}</div>
              <div class="profile-kpi-sub">累计订单 {{ profileData.summary?.totalOrderCount || 0 }} 笔</div>
            </div>
            <div class="profile-kpi-item">
              <div class="profile-kpi-label">应收 / 已收</div>
              <div class="profile-kpi-value">¥{{ formatMoney(profileData.summary?.totalReceivableAmount) }}</div>
              <div class="profile-kpi-sub">已收 ¥{{ formatMoney(profileData.summary?.totalReceivedAmount) }}（回款率 {{ formatRate(profileData.summary?.collectionRate) }}%）</div>
            </div>
            <div class="profile-kpi-item">
              <div class="profile-kpi-label">未收 / 逾期</div>
              <div class="profile-kpi-value" :class="{ 'is-danger': Number(profileData.summary?.overdueAmount || 0) > 0 }">
                ¥{{ formatMoney(profileData.summary?.remainReceivableAmount) }}
              </div>
              <div class="profile-kpi-sub">逾期 ¥{{ formatMoney(profileData.summary?.overdueAmount) }}（{{ profileData.summary?.overdueCount || 0 }} 笔）</div>
            </div>
            <div class="profile-kpi-item">
              <div class="profile-kpi-label">信用额度占用</div>
              <div class="profile-kpi-value">{{ formatRate(profileData.summary?.creditUsageRate) }}%</div>
              <div class="profile-kpi-sub">授信 ¥{{ formatMoney(profileData.summary?.creditLimit) }}</div>
            </div>
          </div>

          <div class="profile-section-title">常购商品 Top 5</div>
          <el-table :data="profileData.topProductList || []" size="small" border class="mb16">
            <el-table-column label="商品编码" prop="productCode" min-width="110" />
            <el-table-column label="商品名称" prop="productName" min-width="160" show-overflow-tooltip />
            <el-table-column label="累计采购量" prop="totalQty" align="right" width="100" />
            <el-table-column label="累计金额" align="right" width="120">
              <template #default="scope">¥{{ formatMoney(scope.row.totalAmount) }}</template>
            </el-table-column>
          </el-table>

          <div class="profile-section-title">
            <span>最近销售订单</span>
            <el-button link type="primary" @click="jumpCustomerSaleOrders">查看全部订单</el-button>
          </div>
          <el-table :data="profileData.recentOrderList || []" size="small" border class="mb16">
            <el-table-column label="销售单号" prop="orderNo" min-width="150" />
            <el-table-column label="数量" prop="totalQty" align="right" width="80" />
            <el-table-column label="金额" align="right" width="110">
              <template #default="scope">¥{{ formatMoney(scope.row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="95" align="center">
              <template #default="scope">{{ formatOrderStatus(scope.row.status) }}</template>
            </el-table-column>
            <el-table-column label="下单时间" width="150" align="center">
              <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <div class="profile-section-title">
            <span>最近跟进记录</span>
            <el-button link type="primary" @click="handleViewCustomerFollow(profileData.customer)">去跟进</el-button>
          </div>
          <el-table :data="profileData.recentFollowList || []" size="small" border>
            <el-table-column label="跟进方式" width="90" align="center">
              <template #default="scope">{{ formatFollowType(scope.row.followType) }}</template>
            </el-table-column>
            <el-table-column label="跟进内容" prop="followContent" min-width="200" show-overflow-tooltip />
            <el-table-column label="跟进时间" width="150" align="center">
              <template #default="scope">{{ parseTime(scope.row.followTime) }}</template>
            </el-table-column>
          </el-table>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="Customer">
import { listCustomer, getCustomer, getCustomerProfile, addCustomer, updateCustomer, delCustomer, changeCustomerStatus, importTemplate as downloadImportTemplate } from "@/api/business/customer"
import { getToken } from "@/utils/auth"
import { parseTime } from "@/utils/ruoyi"
import { checkPermi } from "@/utils/permission"

const { proxy } = getCurrentInstance()
const route = useRoute()
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
const profileOpen = ref(false)
const profileLoading = ref(false)
const profileData = ref(null)

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
  { label: "月结", value: "month" }
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

const statusTabItems = [
  { label: '全部客户', value: '' },
  { label: '正常合作', value: '0', dot: 'emerald' },
  { label: '已停用', value: '1', dot: 'slate' }
]

const customerStatItems = computed(() => {
  const rows = customerList.value || []
  const vipCount = rows.filter(r => ['vip', 'gold', 'a'].includes(r.customerLevel)).length
  const totalCredit = rows.reduce((sum, r) => sum + Number(r.creditLimit || 0), 0)
  const activeCount = rows.filter(r => r.status === '0').length
  return [
    { label: '客户档案总数', value: total.value, suffix: '家', hint: '企业全量往来客户', icon: 'User', tone: 'blue' },
    { label: '本页核心大客户', value: vipCount, suffix: '家', hint: 'VIP / 金牌 / A级客户', icon: 'Medal', tone: 'amber' },
    { label: '本页合计授信额', prefix: '¥', value: totalCredit.toFixed(2), hint: '信用额度风控基线', icon: 'CreditCard', tone: 'indigo' },
    { label: '本页正常合作', value: activeCount, suffix: `/ ${rows.length}`, hint: '启用状态允许销售开单', icon: 'CircleCheck', tone: 'emerald' }
  ]
})

function handleQuickStatus(val) {
  queryParams.value.status = val === '' ? undefined : val
  handleQuery()
}

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
    creditDays: 0,
    paymentMethod: "cash",
    customerLevel: "normal",
    status: "0",
    remark: undefined
  }
  proxy.resetForm("customerRef")
}

function handlePaymentMethodChange(val) {
  if (val === "cash") {
    form.value.creditDays = 0
  } else if (!form.value.creditDays || form.value.creditDays === 0) {
    form.value.creditDays = 30
  }
}

function formatPaymentMethod(paymentMethod) {
  if (paymentMethod === "bank") return "银行转账"
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

function handleViewProfile(row) {
  if (!row?.customerId) {
    return
  }
  profileOpen.value = true
  profileLoading.value = true
  profileData.value = null
  getCustomerProfile(row.customerId).then(response => {
    profileData.value = response.data || null
  }).finally(() => {
    profileLoading.value = false
  })
}

function jumpCustomerSaleOrders() {
  const customerId = profileData.value?.customer?.customerId
  if (!customerId) {
    return
  }
  profileOpen.value = false
  proxy.$router.push({ path: "/sales/saleOrder", query: { customerId } })
}

function formatMoney(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toFixed(2) : "0.00"
}

function formatRate(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toFixed(2) : "0.00"
}

function getProfileRiskTagType(riskLevel) {
  if (riskLevel === "高风险") return "danger"
  if (riskLevel === "中风险") return "warning"
  return "success"
}

function formatOrderStatus(status) {
  const map = {
    draft: "草稿",
    submitted: "待经理审核",
    manager_approved: "待财务审核",
    audited: "已审核",
    rejected: "已驳回"
  }
  return map[status] || status || "-"
}

function formatFollowType(followType) {
  const map = {
    phone: "电话",
    visit: "上门",
    wechat: "微信",
    email: "邮件",
    sms: "短信",
    meeting: "面谈"
  }
  return map[String(followType || "").toLowerCase()] || followType || "-"
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

// 删除按钮操作，兼容单条删除和批量删除，并在空选择时及时提醒用户。
function handleDelete(row) {
  const customerIdList = row?.customerId ? [row.customerId] : ids.value
  if (!customerIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的客户")
    return
  }
  const displayCustomerText = row?.customerId
    ? `${row.customerName || row.customerCode || row.customerId}`
    : `已选中的 ${ids.value.length} 个客户`
  proxy.$modal.confirm(`是否确认删除“${displayCustomerText}”？`).then(function () {
    return delCustomer(customerIdList)
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

// 判断当前是否来自首页一键新增客户入口。
function shouldAutoOpenCustomerFromRoute() {
  return route.query.mode === "create"
}

// 初始化客户页面列表，并在需要时自动打开新增客户弹窗。
function initializePage() {
  getList()
  if (shouldAutoOpenCustomerFromRoute()) {
    handleAdd()
  }
}

// 监听路由参数变化，保证首页重复进入客户页时新增入口会重新生效。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>

<style scoped lang="scss">
.profile-drawer-body {
  padding: 0 4px 16px;
}
.profile-header-card {
  padding: 16px 18px;
  border-radius: 12px;
  border: 1px solid var(--erp-border);
  background: var(--erp-surface-soft);
  margin-bottom: 16px;
}
.profile-header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.profile-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--erp-heading);
}
.profile-subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: var(--erp-text-secondary);
}
.profile-risk-note {
  margin-top: 8px;
  font-size: 12px;
  color: var(--erp-text-secondary);
}
.profile-kpi-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.profile-kpi-item {
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid var(--erp-border);
  background: var(--erp-surface);
  box-shadow: var(--erp-shadow-xs);
}
.profile-kpi-label {
  font-size: 12px;
  color: var(--erp-text-secondary);
}
.profile-kpi-value {
  margin-top: 6px;
  font-size: 20px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--erp-heading);
}
.profile-kpi-value.is-danger {
  color: var(--erp-danger);
}
.profile-kpi-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--erp-text-muted);
}
.profile-section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 14px 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-heading);
}
.mb16 {
  margin-bottom: 16px;
}
</style>
