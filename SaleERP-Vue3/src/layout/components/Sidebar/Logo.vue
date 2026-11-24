<template>
  <div class="sidebar-logo-container" :class="{ collapse }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <span class="brand-emblem" aria-hidden="true">
          <svg viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="36" height="36" rx="10" fill="url(#saleerp_brand_grad_c)" />
            <path d="M11 13.5C11 11.567 12.567 10 14.5 10H23.5C24.3284 10 25 10.6716 25 11.5C25 12.3284 24.3284 13 23.5 13H15.2C14.5373 13 14 13.5373 14 14.2C14 14.8627 14.5373 15.4 15.2 15.4H21.5C23.433 15.4 25 16.967 25 18.9V21.5C25 23.9853 22.9853 26 20.5 26H12.5C11.6716 26 11 25.3284 11 24.5C11 23.6716 11.6716 23 12.5 23H20.3C21.2389 23 22 22.2389 22 21.3V19.6C22 18.9373 21.4627 18.4 20.8 18.4H14.5C12.567 18.4 11 16.833 11 14.9V13.5Z" fill="#FFFFFF" />
            <circle cx="24" cy="11.5" r="2.2" fill="#38BDF8" />
            <defs>
              <linearGradient id="saleerp_brand_grad_c" x1="0" y1="0" x2="36" y2="36" gradientUnits="userSpaceOnUse">
                <stop stop-color="#4F46E5" />
                <stop offset="1" stop-color="#7C3AED" />
              </linearGradient>
            </defs>
          </svg>
        </span>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <span class="brand-emblem" aria-hidden="true">
          <svg viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="36" height="36" rx="10" fill="url(#saleerp_brand_grad_e)" />
            <path d="M11 13.5C11 11.567 12.567 10 14.5 10H23.5C24.3284 10 25 10.6716 25 11.5C25 12.3284 24.3284 13 23.5 13H15.2C14.5373 13 14 13.5373 14 14.2C14 14.8627 14.5373 15.4 15.2 15.4H21.5C23.433 15.4 25 16.967 25 18.9V21.5C25 23.9853 22.9853 26 20.5 26H12.5C11.6716 26 11 25.3284 11 24.5C11 23.6716 11.6716 23 12.5 23H20.3C21.2389 23 22 22.2389 22 21.3V19.6C22 18.9373 21.4627 18.4 20.8 18.4H14.5C12.567 18.4 11 16.833 11 14.9V13.5Z" fill="#FFFFFF" />
            <circle cx="24" cy="11.5" r="2.2" fill="#38BDF8" />
            <defs>
              <linearGradient id="saleerp_brand_grad_e" x1="0" y1="0" x2="36" y2="36" gradientUnits="userSpaceOnUse">
                <stop stop-color="#4F46E5" />
                <stop offset="1" stop-color="#7C3AED" />
              </linearGradient>
            </defs>
          </svg>
        </span>
        <div class="brand-text">
          <h1 class="sidebar-title">SaleERP</h1>
          <span class="sidebar-subtitle">进销存管理系统</span>
        </div>
      </router-link>
    </transition>
  </div>
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import variables from '@/assets/styles/variables.module.scss'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

const settingsStore = useSettingsStore()
const sideTheme = computed(() => settingsStore.sideTheme)

const getLogoBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  if (settingsStore.navType == 3) {
    return 'transparent'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

const getLogoTextColor = computed(() => {
  if (settingsStore.isDark) {
    return '#f8fafc'
  }
  if (settingsStore.navType == 3) {
    return '#0f172a'
  }
  return sideTheme.value === 'theme-dark' ? '#f8fafc' : '#0f172a'
})

const getLogoSubColor = computed(() => {
  if (settingsStore.isDark || (sideTheme.value === 'theme-dark' && settingsStore.navType != 3)) {
    return '#94a3b8'
  }
  return '#64748b'
})
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 0.35s ease;
}

.sidebarLogoFade-enter-from,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  height: 62px;
  font-size: 14px;
  background: v-bind(getLogoBackground);
  text-align: left;
  overflow: hidden;
  padding: 12px 10px;
  border-bottom: 1px solid var(--erp-border);
  margin-bottom: 8px;

  .sidebar-logo-link {
    display: flex !important;
    align-items: center;
    gap: 11px;
    height: 100%;
    width: 100%;
    padding: 0 4px;
    text-decoration: none;
  }

  .brand-emblem {
    width: 36px;
    height: 36px;
    flex: 0 0 36px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    box-shadow: 0 8px 18px -4px rgba(79, 70, 229, 0.42);
    transition: transform 0.2s ease;

    svg {
      width: 36px;
      height: 36px;
      display: block;
    }
  }

  .sidebar-logo-link:hover .brand-emblem {
    transform: translateY(-1px) scale(1.03);
  }

  .brand-text {
    display: flex;
    flex-direction: column;
    min-width: 0;
  }

  .sidebar-title {
    margin: 0;
    color: v-bind(getLogoTextColor);
    font-weight: 800;
    line-height: 1.15;
    font-size: 16.5px;
    letter-spacing: -0.02em;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .sidebar-subtitle {
    margin-top: 2px;
    font-size: 10.5px;
    font-weight: 500;
    letter-spacing: 0.06em;
    color: v-bind(getLogoSubColor);
    line-height: 1.1;
    white-space: nowrap;
  }

  &.collapse {
    padding: 12px 6px;

    .sidebar-logo-link {
      justify-content: center;
      padding: 0;
    }
  }
}
</style>
