<template>
  <div class="profile-page">
    <!-- 个人资料卡片 -->
    <div class="profile-card">
      <div class="profile-banner">
        <div class="avatar">
          <img v-if="user.avatar" :src="user.avatar" class="avatar-img" alt="头像" />
          <span v-else>{{ (user.nickname || user.username || 'U').charAt(0).toUpperCase() }}</span>
        </div>
        <div class="profile-meta">
          <div class="profile-name">{{ user.nickname || user.username }}</div>
          <div class="profile-sub">
            <el-tag size="mini" type="info">{{ roleName || '普通用户' }}</el-tag>
            <span class="profile-account">账号：{{ user.username }}</span>
          </div>
        </div>
        <div class="profile-actions">
          <el-button type="primary" icon="el-icon-edit" @click="openEdit">编辑资料</el-button>
          <el-button icon="el-icon-key" @click="openPwd">修改密码</el-button>
        </div>
      </div>

      <el-divider></el-divider>

      <el-row :gutter="20">
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">昵 称</div>
            <div class="info-value">{{ user.nickname || '未设置' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">性 别</div>
            <div class="info-value">{{ sexText(user.sex) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">年 龄</div>
            <div class="info-value">{{ user.age ? user.age + ' 岁' : '未设置' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">手机号</div>
            <div class="info-value">{{ user.phone || '未绑定' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">角 色</div>
            <div class="info-value">{{ roleName || '普通用户' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">注册时间</div>
            <div class="info-value">{{ user.createTime || '--' }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog title="编辑个人资料" :visible.sync="editVisible" width="480px" @closed="resetEditForm">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action=""
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            accept="image/*"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="form-avatar" alt="头像" />
            <div v-else class="form-avatar form-avatar-placeholder">
              <i class="el-icon-plus"></i>
            </div>
          </el-upload>
          <div class="avatar-tip">点击上传，可裁剪</div>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="editForm.sex">
            <el-radio label="0">男</el-radio>
            <el-radio label="1">女</el-radio>
            <el-radio label="2">保密</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="editForm.age" :min="1" :max="120" placeholder="请输入年龄"></el-input-number>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入11位手机号" maxlength="11"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editVisible = false">取 消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog title="修改登录密码" :visible.sync="pwdVisible" width="480px" @closed="resetPwdForm">
      <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-16位新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="pwdVisible = false">取 消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="submitPwd">确认修改</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getInfo, updateProfile, updatePwd } from '@/api/login'
import { getUser, setUser } from '@/utils/auth'

export default {
  name: 'PortalPersonalCenter',
  data() {
    const validatePhone = (rule, value, callback) => {
      if (value && !/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的11位手机号'))
      } else {
        callback()
      }
    }
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.pwdForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      user: getUser() || {},
      roleName: '',
      cropVisible: false,
      cropLoading: false,
      cropImg: '',
      cropper: null,
      editVisible: false,
      editLoading: false,
      editForm: { nickname: '', sex: '2', age: null, phone: '' },
      editRules: {
        nickname: [{ required: true, message: '昵称不能为空', trigger: 'blur' }],
        phone: [{ validator: validatePhone, trigger: 'blur' }]
      },
      pwdVisible: false,
      pwdLoading: false,
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      pwdRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 16, message: '新密码长度6-16位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadUser()
  },
  methods: {
    loadUser() {
      getInfo().then(res => {
        const u = res.data && res.data.user ? res.data.user : (res.user || {})
        this.user = { ...this.user, ...u }
        this.roleName = (res.data && res.data.roleName) || res.roleName || '普通用户'
        // 同步到 sessionStorage，顶栏/AI助手/个人中心都更新
        const stored = getUser() || {}
        setUser({ ...stored, nickname: this.user.nickname, username: this.user.username, avatar: this.user.avatar })
      })
    },
    // 头像上传：校验类型 → 读取为 dataURL → 弹出裁剪窗口
    beforeAvatarUpload(file) {
      const isImage = file.type.indexOf('image/') === 0
      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return false
      }
      if (file.size / 1024 / 1024 > 5) {
        this.$message.error('图片不能超过 5MB')
        return false
      }
      const reader = new FileReader()
      reader.onload = (e) => {
        const img = new Image()
        img.onload = () => {
          // 居中裁剪正方形
          const size = Math.min(img.width, img.height)
          const x = (img.width - size) / 2
          const y = (img.height - size) / 2
          const canvas = document.createElement('canvas')
          canvas.width = 200
          canvas.height = 200
          const ctx = canvas.getContext('2d')
          ctx.drawImage(img, x, y, size, size, 0, 0, 200, 200)
          const base64 = canvas.toDataURL('image/png')
          this.$set(this.editForm, 'avatar', base64)
          this.$message.success('头像已更新，点"保存"生效')
        }
        img.src = e.target.result
      }
      reader.readAsDataURL(file)
      return false // 阻止 el-upload 默认上传
    },
    sexText(sex) {
      if (sex === '0') return '男'
      if (sex === '1') return '女'
      return '保密'
    },
    openEdit() {
      this.editForm = {
        nickname: this.user.nickname || '',
        sex: this.user.sex || '2',
        age: this.user.age || null,
        phone: this.user.phone || '',
        avatar: this.user.avatar || ''
      }
      this.editVisible = true
    },
    resetEditForm() {
      if (this.$refs.editForm) this.$refs.editForm.clearValidate()
    },
    submitEdit() {
      this.$refs.editForm.validate(valid => {
        if (!valid) return
        this.editLoading = true
        updateProfile(this.editForm).then(() => {
          this.$message.success('资料修改成功')
          this.editVisible = false
          this.loadUser()
        }).finally(() => {
          this.editLoading = false
        })
      })
    },
    openPwd() {
      this.pwdVisible = true
    },
    resetPwdForm() {
      this.pwdForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
      if (this.$refs.pwdForm) this.$refs.pwdForm.clearValidate()
    },
    submitPwd() {
      this.$refs.pwdForm.validate(valid => {
        if (!valid) return
        this.pwdLoading = true
        updatePwd({
          oldPassword: this.pwdForm.oldPassword,
          newPassword: this.pwdForm.newPassword
        }).then(() => {
          this.$message.success('密码修改成功')
          this.pwdVisible = false
        }).finally(() => {
          this.pwdLoading = false
        })
      })
    }
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 900px;
  margin: 0 auto;
}
.profile-card {
  background: #fff;
  border-radius: 10px;
  padding: 28px 32px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.profile-banner {
  display: flex;
  align-items: center;
}
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(120deg, #667eea, #764ba2);
  color: #fff;
  font-size: 28px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  overflow: hidden;
  flex-shrink: 0;
}
.avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
.avatar-uploader {
  position: relative;
  cursor: pointer;
  margin-right: 20px;
}
.avatar-uploader .avatar,
.avatar-uploader .avatar-img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-uploader .avatar {
  background: linear-gradient(120deg, #667eea, #764ba2);
  color: #fff;
  font-size: 28px;
  font-weight: bold;
}
.avatar-uploader .avatar-img {
  object-fit: cover;
  border: 2px solid #e4e7ed;
}
.avatar-mask {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 22px;
  display: none;
  align-items: center;
  justify-content: center;
}
.avatar-uploader:hover .avatar-mask {
  display: flex;
}
.cropper-wrapper {
  width: 100%;
  height: 360px;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
}
.form-avatar {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  display: block;
}
.form-avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  font-size: 24px;
  background: #fbfdff;
}
.avatar-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
  display: inline-block;
}
.profile-meta {
  flex: 1;
}
.profile-name {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}
.profile-sub {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.profile-account {
  color: #909399;
  font-size: 13px;
}
.profile-actions {
  display: flex;
  gap: 10px;
}
.info-item {
  padding: 12px 0;
}
.info-label {
  color: #909399;
  font-size: 12px;
  margin-bottom: 6px;
}
.info-value {
  color: #303133;
  font-size: 15px;
}
</style>
