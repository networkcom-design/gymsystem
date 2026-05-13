<script setup>
import { ref, onMounted } from 'vue'
import http from '../api/http'

const dni     = ref('')
const estado  = ref('idle')   // idle | loading | ok | error
const alumno  = ref(null)
const mensaje = ref('')
const dniInput = ref(null)

onMounted(() => {
  setTimeout(() => dniInput.value?.focus(), 100)
})

async function buscar() {
  if (!dni.value.trim()) return
  estado.value = 'loading'
  alumno.value = null

  try {
    const { data } = await http.get(`/alumnos/por-dni/${dni.value.trim()}`)
    alumno.value = data
    estado.value = 'ok'

    setTimeout(() => {
      estado.value = 'idle'
      alumno.value = null
      dni.value = ''
      setTimeout(() => dniInput.value?.focus(), 100)
    }, 3000)

  } catch (e) {
    estado.value = 'error'
    mensaje.value = e.response?.status === 404
      ? 'DNI no encontrado. Consultá con el gimnasio.'
      : 'Error de conexión. Intentá de nuevo.'

    setTimeout(() => {
      estado.value = 'idle'
      mensaje.value = ''
      dni.value = ''
      setTimeout(() => dniInput.value?.focus(), 100)
    }, 3000)
  }
}

function cuotaAlDia(alumno) {
  if (!alumno.fechaVencimientoCuota) return null
  const hoy = new Date()
  const vence = new Date(alumno.fechaVencimientoCuota)
  const diff = Math.ceil((vence - hoy) / (1000 * 60 * 60 * 24))
  return diff
}
</script>

<template>
  <div class="acceso-wrap">
    <div class="acceso-card">

      <!-- LOGO / TÍTULO -->
      <div class="acceso-header">
        <div class="acceso-logo">🏋️</div>
        <h1 class="acceso-titulo">Control de Acceso</h1>
        <p class="acceso-sub">Ingresá tu DNI para registrar tu entrada</p>
      </div>

      <!-- FORMULARIO -->
      <div v-if="estado === 'idle' || estado === 'loading'" class="acceso-form">
        <input
          ref="dniInput"
          v-model="dni"
          type="text"
          placeholder="Ingresá tu DNI"
          class="acceso-input"
          @keyup.enter="buscar"
          :disabled="estado === 'loading'"
        />
        <button
          class="acceso-btn"
          @click="buscar"
          :disabled="estado === 'loading' || !dni.trim()"
        >
          {{ estado === 'loading' ? 'Verificando...' : 'Ingresar' }}
        </button>
      </div>

      <!-- RESULTADO OK -->
      <div v-else-if="estado === 'ok' && alumno" class="acceso-resultado ok">
        <div class="resultado-icono">✅</div>
        <div class="resultado-nombre">{{ alumno.nombre }} {{ alumno.apellido }}</div>
        <div class="resultado-dni">DNI: {{ alumno.dni }}</div>

        <!-- Estado cuota -->
        <div v-if="alumno.estadoCuota === 'SIN_CUOTA'" class="cuota-badge sin-info">
          Sin información de cuota
        </div>
        <div v-else-if="alumno.estadoCuota === 'VENCIDA'" class="cuota-badge vencida">
          ❌ Cuota vencida
        </div>
        <div v-else-if="alumno.estadoCuota === 'POR_VENCER'" class="cuota-badge por-vencer">
          ⚠️ Pagó el mes pasado — renovar este mes
        </div>
        <div v-else-if="alumno.estadoCuota === 'AL_DIA'" class="cuota-badge al-dia">
          ✅ Cuota al día
        </div>

        <div class="countdown">Cerrando en 3 segundos...</div>
      </div>

      <!-- RESULTADO ERROR -->
      <div v-else-if="estado === 'error'" class="acceso-resultado error">
        <div class="resultado-icono">❌</div>
        <div class="resultado-msg">{{ mensaje }}</div>
        <div class="countdown">Volviendo...</div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.acceso-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  padding: 24px;
}

.acceso-card {
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 48px 40px;
  width: 100%;
  max-width: 420px;
  text-align: center;
  box-shadow: 0 8px 40px rgba(0,0,0,0.3);
}

.acceso-header { margin-bottom: 32px; }
.acceso-logo { font-size: 48px; margin-bottom: 12px; }
.acceso-titulo { font-size: 22px; font-weight: 700; margin: 0 0 6px; }
.acceso-sub { font-size: 13px; color: var(--muted); margin: 0; }

.acceso-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.acceso-input {
  font-size: 20px;
  text-align: center;
  padding: 14px;
  border-radius: 8px;
  border: 2px solid var(--border);
  background: var(--bg);
  color: var(--text);
  letter-spacing: 4px;
}
.acceso-input:focus { border-color: var(--primary); outline: none; }

.acceso-btn {
  padding: 14px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 8px;
  background: var(--primary);
  color: white;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s;
}
.acceso-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.acceso-btn:hover:not(:disabled) { opacity: 0.85; }

.acceso-resultado {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px 0;
}

.resultado-icono { font-size: 48px; }
.resultado-nombre { font-size: 22px; font-weight: 700; }
.resultado-dni { font-size: 14px; color: var(--muted); }
.resultado-msg { font-size: 16px; color: var(--danger); }

.cuota-badge {
  margin-top: 8px;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}
.cuota-badge.al-dia     { background: rgba(34,197,94,0.15);   color: var(--success); }
.cuota-badge.por-vencer { background: rgba(234,179,8,0.15);   color: var(--warning); }
.cuota-badge.vencida    { background: rgba(239,68,68,0.15);   color: var(--danger);  }
.cuota-badge.sin-info   { background: rgba(100,100,100,0.15); color: var(--muted);   }

.countdown {
  margin-top: 12px;
  font-size: 12px;
  color: var(--muted);
}
</style>