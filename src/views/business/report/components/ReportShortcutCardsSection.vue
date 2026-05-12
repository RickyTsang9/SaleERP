<template>
  <el-row :gutter="12" :style="{ marginTop }">
    <el-col v-for="shortcutCard in cardList" :key="shortcutCard.shortcutType" :span="columnSpan">
      <el-card shadow="hover" class="dashboard-action-card" @click="handleShortcut(shortcutCard.shortcutType)">
        <div class="stat-title">{{ shortcutCard.title }}</div>
        <div class="stat-value">{{ shortcutCard.value }}</div>
        <div class="stat-action">{{ shortcutCard.actionText }}</div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
// 经营看板快捷卡片只负责统一展示，具体跳转逻辑仍由父页根据业务类型处理。
defineProps({
  cardList: {
    type: Array,
    default: () => []
  },
  columnSpan: {
    type: Number,
    default: 4
  },
  marginTop: {
    type: String,
    default: "12px"
  }
})

const emit = defineEmits(["shortcut"])

// 回传快捷入口类型，让父页继续复用现有路由映射。
function handleShortcut(shortcutType) {
  emit("shortcut", shortcutType)
}
</script>

<style scoped>
.stat-title {
  color: #606266;
  font-size: 14px;
}

.stat-value {
  margin-top: 8px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.stat-action {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.dashboard-action-card {
  cursor: pointer;
}

.dashboard-action-card:hover .stat-action {
  color: #409eff;
}
</style>
