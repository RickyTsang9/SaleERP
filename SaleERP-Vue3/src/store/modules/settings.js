import defaultSettings from '@/settings'
import { useDark, useToggle } from '@vueuse/core'
import { useDynamicTitle } from '@/utils/dynamicTitle'
import { handleThemeStyle } from '@/utils/theme'

const isDark = useDark()
const toggleDark = useToggle(isDark)

const { sideTheme, theme, uiVersion, showSettings, navType, tagsView, tagsIcon, fixedHeader, sidebarLogo, dynamicTitle, footerVisible, footerContent } = defaultSettings

function loadStorageSetting() {
  try {
    const raw = localStorage.getItem('layout-setting')
    if (!raw) return {}
    const parsed = JSON.parse(raw) || {}
    if (parsed.uiVersion !== uiVersion) {
      const legacyThemes = ['#1E6F7A', '#1e6f7a', '#409EFF', '#409eff']
      if (!parsed.theme || legacyThemes.includes(parsed.theme)) {
        parsed.theme = theme
      }
      parsed.uiVersion = uiVersion
      localStorage.setItem('layout-setting', JSON.stringify(parsed))
    }
    return parsed
  } catch (e) {
    return {}
  }
}

const storageSetting = loadStorageSetting()

const useSettingsStore = defineStore(
  'settings',
  {
    state: () => ({
      title: '',
      theme: storageSetting.theme || theme,
      sideTheme: storageSetting.sideTheme || sideTheme,
      showSettings: showSettings,
      navType: storageSetting.navType === undefined ? navType : storageSetting.navType,
      tagsView: storageSetting.tagsView === undefined ? tagsView : storageSetting.tagsView,
      tagsIcon: storageSetting.tagsIcon === undefined ? tagsIcon : storageSetting.tagsIcon,
      fixedHeader: storageSetting.fixedHeader === undefined ? fixedHeader : storageSetting.fixedHeader,
      sidebarLogo: storageSetting.sidebarLogo === undefined ? sidebarLogo : storageSetting.sidebarLogo,
      dynamicTitle: storageSetting.dynamicTitle === undefined ? dynamicTitle : storageSetting.dynamicTitle,
      footerVisible: storageSetting.footerVisible === undefined ? footerVisible : storageSetting.footerVisible,
      footerContent: footerContent,
      isDark: isDark.value
    }),
    actions: {
      // 修改布局设置
      changeSetting(data) {
        const { key, value } = data
        if (this.hasOwnProperty(key)) {
          this[key] = value
        }
      },
      // 设置网页标题
      setTitle(title) {
        this.title = title
        useDynamicTitle()
      },
      // 切换暗黑模式
      toggleTheme() {
        this.isDark = !this.isDark
        toggleDark()
        handleThemeStyle(this.theme, this.isDark)
      }
    }
  })

export default useSettingsStore
