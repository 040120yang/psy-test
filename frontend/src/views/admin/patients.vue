<template>
  <div class="page-stack">
    <PageHeader
      eyebrow="Student Records"
      title="学生管理"
      :description="
        isAdmin
          ? '维护学生档案、登录账号、负责教师与心理关注信息。'
          : '维护负责学生的心理关注信息；登录账号与负责教师由系统管理员管理。'
      "
    >
      <template #actions>
        <el-button v-if="isAdmin" type="primary" @click="openAdd">
          <el-icon><Plus /></el-icon>新增学生
        </el-button>
      </template>
    </PageHeader>

    <section class="panel">
      <div class="panel-head">
        <el-input
          v-model="query.patientName"
          clearable
          placeholder="学生姓名"
          style="width: 200px"
        />
        <el-input
          v-model="query.phone"
          clearable
          placeholder="联系电话"
          style="width: 200px"
        />
        <el-button type="primary" @click="search">查询</el-button>
      </div>

      <div class="panel-body">
        <el-table v-if="rows.length" :data="rows" stripe>
          <el-table-column
            prop="patientName"
            label="姓名"
            min-width="110"
          />
          <el-table-column
            prop="username"
            label="登录账号"
            min-width="130"
          />
          <el-table-column label="性别" width="80">
            <template #default="{ row }">
              {{ row.sex === '0' ? '男' : row.sex === '1' ? '女' : '未知' }}
            </template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" width="80" />
          <el-table-column prop="phone" label="联系电话" min-width="130" />
          <el-table-column prop="tag" label="标签" min-width="110">
            <template #default="{ row }">
              <el-tag v-if="row.tag" effect="plain">{{ row.tag }}</el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="负责教师" min-width="120">
            <template #default="{ row }">
              <span v-if="row.teacherName">{{ row.teacherName }}</span>
              <el-tag v-else size="small" type="warning" effect="plain">
                待分配
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="medicalHistory"
            label="既往经历/主要困扰"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column
            label="操作"
            :width="isAdmin ? 240 : 180"
            align="center"
            header-align="center"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button text @click="openProfile(row)">个案档案</el-button>
              <el-button text type="primary" @click="openEdit(row)">
                编辑
              </el-button>
              <el-button
                v-if="isAdmin"
                text
                type="danger"
                @click="remove(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <EmptyPanel v-else title="暂无学生档案" />

        <el-pagination
          v-if="total"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
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
      :title="form.patientId ? '编辑学生' : '新增学生'"
      width="720px"
    >
      <el-form :model="form" label-position="top">
        <div class="form-grid">
          <el-form-item label="姓名">
            <el-input v-model="form.patientName" />
          </el-form-item>

          <el-form-item label="性别">
            <el-select v-model="form.sex" style="width: 100%">
              <el-option label="男" value="0" />
              <el-option label="女" value="1" />
              <el-option label="未知" value="2" />
            </el-select>
          </el-form-item>

          <el-form-item label="登录账号">
            <el-input
              v-model="form.username"
              :disabled="!!form.userId"
              placeholder="不填则使用学号或手机号"
            />
          </el-form-item>

          <el-form-item v-if="!form.userId" label="初始密码">
            <el-input
              v-model="form.password"
              type="password"
              show-password
            />
          </el-form-item>

          <el-form-item label="年龄">
            <el-input-number
              v-model="form.age"
              :min="1"
              :max="120"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="联系电话">
            <el-input v-model="form.phone" />
          </el-form-item>

          <el-form-item label="学号/证件号">
            <el-input v-model="form.idCard" />
          </el-form-item>

          <el-form-item label="标签">
            <el-input v-model="form.tag" />
          </el-form-item>

          <el-form-item label="负责教师">
            <el-select
              v-model="form.teacherId"
              :disabled="!isAdmin"
              clearable
              filterable
              :placeholder="
                isAdmin ? '不指定则显示为待分配' : '负责教师由系统管理员指派'
              "
              style="width: 100%"
            >
              <el-option
                v-for="d in doctors"
                :key="d.userId"
                :label="`${d.nickname}(${d.username})`"
                :value="d.userId"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="联系地址">
          <el-input v-model="form.address" />
        </el-form-item>

        <el-form-item label="既往经历/主要困扰">
          <el-input
            v-model="form.medicalHistory"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { getUser } from '@/utils/auth'
import {
  listPatient,
  addPatient,
  updatePatient,
  delPatient
} from '@/api/patient'
import { listDoctors } from '@/api/system'

const router = useRouter()

const isAdmin = computed(() => getUser()?.roles?.[0] === 'admin')

function openProfile(row) {
  if (!row.userId) {
    return ElMessage.warning('该学生未关联登录账号，暂无个案档案')
  }
  router.push({
    path: '/admin/student-profile',
    query: { studentId: row.userId }
  })
}

const rows = ref([])
const total = ref(0)
const visible = ref(false)
const doctors = ref([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  patientName: '',
  phone: ''
})
const form = reactive(blank())

function blank() {
  return {
    patientId: null,
    userId: null,
    username: '',
    password: '123456',
    patientName: '',
    sex: '2',
    age: null,
    phone: '',
    idCard: '',
    address: '',
    medicalHistory: '',
    tag: '',
    teacherId: null
  }
}

async function load() {
  const res = await listPatient(query)
  rows.value = res.rows || []
  total.value = res.total || rows.value.length
}

// 查询时重置到第一页，避免停留在靠后的页码导致看不到结果
function search() {
  query.pageNum = 1
  load()
}

function reset() {
  Object.assign(form, blank())
}

function openAdd() {
  reset()
  visible.value = true
}

function openEdit(row) {
  Object.assign(form, blank(), row)
  visible.value = true
}

async function save() {
  if (!form.patientName) {
    return ElMessage.warning('请输入学生姓名')
  }
  if (!form.userId && !form.username && !form.idCard && !form.phone) {
    return ElMessage.warning('请填写登录账号、学号或手机号')
  }
  if (!form.userId && form.password.length < 6) {
    return ElMessage.warning('初始密码长度不能少于6位')
  }

  if (form.patientId) {
    await updatePatient(form)
  } else {
    await addPatient(form)
  }
  ElMessage.success('保存成功，学生账户已同步')
  visible.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(
    `确认删除学生“${row.patientName}”吗？`,
    '删除确认',
    { type: 'warning' }
  )
  await delPatient(row.patientId)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  await load()
  try {
    const res = await listDoctors()
    doctors.value = res.data || []
  } catch (e) {}
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
