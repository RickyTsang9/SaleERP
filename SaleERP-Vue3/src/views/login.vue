<template>
  <div class="login">
    <div class="login-orb login-orb--1" aria-hidden="true" />
    <div class="login-orb login-orb--2" aria-hidden="true" />

    <div class="login-shell">
      <section class="login-brand">
        <div class="login-brand__top">
          <span class="login-brand__emblem" aria-hidden="true">
            <svg viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="36" height="36" rx="10" fill="url(#login_brand_grad)" />
              <path d="M11 13.5C11 11.567 12.567 10 14.5 10H23.5C24.3284 10 25 10.6716 25 11.5C25 12.3284 24.3284 13 23.5 13H15.2C14.5373 13 14 13.5373 14 14.2C14 14.8627 14.5373 15.4 15.2 15.4H21.5C23.433 15.4 25 16.967 25 18.9V21.5C25 23.9853 22.9853 26 20.5 26H12.5C11.6716 26 11 25.3284 11 24.5C11 23.6716 11.6716 23 12.5 23H20.3C21.2389 23 22 22.2389 22 21.3V19.6C22 18.9373 21.4627 18.4 20.8 18.4H14.5C12.567 18.4 11 16.833 11 14.9V13.5Z" fill="#FFFFFF" />
              <circle cx="24" cy="11.5" r="2.2" fill="#38BDF8" />
              <defs>
                <linearGradient id="login_brand_grad" x1="0" y1="0" x2="36" y2="36" gradientUnits="userSpaceOnUse">
                  <stop stop-color="#6366F1" />
                  <stop offset="1" stop-color="#7C3AED" />
                </linearGradient>
              </defs>
            </svg>
          </span>
          <div class="login-brand__wordmark">
            <span class="login-brand__name">SaleERP</span>
            <span class="login-brand__tag">ENTERPRISE EDITION</span>
          </div>
        </div>

        <div class="login-brand__hero">
          <div class="login-brand__eyebrow">智慧经销云平台</div>
          <h1 class="login-brand__headline">全链路业财一体<br />精准驱动增长决策</h1>
          <p class="login-brand__desc">
            整合采购、销售、FEFO 批次仓配、PDA 移动作业、资金月结与实时经营驾驶舱，构建高效透明的现代化商贸 ERP。
          </p>
        </div>

        <div class="login-brand__features">
          <div class="login-feature-card">
            <div class="login-feature-card__title">业财闭环协同</div>
            <div class="login-feature-card__desc">销售、出入库、应收应付、发票与月结关账全自动穿透</div>
          </div>
          <div class="login-feature-card">
            <div class="login-feature-card__title">仓配扫码与效期</div>
            <div class="login-feature-card__desc">支持无线 PDA 连续盲盘、近效期预警与 FEFO 智能拣货</div>
          </div>
          <div class="login-feature-card">
            <div class="login-feature-card__title">经营驾驶舱洞察</div>
            <div class="login-feature-card__desc">毛利排行、账龄风控、预算追踪与现金流压力即时推演</div>
          </div>
        </div>
      </section>

      <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
        <div class="login-form__header">
          <span class="login-form__badge">统一身份认证</span>
          <h2 class="title">{{ title }}</h2>
          <p class="login-form__subtitle">欢迎回来，请输入您的企业账号与密码进入工作台</p>
        </div>

        <el-alert
          v-if="loginServiceUnavailable"
          :title="loginServiceMessage"
          type="error"
          :closable="false"
          show-icon
          class="login-service-alert"
        >
          <template #default>
            <div class="login-service-alert__content">
              <span>{{ loginServiceMessage }}</span>
              <el-button link type="primary" @click="retryLoginService">重新检测</el-button>
            </div>
          </template>
        </el-alert>

        <el-form-item prop="username">
          <label class="login-field-label">登录账号</label>
          <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="请输入账号"
          >
            <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <label class="login-field-label">登录密码</label>
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            show-password
            auto-complete="off"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          >
            <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="code" v-if="captchaEnabled">
          <label class="login-field-label">安全校验码</label>
          <div class="login-code-row">
            <el-input
              v-model="loginForm.code"
              size="large"
              auto-complete="off"
              placeholder="请输入验证码"
              @keyup.enter="handleLogin"
            >
              <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
            </el-input>
            <button class="login-code" type="button" @click="getCode" title="点击刷新验证码">
              <img :src="codeUrl" class="login-code-img" alt="验证码" />
            </button>
          </div>
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="loginForm.rememberMe">记住登录凭证</el-checkbox>
          <router-link v-if="register" class="link-type" :to="'/register'">注册新账号</router-link>
        </div>

        <el-form-item class="login-submit">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            class="login-submit-btn"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">进入工作台</span>
            <span v-else>正在验证身份...</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'

const title = defaultSettings.title
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = computed(() => ({
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: captchaEnabled.value ? [{ required: true, trigger: "change", message: "请输入验证码" }] : []
}))

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关（默认关闭，待后端接口返回后再按服务端配置显隐，避免关闭验证码时短暂闪现破损图片）
const captchaEnabled = ref(false)
// 注册开关
const register = ref(false)
const redirect = ref(undefined)
const loginServiceUnavailable = ref(false)
const loginServiceMessage = ref("")

watch(route, (newRoute) => {
  redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

// 标记当前登录服务不可用，并展示更明确的用户提示。
function markLoginServiceUnavailable(messageText) {
  loginServiceUnavailable.value = true
  loginServiceMessage.value = messageText
}

// 清理登录服务不可用提示，让页面恢复正常登录状态。
function clearLoginServiceUnavailable() {
  loginServiceUnavailable.value = false
  loginServiceMessage.value = ""
}

// 重新检测登录服务状态，方便用户在后端恢复后直接重试。
function retryLoginService() {
  getCode()
}

// 判断登录失败是否属于服务连接异常，避免把验证码或账号错误误提示为服务不可用。
function isLoginServiceError(error) {
  const errorMessage = error && error.message ? error.message : String(error || "")
  return errorMessage.includes("Network Error")
    || errorMessage.includes("timeout")
    || errorMessage.includes("Request failed with status code")
    || errorMessage.includes("后端接口连接异常")
    || errorMessage.includes("系统接口请求超时")
}

// 执行登录校验和提交流程，登录失败时给出更明确的服务状态提示。
function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      userStore.login(loginForm.value).then(() => {
        clearLoginServiceUnavailable()
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(error => {
        loading.value = false
        if (isLoginServiceError(error)) {
          markLoginServiceUnavailable("当前登录服务暂不可用，请检查后端服务连接或稍后重试。")
        } else {
          clearLoginServiceUnavailable()
        }
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

// 获取验证码并同步登录服务状态，后端不可用时直接给出页面级提示。
function getCode() {
  getCodeImg().then(res => {
    clearLoginServiceUnavailable()
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    } else {
      loginForm.value.code = ""
      loginForm.value.uuid = ""
    }
  }).catch(() => {
    captchaEnabled.value = false
    codeUrl.value = ""
    loginForm.value.uuid = ""
    markLoginServiceUnavailable("验证码服务暂不可用，当前无法完成登录。请确认后端服务已启动后重试。")
  })
}

// 读取本地记住密码信息，避免覆盖用户手工输入的账号和密码。
function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
    code: loginForm.value.code,
    uuid: loginForm.value.uuid
  }
}

getCode()
getCookie()
</script>

<style lang="scss" scoped>
.login {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 40px 24px 64px;
  overflow: hidden;
  background:
    radial-gradient(circle at 12% 18%, rgba(79, 70, 229, 0.14), transparent 36%),
    radial-gradient(circle at 86% 82%, rgba(37, 99, 235, 0.12), transparent 38%),
    var(--erp-canvas);
}

.login-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(72px);
  pointer-events: none;
  opacity: 0.45;

  &--1 {
    width: 360px;
    height: 360px;
    top: -120px;
    left: -80px;
    background: rgba(79, 70, 229, 0.35);
  }

  &--2 {
    width: 320px;
    height: 320px;
    right: -80px;
    bottom: -100px;
    background: rgba(56, 189, 248, 0.28);
  }
}

.login-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(360px, 1.12fr) minmax(380px, 0.96fr);
  align-items: stretch;
  width: min(1020px, 100%);
  min-height: 600px;
  overflow: hidden;
  border: 1px solid var(--erp-border);
  border-radius: 22px;
  background: var(--erp-surface);
  box-shadow: var(--erp-shadow-lg);
}

.login-brand {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 28px;
  padding: 40px 38px;
  color: #f8fafc;
  background:
    radial-gradient(circle at 18% 12%, rgba(99, 102, 241, 0.45), transparent 45%),
    radial-gradient(circle at 85% 90%, rgba(14, 165, 233, 0.28), transparent 46%),
    linear-gradient(155deg, #0f172a 0%, #1e1b4b 54%, #172554 100%);
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
    background-size: 32px 32px;
    pointer-events: none;
  }
}

.login-brand__top,
.login-brand__hero,
.login-brand__features {
  position: relative;
  z-index: 1;
}

.login-brand__top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.login-brand__emblem {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  box-shadow: 0 10px 24px -4px rgba(99, 102, 241, 0.55);

  svg {
    width: 40px;
    height: 40px;
    display: block;
  }
}

.login-brand__wordmark {
  display: flex;
  flex-direction: column;
}

.login-brand__name {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.1;
}

.login-brand__tag {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.14em;
  color: rgba(199, 210, 254, 0.78);
  margin-top: 3px;
}

.login-brand__eyebrow {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #c7d2fe;
  background: rgba(99, 102, 241, 0.24);
  border: 1px solid rgba(165, 180, 252, 0.28);
  margin-bottom: 16px;
}

.login-brand__headline {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: -0.02em;
  color: #ffffff;
}

.login-brand__desc {
  margin: 14px 0 0;
  font-size: 13.5px;
  line-height: 1.7;
  color: rgba(226, 232, 240, 0.8);
}

.login-brand__features {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.login-feature-card {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
}

.login-feature-card__title {
  font-size: 13px;
  font-weight: 600;
  color: #ffffff;
}

.login-feature-card__desc {
  margin-top: 3px;
  font-size: 12px;
  color: rgba(203, 213, 225, 0.78);
  line-height: 1.45;
}

.login-form {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 100%;
  padding: 46px 46px 40px;
  background: var(--erp-surface);

  .login-form__header {
    margin-bottom: 28px;
  }

  .login-form__badge {
    display: inline-flex;
    align-items: center;
    padding: 3px 10px;
    border-radius: 999px;
    font-size: 11.5px;
    font-weight: 600;
    color: var(--erp-primary);
    background: var(--erp-primary-soft);
    margin-bottom: 10px;
  }

  .title {
    margin: 0;
    color: var(--erp-heading);
    font-size: 24px;
    font-weight: 800;
    letter-spacing: -0.02em;
  }

  .login-form__subtitle {
    margin: 8px 0 0;
    color: var(--erp-text-muted);
    font-size: 13px;
  }

  .login-field-label {
    display: block;
    width: 100%;
    font-size: 12.5px;
    font-weight: 600;
    color: var(--erp-text-secondary);
    margin-bottom: 6px;
    line-height: 1.2;
  }

  .el-input {
    height: 44px;

    :deep(input) {
      height: 44px;
    }
  }

  .input-icon {
    height: 42px;
    width: 15px;
    color: var(--erp-text-muted);
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__content) {
    flex-direction: column;
    align-items: stretch;
  }
}

.login-service-alert {
  margin-bottom: 18px;
}

.login-service-alert__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.login-code-row {
  display: grid;
  grid-template-columns: 1fr 118px;
  gap: 10px;
  width: 100%;
}

.login-code {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  padding: 0;
  overflow: hidden;
  border: 1px solid var(--erp-border);
  border-radius: 10px;
  background: var(--erp-surface-soft);
  cursor: pointer;
  transition: border-color 0.2s ease;

  &:hover {
    border-color: var(--erp-primary);
  }
}

.login-code-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 24px;
  margin: 2px 0 22px;
}

.login-submit {
  margin-bottom: 0;
}

.login-submit-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px !important;
  background: linear-gradient(135deg, var(--erp-primary) 0%, #6366f1 100%) !important;
  border: none !important;
  box-shadow: 0 10px 22px -6px rgba(var(--erp-primary-rgb), 0.55) !important;
}

.el-login-footer {
  position: fixed;
  bottom: 0;
  width: 100%;
  height: 40px;
  line-height: 40px;
  text-align: center;
  color: var(--erp-text-muted);
  font-size: 12px;
  letter-spacing: 0.04em;
}

@media (max-width: 880px) {
  .login {
    align-items: flex-start;
    padding: 24px 16px 64px;
  }
  .login-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }
  .login-brand {
    padding: 28px 24px;
    gap: 18px;
  }
  .login-brand__features {
    display: none;
  }
  .login-brand__headline {
    font-size: 24px;
  }
  .login-form {
    padding: 32px 24px 28px;
  }
}

@media (max-width: 420px) {
  .login-code-row {
    grid-template-columns: 1fr;
  }
  .login-code {
    width: 100%;
  }
}
</style>
