<template>
  <div class="app-container">
    <el-alert
      v-if="routeSupplierNotice"
      :title="routeSupplierNotice"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="供应商编码" prop="supplierCode">
        <el-input
          v-model="queryParams.supplierCode"
          placeholder="请输入供应商编码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="供应商名称" prop="supplierName">
        <el-input
          v-model="queryParams.supplierName"
          placeholder="请输入供应商名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系人" prop="contactPerson">
        <el-input
          v-model="queryParams.contactPerson"
          placeholder="请输入联系人"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['business:supplier:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['business:supplier:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Tickets"
          :disabled="single"
          @click="handleViewPurchaseOrder"
          v-hasPermi="['business:purchaseOrder:list']"
        >采购订单</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Money"
          :disabled="single"
          @click="handleViewPayable"
          v-hasPermi="['business:payable:list']"
        >应付台账</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['business:supplier:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['business:supplier:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="supplierList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="供应商编码" align="center" prop="supplierCode" />
      <el-table-column label="供应商名称" align="center" prop="supplierName" />
      <el-table-column label="联系人" align="center" prop="contactPerson" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280">
        <template #default="scope">
          <el-button link type="primary" icon="Tickets" @click="handleViewPurchaseOrder(scope.row)" v-hasPermi="['business:purchaseOrder:list']">采购订单</el-button>
          <el-button link type="primary" icon="Money" @click="handleViewPayable(scope.row)" v-hasPermi="['business:payable:list']">应付</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:supplier:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:supplier:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

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
  </div>
</template>

<script setup name="BusinessSupplier">
import { listSupplier, getSupplier, delSupplier, addSupplier, updateSupplier } from "@/api/business/supplier";

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

// 根据来源路由恢复供应商筛选条件，支持从采购订单直接落到指定供应商资料。
async function initializeQueryParamsFromRoute() {
  if (!route.query.supplierId) {
    routeSupplierNotice.value = "";
    return;
  }
  try {
    const response = await getSupplier(route.query.supplierId);
    if (!response?.data?.supplierId) {
      routeSupplierNotice.value = `当前来源单据引用的是历史供应商ID：${route.query.supplierId}，该供应商主数据已不存在，因此资料页暂无可展示记录。`;
      return;
    }
    queryParams.value.supplierName = response.data.supplierName || undefined;
    queryParams.value.supplierCode = response.data.supplierCode || undefined;
    routeSupplierNotice.value = response.data.supplierName
      ? `当前仅展示供应商“${response.data.supplierName}”的资料。`
      : "";
  } catch (error) {
    // 保留普通列表查询逻辑，避免历史供应商或异常数据阻断页面打开。
    routeSupplierNotice.value = `当前来源单据引用的是历史供应商ID：${route.query.supplierId}，该供应商主数据已不存在，因此资料页暂无可展示记录。`;
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

/** 删除按钮操作 */
function handleDelete(row) {
  const supplierIds = row.supplierId || ids.value;
  const displaySupplierText = row?.supplierId
    ? `${row.supplierName || row.supplierCode || row.supplierId}`
    : `已选中的 ${ids.value.length} 个供应商`;
  proxy.$modal.confirm(`是否确认删除“${displaySupplierText}”？`).then(function() {
    return delSupplier(supplierIds);
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

initializeQueryParamsFromRoute().finally(() => {
  getList();
});
</script>
