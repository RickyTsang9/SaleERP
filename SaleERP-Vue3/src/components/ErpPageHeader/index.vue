<template>
  <header class="erp-page-header" :class="[`tone-${tone}`, { 'has-border': bordered }]">
    <div class="erp-page-header__left">
      <div v-if="icon" class="erp-page-header__icon" :class="`tone-${tone}`">
        <el-icon :size="20">
          <component :is="icon" />
        </el-icon>
      </div>
      <div class="erp-page-header__copy">
        <div v-if="eyebrow || badge" class="erp-page-header__meta">
          <span v-if="eyebrow" class="erp-page-header__eyebrow">{{ eyebrow }}</span>
          <span v-if="badge" class="erp-page-header__badge">{{ badge }}</span>
          <slot name="meta" />
        </div>
        <h1 class="erp-page-header__title">{{ title }}</h1>
        <p v-if="subtitle" class="erp-page-header__subtitle">{{ subtitle }}</p>
      </div>
    </div>
    <div v-if="$slots.actions" class="erp-page-header__actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  eyebrow: {
    type: String,
    default: ''
  },
  badge: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'indigo'
  },
  bordered: {
    type: Boolean,
    default: true
  }
})
</script>

<style lang="scss" scoped>
.erp-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 18px 22px;
  background: var(--erp-surface);
  border-radius: var(--erp-radius-lg);
  border: 1px solid var(--erp-border);
  box-shadow: var(--erp-shadow-xs);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3.5px;
    background: linear-gradient(180deg, var(--erp-primary) 0%, #6366f1 100%);
  }

  &.tone-blue::before {
    background: linear-gradient(180deg, #2563eb 0%, #38bdf8 100%);
  }

  &.tone-emerald::before {
    background: linear-gradient(180deg, #059669 0%, #34d399 100%);
  }

  &.tone-amber::before {
    background: linear-gradient(180deg, #d97706 0%, #fbbf24 100%);
  }

  &.tone-rose::before {
    background: linear-gradient(180deg, #e11d48 0%, #fb7185 100%);
  }

  &.tone-violet::before {
    background: linear-gradient(180deg, #7c3aed 0%, #a78bfa 100%);
  }

  &.tone-slate::before {
    background: linear-gradient(180deg, #475569 0%, #94a3b8 100%);
  }
}

.erp-page-header__left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.erp-page-header__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #4f46e5;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.13) 0%, rgba(99, 102, 241, 0.06) 100%);
  border: 1px solid rgba(79, 70, 229, 0.18);

  &.tone-blue {
    color: #2563eb;
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.13) 0%, rgba(56, 189, 248, 0.07) 100%);
    border-color: rgba(37, 99, 235, 0.18);
  }

  &.tone-emerald {
    color: #059669;
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.14) 0%, rgba(52, 211, 153, 0.07) 100%);
    border-color: rgba(16, 185, 129, 0.20);
  }

  &.tone-amber {
    color: #d97706;
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.14) 0%, rgba(251, 191, 36, 0.07) 100%);
    border-color: rgba(245, 158, 11, 0.22);
  }

  &.tone-rose {
    color: #e11d48;
    background: linear-gradient(135deg, rgba(225, 29, 72, 0.13) 0%, rgba(251, 113, 133, 0.07) 100%);
    border-color: rgba(225, 29, 72, 0.20);
  }

  &.tone-violet {
    color: #7c3aed;
    background: linear-gradient(135deg, rgba(124, 58, 237, 0.13) 0%, rgba(167, 139, 250, 0.07) 100%);
    border-color: rgba(124, 58, 237, 0.20);
  }

  &.tone-slate {
    color: #475569;
    background: linear-gradient(135deg, rgba(100, 116, 139, 0.14) 0%, rgba(148, 163, 184, 0.06) 100%);
    border-color: rgba(100, 116, 139, 0.20);
  }
}

.erp-page-header__copy {
  min-width: 0;
}

.erp-page-header__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 3px;
}

.erp-page-header__eyebrow {
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--erp-primary);
  text-transform: uppercase;
}

.erp-page-header__badge {
  display: inline-flex;
  align-items: center;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  color: var(--erp-text-secondary);
  background: var(--erp-surface-soft);
  border: 1px solid var(--erp-border);
}

.erp-page-header__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.25;
  letter-spacing: -0.015em;
  color: var(--erp-heading);
}

.erp-page-header__subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--erp-text-muted);
  line-height: 1.45;
}

.erp-page-header__actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

:global(html.dark) .erp-page-header__icon {
  color: #a5b4fc;
  &.tone-blue { color: #60a5fa; }
  &.tone-emerald { color: #34d399; }
  &.tone-amber { color: #fbbf24; }
  &.tone-rose { color: #fb7185; }
  &.tone-violet { color: #c4b5fd; }
  &.tone-slate { color: #cbd5e1; }
}

@media (max-width: 768px) {
  .erp-page-header {
    padding: 14px 16px;
  }
  .erp-page-header__title {
    font-size: 18px;
  }
}
</style>
