# 销售管理系统

## 项目简介

销售管理系统是一个基于若依 `RuoYi v3.9.1` 二次开发的销售管理 ERP 系统，采用前后端分离架构，适合用于销售、采购、仓储、库存、财务结算等业务场景。

本文档遵循“外中内英”的命名原则：

- 对外展示名称使用中文：`销售管理系统`
- 内部工程、目录、技术标识继续使用英文：`SaleERP`

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

- 库存台账
- 库存流水
- 库存盘点
- 库存调拨

### 经营分析

- 经营看板
- 销售报表
- 往来对账
- 发票税务统计
- 现金流预测
- 年度预算计划
- 经营简报归档
- 站内消息提醒
- 数据备份与恢复

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

推荐优先执行以下脚本：

- `SaleERP/sql/all_in_one_20260319.sql`：基础库结构、菜单、权限、基础数据
- `SaleERP/sql/mock_demo_data.sql`：演示数据

如果你的数据库不是从最新版本完整初始化，建议再按文件名日期顺序补充执行 `SaleERP/sql` 目录下的增量脚本，例如：

- `fix_sys_menu_mojibake_20260409.sql`
- `add_sale_order_link_to_outbound_20260410.sql`
- `add_purchase_order_link_to_inbound_20260410.sql`
- `add_purchase_order_item_link_to_inbound_item_20260410.sql`
- `fix_business_menu_entry_20260410.sql`
- `upgrade_fin_receipt_20260411.sql`
- `fix_menu_icons_20260411.sql`
- `add_management_phase2_20260411.sql`

### 2. 修改后端配置

启动前请按本地环境修改以下配置：

- `SaleERP/ruoyi-admin/src/main/resources/application.yml`
  - 服务端口
  - 文件上传路径
  - Redis 配置
  - Token 配置
- `SaleERP/ruoyi-admin/src/main/resources/application-druid.yml`
  - MySQL 数据源地址
  - 数据库用户名与密码

### 3. 启动后端

在根目录执行：

```bash
cd SaleERP
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

后端默认端口配置为 `5589`。

接口文档默认地址：

```text
http://localhost:5589/swagger-ui.html
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

前端开发代理配置位于：

```text
SaleERP-Vue3/vite.config.js
```

开发时会通过 `/dev-api` 代理到后端接口地址。若你的后端地址不是当前仓库配置中的地址，请先修改该文件中的代理目标后再启动前端。

## 常用访问地址

- 前端开发地址：`http://localhost:8064`
- 后端接口地址：`http://localhost:5589`
- Swagger 文档：`http://localhost:5589/swagger-ui.html`

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
