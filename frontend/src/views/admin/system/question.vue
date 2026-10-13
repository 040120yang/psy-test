<template>
  <div class="page-stack">
    <PageHeader eyebrow="Question Bank" title="题目管理" description="按量表维护题号、题干和反向计分设置。">
      <template #actions>
        <el-button @click="downloadTemplate"><el-icon><Download /></el-icon>下载模板</el-button>
        <el-button @click="openImport"><el-icon><Upload /></el-icon>批量导入</el-button>
        <el-button @click="exportCsv"><el-icon><Download /></el-icon>导出</el-button>
        <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>新增题目</el-button>
      </template>
    </PageHeader>
    <section class="panel"><div class="panel-head"><el-select v-model="query.scaleId" placeholder="选择量表" style="width:200px" @change="search"><el-option v-for="s in scales" :key="s.scaleId" :label="s.scaleName" :value="s.scaleId"/></el-select><el-input v-model="query.content" clearable placeholder="题干关键词" style="width:240px"/><el-button type="primary" @click="search">查询</el-button></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe><el-table-column prop="questionId" label="ID" width="80"/><el-table-column prop="scaleName" label="所属量表" min-width="150"/><el-table-column prop="sortNo" label="题号" width="80"/><el-table-column prop="content" label="题目内容" min-width="360"/><el-table-column label="反向计分" width="100"><template #default="{row}"><el-tag v-if="row.reverseFlag===1" type="warning" effect="plain">是</el-tag><el-tag v-else effect="plain">否</el-tag></template></el-table-column><el-table-column label="操作" width="140"><template #default="{row}"><el-button text type="primary" @click="openEdit(row)">编辑</el-button><el-button text type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table><EmptyPanel v-else title="暂无题目"/><el-pagination v-if="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" :total="total" @current-change="load" @size-change="search"/></div></section>
    <el-dialog v-model="visible" :title="form.questionId?'编辑题目':'新增题目'" width="680px"><el-form :model="form" label-position="top"><div class="form-grid"><el-form-item label="所属量表"><el-select v-model="form.scaleId" style="width:100%"><el-option v-for="s in scales" :key="s.scaleId" :label="s.scaleName" :value="s.scaleId"/></el-select></el-form-item><el-form-item label="题号"><el-input-number v-model="form.sortNo" :min="1" style="width:100%"/></el-form-item><el-form-item label="反向计分"><el-switch v-model="form.reverseFlag" :active-value="1" :inactive-value="0"/></el-form-item></div><el-form-item label="题目内容"><el-input v-model="form.content" type="textarea" :rows="4"/></el-form-item></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
    <el-dialog v-model="importVisible" title="批量导入题目" width="560px">
      <el-form label-position="top">
        <el-form-item label="目标量表"><el-select v-model="importScaleId" style="width:100%"><el-option v-for="s in scales" :key="s.scaleId" :label="s.scaleName" :value="s.scaleId"/></el-select></el-form-item>
        <el-form-item label="导入方式"><el-radio-group v-model="importMode"><el-radio label="append">追加（保留原有题目）</el-radio><el-radio label="overwrite">覆盖（先清空再导入）</el-radio></el-radio-group></el-form-item>
        <el-form-item label="CSV 文件"><el-upload :auto-upload="false" :limit="1" accept=".csv" :on-change="onFileChange" :file-list="fileList" drag><el-icon><UploadFilled /></el-icon><div class="el-upload__text">拖拽 CSV 文件到此处，或<em>点击上传</em></div><template #tip><div class="el-upload__tip">格式：题号,题目内容,反向计分（0/1）</div></template></el-upload></el-form-item>
      </el-form>
      <template #footer><el-button @click="importVisible=false">取消</el-button><el-button type="primary" :disabled="!importFile" @click="doImport">开始导入</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listQuestion, addQuestion, updateQuestion, delQuestion, listScale, importQuestion } from '@/api/system'
import request from '@/utils/request'
const rows=ref([]); const total=ref(0); const scales=ref([]); const visible=ref(false); const importVisible=ref(false); const importScaleId=ref(null); const importMode=ref('append'); const importFile=ref(null); const fileList=ref([]); const query=reactive({pageNum:1,pageSize:10,scaleId:null,content:''}); const form=reactive(blank())
function blank(){return {questionId:null,scaleId:null,sortNo:1,content:'',reverseFlag:0}}
async function load(){const res=await listQuestion(query);rows.value=res.rows||[];total.value=res.total||rows.value.length}
function search(){query.pageNum=1;load()}
function openAdd(){Object.assign(form,blank(),{scaleId:query.scaleId||scales.value[0]?.scaleId});visible.value=true}
function openEdit(row){Object.assign(form,blank(),row);visible.value=true}
async function save(){if(!form.content)return ElMessage.warning('请输入题目内容');form.questionId?await updateQuestion(form):await addQuestion(form);ElMessage.success('保存成功');visible.value=false;load()}
async function remove(row){await ElMessageBox.confirm('确认删除该题目吗？','删除确认',{type:'warning'});await delQuestion(row.questionId);ElMessage.success('已删除');load()}
function saveBlob(res, filename){const blob=new Blob([res],{type:'text/csv;charset=utf-8'});const link=document.createElement('a');link.href=URL.createObjectURL(blob);link.download=filename;link.click();URL.revokeObjectURL(link.href)}
function downloadTemplate(){request({url:'/system/question/template',method:'get',responseType:'blob'}).then(res=>saveBlob(res,'question_template.csv')).catch(()=>{})}
function openImport(){importScaleId.value=query.scaleId||scales.value[0]?.scaleId;importMode.value='append';importFile.value=null;fileList.value=[];importVisible.value=true}
function onFileChange(file){importFile.value=file.raw;fileList.value=[file]}
function exportCsv(){if(!query.scaleId)return ElMessage.warning('请先选择量表');request({url:'/system/question/export?scaleId='+query.scaleId,method:'get',responseType:'blob'}).then(res=>saveBlob(res,'questions_'+query.scaleId+'.csv')).catch(()=>{})}
async function doImport(){if(!importScaleId.value)return ElMessage.warning('请选择目标量表');if(!importFile.value)return ElMessage.warning('请选择 CSV 文件');await importQuestion(importScaleId.value,importMode.value,importFile.value);ElMessage.success('导入完成');importVisible.value=false;load()}
onMounted(async()=>{const res=await listScale({pageNum:1,pageSize:100});scales.value=res.rows||[];if(scales.value.length)query.scaleId=scales.value[0].scaleId;load()})
</script>
<style scoped>
.form-grid{display:grid;grid-template-columns:2fr 1fr 1fr;gap:0 16px}@media(max-width:620px){.form-grid{grid-template-columns:1fr}}
</style>
