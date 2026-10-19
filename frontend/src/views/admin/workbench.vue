<template>
  <div class="page-stack">
    <PageHeader
      eyebrow="Workbench"
      title="待办工作台"
      :description="scopeText"
    >
      <template #actions>
        <span
          class="live-badge"
          :class="{ ticking: refreshing }"
        >
          <i></i>
          {{ lastUpdatedText }}
        </span>
        <el-button @click="load">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </template>
    </PageHeader>

    <section class="soft-grid">
      <StatCard
        label="待处理沟通"
        :value="block('pendingChat').total"
        icon="ChatLineSquare"
        color="var(--primary)"
      />
      <StatCard
        label="逾期随访"
        :value="block('overdueFollow').total"
        icon="Bell"
        color="var(--danger)"
      />
      <StatCard
        label="待填写评估结论"
        :value="block('pendingEvaluation').total"
        icon="EditPen"
        color="var(--warning)"
      />
      <StatCard
        label="本周中/重度测评"
        :value="block('weekRisk').total"
        icon="Warning"
        color="var(--accent)"
      />
    </section>

    <section class="soft-grid cols-2">
      <div class="panel">
        <div class="panel-head">
          <div>
            <div class="panel-title">待处理沟通</div>
            <div class="panel-subtitle">
              学生发来且尚未回复的消息
            </div>
          </div>
          <el-button
            text
            type="primary"
            @click="go('/admin/chat')"
          >
            前往沟通中心
          </el-button>
        </div>

        <div class="panel-body">
          <el-table
            v-if="block('pendingChat').rows.length"
            :data="block('pendingChat').rows"
            size="small"
          >
            <el-table-column label="学生" min-width="140">
              <template #default="{ row }">
                <el-button
                  text
                  type="primary"
                  @click="openProfile(row.studentId)"
                >
                  {{ row.studentName || row.studentUsername }}
                </el-button>
              </template>
            </el-table-column>
            <el-table-column
              prop="unread"
              label="未读"
              width="70"
              align="center"
            />
            <el-table-column label="最近消息" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.lastContent }}
              </template>
            </el-table-column>
            <el-table-column
              prop="lastTime"
              label="时间"
              width="150"
              :formatter="timeColumnFormatter"
            />
          </el-table>
          <div v-else class="todo-empty">暂无待处理沟通</div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <div>
            <div class="panel-title">逾期随访</div>
            <div class="panel-subtitle">
              随访日期已过且尚未完成
            </div>
          </div>
          <el-button
            text
            type="primary"
            @click="go('/admin/follow')"
          >
            前往随访管理
          </el-button>
        </div>

        <div class="panel-body">
          <el-table
            v-if="block('overdueFollow').rows.length"
            :data="block('overdueFollow').rows"
            size="small"
          >
            <el-table-column label="学生" min-width="140">
              <template #default="{ row }">
                <el-button
                  text
                  type="primary"
                  @click="openProfile(row.userId)"
                >
                  {{ row.studentName || row.studentUsername }}
                </el-button>
              </template>
            </el-table-column>
            <el-table-column
              prop="followDate"
              label="随访日期"
              width="120"
            />
            <el-table-column
              prop="followType"
              label="方式"
              width="80"
            />
          </el-table>
          <div v-else class="todo-empty">暂无逾期随访</div>
        </div>
      </div>
    </section>

    <section class="soft-grid cols-2">
      <div class="panel">
        <div class="panel-head">
          <div>
            <div class="panel-title">待填写评估结论</div>
            <div class="panel-subtitle">
              中/重度测评尚未填写评估结论与关怀建议
            </div>
          </div>
          <el-button
            text
            type="primary"
            @click="go('/admin/records')"
          >
            前往测评记录
          </el-button>
        </div>

        <div class="panel-body">
          <el-table
            v-if="block('pendingEvaluation').rows.length"
            :data="block('pendingEvaluation').rows"
            size="small"
          >
            <el-table-column label="学生" min-width="130">
              <template #default="{ row }">
                <el-button
                  text
                  type="primary"
                  @click="openProfile(row.userId)"
                >
                  {{ row.studentName || row.studentUsername }}
                </el-button>
              </template>
            </el-table-column>
            <el-table-column
              prop="scaleName"
              label="量表"
              min-width="140"
            />
            <el-table-column
              prop="stdScore"
              label="标准分"
              width="80"
              align="center"
            />
            <el-table-column label="等级" width="120">
              <template #default="{ row }">
                <StatusTag :text="row.level" />
              </template>
            </el-table-column>
            <el-table-column
              prop="createTime"
              label="测评时间"
              width="150"
              :formatter="timeColumnFormatter"
            />
          </el-table>
          <div v-else class="todo-empty">
            评估结论已全部填写
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <div>
            <div class="panel-title">本周新增中/重度测评</div>
            <div class="panel-subtitle">
              本周一至今筛查出的风险测评
            </div>
          </div>
          <el-button
            text
            type="primary"
            @click="go('/admin/records')"
          >
            前往测评记录
          </el-button>
        </div>

        <div class="panel-body">
          <el-table
            v-if="block('weekRisk').rows.length"
            :data="block('weekRisk').rows"
            size="small"
          >
            <el-table-column label="学生" min-width="130">
              <template #default="{ row }">
                <el-button
                  text
                  type="primary"
                  @click="openProfile(row.userId)"
                >
                  {{ row.studentName || row.studentUsername }}
                </el-button>
              </template>
            </el-table-column>
            <el-table-column
              prop="scaleName"
              label="量表"
              min-width="140"
            />
            <el-table-column
              prop="stdScore"
              label="标准分"
              width="80"
              align="center"
            />
            <el-table-column label="等级" width="120">
              <template #default="{ row }">
                <StatusTag :text="row.level" />
              </template>
            </el-table-column>
            <el-table-column
              prop="createTime"
              label="测评时间"
              width="150"
              :formatter="timeColumnFormatter"
            />
          </el-table>
          <div v-else class="todo-empty">
            本周暂无中/重度测评
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import { getWorkbench } from '@/api/dashboard'
import { timeColumnFormatter } from '@/utils/format'

/** 实时刷新间隔（毫秒） */
const REFRESH_INTERVAL = 10000

const router = useRouter()
const data = ref({})
const empty = { total: 0, rows: [] }

const block = key =>
  (data.value && data.value[key]) || empty

const scopeText = computed(() =>
  data.value.scope === 'all'
    ? '全局待办：待处理沟通、逾期随访与风险测评一览，点击学生可查看个案档案。'
    : '我的待办：仅统计我负责的学生（含尚未指派负责教师的学生），点击学生可查看个案档案。'
)

// 实时刷新：轮询 + 窗口聚焦/页面可见时立即刷新，保证待处理沟通等数据无需手动刷新
const refreshing = ref(false)
const lastUpdated = ref(null)
let timer = null
let inFlight = false

const lastUpdatedText = computed(() => {
  if (refreshing.value) return '正在更新…'
  if (!lastUpdated.value) return '实时更新'
  const d = lastUpdated.value
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `实时更新 · ${hh}:${mm}:${ss}`
})

async function load() {
  if (inFlight) return
  inFlight = true
  refreshing.value = true
  try {
    const res = await getWorkbench()
    data.value = res.data || {}
    lastUpdated.value = new Date()
  } finally {
    inFlight = false
    refreshing.value = false
  }
}

function onFocus() {
  load()
}

function onVisibility() {
  if (!document.hidden) load()
}

function go(path) {
  router.push(path)
}

function openProfile(studentId) {
  router.push({
    path: '/admin/student-profile',
    query: { studentId }
  })
}

onMounted(() => {
  load()
  timer = setInterval(load, REFRESH_INTERVAL)
  window.addEventListener('focus', onFocus)
  document.addEventListener('visibilitychange', onVisibility)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('focus', onFocus)
  document.removeEventListener('visibilitychange', onVisibility)
})
</script>

<style scoped>
.todo-empty {
  padding: 28px 0;
  color: var(--text-3);
  font-size: 13px;
  text-align: center;
}

.live-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-right: 10px;
  padding: 5px 11px;
  border: 1px solid #d9eee5;
  border-radius: 999px;
  background: #f4fbf7;
  color: #36a269;
  font-size: 12px;
  white-space: nowrap;
}

.live-badge i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #36a269;
  box-shadow: 0 0 0 3px rgba(54, 162, 105, 0.12);
}

.live-badge.ticking i {
  animation: live-pulse 0.9s ease-in-out infinite;
}

@keyframes live-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}
</style>
