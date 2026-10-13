<template>
  <div class="page-stack">
    <PageHeader
      eyebrow="My Reports"
      title="我的测评记录"
      description="查看历次测评的标准分、风险等级和建议，以及心理教师填写的评估结论与关怀建议。"
    >
      <template #actions>
        <el-button type="primary" @click="$router.push('/portal/scales')">
          开始新测评
        </el-button>
      </template>
    </PageHeader>

    <section class="panel">
      <div class="panel-head">
        <div>
          <div class="panel-title">记录列表</div>
          <div class="panel-subtitle">共 {{ total }} 条记录</div>
        </div>
        <el-input
          v-model="query.scaleName"
          clearable
          placeholder="按量表名称/编码搜索"
          style="width: 240px"
          @keyup.enter="search"
        />
        <el-button @click="search">查询</el-button>
      </div>

      <div class="panel-body">
        <el-table v-if="rows.length" :data="rows" stripe>
          <el-table-column prop="scaleName" label="量表" min-width="180" />
          <el-table-column prop="scaleCode" label="编码" width="110" />
          <el-table-column
            prop="rawScore"
            label="粗分"
            width="90"
            align="center"
          />
          <el-table-column
            prop="stdScore"
            label="标准分"
            width="100"
            align="center"
          />
          <el-table-column label="等级" width="140">
            <template #default="{ row }">
              <StatusTag :text="row.level" />
            </template>
          </el-table-column>
          <el-table-column label="教师评估" width="140">
            <template #default="{ row }">
              <el-button
                v-if="hasEval(row)"
                text
                type="primary"
                @click="openEval(row)"
              >
                查看评估意见
              </el-button>
              <el-tag v-else size="small" type="info" effect="plain">
                待评估
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="测评时间" min-width="170" />
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="open(row)">查看报告</el-button>
              <el-button text type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <EmptyPanel
          v-else
          title="暂无测评记录"
          description="完成一次测评后，记录会显示在这里"
        >
          <el-button type="primary" @click="$router.push('/portal/scales')">
            开始测评
          </el-button>
        </EmptyPanel>

        <el-pagination
          v-if="total"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="load"
        />
      </div>
    </section>

    <el-dialog v-model="evalVisible" title="心理教师评估意见" width="640px">
      <div v-if="evalRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="量表">
            {{ evalRow.scaleName }}
          </el-descriptions-item>
          <el-descriptions-item label="结果等级">
            <StatusTag :text="evalRow.level" />
          </el-descriptions-item>
          <el-descriptions-item label="标准分">
            {{ evalRow.stdScore }}
          </el-descriptions-item>
          <el-descriptions-item label="测评时间">
            {{ evalRow.createTime }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">评估结论</el-divider>
        <div class="notice">
          {{ evalRow.doctorConclusion || '暂无评估结论' }}
        </div>

        <el-divider content-position="left">关怀建议</el-divider>
        <div class="notice">
          {{ evalRow.doctorAdvice || '暂无关怀建议' }}
        </div>

        <p class="text-muted" style="margin: 14px 0 0; font-size: 12px">
          以上内容由心理教师/辅导员针对本次测评填写，仅作关怀参考，不能替代专业诊断。
        </p>
      </div>

      <template #footer>
        <el-button @click="goReport">查看完整报告</el-button>
        <el-button type="primary" @click="evalVisible = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listMyRecords, delRecord } from '@/api/test'

const router = useRouter()
const rows = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  scaleName: ''
})
const evalVisible = ref(false)
const evalRow = ref(null)

async function load() {
  const res = await listMyRecords(query)
  rows.value = res.rows || []
  total.value = res.total || rows.value.length
}

// 查询：重置到第 1 页后按量表名称筛选
async function search() {
  query.pageNum = 1
  await load()
}

function open(row) {
  router.push({
    path: '/portal/result',
    query: { recordId: row.recordId }
  })
}

function hasEval(row) {
  return !!(row.doctorConclusion || row.doctorAdvice)
}

function openEval(row) {
  evalRow.value = row
  evalVisible.value = true
}

function goReport() {
  evalVisible.value = false
  open(evalRow.value)
}

async function remove(row) {
  await ElMessageBox.confirm(
    '确认删除这条测评记录吗？删除后无法恢复。',
    '删除确认',
    { type: 'warning' }
  )
  await delRecord(row.recordId)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
