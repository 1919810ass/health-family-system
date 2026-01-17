export const phoneRule = { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }
export const requiredRule = { required: true, message: '此项为必填', trigger: 'blur' }
export const passwordRule = { min: 6, message: '密码长度至少6位', trigger: 'blur' }