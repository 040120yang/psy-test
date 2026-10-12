import request from '@/utils/request'

/** 学生 360° 个案档案（基本信息 + 测评 + 评估 + 随访 + 沟通） */
export function getStudentProfile(studentId) {
  return request({ url: '/student/profile', method: 'get', params: { studentId } })
}
