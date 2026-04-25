<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function login() {
  error.value = ''
  loading.value = true
  try {
    const { data } = await http.post('/auth/login', { username: username.value, password: password.value })
    localStorage.setItem('gymsystem_token', data.token)
    localStorage.setItem('gymsystem_user', JSON.stringify({ username: data.username, nombre: data.nombre }))
    router.push({ name: 'dashboard' })
  } catch (e) {
    error.value = e.response?.data?.message || 'Usuario o contraseña incorrectos'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-bg">
    <div class="login-box">
      <h1>GYM SYSTEM</h1>
      <div class="sub">Administración</div>
      <form @submit.prevent="login">
        <div class="field">
          <label>Usuario</label>
          <input v-model="username" type="text" placeholder="admin" autocomplete="username" />
        </div>
        <div class="field">
          <label>Contraseña</label>
          <input v-model="password" type="password" placeholder="••••••" autocomplete="current-password" />
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? 'Ingresando...' : 'Ingresar' }}
        </button>
        <div v-if="error" class="error">{{ error }}</div>
      </form>
      <div class="hint">Gym System — Panel de Administración</div>
    </div>
  </div>
</template>
