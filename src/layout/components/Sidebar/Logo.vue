<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 v-else class="sidebar-title">{{ title }}</h1>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 class="sidebar-title">{{ title }}</h1>
      </router-link>
    </transition>
  </div>
</template>

<script setup>
import logo from '@/assets/logo/logo.png'
import defaultSettings from '@/settings'
import useSettingsStore from '@/store/modules/settings'
import variables from '@/assets/styles/variables.module.scss'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

const settingsStore = useSettingsStore()
const title = computed(() => settingsStore.title || defaultSettings.title)
const sideTheme = computed(() => settingsStore.sideTheme)

// 获取Logo背景色
const getLogoBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  if (settingsStore.navType == 3) {
    return variables.menuLightBg
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

// 获取Logo文字颜色
const getLogoTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-text)'
  }
  if (settingsStore.navType == 3) {
    return variables.menuLightText
  }
  return sideTheme.value === 'theme-dark' ? '#fff' : variables.menuLightText
})
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  height: 56px;
  line-height: 56px;
  background: v-bind(getLogoBackground);
  text-align: left;
  overflow: hidden;
  padding: 10px 8px;

  & .sidebar-logo-link {
    display: flex;
    align-items: center;
    height: 100%;
    width: 100%;

    & .sidebar-logo {
      width: 34px;
      height: 34px;
      flex: 0 0 auto;
      margin-right: 12px;
      border-radius: 10px;
      box-shadow: 0 8px 18px rgba(30, 111, 122, 0.16);
    }

    & .sidebar-title {
      min-width: 0;
      margin: 0;
      color: v-bind(getLogoTextColor);
      font-weight: 700;
      line-height: 1.2;
      font-size: 15px;
      font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &.collapse {
    padding: 10px;

    .sidebar-logo-link {
      justify-content: center;
    }

    .sidebar-logo {
      margin-right: 0px;
    }
  }
}
</style>
