<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-title">
        <h2>区域智能诊疗辅助诊断系统</h2>
        <p>Regional smart diagnosis and treatment support system</p>
      </div>
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" size="medium">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            autocomplete="off"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            show-password
            autocomplete="new-password"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="code">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.code"
              placeholder="验证码"
              prefix-icon="el-icon-key"
              @keyup.enter.native="handleLogin"
            />
            <img v-if="captchaImg" :src="captchaImg" class="captcha-img" @click="getCaptcha" title="点击刷新" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">
        <!-- <p>演示账号（密码均为 123456）：</p>
        <p>admin（系统管理员） / doctor（临床医护人员） / user（公众用户）</p> -->
        <p class="register-link">
          还没有账号？
          <el-button type="text" @click="$router.push('/register')">立即注册</el-button>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { getCaptcha, login, getInfo } from '@/api/login'
import { setToken, setUser, removeToken, removeUser } from '@/utils/auth'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        code: '',
        uuid: ''
      },
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      captchaImg: '',
      loading: false
    }
  },
  created() {
    this.getCaptcha()
  },
  methods: {
    getCaptcha() {
      getCaptcha().then(res => {
        this.captchaImg = 'data:image/png;base64,' + res.data.img
        this.loginForm.uuid = res.data.uuid
        this.loginForm.code = ''
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return
        this.loading = true
        login(this.loginForm)
          .then(res => {
            setToken(res.data.token)
            // 拉取用户信息
            return getInfo()
          })
          .then(res => {
            const userInfo = res.data
            setUser({
              userId: userInfo.user.userId,
              username: userInfo.user.username,
              nickname: userInfo.user.nickname,
              roles: userInfo.roles,
              roleName: userInfo.roleName
            })
            // 按角色进入对应端：公众用户 → 用户端，医护/管理员 → 管理端
            const role = (userInfo.roles && userInfo.roles[0]) || 'user'
            this.$router.push(role === 'user' ? '/portal/home' : '/admin/dashboard')
            this.$message.success('登录成功')
          })
          .catch(() => {
            this.getCaptcha()
          })
          .finally(() => {
            this.loading = false
          })
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 8px;
  padding: 40px 40px 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
.login-title {
  text-align: center;
  margin-bottom: 28px;
}
.login-title h2 {
  margin: 0 0 6px;
  color: #333;
  font-size: 22px;
}
.login-title p {
  margin: 0;
  color: #999;
  font-size: 12px;
}
.captcha-row {
  display: flex;
  align-items: center;
}
.captcha-img {
  width: 120px;
  height: 38px;
  margin-left: 10px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
.login-btn {
  width: 100%;
}
.login-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  text-align: center;
  line-height: 1.8;
}
</style>
