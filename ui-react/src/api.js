
// REST-Client für das Spring-Boot-Backend.
// Relative URLs: im Container leitet nginx /api an das Backend weiter,
// im Dev-Modus der Vite-Proxy.

async function request(path, options = {}) {
    const method = options.method ?? 'GET'
    let response

    try {
        response = await fetch(`/api${path}`, {
            ...options,
            headers: options.body instanceof FormData
                ? options.headers
                : { 'Content-Type': 'application/json', ...options.headers }
        })
    } catch (err) {
        console.error(
            `[api] ${method} /api${path}: Server nicht erreichbar`,
            err
        )
        throw new Error(
            'Server nicht erreichbar. Bitte später erneut versuchen.',
            { cause: err }
        )
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

        const logLevel = response.status >= 500 ? 'error' : 'warn'

        console[logLevel](
            `[api] ${method} /api${path} -> ${response.status}: ${message}`
        )

        throw new Error(message)
    }

    if (response.status === 204) {
        return null
    }

    const contentType = response.headers.get('content-type') || ''

    return contentType.includes('application/json')
        ? response.json()
        : response.text()
}

export const api = {
    // Alle Dokumente laden
    getDocuments: () => request('/documents'),

    // PDF und Titel gemeinsam hochladen
    uploadDocument: (file, title) => {
        const formData = new FormData()

        formData.append('file', file)
        formData.append('title', title)

        return request('/documents/upload', {
            method: 'POST',
            body: formData
        })
    },

    // Volltextsuche über Elasticsearch
    searchDocuments: query =>
        request(`/documents/search?q=${encodeURIComponent(query)}`),

    // Einzelnes Dokument laden
    getDocument: id =>
        request(`/documents/${id}`),

    // Dokument aktualisieren
    updateDocument: (id, doc) =>
        request(`/documents/${id}`, {
            method: 'PUT',
            body: JSON.stringify(doc)
        }),

    // Dokument löschen
    deleteDocument: id =>
        request(`/documents/${id}`, {
            method: 'DELETE'
        }),

    // Kommentare laden
    getComments: documentId =>
        request(`/documents/${documentId}/comments`),

    // Kommentar hinzufügen
    addComment: (documentId, text) =>
        request(`/documents/${documentId}/comments`, {
            method: 'POST',
            body: JSON.stringify({ text })
        }),

    // Kommentar löschen
    deleteComment: (documentId, commentId) =>
        request(`/documents/${documentId}/comments/${commentId}`, {
            method: 'DELETE'
        })
}

// Datum formatieren
export function formatDate(value) {
    if (!value) {
        return '–'
    }

    const date = new Date(value)

    return Number.isNaN(date.getTime())
        ? value
        : date.toLocaleString('de-AT')
}
