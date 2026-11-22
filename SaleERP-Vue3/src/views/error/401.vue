<template>
  <div class="erp-error-page">
    <div class="erp-error-card">
      <div class="erp-error-badge is-warning">HTTP 401 · UNAUTHORIZED</div>
      <h1 class="erp-error-code">401</h1>
      <h2 class="erp-error-title">您暂无当前页面的访问权限</h2>
      <p class="erp-error-desc">
        当前登录账号未被授予此模块的访问授权。如需办理相关业务，请联系系统管理员为您配置对应角色权限。
      </p>
      <div class="erp-error-actions">
        <el-button type="primary" icon="ArrowLeft" @click="back">返回上一页</el-button>
        <router-link to="/index">
          <el-button icon="HomeFilled">回到工作台首页</el-button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
const { proxy } = getCurrentInstance()

function back() {
  if (proxy.$route.query.noGoBack) {
    proxy.$router.push({ path: "/" })
  } else {
    proxy.$router.go(-1)
  }
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

  &.is-warning {
    background: rgba(245, 158, 11, 0.14);
    color: #d97706;
  }
}

.erp-error-code {
  margin: 14px 0 8px;
  font-size: 72px;
  line-height: 1;
  font-weight: 800;
  letter-spacing: -0.04em;
  background: linear-gradient(135deg, #4f46e5 0%, #2563eb 100%);
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
