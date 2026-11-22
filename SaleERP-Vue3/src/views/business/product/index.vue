<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="基础主数据"
      title="商品资料管理"
      subtitle="统一维护商品编码、规格条码、成本与标准售价，支持批量导入导出及单品经营画像洞察。"
      icon="Goods"
      tone="indigo"
      :badge="`共 ${total} 款商品`"
    >
      <template #actions>
        <el-button icon="Upload" plain @click="handleImport" v-hasPermi="['business:product:import']">批量导入</el-button>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:product:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:product:add']">新增商品</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="productStatItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="queryParams.productCode" placeholder="请输入商品编码" clearable style="width: 190px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="queryParams.productName" placeholder="请输入商品名称" clearable style="width: 190px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryName">
          <el-input v-model="queryParams.categoryName" placeholder="请输入分类" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable style="width: 150px" placeholder="全部状态">
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
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:product:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:product:remove']">删除</el-button>
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="商品编码" align="center" prop="productCode" min-width="125" show-overflow-tooltip>
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.productCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品名称 / 规格" align="left" prop="productName" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.productName }}</div>
            <div class="erp-cell-sub">{{ scope.row.productSpec || '标准规格' }} · {{ scope.row.unitName || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="分类 / 品牌" align="center" min-width="140" show-overflow-tooltip>
          <template #default="scope">
            <el-tag size="small" type="info" effect="plain">{{ scope.row.categoryName || '未分类' }}</el-tag>
            <div v-if="scope.row.brandName" class="erp-cell-sub">{{ scope.row.brandName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="成本价" align="right" prop="costPrice" width="108">
          <template #default="scope">
            <span class="erp-num">¥{{ formatMoney(scope.row.costPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标准售价" align="right" prop="salePrice" width="112">
          <template #default="scope">
            <span class="erp-amount is-primary">¥{{ formatMoney(scope.row.salePrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="88">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="0"
              inactive-value="1"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="165">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="DataAnalysis" @click="handleViewProfile(scope.row)" v-hasPermi="['business:product:query']">画像</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:product:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:product:remove']">删除</el-button>
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
      <el-form ref="productRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="商品编码" prop="productCode">
              <el-input v-model="form.productCode" placeholder="请输入商品编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="规格" prop="productSpec">
              <el-input v-model="form.productSpec" placeholder="请输入规格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unitName">
              <el-input v-model="form.unitName" placeholder="请输入单位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="条码" prop="barCode">
              <el-input v-model="form.barCode" placeholder="请输入条码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryName">
              <el-input v-model="form.categoryName" placeholder="请输入分类" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brandName">
              <el-input v-model="form.brandName" placeholder="请输入品牌" />
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
        <el-row>
          <el-col :span="12">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售价" prop="salePrice">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图片地址" prop="productImage">
          <el-input v-model="form.productImage" placeholder="请输入图片地址" />
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

    <!-- 商品导入对话框 -->
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
              <el-checkbox v-model="upload.updateSupport" /> 是否更新已经存在的商品数据
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

    <!-- 商品经营画像抽屉 -->
    <el-drawer v-model="profileOpen" title="商品经营画像" size="740px" append-to-body>
      <div v-loading="profileLoading" class="profile-drawer-body">
        <template v-if="profileData && profileData.product">
          <div class="profile-header-card">
            <div class="profile-header-top">
              <div>
                <div class="profile-title">{{ profileData.product.productName }}</div>
                <div class="profile-subtitle">
                  编码：{{ profileData.product.productCode || '-' }} · 规格：{{ profileData.product.productSpec || '-' }} · 分类：{{ profileData.product.categoryName || '-' }} · 品牌：{{ profileData.product.brandName || '-' }}
                </div>
                <div class="profile-subtitle">
                  成本价：{{ profileData.product.costPrice === null || profileData.product.costPrice === undefined ? '权限脱敏' : ('¥' + formatMoney(profileData.product.costPrice)) }} · 标准销售价：¥{{ formatMoney(profileData.product.salePrice) }}
                </div>
              </div>
              <el-tag :type="getWarningTagType(profileData.summary?.warningStatus)" effect="dark" size="large">
                库存：{{ profileData.summary?.warningStatus || '正常' }}
              </el-tag>
            </div>
            <div style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap">
              <el-button size="small" type="primary" plain icon="Box" @click="handleJumpProductStock(profileData.product)">查看库存台账</el-button>
              <el-button size="small" type="info" plain icon="Document" @click="handleJumpProductStockLog(profileData.product)">查看库存流水</el-button>
              <el-button size="small" type="success" plain icon="TrendCharts" @click="handleJumpProductSaleReport(profileData.product)">查看销售明细</el-button>
            </div>
          </div>

          <div class="profile-kpi-grid">
            <div class="profile-kpi-item">
              <div class="profile-kpi-label">累计审核销售</div>
              <div class="profile-kpi-value">¥{{ formatMoney(profileData.summary?.totalSaleAmount) }}</div>
              <div class="profile-kpi-sub">销量 {{ profileData.summary?.totalSaleQty || 0 }}（涉及 {{ profileData.summary?.saleOrderCount || 0 }} 单）</div>
            </div>
            <div class="profile-kpi-item">
              <div class="profile-kpi-label">预估毛利 / 毛利率</div>
              <div class="profile-kpi-value">
                {{ profileData.summary?.grossProfitAmount === null || profileData.summary?.grossProfitAmount === undefined ? '权限脱敏' : ('¥' + formatMoney(profileData.summary?.grossProfitAmount)) }}
              </div>
              <div class="profile-kpi-sub">
                {{ profileData.summary?.grossMarginRate === null || profileData.summary?.grossMarginRate === undefined ? '当前角色未授权查看成本毛利' : (`毛利率 ${formatRate(profileData.summary?.grossMarginRate)}% · 成本 ¥${formatMoney(profileData.summary?.estimatedCostAmount)}`) }}
              </div>
            </div>
            <div class="profile-kpi-item">
              <div class="profile-kpi-label">退货统计</div>
              <div class="profile-kpi-value">{{ formatRate(profileData.summary?.returnRate) }}%</div>
              <div class="profile-kpi-sub">退货 {{ profileData.summary?.returnQty || 0 }} 件 · 金额 ¥{{ formatMoney(profileData.summary?.returnAmount) }}</div>
            </div>
            <div class="profile-kpi-item">
              <div class="profile-kpi-label">当前总库存 / 可用</div>
              <div class="profile-kpi-value">{{ profileData.summary?.totalStockQty || 0 }}</div>
              <div class="profile-kpi-sub">可用 {{ profileData.summary?.totalAvailableQty || 0 }} · 累计采购 {{ profileData.summary?.totalPurchaseQty || 0 }}</div>
            </div>
          </div>

          <div class="profile-section-title">分仓分库位库存分布</div>
          <el-table :data="profileData.warehouseStockList || []" size="small" border>
            <el-table-column label="仓库" prop="warehouseName" min-width="130" show-overflow-tooltip />
            <el-table-column label="库位" prop="locationName" min-width="110" show-overflow-tooltip />
            <el-table-column label="批次号" prop="batchNo" min-width="110" />
            <el-table-column label="库存" prop="quantity" align="right" width="80" />
            <el-table-column label="可用" prop="availableQuantity" align="right" width="80" />
            <el-table-column label="预警区间" align="center" width="115">
              <template #default="scope">
                {{ scope.row.warningMinQty ?? '-' }} ~ {{ scope.row.warningMaxQty ?? '-' }}
              </template>
            </el-table-column>
          </el-table>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="BusinessProduct">
import { listProduct, getProduct, getProductProfile, addProduct, updateProduct, delProduct, changeProductStatus, importTemplate as downloadImportTemplate } from "@/api/business/product"
import { getToken } from "@/utils/auth"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const router = useRouter()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")

function handleJumpProductStock(product) {
  if (!product?.productId) return
  profileOpen.value = false
  router.push({ path: "/inventory/stock", query: { productId: String(product.productId) } })
}

function handleJumpProductStockLog(product) {
  if (!product?.productId) return
  profileOpen.value = false
  router.push({ path: "/inventory/stockLog", query: { productId: String(product.productId) } })
}

function handleJumpProductSaleReport(product) {
  if (!product?.productName) return
  profileOpen.value = false
  router.push({ path: "/finance/saleReport", query: { productName: product.productName } })
}

const productList = ref([])
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

/*** 商品导入参数 */
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
  url: import.meta.env.VITE_APP_BASE_API + "/business/product/importData"
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    productCode: undefined,
    productName: undefined,
    categoryName: undefined,
    status: undefined
  },
  rules: {
    productCode: [{ required: true, message: "商品编码不能为空", trigger: "blur" }],
    productName: [{ required: true, message: "商品名称不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const statusTabItems = [
  { label: '全部商品', value: '' },
  { label: '正常在售', value: '0', dot: 'emerald' },
  { label: '已停用', value: '1', dot: 'slate' }
]

const productStatItems = computed(() => {
  const rows = productList.value || []
  const enabledCount = rows.filter(r => r.status === '0').length
  const barcodeCount = rows.filter(r => Boolean(r.barCode)).length
  const avgMargin = rows.length
    ? rows.reduce((sum, r) => {
        const s = Number(r.salePrice || 0)
        const c = Number(r.costPrice || 0)
        return sum + (s > 0 ? ((s - c) / s) * 100 : 0)
      }, 0) / rows.length
    : 0
  return [
    { label: '商品档案总数', value: total.value, suffix: '款', hint: '系统全量 SKU 档案', icon: 'Goods', tone: 'indigo' },
    { label: '本页在售商品', value: enabledCount, suffix: `/ ${rows.length}`, hint: '启用状态可正常开单', icon: 'CircleCheck', tone: 'emerald' },
    { label: '本页条码覆盖', value: barcodeCount, suffix: '款', hint: '支持 PDA 扫码识别', icon: 'FullScreen', tone: 'blue' },
    { label: '本页参考毛利率', value: avgMargin.toFixed(1), suffix: '%', hint: '按标准售价与成本估算', icon: 'TrendCharts', tone: 'violet' }
  ]
})

function handleQuickStatus(val) {
  queryParams.value.status = val === '' ? undefined : val
  handleQuery()
}

function getList() {
  loading.value = true
  listProduct(queryParams.value).then(response => {
    productList.value = response.rows
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
    productId: undefined,
    productCode: undefined,
    productName: undefined,
    productSpec: undefined,
    unitName: undefined,
    barCode: undefined,
    categoryName: undefined,
    brandName: undefined,
    productImage: undefined,
    costPrice: undefined,
    salePrice: undefined,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("productRef")
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
  ids.value = selection.map(item => item.productId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增商品"
}

function handleUpdate(row) {
  reset()
  const productId = row.productId || ids.value
  getProduct(productId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改商品"
  })
}

function submitForm() {
  proxy.$refs["productRef"].validate(valid => {
    if (valid) {
      if (form.value.productId !== undefined) {
        updateProduct(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProduct(form.value).then(() => {
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
  const productIdList = row?.productId ? [row.productId] : ids.value
  if (!productIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的商品")
    return
  }
  proxy.$modal.confirm('是否确认删除商品编号为"' + productIdList.join(",") + '"的数据项？').then(function () {
    return delProduct(productIdList)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleStatusChange(row) {
  const targetStatusText = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + targetStatusText + '""' + row.productName + '"商品吗？').then(function () {
    return changeProductStatus({ productId: row.productId, status: row.status })
  }).then(() => {
    proxy.$modal.msgSuccess(targetStatusText + "成功")
  }).catch(() => {
    row.status = row.status === "0" ? "1" : "0"
  })
}

function handleExport() {
  proxy.download("business/product/export", {
    ...queryParams.value
  }, `product_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
function handleImport() {
  upload.title = "商品导入";
  upload.open = true;
}

/** 下载模板操作 */
function importTemplate() {
  downloadImportTemplate().then(response => {
    proxy.download.saveAs(response, `product_template_${new Date().getTime()}.xlsx`)
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

function handleViewProfile(row) {
  if (!row?.productId) {
    return
  }
  profileOpen.value = true
  profileLoading.value = true
  profileData.value = null
  getProductProfile(row.productId).then(response => {
    profileData.value = response.data || null
  }).finally(() => {
    profileLoading.value = false
  })
}

function formatMoney(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toFixed(2) : "0.00"
}

function formatRate(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toFixed(2) : "0.00"
}

function getWarningTagType(status) {
  if (status === "低于下限" || status === "无库存") return "danger"
  if (status === "超出上限") return "warning"
  return "success"
}

getList()
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
.profile-kpi-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--erp-text-muted);
}
.profile-section-title {
  margin: 14px 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-heading);
}
</style>
