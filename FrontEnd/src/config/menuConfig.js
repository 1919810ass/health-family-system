
export const MENU_CONFIG = {
  USER: [
    {
      title: '首页概览',
      path: '/home',
      icon: 'HomeFilled'
    },
    {
      title: '家庭管理',
      path: '/families',
      icon: 'UserFilled'
    },
    {
      title: '中医体质',
      icon: 'DataAnalysis',
      children: [
        {
          title: '体质测评',
          path: '/tcm/assessments',
          icon: 'DataAnalysis'
        },
        {
          title: '体质趋势分析',
          path: '/tcm/trend',
          icon: 'TrendCharts'
        },
        {
          title: 'AI体质测评',
          path: '/tcm/ai',
          icon: 'ChatDotRound'
        },
        {
          title: '个性化方案',
          path: '/tcm/personalized-plan',
          icon: 'DataAnalysis'
        }
      ]
    },
    {
      title: '健康日志',
      path: '/logs',
      icon: 'Notebook'
    },
    {
      title: '智能报告解读',
      path: '/report',
      icon: 'Document'
    },
    {
      title: '个性化建议',
      path: '/recommendations',
      icon: 'MagicStick'
    },
    {
      title: '生活方式',
      path: '/lifestyle',
      icon: 'DataAnalysis'
    },
    {
      title: '在线咨询',
      path: '/doctor-consultation',
      icon: 'ChatDotRound'
    },
    {
      title: '智能咨询',
      path: '/consultation',
      icon: 'Headset'
    },
    {
      title: 'AI 健康助手',
      path: '/ai-chat',
      icon: 'Cpu' // 确保在 Sidebar.vue 中引入或注册了这个图标，如果没有，可以用 ChatSquare 等
    },
    {
      title: '健康提醒',
      path: '/reminders',
      icon: 'Bell',
      permission: 'REMINDER_VISIBLE' // Special flag to handle dynamic visibility
    },
    {
      title: '家庭协作',
      path: '/collaboration',
      icon: 'Notebook'
    },
    {
      title: '家庭医生对接',
      path: '/family-doctor',
      icon: 'FirstAidKit',
      permission: 'FAMILY_DOCTOR_VISIBLE'
    },
    {
      title: '系统设置',
      path: '/settings',
      icon: 'Setting'
    },
    {
      title: '数据安全与隐私',
      path: '/security',
      icon: 'Lock'
    },
    {
      title: '系统运维',
      path: '/ops',
      icon: 'DataAnalysis',
      permission: 'ADMIN_VISIBLE' // Special flag for admin visibility in user layout
    }
  ],
  DOCTOR: [
    {
      title: '医生工作台',
      path: '/doctor/workbench',
      icon: 'HomeFilled'
    },
    {
      title: '患者管理',
      path: '/doctor/patients',
      icon: 'UserFilled'
    },
    {
      title: '体检报告与点评',
      path: '/doctor/reports',
      icon: 'Document'
    },
    {
      title: '智能报告生成',
      path: '/doctor/report-generation',
      icon: 'Printer'
    },
    {
      title: '在线咨询',
      path: '/doctor/consultation',
      icon: 'ChatDotRound'
    },
    {
      title: '健康监测与预警',
      path: '/doctor/enhanced-monitoring',
      icon: 'DataAnalysis'
    },
    {
      title: '健康计划与随访',
      path: '/doctor/plans',
      icon: 'Notebook'
    },
    {
      title: '数据统计与分析',
      path: '/doctor/analysis',
      icon: 'DataAnalysis'
    },
    {
      title: '我的评价',
      path: '/doctor/ratings',
      icon: 'StarFilled'
    },
    {
      title: '系统设置',
      path: '/doctor/settings',
      icon: 'Setting'
    },
    {
      title: '家庭医生对接',
      path: '/doctor/bind',
      icon: 'UserFilled'
    }
  ],
  ADMIN: [
    {
      title: '仪表板',
      path: '/admin/dashboard',
      icon: 'House'
    },
    {
      title: '用户管理',
      path: 'user', // Submenu index
      icon: 'User',
      children: [
        { title: '用户列表', path: '/admin/users' },
        { title: '用户审核', path: '/admin/users/audit' },
        { title: '角色管理', path: '/admin/users/roles' },
        { title: '活动日志', path: '/admin/users/logs' }
      ]
    },
    {
      title: '家庭管理',
      path: 'family',
      icon: 'House',
      children: [
        { title: '家庭列表', path: '/admin/families' },
        { title: '家庭审核', path: '/admin/families/audit' },
        { title: '成员管理', path: '/admin/families/members' },
        { title: '数据统计', path: '/admin/families/stats' }
      ]
    },
    {
      title: '健康数据',
      path: 'health',
      icon: 'DataAnalysis',
      children: [
        { title: '健康日志', path: '/admin/health/logs' },
        { title: '体质测评', path: '/admin/health/assessments' },
        { title: '健康提醒', path: '/admin/health/reminders' },
        { title: 'AI建议', path: '/admin/health/recommendations' }
      ]
    },
    {
      title: '医生管理',
      path: 'doctor',
      icon: 'User',
      children: [
        { title: '医生列表', path: '/admin/doctors' },
        { title: '资质审核', path: '/admin/doctors/audit' },
        { title: '协作监控', path: '/admin/doctors/collaboration' },
        { title: '服务统计', path: '/admin/doctors/stats' }
      ]
    },
    {
      title: '系统监控',
      path: 'monitoring',
      icon: 'Monitor',
      children: [
        { title: '系统监控', path: '/admin/monitoring/system' },
        { title: '用户活动', path: '/admin/monitoring/users' },
        { title: '数据报告', path: '/admin/monitoring/reports' },
        { title: '自定义报告', path: '/admin/monitoring/custom' }
      ]
    },
    {
      title: '系统配置',
      path: 'system',
      icon: 'Setting',
      children: [
        { title: '系统配置', path: '/admin/system/config' },
        { title: '安全设置', path: '/admin/system/security' },
        { title: '操作审计', path: '/admin/system/audit' },
        { title: '数据备份', path: '/admin/system/backup' }
      ]
    },
    {
      title: '系统运维',
      path: '/admin/ops',
      icon: 'DataAnalysis'
    }
  ]
}
