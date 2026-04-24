export function formatARS(value) {
  if (value === null || value === undefined) return '-'
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 2 }).format(Number(value))
}

export function formatFecha(fecha) {
  if (!fecha) return '-'
  return new Date(fecha).toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

export const MESES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']

export function nombreMes(m) { return MESES[m - 1] || '' }

export function formatNum(v, decimals = 1) {
  if (v === null || v === undefined) return '-'
  return Number(v).toFixed(decimals)
}
