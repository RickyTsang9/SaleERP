<template>
  <div class="notice-bell-container">
    <el-popover placement="bottom" :width="350" trigger="click" v-model:visible="visible" @show="fetchNotices">
      <template #reference>
        <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="item">
          <el-icon><Bell /></el-icon>
        </el-badge>
      </template>

      <div class="notice-container" v-loading="loading">
        <div class="notice-header">
          <span>消息通知</span>
          <el-button link type="primary" @click="handleReadAll" :disabled="unreadCount === 0">全部已读</el-button>
        </div>
        
        <el-scrollbar height="300px">
          <div v-if="notices.length === 0" class="empty-notice">
            <el-empty description="暂无新消息" :image-size="60"></el-empty>
          </div>
          
          <ul v-else class="notice-list">
            <li v-for="item in notices" :key="item.messageId" class="notice-item" @click="handleRead(item)">
              <div class="notice-icon" :class="'icon-' + item.messageLevel">
                <el-icon v-if="item.messageLevel === 'warning'"><WarningFilled /></el-icon>
                <el-icon v-else-if="item.messageLevel === 'info'"><InfoFilled /></el-icon>
                <el-icon v-else><BellFilled /></el-icon>
              </div>
              <div class="notice-content">
                <div class="notice-title">
                  <span class="text-ellipsis" :title="item.messageTitle">{{ item.messageTitle }}</span>
                  <el-tag size="small" type="danger" v-if="item.status === '0'" effect="dark" round>未读</el-tag>
                </div>
                <div class="notice-desc text-ellipsis">{{ item.messageContent }}</div>
                <div class="notice-time">{{ parseTime(item.createTime) }}</div>
              </div>
            </li>
          </ul>
        </el-scrollbar>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Bell, WarningFilled, InfoFilled, BellFilled } from '@element-plus/icons-vue'
import { unreadCount as getUnreadCount, popupList, readMessage, readAllMessage } from '@/api/business/message'
import { parseTime } from '@/utils/ruoyi'

const visible = ref(false)
const unreadCount = ref(0)
const notices = ref([])
const loading = ref(false)
let timer = null

const fetchUnreadCount = () => {
  getUnreadCount().then(res => {
    unreadCount.value = res.unreadCount || 0
  })
}

const fetchNotices = () => {
  loading.value = true
  popupList({ limitCount: 10 }).then(res => {
    notices.value = res.rows || []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

const handleRead = (item) => {
  if (item.status === '0') {
    readMessage(item.messageId).then(() => {
      item.status = '1'
      fetchUnreadCount()
    })
  }
}

const handleReadAll = () => {
  readAllMessage().then(() => {
    fetchUnreadCount()
    fetchNotices()
  })
}

onMounted(() => {
  fetchUnreadCount()
  // 轮询未读消息数，每1分钟一次
  timer = setInterval(() => {
    fetchUnreadCount()
  }, 60000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.notice-bell-container {
  padding: 0 10px;
  cursor: pointer;
  display: flex;
  align-items: center;

  .item {
    display: flex;
    align-items: center;
    .el-icon {
      font-size: 20px;
    }
  }
}

.notice-container {
  .notice-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 10px;
    border-bottom: 1px solid #EBEEF5;
    margin-bottom: 10px;
    font-weight: bold;
    color: #303133;
  }

  .empty-notice {
    padding: 20px 0;
  }

  .notice-list {
    list-style: none;
    padding: 0;
    margin: 0;

    .notice-item {
      display: flex;
      padding: 12px 10px;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }

      .notice-icon {
        font-size: 24px;
        margin-right: 12px;
        display: flex;
        align-items: center;
        
        &.icon-warning { color: #E6A23C; }
        &.icon-danger { color: #F56C6C; }
        &.icon-info { color: #909399; }
      }

      .notice-content {
        flex: 1;
        min-width: 0;

        .notice-title {
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .text-ellipsis {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 200px;
          }
        }

        .notice-desc {
          font-size: 12px;
          color: #606266;
          margin-bottom: 4px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .notice-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}
</style>