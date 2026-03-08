<template>
  <div class="assessments-management-container">
    <div class="page-header">
      <h1>测评问卷管理</h1>
      <p class="subtitle">管理测评问卷、题目、选项及对应的体质结果解读。</p>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="问卷编辑器" name="editor">
        <el-row :gutter="20">
          <!-- Left: Questionnaire List -->
          <el-col :span="8">
            <el-card class="box-card">
              <template #header>
                <div class="card-header"><span>问卷列表</span><el-button type="primary" size="small" @click="handleCreateQuestionnaire">新增问卷</el-button></div>
              </template>
              <el-table :data="questionnaires" @row-click="handleSelectQuestionnaire" highlight-current-row :row-key="row => row.id">
                <el-table-column prop="title" label="问卷标题" />
                <el-table-column label="状态" width="80">
                  <template #default="{ row }"><el-tag :type="row.isActive ? 'success' : 'info'">{{ row.isActive ? '激活' : '禁用' }}</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="{ row }"><el-button size="small" @click.stop="handleEditQuestionnaire(row)">编辑</el-button></template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>

          <!-- Right: Questions and Options -->
          <el-col :span="16">
            <el-card class="box-card" v-if="selectedQuestionnaire">
              <template #header>
                <div class="card-header"><span>题目管理: {{ selectedQuestionnaire.title }}</span><el-button type="primary" size="small" @click="handleCreateQuestion">新增题目</el-button></div>
              </template>
              <el-collapse v-model="activeQuestionId" accordion>
                <el-collapse-item v-for="question in selectedQuestionnaire.questions" :key="question.id" :name="question.id">
                  <template #title>
                    <div class="question-title"><span>{{ question.displayOrder }}. {{ question.text }}</span>
                      <div>
                        <el-button size="small" type="text" @click.stop="handleEditQuestion(question)">编辑</el-button>
                        <el-popconfirm title="确定删除此题目及其所有选项吗?" @confirm="handleDeleteQuestion(question.id)"><template #reference><el-button size="small" type="text" @click.stop>删除</el-button></template></el-popconfirm>
                      </div>
                    </div>
                  </template>
                  <el-table :data="question.options" size="small" class="options-table">
                    <el-table-column prop="text" label="选项内容" />
                    <el-table-column prop="score" label="分值" width="80" />
                    <el-table-column prop="constitutionType" label="关联体质" width="120" />
                    <el-table-column label="操作" width="120">
                      <template #default="{ row }">
                        <el-button size="small" type="text" @click="handleEditOption(question, row)">编辑</el-button>
                        <el-popconfirm title="确定删除此选项吗?" @confirm="handleDeleteOption(row.id)"><template #reference><el-button size="small" type="text">删除</el-button></template></el-popconfirm>
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-button class="add-option-btn" size="small" @click="handleCreateOption(question)">新增选项</el-button>
                </el-collapse-item>
              </el-collapse>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="体质定义管理" name="constitutions">
        <el-card class="box-card">
          <el-table :data="constitutions" style="width: 100%">
            <el-table-column prop="name" label="体质名称" width="120" />
            <el-table-column prop="description" label="体质解读" show-overflow-tooltip />
            <el-table-column prop="cause" label="成因" show-overflow-tooltip />
            <el-table-column prop="performance" label="表现" show-overflow-tooltip />
            <el-table-column label="操作" width="80">
              <template #default="{ row }"><el-button size="small" @click="handleEditConstitution(row)">编辑</el-button></template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- Dialogs -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="60%">
      <el-form :model="dialog.form" label-width="100px" label-position="top">
        <template v-if="dialog.type === 'questionnaire'">
          <el-form-item label="问卷标题"><el-input v-model="dialog.form.title" /></el-form-item>
          <el-form-item label="描述"><el-input v-model="dialog.form.description" type="textarea" /></el-form-item>
          <el-form-item label="状态"><el-switch v-model="dialog.form.isActive" /></el-form-item>
        </template>
        <template v-if="dialog.type === 'question'">
          <el-form-item label="题目内容"><el-input v-model="dialog.form.text" type="textarea" /></el-form-item>
          <el-form-item label="显示顺序"><el-input-number v-model="dialog.form.displayOrder" :min="1" /></el-form-item>
          <el-form-item label="关联体质">
            <el-select v-model="dialog.form.constitutionType" placeholder="选择体质">
              <el-option v-for="item in constitutionTypes" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </template>
        <template v-if="dialog.type === 'option'">
          <el-form-item label="选项内容"><el-input v-model="dialog.form.text" /></el-form-item>
          <el-form-item label="分值"><el-input-number v-model="dialog.form.score" /></el-form-item>
        </template>
        <template v-if="dialog.type === 'constitution'">
          <el-form-item label="体质名称"><el-input v-model="dialog.form.name" disabled /></el-form-item>
          <el-form-item label="体质解读"><el-input v-model="dialog.form.description" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="成因"><el-input v-model="dialog.form.cause" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="表现"><el-input v-model="dialog.form.performance" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="饮食建议"><el-input v-model="dialog.form.dietAdvice" type="textarea" :rows="4" /></el-form-item>
          <el-form-item label="运动建议"><el-input v-model="dialog.form.sportAdvice" type="textarea" :rows="4" /></el-form-item>
          <el-form-item label="生活建议"><el-input v-model="dialog.form.lifestyleAdvice" type="textarea" :rows="4" /></el-form-item>
        </template>
      </el-form>
      <template #footer><el-button @click="dialog.visible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { 
  getQuestionnaires, createQuestionnaire, updateQuestionnaire, getQuestionnaireById, deleteQuestionnaire,
  addQuestion, updateQuestion, deleteQuestion,
  addOption, updateOption, deleteOption,
  getConstitutions, createOrUpdateConstitution
} from '../../../api/admin';

const activeTab = ref('editor');
const questionnaires = ref([]);
const selectedQuestionnaire = ref(null);
const activeQuestionId = ref(null);
const constitutions = ref([]);
const constitutionTypes = ref(['PING_HE', 'QI_XU', 'YANG_XU', 'YIN_XU', 'TAN_SHI', 'SHI_RE', 'XUE_YU', 'QI_YU', 'TE_BING']);

const dialog = reactive({ visible: false, title: '', type: '', form: {} });

const fetchQuestionnaires = async () => {
  try {
    const res = await getQuestionnaires();
    questionnaires.value = res.data || res;
    if (questionnaires.value.length > 0 && !selectedQuestionnaire.value) {
      handleSelectQuestionnaire(questionnaires.value[0]);
    }
  } catch (e) { ElMessage.error('加载问卷列表失败'); }
};

const fetchConstitutions = async () => {
  try {
    const res = await getConstitutions();
    constitutions.value = res.data || res;
  } catch (e) { ElMessage.error('加载体质定义失败'); }
};

onMounted(() => {
  fetchQuestionnaires();
  fetchConstitutions();
});

const handleSelectQuestionnaire = async (row) => {
  if (!row || !row.id) {
    selectedQuestionnaire.value = null;
    return;
  }
  try {
    const res = await getQuestionnaireById(row.id);
    selectedQuestionnaire.value = res.data || res;
  } catch (e) { ElMessage.error('加载问卷详情失败'); }
};

const refreshCurrentQuestionnaire = () => { if (selectedQuestionnaire.value) handleSelectQuestionnaire(selectedQuestionnaire.value); }

// Questionnaire CRUD
const handleCreateQuestionnaire = () => { dialog.title = '新增问卷'; dialog.type = 'questionnaire'; dialog.form = { title: '', description: '', isActive: true }; dialog.visible = true; };
const handleEditQuestionnaire = (row) => { dialog.title = '编辑问卷'; dialog.type = 'questionnaire'; dialog.form = { ...row }; dialog.visible = true; };

// Question CRUD
const handleCreateQuestion = () => { dialog.title = '新增题目'; dialog.type = 'question'; dialog.form = { text: '', displayOrder: (selectedQuestionnaire.value.questions?.length || 0) + 1 }; dialog.visible = true; };
const handleEditQuestion = (question) => { dialog.title = '编辑题目'; dialog.type = 'question'; dialog.form = { ...question }; dialog.visible = true; };
const handleDeleteQuestion = async (id) => { try { await deleteQuestion(id); ElMessage.success('删除成功'); refreshCurrentQuestionnaire(); } catch (e) { ElMessage.error('删除失败'); } };

// Option CRUD
const handleCreateOption = (question) => { dialog.title = '新增选项'; dialog.type = 'option'; dialog.form = { text: '', score: 0, constitutionType: null, questionId: question.id }; dialog.visible = true; };
const handleEditOption = (question, option) => { dialog.title = '编辑选项'; dialog.type = 'option'; dialog.form = { ...option, questionId: question.id }; dialog.visible = true; };
const handleDeleteOption = async (id) => { try { await deleteOption(id); ElMessage.success('删除成功'); refreshCurrentQuestionnaire(); } catch (e) { ElMessage.error('删除失败'); } };

// Constitution CRUD
const handleEditConstitution = (row) => { dialog.title = `编辑 - ${row.name}`; dialog.type = 'constitution'; dialog.form = { ...row }; dialog.visible = true; };

// Unified Submit Logic
const submitForm = async () => {
  try {
    const { type, form } = dialog;
    if (type === 'questionnaire') { form.id ? await updateQuestionnaire(form.id, form) : await createQuestionnaire(form); fetchQuestionnaires(); }
    else if (type === 'question') { form.id ? await updateQuestion(form.id, form) : await addQuestion(selectedQuestionnaire.value.id, form); refreshCurrentQuestionnaire(); }
    else if (type === 'option') { form.id ? await updateOption(form.id, form) : await addOption(form.questionId, form); refreshCurrentQuestionnaire(); }
    else if (type === 'constitution') { await createOrUpdateConstitution(form); fetchConstitutions(); }
    ElMessage.success('操作成功');
    dialog.visible = false;
  } catch (e) { ElMessage.error('操作失败'); }
};

</script>

<style scoped>
.assessments-management-container { padding: 2rem; }
.page-header h1 { font-size: 2rem; font-weight: bold; }
.subtitle { font-size: 1rem; color: #6c757d; margin-top: 0.5rem; margin-bottom: 1.5rem; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.question-title { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.options-table { margin-bottom: 1rem; }
.add-option-btn { width: 100%; }
</style>
