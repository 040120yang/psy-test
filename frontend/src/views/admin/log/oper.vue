<template>
  <div class="page-stack">
    <PageHeader
      eyebrow="Audit"
      title="操作日志"
      description="查看管理端接口操作和异常信息。"
    />

    <section class="panel">
      <div class="panel-head">
        <div>
          <div class="panel-title">操作记录</div>
          <div class="panel-subtitle">最多显示最近 200 条</div>
        </div>
        <el-button @click="load">刷新</el-button>
      </div>

      <div class="panel-body">
        <el-table v-if="rows.length" :data="rows" stripe>
          <el-table-column prop="title" label="模块" min-width="150" />

          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              {{ businessTypeText[row.businessType] || '其他' }}
            </template>
          </el-table-column>

          <el-table-column prop="operName" label="操作人" width="120" />
          <el-table-column
            prop="operUrl"
            label="请求地址"
            min-width="220"
          />
          <el-table-column prop="operIp" label="IP地址" width="140" />

          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <StatusTag :text="row.status === 0 ? '成功' : '异常'" />
            </template>
          </el-table-column>

          <el-table-column
            prop="errorMsg"
            label="异常信息"
            min-width="180"
            show-overflow-tooltip
          />
          <el-table-column
            prop="operTime"
            label="操作时间"
            width="180"
            align="center"
            header-align="center"
            :formatter="timeColumnFormatter"
          />
        </el-table>

        <EmptyPanel v-else title="暂无操作日志" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listOperLogs } from '@/api/business'
import { timeColumnFormatter } from '@/utils/format'

const rows = ref([])

const businessTypeText = {
  0: '其他',
  1: '新增',
  2: '修改',
  3: '删除'
}

async function load() {
  const res = await listOperLogs()
  rows.value = res.rows || []
}

onMounted(load)
</script>
