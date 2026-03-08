import request from '../utils/request'

export const generateWeeklyReport = (familyId) => request.post(`/reports/family/${familyId}/weekly`);

export const getUserReports = (userId) => request.get('/reports', { params: { userId } });

export const submitReport = (data, userId) => request.post('/reports', data);

export const uploadReportImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const getReportDetail = (reportId, userId) => request.get(`/reports/${reportId}`);

export const getReportStatus = (reportId, userId) => request.get(`/reports/${reportId}/status`);

// Doctor Generated Reports (Patient View)
export const getMyDoctorReports = () => request.get('/patient/reports')
export const getMyDoctorReportDetail = (id) => request.get(`/patient/reports/${id}`)
