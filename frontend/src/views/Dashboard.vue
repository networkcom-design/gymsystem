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
          <div class="value success">{{ stats.cuotasPagadas }}</div>
        </div>
        <div class="card stat">
          <div class="label">Cuotas pendientes</div>
          <div class="value warning">{{ stats.cuotasPendientes }}</div>
        </div>
        <div class="card stat">
          <div class="label">Cuotas vencidas</div>
          <div class="value" style="color:var(--danger)">{{ stats.cuotasVencidas }}</div>
        </div>
      </div>

      <!-- Ingresos + Rutinas -->
      <div class="grid grid-3" style="margin-bottom:24px">
        <div class="card stat">
          <div class="label">Recaudado este mes</div>
          <div class="value success">{{ formatARS(stats.recaudadoMes) }}</div>
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

      <!-- Tabla de disciplinas -->
      <div class="card" style="margin-bottom:24px">
        <h3 style="margin-bottom:16px;font-size:15px;color:var(--muted);text-transform:uppercase;letter-spacing:1px">Disciplinas activas</h3>
        <table>
          <thead>
            <tr>
              <th>Disciplina</th>
              <th>Alumnos</th>
              <th>Cuota mensual</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in stats.disciplinas" :key="d.id">
              <td>{{ d.nombre }}</td>
              <td>{{ d.alumnosCount }}</td>
              <td>{{ formatARS(d.precio) }}</td>
            </tr>
            <tr v-if="!stats.disciplinas?.length">
              <td colspan="3" style="text-align:center;color:var(--muted)">Sin disciplinas</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Cuotas vencidas recientes -->
      <div class="card">
        <h3 style="margin-bottom:16px;font-size:15px;color:var(--muted);text-transform:uppercase;letter-spacing:1px">Cuotas vencidas recientes</h3>
        <table>
          <thead>
            <tr>
              <th>Alumno</th>
              <th>Período</th>
              <th>Monto</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in stats.cuotasVencidasRecientes" :key="c.id">
              <td>{{ c.alumnoNombre }}</td>
              <td>{{ nombreMes(c.mes) }} {{ c.anio }}</td>
              <td>{{ formatARS(c.monto) }}</td>
            </tr>
            <tr v-if="!stats.cuotasVencidasRecientes?.length">
              <td colspan="3" style="text-align:center;color:var(--muted)">Sin cuotas vencidas</td>
            </tr>
          </tbody>
        </table>
      </div>

    </template>
  </div>
</template>
