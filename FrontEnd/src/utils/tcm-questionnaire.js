// 中医体质测评问卷（基于《中医体质分类与判定》标准）

export const QUESTION_OPTIONS = [
  { label: '没有（根本不/从来没有）', value: 1 },
  { label: '很少（有一点/偶尔有）', value: 2 },
  { label: '有时（有些/少数时间有）', value: 3 },
  { label: '经常（相当/多数时间有）', value: 4 },
  { label: '总是（非常/每天都有）', value: 5 }
]

// 9种体质的题目映射
// 题目来源于标准问卷，为简化演示，每种体质选取4个核心问题
export const TCM_QUESTIONS = [
  // 1. 阳虚质
  { id: 'yang_1', type: 'YANG_DEFICIENCY', content: '您手脚发凉吗？' },
  { id: 'yang_2', type: 'YANG_DEFICIENCY', content: '您胃脘部、背部或腰膝部怕冷吗？' },
  { id: 'yang_3', type: 'YANG_DEFICIENCY', content: '您感到怕冷、衣服比别人穿得多吗？' },
  { id: 'yang_4', type: 'YANG_DEFICIENCY', content: '您吃(喝)凉的东西会感到不舒服或者怕吃(喝)凉的东西吗？' },

  // 2. 阴虚质
  { id: 'yin_1', type: 'YIN_DEFICIENCY', content: '您感到手脚心发热吗？' },
  { id: 'yin_2', type: 'YIN_DEFICIENCY', content: '您感觉身体、脸上发热吗？' },
  { id: 'yin_3', type: 'YIN_DEFICIENCY', content: '您感到口干咽燥、总想喝水吗？' },
  { id: 'yin_4', type: 'YIN_DEFICIENCY', content: '您皮肤或口唇干吗？' },

  // 3. 气虚质
  { id: 'qi_1', type: 'QI_DEFICIENCY', content: '您容易疲乏吗？' },
  { id: 'qi_2', type: 'QI_DEFICIENCY', content: '您容易气短(呼吸短促，接不上气)吗？' },
  { id: 'qi_3', type: 'QI_DEFICIENCY', content: '您容易心慌吗？' },
  { id: 'qi_4', type: 'QI_DEFICIENCY', content: '您说话声音低弱无力吗？' },

  // 4. 痰湿质
  { id: 'phlegm_1', type: 'PHLEGM_DAMPNESS', content: '您感到胸闷或腹部胀满吗？' },
  { id: 'phlegm_2', type: 'PHLEGM_DAMPNESS', content: '您感到身体沉重不轻松或不爽快吗？' },
  { id: 'phlegm_3', type: 'PHLEGM_DAMPNESS', content: '您腹部肥满松软吗？' },
  { id: 'phlegm_4', type: 'PHLEGM_DAMPNESS', content: '您嘴里有黏黏的感觉吗？' },

  // 5. 湿热质
  { id: 'damp_1', type: 'DAMP_HEAT', content: '您面部或鼻部有油腻感或者油亮发光吗？' },
  { id: 'damp_2', type: 'DAMP_HEAT', content: '您容易生痤疮或疮疖吗？' },
  { id: 'damp_3', type: 'DAMP_HEAT', content: '您感到口苦或嘴里有异味吗？' },
  { id: 'damp_4', type: 'DAMP_HEAT', content: '您大便黏滞不爽、有解不尽的感觉吗？' },

  // 6. 血瘀质
  { id: 'blood_1', type: 'BLOOD_STASIS', content: '您的皮肤在不知不觉中会出现青紫瘀斑(皮下出血)吗？' },
  { id: 'blood_2', type: 'BLOOD_STASIS', content: '您两颧部有细微红丝吗？' },
  { id: 'blood_3', type: 'BLOOD_STASIS', content: '您身体上有哪里疼痛吗？' },
  { id: 'blood_4', type: 'BLOOD_STASIS', content: '您容易忘事（健忘）吗？' },

  // 7. 气郁质
  { id: 'stagnation_1', type: 'QI_STAGNATION', content: '您感到闷闷不乐、情绪低沉吗？' },
  { id: 'stagnation_2', type: 'QI_STAGNATION', content: '您容易精神紧张、焦虑不安吗？' },
  { id: 'stagnation_3', type: 'QI_STAGNATION', content: '您因为生活状态改变而感到适应困难吗？' },
  { id: 'stagnation_4', type: 'QI_STAGNATION', content: '您无缘无故叹气吗？' },

  // 8. 特禀质
  { id: 'special_1', type: 'SPECIAL_DIATHESIS', content: '您没有感冒时也会打喷嚏吗？' },
  { id: 'special_2', type: 'SPECIAL_DIATHESIS', content: '您没有感冒时也会鼻塞、流鼻涕吗？' },
  { id: 'special_3', type: 'SPECIAL_DIATHESIS', content: '您有因季节变化、温度变化或异味等原因而咳喘的现象吗？' },
  { id: 'special_4', type: 'SPECIAL_DIATHESIS', content: '您容易过敏(对药物、食物、气味、花粉或在季节交替、气候变化时)吗？' },

  // 9. 平和质 (反向计分，但为简化逻辑，这里按正向问：精力充沛吗？答案1-5分，5分最好)
  // 注意：标准问卷平和质计算较复杂，这里简化为：各问题得分越高越好
  { id: 'balanced_1', type: 'BALANCED', content: '您精力充沛吗？' },
  { id: 'balanced_2', type: 'BALANCED', content: '您容易疲劳吗？' }, // 逆向题
  { id: 'balanced_3', type: 'BALANCED', content: '您说话声音低弱无力吗？' }, // 逆向题
  { id: 'balanced_4', type: 'BALANCED', content: '您容易情绪低落吗？' }, // 逆向题
]

/**
 * 计算体质转化分
 * 转化分 = [(原始分 - 条目数) / (条目数 * 4)] * 100
 * @param {string} type 体质类型
 * @param {Array} typeQuestions 该体质下的题目列表
 * @param {Object} allAnswers 用户答案 {qid: score}
 */
export function calculateScore(type, typeQuestions, allAnswers) {
  let rawScore = 0
  let count = 0

  typeQuestions.forEach(q => {
    let score = Number(allAnswers[q.id] || 0)
    
    // 平和质特殊处理：除了第一题，其他都是逆向题（即问的是病理表现）
    // 如果是平和质的逆向题，分数需要反转：1->5, 2->4, 3->3, 4->2, 5->1
    // 逻辑：平和质问卷里，精力充沛(正向)，容易疲劳(逆向)。
    // 标准判定里，平和质是基于其他8种体质分数低 + 平和质分数高。
    // 为了简化演示，我们这里简单处理：
    // 平和质的题目如果是负面描述（如容易疲劳），用户选"没有(1)"，其实代表平和度高(5)。
    if (type === 'BALANCED' && q.id !== 'balanced_1') {
       if (score > 0) score = 6 - score // 1->5, 5->1
    }
    
    if (score > 0) {
      rawScore += score
      count++
    }
  })

  if (count === 0) return 0

  // 转化分公式
  const conversionScore = ((rawScore - count) / (count * 4)) * 100
  return Math.max(0, Math.min(100, conversionScore)) // 限制在0-100
}
