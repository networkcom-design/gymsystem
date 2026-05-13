<script setup>
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { computed, ref, watch } from 'vue'

const route = useRoute()
const router = useRouter()
const isPublic = computed(() => route.meta.public)
const user = computed(() => {
  try {
    const raw = localStorage.getItem('gymsystem_user')
    return raw && raw !== 'undefined' ? JSON.parse(raw) : null
  } catch { return null }
})

const sidebarOpen = ref(true)
const mobileMenuOpen = ref(false)
watch(() => route.fullPath, () => { mobileMenuOpen.value = false })

function closeMenu() { mobileMenuOpen.value = false }
function toggleSidebar() {
  if (window.innerWidth <= 768) {
    mobileMenuOpen.value = !mobileMenuOpen.value
  } else {
    sidebarOpen.value = !sidebarOpen.value
  }
}

function logout() {
  localStorage.removeItem('gymsystem_token')
  localStorage.removeItem('gymsystem_user')
  router.push({ name: 'login' })
}
</script>

<template>
  <div v-if="isPublic"><RouterView /></div>
  <div v-else class="app-layout">
    <header class="mobile-topbar">
      <button class="hamburger" @click="toggleSidebar" aria-label="Menu">
        <span></span><span></span><span></span>
      </button>
      <div class="mobile-brand">
        <strong>GYM SYSTEM</strong>
        <span>Administracion</span>
      </div>
    </header>

    <div v-if="mobileMenuOpen" class="sidebar-overlay" @click="closeMenu"></div>

    <aside class="sidebar" :class="{ open: mobileMenuOpen, collapsed: !sidebarOpen }">
      <div class="brand">
        <h1>GYM SYSTEM</h1>
        <div class="sub">Administracion</div>
      </div>
      <nav class="nav">
        <RouterLink to="/dashboard"   active-class="active" @click="closeMenu">Dashboard</RouterLink>
        <RouterLink to="/alumnos"     active-class="active" @click="closeMenu">Alumnos</RouterLink>
        <RouterLink to="/disciplinas" active-class="active" @click="closeMenu">Disciplinas</RouterLink>
        <RouterLink to="/cuotas"      active-class="active" @click="closeMenu">Cuotas</RouterLink>
        <RouterLink to="/rutinas"     active-class="active" @click="closeMenu">Rutinas</RouterLink>
        <RouterLink to="/progreso"    active-class="active" @click="closeMenu">Progreso</RouterLink>
        <RouterLink to="/acceso"      active-class="active" @click="closeMenu">Acceso</RouterLink>
      </nav>
      <div class="sidebar-footer">
        <div>{{ user?.nombre || user?.username }}</div>
        <button class="secondary" @click="logout">Cerrar sesión</button>
      </div>
    </aside>

    <main class="main" :class="{ 'sidebar-collapsed': !sidebarOpen }">
      <RouterView />
    </main>
  </div>
</template>