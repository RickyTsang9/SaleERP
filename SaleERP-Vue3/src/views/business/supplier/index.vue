<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="基础主数据 · 供应链"
      title="供应商管理"
      subtitle="统一维护上游供应商档案与联系方式，支持直达采购订单及应付往来台账。"
      icon="OfficeBuilding"
      tone="emerald"
      :badge="`共 ${total} 家供应商`"
    >
      <template #actions>
        <el-button icon="Upload" plain @click="handleImport" v-hasPermi="['business:supplier:import', 'business:supplier:add']">批量导入</el-button>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:supplier:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:supplier:add']">新增供应商</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="supplierStatItems" :columns="3" />

    <el-alert v-if="routeSupplierNotice" :title="routeSupplierNotice" type="info" :closable="false" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="88px">
        <el-form-item label="供应商编码" prop="supplierCode">
          <el-input v-model="queryParams.supplierCode" placeholder="请输入编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="queryParams.supplierName" placeholder="请输入名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="queryParams.contactPerson" placeholder="请输入联系人" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
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
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="statusTabItems" @change="handleQuickStatus" />
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:supplier:edit']">修改</el-button>
          <el-button type="primary" plain icon="Tickets" :disabled="single" @click="handleViewPurchaseOrder" v-hasPermi="['business:purchaseOrder:list']">采购订单</el-button>
          <el-button type="primary" plain icon="Money" :disabled="single" @click="handleViewPayable" v-hasPermi="['business:payable:list']">应付台账</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:supplier:remove']">删除</el-button>
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="supplierList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="供应商编码" align="center" prop="supplierCode" min-width="125" show-overflow-tooltip>
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.supplierCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="供应商名称 / 地址" align="left" prop="supplierName" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.supplierName }}</div>
            <div class="erp-cell-sub">{{ scope.row.address || '未填写地址' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="联系人 / 电话" align="left" min-width="150">
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.contactPerson || '-' }}</div>
            <div class="erp-cell-sub">{{ scope.row.contactPhone || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="合作状态" align="center" prop="status" width="95">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" effect="light">
              {{ scope.row.status === '0' ? '正常合作' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="120">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="260">
          <template #default="scope">
            <el-button link type="primary" icon="Tickets" @click="handleViewPurchaseOrder(scope.row)" v-hasPermi="['business:purchaseOrder:list']">采购订单</el-button>
            <el-button link type="primary" icon="Money" @click="handleViewPayable(scope.row)" v-hasPermi="['business:payable:list']">应付</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:supplier:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:supplier:remove']">删除</el-button>
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

    <!-- 添加或修改供应商对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="supplierRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="供应商编码" prop="supplierCode">
              <el-input v-model="form.supplierCode" placeholder="请输入供应商编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>
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
          <el-col :span="12">
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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

    <!-- 供应商导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="420px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="() => (upload.isUploading = true)"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip"><el-checkbox v-model="upload.updateSupport" />是否更新已经存在的供应商数据</div>
            <span>仅允许导入 xls、xlsx 格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="upload.isUploading" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Supplier">
import { listSupplier, getSupplier, delSupplier, addSupplier, updateSupplier } from "@/api/business/supplier";
import { parseTime } from "@/utils/ruoyi";
import { getToken } from "@/utils/auth";

const { proxy } = getCurrentInstance();
const route = useRoute();

const supplierList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const routeSupplierNotice = ref("");
const upload = reactive({
  open: false,
  title: "供应商资料导入",
  isUploading: false,
  updateSupport: false,
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + "/business/supplier/importData"
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    supplierCode: undefined,
    supplierName: undefined,
    contactPerson: undefined,
    status: undefined
  },
  rules: {
    supplierCode: [
      { required: true, message: "供应商编码不能为空", trigger: "blur" }
    ],
    supplierName: [
      { required: true, message: "供应商名称不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

const statusTabItems = [
  { label: "全部供应商", value: "" },
  { label: "正常合作", value: "0", dot: "emerald" },
  { label: "已停用", value: "1", dot: "rose" }
];

const supplierStatItems = computed(() => {
  const rows = supplierList.value || [];
  const activeCount = rows.filter(r => r.status === "0").length;
  const disabledCount = rows.filter(r => r.status !== "0").length;
  return [
    { label: "供应商档案总数", value: total.value, suffix: "家", hint: "系统登记上游合作商", icon: "OfficeBuilding", tone: "emerald" },
    { label: "本页正常合作", value: activeCount, suffix: "家", hint: "可直接发起采购订单", icon: "CircleCheck", tone: "indigo" },
    { label: "本页停用供应商", value: disabledCount, suffix: "家", hint: "已暂停采购合作", icon: "CircleClose", tone: "slate" }
  ];
});

function handleQuickStatus(val) {
  queryParams.value.status = val === "" ? undefined : val;
  handleQuery();
}

// 根据来源路由恢复供应商筛选条件，支持从采购订单直接落到指定供应商资料。
async function initializeQueryParamsFromRoute() {
  queryParams.value.supplierName = undefined;
  queryParams.value.supplierCode = undefined;
  routeSupplierNotice.value = "";
  if (!route.query.supplierId) {
    return;
  }
  try {
    const response = await getSupplier(route.query.supplierId);
    if (!response?.data?.supplierId) {
      routeSupplierNotice.value = "当前来源单据引用的供应商主数据已不存在，因此资料页暂无可展示记录。";
      return;
    }
    queryParams.value.supplierName = response.data.supplierName || undefined;
    queryParams.value.supplierCode = response.data.supplierCode || undefined;
    routeSupplierNotice.value = response.data.supplierName
      ? `当前仅展示供应商“${response.data.supplierName}”的资料。`
      : "";
  } catch (error) {
    // 保留普通列表查询逻辑，避免历史供应商或异常数据阻断页面打开。
    routeSupplierNotice.value = "当前来源单据引用的供应商主数据已不存在，因此资料页暂无可展示记录。";
  }
}

/** 查询供应商列表 */
function getList() {
  loading.value = true;
  listSupplier(queryParams.value).then(response => {
    supplierList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    supplierId: undefined,
    supplierCode: undefined,
    supplierName: undefined,
    contactPerson: undefined,
    contactPhone: undefined,
    contactEmail: undefined,
    address: undefined,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("supplierRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.supplierId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

// 打开供应商的采购订单列表，并自动带上供应商筛选，减少采购人员二次查找。
function handleViewPurchaseOrder(row) {
  const supplierRow = row || supplierList.value.find(supplierItem => supplierItem.supplierId === ids.value[0]);
  if (!supplierRow?.supplierId) {
    return;
  }
  proxy.$router.push({
    path: "/purchase/purchaseOrder",
    query: {
      supplierId: supplierRow.supplierId
    }
  });
}

// 打开供应商的应付台账列表，让采购和财务可以直接查看该供应商的待付款项。
function handleViewPayable(row) {
  const supplierRow = row || supplierList.value.find(supplierItem => supplierItem.supplierId === ids.value[0]);
  if (!supplierRow?.supplierId) {
    return;
  }
  proxy.$router.push({
    path: "/finance/payable",
    query: {
      supplierId: supplierRow.supplierId
    }
  });
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加供应商";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const supplierId = row.supplierId || ids.value;
  getSupplier(supplierId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改供应商";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["supplierRef"].validate(valid => {
    if (valid) {
      if (form.value.supplierId != undefined) {
        updateSupplier(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addSupplier(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作，兼容单条删除和批量删除，并在空选择时及时提醒用户。 */
function handleDelete(row) {
  const supplierIdList = row?.supplierId ? [row.supplierId] : ids.value;
  if (!supplierIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的供应商");
    return;
  }
  const displaySupplierText = row?.supplierId
    ? `${row.supplierName || row.supplierCode || row.supplierId}`
    : `已选中的 ${ids.value.length} 个供应商`;
  proxy.$modal.confirm(`是否确认删除“${displaySupplierText}”？`).then(function() {
    return delSupplier(supplierIdList);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('business/supplier/export', {
    ...queryParams.value
  }, `supplier_${new Date().getTime()}.xlsx`)
}

function handleImport() {
  upload.open = true;
}

function importTemplate() {
  proxy.download("business/supplier/importTemplate", {}, `supplier_template_${new Date().getTime()}.xlsx`);
}

function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
}

function handleFileSuccess(response) {
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].handleRemove();
  proxy.$alert("<div style='overflow: auto;max-height: 60vh;padding: 10px 16px;'>" + (response.msg || "导入完成") + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  getList();
}

// 初始化页面筛选条件和列表数据，保证从采购订单重复跳转时供应商资料会跟随最新参数刷新。
async function initializePage() {
  await initializeQueryParamsFromRoute();
  await getList();
}

// 监听同一路由下的查询参数变化，避免供应商资料页仍然停留在旧的来源供应商筛选结果。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return;
  }
  initializePage();
});

initializePage();
</script>
