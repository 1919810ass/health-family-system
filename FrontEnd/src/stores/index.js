/**
 * 前端状态管理模块：index.js
 *
 * 存储与管理全局/模块级状态，并封装与接口交互的动作。
 */

import { useAppStore } from './app'
import { useUserStore } from './user'
import { useFamilyStore } from './family'
import { useAssessmentStore } from './assessment'
import { useLogStore } from './log'
import { useRecommendationStore } from './recommendation'
import { useDoctorStore } from './doctor'

export {
  useAppStore,
  useUserStore,
  useFamilyStore,
  useAssessmentStore,
  useLogStore,
  useRecommendationStore,
  useDoctorStore,
}