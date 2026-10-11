
import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [documents, setDocuments] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [title, setTitle] = useState('')
  const [filename, setFilename] = useState('')
  const [contentType, setContentType] = useState('application/pdf')
  const [message, setMessage] = useState('')
  const [saving, setSaving] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [commentTexts, setCommentTexts] = useState({})
  const [commentSavingId, setCommentSavingId] = useState(null)
  const [editTitle, setEditTitle] = useState('')

  async function loadDocuments() {
    try {
      const response = await fetch('/api/documents')

      if (!response.ok) {
        throw new Error('Dokumente konnten nicht geladen werden')
      }

      const data = await response.json()
      setDocuments(data)
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDocuments()
  }, [])

  async function handleCreate(event) {
    event.preventDefault()
    setMessage('')
    setSaving(true)

    try {
      const response = await fetch('/api/documents', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          title: title.trim(),
          filename: filename.trim(),
          contentType
        })
      })

      if (!response.ok) {
        throw new Error('Dokument konnte nicht erstellt werden')
      }

      setTitle('')
      setFilename('')
      setContentType('application/pdf')
      setMessage('Dokument erfolgreich erstellt!')
      await loadDocuments()
    } catch (err) {
      setMessage(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleUpdate(id) {
    const document = documents.find(doc => doc.id === id)

    if (!document || !editTitle.trim()) {
      return
    }

    try {
      const response = await fetch(`/api/documents/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          title: editTitle.trim(),
          filename: document.filename,
          contentType: document.contentType
        })
      })

      if (!response.ok) {
        throw new Error('Dokument konnte nicht bearbeitet werden')
      }

      setEditingId(null)
      setEditTitle('')
      await loadDocuments()
    } catch (err) {
      alert(err.message)
    }
  }

  async function handleDelete(id, documentTitle) {
    const confirmed = window.confirm(
        `Möchtest du "${documentTitle}" wirklich löschen?`
    )

    if (!confirmed) {
      return
    }

    try {
      const response = await fetch(`/api/documents/${id}`, {
        method: 'DELETE'
      })

      if (!response.ok) {
        throw new Error('Dokument konnte nicht gelöscht werden')
      }

      await loadDocuments()
    } catch (err) {
      alert(err.message)
    }
  }

  async function handleAddComment(documentId) {
    const text = (commentTexts[documentId] ?? '').trim()

    if (!text) {
      return
    }

    setCommentSavingId(documentId)

    try {
      const response = await fetch(
          `/api/documents/${documentId}/comments`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({ text })
          }
      )

      if (!response.ok) {
        throw new Error('Kommentar konnte nicht gespeichert werden')
      }

      setCommentTexts(previous => ({
        ...previous,
        [documentId]: ''
      }))

      await loadDocuments()
    } catch (err) {
      alert(err.message)
    } finally {
      setCommentSavingId(null)
    }
  }

  const filteredDocuments = documents.filter(doc =>
      doc.title.toLowerCase().includes(search.toLowerCase())
  )

  return (
      <main className="dashboard">
        <h1>Paperless Dashboard</h1>
        <p className="subtitle">
          SWEN3 – Dokumentenverwaltung mit React
        </p>

        <section className="create-form">
          <h2>Neues Dokument erstellen</h2>

          <form onSubmit={handleCreate}>
            <label htmlFor="title">Titel</label>
            <input
                id="title"
                value={title}
                onChange={event => setTitle(event.target.value)}
                maxLength={255}
                required
                placeholder="z. B. Semesterprojekt"
            />

            <label htmlFor="filename">Dateiname</label>
            <input
                id="filename"
                value={filename}
                onChange={event => setFilename(event.target.value)}
                maxLength={255}
                required
                placeholder="z. B. semesterprojekt.pdf"
            />

            <label htmlFor="contentType">Dateityp</label>
            <select
                id="contentType"
                value={contentType}
                onChange={event => setContentType(event.target.value)}
            >
              <option value="application/pdf">PDF</option>
              <option value="image/png">PNG</option>
              <option value="image/jpeg">JPEG</option>
              <option value="text/plain">Text</option>
            </select>

            <button type="submit" disabled={saving}>
              {saving ? 'Speichern...' : 'Dokument erstellen'}
            </button>
          </form>

          {message && <p role="status">{message}</p>}
        </section>

        <div className="search">
          <input
              type="text"
              placeholder="Dokumente suchen..."
              value={search}
              onChange={event => setSearch(event.target.value)}
          />
        </div>

        <h2>Meine Dokumente</h2>

        {loading && <p>Dokumente werden geladen...</p>}
        {error && <p role="alert">{error}</p>}

        {!loading && !error && (
            <div className="documents">
              {filteredDocuments.length === 0 ? (
                  <p>Keine Dokumente gefunden.</p>
              ) : (
                  filteredDocuments.map(doc => (
                      <div className="document-card" key={doc.id}>
                        <h3>{doc.title}</h3>
                        <p>Dateiname: {doc.filename}</p>
                        <p>Dateityp: {doc.contentType}</p>
                        <p>Kommentare: {doc.comments?.length ?? 0}</p>

                        <div className="comments-section">
                          <h4>Kommentare</h4>

                          {(!doc.comments || doc.comments.length === 0) ? (
                              <p>Noch keine Kommentare vorhanden.</p>
                          ) : (
                              <ul className="comment-list">
                                {doc.comments.map(comment => (
                                    <li key={comment.id}>
                                      {comment.text}
                                    </li>
                                ))}
                              </ul>
                          )}

                          <form
                              className="comment-form"
                              onSubmit={event => {
                                event.preventDefault()
                                handleAddComment(doc.id)
                              }}
                          >
    <textarea
        placeholder="Neuen Kommentar schreiben..."
        value={commentTexts[doc.id] ?? ''}
        maxLength={255}
        required
        onChange={event => {
          setCommentTexts(previous => ({
            ...previous,
            [doc.id]: event.target.value
          }))
        }}
    />

                            <button
                                type="submit"
                                disabled={
                                    commentSavingId === doc.id ||
                                    !(commentTexts[doc.id] ?? '').trim()
                                }
                            >
                              {commentSavingId === doc.id
                                  ? 'Speichern...'
                                  : 'Kommentar hinzufügen'}
                            </button>
                          </form>
                        </div>

                        {editingId === doc.id ? (
                            <div className="edit-form">
                              <input
                                  type="text"
                                  value={editTitle}
                                  maxLength={255}
                                  required
                                  onChange={event => setEditTitle(event.target.value)}
                              />

                              <button
                                  onClick={() => handleUpdate(doc.id)}
                                  disabled={!editTitle.trim()}
                              >
                                Speichern
                              </button>

                              <button onClick={() => setEditingId(null)}>
                                Abbrechen
                              </button>
                            </div>
                        ) : (

                            <>
                              <button
                                  onClick={() => {
                                    setEditingId(doc.id)
                                    setEditTitle(doc.title)
                                  }}
                              >
                                Bearbeiten
                              </button>

                              <button
                                  className="delete-button"
                                  onClick={() => handleDelete(doc.id, doc.title)}
                              >
                                Löschen
                              </button>
                            </>

                        )}

                      </div>
                  ))
              )}
            </div>
        )}
      </main>
  )
}

export default App
