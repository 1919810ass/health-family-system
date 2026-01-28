import { getToken } from '../utils/auth'

export const chatStream = async (message, onMessage, onError, onComplete) => {
  try {
    const token = getToken()
    const response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ message })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk
      
      const lines = buffer.split('\n');
      buffer = lines.pop() || ''; 

      for (const line of lines) {
        if (line.trim() === '') continue; // Skip empty lines
        
        if (line.startsWith('data:')) {
          let content = line.replace(/^data:\s*/, '');
          // Double check for double data prefix
          if (content.startsWith('data:')) {
             content = content.replace(/^data:\s*/, '');
          }
          
          // Check for [DONE]
          if (content.trim() === '[DONE]') continue;

          if (content) { 
             onMessage(content);
          }
        }
      }
    }
    
    // 处理剩余的 buffer（如果最后一行没有换行符但流结束了）
    if (buffer && buffer.startsWith('data:')) {
        const content = buffer.substring(5);
        if (content) onMessage(content);
    }
    
    if (onComplete) onComplete()

  } catch (error) {
    console.error('Stream error:', error)
    if (onError) onError(error)
  }
}

export const chatImageStream = async (message, image, onMessage, onError, onComplete) => {
  try {
    const token = getToken()
    const response = await fetch('/api/ai/chat/image-stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ message, image })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk
      
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        if (line.trim() === '') continue;

        if (line.startsWith('data:')) {
          let content = line.replace(/^data:\s*/, '');
          if (content.startsWith('data:')) {
             content = content.replace(/^data:\s*/, '');
          }
          
          if (content.trim() === '[DONE]') continue;

          if (content) {
             onMessage(content);
          }
        }
      }
    }
    
    if (buffer && buffer.startsWith('data:')) {
        const content = buffer.substring(5);
        if (content) onMessage(content);
    }
    
    if (onComplete) onComplete()

  } catch (error) {
    console.error('Image stream error:', error)
    if (onError) onError(error)
  }
}
