<script setup>
import { ref, onMounted, computed } from 'vue'
import http from '../api/http'
import { formatFecha } from '../utils/format'

const alumnos = ref([])
const disciplinas = ref([])
const loading = ref(true)
const search = ref('')
const filtroActivo = ref('')

const alumnoVacio = () => ({
  id: null, nombre: '', apellido: '', email: '', telefono: '',
  fechaNacimiento: '', fechaAlta: '', activo: true, disciplinaIds: []
})
const form = ref(alumnoVacio())
const showModal = ref(false)
const saving = ref(false)
const errorMsg = ref('')

onMounted(async () => {
  await Promise.all([cargarAlumnos(), cargarDisciplinas()])
  loading.value = false
})

async function cargarAlumnos() {
  const { data } = await http.get('/alumnos')
  alumnos.value = data
}
async function cargarDisciplinas() {
  const { data } = await http.get('/disciplinas')
  disciplinas.value = data
}

const alumnosFiltrados = computed(() => {
  let list = alumnos.value
  if (search.value) {
    const q = search.value.toLowerCase()
    list = list.filter(a =>
      (a.nombre + ' ' + a.apellido).toLowerCase().includes(q) ||
      a.email?.toLowerCase().includes(q) ||
      a.telefono?.includes(q)
    )
  }
  if (filtroActivo.value === 'activo') list = list.filter(a => a.activo)
  if (filtroActivo.value === 'inactivo') list = list.filter(a => !a.activo)
  return list
})

function abrirNuevo() {
  form.value = alumnoVacio()
  errorMsg.value = ''
  showModal.value = true
}
function abrirEditar(a) {
  form.value = {
    ...a,
    fechaNacimiento: a.fechaNacimiento ? a.fechaNacimiento.slice(0, 10) : '',
    fechaAlta: a.fechaAlta ? a.fechaAlta.slice(0, 10) : '',
    disciplinaIds: a.disciplinas?.map(d => d.id) || []
  }
  errorMsg.value = ''
  showModal.value = true
}
function cerrar() { showModal.value = false }

async function guardar() {
  if (!form.value.nombre.trim() || !form.value.apellido.trim()) {
    errorMsg.value = 'Nombre y apellido son requeridos'
    return
  }
  saving.value = true
  errorMsg.value = ''
  try {
    const payload = {
      nombre: form.value.nombre,
      apellido: form.value.apellido,
      email: form.value.email,
      telefono: form.value.telefono,
      fechaNacimiento: form.value.fechaNacimiento || null,
      fechaAlta: form.value.fechaAlta || null,
      activo: form.value.activo,
      disciplinaIds: form.value.disciplinaIds
    }
    if (form.value.id) {
      await http.put(`/alumnos/${form.value.id}`, payload)
    } else {
      await http.post('/alumnos', payload)
    }
    await cargarAlumnos()
    cerrar()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || 'Error al guardar'
  } finally {
    saving.value = false
  }
}

async function toggleActivo(a) {
  try {
    await http.put(`/alumnos/${a.id}`, { activo: !a.activo })
    a.activo = !a.activo
  } catch { /* noop */ }
}

function toggleDisciplina(id) {
  const idx = form.value.disciplinaIds.indexOf(id)
  if (idx === -1) form.value.disciplinaIds.push(id)
  else form.value.disciplinaIds.splice(idx, 1)
}
</script>

<template>
  <div>
    <h2>Alumnos</h2>

    <div class="toolbar">
      <input v-model="search" placeholder="Buscar por nombre, email, teléfono…" style="max-width:280px" />
      <select v-model="filtroActivo" style="max-width:160px">
        <option value="">Todos</option>
        <option value="activo">Activos</option>
        <option value="inactivo">Inactivos</option>
      </select>
      <div class="spacer"></div>
      <button @click="abrirNuevo">+ Nuevo alumno</button>
    </div>

    <div v-if="loading" class="empty">Cargando...</div>
    <table v-else>
      <thead>
        <tr>
          <th>Nombre</th>
          <th>Contacto</th>
          <th>Disciplinas</th>
          <th>Inicio</th>
          <th>Estado</th>
          <th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="a in alumnosFiltrados" :key="a.id">
          <td>
            <strong>{{ a.apellido }}, {{ a.nombre }}</strong>
          </td>
          <td>
            <div>{{ a.email || '—' }}</div>
            <div style="color:var(--muted);font-size:12px">{{ a.telefono || '' }}</div>
          </td>
          <td>
            <div class="chip-group">
              <span v-for="d in a.disciplinas" :key="d.id" class="chip">{{ d.nombre }}</span>
              <span v-if="!a.disciplinas?.length" style="color:var(--muted);font-size:12px">Sin disciplina</span>
            </div>
          </td>
          <td>{{ formatFecha(a.fechaAlta) }}</td>
          <td>
            <span class="badge" :class="a.activo ? 'pagada' : 'vencida'">
              {{ a.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </td>
          <td>
            <div style="display:flex;gap:6px;flex-wrap:wrap">
              <button class="small secondary" @click="abrirEditar(a)">Editar</button>
              <button class="small" :class="a.activo ? 'danger' : 'success'" @click="toggleActivo(a)">
                {{ a.activo ? 'Desactivar' : 'Activar' }}
              </button>
            </div>
          </td>
        </tr>
        <tr v-if="alumnosFiltrados.length === 0">
          <td colspan="6" class="empty">Sin resultados</td>
        </tr>
      </tbody>
    </table>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="cerrar">
      <div class="modal">
        <h3>{{ form.id ? 'Editar alumno' : 'Nuevo alumno' }}</h3>
        <div class="grid grid-2">
          <div class="field">
            <label>Nombre *</label>
            <input v-model="form.nombre" placeholder="Juan" />
          </div>
          <div class="field">
            <label>Apellido *</label>
            <input v-model="form.apellido" placeholder="García" />
          </div>
        </div>
        <div class="grid grid-2">
          <div class="field">
            <label>Email</label>
            <input v-model="form.email" type="email" placeholder="juan@mail.com" />
          </div>
          <div class="field">
            <label>Teléfono</label>
            <input v-model="form.telefono" placeholder="+54 362 ..." />
          </div>
        </div>
        <div class="grid grid-2">
          <div class="field">
            <label>Fecha de nacimiento</label>
            <input v-model="form.fechaNacimiento" type="date" />
          </div>
          <div class="field">
            <label>Fecha de inicio</label>
            <input v-model="form.fechaAlta" type="date" />
          </div>
        </div>

        <div class="field">
          <label>Disciplinas</label>
          <div class="chip-group" style="margin-top:4px">
            <span
              v-for="d in disciplinas" :key="d.id"
              class="chip selectable"
              :class="{ selected: form.disciplinaIds.includes(d.id) }"
              @click="toggleDisciplina(d.id)"
            >{{ d.nombre }}</span>
          </div>
        </div>

        <div class="field" style="display:flex;align-items:center;gap:10px">
          <input type="checkbox" v-model="form.activo" id="chk-activo" style="width:auto" />
          <label for="chk-activo" style="margin:0;text-transform:none;font-size:14px;color:var(--text)">Alumno activo</label>
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
