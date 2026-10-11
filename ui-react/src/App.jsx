import { Link, NavLink, Route, Routes } from 'react-router'
import Dashboard from './pages/Dashboard'
import DocumentDetail from './pages/DocumentDetail'
import './App.css'

function App() {
  return (
    <>
      <header className="topbar">
        <Link to="/" className="brand">Paperless</Link>
        <nav>
          <NavLink to="/" end>Dashboard</NavLink>
        </nav>
      </header>

      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/documents/:id" element={<DocumentDetail />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  )
}

function NotFound() {
  return (
    <main className="page">
      <h1>Seite nicht gefunden</h1>
      <Link to="/" className="back-link">&larr; Zurück zum Dashboard</Link>
    </main>
  )
}

export default App
