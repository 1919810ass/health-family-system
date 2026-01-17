let token = ''

export const getToken = () => token
export const setToken = (t) => {
  token = t
}
export const removeToken = () => {
  token = ''
}