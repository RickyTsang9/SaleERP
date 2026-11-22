<template>
  <el-row :gutter="12" :style="{ marginTop }">
    <el-col v-for="shortcutCard in cardList" :key="shortcutCard.shortcutType" :xs="12" :sm="8" :md="8" :lg="columnSpan">
      <el-card shadow="hover" class="dashboard-action-card" @click="handleShortcut(shortcutCard.shortcutType)">
        <div class="stat-title">{{ shortcutCard.title }}</div>
        <div class="stat-value" :title="String(shortcutCard.value)">{{ shortcutCard.value }}</div>
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
  color: var(--erp-text-muted);
  font-size: 12.5px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-value {
  margin-top: 8px;
  color: var(--erp-heading);
  font-size: clamp(17px, 1.35vw, 22px);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-action {
  margin-top: 10px;
  color: var(--erp-text-muted);
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.18s ease;
}

.dashboard-action-card {
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.dashboard-action-card:hover {
  transform: translateY(-2px);
  border-color: rgba(var(--erp-primary-rgb), 0.4) !important;
  box-shadow: var(--erp-shadow-md) !important;
}

.dashboard-action-card :deep(.el-card__body) {
  padding: 16px 16px !important;
  overflow: hidden;
}

.dashboard-action-card:hover .stat-action {
  color: var(--erp-primary);
}
</style>
