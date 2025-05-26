<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<style>
.app-container {
  background-image: url('./assets/background.jpg');
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
  min-height: 100vh; /* 确保背景覆盖整个视口高度 */
}
</style>
