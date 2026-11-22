<template>
  <router-view />
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

const settingsStore = useSettingsStore()

watch(
  () => [settingsStore.theme, settingsStore.isDark],
  ([theme, dark]) => {
    handleThemeStyle(theme, dark)
  },
  { immediate: true }
)

onMounted(() => {
  nextTick(() => {
    handleThemeStyle(settingsStore.theme, settingsStore.isDark)
  })
})
</script>
