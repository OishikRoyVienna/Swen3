import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { api, formatDate } from '../api'
import { MAX_LENGTH, hasErrors, normalizeDocument, validateComment, validateDocument } from '../validation'
import DocumentFields, { Field } from '../components/DocumentFields'

function toForm(doc) {
  return { title: doc.title, filename: doc.filename, contentType: doc.contentType ?? '' }
}

function DocumentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const validId = /^\d+$/.test(id)

  const [doc, setDoc] = useState(null)
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(validId)
  const [pageError, setPageError] = useState('')

  const [form, setForm] = useState(null)
  const [editSubmitted, setEditSubmitted] = useState(false)
  const [editMessage, setEditMessage] = useState(null)

  const [commentText, setCommentText] = useState('')
  const [commentSubmitted, setCommentSubmitted] = useState(false)
  const [commentMessage, setCommentMessage] = useState('')

  const editErrors = editSubmitted && form ? validateDocument(form) : {}
  const commentErrors = commentSubmitted ? validateComment(commentText) : {}

  useEffect(() => {
    if (!validId) {
      return
    }
    let ignore = false

    async function load() {
      try {
        const [loadedDoc, loadedComments] = await Promise.all([api.getDocument(id), api.getComments(id)])
        if (!ignore) {
          setDoc(loadedDoc)
          setForm(toForm(loadedDoc))
          setComments(loadedComments)
        }
      } catch (err) {
        if (!ignore) {
          setPageError(`Dokument konnte nicht geladen werden: ${err.message}`)
        }
      } finally {
        if (!ignore) {
          setLoading(false)
        }
      }
    }

    load()
    return () => {
      ignore = true
    }
  }, [id, validId])

  async function reloadComments() {
    try {
      setComments(await api.getComments(id))
    } catch (err) {
      setCommentMessage(`Kommentare konnten nicht geladen werden: ${err.message}`)
    }
  }

  async function handleUpdate(event) {
    event.preventDefault()
    setEditSubmitted(true)
    setEditMessage(null)

    if (hasErrors(validateDocument(form))) {
      return
    }

    try {
      const updated = await api.updateDocument(id, normalizeDocument(form))
      setDoc(updated)
      setForm(toForm(updated))
      setEditSubmitted(false)
      setEditMessage({ type: 'success', text: 'Änderungen gespeichert.' })
    } catch (err) {
      setEditMessage({ type: 'error', text: `Änderungen konnten nicht gespeichert werden: ${err.message}` })
    }
  }

  async function handleDeleteDocument() {
    if (!window.confirm(`Möchtest du "${doc.title}" inklusive Kommentaren wirklich löschen?`)) {
      return
    }
    try {
      await api.deleteDocument(id)
      navigate('/')
    } catch (err) {
      setPageError(`Dokument konnte nicht gelöscht werden: ${err.message}`)
    }
  }

  async function handleAddComment(event) {
    event.preventDefault()
    setCommentSubmitted(true)
    setCommentMessage('')

    if (hasErrors(validateComment(commentText))) {
      return
    }

    try {
      await api.addComment(id, commentText.trim())
      setCommentText('')
      setCommentSubmitted(false)
      await reloadComments()
    } catch (err) {
      setCommentMessage(`Kommentar konnte nicht gespeichert werden: ${err.message}`)
    }
  }

  async function handleDeleteComment(commentId) {
    if (!window.confirm('Diesen Kommentar löschen?')) {
      return
    }
    try {
      await api.deleteComment(id, commentId)
      await reloadComments()
    } catch (err) {
      setCommentMessage(`Kommentar konnte nicht gelöscht werden: ${err.message}`)
    }
  }

  return (
    <main className="page">
      <Link to="/" className="back-link">&larr; Zurück zum Dashboard</Link>

      {!validId && <p className="message error" role="alert">Ungültige Dokument-ID.</p>}
      {loading && <p>Dokument wird geladen...</p>}
      {pageError && <p className="message error" role="alert">{pageError}</p>}

      {doc && (
        <>
          <section className="card">
            <div className="section-header">
              <h1>{doc.title}</h1>
              <button className="btn btn-danger" onClick={handleDeleteDocument}>Dokument löschen</button>
            </div>
            <dl className="meta">
              <dt>ID</dt><dd>{doc.id}</dd>
              <dt>Dateiname</dt><dd>{doc.filename}</dd>
              <dt>Dateityp</dt><dd>{doc.contentType || '–'}</dd>
              <dt>Hochgeladen</dt><dd>{formatDate(doc.uploadedAt)}</dd>
            </dl>
          </section>

          <section className="card">
            <h2>Dokument bearbeiten</h2>
            <form className="form" onSubmit={handleUpdate} noValidate>
              <DocumentFields values={form} errors={editErrors} onChange={setForm} />
              <button type="submit" className="btn">Änderungen speichern</button>
            </form>
            {editMessage && <p className={`message ${editMessage.type}`} role="status">{editMessage.text}</p>}
          </section>

          <section className="card">
            <h2>Kommentare</h2>
            {comments.length === 0 ? (
              <p className="muted">Noch keine Kommentare vorhanden.</p>
            ) : (
              <ul className="comment-list">
                {comments.map(comment => (
                  <li key={comment.id}>
                    <div>
                      <p>{comment.text}</p>
                      <small className="muted">{formatDate(comment.createdAt)}</small>
                    </div>
                    <button className="btn btn-danger btn-small" onClick={() => handleDeleteComment(comment.id)}>
                      Löschen
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <form className="form" onSubmit={handleAddComment} noValidate>
              <Field id="comment" label="Neuer Kommentar *" error={commentErrors.text}>
                <textarea
                  id="comment"
                  placeholder="Neuen Kommentar schreiben..."
                  value={commentText}
                  maxLength={MAX_LENGTH}
                  onChange={event => setCommentText(event.target.value)}
                  className={commentErrors.text ? 'invalid' : undefined}
                  aria-invalid={Boolean(commentErrors.text)}
                  aria-describedby="comment-error"
                />
              </Field>
              <button type="submit" className="btn">Kommentar hinzufügen</button>
            </form>
            {commentMessage && <p className="message error" role="alert">{commentMessage}</p>}
          </section>
        </>
      )}
    </main>
  )
}

export default DocumentDetail
