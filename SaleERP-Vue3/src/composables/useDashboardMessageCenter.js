import {
  buildDashboardMessagePreviewData,
  canDirectJumpDashboardMessage,
  getDashboardMessageActionLabel,
  getDashboardMessageTypeLabel,
  resolveDashboardMessageRoute
} from '@/utils/businessDashboard'

// 统一首页与经营看板的消息中心交互，避免消息预览和跳转逻辑继续重复维护。
export function useDashboardMessageCenter() {
  const router = useRouter()
  const messagePreviewOpen = ref(false)
  const messagePreviewData = ref({})

  // 打开消息详情弹窗，统一补齐预览正文内容。
  function openMessagePreview(messageItem) {
    messagePreviewData.value = buildDashboardMessagePreviewData(messageItem)
    messagePreviewOpen.value = true
  }

  // 根据消息类型跳转到目标页面，无法直跳时自动回退到详情预览。
  function handleMessageJump(messageItem) {
    const targetRoute = resolveDashboardMessageRoute(messageItem)
    if (!targetRoute) {
      openMessagePreview(messageItem)
      return
    }
    router.push(targetRoute)
  }

  // 处理消息主按钮动作，优先进入可处理页面，其余消息打开详情。
  function handleMessageAction(messageItem) {
    if (canDirectJumpDashboardMessage(messageItem)) {
      handleMessageJump(messageItem)
      return
    }
    openMessagePreview(messageItem)
  }

  return {
    messagePreviewOpen,
    messagePreviewData,
    openMessagePreview,
    handleMessageJump,
    handleMessageAction,
    canDirectJumpMessage: canDirectJumpDashboardMessage,
    getMessageActionLabel: getDashboardMessageActionLabel,
    getMessageTypeLabel: getDashboardMessageTypeLabel
  }
}
