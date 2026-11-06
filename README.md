# 猫宁早点预订与配送管理系统

一个面向校园早餐预订场景的 Spring Boot 后端项目，覆盖商品管理、用户认证、库存扣减、订单履约、配送接单和经营统计。

## 项目亮点

- Spring Boot 3 + Java 17 + Maven
- Spring Security + JWT 无状态认证
- JPA + MySQL（生产运行）和 H2（本地与测试）
- Flyway 数据库版本管理
- 商品库存使用悲观锁扣减，避免并发下单造成超卖
- 顾客、配送员、管理员三类角色和清晰的接口权限隔离
- OpenAPI/Swagger 在线接口文档
- JUnit 5 + MockMvc 集成测试
- Docker Compose 一键启动 MySQL 和应用

## 功能模块

| 模块 | 功能 |
| --- | --- |
| 用户认证 | 手机号注册、登录、JWT 鉴权 |
| 商品管理 | 商品查询、创建、修改、上下架 |
| 订单管理 | 创建订单、查询订单、取消订单、库存恢复 |
| 配送履约 | 配送员接单、开始配送、完成配送 |
| 经营统计 | 销售额、订单数、热销商品 |

## 技术架构

```text
Controller -> Service -> Repository -> MySQL
                |
           DTO / Validation
                |
        Spring Security + JWT
```

核心并发控制位于下单流程：系统在同一个数据库事务中通过 `PESSIMISTIC_WRITE` 锁定商品记录，检查库存后再扣减，避免多个用户同时预订最后一份商品时出现超卖。

## 本地启动

### 1. 环境要求

- JDK 17+
- Maven 3.9+

### 2. 运行

```bash
./mvnw spring-boot:run
```

默认使用 H2 内存数据库，启动后访问：

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- H2 Console: <http://localhost:8080/h2-console>
- Health Check: <http://localhost:8080/actuator/health>

H2 控制台连接信息：

```text
JDBC URL: jdbc:h2:mem:catning
User Name: sa
Password: （留空）
```

演示管理员账号：

```text
手机号：13800000000
密码：Admin123!
```

演示配送员账号：

```text
手机号：13900000000
密码：Courier123!
```

账号仅用于本地演示，部署前请通过环境变量修改。

### 3. 使用 MySQL

```bash
docker compose up -d mysql
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

默认环境变量：

```text
DB_URL=jdbc:mysql://localhost:3306/catning?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=catning
DB_PASSWORD=catning123
JWT_SECRET=replace-with-at-least-32-characters-secret
```

### 4. Docker 一键启动

```bash
docker compose up --build
```

## 主要接口

### 认证

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 公开 | 注册顾客账号 |
| POST | `/api/auth/login` | 公开 | 登录并获取 JWT |

### 商品

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/products` | 公开 | 分页查询在售商品 |
| POST | `/api/admin/products` | ADMIN | 创建商品 |
| PUT | `/api/admin/products/{id}` | ADMIN | 修改商品 |
| PATCH | `/api/admin/products/{id}/status` | ADMIN | 上架或下架 |

### 订单

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/orders` | CUSTOMER | 创建订单 |
| GET | `/api/orders/me` | 登录用户 | 查看自己的订单 |
| POST | `/api/orders/{id}/cancel` | CUSTOMER | 取消待确认订单 |
| POST | `/api/admin/orders/{id}/confirm` | ADMIN | 确认并派单 |
| GET | `/api/courier/orders/available` | COURIER | 查看可接订单 |
| POST | `/api/courier/orders/{id}/accept` | COURIER | 接单 |
| POST | `/api/courier/orders/{id}/complete` | COURIER | 完成配送 |

### 经营统计

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/admin/statistics/overview` | ADMIN | 销售额、订单数、热销商品 |

## 示例请求

注册：

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"测试用户","phone":"13700000001","password":"Password123!"}'
```

登录：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13700000001","password":"Password123!"}'
```

创建订单：

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"items":[{"productId":1,"quantity":2}],"deliveryAddress":"吉林师范大学 1 号宿舍楼","remark":"少糖"}'
```

## 数据库设计

- `users`：账户、角色、状态
- `products`：商品、价格、库存、上下架状态、版本号
- `breakfast_orders`：订单、履约状态、配送员、金额
- `order_items`：订单商品快照，价格与名称在创建时固化

订单商品保存名称和单价快照，避免后续商品改价影响历史订单。

## 测试

```bash
./mvnw test
```

测试覆盖登录鉴权、订单完整履约、取消订单恢复库存以及并发争抢最后一份库存时不超卖等核心流程。

## 后续演进

- Redis 缓存商品列表和登录会话黑名单
- RabbitMQ/Kafka 异步处理订单通知
- 引入支付回调与幂等键
- 使用 Prometheus + Grafana 做可观测性建设
- 增加配送路线与预计送达时间

## 作者

郑立中
