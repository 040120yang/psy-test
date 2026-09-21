<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-title">
        <h2>注册公众用户</h2>
        <p>区域智能诊疗辅助诊断系统 · 用户端</p>
      </div>
      <el-form ref="registerForm" :model="registerForm" :rules="registerRules" size="medium">
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名（登录账号）"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="昵称（选填，默认同用户名）"
            prefix-icon="el-icon-postcard"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码（不少于 6 位）"
            prefix-icon="el-icon-lock"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="el-icon-lock"
            show-password
            autocomplete="new-password"
            @keyup.enter.native="handleRegister"
          />
        </el-form-item>
        <el-form-item prop="phone" label-width="0" class="half-item">
          <el-input
            v-model="registerForm.phone"
            placeholder="手机号（选填）"
            prefix-icon="el-icon-mobile-phone"
          />
        </el-form-item>
        <el-form-item label-width="0" class="half-item">
          <el-input
            v-model="registerForm.age"
            placeholder="年龄（选填）"
            prefix-icon="el-icon-medal"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-btn" :loading="loading" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <el-button type="text" @click="$router.push('/login')">返回登录</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { register } from '@/api/login'

export default {
  name: 'Register',
  data() {
    return {
      registerForm: {
        username: '',
        nickname: '',
        password: '',
        confirmPassword: '',
        phone: '',
        age: null,
        sex: '2'
      },
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度 3-20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.registerForm.password) {
                callback(new Error('两次输入的密码不一致'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ],
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }]
      },
      loading: false
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (!valid) return
        this.loading = true
        register({
          username: this.registerForm.username,
          nickname: this.registerForm.nickname,
          password: this.registerForm.password,
          phone: this.registerForm.phone,
          age: this.registerForm.age,
          sex: '2'
        })
          .then(() => {
            this.$message.success('注册成功，请登录')
            this.$router.push('/login')
          })
          .catch(() => {})
          .finally(() => {
            this.loading = false
          })
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  width: 420px;
  background: #fff;
  border-radius: 8px;
  padding: 36px 40px 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
.register-title {
  text-align: center;
  margin-bottom: 24px;
}
.register-title h2 {
  margin: 0 0 6px;
  color: #333;
  font-size: 22px;
}
.register-title p {
  margin: 0;
  color: #999;
  font-size: 12px;
}
.extra-row {
  display: flex;
  justify-content: space-between;
}
.half-item {
  display: inline-block;
  width: 48%;
}
.register-btn {
  width: 100%;
}
.register-footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
}
</style>
