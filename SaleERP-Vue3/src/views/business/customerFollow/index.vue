<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="CRM 客户运营"
      title="客户跟进与回访"
      subtitle="按逾期与今日优先级推进客户触达，完整沉淀沟通记录与下次跟进计划。"
      icon="ChatDotRound"
      tone="violet"
      :badge="`共 ${total} 条记录`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:customerFollow:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:customerFollow:add']">新建跟进</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="followStatItems" :columns="3" @select="handleStatSelect" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="82px">
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
            style="width: 210px"
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
          <el-select v-model="queryParams.followType" placeholder="全部方式" clearable style="width: 150px">
            <el-option label="电话" value="电话" />
            <el-option label="上门" value="上门" />
            <el-option label="微信" value="微信" />
            <el-option label="邮件" value="邮件" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px" @change="handleStatusChange">
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
    </section>

    <el-alert :title="getTodoAlertTitle()" type="info" :closable="false" />

    <section class="erp-table-card">
      <div class="erp-toolbar">
        <div class="erp-toolbar-left">
          <erp-status-tabs :model-value="queryParams.todoType || ''" :items="followTabItems" @change="handleQuickTodoTab" />
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:customerFollow:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:customerFollow:remove']">删除</el-button>
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery" />
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="customerFollowList"
        :row-class-name="getCustomerFollowRowClassName"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" align="center" />
        <el-table-column label="客户" align="left" prop="customerId" min-width="155" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.customerName || getCustomerName(scope.row.customerId) }}</div>
            <div class="erp-cell-sub">跟进人：{{ scope.row.createBy || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态 / 提醒" align="center" width="115">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light" size="small">{{ getStatusName(scope.row.status) }}</el-tag>
            <div class="erp-cell-sub">
              <el-tag :type="getTodoReminderType(scope.row)" effect="plain" size="small" style="margin-top: 3px">{{ getTodoReminderLabel(scope.row) }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="下次跟进" align="center" prop="nextFollowTime" width="136">
          <template #default="scope">
            <span :class="getNextFollowTimeClassName(scope.row)">{{ formatNextFollowTime(scope.row.nextFollowTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最近跟进" align="center" prop="followTime" width="136">
          <template #default="scope">
            <span>{{ parseTime(scope.row.followTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="方式" align="center" prop="followType" width="76">
          <template #default="scope">
            <el-tag size="small" type="info" effect="plain">{{ getFollowTypeLabel(scope.row.followType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="跟进内容" align="left" prop="followContent" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" align="center" fixed="right" width="132">
          <template #default="scope">
            <div class="erp-row-actions">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:customerFollow:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:customerFollow:remove']">删除</el-button>
            </div>
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

const followStatItems = computed(() => [
  {
    key: "processing",
    label: "进行中跟进",
    value: getTodoSummaryCount("processing_count"),
    suffix: "项",
    hint: "点击筛选全部进行中跟进",
    icon: "ChatLineRound",
    tone: "indigo",
    clickable: true,
    active: queryParams.value.todoType === "processing"
  },
  {
    key: "today",
    label: "今日待跟进",
    value: getTodoSummaryCount("today_count"),
    suffix: "项",
    hint: "点击筛选今日计划触达客户",
    icon: "Clock",
    tone: "amber",
    clickable: true,
    active: queryParams.value.todoType === "today"
  },
  {
    key: "overdue",
    label: "已逾期待跟进",
    value: getTodoSummaryCount("overdue_count"),
    suffix: "项",
    hint: "请优先补齐逾期回访动作",
    icon: "Warning",
    tone: "rose",
    clickable: true,
    active: queryParams.value.todoType === "overdue"
  }
]);

const followTabItems = computed(() => [
  { label: "全部记录", value: "" },
  { label: "进行中", value: "processing", dot: "indigo", count: getTodoSummaryCount("processing_count") },
  { label: "今日待办", value: "today", dot: "amber", count: getTodoSummaryCount("today_count") },
  { label: "已逾期", value: "overdue", dot: "rose", count: getTodoSummaryCount("overdue_count") }
]);

function handleStatSelect(item) {
  if (item?.key) {
    handleTodoFilter(item.key);
  }
}

function handleQuickTodoTab(val) {
  queryParams.value.pageNum = 1;
  queryParams.value.todoType = val || undefined;
  if (queryParams.value.todoType) {
    queryParams.value.status = undefined;
  }
  getList();
}

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

// 返回跟进方式中文名称，兼容历史英文编码值（phone/visit/wechat/email）。
function getFollowTypeLabel(followType) {
  const followTypeMap = {
    phone: "电话",
    visit: "上门",
    wechat: "微信",
    email: "邮件",
    sms: "短信",
    meeting: "面谈"
  };
  return followTypeMap[String(followType || "").toLowerCase()] || followType || "-";
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

// 删除按钮操作，兼容单条删除和批量删除，并在空选择时及时提醒用户。
function handleDelete(row) {
  const followIdList = row?.followId ? [row.followId] : ids.value;
  if (!followIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的跟进记录");
    return;
  }
  const displayFollowText = buildDeleteFollowDisplayText(row);
  proxy.$modal.confirm(`是否确认删除“${displayFollowText}”？`).then(function() {
    return delCustomerFollow(followIdList);
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
  border: 1px solid var(--app-border, var(--el-border-color-light));
  border-radius: 12px;
  background: linear-gradient(180deg, var(--app-panel-bg, #ffffff) 0%, var(--app-panel-bg-soft, #fafbfd) 100%);
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
  --el-table-tr-bg-color: rgba(245, 108, 108, 0.1);
}

:deep(.el-table .todo-today-row) {
  --el-table-tr-bg-color: rgba(230, 162, 60, 0.1);
}
</style>
