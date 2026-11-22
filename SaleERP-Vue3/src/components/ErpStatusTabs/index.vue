<template>
  <div class="erp-status-tabs" role="tablist">
    <button
      v-for="item in items"
      :key="String(item.value ?? 'all')"
      type="button"
      role="tab"
      class="erp-status-tabs__item"
      :class="{ 'is-active': isSameValue(modelValue, item.value) }"
      @click="handleSelect(item.value)"
    >
      <span v-if="item.dot" class="erp-status-tabs__dot" :class="`dot-${item.dot}`" />
      <span class="erp-status-tabs__label">{{ item.label }}</span>
      <span v-if="item.count !== undefined && item.count !== null" class="erp-status-tabs__count erp-num">
        {{ item.count }}
      </span>
    </button>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: [String, Number, Boolean],
    default: ''
  },
  items: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

function isSameValue(a, b) {
  const normA = a === undefined || a === null ? '' : String(a)
  const normB = b === undefined || b === null ? '' : String(b)
  return normA === normB
}

function handleSelect(val) {
  if (isSameValue(props.modelValue, val)) return
  emit('update:modelValue', val)
  emit('change', val)
}
</script>

<style lang="scss" scoped>
.erp-status-tabs {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  padding: 4px;
  background: var(--erp-surface-soft);
  border: 1px solid var(--erp-border);
  border-radius: 12px;
  max-width: 100%;
}

.erp-status-tabs__item {
  appearance: none;
  border: 1px solid transparent;
  background: transparent;
  color: var(--erp-text-muted);
  font-size: 13px;
  font-weight: 500;
  line-height: 1;
  padding: 7px 12px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: inherit;

  &:hover:not(.is-active) {
    color: var(--erp-heading);
    background: rgba(var(--erp-primary-rgb), 0.05);
  }

  &.is-active {
    background: var(--erp-surface);
    color: var(--erp-primary);
    border-color: var(--erp-border);
    box-shadow: var(--erp-shadow-xs);
    font-weight: 600;
  }
}

.erp-status-tabs__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;

  &.dot-indigo { background: #4f46e5; }
  &.dot-blue { background: #2563eb; }
  &.dot-emerald { background: #10b981; }
  &.dot-amber { background: #f59e0b; }
  &.dot-rose { background: #e11d48; }
  &.dot-slate { background: #94a3b8; }
}

.erp-status-tabs__count {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 999px;
  background: var(--erp-border);
  color: var(--erp-text-secondary);
}

.erp-status-tabs__item.is-active .erp-status-tabs__count {
  background: var(--erp-primary-soft);
  color: var(--erp-primary);
}

:global(html.dark) .erp-status-tabs__item.is-active {
  color: #a5b4fc;
}
:global(html.dark) .erp-status-tabs__item.is-active .erp-status-tabs__count {
  color: #a5b4fc;
}
</style>
