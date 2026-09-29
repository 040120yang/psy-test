<template>
  <div class="page-head">
    <div>
      <div v-if="eyebrow" class="page-eyebrow">{{ eyebrow }}</div>
      <h1>{{ title }}</h1>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions"><slot name="actions" /></div>
  </div>
</template>
<script setup>
defineProps({ title: { type:String, required:true }, description:String, eyebrow:String })
</script>
