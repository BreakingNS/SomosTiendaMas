(function () {
  const provinciaSel = document.getElementById('provinciaSelect');
  const departamentoSel = document.getElementById('departamentoSelect');
  const municipioSel = document.getElementById('municipioSelect');
  const localidadSel = document.getElementById('localidadSelect');

  const API_BASE = '/api/import-ubicaciones';

  function reset(select, placeholder = 'Seleccione...') {
    if (!select) return;
    select.innerHTML = `<option value="">${placeholder}</option>`;
    select.disabled = true;
  }

  function populate(select, items, textField = 'nombre', valueField = 'id') {
    if (!select) return;
    reset(select);
    if (!Array.isArray(items) || items.length === 0) return;
    items.forEach(it => {
      const opt = document.createElement('option');
      opt.value = it[valueField];
      opt.textContent = it[textField] ?? it.nombre ?? it.id;
      select.appendChild(opt);
    });
    select.disabled = false;
  }

  async function fetchJson(url) {
    const res = await fetch(url, { method: 'GET', credentials: 'include', headers: { 'Accept': 'application/json' }});
    if (!res.ok) throw new Error('HTTP ' + res.status);
    return res.json();
  }

  async function loadProvincias() {
    try {
      const data = await fetchJson(`${API_BASE}/provincias`);
      populate(provinciaSel, data);
      // Si hay valor preseleccionado en el select (ej. edición), disparar carga siguiente
      if (provinciaSel.value) provinciaSel.dispatchEvent(new Event('change'));
    } catch (e) { console.error('Error cargando provincias', e); }
  }

  async function loadDepartamentos(provinciaId) {
    try {
      const data = await fetchJson(`${API_BASE}/departamentos?provinciaId=${encodeURIComponent(provinciaId)}`);
      populate(departamentoSel, data);
      if (departamentoSel.value) departamentoSel.dispatchEvent(new Event('change'));
    } catch (e) { console.error('Error cargando departamentos', e); }
  }

  async function loadMunicipios(departamentoId) {
    try {
      const data = await fetchJson(`${API_BASE}/municipios?departamentoId=${encodeURIComponent(departamentoId)}`);
      populate(municipioSel, data);
      if (municipioSel.value) municipioSel.dispatchEvent(new Event('change'));
    } catch (e) { console.error('Error cargando municipios', e); }
  }

  async function loadLocalidades(municipioId) {
    try {
      const data = await fetchJson(`${API_BASE}/localidades?municipioId=${encodeURIComponent(municipioId)}`);
      populate(localidadSel, data);
    } catch (e) { console.error('Error cargando localidades', e); }
  }

  // Listeners encadenados
  if (provinciaSel) {
    provinciaSel.addEventListener('change', function () {
      reset(departamentoSel);
      reset(municipioSel);
      reset(localidadSel);
      const id = this.value;
      if (id) loadDepartamentos(id);
    });
  }

  if (departamentoSel) {
    departamentoSel.addEventListener('change', function () {
      reset(municipioSel);
      reset(localidadSel);
      const id = this.value;
      if (id) loadMunicipios(id);
    });
  }

  if (municipioSel) {
    municipioSel.addEventListener('change', function () {
      reset(localidadSel);
      const id = this.value;
      if (id) loadLocalidades(id);
    });
  }

  // Inicializar al cargar el DOM
  document.addEventListener('DOMContentLoaded', function () {
    reset(provinciaSel);
    reset(departamentoSel);
    reset(municipioSel);
    reset(localidadSel);
    loadProvincias();
  });
})();