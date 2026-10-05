const form = document.getElementById('document-form');
const formMessage = document.getElementById('form-message');
const listMessage = document.getElementById('list-message');
const rows = document.getElementById('document-rows');
const emptyState = document.getElementById('empty-state');
const search = document.getElementById('search');

let documents = [];

function renderDocuments() {
    const filter = search.value.trim().toLowerCase();
    const visible = documents.filter((doc) =>
        !filter
        || doc.title.toLowerCase().includes(filter)
        || doc.filename.toLowerCase().includes(filter));

    rows.innerHTML = visible.map((doc) => `
        <tr>
            <td><a href="document.html?id=${doc.id}">${escapeHtml(doc.title)}</a></td>
            <td>${escapeHtml(doc.filename)}</td>
            <td>${escapeHtml(doc.contentType || '–')}</td>
            <td>${formatDate(doc.uploadedAt)}</td>
            <td>${doc.comments ? doc.comments.length : 0}</td>
            <td class="actions">
                <a class="btn" href="document.html?id=${doc.id}">Details</a>
                <button class="btn danger" data-delete="${doc.id}">Delete</button>
            </td>
        </tr>`).join('');

    emptyState.hidden = visible.length > 0;
    emptyState.textContent = documents.length === 0 ? 'No documents yet.' : 'No documents match the filter.';
}

async function loadDocuments() {
    try {
        documents = await api.getDocuments();
        showMessage(listMessage, '');
        renderDocuments();
    } catch (err) {
        showMessage(listMessage, `Could not load documents: ${err.message}`);
    }
}

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const doc = readDocumentForm(form);
    if (!showFieldErrors(form, validateDocument(doc))) {
        showMessage(formMessage, '');
        return;
    }

    try {
        await api.createDocument(doc);
        form.reset();
        showMessage(formMessage, 'Document saved.', 'success');
        await loadDocuments();
    } catch (err) {
        showMessage(formMessage, `Could not save document: ${err.message}`);
    }
});

// Re-validate a field as soon as the user edits it after a failed submit
form.addEventListener('input', (event) => {
    if (event.target.classList.contains('invalid')) {
        showFieldErrors(form, validateDocument(readDocumentForm(form)));
    }
});

rows.addEventListener('click', async (event) => {
    const id = event.target.dataset.delete;
    if (!id || !confirm('Delete this document and all its comments?')) {
        return;
    }
    try {
        await api.deleteDocument(id);
        await loadDocuments();
    } catch (err) {
        showMessage(listMessage, `Could not delete document: ${err.message}`);
    }
});

search.addEventListener('input', renderDocuments);

loadDocuments();
