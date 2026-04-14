<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="仓库" prop="warehouseId">
        <el-select
          v-model="queryParams.warehouseId"
          placeholder="请选择仓库"
          clearable
          filterable
          remote
          reserve-keyword
          remote-show-suffix
          :remote-method="loadWarehouseOptionList"
          :loading="warehouseListLoading"
          style="width: 220px"
        >
          <el-option
            v-for="warehouse in warehouseSelectOptionList"
            :key="warehouse.warehouseId"
            :label="warehouse.warehouseName"
            :value="warehouse.warehouseId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="库位编码" prop="locationCode">
        <el-input v-model="queryParams.locationCode" placeholder="请输入库位编码" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="库位名称" prop="locationName">
        <el-input v-model="queryParams.locationName" placeholder="请输入库位名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 200px" placeholder="请选择状态">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:location:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:location:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:location:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:location:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="locationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="仓库" align="center" prop="warehouseId" min-width="180">
        <template #default="scope">
          <span>{{ getWarehouseName(scope.row.warehouseId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库位编码" align="center" prop="locationCode" min-width="140" />
      <el-table-column label="库位名称" align="center" prop="locationName" min-width="180" />
      <el-table-column label="状态" align="center" prop="status" width="120">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:location:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:location:remove']">删除</el-button>
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
      <el-form ref="locationRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属仓库" prop="warehouseId">
              <el-select
                v-model="form.warehouseId"
                placeholder="请选择仓库"
                clearable
                filterable
                remote
                reserve-keyword
                remote-show-suffix
                :remote-method="loadWarehouseOptionList"
                :loading="warehouseListLoading"
                style="width: 100%"
              >
                <el-option
                  v-for="warehouse in warehouseSelectOptionList"
                  :key="warehouse.warehouseId"
                  :label="warehouse.warehouseName"
                  :value="warehouse.warehouseId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库位编码" prop="locationCode">
              <el-input v-model="form.locationCode" placeholder="请输入库位编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="库位名称" prop="locationName">
              <el-input v-model="form.locationName" placeholder="请输入库位名称" />
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

<script setup name="BusinessLocation">
import { listLocation, getLocation, addLocation, updateLocation, delLocation } from "@/api/business/location"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")
const route = useRoute()

const locationList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const warehouseList = ref([])
const warehouseListLoading = ref(false)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    warehouseId: undefined,
    locationCode: undefined,
    locationName: undefined,
    status: undefined
  },
  rules: {
    warehouseId: [{ required: true, message: "所属仓库不能为空", trigger: "change" }],
    locationCode: [{ required: true, message: "库位编码不能为空", trigger: "blur" }],
    locationName: [{ required: true, message: "库位名称不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)
const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())

// 根据路由参数恢复库位筛选条件，支持从仓库页直接跳到指定仓库的库位列表。
function initializeQueryParamsFromRoute() {
  queryParams.value.warehouseId = convertRouteNumberValue(route.query.warehouseId)
}

// 将路由中的数字参数转换为数值类型，避免空字符串影响查询条件。
function convertRouteNumberValue(routeValue) {
  if (routeValue === undefined || routeValue === null || routeValue === "") {
    return undefined
  }
  const numberValue = Number(routeValue)
  return Number.isNaN(numberValue) ? undefined : numberValue
}

// 初始化仓库下拉数据，保证页面打开后即可按名称选择仓库。
async function initBasicData() {
  await loadWarehouseOptionList()
}

// 远程加载仓库下拉选项，避免一次性加载全量仓库资料。
async function loadWarehouseOptionList(searchKeyword) {
  warehouseListLoading.value = true
  try {
    const response = await listWarehouse({
      pageNum: 1,
      pageSize: 20,
      warehouseName: normalizeRemoteKeyword(searchKeyword)
    })
    const warehouseRowList = response.rows || []
    warehouseRowList.forEach(warehouseItem => {
      appendUniqueSelectOption(warehouseList.value, warehouseItem, "warehouseId")
    })
  } finally {
    warehouseListLoading.value = false
  }
}

// 按仓库编号补齐名称数据，保证列表、搜索区和弹窗都能正确显示仓库名称。
async function ensureWarehouseOptionLoaded(warehouseIdList) {
  const validWarehouseIdList = [...new Set((warehouseIdList || []).filter(warehouseId => warehouseId !== undefined && warehouseId !== null && warehouseId !== ""))]
  for (const warehouseId of validWarehouseIdList) {
    if (warehouseList.value.some(warehouseItem => warehouseItem.warehouseId === warehouseId)) {
      continue
    }
    try {
      const response = await getWarehouse(warehouseId)
      if (response?.data?.warehouseId !== undefined) {
        appendUniqueSelectOption(warehouseList.value, response.data, "warehouseId")
        continue
      }
    } catch (error) {
      // 兼容历史库位引用旧仓库编号时的名称回显。
    }
    appendUniqueSelectOption(warehouseList.value, { warehouseId, warehouseName: "仓库资料缺失" }, "warehouseId")
  }
}

// 构造仓库下拉选项，当前值不在远程结果中时补充资料缺失兜底。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId, form.value.warehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 返回仓库名称，旧数据缺少主数据时保留明确的资料缺失提示。
function getWarehouseName(warehouseId) {
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse ? warehouse.warehouseName : (warehouseId ? "仓库资料缺失" : "")
}

// 查询库位列表，并按当前页数据补齐仓库名称显示。
async function getList() {
  loading.value = true
  try {
    const response = await listLocation(queryParams.value)
    locationList.value = response.rows || []
    total.value = response.total || 0
    await ensureWarehouseOptionLoaded(locationList.value.map(locationItem => locationItem.warehouseId))
  } finally {
    loading.value = false
  }
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    locationId: undefined,
    warehouseId: undefined,
    locationCode: undefined,
    locationName: undefined,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("locationRef")
}

// 搜索按钮操作
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

// 重置按钮操作
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(locationItem => locationItem.locationId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// 新增按钮操作
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增库位"
}

// 修改按钮操作
function handleUpdate(row) {
  reset()
  const locationId = row.locationId || ids.value[0]
  getLocation(locationId).then(async response => {
    form.value = response.data
    await ensureWarehouseOptionLoaded([form.value.warehouseId])
    open.value = true
    title.value = "修改库位"
  })
}

// 提交按钮
function submitForm() {
  proxy.$refs["locationRef"].validate(valid => {
    if (valid) {
      if (form.value.locationId !== undefined) {
        updateLocation(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addLocation(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

// 生成删除提示文案，单条删除时优先提示库位名称，减少仓库误删风险。
function buildDeleteLocationDisplayText(row) {
  if (row?.locationId) {
    return `${row.locationName || row.locationCode || row.locationId}`
  }
  return `已选中的 ${ids.value.length} 个库位`
}

// 删除按钮操作
function handleDelete(row) {
  const locationIds = row?.locationId || ids.value
  const displayLocationText = buildDeleteLocationDisplayText(row)
  proxy.$modal.confirm(`是否确认删除“${displayLocationText}”？`).then(function () {
    return delLocation(locationIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 导出按钮操作
function handleExport() {
  proxy.download("business/location/export", {
    ...queryParams.value
  }, `location_${new Date().getTime()}.xlsx`)
}

// 初始化页面筛选条件和下拉数据，保证从仓库页重复跳转时库位列表会正确刷新。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await initBasicData()
  await getList()
}

// 监听同一路由下的查询参数变化，避免切换仓库后列表仍停留在旧筛选结果。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
