<template>
  <div class="page-stack">
    <PageHeader
      eyebrow="Follow-up Management"
      title="随访管理"
      description="查看随访任务、完成随访并设置下次随访日期。"
    >
      <template #actions>
        <el-button type="primary" @click="openAdd">
          <el-icon><Plus /></el-icon>新增随访
        </el-button>
      </template>
    </PageHeader>

    <section class="panel">
      <div class="panel-head">
        <el-select
          v-model="status"
          clearable
          placeholder="全部状态"
          style="width: 180px"
          @change="search"
        >
          <el-option label="待随访" value="待随访" />
          <el-option label="已完成" value="已完成" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>

      <div class="panel-body">
        <el-table v-if="rows.length" :data="rows" stripe>
          <el-table-column
            prop="username"
            label="用户名"
            min-width="140"
            show-overflow-tooltip
          />
          <el-table-column
            prop="nickname"
            label="姓名"
            min-width="120"
            show-overflow-tooltip
          />
          <el-table-column label="性别" width="80" align="center">
            <template #default="{ row }">
              {{ row.sex === '0' ? '男' : row.sex === '1' ? '女' : '未知' }}
            </template>
          </el-table-column>
          <el-table-column label="年龄" width="80" align="center">
            <template #default="{ row }">
              {{ row.age != null ? row.age : '-' }}
            </template>
          </el-table-column>
          <el-table-column
            prop="followDate"
            label="随访日期"
            width="120"
          />
          <el-table-column prop="followType" label="方式" width="90" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <StatusTag :text="row.status" />
            </template>
          </el-table-column>
          <el-table-column
            prop="symptomScore"
            label="状态评分"
            width="100"
          />
          <el-table-column
            prop="doctorNote"
            label="心理教师记录"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column
            prop="nextFollowDate"
            label="下次随访"
            width="140"
            align="center"
          />
          <el-table-column
            label="操作"
            width="230"
            align="center"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button
                v-if="row.status !== '已完成'"
                text
                type="primary"
                @click="openFinish(row)"
              >
                完成随访
              </el-button>
              <el-button text @click="openEdit(row)">编辑</el-button>
              <el-button text type="danger" @click="remove(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <EmptyPanel v-else title="暂无随访任务" />

        <el-pagination
          v-if="total"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </section>

    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑随访' : '新增随访'"
      width="680px"
    >
      <el-form :model="form" label-position="top">
        <div class="form-grid">
          <el-form-item label="学生">
            <el-select
              v-model="form.userId"
              filterable
              clearable
              placeholder="请选择学生"
              :loading="studentsLoading"
              style="width: 100%"
              @change="handleStudentChange"
            >
              <el-option
                v-for="item in students"
                :key="item.userId"
                :label="`${item.nickname}(${item.username})`"
                :value="item.userId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="测评记录">
            <el-select
              v-model="form.recordId"
              filterable
              clearable
              placeholder="请先选择学生"
              :disabled="!form.userId"
              :loading="recordsLoading"
              style="width: 100%"
            >
              <el-option
                v-for="item in recordOptions"
                :key="item.recordId"
                :label="`#${item.recordId} · ${item.scaleName || '未知量表'} · ${item.createTime || '无日期'}`"
                :value="item.recordId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="随访日期">
            <el-date-picker
              v-model="form.followDate"
              type="date"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="随访方式">
            <el-select v-model="form.followType" style="width: 100%">
              <el-option label="线上" value="线上" />
              <el-option label="电话" value="电话" />
              <el-option label="面谈" value="面谈" />
            </el-select>
          </el-form-item>

          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="待随访" value="待随访" />
              <el-option label="已完成" value="已完成" />
            </el-select>
          </el-form-item>

          <el-form-item label="状态评分">
            <el-input-number
              v-model="form.symptomScore"
              :min="0"
              :max="10"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <el-form-item label="心理教师记录">
          <el-input
            v-model="form.doctorNote"
            type="textarea"
            :rows="3"
          />
        </el-form-item>

        <el-form-item label="下次随访日期">
          <el-date-picker
            v-model="form.nextFollowDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="finishVisible" title="完成随访" width="620px">
      <el-form label-position="top">
        <el-form-item label="状态评分（0-10）">
          <el-input-number
            v-model="finishForm.symptomScore"
            :min="0"
            :max="10"
          />
        </el-form-item>

        <el-form-item label="随访记录">
          <el-input
            v-model="finishForm.doctorNote"
            type="textarea"
            :rows="4"
          />
        </el-form-item>

        <el-form-item label="下次随访日期">
          <el-date-picker
            v-model="finishForm.nextFollowDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="primary" @click="finish">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import {
  listAdminFollows,
  addFollow,
  updateFollow,
  finishFollow,
  delFollow
} from '@/api/follow'
import { listPatient } from '@/api/patient'
import { listAllRecords } from '@/api/test'

const rows = ref([])
const status = ref('')
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const visible = ref(false)
const finishVisible = ref(false)
const form = reactive(blank())
const finishForm = reactive({
  id: null,
  doctorNote: '',
  symptomScore: 0,
  nextFollowDate: ''
})

const students = ref([])
const recordOptions = ref([])
const studentsLoading = ref(false)
const recordsLoading = ref(false)

function blank() {
  return {
    id: null,
    userId: null,
    recordId: null,
    followDate: '',
    followType: '线上',
    status: '待随访',
    symptomScore: null,
    doctorNote: '',
    nextFollowDate: ''
  }
}

async function load() {
  const res = await listAdminFollows({
    status: status.value,
    pageNum: pageNum.value,
    pageSize: pageSize.value
  })
  rows.value = res.rows || []
  total.value = res.total || rows.value.length
}

function search() {
  pageNum.value = 1
  load()
}

async function loadStudents() {
  studentsLoading.value = true
  try {
    const res = await listPatient({ pageNum: 1, pageSize: 1000 })
    const unique = new Map()
    ;(res.rows || [])
      .filter(item => item.userId != null)
      .forEach(item => unique.set(item.userId, item))
    students.value = [...unique.values()]
  } finally {
    studentsLoading.value = false
  }
}

async function loadRecords(userId) {
  recordOptions.value = []
  if (!userId) return
  recordsLoading.value = true
  try {
    const res = await listAllRecords({
      pageNum: 1,
      pageSize: 1000,
      userId
    })
    recordOptions.value = res.rows || []
  } finally {
    recordsLoading.value = false
  }
}

async function handleStudentChange(userId) {
  form.recordId = null
  await loadRecords(userId)
}

async function openAdd() {
  Object.assign(form, blank())
  recordOptions.value = []
  visible.value = true
  if (!students.value.length) {
    await loadStudents()
  }
}

async function openEdit(row) {
  Object.assign(form, blank(), row)
  visible.value = true
  if (!students.value.length) {
    await loadStudents()
  }
  await loadRecords(form.userId)
}

function openFinish(row) {
  Object.assign(finishForm, {
    id: row.id,
    doctorNote: '',
    symptomScore: row.symptomScore || 0,
    nextFollowDate: ''
  })
  finishVisible.value = true
}

async function save() {
  if (!form.userId) {
    return ElMessage.warning('请选择学生')
  }
  if (!form.recordId) {
    return ElMessage.warning('请选择测评记录')
  }

  if (form.id) {
    await updateFollow(form)
  } else {
    await addFollow(form)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function finish() {
  await finishFollow(finishForm)
  ElMessage.success('随访已完成')
  finishVisible.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm('确认删除该随访任务吗？', '删除确认', {
    type: 'warning'
  })
  await delFollow(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => {
  load()
  loadStudents()
})
</script>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

@media (max-width: 620px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
