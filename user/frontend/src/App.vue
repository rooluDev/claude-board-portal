<template>
  <v-app>
    <navbar />
    <v-main>
      <router-view />
    </v-main>
    <footer-component />
  </v-app>
</template>

<script>
import Navbar from '@/components/layout/Navbar.vue'
import FooterComponent from '@/components/layout/Footer.vue'

export default {
  name: 'App',
  components: {
    Navbar,
    FooterComponent
  }
}
</script>

<style>
html {
  overflow-y: auto !important;
}
</style>
