<template>
  <section class="erp-stat-strip" :class="[`cols-${columns}`]" :style="{ '--stat-cols': columns }">
    <div
      v-for="(item, idx) in items"
      :key="item.key || item.label || idx"
      class="erp-stat-card"
      :class="[
        `tone-${item.tone || 'indigo'}`,
        { 'is-clickable': item.clickable, 'is-active': item.active }
      ]"
      @click="item.clickable && emit('select', item)"
    >
      <div class="erp-stat-card__top">
        <span class="erp-stat-card__label">{{ item.label }}</span>
        <span v-if="item.icon" class="erp-stat-card__icon">
          <el-icon :size="16"><component :is="item.icon" /></el-icon>
        </span>
      </div>
      <div class="erp-stat-card__value erp-num">
        <span v-if="item.prefix" class="erp-stat-card__affix">{{ item.prefix }}</span>
        <span>{{ item.value }}</span>
        <span v-if="item.suffix" class="erp-stat-card__affix is-suffix">{{ item.suffix }}</span>
      </div>
      <div v-if="item.hint" class="erp-stat-card__hint" :title="item.hint">{{ item.hint }}</div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Number,
    default: 4
  }
})

const emit = defineEmits(['select'])
</script>

<style lang="scss" scoped>
.erp-stat-strip {
  display: grid;
  grid-template-columns: repeat(var(--stat-cols, 4), minmax(0, 1fr));
  gap: 14px;
}

.erp-stat-card {
  position: relative;
  background: var(--erp-surface);
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius-lg);
  padding: 15px 18px 14px;
  box-shadow: var(--erp-shadow-xs);
  transition: transform 0.2s cubic-bezier(0.22, 1, 0.36, 1), border-color 0.2s ease, box-shadow 0.2s ease;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    left: 18px;
    right: 18px;
    bottom: 0;
    height: 2.5px;
    border-radius: 999px 999px 0 0;
    opacity: 0;
    transition: opacity 0.2s ease;
    background: var(--erp-primary);
  }

  &.is-clickable {
    cursor: pointer;

    &:hover {
      transform: translateY(-2px);
      border-color: var(--erp-border-strong);
      box-shadow: var(--erp-shadow-md);

      &::after {
        opacity: 0.7;
      }
    }
  }

  &.is-active {
    border-color: rgba(var(--erp-primary-rgb), 0.55);
    box-shadow: 0 0 0 3px rgba(var(--erp-primary-rgb), 0.10), var(--erp-shadow-sm);

    &::after {
      opacity: 1;
    }
  }

  &.tone-indigo .erp-stat-card__icon {
    color: #4f46e5;
    background: rgba(79, 70, 229, 0.11);
  }
  &.tone-blue .erp-stat-card__icon {
    color: #2563eb;
    background: rgba(37, 99, 235, 0.11);
  }
  &.tone-blue::after {
    background: #2563eb;
  }
  &.tone-emerald .erp-stat-card__icon {
    color: #059669;
    background: rgba(16, 185, 129, 0.12);
  }
  &.tone-emerald::after {
    background: #10b981;
  }
  &.tone-amber .erp-stat-card__icon {
    color: #d97706;
    background: rgba(245, 158, 11, 0.13);
  }
  &.tone-amber::after {
    background: #f59e0b;
  }
  &.tone-rose .erp-stat-card__icon {
    color: #e11d48;
    background: rgba(225, 29, 72, 0.12);
  }
  &.tone-rose::after {
    background: #e11d48;
  }
  &.tone-violet .erp-stat-card__icon {
    color: #7c3aed;
    background: rgba(124, 58, 237, 0.12);
  }
  &.tone-violet::after {
    background: #7c3aed;
  }
  &.tone-slate .erp-stat-card__icon {
    color: #475569;
    background: rgba(100, 116, 139, 0.12);
  }
  &.tone-slate::after {
    background: #64748b;
  }
}

.erp-stat-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.erp-stat-card__label {
  font-size: 12.5px;
  font-weight: 500;
  color: var(--erp-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.erp-stat-card__icon {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.erp-stat-card__value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.02em;
  color: var(--erp-heading);
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.erp-stat-card__affix {
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-text-muted);

  &.is-suffix {
    font-size: 12.5px;
    font-weight: 500;
  }
}

.erp-stat-card__hint {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.45;
  color: var(--erp-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

:global(html.dark) {
  .erp-stat-card.tone-indigo .erp-stat-card__icon { color: #a5b4fc; }
  .erp-stat-card.tone-blue .erp-stat-card__icon { color: #60a5fa; }
  .erp-stat-card.tone-emerald .erp-stat-card__icon { color: #34d399; }
  .erp-stat-card.tone-amber .erp-stat-card__icon { color: #fbbf24; }
  .erp-stat-card.tone-rose .erp-stat-card__icon { color: #fb7185; }
  .erp-stat-card.tone-violet .erp-stat-card__icon { color: #c4b5fd; }
  .erp-stat-card.tone-slate .erp-stat-card__icon { color: #cbd5e1; }
}

@media (max-width: 1200px) {
  .erp-stat-strip:not(.cols-3) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .erp-stat-strip.cols-3 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .erp-stat-strip,
  .erp-stat-strip.cols-3 {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
