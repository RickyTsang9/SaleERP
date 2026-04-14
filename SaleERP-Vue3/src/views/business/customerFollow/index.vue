<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="客户" prop="customerId">
        <el-select
          v-model="queryParams.customerId"
          placeholder="请选择客户"
          clearable
          filterable
          remote
          reserve-keyword
          remote-show-suffix
          :remote-method="loadCustomerOptionList"
          :loading="customerListLoading"
          style="width: 220px"
        >
          <el-option
            v-for="customer in customerSelectOptionList"
            :key="customer.customerId"
            :label="customer.customerName"
            :value="customer.customerId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="跟进方式" prop="followType">
        <el-select v-model="queryParams.followType" placeholder="请选择跟进方式" clearable style="width: 180px">
          <el-option label="电话" value="电话" />
          <el-option label="上门" value="上门" />
          <el-option label="微信" value="微信" />
          <el-option label="邮件" value="邮件" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 180px" @change="handleStatusChange">
          <el-option label="进行中" value="0" />
          <el-option label="已转化" value="1" />
          <el-option label="已流失" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="12" class="mb8">
      <el-col :xs="24" :sm="8">
        <div class="follow-summary-card" :class="{ 'is-active': queryParams.todoType === 'processing' }" @click="handleTodoFilter('processing')">
          <div class="follow-summary-label">进行中跟进</div>
          <div class="follow-summary-value">{{ getTodoSummaryCount("processing_count") }}</div>
          <div class="follow-summary-desc">仍需持续推进的客户跟进</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="follow-summary-card" :class="{ 'is-active': queryParams.todoType === 'today' }" @click="handleTodoFilter('today')">
          <div class="follow-summary-label">今日待跟进</div>
          <div class="follow-summary-value warning">{{ getTodoSummaryCount("today_count") }}</div>
          <div class="follow-summary-desc">今天需要完成的客户触达</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="follow-summary-card" :class="{ 'is-active': queryParams.todoType === 'overdue' }" @click="handleTodoFilter('overdue')">
          <div class="follow-summary-label">已逾期待跟进</div>
          <div class="follow-summary-value danger">{{ getTodoSummaryCount("overdue_count") }}</div>
          <div class="follow-summary-desc">需要优先补跟进的客户</div>
        </div>
      </el-col>
    </el-row>

    <el-alert
      :title="getTodoAlertTitle()"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['business:customerFollow:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['business:customerFollow:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['business:customerFollow:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['business:customerFollow:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery"></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="customerFollowList"
      :row-class-name="getCustomerFollowRowClassName"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="客户" align="center" prop="customerId" min-width="180">
        <template #default="scope">
          <span>{{ getCustomerName(scope.row.customerId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="跟进状态" align="center" prop="status" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="待办提醒" align="center" width="130">
        <template #default="scope">
          <el-tag :type="getTodoReminderType(scope.row)">{{ getTodoReminderLabel(scope.row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下次跟进时间" align="center" prop="nextFollowTime" width="180">
        <template #default="scope">
          <span :class="getNextFollowTimeClassName(scope.row)">{{ formatNextFollowTime(scope.row.nextFollowTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最近跟进时间" align="center" prop="followTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.followTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="跟进方式" align="center" prop="followType" width="120" />
      <el-table-column label="跟进内容" align="center" prop="followContent" min-width="260" show-overflow-tooltip />
      <el-table-column label="跟进人" align="center" prop="createBy" width="120" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:customerFollow:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:customerFollow:remove']">删除</el-button>
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

    <!-- 添加或修改客户跟进记录对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="customerFollowRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="form.customerId"
                placeholder="请选择客户"
                style="width: 100%"
                clearable
                filterable
                remote
                reserve-keyword
                remote-show-suffix
                :remote-method="loadCustomerOptionList"
                :loading="customerListLoading"
              >
                <el-option
                  v-for="customer in customerSelectOptionList"
                  :key="customer.customerId"
                  :label="customer.customerName"
                  :value="customer.customerId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跟进方式" prop="followType">
              <el-select v-model="form.followType" placeholder="请选择跟进方式" style="width: 100%">
                <el-option label="电话" value="电话" />
                <el-option label="上门" value="上门" />
                <el-option label="微信" value="微信" />
                <el-option label="邮件" value="邮件" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跟进状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择跟进状态" style="width: 100%">
                <el-option label="进行中" value="0" />
                <el-option label="已转化" value="1" />
                <el-option label="已流失" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跟进时间" prop="followTime">
              <el-date-picker
                clearable
                v-model="form.followTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择跟进时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次跟进时间" prop="nextFollowTime">
              <el-date-picker
                clearable
                v-model="form.nextFollowTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择下次跟进时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="跟进内容" prop="followContent">
              <el-input v-model="form.followContent" type="textarea" :rows="4" placeholder="请输入跟进内容" />
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

<script setup name="BusinessCustomerFollow">
import { listCustomerFollow, getCustomerFollow, delCustomerFollow, addCustomerFollow, updateCustomerFollow } from "@/api/business/customerFollow";
import { listCustomer, getCustomer } from "@/api/business/customer";
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect";
import { parseTime } from "@/utils/ruoyi";

const { proxy } = getCurrentInstance();
const route = useRoute();

const customerFollowList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const customerList = ref([]);
const customerListLoading = ref(false);
const todoSummary = ref({
  processing_count: 0,
  today_count: 0,
  overdue_count: 0
});
const todoSummaryQueryPageSize = 1000;

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    customerId: undefined,
    followType: undefined,
    status: undefined,
    todoType: undefined
  },
  rules: {
    customerId: [
      { required: true, message: "客户不能为空", trigger: "change" }
    ],
    followType: [
      { required: true, message: "跟进方式不能为空", trigger: "change" }
    ],
    followTime: [
      { required: true, message: "跟进时间不能为空", trigger: "change" }
    ],
    followContent: [
      { required: true, message: "跟进内容不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);
const customerSelectOptionList = computed(() => buildCustomerSelectOptionList());

// 根据路由参数恢复筛选条件，保证从客户页跳转后可以直接落到对应客户的跟进待办。
function initializeQueryParamsFromRoute() {
  queryParams.value.customerId = convertRouteNumberValue(route.query.customerId);
}

// 将路由中的数字参数转换为数值类型，避免空字符串影响筛选。
function convertRouteNumberValue(routeValue) {
  if (routeValue === undefined || routeValue === null || routeValue === "") {
    return undefined;
  }
  const numberValue = Number(routeValue);
  return Number.isNaN(numberValue) ? undefined : numberValue;
}

// 初始化客户远程下拉基础数据，保证首次进入页面即可按名称检索客户。
async function initBasicData() {
  await loadCustomerOptionList();
}

// 远程加载客户选项，避免一次性读取全部客户资料影响页面速度。
async function loadCustomerOptionList(searchKeyword) {
  customerListLoading.value = true;
  try {
    const response = await listCustomer({
      pageNum: 1,
      pageSize: 20,
      customerName: normalizeRemoteKeyword(searchKeyword)
    });
    const customerRowList = response.rows || [];
    customerRowList.forEach(customerItem => {
      appendUniqueSelectOption(customerList.value, customerItem, "customerId");
    });
  } finally {
    customerListLoading.value = false;
  }
}

// 按客户编号补齐名称选项，保证列表、搜索区和弹窗回显稳定。
async function ensureCustomerOptionLoaded(customerIdList) {
  const validCustomerIdList = [...new Set((customerIdList || []).filter(customerId => customerId !== undefined && customerId !== null && customerId !== ""))];
  for (const customerId of validCustomerIdList) {
    if (customerList.value.some(customerItem => customerItem.customerId === customerId)) {
      continue;
    }
    try {
      const response = await getCustomer(customerId);
      if (response?.data?.customerId !== undefined) {
        appendUniqueSelectOption(customerList.value, response.data, "customerId");
        continue;
      }
    } catch (error) {
      // 主数据缺失时保留友好提示，避免旧跟进记录显示空白。
    }
    appendUniqueSelectOption(customerList.value, { customerId, customerName: "客户资料缺失" }, "customerId");
  }
}

// 构造客户下拉选项，当前值未命中远程结果时补充资料缺失占位。
function buildCustomerSelectOptionList() {
  return buildSelectOptionList(customerList.value, [queryParams.value.customerId, form.value.customerId], "customerId", "customerName", "客户资料缺失");
}

// 返回客户名称，历史跟进记录缺主数据时统一提示资料缺失。
function getCustomerName(customerId) {
  const customer = customerList.value.find(customerItem => customerItem.customerId === customerId);
  return customer ? customer.customerName : (customerId ? "客户资料缺失" : "");
}

// 返回跟进状态名称，统一列表和弹窗口径。
function getStatusName(statusValue) {
  const statusMap = { "0": "进行中", "1": "已转化", "2": "已流失" };
  return statusMap[statusValue] || "未知";
}

// 返回跟进状态标签样式，帮助销售快速区分跟进进展。
function getStatusType(statusValue) {
  const typeMap = { "0": "primary", "1": "success", "2": "info" };
  return typeMap[statusValue] || "info";
}

// 解析跟进时间文本，兼容接口返回的日期字符串格式。
function parseDateTimeValue(dateTimeValue) {
  if (!dateTimeValue) {
    return undefined;
  }
  return new Date(String(dateTimeValue).replace(/-/g, "/"));
}

// 判断给定时间是否属于当天待办，用于今日跟进提醒。
function isTodayFollowDate(dateTimeValue) {
  const followDate = parseDateTimeValue(dateTimeValue);
  if (!followDate) {
    return false;
  }
  const currentDate = new Date();
  return followDate.getFullYear() === currentDate.getFullYear()
    && followDate.getMonth() === currentDate.getMonth()
    && followDate.getDate() === currentDate.getDate();
}

// 判断跟进记录是否已经逾期，优先提示需要补跟进的客户。
function isOverdueFollow(customerFollowRow) {
  if (!customerFollowRow || customerFollowRow.status !== "0") {
    return false;
  }
  const nextFollowDate = parseDateTimeValue(customerFollowRow.nextFollowTime);
  return !!nextFollowDate && nextFollowDate.getTime() < Date.now();
}

// 判断跟进记录是否属于当天待办，帮助销售识别今日重点事项。
function isTodayFollow(customerFollowRow) {
  return !!customerFollowRow && customerFollowRow.status === "0" && isTodayFollowDate(customerFollowRow.nextFollowTime);
}

// 返回待办提醒文案，突出逾期、今日和未安排三类常见跟进场景。
function getTodoReminderLabel(customerFollowRow) {
  if (isOverdueFollow(customerFollowRow)) {
    return "已逾期";
  }
  if (isTodayFollow(customerFollowRow)) {
    return "今天跟进";
  }
  if (customerFollowRow?.status !== "0") {
    return "已结束";
  }
  if (!customerFollowRow?.nextFollowTime) {
    return "待安排";
  }
  return "正常";
}

// 返回待办提醒标签样式，方便列表里快速扫读优先级。
function getTodoReminderType(customerFollowRow) {
  if (isOverdueFollow(customerFollowRow)) {
    return "danger";
  }
  if (isTodayFollow(customerFollowRow)) {
    return "warning";
  }
  if (customerFollowRow?.status !== "0") {
    return "info";
  }
  if (!customerFollowRow?.nextFollowTime) {
    return "primary";
  }
  return "success";
}

// 格式化下次跟进时间，未安排时直接提示用户补计划。
function formatNextFollowTime(nextFollowTimeValue) {
  return nextFollowTimeValue ? parseTime(nextFollowTimeValue, "{y}-{m}-{d} {h}:{i}:{s}") : "未安排";
}

// 返回下次跟进时间样式，逾期记录在列表中更醒目。
function getNextFollowTimeClassName(customerFollowRow) {
  if (isOverdueFollow(customerFollowRow)) {
    return "is-danger";
  }
  if (isTodayFollow(customerFollowRow)) {
    return "is-warning";
  }
  return "";
}

// 返回跟进列表行样式，帮助销售在表格里优先处理重点客户。
function getCustomerFollowRowClassName({ row }) {
  if (isOverdueFollow(row)) {
    return "todo-overdue-row";
  }
  if (isTodayFollow(row)) {
    return "todo-today-row";
  }
  return "";
}

// 查询客户跟进记录列表，并补齐当前页所需的客户名称。
async function getList() {
  loading.value = true;
  try {
    if (queryParams.value.todoType) {
      const response = await listCustomerFollow(buildTodoSummaryListQueryParams());
      const filteredCustomerFollowRowList = filterCustomerFollowRowListByTodoType(response.rows || [], queryParams.value.todoType);
      total.value = filteredCustomerFollowRowList.length;
      customerFollowList.value = paginateCustomerFollowRowList(filteredCustomerFollowRowList);
    } else {
      const response = await listCustomerFollow(queryParams.value);
      customerFollowList.value = response.rows || [];
      total.value = response.total || 0;
    }
    await ensureCustomerOptionLoaded(customerFollowList.value.map(customerFollowItem => customerFollowItem.customerId));
  } finally {
    loading.value = false;
  }
}

// 生成待办摘要查询条件，仅保留影响待办统计口径的筛选项。
function buildTodoSummaryQueryParams() {
  return {
    customerId: queryParams.value.customerId,
    followType: queryParams.value.followType
  };
}

// 构造待办摘要和待办筛选的基础查询条件，统一复用进行中的跟进范围。
function buildTodoSummaryListQueryParams() {
  return {
    ...buildTodoSummaryQueryParams(),
    pageNum: 1,
    pageSize: todoSummaryQueryPageSize,
    status: "0"
  };
}

// 按待办类型过滤跟进记录，让顶部卡片和列表筛选保持同一套业务口径。
function filterCustomerFollowRowListByTodoType(customerFollowRowList, todoTypeValue) {
  if (todoTypeValue === "today") {
    return customerFollowRowList.filter(customerFollowRow => isTodayFollow(customerFollowRow));
  }
  if (todoTypeValue === "overdue") {
    return customerFollowRowList.filter(customerFollowRow => isOverdueFollow(customerFollowRow));
  }
  if (todoTypeValue === "processing") {
    return customerFollowRowList.filter(customerFollowRow => customerFollowRow?.status === "0");
  }
  return customerFollowRowList;
}

// 在前端按当前分页参数截取待办列表，保证待办卡片筛选在旧接口下也可用。
function paginateCustomerFollowRowList(customerFollowRowList) {
  const startIndex = (queryParams.value.pageNum - 1) * queryParams.value.pageSize;
  const endIndex = startIndex + queryParams.value.pageSize;
  return customerFollowRowList.slice(startIndex, endIndex);
}

// 查询客户跟进待办摘要，当前直接基于进行中跟进列表计算，兼容未部署摘要接口的环境。
async function loadTodoSummary() {
  const response = await listCustomerFollow(buildTodoSummaryListQueryParams());
  const customerFollowRowList = response.rows || [];
  todoSummary.value = {
    processing_count: Number(response.total || customerFollowRowList.length || 0),
    today_count: filterCustomerFollowRowListByTodoType(customerFollowRowList, "today").length,
    overdue_count: filterCustomerFollowRowListByTodoType(customerFollowRowList, "overdue").length
  };
}

// 并行刷新列表与待办摘要，保证查询结果和顶部待办卡片同步更新。
async function refreshPageData() {
  await Promise.all([
    getList(),
    loadTodoSummary()
  ]);
}

// 返回待办摘要数值，统一处理空值情况。
function getTodoSummaryCount(summaryFieldName) {
  return todoSummary.value?.[summaryFieldName] || 0;
}

// 返回当前待办视图提示文案，让销售清楚当前看到的是哪类跟进记录。
function getTodoAlertTitle() {
  if (queryParams.value.todoType === "processing") {
    return "当前正在查看全部进行中的客户跟进，适合统一梳理还未结束的跟进事项。";
  }
  if (queryParams.value.todoType === "today") {
    return "当前仅展示今天需要跟进的客户，适合快速处理当日待办。";
  }
  if (queryParams.value.todoType === "overdue") {
    return "当前仅展示已逾期未跟进的客户，请优先补齐这部分跟进动作。";
  }
  return "跟进页已按待办优先级排序，逾期和今日待跟进客户会优先排在前面。";
}

// 处理待办卡片筛选，点击同一张卡片可恢复常规列表视图。
function handleTodoFilter(todoTypeValue) {
  queryParams.value.pageNum = 1;
  queryParams.value.todoType = queryParams.value.todoType === todoTypeValue ? undefined : todoTypeValue;
  if (queryParams.value.todoType) {
    queryParams.value.status = undefined;
  }
  getList();
}

// 手动切换状态筛选时清除待办快捷条件，避免筛选口径互相冲突。
function handleStatusChange() {
  queryParams.value.todoType = undefined;
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    followId: undefined,
    customerId: undefined,
    followType: "电话",
    followTime: parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}"),
    followContent: undefined,
    nextFollowTime: undefined,
    status: "0"
  };
  proxy.resetForm("customerFollowRef");
}

// 搜索按钮操作
async function handleQuery() {
  queryParams.value.pageNum = 1;
  await refreshPageData();
}

// 重置按钮操作
async function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.todoType = undefined;
  await handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(customerFollowItem => customerFollowItem.followId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

// 新增按钮操作
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加跟进记录";
}

// 修改按钮操作
function handleUpdate(row) {
  reset();
  const followId = row.followId || ids.value[0];
  getCustomerFollow(followId).then(async response => {
    form.value = response.data;
    await ensureCustomerOptionLoaded([form.value.customerId]);
    open.value = true;
    title.value = "修改跟进记录";
  });
}

// 提交按钮
function submitForm() {
  proxy.$refs["customerFollowRef"].validate(valid => {
    if (valid) {
      if (form.value.followId != null) {
        updateCustomerFollow(form.value).then(async () => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          await refreshPageData();
        });
      } else {
        addCustomerFollow(form.value).then(async () => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          await refreshPageData();
        });
      }
    }
  });
}

// 生成删除提示文案，单条删除时优先展示客户与跟进时间，减少误删风险。
function buildDeleteFollowDisplayText(row) {
  if (row?.followId) {
    const customerName = getCustomerName(row.customerId);
    const followTimeText = row.followTime ? parseTime(row.followTime, "{y}-{m}-{d} {h}:{i}") : "未记录跟进时间";
    return `${customerName}（${followTimeText}）`;
  }
  return `已选中的 ${ids.value.length} 条跟进记录`;
}

// 删除按钮操作
function handleDelete(row) {
  const followIds = row?.followId || ids.value;
  const displayFollowText = buildDeleteFollowDisplayText(row);
  proxy.$modal.confirm(`是否确认删除“${displayFollowText}”？`).then(function() {
    return delCustomerFollow(followIds);
  }).then(async () => {
    await refreshPageData();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

// 导出按钮操作
function handleExport() {
  proxy.download("business/customerFollow/export", {
    ...queryParams.value
  }, `customerFollow_${new Date().getTime()}.xlsx`);
}

// 初始化页面筛选条件和数据，保证从客户页重复跳转时列表和待办卡片会同步刷新。
async function initializePage() {
  initializeQueryParamsFromRoute();
  await initBasicData();
  await refreshPageData();
}

// 监听同一路由下的查询参数变化，避免地址栏客户已切换但页面仍停留在旧跟进记录。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return;
  }
  initializePage();
});

initializePage();
</script>

<style scoped>
.follow-summary-card {
  padding: 18px 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfd 100%);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.follow-summary-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 10px 24px rgba(20, 48, 90, 0.08);
  transform: translateY(-1px);
}

.follow-summary-card.is-active {
  border-color: var(--el-color-primary);
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.14);
}

.follow-summary-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.follow-summary-value {
  margin-top: 10px;
  font-size: 30px;
  line-height: 1;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.follow-summary-value.warning {
  color: var(--el-color-warning);
}

.follow-summary-value.danger {
  color: var(--el-color-danger);
}

.follow-summary-desc {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.is-danger {
  color: var(--el-color-danger);
  font-weight: 600;
}

.is-warning {
  color: var(--el-color-warning);
  font-weight: 600;
}

:deep(.el-table .todo-overdue-row) {
  --el-table-tr-bg-color: #fff4f4;
}

:deep(.el-table .todo-today-row) {
  --el-table-tr-bg-color: #fff9ef;
}
</style>
