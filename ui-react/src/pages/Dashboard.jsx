import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router'
import { api, formatDate } from '../api'
import { hasErrors, normalizeDocument, validateDocument } from '../validation'
import DocumentFields from '../components/DocumentFields'

const EMPTY_FORM = { title: '', filename: '', contentType: 'application/pdf' }

function Dashboard() {
  const [documents, setDocuments] = useState([])
  const [loading, setLoading] = useState(true)
  const [listError, setListError] = useState('')
  const [search, setSearch] = useState('')

  const [form, setForm] = useState(EMPTY_FORM)
  // Fehler erst nach dem ersten Absenden zeigen, danach bei jeder Eingabe neu prüfen
  const [submitted, setSubmitted] = useState(false)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState(null)

  const errors = submitted ? validateDocument(form) : {}

  const loadDocuments = useCallback(() =>
    api.getDocuments()
      .then(data => {
        setDocuments(data)
        setListError('')
      })
      .catch(err => setListError(`Dokumente konnten nicht geladen werden: ${err.message}`))
      .finally(() => setLoading(false)), [])

  useEffect(() => {
    loadDocuments()
  }, [loadDocuments])

  async function handleCreate(event) {
    event.preventDefault()
    setSubmitted(true)
    setMessage(null)

    if (hasErrors(validateDocument(form))) {
      return // ungültig → kein Request ans Backend
    }

    setSaving(true)
    try {
      await api.createDocument(normalizeDocument(form))
      setForm(EMPTY_FORM)
      setSubmitted(false)
      setMessage({ type: 'success', text: 'Dokument erfolgreich erstellt.' })
      await loadDocuments()
    } catch (err) {
      setMessage({ type: 'error', text: `Dokument konnte nicht erstellt werden: ${err.message}` })
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(doc) {
    if (!window.confirm(`Möchtest du "${doc.title}" inklusive Kommentaren wirklich löschen?`)) {
      return
    }
    try {
      await api.deleteDocument(doc.id)
      await loadDocuments()
    } catch (err) {
      setListError(`Dokument konnte nicht gelöscht werden: ${err.message}`)
    }
  }

  const filter = search.trim().toLowerCase()
  const filteredDocuments = documents.filter(doc =>
    !filter
    || doc.title.toLowerCase().includes(filter)
    || doc.filename.toLowerCase().includes(filter))

  return (
    <main className="page">
      <h1>Paperless Dashboard</h1>
      <p className="subtitle">SWEN3 – Dokumentenverwaltung mit React</p>

      <section className="card">
        <h2>Neues Dokument erstellen</h2>
        <form className="form" onSubmit={handleCreate} noValidate>
          <DocumentFields values={form} errors={errors} onChange={setForm} />
          <button type="submit" className="btn" disabled={saving}>
            {saving ? 'Speichern...' : 'Dokument erstellen'}
          </button>
        </form>
        {message && <p className={`message ${message.type}`} role="status">{message.text}</p>}
      </section>

      <section>
        <div className="section-header">
          <h2>Meine Dokumente</h2>
          <input
            type="search"
            className="search"
            placeholder="Nach Titel oder Dateiname suchen..."
            aria-label="Dokumente filtern"
            value={search}
            onChange={event => setSearch(event.target.value)}
          />
        </div>

        {loading && <p>Dokumente werden geladen...</p>}
        {listError && <p className="message error" role="alert">{listError}</p>}

        {!loading && !listError && (
          filteredDocuments.length === 0 ? (
            <p className="muted">
              {documents.length === 0 ? 'Noch keine Dokumente vorhanden.' : 'Keine Dokumente passen zum Filter.'}
            </p>
          ) : (
            <div className="documents">
              {filteredDocuments.map(doc => (
                <article className="document-card" key={doc.id}>
                  <h3><Link to={`/documents/${doc.id}`}>{doc.title}</Link></h3>
                  <dl className="meta">
                    <dt>Dateiname</dt><dd>{doc.filename}</dd>
                    <dt>Dateityp</dt><dd>{doc.contentType || '–'}</dd>
                    <dt>Hochgeladen</dt><dd>{formatDate(doc.uploadedAt)}</dd>
                    <dt>Kommentare</dt><dd>{doc.comments?.length ?? 0}</dd>
                  </dl>
                  <div className="actions">
                    <Link className="btn" to={`/documents/${doc.id}`}>Details</Link>
                    <button className="btn btn-danger" onClick={() => handleDelete(doc)}>Löschen</button>
                  </div>
                </article>
              ))}
            </div>
          )
        )}
      </section>
    </main>
  )
}

export default Dashboard
