<template>
  <el-row :gutter="12" style="margin-top: 12px">
    <el-col :span="24">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>消息中心</span>
            <el-select :model-value="selectedMessageType" placeholder="请选择消息类型" clearable style="width: 220px" @update:model-value="handleMessageTypeChange">
              <el-option label="全部消息" value="all" />
              <el-option label="销售审核消息" value="sale_order_audit" />
              <el-option label="库存预警消息" value="stock_warning" />
              <el-option label="系统公告消息" value="notice" />
              <el-option label="应收到期消息" value="receivable" />
            </el-select>
          </div>
        </template>
        <el-table :data="messageList" border>
          <el-table-column label="消息类型" align="center" prop="message_type" width="140">
            <template #default="scope">
              <span>{{ getMessageTypeLabel(scope.row.message_type) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="消息标题" align="left" prop="message_title" />
          <el-table-column label="消息等级" align="center" prop="messageLevel" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.messageLevel === 'warning' ? 'danger' : 'info'">
                {{ scope.row.messageLevel === "warning" ? "预警" : "通知" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="时间" align="center" prop="message_time" width="200">
            <template #default="scope">
              <span>{{ parseTime(scope.row.message_time) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="140">
            <template #default="scope">
              <el-button link type="primary" @click="emit('message-action', scope.row)">
                {{ getMessageActionLabel(scope.row) }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
// 经营看板消息中心只负责展示和抛出动作，消息跳转与预览仍由父页统一处理。
defineProps({
  selectedMessageType: {
    type: String,
    default: "all"
  },
  messageList: {
    type: Array,
    default: () => []
  },
  parseTime: {
    type: Function,
    required: true
  },
  getMessageTypeLabel: {
    type: Function,
    required: true
  },
  getMessageActionLabel: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(["update:selectedMessageType", "message-action"])

// 回传消息类型筛选值，让父页继续掌管筛选状态。
function handleMessageTypeChange(messageType) {
  emit("update:selectedMessageType", messageType)
}
</script>
