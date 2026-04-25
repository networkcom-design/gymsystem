<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import http from '../api/http'
import { formatARS, formatFecha, nombreMes, MESES } from '../utils/format'

const anioActual = new Date().getFullYear()
const mesActual  = new Date().getMonth() + 1

const anio        = ref(anioActual)
const mesFilter   = ref('')
const estadoFilter= ref('')
const alumnoFilter= ref('')
const cuotas      = ref([])
const alumnos     = ref([])
const loading     = ref(true)
const generando   = ref(false)
const exportando  = ref(false)

// modal pago
const showPagoModal = ref(false)
const cuotaSeleccionada = ref(null)
const metodoPago = ref('EFECTIVO')
const pagoFecha  = ref(new Date().toISOString().slice(0, 10))
const pagandoId  = ref(null)

onMounted(async () => {
  await Promise.all([cargarCuotas(), cargarAlumnos()])
  loading.value = false
})
watch(anio, cargarCuotas)

async function cargarCuotas() {
  const { data } = await http.get('/cuotas', { params: { anio: anio.value } })
  cuotas.value = data
}
async function cargarAlumnos() {
  const { data } = await http.get('/alumnos', { params: { activo: true } })
  alumnos.value = data
}

const cuotasFiltradas = computed(() => {
  let list = cuotas.value
  if (mesFilter.value)    list = list.filter(c => c.mes === Number(mesFilter.value))
  if (estadoFilter.value) list = list.filter(c => c.estado === estadoFilter.value)
  if (alumnoFilter.value) {
    const q = alumnoFilter.value.toLowerCase()
    list = list.filter(c => {
      const nombre = `${c.alumno?.apellido || ''} ${c.alumno?.nombre || ''}`.toLowerCase()
      return nombre.includes(q)
    })
  }
  return list
})

const totalFiltrado  = computed(() => cuotasFiltradas.value.reduce((s, c) => s + (c.monto || 0), 0))
const totalRecaudado = computed(() => cuotasFiltradas.value.filter(c => c.estado === 'PAGADA').reduce((s, c) => s + (c.monto || 0), 0))

async function generarMes() {
  if (!confirm(`¿Generar cuotas para ${nombreMes(mesActual)} ${anioActual}?`)) return
  generando.value = true
  try {
    await http.post('/cuotas/generar-mes', { mes: mesActual, anio: anioActual })
    await cargarCuotas()
  } catch (e) {
    alert(e.response?.data?.message || 'Error al generar cuotas')
  } finally {
    generando.value = false
  }
}

function abrirPago(c) {
  cuotaSeleccionada.value = c
  metodoPago.value = 'EFECTIVO'
  pagoFecha.value = new Date().toISOString().slice(0, 10)
  showPagoModal.value = true
}
function cerrarPago() { showPagoModal.value = false; cuotaSeleccionada.value = null }

async function registrarPago() {
  pagandoId.value = cuotaSeleccionada.value.id
  try {
    await http.post(`/cuotas/${cuotaSeleccionada.value.id}/pagar`, {
      metodoPago: metodoPago.value,
      fechaPago: pagoFecha.value
    })
    await cargarCuotas()
    cerrarPago()
  } catch (e) {
    alert(e.response?.data?.message || 'Error al registrar pago')
  } finally {
    pagandoId.value = null
  }
}

async function exportarExcel() {
  exportando.value = true
  try {
    const resp = await http.get('/cuotas/export', {
      params: { anio: anio.value },
      responseType: 'blob'
    })
    const cd = resp.headers['content-disposition'] || ''
    const match = cd.match(/filename="?([^"]+)"?/)
    const filename = match ? match[1] : `cuotas_${anio.value}.xlsx`
    const url = URL.createObjectURL(new Blob([resp.data]))
    const a = document.createElement('a')
    a.href = url; a.download = filename; a.click()
    URL.revokeObjectURL(url)
  } catch { alert('Error al exportar') }
  finally { exportando.value = false }
}

function estadoClass(e) {
  if (e === 'PAGADA') return 'pagada'
  if (e === 'PENDIENTE') return 'pendiente'
  return 'vencida'
}
function estadoLabel(e) {
  if (e === 'PAGADA') return 'Pagada'
  if (e === 'PENDIENTE') return 'Pendiente'
  return 'Vencida'
}
</script>

<template>
  <div>
    <h2>Cuotas</h2>

    <div class="toolbar">
      <select v-model.number="anio" style="max-width:100px">
        <option v-for="y in [anioActual-1, anioActual, anioActual+1]" :key="y" :value="y">{{ y }}</option>
      </select>
      <select v-model.number="mesFilter" style="max-width:140px">
        <option value="">Todos los meses</option>
        <option v-for="(m,i) in MESES" :key="i+1" :value="i+1">{{ m }}</option>
      </select>
      <select v-model="estadoFilter" style="max-width:140px">
        <option value="">Todos los estados</option>
        <option value="PAGADA">Pagadas</option>
        <option value="PENDIENTE">Pendientes</option>
        <option value="VENCIDA">Vencidas</option>
      </select>
      <input v-model="alumnoFilter" placeholder="Buscar alumno…" style="max-width:200px" />
      <div class="spacer"></div>
      <button class="secondary small" @click="exportarExcel" :disabled="exportando">
        {{ exportando ? 'Exportando…' : '⬇ Excel' }}
      </button>
      <button @click="generarMes" :disabled="generando">
        {{ generando ? 'Generando…' : `Generar ${nombreMes(mesActual)}` }}
      </button>
    </div>

    <!-- Totales rápidos -->
    <div class="grid grid-3" style="margin-bottom:20px">
      <div class="card stat">
        <div class="label">Cuotas</div>
        <div class="value primary">{{ cuotasFiltradas.length }}</div>
      </div>
      <div class="card stat">
        <div class="label">Total facturado</div>
        <div class="value">{{ formatARS(totalFiltrado) }}</div>
      </div>
      <div class="card stat">
        <div class="label">Recaudado</div>
        <div class="value success">{{ formatARS(totalRecaudado) }}</div>
      </div>
    </div>

    <div v-if="loading" class="empty">Cargando...</div>
    <table v-else>
      <thead>
        <tr>
          <th>Alumno</th>
          <th>Disciplinas</th>
          <th>Período</th>
          <th>Monto</th>
          <th>Vencimiento</th>
          <th>Estado</th>
          <th>Pago</th>
          <th>Acción</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="c in cuotasFiltradas" :key="c.id">
          <td><strong>{{ c.alumno?.apellido }}, {{ c.alumno?.nombre }}</strong></td>
          <td>
            <div class="chip-group">
              <span v-for="d in c.disciplinas" :key="d.id" class="chip">{{ d.nombre }}</span>
              <span v-if="!c.disciplinas?.length" style="color:var(--muted);font-size:12px">—</span>
            </div>
          </td>
          <td>{{ nombreMes(c.mes) }} {{ c.anio }}</td>
          <td>{{ formatARS(c.monto) }}</td>
          <td>{{ formatFecha(c.fechaVencimiento) }}</td>
          <td><span class="badge" :class="estadoClass(c.estado)">{{ estadoLabel(c.estado) }}</span></td>
          <td>
            <div v-if="c.estado === 'PAGADA'">
              <div style="font-size:12px;color:var(--muted)">{{ formatFecha(c.fechaPago) }}</div>
              <div style="font-size:11px;color:var(--muted)">{{ c.metodoPago }}</div>
            </div>
            <span v-else style="color:var(--muted)">—</span>
          </td>
          <td>
            <button
              v-if="c.estado !== 'PAGADA'"
              class="small success"
              @click="abrirPago(c)"
              :disabled="pagandoId === c.id"
            >Registrar pago</button>
            <span v-else style="color:var(--success);font-size:12px">✓</span>
          </td>
        </tr>
        <tr v-if="cuotasFiltradas.length === 0">
          <td colspan="8" class="empty">Sin cuotas para mostrar</td>
        </tr>
      </tbody>
    </table>

    <!-- Modal pago -->
    <div v-if="showPagoModal" class="modal-overlay" @click.self="cerrarPago">
      <div class="modal">
        <h3>Registrar pago</h3>
        <p style="color:var(--muted);margin-bottom:16px">
          {{ cuotaSeleccionada?.alumno?.apellido }}, {{ cuotaSeleccionada?.alumno?.nombre }} — {{ nombreMes(cuotaSeleccionada?.mes) }} {{ cuotaSeleccionada?.anio }}
          — {{ formatARS(cuotaSeleccionada?.monto) }}
        </p>
        <div class="field">
          <label>Método de pago</label>
          <select v-model="metodoPago">
            <option value="EFECTIVO">Efectivo</option>
            <option value="TRANSFERENCIA">Transferencia</option>
            <option value="TARJETA_DEBITO">Tarjeta débito</option>
            <option value="TARJETA_CREDITO">Tarjeta crédito</option>
            <option value="MERCADO_PAGO">Mercado Pago</option>
          </select>
        </div>
        <div class="field">
          <label>Fecha de pago</label>
          <input v-model="pagoFecha" type="date" />
        </div>
        <div class="actions">
          <button class="secondary" @click="cerrarPago">Cancelar</button>
          <button class="success" @click="registrarPago" :disabled="pagandoId !== null">
            {{ pagandoId !== null ? 'Registrando…' : 'Confirmar pago' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
