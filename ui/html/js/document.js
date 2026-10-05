const documentId = new URLSearchParams(window.location.search).get('id');

const pageMessage = document.getElementById('page-message');
const content = document.getElementById('content');
const docForm = document.getElementById('document-form');
const formMessage = document.getElementById('form-message');
const commentForm = document.getElementById('comment-form');
const commentMessage = document.getElementById('comment-message');
const commentList = document.getElementById('comment-list');
const noComments = document.getElementById('no-comments');

function renderDocument(doc) {
    document.title = `Paperless – ${doc.title}`;
    document.getElementById('doc-title').textContent = doc.title;
    document.getElementById('doc-id').textContent = doc.id;
    document.getElementById('doc-filename').textContent = doc.filename;
    document.getElementById('doc-contentType').textContent = doc.contentType || '–';
    document.getElementById('doc-uploadedAt').textContent = formatDate(doc.uploadedAt);

    docForm.elements.title.value = doc.title;
    docForm.elements.filename.value = doc.filename;
    docForm.elements.contentType.value = doc.contentType || '';
}

function renderComments(comments) {
    commentList.innerHTML = comments.map((comment) => `
        <li>
            <div>
                <p>${escapeHtml(comment.text)}</p>
                <small class="muted">${formatDate(comment.createdAt)}</small>
            </div>
            <button class="btn danger small" data-delete="${comment.id}">Delete</button>
        </li>`).join('');
    noComments.hidden = comments.length > 0;
}

async function loadComments() {
    try {
        renderComments(await api.getComments(documentId));
    } catch (err) {
        showMessage(commentMessage, `Could not load comments: ${err.message}`);
    }
}

async function loadDocument() {
    if (!/^\d+$/.test(documentId || '')) {
        showMessage(pageMessage, 'Invalid or missing document id.');
        return;
    }
    try {
        renderDocument(await api.getDocument(documentId));
        content.hidden = false;
        await loadComments();
    } catch (err) {
        showMessage(pageMessage, `Could not load document: ${err.message}`);
    }
}

docForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const doc = readDocumentForm(docForm);
    if (!showFieldErrors(docForm, validateDocument(doc))) {
        showMessage(formMessage, '');
        return;
    }
    try {
        renderDocument(await api.updateDocument(documentId, doc));
        showMessage(formMessage, 'Changes saved.', 'success');
    } catch (err) {
        showMessage(formMessage, `Could not save changes: ${err.message}`);
    }
});

docForm.addEventListener('input', (event) => {
    if (event.target.classList.contains('invalid')) {
        showFieldErrors(docForm, validateDocument(readDocumentForm(docForm)));
    }
});

document.getElementById('delete-document').addEventListener('click', async () => {
    if (!confirm('Delete this document and all its comments?')) {
        return;
    }
    try {
        await api.deleteDocument(documentId);
        window.location.href = 'index.html';
    } catch (err) {
        showMessage(pageMessage, `Could not delete document: ${err.message}`);
    }
});

commentForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const text = commentForm.elements.text.value.trim();
    if (!showFieldErrors(commentForm, validateComment(text))) {
        showMessage(commentMessage, '');
        return;
    }
    try {
        await api.addComment(documentId, text);
        commentForm.reset();
        showMessage(commentMessage, '');
        await loadComments();
    } catch (err) {
        showMessage(commentMessage, `Could not add comment: ${err.message}`);
    }
});

commentForm.addEventListener('input', (event) => {
    if (event.target.classList.contains('invalid')) {
        showFieldErrors(commentForm, validateComment(commentForm.elements.text.value.trim()));
    }
});

commentList.addEventListener('click', async (event) => {
    const commentId = event.target.dataset.delete;
    if (!commentId || !confirm('Delete this comment?')) {
        return;
    }
    try {
        await api.deleteComment(documentId, commentId);
        await loadComments();
    } catch (err) {
        showMessage(commentMessage, `Could not delete comment: ${err.message}`);
    }
});

loadDocument();
