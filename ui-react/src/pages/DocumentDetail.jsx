
import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { api, formatDate } from '../api'
import {
  MAX_LENGTH,
  hasErrors,
  normalizeDocument,
  validateComment,
  validateDocument
} from '../validation'
import DocumentFields, { Field } from '../components/DocumentFields'
import { useConfirm } from '../hooks/useConfirm'

function toForm(doc) {
  return {
    title: doc.title,
    filename: doc.filename,
    contentType: doc.contentType ?? ''
  }
}

function DocumentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [confirm, confirmDialog] = useConfirm()
  const validId = /^\d+$/.test(id ?? '')

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

  const editErrors =
      editSubmitted && form ? validateDocument(form) : {}

  const commentErrors =
      commentSubmitted ? validateComment(commentText) : {}

  useEffect(() => {
    if (!validId) {
      return
    }

    let ignore = false

    async function load() {
      try {
        const [loadedDoc, loadedComments] = await Promise.all([
          api.getDocument(id),
          api.getComments(id)
        ])

        if (!ignore) {
          setDoc(loadedDoc)
          setForm(toForm(loadedDoc))
          setComments(loadedComments)
          setPageError('')
        }
      } catch (err) {
        if (!ignore) {
          setPageError(
              `Dokument konnte nicht geladen werden: ${err.message}`
          )
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

  // Die Zusammenfassung entsteht im Hintergrund (OCR + Gemini). Solange sie fehlt,
  // alle 5 Sekunden nachfragen, höchstens 2 Minuten lang. Nur "summary" wird übernommen,
  // damit ein gerade bearbeitetes Formular nicht überschrieben wird.
  const waitingForSummary = Boolean(doc) && !doc.summary

  useEffect(() => {
    if (!waitingForSummary) {
      return
    }

    let attempts = 0
    const timer = setInterval(() => {
      attempts += 1
      if (attempts > 24) {
        clearInterval(timer)
        return
      }
      api.getDocument(id)
          .then(fresh => {
            if (fresh.summary) {
              setDoc(previous => ({ ...previous, summary: fresh.summary }))
            }
          })
          .catch(() => {
            // einzelne fehlgeschlagene Abfrage ignorieren, beim nächsten Intervall erneut versuchen
          })
    }, 5000)

    return () => clearInterval(timer)
  }, [id, waitingForSummary])

  async function reloadComments() {
    try {
      setComments(await api.getComments(id))
    } catch (err) {
      setCommentMessage(
          `Kommentare konnten nicht geladen werden: ${err.message}`
      )
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
      const updated = await api.updateDocument(
          id,
          normalizeDocument(form)
      )

      setDoc(updated)
      setForm(toForm(updated))
      setEditSubmitted(false)
      setEditMessage({
        type: 'success',
        text: 'Änderungen gespeichert.'
      })
    } catch (err) {
      setEditMessage({
        type: 'error',
        text: `Änderungen konnten nicht gespeichert werden: ${err.message}`
      })
    }
  }

  async function handleDeleteDocument() {
    if (!(await confirm({
      title: 'Dokument löschen?',
      message: `„${doc.title}“ wird mit allen Kommentaren gelöscht. Das kann nicht rückgängig gemacht werden.`
    }))) {
      return
    }

    try {
      await api.deleteDocument(id)
      navigate('/')
    } catch (err) {
      setPageError(
          `Dokument konnte nicht gelöscht werden: ${err.message}`
      )
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
      setCommentMessage(
          `Kommentar konnte nicht gespeichert werden: ${err.message}`
      )
    }
  }

  async function handleDeleteComment(commentId) {
    if (!(await confirm({
      title: 'Kommentar löschen?',
      message: 'Der Kommentar wird endgültig entfernt.'
    }))) {
      return
    }

    try {
      await api.deleteComment(id, commentId)
      await reloadComments()
    } catch (err) {
      setCommentMessage(
          `Kommentar konnte nicht gelöscht werden: ${err.message}`
      )
    }
  }

  const pdfUrl = `/api/documents/${encodeURIComponent(id)}/file`

  return (
      <main className="page">
        <Link to="/" className="back-link">
          &larr; Zurück zum Dashboard
        </Link>

        {!validId && (
            <p className="message error" role="alert">
              Ungültige Dokument-ID.
            </p>
        )}

        {loading && <p>Dokument wird geladen...</p>}

        {pageError && (
            <p className="message error" role="alert">
              {pageError}
            </p>
        )}

        {doc && form && (
            <>
              <div className="detail-heading">
                <div>
                  <h1>{doc.title}</h1>
                  <p className="muted">{doc.filename}</p>
                </div>

                <button
                    type="button"
                    className="btn btn-danger"
                    onClick={handleDeleteDocument}
                >
                  Dokument löschen
                </button>
              </div>

              {/* OBEN: PDF LINKS, DETAILS RECHTS */}
              <div className="detail-workspace">
                <section className="detail-viewer">
                  <div className="detail-viewer-toolbar">
                    <span>Dokumentvorschau</span>

                    <a
                        href={pdfUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="detail-open-pdf"
                    >
                      PDF öffnen ↗
                    </a>
                  </div>

                  <iframe
                      key={id}
                      src={pdfUrl}
                      title={`PDF-Vorschau: ${doc.title}`}
                      className="detail-pdf-frame"
                  />
                </section>

                <aside className="detail-sidebar">
                  {/* ZUSAMMENFASSUNG (von Gemini, kommt nach OCR automatisch) */}
                  <section className="detail-panel">
                    <h2>Zusammenfassung</h2>
                    {doc.summary ? (
                        <p className="summary-text">{doc.summary}</p>
                    ) : (
                        <p className="muted">
                          Zusammenfassung wird erstellt … sie erscheint hier automatisch,
                          sobald sie fertig ist.
                        </p>
                    )}
                  </section>

                  {/* DOKUMENTDETAILS */}
                  <section className="detail-panel">
                    <h2>Dokumentdetails</h2>

                    <dl className="detail-info">
                      <dt>ID</dt>
                      <dd>{doc.id}</dd>

                      <dt>Dateiname</dt>
                      <dd>{doc.filename}</dd>

                      <dt>Dateityp</dt>
                      <dd>{doc.contentType || '–'}</dd>

                      <dt>Hochgeladen</dt>
                      <dd>{formatDate(doc.uploadedAt)}</dd>
                    </dl>
                  </section>

                  {/* DOKUMENT BEARBEITEN */}
                  <section className="detail-panel">
                    <h2>Dokument bearbeiten</h2>

                    <form
                        className="form"
                        onSubmit={handleUpdate}
                        noValidate
                    >
                      <DocumentFields
                          values={form}
                          errors={editErrors}
                          onChange={setForm}
                      />

                      <button type="submit" className="btn">
                        Änderungen speichern
                      </button>
                    </form>

                    {editMessage && (
                        <p
                            className={`message ${editMessage.type}`}
                            role="status"
                        >
                          {editMessage.text}
                        </p>
                    )}
                  </section>
                </aside>
              </div>

              {/* UNTEN: KOMMENTARE ÜBER DIE GESAMTE BREITE */}
              <section className="detail-panel detail-comments">
                <h2>Kommentare</h2>

                {comments.length === 0 ? (
                    <p className="muted">
                      Noch keine Kommentare vorhanden.
                    </p>
                ) : (
                    <ul className="comment-list">
                      {comments.map(comment => (
                          <li key={comment.id}>
                            <div>
                              <p>{comment.text}</p>
                              <small className="muted">
                                {formatDate(comment.createdAt)}
                              </small>
                            </div>

                            <button
                                type="button"
                                className="btn btn-danger btn-small"
                                onClick={() =>
                                    handleDeleteComment(comment.id)
                                }
                            >
                              Löschen
                            </button>
                          </li>
                      ))}
                    </ul>
                )}

                <form
                    className="form"
                    onSubmit={handleAddComment}
                    noValidate
                >
                  <Field
                      id="comment"
                      label="Neuer Kommentar *"
                      error={commentErrors.text}
                  >
                <textarea
                    id="comment"
                    placeholder="Neuen Kommentar schreiben..."
                    value={commentText}
                    maxLength={MAX_LENGTH}
                    onChange={event =>
                        setCommentText(event.target.value)
                    }
                    className={
                      commentErrors.text ? 'invalid' : undefined
                    }
                    aria-invalid={Boolean(commentErrors.text)}
                    aria-describedby="comment-error"
                />
                  </Field>

                  <button type="submit" className="btn">
                    Kommentar hinzufügen
                  </button>
                </form>

                {commentMessage && (
                    <p className="message error" role="alert">
                      {commentMessage}
                    </p>
                )}
              </section>
            </>
        )}

        {confirmDialog}
      </main>
  )
}

export default DocumentDetail
