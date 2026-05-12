<template>
  <div class="login">
    <div class="login-shell">
      <section class="login-brand">
        <div class="login-brand__mark">S</div>
        <div class="login-brand__title">SaleERP</div>
        <div class="login-brand__subtitle">统一业务工作台</div>
      </section>
      <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
        <div class="login-form__header">
          <h3 class="title">{{ title }}</h3>
          <p class="login-form__subtitle">欢迎回来，请登录后继续处理业务。</p>
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
          <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="账号"
          >
            <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            auto-complete="off"
            placeholder="密码"
            @keyup.enter="handleLogin"
          >
            <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <div class="login-code-row">
            <el-input
              v-model="loginForm.code"
              size="large"
              auto-complete="off"
              placeholder="验证码"
              @keyup.enter="handleLogin"
            >
              <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
            </el-input>
            <button class="login-code" type="button" @click="getCode">
              <img :src="codeUrl" class="login-code-img" alt="验证码" />
            </button>
          </div>
        </el-form-item>
        <div class="login-options">
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
          <router-link v-if="register" class="link-type" :to="'/register'">立即注册</router-link>
        </div>
        <el-form-item class="login-submit">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
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
// 验证码开关
const captchaEnabled = ref(true)
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
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
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
        // 重新获取验证码
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

<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100%;
  padding: 48px 28px 72px;
  background:
    linear-gradient(90deg, rgba(30, 111, 122, 0.08) 1px, transparent 1px),
    linear-gradient(180deg, rgba(30, 111, 122, 0.08) 1px, transparent 1px),
    #f4f7f9;
  background-size: 36px 36px;
}
.login-shell {
  display: grid;
  grid-template-columns: minmax(300px, 420px) minmax(380px, 430px);
  align-items: stretch;
  width: min(920px, 100%);
  min-height: 520px;
  overflow: hidden;
  border: 1px solid rgba(31, 45, 61, 0.08);
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 24px 70px rgba(20, 46, 62, 0.16);
}
.login-brand {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 42px;
  color: #ffffff;
  background: #1e6f7a;
}
.login-brand::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
  background-position: center;
  opacity: 0.2;
}
.login-brand::after {
  content: "";
  position: absolute;
  left: 42px;
  right: 42px;
  bottom: 118px;
  height: 1px;
  background: rgba(255, 255, 255, 0.32);
}
.login-brand__mark,
.login-brand__title,
.login-brand__subtitle {
  position: relative;
  z-index: 1;
}
.login-brand__mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.56);
  border-radius: 8px;
  font-size: 26px;
  font-weight: 700;
}
.login-brand__title {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}
.login-brand__subtitle {
  margin-top: 14px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 15px;
}
.title {
  margin: 0;
  text-align: center;
  color: #1f2d3d;
  font-size: 24px;
  font-weight: 700;
}

.login-service-alert {
  margin-bottom: 18px;
}

.login-form {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 100%;
  padding: 50px 46px 34px;
  z-index: 1;
  background: #ffffff;
  .login-form__header {
    margin-bottom: 30px;
    text-align: center;
  }
  .login-form__subtitle {
    margin: 10px 0 0;
    color: #7b8794;
    font-size: 13px;
  }
  .el-input {
    height: 44px;
    input {
      height: 44px;
    }
  }
  .input-icon {
    height: 43px;
    width: 14px;
    margin-left: 0px;
  }
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  :deep(.el-input__wrapper) {
    border-radius: 6px;
    box-shadow: 0 0 0 1px #d8dee8 inset;
  }
  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #1e6f7a inset;
  }
  :deep(.el-button--primary) {
    width: 100%;
    height: 44px;
    border-color: #1e6f7a;
    background: #1e6f7a;
    border-radius: 6px;
    font-weight: 600;
  }
}
.login-service-alert__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code-row {
  display: grid;
  grid-template-columns: 1fr 116px;
  gap: 12px;
  width: 100%;
}
.login-code {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  padding: 0;
  overflow: hidden;
  border: 1px solid #d8dee8;
  border-radius: 6px;
  background: #f8fafc;
  cursor: pointer;
}
.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 24px;
  margin: 0 0 24px;
}
.login-submit {
  margin-bottom: 0;
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #65758b;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 860px) {
  .login {
    align-items: flex-start;
    padding: 28px 16px 72px;
  }
  .login-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }
  .login-brand {
    min-height: 180px;
    padding: 28px;
  }
  .login-brand::after {
    left: 28px;
    right: 28px;
    bottom: 92px;
  }
  .login-brand__title {
    font-size: 30px;
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
