export const log = (level, module, action, detail) => {
  try {
    const entry = {
      level,
      module,
      action,
      detail,
      createdAt: new Date().toISOString(),
    }
    const raw = localStorage.getItem('hf_logs') || '[]'
    const list = JSON.parse(raw)
    list.unshift(entry)
    if (list.length > 500) list.pop()
    localStorage.setItem('hf_logs', JSON.stringify(list))
    if (level === 'ERROR') {
      const evt = new CustomEvent('hf-error', { detail: entry })
      window.dispatchEvent(evt)
    }
    console[level === 'ERROR' ? 'error' : level === 'WARN' ? 'warn' : 'log'](`[HF][${module}] ${action}`, detail)
    if (import.meta.env.PROD) {
      import('axios').then(({ default: axios }) => {
        axios.post('/api/admin/ops/logs', entry).catch(() => {})
      }).catch(() => {})
    }
  } catch {}
}

export const info = (module, action, detail) => log('INFO', module, action, detail)
export const warn = (module, action, detail) => log('WARN', module, action, detail)
export const error = (module, action, detail) => log('ERROR', module, action, detail)
