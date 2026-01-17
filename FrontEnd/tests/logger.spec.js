import { describe, it, expect } from 'vitest'
import { error, info } from '../src/utils/logger'

describe('logger', () => {
  it('should write logs to localStorage and dispatch event on error', async () => {
    const handler = (e) => { globalThis.__evt = e.detail }
    window.addEventListener('hf-error', handler)
    info('TEST', 'INFO_EVENT', { a: 1 })
    error('TEST', 'ERROR_EVENT', { a: 2 })
    window.removeEventListener('hf-error', handler)
    const raw = localStorage.getItem('hf_logs')
    const list = JSON.parse(raw)
    expect(list.length).toBeGreaterThan(0)
    expect(globalThis.__evt).toBeTruthy()
    expect(globalThis.__evt.action).toBe('ERROR_EVENT')
  })
})
