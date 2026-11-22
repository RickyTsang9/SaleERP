# SaleERP管理系统

## 项目简介

SaleERP管理系统是一个基于若依 `RuoYi v3.9.1` 二次开发的销售管理 ERP 系统，采用前后端分离架构，适合用于销售、采购、仓储、库存、财务结算等业务场景。

本文档遵循“外中内英”的命名原则：

- 对外展示名称使用中文：`SaleERP管理系统`
- 内部工程、目录、技术标识继续使用英文：`SaleERP`

## 更新日志

- 详细更新内容请查看 [CHANGELOG.md](./CHANGELOG.md)

## 仓库说明

这个目录用于整理 GitHub 对外发布的总仓库版本。

- `SaleERP`：后端源码目录
- `SaleERP-Vue3`：前端源码目录
- 本仓库仅保留适合公开发布的源码与示例配置
- 日常内网开发仓库可以继续独立维护，不与这个公开仓库直接混用

当前工作区由两个子项目组成：

- `SaleERP`：后端项目，基于 Spring Boot + Spring Security + MyBatis + Redis + JWT
- `SaleERP-Vue3`：前端项目，基于 Vue 3 + Element Plus + Vite + Pinia

项目在保留若依原有权限管理、系统管理、代码生成、日志监控等能力的基础上，扩展了面向业务的 ERP 模块，已经具备完整的基础资料、订单、库存、财务、报表等功能。

## 技术栈

### 后端

- Java 8
- Spring Boot 2.7.18
- Spring Security
- MyBatis
- Druid
- PageHelper
- Redis
- JWT
- Springdoc OpenAPI

### 前端

- Vue 3.5
- Element Plus 2.13
- Vite 6
- Pinia
- Vue Router 4
- Axios
- ECharts

### 数据与中间件

- MySQL
- Redis

## 核心业务模块

### 基础资料

- 客户资料
- 供应商资料
- 商品资料
- 仓库资料
- 库位资料

### 销售与 CRM

- 客户跟进记录
- 销售订单
- 销售退货
- 出库管理
- 回款登记
- 应收管理

### 采购与供应链

- 采购订单
- 采购退货
- 入库管理
- 付款登记
- 应付管理

### 仓储与库存

- 库存台账（支持乐观锁防超卖、批次效期/临期预警、冻结/解冻、期初导入）
- 库存流水（支持单号溯源直达源单）
- 库存盘点（全仓快照载入与盲盘模式）
- 库存调拨（调出在途 → 调入签收 / 在途撤回闭环）
- 移动仓配 PDA 工作台（扫码收货、FEFO 拣货、移动连续盲盘）

### 财务与税务核算

- 应收/应付台账（红字退款、多单合并核销/付款、账龄下钻）
- 回款/付款记录（差错冲红作废、凭证打印）
- 资金账户与月结关账（多渠道余额池、资金收支流水、账户互转、日常收支、会计期间锁定与反结账）
- 进销项发票台账（多税率价税分离、防超额开票、订单开票进度反写、红冲留痕）

### 经营分析

- 经营看板（Light & Dark 双主题、当月/上月净额毛利与健康评分）
- 销售明细报表（含退货冲减、历史成本快照、权限脱敏与 Excel 导出）
- 往来对账与标准往来对账单（CSV 导出 / 打印）
- 商品经营多维排行、客户/商品 360° 经营画像
- 发票税务统计与现金流预测
- 年度预算计划、经营简报归档与决议事项
- 统一待办中心（19 类跨模块待办按岗位权限过滤）
- 站内消息提醒
- 数据备份与离线包容灾恢复（GZIP `.json.gz` 下载与上传恢复）

### 系统基础能力

- 用户、角色、菜单、部门、岗位管理
- 登录日志、操作日志、在线用户
- 参数配置、字典管理、通知公告
- 定时任务、代码生成、接口文档
- 服务监控、缓存监控

## 项目结构

```text
SaleERP/
├─ README.MD                 根目录说明文档
├─ SaleERP/                  后端工程
│  ├─ ruoyi-admin/           启动入口与 Web 层
│  ├─ ruoyi-framework/       权限、安全、通用框架能力
│  ├─ ruoyi-system/          系统模块 + 业务模块
│  ├─ ruoyi-common/          通用工具与公共组件
│  ├─ ruoyi-quartz/          定时任务模块
│  ├─ ruoyi-generator/       代码生成模块
│  ├─ sql/                   数据库初始化与增量脚本
│  └─ doc/                   项目相关说明文档
└─ SaleERP-Vue3/             Vue3 前端工程
   ├─ src/api/business/      业务接口封装
   ├─ src/views/business/    业务页面
   ├─ src/views/system/      系统管理页面
   ├─ src/views/monitor/     监控页面
   └─ vite.config.js         前端开发代理配置
```

## 后端业务代码位置说明

业务代码主要位于后端 `SaleERP/ruoyi-system` 模块内：

- `com.ruoyi.business.domain`：业务实体
- `com.ruoyi.business.service`：业务服务
- `com.ruoyi.business.mapper`：数据访问接口
- `mapper/business/*.xml`：MyBatis 映射文件

控制层接口位于 `SaleERP/ruoyi-admin/src/main/java/com/ruoyi/web/controller/business`。

## 环境要求

- JDK 8
- Maven 3.6 及以上
- Node.js 18 及以上
- MySQL
- Redis

## 快速启动

### 1. 初始化数据库

推荐直接执行最新全量一键初始化基线脚本（包含 72 张表结构、全部菜单权限及闭环演示数据）：

- `SaleERP/sql/sale_erp_full_baseline_20261122.sql`

> 默认演示管理员账号：`admin` / `admin123`（上线前请及时修改密码）。

### 2. 修改后端配置

后端已支持通过环境变量（`DB_URL` / `DB_USERNAME` / `DB_PASSWORD` / `REDIS_HOST` / `REDIS_PASSWORD` / `JWT_TOKEN_SECRET` / `SERVER_PORT` / `UPLOAD_PATH`）注入生产配置，也可直接修改以下配置文件：

- `SaleERP/ruoyi-admin/src/main/resources/application.yml`
- `SaleERP/ruoyi-admin/src/main/resources/application-druid.yml`

### 3. 启动后端

在根目录执行：

```bash
cd SaleERP
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

后端默认端口配置为 `5954`。

接口文档默认地址：

```text
http://localhost:5954/swagger-ui.html
```

### 4. 启动前端

在根目录执行：

```bash
cd SaleERP-Vue3
npm install
npm run dev
```

前端开发服务默认端口为 `8064`。

### 5. 前端代理配置说明

前端开发代理优先读取 `SaleERP-Vue3/.env.development` 中的 `VITE_PROXY_TARGET`（默认 `http://127.0.0.1:5954`），通过 `/dev-api` 转发到后端服务。

## 常用访问地址

- 前端开发地址：`http://localhost:8064`
- 后端接口地址：`http://localhost:5954`
- Swagger 文档：`http://localhost:5954/swagger-ui.html`

## 已落地的业务接口与页面

从当前代码结构来看，系统已经包含以下业务页面与接口：

- 客户、供应商、商品、仓库、库位管理
- 客户跟进记录
- 销售订单、销售退货、出库
- 采购订单、采购退货、入库
- 库存、库存流水、库存盘点、调拨
- 应收、应付、回款、付款
- 数据备份
- 经营分析报表

后端还包含以下管理层相关模块：

- 年度预算计划
- 经营简报归档
- 业务消息提醒

## 开发说明

- 后端整体延续若依标准分层结构，业务调用链为 `controller -> service -> mapper`。
- 前端业务页面集中在 `SaleERP-Vue3/src/views/business`。
- 业务接口封装集中在 `SaleERP-Vue3/src/api/business`。
- 若依原生系统能力仍然可用，适合作为权限、日志、监控和代码生成基础平台继续扩展。

## 上传 GitHub 前建议检查

当前仓库中已经存在开发环境配置文件，建议在上传 GitHub 前重点检查以下内容是否需要脱敏或改为示例值：

- 数据库连接地址、用户名、密码
- Redis 地址与密码
- 前端代理中的内网地址
- 上传目录等本地路径配置

## 参考资料

- 后端原始说明：`SaleERP/README.md`
- 前端原始说明：`SaleERP-Vue3/README.md`
- 环境文档：`SaleERP/doc/若依环境使用手册.docx`

## 致谢

本项目基于若依开源框架进行二次开发，在此基础上结合 SaleERP 业务场景完成了前后端扩展与业务模块落地。
