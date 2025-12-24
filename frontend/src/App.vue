<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
/* 保证页面占满全屏，避免高度为 0 导致看不见 */
html, body, #app {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
}
</style>