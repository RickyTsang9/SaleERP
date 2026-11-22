<template>
  <div class="erp-error-page">
    <div class="erp-error-card">
      <div class="erp-error-badge">HTTP 404 · NOT FOUND</div>
      <h1 class="erp-error-code">404</h1>
      <h2 class="erp-error-title">{{ message }}</h2>
      <p class="erp-error-desc">
        您访问的页面路径不存在或已被调整。请核对地址栏链接，或返回工作台继续处理业务单据。
      </p>
      <div class="erp-error-actions">
        <router-link to="/index">
          <el-button type="primary" icon="HomeFilled">返回首页工作台</el-button>
        </router-link>
        <el-button icon="ArrowLeft" @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
const router = useRouter()

const message = computed(() => {
  return '未找到您访问的页面'
})

function goBack() {
  router.go(-1)
}
</script>

<style lang="scss" scoped>
.erp-error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--erp-canvas);
}

.erp-error-card {
  width: 100%;
  max-width: 520px;
  padding: 44px 40px;
  text-align: center;
  background: var(--erp-surface);
  border: 1px solid var(--erp-border);
  border-radius: 20px;
  box-shadow: var(--erp-shadow-lg);
}

.erp-error-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.06em;
  background: var(--erp-primary-soft);
  color: var(--erp-primary);
}

.erp-error-code {
  margin: 14px 0 8px;
  font-size: 72px;
  line-height: 1;
  font-weight: 800;
  letter-spacing: -0.04em;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 50%, #2563eb 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.erp-error-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 700;
  color: var(--erp-heading);
}

.erp-error-desc {
  margin: 0 0 28px;
  font-size: 14px;
  line-height: 1.65;
  color: var(--erp-text-secondary);
}

.erp-error-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
