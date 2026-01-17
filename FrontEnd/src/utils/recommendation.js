export const mapToBackendCategories = (arr) => {
  const M = { DIET: 'DIET', SLEEP: 'REST', SPORT: 'SPORT', MOOD: 'EMOTION' }
  return (arr || []).map(k => M[k] || k)
}

