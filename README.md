# 软件工程管理 G07 产业知识智能问答工具
## 运行步骤
### 数据库
运行 `database/db.sql` 在本地建立数据库。
记得将配置文件中的数据库用户名和密码改成自己的。

### 后端
先进入后端目录。
```powershell
cd backend
```
运行如下命令安装相关依赖。
```powershell
mvn clean install -DskipTests
```
通过docker运行MinIO。
输入如下命令运行后端。
```
mvn spring-boot:run
```

### 前端
先进入前端目录。
```powershell
cd frontend
```
安装相关依赖。
```powershell
npm install
npm install vuedraggable@next
```
执行如下命令运行前端
```powershell
npm run dev
```
在浏览器输入网址<http://localhost:5173>进入网页。

## 关于用户角色
### 普通用户
也就是viewer，只支持查看、下载产业知识文档以及使用ai智能问答功能。
### 研究员
也就是researcher，在普通用户权限基础上支持上传、删除知识文档，支持管理知识库（包括创建、删除、重命名、排序）。
### 管理员
也就是admin，在研究员的基础上支持用户管理。