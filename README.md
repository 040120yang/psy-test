# 区域心理测试系统（Regional Psychological Testing System）

> 开源协作版：任何人可 fork / clone 共同开发，欢迎提交 Issue 与 Pull Request。

基于 **SpringBoot + Vue 前后端分离** 架构的区域心理测试系统，项目模板参照 **若依（RuoYi-Vue）** 框架的工程组织、接口风格与分层思想实现。系统采用**双端结构**（对应"公众心理服务 + 医护诊疗辅助"设计）：

- **用户端**（`/portal`，面向公众用户）：工作台首页（我的统计 + 快捷操作）、心理测评（选择量表 → 在线答题 → 测评报告）、我的测评记录、**自助注册**（登录页可注册公众账号）
- **管理端**（`/admin`，面向系统管理员/临床医护人员）：系统概览、全部测评记录、患者管理、系统管理（用户/量表/题目）

登录后系统根据角色自动进入对应端：`user`（公众用户）→ 用户端；`admin`/`doctor` → 管理端。

## 一、技术栈

| 端 | 技术 | 说明 |
|---|---|---|
| 后端 | Spring Boot 2.7.18 | 兼容 JDK 8 / 11 / 17 |
| 后端 | MyBatis + PageHelper | 数据持久化与分页（对齐若依） |
| 后端 | MySQL 8.0 | 数据库 |
| 后端 | Druid | 数据库连接池 |
| 后端 | JJWT + spring-security-crypto | 无状态令牌认证 + BCrypt 密码加密 |
| 前端 | Vue 2.7 + Vue Router 3 | 前端框架（对齐若依 RuoYi-Vue） |
| 前端 | Element UI 2.15 | 组件库（对齐若依） |
| 前端 | Axios + ECharts | 请求封装与统计图表 |

## 二、目录结构

```
psy-test/
├── sql/
│   └── psy_test.sql              # 数据库初始化脚本（建库+建表+初始数据+80道量表题目）
├── backend/                      # SpringBoot 后端工程（单模块，分层对齐若依）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/psy/
│       │   ├── common/           # 通用模块：AjaxResult/TableDataInfo/BaseEntity/分页/异常
│       │   ├── framework/        # 框架模块：JWT 认证过滤器/令牌服务/验证码/登录服务/跨域
│       │   ├── system/           # 系统模块：用户管理（角色：admin/doctor/user）
│       │   └── business/         # 业务模块：量表/题目/测评/记录/患者/统计
│       └── resources/
│           ├── application.yml   # 数据源与令牌配置
│           └── mapper/           # MyBatis XML
├── frontend/                     # Vue2 前端工程（若依风格，双端入口）
│   ├── package.json
│   ├── vue.config.js             # 端口 8088，/dev-api 代理到 8080
│   └── src/
│       ├── api/                  # 接口定义
│       ├── layout/               # PortalLayout（用户端顶栏布局）+ AdminLayout（管理端侧边栏布局）
│       ├── views/portal/         # 用户端页面：工作台首页 / 量表选择 / 在线答题 / 测评报告 / 我的测评记录
│       ├── views/admin/          # 管理端页面：系统概览 / 全部记录 / 患者管理 / 系统管理
│       ├── views/login/          # 登录页 + 注册页（登录后按角色进入对应端）
│       ├── permission.js         # 路由守卫
│       └── utils/request.js      # Axios 封装（token 携带、统一响应）
└── README.md
```

## 三、快速启动

### 1. 初始化数据库

1. 启动本机 MySQL 8.0 服务。
2. 修改后端数据库密码（`backend/src/main/resources/application.yml`）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/psy_test?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: "你的MySQL密码"     # ← 改成你自己的 root 密码
```

3. 导入初始化脚本（任选其一）：

```bash
mysql -uroot -p < sql/psy_test.sql
```

或使用 Navicat / DataGrip 等工具执行 `sql/psy_test.sql`。

### 2. 启动后端（端口 8080）

```bash
cd backend
mvn spring-boot:run
```

或打包运行：

```bash
mvn clean package -DskipTests
java -jar target/psy-test.jar
```

启动成功提示：`区域心理测试系统后端启动成功，访问地址：http://localhost:8080`

### 3. 启动前端（端口 8088）

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 **http://localhost:8088**（开发服务器已将 `/dev-api` 代理到后端 8080）。

## 四、演示账号（密码均为 123456）

| 账号 | 角色 | 登录后进入 | 可访问功能 |
|---|---|---|---|
| `admin` | 系统管理员 | 管理端 `/admin` | 系统概览、全部测评记录、患者管理、系统管理（用户/量表/题目） |
| `doctor` | 临床医护人员 | 管理端 `/admin` | 系统概览、全部测评记录、患者管理 |
| `user` | 公众用户 | 用户端 `/portal` | 工作台首页、心理测评（量表选择/答题/报告）、我的测评记录 |

> 访问根路径 `/` 会自动进入用户端首页；登录页提供"立即注册"，公众用户可自助注册账号（自动分配 `user` 角色）；管理端页面仅限 admin/doctor 账号访问。

## 五、功能清单

### 用户端（`/portal`，公众用户）
| 模块 | 功能 |
|---|---|
| 用户注册 | 登录页"立即注册"自助注册公众账号，注册成功即可登录 |
| 工作台首页 | 欢迎横幅 + 心理援助热线 12356 + 我的数据统计（测评次数/正常/需关注/最近时间）+ 快捷操作 + 平台概览 |
| 心理测评 | SAS 焦虑 / SDS 抑郁 / SCL-90 症状 / SRSS 睡眠 四类量表在线作答，自动计分 |
| 测评报告 | 粗分、标准分、结果等级（正常/轻/中/重度）+ 分级建议 + 答题明细 |
| 我的测评记录 | 个人测评记录查询、报告查看、记录删除 |
| AI 智能体 | 对接扣子（Coze）智能体，与 AI 心理助手在线对话（需配置，见"九、扣子智能体配置"） |

### 管理端（`/admin`，系统管理员/临床医护人员）
| 模块 | 功能 |
|---|---|
| 系统概览 | 用户/测评/量表/患者统计卡片 + 量表分布、等级分布、近7天趋势图（ECharts） |
| 测评记录 | 全部测评记录查询（按量表/等级/用户名筛选）、报告详情、删除 |
| 患者管理 | 患者信息建档、查询、修改、删除 |
| 系统管理 | 用户管理（增删改查、重置密码）、量表管理（启停用）、题目管理（反向计分标记） |

### 公共（登录认证）
图片验证码 + 账号密码登录（BCrypt 加密）、用户自助注册、JWT 无状态令牌、路由守卫、按角色自动进入对应端。登录态存于浏览器会话（sessionStorage），**关闭浏览器后需重新登录**；登录页不预填账号密码。

## 六、测评计分规则

| 量表 | 评分制 | 标准分 | 等级阈值 |
|---|---|---|---|
| SAS 焦虑自评 | 1-4 分，5/9/13/17/19 反向计分 | 粗分 × 1.25 | <50 正常；50-59 轻度；60-69 中度；≥70 重度 |
| SDS 抑郁自评 | 1-4 分，10 题反向计分 | 粗分 × 1.25 | <53 正常；53-62 轻度；63-72 中度；≥73 重度 |
| SCL-90 症状自评 | 1-5 分 | 粗分（满分100），按均分 | 均分<1.5 正常；1.5-2.5 轻度；2.5-3.5 中度；≥3.5 重度 |
| SRSS 睡眠自评 | 1-5 分 | 粗分（满分100） | <40 正常；40-59 轻度；60-79 中度；≥80 重度 |

> 反向计分公式：得分 = 满分 + 1 − 原始分值。示例数据每类量表 20 题（SCL-90、SRSS 可在"题目管理"中按标准量表扩充至 90 题 / 10 题）。

## 七、与若依（RuoYi-Vue）的对应关系

- **响应格式**：`AjaxResult{code,msg,data}`、`TableDataInfo{total,rows,code,msg}`，分页参数 `pageNum/pageSize`；
- **分层结构**：`common（core/exception/utils）`、`framework（security/config/web）`、`system`、`business`，与若依 `ruoyi-common / ruoyi-framework / ruoyi-system` 对齐；
- **认证方式**：登录接口 `/login`、用户信息 `/getInfo`、`Authorization: Bearer <token>` 请求头、JWT 过滤器 + ThreadLocal 获取当前用户（`SecurityUtils`）；
- **前端组织**：`layout` 布局 + 按角色过滤菜单、`permission.js` 路由守卫、`utils/request.js` 拦截器、`/dev-api` 代理；
- **安全**：密码 BCrypt 加密存储；测评报告明确"仅作参考、不能替代医疗诊断"，并对中重度结果给出就医与援助热线提示。

## 八、常见问题

| 问题 | 处理 |
|---|---|
| 登录提示"用户名或密码错误" | 确认已导入 `psy_test.sql` 且账号为 admin/doctor/user、密码 123456 |
| 验证码一直刷新失败 | 检查后端 8080 是否启动、前端代理是否生效 |
| 访问 8088 无页面 | 确认 `npm run dev` 成功且访问的是 `http://localhost:8088` |
| 接口 500 数据库异常 | 检查 `application.yml` 的数据库账号密码、`psy_test` 库是否已导入 |

## 九、扣子（Coze）智能体配置

用户端"快捷操作 → AI 智能体"对接扣子开放平台 API，需要两步准备：

1. **获取访问令牌**：登录扣子开放平台 https://www.coze.cn/open/oauth/pats ，创建个人访问令牌（格式 `pat_xxx`）。
2. **获取智能体 ID 并发布为 API**：在扣子 https://www.coze.cn 搭建智能体后点击"发布"，**勾选「API」**；开发页 URL 中 `bot` 参数后的数字即为智能体 ID。

在 `backend/src/main/resources/application.yml` 填写：

```yaml
coze:
  api-token: "pat_xxxxxxxx"   # 你的个人访问令牌
  bot-id: "7342xxxxxx"        # 你的智能体 ID
```

保存后重启后端（8080）即可使用。未配置时页面会提示"未配置扣子智能体"。

### 在线接口文档（Knife4j）

后端已集成 Knife4j（基于 Swagger / OpenAPI 的在线接口文档），启动后端后访问：

```
http://localhost:8080/doc.html
```

**它是什么 / 有什么作用**：接口文档由后端代码**自动生成**，无需手写维护，集中展示本系统全部 HTTP 接口的地址、请求方法、请求参数、返回字段与示例，主要用于：

- **前后端联调**：前端开发时直接对照文档确认接口地址与参数格式，不用逐行翻后端代码；
- **在线调试**：浏览器里直接填参数发请求、看返回，不用 Postman / Apifox；
- **对接参考**：第三方或新接手的开发者可据此理解接口约定，快速上手二次开发；
- **接口留档**：后端新增/修改接口后，重启服务文档自动同步，避免文档与代码不一致。

**怎么用（三步）**：

1. 启动后端（8080）后浏览器打开 `http://localhost:8080/doc.html`，左侧按 Controller 分组列出全部接口；
2. 调试需鉴权：先在 `login-controller` 分组里调用 `/login`（用户名 + 密码 + 图形验证码）拿到返回的 `token`；
3. 点击文档页面右上角 **「Authorize / 调试」** 按钮，在 Authorization 输入框填写 `Bearer <token>`（注意 `Bearer` 后有空格），保存后即可在线调试其他接口。

> 说明：各接口分组与功能的中文释义见下方表格；Knife4j 页面本身保留英文原始字段名（如 captcha-controller、/coze/chat 等），以下表格逐项解释其含义与作用。

各接口分组与功能说明如下：

#### 1. captcha-controller — 验证码

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/captchaImage` | GET | 获取图形验证码：登录/注册前调用，返回验证码图片（Base64）与 uuid，提交时一并回传校验 |

#### 2. coze-controller — AI 智能体

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/coze/chat` | POST | AI 智能体对话：向扣子（Coze）智能体发送消息并返回心理陪伴回复；携带 conversationId 可继续多轮对话，令牌仅存后端不暴露给前端 |

#### 3. dashboard-controller — 系统概览

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/dashboard/stats` | GET | 管理端概览统计：用户/测评/量表/患者总数、测评趋势、结果等级分布（仅管理员与医生可访问） |

#### 4. login-controller — 登录认证

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/login` | POST | 用户登录：账号+密码+验证码校验，成功返回 JWT 令牌（后续接口鉴权凭证） |
| `/getInfo` | GET | 获取当前登录用户信息：返回用户资料、角色标识与角色名（前端据此跳转对应端） |
| `/logout` | POST | 退出登录：前端清除本地令牌即可 |
| `/register` | POST | 公众用户自助注册：创建 role_id=3 的普通用户，用户名唯一，密码 BCrypt 加密存储 |

#### 5. patient-controller — 患者管理

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/patient/list` | GET | 分页查询患者列表，支持按姓名/电话筛选 |
| `/patient` | POST | 新增患者：创建患者档案（姓名/性别/年龄/电话），手机号需为 11 位 |
| `/patient` | PUT | 修改患者：更新患者档案信息 |
| `/patient/{patientId}` | DELETE | 删除患者：按患者 ID 删除档案 |

#### 6. question-controller — 题目管理（仅管理员）

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/system/question/list` | GET | 分页查询题目列表，支持按量表/题干筛选 |
| `/system/question/listByScale` | GET | 按量表查询题目：返回指定量表全部题目（测评答题页使用） |
| `/system/question` | POST | 新增题目：创建量表题目（题干/选项/分值/题号） |
| `/system/question` | PUT | 修改题目：更新题干/选项/分值等 |
| `/system/question/{questionId}` | DELETE | 删除题目：按题目 ID 删除 |

#### 7. record-controller — 测评记录

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/test/record/my` | GET | 我的测评记录：当前登录用户的测评历史（用户端个人中心） |
| `/test/record/list` | GET | 全部测评记录：管理端分页查询，支持按用户/量表筛选 |
| `/test/record/{recordId}` | GET | 记录详情：返回总分、等级与逐题答题明细 |
| `/test/record/{recordId}` | DELETE | 删除测评记录：按记录 ID 物理删除 |

#### 8. scale-controller — 量表管理（仅管理员）

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/system/scale/list` | GET | 分页查询量表列表，支持按名称/编码筛选 |
| `/system/scale` | POST | 新增量表：创建四类心理量表（SAS/SDS/SCL-90/SRSS，含计分规则与等级阈值） |
| `/system/scale` | PUT | 修改量表：更新名称/编码/计分规则等 |
| `/system/scale/{scaleId}` | DELETE | 删除量表：按量表 ID 删除（级联删除其题目） |

#### 9. sys-user-controller — 用户管理（仅管理员）

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/system/user/list` | GET | 分页查询用户列表，支持按用户名/昵称筛选 |
| `/system/user/roles` | GET | 角色列表：返回系统全部角色（用户管理页分配角色下拉框使用） |
| `/system/user` | POST | 新增用户：创建账号（密码 BCrypt 加密，可分配角色） |
| `/system/user` | PUT | 修改用户：更新昵称/性别/年龄/角色等 |
| `/system/user/{userId}` | DELETE | 删除用户：按用户 ID 删除（不可删除超级管理员） |
| `/system/user/resetPwd` | PUT | 重置密码：将指定用户密码重置为新值（BCrypt 加密） |

#### 10. test-controller — 心理测评（公众用户答题端）

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/test/scales` | GET | 获取全部启用量表：用户端测评入口，返回四类量表 |
| `/test/questions` | GET | 获取量表题目：按题号排序返回指定量表的全部题目（含选项与分值） |
| `/test/submit` | POST | 提交答卷：按量表计分规则计算总分与等级，自动保存测评记录并返回结果（含健康建议） |

#### 11. login-controller — 个人中心（用户端）

| 接口 | 方法 | 功能说明 |
|---|---|---|
| `/getInfo` | GET | 获取当前登录用户信息：返回昵称/性别/年龄/手机号/头像/角色（个人中心页初始化） |
| `/user/profile` | PUT | 修改个人资料：更新昵称/性别/年龄/手机号/头像（手机号 11 位正则校验） |
| `/user/profile/updatePwd` | PUT | 修改登录密码：校验原密码后 BCrypt 加密新密码 |

## 十、开发者协作

### 环境要求
- JDK 8 / 11 / 17 任一版本 + Maven 3.6+
- Node.js 14+（npm）
- MySQL 5.7 / 8.0
- 不需要 Redis（见"八、常见问题"）

### 用 IDEA 打开项目（重要）
后端 Maven 工程位于 `backend/` **子目录**（仓库根目录没有 pom.xml），请按以下方式导入：

1. `git clone` 后，**不要直接打开仓库根目录**
2. 方式一（推荐）：IDEA → `File → Open` → 选择 **`backend`** 文件夹 → 打开后 IDEA 自动识别为 Maven 项目并下载依赖
3. 方式二：若已打开根目录，右键 `backend/pom.xml` → **Add as Maven Project**
4. 确认 Project SDK 为 **JDK 8 / 11 / 17**；项目使用 Lombok（新版 IDEA 自动支持，旧版需安装 Lombok 插件并在 Settings → Build → Compiler → Annotation Processors 勾选 Enable）
5. 前端：终端执行 `cd frontend && npm install` 生成依赖后，用 IDEA（安装 Vue.js 插件）或 VSCode 打开 `frontend` 目录开发

### 本地开发步骤
```bash
# 1. 克隆项目
git clone <你的仓库地址>

# 2. 初始化数据库（导入 sql/psy_test.sql 到本地 MySQL）

# 3. 配置后端（复制模板并按环境修改数据库密码）
copy backend/src/main/resources/application-example.yml backend/src/main/resources/application.yml

# 4. 启动后端（8080）
cd backend && mvn spring-boot:run

# 5. 启动前端（8088，需先安装依赖）
cd frontend && npm install && npm run dev
```

### 参与开发
1. **Fork** 本仓库到你的账号，clone 到本地
2. 新建功能分支：`git checkout -b feat/your-feature`
3. 开发并自测（后端 `mvn compile` / 前端 `npm run build:prod` 通过）
4. 提交（Commit 信息建议：`feat:` 新功能 / `fix:` 修复 / `docs:` 文档 / `refactor:` 重构）
5. 推送分支并发起 **Pull Request**

### 安全约定
- `application.yml`（含数据库密码、AI 令牌等敏感配置）已加入 `.gitignore`，**严禁提交**
- 新增配置时同步更新 `application-example.yml` 模板
