(function(){
    const API = '/api/sesiones/private/datos-sesion';
    function formatISO(iso) {
        if (!iso) return '---';
        try { const d = new Date(iso); return isNaN(d) ? iso : d.toLocaleString(); } catch(e){ return iso; }
    }
    function updateUI(data) {
        const active = data && data.sesionActiva === true;
        const dot = document.getElementById('ss-dot');
        if (dot) {
            dot.className = active ? 'status-dot dot-green' : 'status-dot dot-red';
        }
        const set = (id, v) => { const el = document.getElementById(id); if (el) el.textContent = v ?? '---'; };
        set('ss-usuario', data?.username ?? '---');
        set('ss-rol', data?.rol ?? '---');
        set('ss-jwt', formatISO(data?.jwtExpiracion));
        set('ss-refresh', formatISO(data?.refreshExpiracion));
        set('ss-last', new Date().toLocaleString());
        const sesionLabel = document.getElementById('ss-sesion-label');
        if (sesionLabel) sesionLabel.textContent = active ? 'Activa' : 'Inactiva';
    }
    async function fetchSession() {
        try {
            const res = await (typeof AuthClient !== 'undefined'
                ? AuthClient.fetchWithAuth(API, { method:'GET' })
                : fetch(API, { method:'GET', credentials:'include', headers:{ 'Accept':'application/json' } }));
            if (!res.ok) { updateUI(null); return; }
            const json = await res.json();
            updateUI(json);
        } catch (e) { updateUI(null); }
    }
    // Logout: llama al endpoint protegido y actualiza UI / redirige al login
    async function doLogout() {
        try {
            const res = await (typeof AuthClient !== 'undefined'
                ? AuthClient.fetchWithAuth('/api/auth/private/logout', { method: 'POST' })
                : fetch('/api/auth/private/logout', { method:'POST', credentials:'include' }));

            // En cualquier caso, limpiar estado cliente y llevar al login
            updateUI(null);
            // si la respuesta es ok, redirigir; si no, también forzar login
            setTimeout(() => { window.location.href = '/public/iniciarSesion.html'; }, 300);
        } catch (e) {
            console.error('Logout error', e);
            updateUI(null);
            setTimeout(() => { window.location.href = '/public/iniciarSesion.html'; }, 300);
        }
    }

    // Enlazar botón logout al DOMContentLoaded
     document.addEventListener('DOMContentLoaded', function(){
         fetchSession();
         setInterval(fetchSession, 60000);
         // Iniciar refresco proactivo cada 10 minutos si AuthClient está disponible
         if (typeof AuthClient !== 'undefined' && AuthClient.startProactiveRefresh) {
             AuthClient.startProactiveRefresh(10);
         }

        const logoutBtn = document.getElementById('ss-logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', function () {
                // opcional: confirmación corta
                if (confirm('¿Deseás cerrar la sesión?')) doLogout();
            });
        }
     });
})();