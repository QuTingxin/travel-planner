# 智能旅行规划系统 - 项目文档

## 项目简介

智能旅行规划系统是一个基于 Spring Boot + React 的全栈 Web 应用，提供语音输入的智能旅行规划功能。用户可以通过语音描述旅行需求，系统自动调用 AI 大模型生成详细的旅行行程和预算规划。

- [github地址](https://github.com/QuTingxin/travel-planner)

## 🏗️ 项目架构

### 技术栈

**后端技术**
- **框架**: Spring Boot 2.7.18
- **语言**: Java 8
- **数据库**: SQLite (嵌入式数据库)
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **构建**: Maven

**前端技术**
- **框架**: React 18 + TypeScript
- **构建**: Vite
- **UI库**: Ant Design 5.x
- **路由**: React Router 6.x
- **HTTP**: Axios
- **状态**: React Hooks

**基础设施**
- **容器化**: Docker + Docker Compose
- **数据库**: SQLite (无需外部数据库)
- **部署**: 单容器部署，数据持久化

## 📁 项目目录结构

```
travel-planner/
├── backend/                 # Spring Boot 后端服务
│   ├── src/main/java/com/travelplanner/
│   │   ├── entity/         # JPA 实体类
│   │   │   ├── User.java               # 用户实体
│   │   │   ├── TravelPlan.java         # 旅行计划实体
│   │   │   └── Expense.java            # 费用记录实体
│   │   ├── repository/     # 数据访问层
│   │   │   ├── UserRepository.java
│   │   │   ├── TravelPlanRepository.java
│   │   │   └── ExpenseRepository.java
│   │   ├── service/        # 业务逻辑层
│   │   │   ├── UserService.java
│   │   │   ├── TravelPlanService.java
│   │   │   ├── AIService.java          # AI 集成服务
│   │   │   └── CustomUserDetailsService.java
│   │   ├── controller/     # 控制器层
│   │   │   ├── AuthController.java
│   │   │   ├── TravelPlanController.java
│   │   │   ├── ExpenseController.java
│   │   │   └── VoicePlanController.java # 语音规划控制器
│   │   ├── dto/           # 数据传输对象
│   │   │   ├── RegisterRequest.java
│   │   │   ├── VoicePlanRequest.java
│   │   │   └── AliyunAIRequest.java
│   │   ├── config/        # 配置类
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   ├── DatabaseConfig.java
│   │   │   └── WebMvcConfig.java
│   │   └── utils/         # 工具类
│   │       ├── JwtUtil.java
│   │       └── JwtRequestFilter.java
│   ├── src/main/resources/
│   │   └── application.yml # 应用配置
│   ├── pom.xml            # Maven 依赖配置
│   └── Dockerfile         # 后端 Docker 配置
├── frontend/              # React 前端应用
│   ├── src/
│   │   ├── components/    # 可复用组件
│   │   │   ├── Layout.tsx              # 主布局组件
│   │   │   ├── TravelPlanCard.tsx      # 旅行计划卡片
│   │   │   └── VoiceInput.tsx          # 语音输入组件
│   │   ├── pages/         # 页面组件
│   │   │   ├── Login.tsx               # 登录页面
│   │   │   ├── TravelPlanner.tsx       # 旅行规划页面
│   │   │   ├── VoicePlanner.tsx        # 语音规划页面
│   │   │   └── Debug.tsx               # 调试页面
│   │   ├── services/      # API 服务
│   │   │   └── api.ts                  # 统一 API 调用
│   │   ├── types/         # TypeScript 类型定义
│   │   │   └── index.ts
│   │   ├── utils/         # 工具函数
│   │   │   └── speechRecognition.ts    # 语音识别工具
│   │   ├── App.tsx        # 根组件
│   │   ├── main.tsx       # 应用入口
│   │   └── vite-env.d.ts  # Vite 类型定义
│   ├── package.json       # Node.js 依赖
│   ├── vite.config.ts     # Vite 构建配置
│   ├── tsconfig.json      # TypeScript 配置
│   ├── index.html         # HTML 入口
│   └── Dockerfile         # 前端 Docker 配置
├── docker-compose.yml     # 服务编排配置
├── .env                   # 环境变量配置
└── README.md              # 项目说明文档
```

## 🚀 核心功能实现

### 1. 用户认证系统

**后端实现**:
- JWT Token 认证机制
- Spring Security 权限控制
- 密码加密存储 (BCrypt)

**关键代码**:
```java
// JWT Token 生成
public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();
}
```

### 2. 语音识别与 AI 集成

**前端语音识别**:
```typescript
// 基于 Web Speech API 的语音识别
export class SpeechRecognitionUtil {
    start(): Promise<string> {
        return new Promise((resolve, reject) => {
            this.recognition.onresult = (event) => {
                const transcript = event.results[0][0].transcript;
                resolve(transcript);
            };
            this.recognition.start();
        });
    }
}
```

**后端 AI 服务集成**:
```java
// 阿里云通义千问集成
public String generateItinerary(String destination, String startDate, 
                               String endDate, Double budget, 
                               Integer travelerCount, String preferences) {
    String prompt = buildDetailedItineraryPrompt(destination, startDate, 
                                                endDate, budget, 
                                                travelerCount, preferences);
    return callAliYunQwenModel(prompt);
}
```

### 3. 数据模型设计

**用户实体**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "user")
    private List<TravelPlan> travelPlans;
}
```

**旅行计划实体**:
```java
@Entity
@Table(name = "travel_plans")
public class TravelPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double budget;
    private Integer travelerCount;
    private String preferences;
    private String itinerary; // AI 生成的行程内容
    
    @ManyToOne
    private User user;
    
    @OneToMany(mappedBy = "travelPlan")
    private List<Expense> expenses;
}
```

## 🛠️ 本地运行指南

### 环境要求
- Java 8+
- Node.js 16+
- Maven 3.6+

### 后端运行步骤

1. **启动后端服务**:
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动


### 前端运行步骤

1. **安装依赖**:
```bash
cd frontend
npm install
```

2. **启动开发服务器**:
```bash
npm run start
```

前端服务将在 `http://localhost:3000` 启动

3. **访问应用**:
打开浏览器访问 `http://localhost:3000`

### 测试账号
系统首次运行需要注册新用户，注册后即可登录使用。

## 🐳 Docker 运行指南

### 使用 Docker Compose（推荐）

1. **构建并启动所有服务**:
```bash
docker-compose up -d
```

2. **查看服务状态**:
```bash
docker-compose ps
```

3. **查看日志**:
```bash
docker-compose logs -f
```

4. **停止服务**:
```bash
docker-compose down
```

### 单独运行容器

**构建后端镜像**:
```bash
cd backend
docker build -t travel-planner-backend .
docker run -p 8080:8080 travel-planner-backend
```

**构建前端镜像**:
```bash
cd frontend
docker build -t travel-planner-frontend .
docker run -p 80:80 travel-planner-frontend
```

### Docker 服务访问

- **前端应用**: http://localhost
- **后端 API**: http://localhost:8080/api
- **数据库文件**: 持久化在 `./data` 目录

## ⚙️ 配置说明

### 环境变量配置

创建 `.env` 文件:
```properties
# 后端配置
SERVER_PORT=8080
JWT_SECRET=your_jwt_secret_key

# 阿里云 AI 配置（可选）
ALIYUN_AI_API_KEY=your_aliyun_api_key

# 前端配置
VITE_API_BASE_URL=http://localhost:8080/api
```

### 数据库配置

默认使用 SQLite 嵌入式数据库，数据文件保存在 `travel_planner.db`。

如需切换数据库，修改 `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/travel_planner
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

## 🔧 故障排除

### 常见问题

1. **CORS 错误**
   - 检查后端 CORS 配置
   - 确认前端代理配置正确



2. **AI 服务不可用**
   - 检查阿里云 API 密钥配置
   - 系统会自动降级到模拟数据

3. **语音识别不支持**
   - 确保使用 Chrome 或 Edge 浏览器
   - 检查麦克风权限设置

### 日志调试

**后端日志级别**:
```yaml
logging:
  level:
    com.travelplanner: DEBUG
    org.springframework.security: DEBUG
```



## 📝 API 接口文档

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 旅行计划接口
- `GET /api/travel-plans` - 获取用户旅行计划
- `POST /api/travel-plans` - 创建旅行计划
- `DELETE /api/travel-plans/{id}` - 删除旅行计划

### 语音规划接口
- `POST /api/voice-plan/generate` - 语音生成旅行计划

### 费用管理接口
- `GET /api/expenses/plan/{planId}` - 获取计划费用
- `POST /api/expenses` - 添加费用记录

## 🎯 功能特点

1. **智能语音输入** - 基于 Web Speech API 的语音识别
2. **AI 行程规划** - 集成阿里云大模型的智能规划
3. **预算管理** - 详细的费用分配和跟踪
4. **响应式设计** - 支持桌面和移动设备
5. **实时交互** - 流畅的用户体验和即时反馈

## 🔄 开发工作流

1. **代码规范**: 遵循 Java 和 TypeScript 编码规范
2. **Git 流程**: 使用 feature branch 工作流
3. **测试**: 包含单元测试和集成测试
4. **部署**: 支持 Docker 容器化部署

---

**项目维护**: Zn 
**最后更新**: 2024年11月  
**版本**: 1.0.0