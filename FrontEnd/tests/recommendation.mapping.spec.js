import { describe, it, expect } from 'vitest'
import { mapToBackendCategories } from '../src/utils/recommendation'

describe('mapToBackendCategories', () => {
  it('should map SLEEP to REST and MOOD to EMOTION', () => {
    const input = ['DIET', 'SLEEP', 'SPORT', 'MOOD']
    const output = mapToBackendCategories(input)
    expect(output).toEqual(['DIET', 'REST', 'SPORT', 'EMOTION'])
  })

  it('should passthrough unknown keys', () => {
    const input = ['UNKNOWN']
    const output = mapToBackendCategories(input)
    expect(output).toEqual(['UNKNOWN'])
  })
})

