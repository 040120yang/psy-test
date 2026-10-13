<template>
  <div class="page-stack">
    <PageHeader
      eyebrow="Data Backup"
      title="数据备份与导出"
      description="查看各表数据量，支持单表 CSV 导出与整库打包导出，并查看恢复指引。"
    >
      <template #actions>
        <el-button type="primary" @click="exportAll">
          <el-icon><Download /></el-icon>
          整库打包导出
        </el-button>
      </template>
    </PageHeader>

    <section class="panel">
      <div class="panel-head">
        <h3 class="panel-title">数据表清单</h3>
        <span class="panel-actions">
          <span class="table-count">
            共 {{ tables.length }} 张表
          </span>
          <el-button text @click="load">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </span>
      </div>

      <div class="panel-body">
        <el-table v-if="tables.length" :data="tables" stripe>
          <el-table-column
            prop="tableName"
            label="表名"
            min-width="200"
          />
          <el-table-column
            prop="tableRows"
            label="行数"
            width="110"
          />
          <el-table-column label="数据大小" width="130">
            <template #default="{ row }">
              {{ formatKb(row.dataKb) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="tableComment"
            label="备注"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column label="操作" width="140" fixed="right" align="center" header-align="center">
            <template #default="{ row }">
              <el-button
                text
                type="primary"
                :loading="downloading === row.tableName"
                @click="exportTable(row.tableName)"
              >
                导出 CSV
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <EmptyPanel v-else title="暂无可导出的表" />
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h3 class="panel-title">恢复指引</h3>
      </div>

      <div class="panel-body">
        <div v-if="guide.database" class="guide-block">
          <p>
            数据库：<strong>{{ guide.database }}</strong>
          </p>
          <p v-if="guide.csvNote">{{ guide.csvNote }}</p>
          <ol class="guide-list">
            <li
              v-for="(s, i) in guide.restoreSteps"
              :key="i"
            >
              {{ s }}
            </li>
          </ol>
        </div>
        <EmptyPanel v-else title="加载中..." />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listBackupTables, backupGuide } from '@/api/system'
import request from '@/utils/request'

const tables = ref([])
const guide = ref({})
const downloading = ref('')

function formatKb(kb) {
  const n = Number(kb)
  if (!n) return '-'
  if (n < 1024) return n + ' KB'
  return (n / 1024).toFixed(2) + ' MB'
}

function saveBlob(res, filename) {
  const blob = new Blob([res])
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

async function load() {
  try {
    const res = await listBackupTables()
    tables.value = res.data || []
  } catch (e) {}
}

async function loadGuide() {
  try {
    const res = await backupGuide()
    guide.value = res.data || {}
  } catch (e) {}
}

async function exportTable(table) {
  downloading.value = table
  try {
    const res = await request({
      url: '/system/backup/exportTable',
      method: 'get',
      params: { table },
      responseType: 'blob'
    })
    saveBlob(res, table + '.csv')
    ElMessage.success('已导出 ' + table + '.csv')
  } catch (e) {
  } finally {
    downloading.value = ''
  }
}

async function exportAll() {
  try {
    const res = await request({
      url: '/system/backup/exportAll',
      method: 'get',
      responseType: 'blob'
    })
    saveBlob(res, (guide.value.database || 'backup') + '_backup.zip')
    ElMessage.success('已导出整库备份压缩包')
  } catch (e) {}
}

onMounted(() => {
  load()
  loadGuide()
})
</script>

<style scoped>
.panel-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.panel-actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.table-count {
  color: #7a8a99;
  font-size: 13px;
}

.guide-block p {
  margin: 6px 0;
  color: #33414f;
  font-size: 14px;
}

.guide-list {
  margin: 8px 0 0;
  padding-left: 20px;
  color: #5b6b7b;
  font-size: 13px;
  line-height: 1.9;
}
</style>
