<template>
  <el-dialog :title="title" v-model="visible" width="820px" append-to-body>
    <div v-loading="loading">
      <template v-if="chainData">
        <slot name="summary" :data="chainData" />
        <slot name="sections" :data="chainData" />
      </template>
      <el-empty v-else-if="!loading" description="暂无上下游单据链路记录" />
    </div>
    <template #footer>
      <div class="dialog-footer">
        <slot name="footer" :data="chainData" />
        <el-button @click="visible = false">关 闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="OrderChainDialog">
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: "单据追踪链路"
  },
  loading: {
    type: Boolean,
    default: false
  },
  chainData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(["update:modelValue"])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit("update:modelValue", val)
})
</script>
