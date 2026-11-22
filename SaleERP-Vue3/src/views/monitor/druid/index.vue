<template>
   <div class="app-container erp-page">
      <erp-page-header
         eyebrow="系统监控"
         title="数据监控"
         subtitle="集成 Druid 连接池实时监控，查看 SQL 执行统计、慢查询与数据源连接状态。"
         icon="Coin"
         tone="blue"
      />
      <div class="erp-section-card" style="padding: 0; overflow: hidden;">
         <i-frame v-model:src="url"></i-frame>
      </div>
   </div>
</template>

<script setup>
import iFrame from '@/components/iFrame'

import { ref } from 'vue'

const url = ref(import.meta.env.VITE_APP_BASE_API + '/druid/login.html')
</script>
