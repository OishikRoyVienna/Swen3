// Shared REST client and helpers for the Paperless UI.
const API_BASE = '/api/documents';

async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: { 'Content-Type': 'application/json' },
        ...options
    });

    if (!response.ok) {
        let message = `${response.status} ${response.statusText}`;
        try {
            const body = await response.json();
            if (body && body.message) {
                message = body.message;
            }
        } catch (ignored) {
            // response had no JSON body
        }
        throw new Error(message);
    }

    return response.status === 204 ? null : response.json();
}

const api = {
    getDocuments: () => request(API_BASE),
    getDocument: (id) => request(`${API_BASE}/${id}`),
    createDocument: (doc) => request(API_BASE, { method: 'POST', body: JSON.stringify(doc) }),
    updateDocument: (id, doc) => request(`${API_BASE}/${id}`, { method: 'PUT', body: JSON.stringify(doc) }),
    deleteDocument: (id) => request(`${API_BASE}/${id}`, { method: 'DELETE' }),

    getComments: (documentId) => request(`${API_BASE}/${documentId}/comments`),
    addComment: (documentId, text) =>
        request(`${API_BASE}/${documentId}/comments`, { method: 'POST', body: JSON.stringify({ text }) }),
    deleteComment: (documentId, commentId) =>
        request(`${API_BASE}/${documentId}/comments/${commentId}`, { method: 'DELETE' })
};

// ---------- Validation ----------

const MAX_LENGTH = 255;
const FILENAME_PATTERN = /^[^\\/:*?"<>|]+\.[A-Za-z0-9]{1,10}$/;
const CONTENT_TYPE_PATTERN = /^[\w.+-]+\/[\w.+-]+$/;

function validateDocument(doc) {
    const errors = {};

    if (!doc.title) {
        errors.title = 'Title is required.';
    } else if (doc.title.length > MAX_LENGTH) {
        errors.title = `Title must be at most ${MAX_LENGTH} characters.`;
    }

    if (!doc.filename) {
        errors.filename = 'Filename is required.';
    } else if (doc.filename.length > MAX_LENGTH) {
        errors.filename = `Filename must be at most ${MAX_LENGTH} characters.`;
    } else if (!FILENAME_PATTERN.test(doc.filename)) {
        errors.filename = 'Filename must have an extension (e.g. report.pdf) and no \\ / : * ? " < > | characters.';
    }

    if (doc.contentType) {
        if (doc.contentType.length > MAX_LENGTH) {
            errors.contentType = `Content type must be at most ${MAX_LENGTH} characters.`;
        } else if (!CONTENT_TYPE_PATTERN.test(doc.contentType)) {
            errors.contentType = 'Content type must look like type/subtype (e.g. application/pdf).';
        }
    }

    return errors;
}

function validateComment(text) {
    const errors = {};
    if (!text) {
        errors.text = 'Comment must not be empty.';
    } else if (text.length > MAX_LENGTH) {
        errors.text = `Comment must be at most ${MAX_LENGTH} characters.`;
    }
    return errors;
}

// Shows field errors next to inputs (elements with id "<field>-error") and marks inputs invalid.
function showFieldErrors(form, errors) {
    form.querySelectorAll('[data-field]').forEach((input) => {
        const field = input.dataset.field;
        const errorEl = form.querySelector(`#${input.id}-error`);
        const message = errors[field] || '';
        input.classList.toggle('invalid', Boolean(message));
        input.setAttribute('aria-invalid', message ? 'true' : 'false');
        if (errorEl) {
            errorEl.textContent = message;
        }
    });
    return Object.keys(errors).length === 0;
}

// ---------- UI helpers ----------

function showMessage(el, text, type = 'error') {
    el.textContent = text;
    el.className = `message ${type}`;
    el.hidden = !text;
}

function formatDate(value) {
    if (!value) {
        return '–';
    }
    const date = new Date(value);
    return isNaN(date) ? value : date.toLocaleString();
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function readDocumentForm(form) {
    return {
        title: form.elements.title.value.trim(),
        filename: form.elements.filename.value.trim(),
        contentType: form.elements.contentType.value.trim() || null
    };
}
