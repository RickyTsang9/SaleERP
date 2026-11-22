<template>
  <div class="register">
    <div class="register-card">
      <div class="register-header">
        <span class="register-badge">创建企业账号</span>
        <h2 class="title">{{ title }}</h2>
        <p class="register-subtitle">注册账号后即可登录智慧经销云平台</p>
      </div>
      <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="请输入账号（2-20位）"
          >
            <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            size="large"
            show-password
            auto-complete="off"
            placeholder="请输入密码（5-20位）"
            @keyup.enter="handleRegister"
          >
            <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            size="large"
            show-password
            auto-complete="off"
            placeholder="请再次确认密码"
            @keyup.enter="handleRegister"
          >
            <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <div class="register-code-row">
            <el-input
              size="large"
              v-model="registerForm.code"
              auto-complete="off"
              placeholder="请输入验证码"
              @keyup.enter="handleRegister"
            >
              <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
            </el-input>
            <button type="button" class="register-code" @click="getCode">
              <img :src="codeUrl" class="register-code-img" alt="验证码" />
            </button>
          </div>
        </el-form-item>
        <el-form-item style="width: 100%; margin-bottom: 12px;">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            class="register-submit-btn"
            @click.prevent="handleRegister"
          >
            <span v-if="!loading">立即注册</span>
            <span v-else>注册中...</span>
          </el-button>
        </el-form-item>
        <div class="register-footer-link">
          已有账号？<router-link class="link-type" :to="'/login'">返回登录</router-link>
        </div>
      </el-form>
    </div>
    <div class="el-register-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from "element-plus"
import { getCodeImg, register } from "@/api/login"
import defaultSettings from '@/settings'

const title = defaultSettings.title
const footerContent = defaultSettings.footerContent
const router = useRouter()
const { proxy } = getCurrentInstance()

const registerForm = ref({
  username: "",
  password: "",
  confirmPassword: "",
  code: "",
  uuid: ""
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" },
    { min: 2, max: 20, message: "用户账号长度必须介于 2 和 20 之间", trigger: "blur" }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入您的密码" },
    { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" },
    { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\ |", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(false)

function handleRegister() {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      loading.value = true
      register(registerForm.value).then(() => {
        const username = registerForm.value.username
        ElMessageBox.alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", "系统提示", {
          dangerouslyUseHTMLString: true,
          type: "success"
        }).then(() => {
          router.push("/login")
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      registerForm.value.uuid = res.uuid
    }
  })
}

getCode()
</script>

<style lang="scss" scoped>
.register {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 40px 20px 64px;
  background:
    radial-gradient(circle at 18% 18%, rgba(79, 70, 229, 0.14), transparent 36%),
    radial-gradient(circle at 84% 82%, rgba(37, 99, 235, 0.12), transparent 38%),
    var(--erp-canvas);
}

.register-card {
  width: min(440px, 100%);
  padding: 36px 34px 28px;
  border-radius: 20px;
  background: var(--erp-surface);
  border: 1px solid var(--erp-border);
  box-shadow: var(--erp-shadow-lg);
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-badge {
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
  font-size: 22px;
  font-weight: 800;
}

.register-subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--erp-text-muted);
}

.register-form {
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
}

.register-code-row {
  display: grid;
  grid-template-columns: 1fr 116px;
  gap: 10px;
  width: 100%;
}

.register-code {
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
}

.register-code-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.register-submit-btn {
  width: 100%;
  height: 44px;
  font-weight: 600;
  border-radius: 10px !important;
  background: linear-gradient(135deg, var(--erp-primary) 0%, #6366f1 100%) !important;
  border: none !important;
}

.register-footer-link {
  text-align: center;
  font-size: 13px;
  color: var(--erp-text-muted);
}

.el-register-footer {
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
</style>
