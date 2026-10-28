// REST-Client für das Spring-Boot-Backend.
// Relative URLs: im Container leitet nginx /api an das Backend weiter, im Dev-Modus der Vite-Proxy.
async function request(path, options = {}) {
  const method = options.method ?? 'GET'
  let response
  try {
    response = await fetch(`/api${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    })
  } catch (err) {
    console.error(`[api] ${method} /api${path}: Server nicht erreichbar`, err)
    throw new Error('Server nicht erreichbar. Bitte später erneut versuchen.', { cause: err })
  }

  if (!response.ok) {
    let message = `${response.status} ${response.statusText}`
    try {
      const body = await response.json()
      if (body?.message) {
        message = body.message
      }
    } catch {
      // Antwort enthielt kein JSON
    }
    // 5xx = Fehler im Server, 4xx = ungültige Anfrage
    const logLevel = response.status >= 500 ? 'error' : 'warn'
    console[logLevel](`[api] ${method} /api${path} -> ${response.status}: ${message}`)
    throw new Error(message)
  }

  return response.status === 204 ? null : response.json()
}

export const api = {
  getDocuments: () => request('/documents'),
  getDocument: id => request(`/documents/${id}`),
  createDocument: doc => request('/documents', { method: 'POST', body: JSON.stringify(doc) }),
  updateDocument: (id, doc) => request(`/documents/${id}`, { method: 'PUT', body: JSON.stringify(doc) }),
  deleteDocument: id => request(`/documents/${id}`, { method: 'DELETE' }),

  getComments: documentId => request(`/documents/${documentId}/comments`),
  addComment: (documentId, text) =>
    request(`/documents/${documentId}/comments`, { method: 'POST', body: JSON.stringify({ text }) }),
  deleteComment: (documentId, commentId) =>
    request(`/documents/${documentId}/comments/${commentId}`, { method: 'DELETE' })
}

export function formatDate(value) {
  if (!value) {
    return '–'
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('de-AT')
}
