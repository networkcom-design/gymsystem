<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import http from '../api/http'
import { formatFecha, formatNum } from '../utils/format'

const alumnos          = ref([])
const alumnoSeleccionado = ref('')
const registros        = ref([])
const loading          = ref(false)

const showModal = ref(false)
const saving    = ref(false)
const errorMsg  = ref('')
const editando  = ref(null)

const formVacio = () => ({
  fecha: new Date().toISOString().slice(0, 10),
  peso: '', altura: '', porcentajeGrasa: '',
  cintura: '', pecho: '', brazos: '', piernas: '', gluteos: '',
  notas: ''
})
const form = ref(formVacio())

onMounted(async () => {
  const { data } = await http.get('/alumnos')
  alumnos.value = data
})

watch(alumnoSeleccionado, async (id) => {
  if (!id) { registros.value = []; return }
  loading.value = true
  try {
    const { data } = await http.get(`/progreso/alumno/${id}`)
    registros.value = data
  } finally {
    loading.value = false
  }
})

const alumnoNombre = computed(() => {
  const a = alumnos.value.find(a => a.id === Number(alumnoSeleccionado.value))
  return a ? `${a.nombre} ${a.apellido}` : ''
})

// Tendencias: comparar último vs anterior
const tendencias = computed(() => {
  if (registros.value.length < 2) return null
  const ultimo    = registros.value[0]
  const anterior  = registros.value[1]
  const delta = (campo) => {
    const u = Number(ultimo[campo])
    const a = Number(anterior[campo])
    if (!u || !a) return null
    const diff = u - a
    return { diff: diff.toFixed(1), up: diff > 0, down: diff < 0 }
  }
  return {
    peso:            delta('peso'),
    porcentajeGrasa: delta('porcentajeGrasa'),
    cintura:         delta('cintura'),
    brazos:          delta('brazos'),
  }
})

function abrirNuevo() {
  editando.value = null
  form.value = formVacio()
  errorMsg.value = ''
  showModal.value = true
}

function abrirEditar(r) {
  editando.value = r.id
  form.value = {
    fecha:           r.fecha?.slice(0, 10) || '',
    peso:            r.peso ?? '',
    altura:          r.altura ?? '',
    porcentajeGrasa: r.porcentajeGrasa ?? '',
    cintura:         r.cintura ?? '',
    pecho:           r.pecho ?? '',
    brazos:          r.brazos ?? '',
    piernas:         r.piernas ?? '',
    gluteos:         r.gluteos ?? '',
    notas:           r.notas ?? ''
  }
  errorMsg.value = ''
  showModal.value = true
}

function cerrar() { showModal.value = false; editando.value = null }

async function guardar() {
  if (!form.value.fecha) { errorMsg.value = 'La fecha es requerida'; return }
  saving.value = true; errorMsg.value = ''
  try {
    const payload = {
      alumnoId:        Number(alumnoSeleccionado.value),
      fecha:           form.value.fecha,
      peso:            form.value.peso !== '' ? Number(form.value.peso) : null,
      altura:          form.value.altura !== '' ? Number(form.value.altura) : null,
      porcentajeGrasa: form.value.porcentajeGrasa !== '' ? Number(form.value.porcentajeGrasa) : null,
      cintura:         form.value.cintura !== '' ? Number(form.value.cintura) : null,
      pecho:           form.value.pecho !== '' ? Number(form.value.pecho) : null,
      brazos:          form.value.brazos !== '' ? Number(form.value.brazos) : null,
      piernas:         form.value.piernas !== '' ? Number(form.value.piernas) : null,
      gluteos:         form.value.gluteos !== '' ? Number(form.value.gluteos) : null,
      notas:           form.value.notas || null
    }
    if (editando.value) {
      await http.put(`/progreso/${editando.value}`, payload)
    } else {
      await http.post(`/progreso/alumno/${alumnoSeleccionado.value}`, payload)
    }
    const { data } = await http.get(`/progreso/alumno/${alumnoSeleccionado.value}`)
    registros.value = data
    cerrar()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || 'Error al guardar'
  } finally {
    saving.value = false
  }
}

async function eliminar(r) {
  if (!confirm(`¿Eliminar medición del ${formatFecha(r.fecha)}?`)) return
  try {
    await http.delete(`/progreso/${r.id}`)
    registros.value = registros.value.filter(x => x.id !== r.id)
  } catch { alert('Error al eliminar') }
}

function trendClass(t) {
  if (!t) return 'trend-flat'
  return t.up ? 'trend-up' : t.down ? 'trend-down' : 'trend-flat'
}
function trendIcon(t, invert = false) {
  if (!t || t.diff == 0) return '–'
  if (invert) return t.up ? '↑' : '↓'
  return t.up ? '↑' : '↓'
}
</script>

<template>
  <div>
    <h2>Progreso</h2>

    <!-- Selector de alumno -->
    <div class="card" style="margin-bottom:24px;padding:20px">
      <div style="font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:10px">
        Seleccioná un alumno para ver su historial de medidas
      </div>
      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
        <select v-model="alumnoSeleccionado" style="max-width:320px;font-size:15px;padding:10px 14px">
          <option value="">— Elegir alumno —</option>
          <option v-for="a in alumnos" :key="a.id" :value="a.id">
            {{ a.apellido }}, {{ a.nombre }}
          </option>
        </select>
        <button v-if="alumnoSeleccionado" @click="abrirNuevo">
          + Nueva medición para {{ alumnoNombre }}
        </button>
      </div>
    </div>

    <!-- Sin alumno seleccionado -->
    <div v-if="!alumnoSeleccionado" class="empty">
      <div style="font-size:40px;margin-bottom:12px">📏</div>
      Seleccioná un alumno para ver y registrar su progreso corporal.
    </div>

    <template v-else>
      <div v-if="loading" class="empty">Cargando...</div>

      <template v-else>
        <!-- Tarjetas de tendencia (si hay ≥2 registros) -->
        <div v-if="tendencias" class="grid grid-4" style="margin-bottom:20px">
          <div class="card stat">
            <div class="label">Peso</div>
            <div class="value" :class="trendClass(tendencias.peso)">
              {{ registros[0].peso ? formatNum(registros[0].peso) + ' kg' : '—' }}
              <span v-if="tendencias.peso" style="font-size:14px">
                {{ trendIcon(tendencias.peso) }} {{ Math.abs(tendencias.peso.diff) }}
              </span>
            </div>
          </div>
          <div class="card stat">
            <div class="label">% Grasa</div>
            <div class="value" :class="tendencias.porcentajeGrasa?.down ? 'trend-up' : tendencias.porcentajeGrasa?.up ? 'trend-down' : 'trend-flat'">
              {{ registros[0].porcentajeGrasa ? formatNum(registros[0].porcentajeGrasa) + '%' : '—' }}
              <span v-if="tendencias.porcentajeGrasa" style="font-size:14px">
                {{ trendIcon(tendencias.porcentajeGrasa, true) }} {{ Math.abs(tendencias.porcentajeGrasa.diff) }}
              </span>
            </div>
          </div>
          <div class="card stat">
            <div class="label">Cintura</div>
            <div class="value" :class="tendencias.cintura?.down ? 'trend-up' : tendencias.cintura?.up ? 'trend-down' : 'trend-flat'">
              {{ registros[0].cintura ? formatNum(registros[0].cintura) + ' cm' : '—' }}
            </div>
          </div>
          <div class="card stat">
            <div class="label">Brazos</div>
            <div class="value" :class="trendClass(tendencias.brazos)">
              {{ registros[0].brazos ? formatNum(registros[0].brazos) + ' cm' : '—' }}
            </div>
          </div>
        </div>

        <!-- Tabla de historial -->
        <div v-if="registros.length === 0" class="empty">
          Sin mediciones registradas para {{ alumnoNombre }}.<br>
          <span style="color:var(--muted);font-size:13px">Registrá la primera medición con el botón de arriba.</span>
        </div>
        <table v-else>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Peso (kg)</th>
              <th>Altura (cm)</th>
              <th>% Grasa</th>
              <th>Cintura</th>
              <th>Pecho</th>
              <th>Brazos</th>
              <th>Piernas</th>
              <th>Glúteos</th>
              <th>Notas</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(r, idx) in registros" :key="r.id" :class="{ 'latest-row': idx === 0 }">
              <td>
                <strong>{{ formatFecha(r.fecha) }}</strong>
                <span v-if="idx === 0" style="margin-left:6px;font-size:10px;color:var(--accent);font-weight:700">ÚLTIMO</span>
              </td>
              <td>{{ r.peso != null ? formatNum(r.peso) : '—' }}</td>
              <td>{{ r.altura != null ? formatNum(r.altura, 0) : '—' }}</td>
              <td>{{ r.porcentajeGrasa != null ? formatNum(r.porcentajeGrasa) + '%' : '—' }}</td>
              <td>{{ r.cintura != null ? formatNum(r.cintura) : '—' }}</td>
              <td>{{ r.pecho != null ? formatNum(r.pecho) : '—' }}</td>
              <td>{{ r.brazos != null ? formatNum(r.brazos) : '—' }}</td>
              <td>{{ r.piernas != null ? formatNum(r.piernas) : '—' }}</td>
              <td>{{ r.gluteos != null ? formatNum(r.gluteos) : '—' }}</td>
              <td style="max-width:160px;white-space:normal;font-size:12px;color:var(--muted)">{{ r.notas || '—' }}</td>
              <td>
                <div style="display:flex;gap:6px">
                  <button class="small secondary" @click="abrirEditar(r)">Editar</button>
                  <button class="small danger" @click="eliminar(r)">Borrar</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </template>
    </template>

    <!-- ═══ MODAL MEDICIÓN ═══ -->
    <div v-if="showModal" class="modal-overlay" @click.self="cerrar">
      <div class="modal" style="width:600px">
        <h3>{{ editando ? 'Editar medición' : `Nueva medición — ${alumnoNombre}` }}</h3>

        <div class="field">
          <label>Fecha *</label>
          <input v-model="form.fecha" type="date" />
        </div>

        <div style="margin:12px 0 8px;font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:1px">
          Medidas generales
        </div>
        <div class="grid grid-2">
          <div class="field">
            <label>Peso (kg)</label>
            <input v-model="form.peso" type="number" step="0.1" min="0" placeholder="75.5" />
          </div>
          <div class="field">
            <label>Altura (cm)</label>
            <input v-model="form.altura" type="number" step="0.1" min="0" placeholder="175" />
          </div>
          <div class="field">
            <label>% Grasa corporal</label>
            <input v-model="form.porcentajeGrasa" type="number" step="0.1" min="0" max="100" placeholder="18.5" />
          </div>
        </div>

        <div style="margin:12px 0 8px;font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:1px">
          Perímetros (cm)
        </div>
        <div class="grid grid-2">
          <div class="field">
            <label>Cintura</label>
            <input v-model="form.cintura" type="number" step="0.1" min="0" placeholder="80" />
          </div>
          <div class="field">
            <label>Pecho</label>
            <input v-model="form.pecho" type="number" step="0.1" min="0" placeholder="100" />
          </div>
          <div class="field">
            <label>Brazos</label>
            <input v-model="form.brazos" type="number" step="0.1" min="0" placeholder="35" />
          </div>
          <div class="field">
            <label>Piernas</label>
            <input v-model="form.piernas" type="number" step="0.1" min="0" placeholder="55" />
          </div>
          <div class="field">
            <label>Glúteos</label>
            <input v-model="form.gluteos" type="number" step="0.1" min="0" placeholder="95" />
          </div>
        </div>

        <div class="field">
          <label>Notas</label>
          <textarea v-model="form.notas" rows="2" placeholder="Observaciones, contexto, fotos de referencia…"></textarea>
        </div>

        <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
        <div class="actions">
          <button class="secondary" @click="cerrar">Cancelar</button>
          <button @click="guardar" :disabled="saving">{{ saving ? 'Guardando...' : 'Guardar' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.latest-row td { background: rgba(37, 99, 235, 0.05); }
</style>
