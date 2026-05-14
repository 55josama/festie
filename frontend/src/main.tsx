import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

async function prepare() {
  const useMock = import.meta.env.DEV && String(import.meta.env.VITE_USE_MSW ?? 'true') === 'true'
  if (useMock) {
    const { worker } = await import('./mocks/browser')
    await worker.start({ onUnhandledRequest: 'bypass' })
  }
}

function mount() {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <App />
    </StrictMode>,
  )
}

prepare()
  .catch((error) => {
    console.error('MSW 초기화 실패:', error)
  })
  .finally(() => {
    mount()
  })
