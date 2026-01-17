if (!globalThis.localStorage) {
  const store = new Map()
  globalThis.localStorage = {
    getItem: (key) => (store.has(String(key)) ? store.get(String(key)) : null),
    setItem: (key, value) => { store.set(String(key), String(value)) },
    removeItem: (key) => { store.delete(String(key)) },
    clear: () => { store.clear() },
    key: (index) => Array.from(store.keys())[index] ?? null,
    get length() { return store.size },
  }
}

if (!globalThis.window) {
  globalThis.window = globalThis
}

if (!globalThis.CustomEvent) {
  globalThis.CustomEvent = class CustomEvent extends Event {
    constructor(type, params) {
      super(type, params)
      this.detail = params?.detail
    }
  }
}

