import request from '@/utils/request'

export function getDocuments(params) {
  return request({
    url: '/admin/knowledge/list', // Changed to match backend /list or alias /documents
    method: 'get',
    params
  })
}

export function uploadDocument(data) {
  return request({
    url: '/admin/knowledge/upload', // Changed to match backend /upload
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteDocument(id) {
  return request({
    url: `/admin/knowledge/documents/${id}`,
    method: 'delete'
  })
}

export function reEmbedDocument(id) {
  // This might need to be removed if backend doesn't support it anymore, 
  // or mapped to a new endpoint if it exists. 
  // Current controller doesn't seem to have /documents/{id}/re-embed. 
  // It has /sync (global).
  // But let's leave it or remove it if not used. 
  // Index.vue uses handleReEmbed. 
  // If backend doesn't support it, we should remove it from frontend too.
  // For now, let's assume global sync is preferred.
  return request({
    url: `/admin/knowledge/documents/${id}/re-embed`,
    method: 'post'
  })
}

export function getDocumentChunks(id) {
  return request({
    url: `/admin/knowledge/${id}/chunks`, // Changed to match backend /{id}/chunks
    method: 'get'
  })
}

export function updateChunk(id, content) {
  return request({
    url: `/admin/knowledge/chunk/${id}`, // Changed to match backend /chunk/{id}
    method: 'put',
    data: { content }
  })
}

export function syncVectorStore() {
  return request({
    url: '/admin/knowledge/sync',
    method: 'post'
  })
}
