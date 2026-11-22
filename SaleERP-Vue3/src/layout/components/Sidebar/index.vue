<template>
  <div :class="{ 'has-logo': showLogo }" class="sidebar-container">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="getMenuBackground"
        :text-color="getMenuTextColor"
        :unique-opened="true"
        :active-text-color="activeTextColor"
        :collapse-transition="false"
        mode="vertical"
        :class="sideTheme"
      >
        <sidebar-item
          v-for="(route, index) in sidebarRouters"
          :key="route.path + index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/assets/styles/variables.module.scss'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

const sidebarRouters = computed(() => permissionStore.sidebarRouters)
const showLogo = computed(() => settingsStore.sidebarLogo)
const sideTheme = computed(() => settingsStore.sideTheme)
const theme = computed(() => settingsStore.theme)
const isCollapse = computed(() => !appStore.sidebar.opened)

const getMenuBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

const getMenuTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-text)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuText : variables.menuLightText
})

const activeTextColor = computed(() => {
  if (settingsStore.isDark || sideTheme.value === 'theme-dark') {
    return '#a5b4fc'
  }
  return theme.value
})

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  padding: 0 11px 16px;
  background-color: v-bind(getMenuBackground);

  .scrollbar-wrapper {
    background-color: v-bind(getMenuBackground);
  }

  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 41px;
      line-height: 41px;
      margin: 3px 0;
      border-radius: 10px;
      color: v-bind(getMenuTextColor);
      font-weight: 500;
      transition: color 0.18s ease, background-color 0.18s ease, transform 0.18s ease;

      &:hover {
        background-color: var(--erp-primary-hover) !important;
        color: v-bind(activeTextColor);
      }
    }

    :deep(.el-menu-item) {
      position: relative;

      &.is-active {
        color: v-bind(activeTextColor) !important;
        background-color: var(--erp-primary-soft) !important;
        font-weight: 600;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 9px;
          bottom: 9px;
          width: 3px;
          border-radius: 0 4px 4px 0;
          background: var(--erp-primary);
        }
      }
    }

    :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
      font-weight: 600;
    }

    :deep(.el-sub-menu .el-menu) {
      padding: 2px 0 4px 4px;
      background-color: transparent !important;
    }

    :deep(.el-sub-menu .el-menu-item) {
      height: 38px;
      line-height: 38px;
      font-size: 13px;
    }
  }
}
</style>
