// 处理主题样式（支持亮色 / 暗黑双模式自适应色阶）
export function handleThemeStyle(theme, isDark = false) {
  const safeTheme = theme || '#4F46E5'
  const dark = typeof isDark === 'boolean' ? isDark : document.documentElement.classList.contains('dark')
  const rgb = hexToRgb(safeTheme)
  const root = document.documentElement.style

  root.setProperty('--el-color-primary', safeTheme)
  root.setProperty('--erp-primary', safeTheme)
  root.setProperty('--erp-primary-rgb', rgb.join(', '))
  root.setProperty('--erp-primary-soft', `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, ${dark ? 0.18 : 0.10})`)
  root.setProperty('--erp-primary-hover', `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, ${dark ? 0.14 : 0.06})`)
  root.setProperty('--app-accent-soft', `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, ${dark ? 0.18 : 0.10})`)
  root.setProperty('--app-accent-hover', `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, ${dark ? 0.14 : 0.06})`)
  root.setProperty('--menu-active-text', dark ? mixColor(safeTheme, '#FFFFFF', 0.32) : safeTheme)

  const lightTarget = dark ? '#111827' : '#FFFFFF'
  for (let i = 1; i <= 9; i++) {
    root.setProperty(`--el-color-primary-light-${i}`, mixColor(safeTheme, lightTarget, i / 10))
  }
  const darkTarget = dark ? '#FFFFFF' : '#0F172A'
  for (let i = 1; i <= 9; i++) {
    root.setProperty(`--el-color-primary-dark-${i}`, mixColor(safeTheme, darkTarget, i / 10))
  }
}

// hex颜色转rgb颜色
export function hexToRgb(str) {
  const clean = String(str || '#4F46E5').replace('#', '').trim()
  const expanded = clean.length === 3
    ? clean.split('').map(ch => ch + ch).join('')
    : clean.padEnd(6, '0').slice(0, 6)
  const hexs = expanded.match(/../g) || ['4F', '46', 'E5']
  return hexs.map(h => parseInt(h, 16))
}

// rgb颜色转Hex颜色
export function rgbToHex(r, g, b) {
  const clamp = v => Math.max(0, Math.min(255, Math.round(v)))
  const hexs = [clamp(r).toString(16), clamp(g).toString(16), clamp(b).toString(16)]
  return `#${hexs.map(h => (h.length === 1 ? `0${h}` : h)).join('')}`
}

// 混色函数 (weight 为 targetColor 占比 0~1)
export function mixColor(baseColor, targetColor, weight) {
  const base = hexToRgb(baseColor)
  const target = hexToRgb(targetColor)
  const r = base[0] * (1 - weight) + target[0] * weight
  const g = base[1] * (1 - weight) + target[1] * weight
  const b = base[2] * (1 - weight) + target[2] * weight
  return rgbToHex(r, g, b)
}

// 变浅颜色值
export function getLightColor(color, level) {
  return mixColor(color, '#FFFFFF', level)
}

// 变深颜色值
export function getDarkColor(color, level) {
  return mixColor(color, '#000000', level)
}
