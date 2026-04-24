<script setup>
import { ref, onMounted } from 'vue'
import http from '../api/http'
import { formatARS } from '../utils/format'

const disciplinas = ref([])
const loading = ref(true)

const formVacio = () => ({ id: null, nombre: '', descripcion: '', precio: '', activa: true })
const form = ref(formVacio())
const showModal = ref(false)
const saving = ref(false)
const errorMsg = ref('')

onMounted(async () => {
  await cargar()
  loading.value = false
})

async function cargar() {
  const { data } = await http.get('/disciplinas')
  disciplinas.value = data
}

function abrirNueva() {
  form.value = formVacio()
  errorMsg.value = ''
  showModal.value = true
}
function abrirEditar(d) {
  form.value = { ...d }
  errorMsg.value = ''
  showModal.value = true
}
function cerrar() { showModal.value = false }

async function guardar() {
  if (!form.value.nombre.trim()) { errorMsg.value = 'El nombre es requerido'; return }
  if (!form.value.precio || isNaN(Number(form.value.precio))) { errorMsg.value = 'El precio debe ser un número válido'; return }
  saving.value = true
  errorMsg.value = ''
  try {
    const payload = {
      nombre: form.value.nombre,
      descripcion: form.value.descripcion,
      precio: Number(form.value.precio),
      activa: form.value.activa
    }
    if (form.value.id) {
      await http.put(`/disciplinas/${form.value.id}`, payload)
    } else {
      await http.post('/disciplinas', payload)
    }
    await cargar()
    cerrar()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || 'Error al guardar'
  } finally {
    saving.value = false
  }
}

async function toggleActiva(d) {
  try {
    await http.put(`/disciplinas/${d.id}`, { ...d, activa: !d.activa })
    d.activa = !d.activa
  } catch { /* noop */ }
}
</script>

<template>
  <div>
    <h2>Disciplinas</h2>

    <div class="toolbar">
      <div class="spacer"></div>
      <button @click="abrirNueva">+ Nueva disciplina</button>
    </div>

    <div v-if="loading" class="empty">Cargando...</div>
    <table v-else>
      <thead>
        <tr>
          <th>Nombre</th>
          <th>Descripción</th>
          <th>Precio mensual</th>
          <th>Alumnos</th>
          <th>Estado</th>
          <th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in disciplinas" :key="d.id">
          <td><strong>{{ d.nombre }}</strong></td>
          <td style="color:var(--muted)">{{ d.descripcion || '—' }}</td>
          <td>{{ formatARS(d.precio) }}</td>
          <td>{{ d.alumnosCount ?? '—' }}</td>
          <td>
            <span class="badge" :class="d.activa ? 'pagada' : 'vencida'">
              {{ d.activa ? 'Activa' : 'Inactiva' }}
            </span>
          </td>
          <td>
            <div style="display:flex;gap:6px;flex-wrap:wrap">
              <button class="small secondary" @click="abrirEditar(d)">Editar</button>
              <button class="small" :class="d.activa ? 'danger' : 'success'" @click="toggleActiva(d)">
                {{ d.activa ? 'Desactivar' : 'Activar' }}
              </button>
            </div>
          </td>
        </tr>
        <tr v-if="disciplinas.length === 0">
          <td colspan="6" class="empty">Sin disciplinas</td>
        </tr>
      </tbody>
    </table>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="cerrar">
      <div class="modal">
        <h3>{{ form.id ? 'Editar disciplina' : 'Nueva disciplina' }}</h3>
        <div class="field">
          <label>Nombre *</label>
          <input v-model="form.nombre" placeholder="Musculación, CrossFit, Yoga…" />
        </div>
        <div class="field">
          <label>Descripción</label>
          <textarea v-model="form.descripcion" rows="2" placeholder="Descripción opcional…"></textarea>
        </div>
        <div class="field">
          <label>Precio mensual (ARS) *</label>
          <input v-model="form.precio" type="number" min="0" placeholder="18000" />
        </div>
        <div class="field" style="display:flex;align-items:center;gap:10px">
          <input type="checkbox" v-model="form.activa" id="chk-activa" style="width:auto" />
          <label for="chk-activa" style="margin:0;text-transform:none;font-size:14px;color:var(--text)">Disciplina activa</label>
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
