<template>
  <div id="tags-view-container" class="tags-view-container">
    <scroll-pane ref="scrollPaneRef" class="tags-view-wrapper" @scroll="handleScroll">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :data-path="tag.path"
        :class="{ active: isActive(tag), 'has-icon': tagsIcon }"
        :to="{ path: tag.path, query: tag.query, fullPath: tag.fullPath }"
        class="tags-view-item"
        @click.middle="!isAffix(tag) ? closeSelectedTag(tag) : ''"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        <span v-if="isActive(tag)" class="active-dot" />
        <svg-icon v-if="tagsIcon && tag.meta && tag.meta.icon && tag.meta.icon !== '#'" :icon-class="tag.meta.icon" />
        <span class="tag-title">{{ tag.title }}</span>
        <span v-if="!isAffix(tag)" class="tag-close-wrap" @click.prevent.stop="closeSelectedTag(tag)">
          <close class="el-icon-close" style="width: 1em; height: 1em; vertical-align: middle;" />
        </span>
      </router-link>
    </scroll-pane>
    <ul v-show="visible" :style="{ left: left + 'px', top: top + 'px' }" class="contextmenu">
      <li @click="refreshSelectedTag(selectedTag)">
        <refresh-right style="width: 1em; height: 1em;" /> 刷新页面
      </li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">
        <close style="width: 1em; height: 1em;" /> 关闭当前
      </li>
      <li @click="closeOthersTags">
        <circle-close style="width: 1em; height: 1em;" /> 关闭其他
      </li>
      <li v-if="!isFirstView()" @click="closeLeftTags">
        <back style="width: 1em; height: 1em;" /> 关闭左侧
      </li>
      <li v-if="!isLastView()" @click="closeRightTags">
        <right style="width: 1em; height: 1em;" /> 关闭右侧
      </li>
      <li @click="closeAllTags(selectedTag)">
        <circle-close style="width: 1em; height: 1em;" /> 全部关闭
      </li>
    </ul>
  </div>
</template>

<script setup>
import ScrollPane from './ScrollPane'
import { getNormalPath } from '@/utils/ruoyi'
import useTagsViewStore from '@/store/modules/tagsView'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const visible = ref(false)
const top = ref(0)
const left = ref(0)
const selectedTag = ref({})
const affixTags = ref([])
const scrollPaneRef = ref(null)

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const visitedViews = computed(() => useTagsViewStore().visitedViews)
const routes = computed(() => usePermissionStore().routes)
const tagsIcon = computed(() => useSettingsStore().tagsIcon)

watch(route, () => {
  addTags()
  moveToCurrentTag()
})

watch(visible, (value) => {
  if (value) {
    document.body.addEventListener('click', closeMenu)
  } else {
    document.body.removeEventListener('click', closeMenu)
  }
})

onMounted(() => {
  initTags()
  addTags()
})

function isActive(r) {
  return r.path === route.path
}

function isAffix(tag) {
  return tag.meta && tag.meta.affix
}

function isFirstView() {
  try {
    return selectedTag.value.fullPath === '/index' || selectedTag.value.fullPath === visitedViews.value[1].fullPath
  } catch (err) {
    return false
  }
}

function isLastView() {
  try {
    return selectedTag.value.fullPath === visitedViews.value[visitedViews.value.length - 1].fullPath
  } catch (err) {
    return false
  }
}

function filterAffixTags(routes, basePath = '') {
  let tags = []
  routes.forEach(route => {
    if (route.meta && route.meta.affix) {
      const tagPath = getNormalPath(basePath + '/' + route.path)
      tags.push({
        fullPath: tagPath,
        path: tagPath,
        name: route.name,
        meta: { ...route.meta }
      })
    }
    if (route.children) {
      const tempTags = filterAffixTags(route.children, route.path)
      if (tempTags.length >= 1) {
        tags = [...tags, ...tempTags]
      }
    }
  })
  return tags
}

function initTags() {
  const res = filterAffixTags(routes.value)
  affixTags.value = res
  for (const tag of res) {
    if (tag.name) {
      useTagsViewStore().addVisitedView(tag)
    }
  }
}

function addTags() {
  const { name } = route
  if (name) {
    useTagsViewStore().addView(route)
  }
}

function moveToCurrentTag() {
  nextTick(() => {
    for (const r of visitedViews.value) {
      if (r.path === route.path) {
        scrollPaneRef.value.moveToTarget(r)
        if (r.fullPath !== route.fullPath) {
          useTagsViewStore().updateVisitedView(route)
        }
      }
    }
  })
}

function refreshSelectedTag(view) {
  proxy.$tab.refreshPage(view)
  if (route.meta.link) {
    useTagsViewStore().delIframeView(route)
  }
}

function closeSelectedTag(view) {
  proxy.$tab.closePage(view).then(({ visitedViews }) => {
    if (isActive(view)) {
      toLastView(visitedViews, view)
    }
  })
}

function closeRightTags() {
  proxy.$tab.closeRightPage(selectedTag.value).then(visitedViews => {
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

function closeLeftTags() {
  proxy.$tab.closeLeftPage(selectedTag.value).then(visitedViews => {
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

function closeOthersTags() {
  router.push(selectedTag.value).catch(() => { })
  proxy.$tab.closeOtherPage(selectedTag.value).then(() => {
    moveToCurrentTag()
  })
}

function closeAllTags(view) {
  proxy.$tab.closeAllPage().then(({ visitedViews }) => {
    if (affixTags.value.some(tag => tag.path === route.path)) {
      return
    }
    toLastView(visitedViews, view)
  })
}

function toLastView(visitedViews, view) {
  const latestView = visitedViews.slice(-1)[0]
  if (latestView) {
    router.push(latestView.fullPath)
  } else {
    if (view.name === 'Dashboard') {
      router.replace({ path: '/redirect' + view.fullPath })
    } else {
      router.push('/')
    }
  }
}

function openMenu(tag, e) {
  const menuMinWidth = 118
  const offsetLeft = proxy.$el.getBoundingClientRect().left
  const offsetWidth = proxy.$el.offsetWidth
  const maxLeft = offsetWidth - menuMinWidth
  const l = e.clientX - offsetLeft + 15

  left.value = l > maxLeft ? maxLeft : l
  top.value = e.clientY
  visible.value = true
  selectedTag.value = tag
}

function closeMenu() {
  visible.value = false
}

function handleScroll() {
  closeMenu()
}
</script>

<style lang="scss" scoped>
.tags-view-container {
  height: 38px;
  width: 100%;
  background: var(--tags-bg, #ffffff);
  border-bottom: 1px solid var(--erp-border);
  display: flex;
  align-items: center;

  .tags-view-wrapper {
    .tags-view-item {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      position: relative;
      cursor: pointer;
      height: 27px;
      line-height: 27px;
      border: 1px solid transparent;
      border-radius: 8px;
      color: var(--tags-item-text, #64748b);
      background: transparent;
      padding: 0 11px;
      font-size: 12.5px;
      font-weight: 500;
      margin-left: 4px;
      margin-top: 5px;
      transition: all 0.18s ease;

      &:first-of-type {
        margin-left: 16px;
      }

      &:last-of-type {
        margin-right: 16px;
      }

      &:hover:not(.active) {
        background: var(--tags-item-hover, #f1f5f9);
        color: var(--erp-heading);
      }

      &.active {
        background: var(--erp-primary-soft);
        color: var(--menu-active-text);
        border-color: rgba(var(--erp-primary-rgb), 0.25);
        font-weight: 600;
      }

      .active-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: var(--erp-primary);
        flex-shrink: 0;
      }

      .tag-close-wrap {
        display: inline-flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .contextmenu {
    margin: 0;
    background: var(--erp-surface);
    z-index: 3000;
    position: absolute;
    list-style-type: none;
    padding: 6px;
    border-radius: 12px;
    font-size: 12.5px;
    font-weight: 500;
    color: var(--erp-text);
    box-shadow: var(--erp-shadow-md);
    border: 1px solid var(--erp-border);

    li {
      margin: 0;
      padding: 7px 14px;
      cursor: pointer;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 8px;

      &:hover {
        background: var(--erp-primary-hover);
        color: var(--erp-primary);
      }
    }
  }
}
</style>

<style lang="scss">
.tags-view-wrapper {
  .tags-view-item {
    .el-icon-close {
      width: 14px;
      height: 14px;
      border-radius: 50%;
      padding: 2px;
      transition: all 0.18s ease;

      &:hover {
        background-color: var(--tags-close-hover, #94a3b8);
        color: #ffffff;
      }
    }
  }
}
</style>
