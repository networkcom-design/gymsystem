<script setup>
import { ref, onMounted } from 'vue'
import http from '../api/http'
import { formatARS, nombreMes } from '../utils/format'

const stats = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const { data } = await http.get('/dashboard')
    stats.value = data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <h2>Dashboard</h2>

    <div v-if="loading" class="empty">Cargando...</div>
    <template v-else-if="stats">

      <!-- KPIs principales -->
      <div class="grid grid-4" style="margin-bottom:24px">
        <div class="card stat">
          <div class="label">Alumnos activos</div>
          <div class="value primary">{{ stats.alumnosActivos }}</div>
        </div>
        <div class="card stat">
          <div class="label">Cuotas al día</div>
          <div class="value success">{{ stats.cuotasPagadasMes }}</div>
        </div>
        <div class="card stat">
          <div class="label">Cuotas pendientes</div>
          <div class="value warning">{{ stats.cuotasPendientesMes }}</div>
        </div>
        <div class="card stat">
          <div class="label">Cuotas vencidas</div>
          <div class="value" style="color:var(--danger)">{{ stats.cuotasVencidasTotal }}</div>
        </div>
      </div>

      <!-- Ingresos + Rutinas -->
      <div class="grid grid-3" style="margin-bottom:24px">
        <div class="card stat">
          <div class="label">Recaudado este mes</div>
          <div class="value success">{{ formatARS(stats.ingresosDelMes) }}</div>
        </div>
        <div class="card stat">
          <div class="label">Rutinas plantillas</div>
          <div class="value primary">{{ stats.rutinasPlantillas }}</div>
        </div>
        <div class="card stat">
          <div class="label">Rutinas personalizadas</div>
          <div class="value" style="color:var(--accent)">{{ stats.rutinasPersonalizadas }}</div>
        </div>
      </div>

      <!-- Resumen rápido -->
      <div class="card">
        <h3 style="margin-bottom:16px;font-size:15px;color:var(--muted);text-transform:uppercase;letter-spacing:1px">Resumen</h3>
        <div class="grid grid-3">
          <div class="stat">
            <div class="label">Total alumnos</div>
            <div class="value">{{ stats.alumnosTotales }}</div>
          </div>
          <div class="stat">
            <div class="label">Disciplinas activas</div>
            <div class="value primary">{{ stats.disciplinas }}</div>
          </div>
          <div class="stat">
            <div class="label">Período</div>
            <div class="value" style="font-size:18px;color:var(--muted)">{{ nombreMes(stats.mes) }} {{ stats.anio }}</div>
          </div>
        </div>
      </div>

    </template>
  </div>
</template>
