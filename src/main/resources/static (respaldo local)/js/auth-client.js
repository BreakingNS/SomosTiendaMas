/**
 * fetchWithAuth: wrapper para fetch que intenta refresh automático en 401.
 * - Usa cookies (credentials: 'include') para enviar access/refresh.
 * - Reintenta la petición original una vez si el refresh fue exitoso.
 */

const AuthClient = (function () {

    async function refreshTokens() {
        try {
            const res = await fetch('/api/auth/public/refresh-token', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                // body no necesario si refresh token está en cookie
            });
            return res.ok;
        } catch (e) {
            console.error('[AuthClient] refreshTokens error', e);
            return false;
        }
    }

    async function fetchWithAuth(input, init = {}) {
        init = Object.assign({}, init);
        init.credentials = 'include'; // IMPORTANTE: enviar cookies
        init.headers = Object.assign({}, init.headers || {}, { 'Accept': 'application/json' });

        let res = await fetch(input, init);

        // Si no es 401, devolver tal cual
        if (res.status !== 401) return res;

        // Si es 401, intentar refresh
        const refreshed = await refreshTokens();
        if (!refreshed) {
            // refresh falló -> NO redirigir automáticamente en páginas públicas
            console.warn('[AuthClient] refresh falló');

            // si la ruta actual es claramente privada, redirigir; si no, devolver res y dejar que el caller maneje
            if (isClearlyPrivatePath()) {
                window.location.href = '/public/iniciarSesion.html';
                return res;
            }

            // devolver la respuesta 401 al caller para que decida (sin navegación forzada)
            return res;
        }

        // Reintentar la petición original (una sola vez)
        res = await fetch(input, init);
        return res;
    }

    // Mejor: detectar rutas públicas vía whitelist y tratar el resto como privadas
    function isClearlyPrivatePath() {
        try {
            const path = window.location.pathname || '';
            // Tratar explícitamente la raíz e index como públicas
            if (path === '/' || path === '/index.html') return false;

            // Rutas consideradas públicas (editar según tu app)
            const publicPrefixes = ['/public/', '/verificar-email', '/correoVerificado', '/restaurarContraseña', '/static/'];
            for (const p of publicPrefixes) {
                if (path.startsWith(p) || path.includes(p)) return false;
            }

            // Si cae aquí, asumir privada
            return true;
        } catch (e) {
            return true; // por seguridad, asumir privada si hay error en la comprobación
        }
    }

    function isPrivateRoute() {
        // heurística simple: rutas bajo /private o páginas específicas
        const path = window.location.pathname;
        return path.startsWith('/private') || path.includes('cambiarContrasenia') || path.includes('perfil');
    }

    // Proactive refresh: llamar periódicamente al endpoint de refresh para mantener sesión viva
    // Intervalo por defecto 10 minutos. Ajustar según expiración access token.
    function startProactiveRefresh(intervalMinutes = 10) {
        if (window.__authClientProactiveInterval) return;
        const ms = Math.max(1, intervalMinutes) * 60 * 1000;
        window.__authClientProactiveInterval = setInterval(async () => {
            const ok = await refreshTokens();
            if (!ok) {
                console.warn('[AuthClient] proactive refresh falló');
                clearInterval(window.__authClientProactiveInterval);
                window.__authClientProactiveInterval = null;
            } else {
                console.debug('[AuthClient] proactive refresh ok');
            }
        }, ms);
    }

    function stopProactiveRefresh() {
        if (window.__authClientProactiveInterval) {
            clearInterval(window.__authClientProactiveInterval);
            window.__authClientProactiveInterval = null;
        }
    }

    return {
        fetchWithAuth,
        refreshTokens,
        startProactiveRefresh,
        stopProactiveRefresh
    };
})();