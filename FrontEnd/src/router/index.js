import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login/index.vue'),
    meta: {
      public: true,
      title: '登录',
    },
  },
  {
    path: '/doctor/login',
    name: 'doctor-login',
    component: () => import('../views/Doctor/Login.vue'),
    meta: { public: true, title: '医生登录' },
  },
  {
    path: '/doctor/register',
    name: 'doctor-register',
    component: () => import('../views/Doctor/Register.vue'),
    meta: { public: true, title: '医生注册' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/Login/Register.vue'),
    meta: { public: true, title: '注册' },
  },
  {
    path: '/admin/register',
    name: 'admin-register',
    component: () => import('../views/Admin/Register.vue'),
    meta: { public: true, title: '管理员注册' },
  },
  {
    path: '/',
    component: () => import('../components/Layout/BaseLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/login',
      },
      {
        path: '/home',
        name: 'home',
        component: () => import('../views/Home/index.vue'),
        meta: {
          title: '首页',
        },
      },
      {
        path: '/report',
        name: 'report',
        component: () => import('../views/HealthReport/Index.vue'),
        meta: {
          title: '智能报告解读',
        },
      },
      {
        path: '/report/upload',
        name: 'report-upload',
        component: () => import('../views/HealthReport/Upload.vue'),
        meta: {
          title: '上传报告',
        },
      },
      {
        path: '/report/detail/:id',
        name: 'report-detail',
        component: () => import('../views/HealthReport/Detail.vue'),
        meta: {
          title: '报告详情',
        },
      },
      {
        path: '/families',
        component: () => import('../views/Family/index.vue'),
        meta: {
          title: '家庭管理',
        },
      },
      {
        path: '/tcm',
        component: () => import('../views/Assessment/TcmMain.vue'),
        meta: {
          title: '中医体质',
        },
        children: [
          {
            path: 'assessments',
            component: () => import('../views/Assessment/index.vue'),
            meta: {
              title: '体质测评',
              parentTitle: '中医体质',
            },
          },
          {
            path: 'personalized-plan',
            component: () => import('../views/Assessment/TcmPersonalizedPlan.vue'),
            meta: {
              title: '个性化中医养生方案',
              parentTitle: '中医体质',
            },
          },
          {
            path: 'trend',
            component: () => import('../views/Assessment/ConstitutionTrendAnalysis.vue'),
            meta: {
              title: '体质变化趋势分析',
              parentTitle: '中医体质',
            },
          },
          {
            path: 'family-overview/:familyId',
            component: () => import('../views/Assessment/FamilyTcmHealthOverview.vue'),
            meta: {
              title: '家庭中医健康概览',
              parentTitle: '中医体质',
            },
            props: true
          },
          {
            path: 'ai',
            component: () => import('../views/Assessment/AiAssessment.vue'),
            meta: {
              title: 'AI中医体质测评',
              parentTitle: '中医体质',
            },
          },
        ]
      },
      {
        path: '/ai-chat',
        component: () => import('../views/AiChat/index.vue'),
        meta: {
          title: 'AI 健康助手',
        },
      },
      {
        path: '/logs',
        component: () => import('../views/HealthLog/index.vue'),
        meta: {
          title: '健康日志',
        },
      },
      {
        path: '/recommendations',
        component: () => import('../views/Recommendation/index.vue'),
        meta: {
          title: '健康建议',
        },
      },
      {
        path: '/lifestyle',
        component: () => import('../views/Lifestyle/index.vue'),
        meta: {
          title: '生活方式',
        },
      },
      {
        path: '/doctor-consultation',
        component: () => import('../views/DoctorConsultation/index.vue'),
        meta: {
          title: '在线咨询',
          allowRoles: ['MEMBER', 'FAMILY_ADMIN', 'ADMIN'],
        },
      },
      {
        path: '/consultation',
        component: () => import('../views/Consultation/index.vue'),
        meta: {
          title: '智能咨询',
        },
      },
      {
        path: '/collaboration',
        component: () => import('../views/Collaboration/index.vue'),
        meta: {
          title: '家庭协作',
        },
      },

      {
        path: '/family-doctor',
        component: () => import('../views/Doctor/index.vue'),
        meta: { title: '家庭医生对接', roles: ['FAMILY_ADMIN', 'DOCTOR'] },
      },
      {
        path: '/reminders',
        component: () => import('../views/Reminder/index.vue'),
        meta: {
          title: '健康提醒',
          roles: ['ADMIN', 'DOCTOR', 'FAMILY_ADMIN', 'MEMBER'],
        },
      },
      {
        path: '/settings',
        component: () => import('../views/Settings/index.vue'),
        meta: {
          title: '系统设置',
        },
      },
      {
        path: '/security',
        component: () => import('../views/Security/index.vue'),
        meta: {
          title: '数据安全与隐私',
        },
      },
      {
        path: '/ops',
        component: () => import('../views/Ops/index.vue'),
        meta: {
          title: '系统运维',
          roles: ['ADMIN'],
        },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('../components/Layout/BaseLayout.vue'),
    meta: { roles: ['ADMIN'] },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard',
      },
      {
        path: 'dashboard',
        component: () => import('../views/Admin/Dashboard.vue'),
        meta: { title: '管理后台仪表板', roles: ['ADMIN'] },
      },
      {
        path: 'users',
        component: () => import('../views/Admin/Users/List.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] },
      },
      {
        path: 'users/audit',
        component: () => import('../views/Admin/Users/List.vue'),
        meta: { title: '用户审核', roles: ['ADMIN'] },
      },
      {
        path: 'users/roles',
        component: () => import('../views/Admin/Users/List.vue'),
        meta: { title: '角色管理', roles: ['ADMIN'] },
      },
      {
        path: 'users/logs',
        component: () => import('../views/Admin/Users/List.vue'),
        meta: { title: '活动日志', roles: ['ADMIN'] },
      },
      {
        path: 'families',
        component: () => import('../views/Admin/Families/List.vue'),
        meta: { title: '家庭管理', roles: ['ADMIN'] },
      },
      {
        path: 'families/audit',
        component: () => import('../views/Admin/Families/List.vue'),
        meta: { title: '家庭审核', roles: ['ADMIN'] },
      },
      {
        path: 'families/members',
        component: () => import('../views/Admin/Families/List.vue'),
        meta: { title: '成员管理', roles: ['ADMIN'] },
      },
      {
        path: 'families/stats',
        component: () => import('../views/Admin/Families/List.vue'),
        meta: { title: '数据统计', roles: ['ADMIN'] },
      },
      {
        path: 'health/logs',
        component: () => import('../views/Admin/Health/Logs.vue'),
        meta: { title: '健康日志管理', roles: ['ADMIN'] },
      },
      {
        path: 'health/assessments',
        component: () => import('../views/Admin/Health/Logs.vue'),
        meta: { title: '体质测评', roles: ['ADMIN'] },
      },
      {
        path: 'health/reminders',
        component: () => import('../views/Admin/Health/Reminders.vue'),
        meta: { title: '健康提醒管理', roles: ['ADMIN'] },
      },
      {
        path: 'health/recommendations',
        component: () => import('../views/Admin/Health/Reminders.vue'),
        meta: { title: 'AI建议', roles: ['ADMIN'] },
      },
      {
        path: 'doctors',
        component: () => import('../views/Admin/Doctors/List.vue'),
        meta: { title: '医生管理', roles: ['ADMIN'] },
      },
      {
        path: 'doctors/audit',
        component: () => import('../views/Admin/Doctors/List.vue'),
        meta: { title: '资质审核', roles: ['ADMIN'] },
      },
      {
        path: 'doctors/collaboration',
        component: () => import('../views/Admin/Doctors/List.vue'),
        meta: { title: '协作监控', roles: ['ADMIN'] },
      },
      {
        path: 'doctors/stats',
        component: () => import('../views/Admin/Doctors/List.vue'),
        meta: { title: '服务统计', roles: ['ADMIN'] },
      },
      {
        path: 'monitoring/system',
        component: () => import('../views/Admin/Monitoring/System.vue'),
        meta: { title: '系统监控', roles: ['ADMIN'] },
      },
      {
        path: 'monitoring/users',
        component: () => import('../views/Admin/Monitoring/Users.vue'),
        meta: { title: '用户活动', roles: ['ADMIN'] },
      },
      {
        path: 'monitoring/reports',
        component: () => import('../views/Admin/Monitoring/Reports.vue'),
        meta: { title: '数据报告', roles: ['ADMIN'] },
      },
      {
        path: 'monitoring/custom',
        component: () => import('../views/Admin/Monitoring/CustomReports.vue'),
        meta: { title: '自定义报告', roles: ['ADMIN'] },
      },
      {
        path: 'system/config',
        component: () => import('../views/Admin/System/Config.vue'),
        meta: { title: '系统配置', roles: ['ADMIN'] },
      },
      {
        path: 'system/security',
        component: () => import('../views/Admin/System/Security.vue'),
        meta: { title: '安全设置', roles: ['ADMIN'] },
      },
      {
        path: 'system/audit',
        component: () => import('../views/Admin/System/Audit.vue'),
        meta: { title: '操作审计', roles: ['ADMIN'] },
      },
      {
        path: 'system/backup',
        component: () => import('../views/Admin/System/Backup.vue'),
        meta: { title: '数据备份', roles: ['ADMIN'] },
      },
      {
        path: 'ops',
        component: () => import('../views/Ops/index.vue'),
        meta: { title: '系统运维', roles: ['ADMIN'] },
      },
    ],
  },
  {
    path: '/doctor',
    component: () => import('../components/Layout/BaseLayout.vue'),
    meta: { roles: ['DOCTOR'] }, // 医生专用路由组
    children: [
      { path: '', redirect: '/doctor/enhanced-monitoring' },
      { path: 'workbench', component: () => import('../views/Doctor/Workbench.vue'), meta: { title: '医生工作台', roles: ['DOCTOR'] } },
      { path: 'patients', component: () => import('../views/Doctor/Patients.vue'), meta: { title: '患者管理', roles: ['DOCTOR'] } },
      { path: 'reports', component: () => import('../views/Doctor/Reports.vue'), meta: { title: '体检报告与点评', roles: ['DOCTOR'] } },
      { path: 'report-generation', component: () => import('../views/Doctor/ReportGeneration.vue'), meta: { title: '智能报告生成', roles: ['DOCTOR'] } },
      { path: 'consultation', component: () => import('../views/Doctor/Consultation.vue'), meta: { title: '在线咨询', roles: ['DOCTOR'] } },
      { path: 'enhanced-monitoring', component: () => import('../views/Doctor/EnhancedMonitoring.vue'), meta: { title: '健康监测与预警', roles: ['DOCTOR'] } },
      { path: 'plans', component: () => import('../views/Doctor/Plans.vue'), meta: { title: '健康计划与随访', roles: ['DOCTOR'] } },
      { path: 'analysis', component: () => import('../views/Doctor/Analysis.vue'), meta: { title: '数据统计与分析', roles: ['DOCTOR'] } },
      { path: 'ratings', component: () => import('../views/Doctor/Ratings.vue'), meta: { title: '我的评价', roles: ['DOCTOR'] } },
      { path: 'settings', component: () => import('../views/Doctor/Settings.vue'), meta: { title: '医生系统设置', roles: ['DOCTOR'] } },
      { path: 'bind', component: () => import('../views/Doctor/index.vue'), meta: { title: '家庭医生对接', roles: ['DOCTOR'] } },
      { 
        path: 'patients/:userId/logs', 
        name: 'doctor-patient-logs',
        component: () => import('../views/Doctor/PatientHealthLog.vue'), 
        meta: { title: '患者健康日志', roles: ['DOCTOR'] } 
      },
      { 
        path: 'patients/:userId/recommendations', 
        name: 'doctor-patient-recommendations',
        component: () => import('../views/Doctor/PatientRecommendation.vue'), 
        meta: { title: '患者健康建议', roles: ['DOCTOR'] } 
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to, from, next) => {
  // 构建页面标题，支持层级结构
  let pageTitle = '健康家庭'
  if (to.meta.parentTitle && to.meta.title) {
    pageTitle = `${to.meta.parentTitle} - ${to.meta.title}｜健康家庭`
  } else if (to.meta.title) {
    pageTitle = `${to.meta.title}｜健康家庭`
  } else {
    pageTitle = '健康家庭'
  }
  document.title = pageTitle
  const token = getToken()
  const isDev = import.meta.env.MODE === 'development'
  const devUserId = localStorage.getItem('dev_user_id')

  // 如果访问登录页，允许访问（Login页面组件会处理清除Token的逻辑）
  if (to.path === '/login') {
    next()
    return
  }

  if (to.path === '/') {
    next('/login')
    return
  }

  if (!to.meta.public && !token) {
    if (isDev && devUserId) {
      next()
      return
    }
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 权限检查
  if (token) {
    const userStore = useUserStore()
    
    // 检查是否为医生专用路由
    // 注意：/doctor-consultation 是患者端在线咨询，不是医生路由
    const isDoctorRoute = to.path.startsWith('/doctor/') && to.path !== '/doctor/login' && to.path !== '/doctor/register'
    
    // 如果访问医生路由或路由需要特定角色权限，需要检查用户角色
    const needRoleCheck = isDoctorRoute || (to.meta.roles && to.meta.roles.length > 0) || (to.meta.allowRoles && to.meta.allowRoles.length > 0)
    
    if (needRoleCheck) {
      // 确保用户信息已加载
      if (!userStore.profile) {
        try {
          console.log(`[Router] Loading user profile for route: ${to.path}`)
          await userStore.fetchProfile()
          console.log(`[Router] User profile loaded:`, userStore.profile)
        } catch (error) {
          console.error(`[Router] Failed to fetch profile for ${to.path}:`, error)
          // 如果获取用户信息失败，重定向到登录页
          next({ path: '/login', query: { redirect: to.fullPath } })
          return
        }
      }

      const role = userStore.profile?.role
      console.log(`[Router] Checking permission for ${to.path}, user role: ${role}, required roles: ${to.meta.roles || to.meta.allowRoles}`)
      
      // 医生路由统一检查：必须是 DOCTOR 角色
      if (isDoctorRoute) {
        if (role !== 'DOCTOR') {
          console.warn(`[Router] Access denied to doctor route for role: ${role}`)
          ElMessage.error('医生专用页面，无权访问')
          // 非医生用户访问医生路由时，重定向到患者端首页
          next('/home')
          return
        }
      } else if (to.meta.roles && to.meta.roles.length > 0) {
        // 受限制的患者端路由按 meta.roles 检查
        if (!role || !to.meta.roles.includes(role)) {
          console.warn(`[Router] Access denied: user role ${role} not in required roles ${to.meta.roles}`)
          ElMessage.error('无权访问该模块')
          next('/home')
          return
        }
      } else if (to.meta.allowRoles && to.meta.allowRoles.length > 0) {
        // 患者端在线咨询等功能按 meta.allowRoles 检查
        if (!role || !to.meta.allowRoles.includes(role)) {
          console.warn(`[Router] Access denied: user role ${role} not in allowed roles ${to.meta.allowRoles}`)
          ElMessage.error('无权访问该模块')
          next('/home')
          return
        }
      }
    }
  }

  next()
})

router.onError((error) => {
  console.error('Router navigation error:', error)
})

export default router
