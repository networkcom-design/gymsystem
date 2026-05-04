<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import http from '../api/http'

// ─── Enums ──────────────────────────────────────────────────────────────────
const NIVELES = ['PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO']
const OBJETIVOS = ['HIPERTROFIA', 'FUERZA', 'RESISTENCIA', 'DEFINICION', 'ACONDICIONAMIENTO', 'REHABILITACION']
const GRUPOS = ['PECHO', 'ESPALDA', 'HOMBROS', 'BICEPS', 'TRICEPS', 'PIERNAS', 'GLUTEOS', 'CORE', 'CARDIO', 'FULLBODY']
const DIAS = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']

const labelNivel = v => ({ PRINCIPIANTE:'Principiante', INTERMEDIO:'Intermedio', AVANZADO:'Avanzado' }[v] || v)
const labelObjetivo = v => ({ HIPERTROFIA:'Hipertrofia', FUERZA:'Fuerza', RESISTENCIA:'Resistencia', DEFINICION:'Definición', ACONDICIONAMIENTO:'Acondicionamiento', REHABILITACION:'Rehabilitación' }[v] || v)
const labelGrupo = v => ({ PECHO:'Pecho', ESPALDA:'Espalda', HOMBROS:'Hombros', BICEPS:'Bíceps', TRICEPS:'Tríceps', PIERNAS:'Piernas', GLUTEOS:'Glúteos', CORE:'Core', CARDIO:'Cardio', FULLBODY:'Full Body' }[v] || v)

// ─── Estado ──────────────────────────────────────────────────────────────────
const tab            = ref('PLANTILLA')   // 'PLANTILLA' | 'PERSONALIZADA'
const rutinas        = ref([])
const alumnos        = ref([])
const disciplinas    = ref([])
const loading        = ref(true) 

// Filtros plantillas
const filtroNivel     = ref('')
const filtroObjetivo  = ref('')
const filtroDisciplina= ref('')
const filtroTexto     = ref('')

// Filtro personalizadas
const alumnoSeleccionado = ref('')

// ─── Modal rutina ─────────────────────────────────────────────────────────────
const showModal  = ref(false)
const saving     = ref(false)
const errorMsg   = ref('')

// ─── QR Modal ─────────────────────────────────────────────────────────────────
const showQrModal = ref(false)
const qrRutina    = ref(null)
const qrImgUrl    = ref(null)
const qrLoading   = ref(false)

async function abrirQr(r) {
  qrRutina.value  = r
  qrImgUrl.value  = null
  qrLoading.value = true
  showQrModal.value = true
  try {
    const response = await http.get(`/rutinas/${r.id}/qr`, { responseType: 'blob' })
    qrImgUrl.value = URL.createObjectURL(response.data)
  } catch {
    alert('Error al generar el QR')
    showQrModal.value = false
  } finally {
    qrLoading.value = false
  }
}

function cerrarQr() {
  if (qrImgUrl.value) URL.revokeObjectURL(qrImgUrl.value)
  qrImgUrl.value    = null
  showQrModal.value = false
  qrRutina.value    = null
}
  
const ejercicioVacio = () => ({
  nombre: '', series: 3, repeticiones: '10', descanso: '60s',
  peso: '', grupoMuscular: '', notas: '', orden: 0
})
const rutinaVacia = () => ({
  id: null, nombre: '', descripcion: '', tipo: 'PLANTILLA',
  nivel: 'PRINCIPIANTE', objetivo: 'HIPERTROFIA', dia: '',
  disciplinaId: null, alumnoId: null,
  ejercicios: [ejercicioVacio()], activa: true
})
const form = ref(rutinaVacia())

// ─── Modal asignar alumno ─────────────────────────────────────────────────────
const showAsignarModal  = ref(false)
const rutinaParaAsignar = ref(null)
const alumnoParaAsignar = ref('')
const asignando         = ref(false)

onMounted(async () => {
  await Promise.all([cargarRutinas(), cargarAlumnos(), cargarDisciplinas()])
  loading.value = false
})

watch(tab, () => {
  filtroNivel.value = ''
  filtroObjetivo.value = ''
  filtroDisciplina.value = ''
  filtroTexto.value = ''
})

// ─── Carga de datos ───────────────────────────────────────────────────────────
async function cargarRutinas() {
  const { data } = await http.get('/rutinas')
  rutinas.value = data
}
async function cargarAlumnos() {
  const { data } = await http.get('/alumnos')
  alumnos.value = data
}
async function cargarDisciplinas() {
  const { data } = await http.get('/disciplinas')
  disciplinas.value = data
}

// ─── Filtros computados ───────────────────────────────────────────────────────
const plantillas = computed(() => {
  let list = rutinas.value.filter(r => r.tipo === 'PLANTILLA')
  if (filtroNivel.value)      list = list.filter(r => r.nivel === filtroNivel.value)
  if (filtroObjetivo.value)   list = list.filter(r => r.objetivo === filtroObjetivo.value)
  if (filtroDisciplina.value) list = list.filter(r => r.disciplinaId === Number(filtroDisciplina.value) || r.disciplina?.id === Number(filtroDisciplina.value))
  if (filtroTexto.value) {
    const q = filtroTexto.value.toLowerCase()
    list = list.filter(r => r.nombre.toLowerCase().includes(q) || r.descripcion?.toLowerCase().includes(q))
  }
  return list
})

const personalizadas = computed(() => {
  let list = rutinas.value.filter(r => r.tipo === 'PERSONALIZADA')
  if (alumnoSeleccionado.value) list = list.filter(r => r.alumnoId === Number(alumnoSeleccionado.value) || r.alumno?.id === Number(alumnoSeleccionado.value))
  return list
})

const alumnoNombre = computed(() => {
  if (!alumnoSeleccionado.value) return null
  const a = alumnos.value.find(a => a.id === Number(alumnoSeleccionado.value))
  return a ? `${a.nombre} ${a.apellido}` : null
})

// ─── CRUD Modal ───────────────────────────────────────────────────────────────
function abrirNueva() {
  form.value = rutinaVacia()
  form.value.tipo = tab.value
  if (tab.value === 'PERSONALIZADA' && alumnoSeleccionado.value) {
    form.value.alumnoId = Number(alumnoSeleccionado.value)
  }
  errorMsg.value = ''
  showModal.value = true
}

function abrirEditar(r) {
  form.value = {
    ...r,
    disciplinaId: r.disciplina?.id || r.disciplinaId || null,
    alumnoId:     r.alumno?.id     || r.alumnoId     || null,
    ejercicios:   (r.ejercicios || []).map(e => ({ ...e }))
  }
  if (!form.value.ejercicios.length) form.value.ejercicios.push(ejercicioVacio())
  errorMsg.value = ''
  showModal.value = true
}

function cerrarModal() { showModal.value = false }

async function guardar() {
  if (!form.value.nombre.trim()) { errorMsg.value = 'El nombre es requerido'; return }
  saving.value = true; errorMsg.value = ''
  try {
    const payload = {
      nombre:      form.value.nombre,
      descripcion: form.value.descripcion,
      tipo:        form.value.tipo,
      nivel:       form.value.nivel,
      objetivo:    form.value.objetivo,
      dia:         form.value.dia || null,
      disciplina:  form.value.disciplinaId ? { id: form.value.disciplinaId } : null,
      alumno:      form.value.alumnoId     ? { id: form.value.alumnoId }     : null,
      activa:      form.value.activa,
      ejercicios:  form.value.ejercicios.filter(e => e.nombre.trim()).map((e, i) => ({ ...e, orden: i }))
    }
    if (form.value.id) {
      await http.put(`/rutinas/${form.value.id}`, payload)
    } else {
      await http.post('/rutinas', payload)
    }
    await cargarRutinas()
    cerrarModal()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || 'Error al guardar'
  } finally {
    saving.value = false
  }
}

async function duplicar(r) {
  try {
    await http.post(`/rutinas/${r.id}/duplicar`)
    await cargarRutinas()
  } catch { alert('Error al duplicar') }
}

async function eliminar(r) {
  if (!confirm(`¿Eliminar "${r.nombre}"?`)) return
  try {
    await http.delete(`/rutinas/${r.id}`)
    await cargarRutinas()
  } catch { alert('Error al eliminar') }
}

// ─── Asignar a alumno ─────────────────────────────────────────────────────────
function abrirAsignar(r) {
  rutinaParaAsignar.value = r
  alumnoParaAsignar.value = ''
  showAsignarModal.value = true
}
function cerrarAsignar() { showAsignarModal.value = false; rutinaParaAsignar.value = null }

async function confirmarAsignar() {
  if (!alumnoParaAsignar.value) return
  asignando.value = true
  try {
    await http.post(`/rutinas/${rutinaParaAsignar.value.id}/asignar-alumno`, null, {
      params: { alumnoId: alumnoParaAsignar.value }
    })
    await cargarRutinas()
    cerrarAsignar()
    // Ir a pestaña personalizadas con ese alumno seleccionado
    alumnoSeleccionado.value = String(alumnoParaAsignar.value)
    tab.value = 'PERSONALIZADA'
  } catch (e) {
    alert(e.response?.data?.message || 'Error al asignar')
  } finally {
    asignando.value = false
  }
}

// ─── Ejercicios helpers ───────────────────────────────────────────────────────
function agregarEjercicio() { form.value.ejercicios.push(ejercicioVacio()) }
function quitarEjercicio(i) {
  if (form.value.ejercicios.length > 1) form.value.ejercicios.splice(i, 1)
}
function subirEjercicio(i) {
  if (i > 0) {
    const arr = form.value.ejercicios
    ;[arr[i - 1], arr[i]] = [arr[i], arr[i - 1]]
  }
}
function bajarEjercicio(i) {
  const arr = form.value.ejercicios
  if (i < arr.length - 1) [arr[i], arr[i + 1]] = [arr[i + 1], arr[i]]
}

// ─── Colores nivel ────────────────────────────────────────────────────────────
function nivelColor(n) {
  return { PRINCIPIANTE: 'var(--success)', INTERMEDIO: 'var(--warning)', AVANZADO: 'var(--danger)' }[n] || 'var(--muted)'
}
function objetivoColor(o) {
  return { HIPERTROFIA:'var(--primary)', FUERZA:'var(--danger)', RESISTENCIA:'var(--success)', DEFINICION:'var(--accent)', ACONDICIONAMIENTO:'var(--warning)', REHABILITACION:'var(--muted)' }[o] || 'var(--muted)'
}
</script>

<template>
  <div>
    <h2>Rutinas</h2>

    <!-- Tabs -->
    <div class="tabs">
      <button class="tab-btn" :class="{ active: tab === 'PLANTILLA' }" @click="tab = 'PLANTILLA'">
        📋 Plantillas
      </button>
      <button class="tab-btn" :class="{ active: tab === 'PERSONALIZADA' }" @click="tab = 'PERSONALIZADA'">
        👤 Personalizadas
      </button>
    </div>

    <!-- ═══ TAB PLANTILLAS ═══ -->
    <template v-if="tab === 'PLANTILLA'">
      <div class="toolbar">
        <input v-model="filtroTexto" placeholder="Buscar…" style="max-width:200px" />
        <select v-model="filtroNivel" style="max-width:160px">
          <option value="">Todos los niveles</option>
          <option v-for="n in NIVELES" :key="n" :value="n">{{ labelNivel(n) }}</option>
        </select>
        <select v-model="filtroObjetivo" style="max-width:180px">
          <option value="">Todos los objetivos</option>
          <option v-for="o in OBJETIVOS" :key="o" :value="o">{{ labelObjetivo(o) }}</option>
        </select>
        <select v-model="filtroDisciplina" style="max-width:180px">
          <option value="">Todas las disciplinas</option>
          <option v-for="d in disciplinas" :key="d.id" :value="d.id">{{ d.nombre }}</option>
        </select>
        <div class="spacer"></div>
        <button @click="abrirNueva">+ Nueva plantilla</button>
      </div>

      <div v-if="loading" class="empty">Cargando...</div>
      <div v-else-if="plantillas.length === 0" class="empty">Sin plantillas. ¡Creá la primera!</div>
      <div v-else class="rutinas-grid">
        <div v-for="r in plantillas" :key="r.id" class="card rutina-card">
          <div class="rutina-header">
            <div class="rutina-nombre">{{ r.nombre }}</div>
            <div class="rutina-badges">
              <span class="badge-small" :style="`color:${nivelColor(r.nivel)}`">{{ labelNivel(r.nivel) }}</span>
              <span class="badge-small" :style="`color:${objetivoColor(r.objetivo)}`">{{ labelObjetivo(r.objetivo) }}</span>
            </div>
          </div>
          <div v-if="r.descripcion" class="rutina-desc">{{ r.descripcion }}</div>
          <div class="rutina-meta">
            <span v-if="r.disciplina" class="chip">{{ r.disciplina.nombre }}</span>
            <span v-if="r.dia" class="chip">{{ r.dia }}</span>
            <span class="chip">{{ r.ejercicios?.length || 0 }} ejercicios</span>
          </div>
          <div v-if="r.ejercicios?.length" class="ejercicios-list">
            <div v-for="(e, i) in r.ejercicios.slice(0, 4)" :key="i" class="ejercicio-item">
              <span class="ej-num">{{ i + 1 }}</span>
              <span class="ej-nombre">{{ e.nombre }}</span>
              <span class="ej-detalle">{{ e.series }}×{{ e.repeticiones }}<span v-if="e.peso"> · {{ e.peso }}</span></span>
            </div>
            <div v-if="r.ejercicios.length > 4" class="ej-mas">+{{ r.ejercicios.length - 4 }} más</div>
          </div>
          <div class="">
            <button class="small secondary" @click="abrirEditar(r)">Editar</button>
            <button class="small secondary" @click="duplicar(r)">Duplicar</button>
            <button class="small qr" @click="abrirQr(r)">📱 QR</button>
            <button class="small success" @click="abrirAsignar(r)">Asignar a cliente</button>
            <button class="small danger" @click="eliminar(r)">Borrar</button>
          </div>
        </div>
      </div>
    </template>

    <!-- ═══ TAB PERSONALIZADAS ═══ -->
    <template v-else>
      <!-- Selector de alumno prominente -->
      <div class="alumno-selector-box">
        <div class="alumno-selector-label">Seleccioná un alumno para ver sus rutinas personalizadas</div>
        <div class="alumno-selector-row">
          <select v-model="alumnoSeleccionado" style="max-width:320px;font-size:15px;padding:10px 14px">
            <option value="">— Elegir alumno —</option>
            <option v-for="a in alumnos" :key="a.id" :value="a.id">
              {{ a.apellido }}, {{ a.nombre }}
            </option>
          </select>
          <button
            v-if="alumnoSeleccionado"
            @click="abrirNueva"
            style="white-space:nowrap"
          >+ Rutina para {{ alumnoNombre }}</button>
        </div>
      </div>

      <div v-if="!alumnoSeleccionado" class="empty">
        <div style="font-size:40px;margin-bottom:12px">👤</div>
        Seleccioná un alumno para ver y gestionar sus rutinas personalizadas.
      </div>

      <template v-else>
        <div v-if="loading" class="empty">Cargando...</div>
        <div v-else-if="personalizadas.length === 0" class="empty">
          {{ alumnoNombre }} no tiene rutinas personalizadas aún.<br>
          <span style="color:var(--muted);font-size:13px">
            Podés crear una nueva o asignarle una plantilla desde la pestaña Plantillas.
          </span>
        </div>
        <div v-else class="rutinas-grid">
          <div v-for="r in personalizadas" :key="r.id" class="card rutina-card personalizada">
            <div class="rutina-header">
              <div class="rutina-nombre">{{ r.nombre }}</div>
              <div class="rutina-badges">
                <span class="badge-small" :style="`color:${nivelColor(r.nivel)}`">{{ labelNivel(r.nivel) }}</span>
                <span class="badge-small" :style="`color:${objetivoColor(r.objetivo)}`">{{ labelObjetivo(r.objetivo) }}</span>
              </div>
            </div>
            <div v-if="r.descripcion" class="rutina-desc">{{ r.descripcion }}</div>
            <div class="rutina-meta">
              <span v-if="r.disciplina" class="chip">{{ r.disciplina.nombre }}</span>
              <span v-if="r.dia" class="chip">{{ r.dia }}</span>
              <span class="chip">{{ r.ejercicios?.length || 0 }} ejercicios</span>
            </div>
            <div v-if="r.ejercicios?.length" class="ejercicios-list">
              <div v-for="(e, i) in r.ejercicios" :key="i" class="ejercicio-item">
                <span class="ej-num">{{ i + 1 }}</span>
                <span class="ej-nombre">{{ e.nombre }}</span>
                <span class="ej-detalle">
                  {{ e.series }}×{{ e.repeticiones }}
                  <span v-if="e.peso"> · {{ e.peso }}</span>
                  <span v-if="e.grupoMuscular" style="color:var(--accent)"> · {{ labelGrupo(e.grupoMuscular) }}</span>
                </span>
              </div>
            </div>
            <div class="rutina-actions">
              <button class="small secondary" @click="abrirEditar(r)">Editar</button>
              <button class="small secondary" @click="duplicar(r)">Duplicar</button>
              <button class="small qr" @click="abrirQr(r)">📱 QR</button>
              <button class="small danger" @click="eliminar(r)">Borrar</button>
            </div>
          </div>
        </div>
      </template>
    </template>

    <!-- ═══ MODAL RUTINA ═══ -->
    <div v-if="showModal" class="modal-overlay" @click.self="cerrarModal">
      <div class="modal" style="width:680px">
        <h3>{{ form.id ? 'Editar rutina' : (form.tipo === 'PLANTILLA' ? 'Nueva plantilla' : `Nueva rutina para alumno`) }}</h3>

        <div class="grid grid-2">
          <div class="field" style="grid-column:1/-1">
            <label>Nombre *</label>
            <input v-model="form.nombre" placeholder="Ej: Rutina de Fuerza Tren Superior" />
          </div>
          <div class="field" style="grid-column:1/-1">
            <label>Descripción</label>
            <textarea v-model="form.descripcion" rows="2" placeholder="Descripción opcional…"></textarea>
          </div>
          <div class="field">
            <label>Nivel</label>
            <select v-model="form.nivel">
              <option v-for="n in NIVELES" :key="n" :value="n">{{ labelNivel(n) }}</option>
            </select>
          </div>
          <div class="field">
            <label>Objetivo</label>
            <select v-model="form.objetivo">
              <option v-for="o in OBJETIVOS" :key="o" :value="o">{{ labelObjetivo(o) }}</option>
            </select>
          </div>
          <div class="field">
            <label>Disciplina (opcional)</label>
            <select v-model="form.disciplinaId">
              <option :value="null">— Sin disciplina —</option>
              <option v-for="d in disciplinas" :key="d.id" :value="d.id">{{ d.nombre }}</option>
            </select>
          </div>
          <div class="field">
            <label>Día (opcional)</label>
            <select v-model="form.dia">
              <option value="">— Sin día asignado —</option>
              <option v-for="d in DIAS" :key="d" :value="d">{{ d }}</option>
            </select>
          </div>

          <!-- Solo si es personalizada -->
          <div v-if="form.tipo === 'PERSONALIZADA'" class="field" style="grid-column:1/-1">
            <label>Alumno</label>
            <select v-model="form.alumnoId">
              <option :value="null">— Sin alumno —</option>
              <option v-for="a in alumnos" :key="a.id" :value="a.id">{{ a.apellido }}, {{ a.nombre }}</option>
            </select>
          </div>
        </div>

        <!-- Ejercicios -->
        <div style="margin-top:8px;margin-bottom:10px;display:flex;align-items:center;justify-content:space-between">
          <label style="margin:0">Ejercicios</label>
          <button class="small secondary" type="button" @click="agregarEjercicio">+ Agregar ejercicio</button>
        </div>

        <div v-for="(ej, i) in form.ejercicios" :key="i" class="ejercicio-editor">
          <div class="ej-order">
            <button class="small secondary" type="button" @click="subirEjercicio(i)" :disabled="i === 0">↑</button>
            <span style="color:var(--muted);font-size:12px">{{ i + 1 }}</span>
            <button class="small secondary" type="button" @click="bajarEjercicio(i)" :disabled="i === form.ejercicios.length - 1">↓</button>
          </div>
          <div class="ej-fields">
            <input v-model="ej.nombre" placeholder="Nombre del ejercicio *" style="font-weight:600" />
            <div class="ej-row">
              <input v-model.number="ej.series" type="number" min="1" placeholder="Series" style="width:70px" />
              <input v-model="ej.repeticiones" placeholder="Reps / Duración" style="width:110px" />
              <input v-model="ej.descanso" placeholder="Descanso" style="width:90px" />
              <input v-model="ej.peso" placeholder="Peso / Carga" style="width:100px" />
              <select v-model="ej.grupoMuscular" style="width:130px">
                <option value="">Grupo musc.</option>
                <option v-for="g in GRUPOS" :key="g" :value="g">{{ labelGrupo(g) }}</option>
              </select>
            </div>
            <input v-model="ej.notas" placeholder="Notas (técnica, variaciones…)" style="font-size:12px;color:var(--muted)" />
          </div>
          <button class="small danger" type="button" @click="quitarEjercicio(i)" :disabled="form.ejercicios.length === 1">✕</button>
        </div>

        <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
        <div class="actions">
          <button class="secondary" @click="cerrarModal">Cancelar</button>
          <button @click="guardar" :disabled="saving">{{ saving ? 'Guardando...' : 'Guardar' }}</button>
        </div>
      </div>
    </div>

    <!-- ═══ MODAL ASIGNAR ALUMNO ═══ -->
    <div v-if="showAsignarModal" class="modal-overlay" @click.self="cerrarAsignar">
      <div class="modal" style="width:420px">
        <h3>Asignar plantilla a alumno</h3>
        <p style="color:var(--muted);margin-bottom:16px">
          Se creará una copia de <strong>{{ rutinaParaAsignar?.nombre }}</strong>
          como rutina personalizada para el alumno seleccionado.
        </p>
        <div class="field">
          <label>Alumno *</label>
          <select v-model="alumnoParaAsignar">
            <option value="">— Elegir alumno —</option>
            <option v-for="a in alumnos" :key="a.id" :value="a.id">{{ a.apellido }}, {{ a.nombre }}</option>
          </select>
        </div>
        <div class="actions">
          <button class="secondary" @click="cerrarAsignar">Cancelar</button>
          <button @click="confirmarAsignar" :disabled="!alumnoParaAsignar || asignando">
            {{ asignando ? 'Asignando...' : 'Asignar' }}
          </button>
        </div>
      </div>
    </div>
    <!-- ═══ MODAL QR ═══ -->
    <div v-if="showQrModal" class="modal-overlay" @click.self="cerrarQr">
      <div class="modal" style="width:340px;text-align:center">
        <h3 style="margin-bottom:4px">📱 Código QR</h3>
        <p style="color:var(--muted);font-size:13px;margin-bottom:16px">
          {{ qrRutina?.nombre }}
        </p>
    
        <div v-if="qrLoading" style="padding:40px 0;color:var(--muted)">
          Generando QR...
        </div>
        <template v-else>
          <img :src="qrImgUrl" alt="QR Rutina" style="width:240px;height:240px;border-radius:8px;border:3px solid var(--border)" />
          <p style="font-size:12px;color:var(--muted);margin-top:12px">
            Escaneá con la cámara del celular para abrir el PDF de la rutina
          </p>
          
            :href="`/api/rutinas/${qrRutina?.id}/pdf`"
            target="_blank"
            style="display:inline-block;margin-top:8px;padding:8px 16px;background:var(--primary);color:#fff;border-radius:6px;font-size:13px;text-decoration:none"
          >
            📄 Abrir PDF
          </a>
        </template>
    
        <div class="actions" style="margin-top:16px;justify-content:center">
          <button class="secondary" @click="cerrarQr">Cerrar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
button.small.qr {
  background: #7c3aed;
  color: white;
  border: none;
}
button.small.qr:hover {
  background: #6d28d9;
}
.rutinas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.rutina-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rutina-card.personalizada {
  border-left: 3px solid var(--primary);
}
.rutina-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}
.rutina-nombre {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
  line-height: 1.3;
}
.rutina-badges {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}
.badge-small {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.8px;
}
.rutina-desc {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.4;
}
.rutina-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.ejercicios-list {
  background: var(--bg);
  border-radius: 6px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.ejercicio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
.ej-num {
  width: 18px;
  height: 18px;
  background: var(--border);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  flex-shrink: 0;
  color: var(--muted);
}
.ej-nombre { font-weight: 600; flex: 1; }
.ej-detalle { color: var(--muted); white-space: nowrap; }
.ej-mas { font-size: 11px; color: var(--muted); text-align: center; padding-top: 2px; }
.rutina-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: auto;
  padding-top: 4px;
  border-top: 1px solid var(--border);
}

/* Selector de alumno */
.alumno-selector-box {
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
}
.alumno-selector-label {
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.alumno-selector-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

/* Editor de ejercicios */
.ejercicio-editor {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 10px;
  margin-bottom: 8px;
}
.ej-order {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
.ej-fields {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.ej-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.ej-row input, .ej-row select { width: auto; flex: 1; min-width: 70px; }

@media (max-width: 768px) {
  .rutinas-grid { grid-template-columns: 1fr; }
  .alumno-selector-row { flex-direction: column; align-items: stretch; }
  .alumno-selector-row select { max-width: 100% !important; }
  .ej-row { flex-direction: column; }
  .ej-row input, .ej-row select { width: 100%; }
}
</style>
