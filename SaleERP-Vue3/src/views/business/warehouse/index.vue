<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="仓储基础架构"
      title="仓库管理"
      subtitle="配置实体与逻辑仓库节点、责任联系人及地址信息，支持一键穿透查看库位分布。"
      icon="House"
      tone="indigo"
      :badge="`共 ${total} 座仓库`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:warehouse:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:warehouse:add']">新增仓库</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="warehouseStatItems" :columns="3" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="仓库编码" prop="warehouseCode">
          <el-input v-model="queryParams.warehouseCode" placeholder="请输入仓库编码" clearable style="width: 185px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="queryParams.warehouseName" placeholder="请输入仓库名称" clearable style="width: 195px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="queryParams.contactPhone" placeholder="请输入联系电话" clearable style="width: 175px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable style="width: 140px" placeholder="全部状态">
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
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="statusTabItems" @change="handleQuickStatus" />
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:warehouse:edit']">修改</el-button>
          <el-button type="primary" plain icon="Grid" :disabled="single" @click="handleViewLocation" v-hasPermi="['business:location:list']">查看库位</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:warehouse:remove']">删除</el-button>
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="warehouseList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="仓库编码" align="center" prop="warehouseCode" min-width="125">
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.warehouseCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="仓库名称 / 地址" align="left" prop="warehouseName" min-width="210" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.warehouseName }}</div>
            <div class="erp-cell-sub">{{ scope.row.address || '未填写地址' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="负责人 / 电话" align="left" min-width="150">
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.contactName || '-' }}</div>
            <div class="erp-cell-sub">{{ scope.row.contactPhone || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Grid" @click="handleViewLocation(scope.row)" v-hasPermi="['business:location:list']">库位</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:warehouse:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:warehouse:remove']">删除</el-button>
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
      <el-form ref="warehouseRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="仓库编码" prop="warehouseCode">
              <el-input v-model="form.warehouseCode" placeholder="请输入仓库编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库名称" prop="warehouseName">
              <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入联系人" />
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
  </div>
</template>

<script setup name="Warehouse">
import { listWarehouse, getWarehouse, addWarehouse, updateWarehouse, delWarehouse } from "@/api/business/warehouse"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")

const warehouseList = ref([])
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
    warehouseCode: undefined,
    warehouseName: undefined,
    contactPhone: undefined,
    status: undefined
  },
  rules: {
    warehouseCode: [{ required: true, message: "仓库编码不能为空", trigger: "blur" }],
    warehouseName: [{ required: true, message: "仓库名称不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const statusTabItems = [
  { label: '全部仓库', value: '' },
  { label: '正常启用', value: '0', dot: 'emerald' },
  { label: '已停用', value: '1', dot: 'slate' }
]

const warehouseStatItems = computed(() => {
  const rows = warehouseList.value || []
  const activeCount = rows.filter(r => r.status === '0').length
  const disabledCount = rows.filter(r => r.status !== '0').length
  return [
    { label: '仓库节点总数', value: total.value, suffix: '座', hint: '全网已配置仓储中心', icon: 'House', tone: 'indigo' },
    { label: '本页运行中', value: activeCount, suffix: '座', hint: '正常承接出入库与调拨', icon: 'CircleCheck', tone: 'emerald' },
    { label: '本页已停用', value: disabledCount, suffix: '座', hint: '暂停收发货作业', icon: 'Remove', tone: 'slate' }
  ]
})

function handleQuickStatus(val) {
  queryParams.value.status = val === '' ? undefined : val
  handleQuery()
}

function getList() {
  loading.value = true
  listWarehouse(queryParams.value).then(response => {
    warehouseList.value = response.rows
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
    warehouseId: undefined,
    warehouseCode: undefined,
    warehouseName: undefined,
    contactName: undefined,
    contactPhone: undefined,
    address: undefined,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("warehouseRef")
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
  ids.value = selection.map(item => item.warehouseId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// 打开库位管理页，并带上仓库筛选，减少仓库管理员二次定位成本。
function handleViewLocation(row) {
  const warehouseRow = row || warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === ids.value[0])
  if (!warehouseRow?.warehouseId) {
    return
  }
  proxy.$router.push({
    path: "/warehouseGroup/location",
    query: {
      warehouseId: warehouseRow.warehouseId
    }
  })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增仓库"
}

function handleUpdate(row) {
  reset()
  const warehouseId = row.warehouseId || ids.value
  getWarehouse(warehouseId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改仓库"
  })
}

function submitForm() {
  proxy.$refs["warehouseRef"].validate(valid => {
    if (valid) {
      if (form.value.warehouseId !== undefined) {
        updateWarehouse(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addWarehouse(form.value).then(() => {
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
  const warehouseIdList = row?.warehouseId ? [row.warehouseId] : ids.value
  if (!warehouseIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的仓库")
    return
  }
  const displayWarehouseText = row?.warehouseId
    ? `${row.warehouseName || row.warehouseCode || row.warehouseId}`
    : `已选中的 ${ids.value.length} 个仓库`
  proxy.$modal.confirm(`是否确认删除“${displayWarehouseText}”？`).then(function () {
    return delWarehouse(warehouseIdList)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/warehouse/export", {
    ...queryParams.value
  }, `warehouse_${new Date().getTime()}.xlsx`)
}

getList()
</script>
