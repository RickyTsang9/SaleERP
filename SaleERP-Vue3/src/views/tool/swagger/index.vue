<template>
   <div class="app-container erp-page">
      <erp-page-header
         eyebrow="研发工具"
         title="系统接口"
         subtitle="在线浏览 Swagger / OpenAPI 接口定义并进行参数调试。"
         icon="Document"
         tone="violet"
      />
      <div class="erp-section-card" style="padding: 0; overflow: hidden;">
         <i-frame v-model:src="url"></i-frame>
      </div>
   </div>
</template>

<script setup>
import iFrame from '@/components/iFrame'

const url = ref(import.meta.env.VITE_APP_BASE_API + "/swagger-ui/index.html")
</script>
