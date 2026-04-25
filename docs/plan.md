# plan

## 目标

此计划只载发布前近程事项：先以测试锁定当前已有内容，再做专服、文档、包体与发布检查。非本轮发布所必需之功能，另见 `docs/future.md`。

## 第零阶段：基线验证

- 固定本地验证命令：`gradlew.bat build --no-daemon`、`gradlew.bat test --no-daemon`、`gradlew.bat spotlessCheck --no-daemon`、`git diff --check`。
- 记录 Java、Gradle、GTNH Convention Plugin、Forge、MC 版本，写入发布说明。
- 确认仓库无被跟踪的生成产物；`.gradle`、`build`、`run`、IDE 文件不得入仓。
- 保持 `usesMixins=false`；若未来重启 mixin，须先补真实 mixin 类与单独计划。

验收：四条验证命令皆过；`git status --short` 中无意外生成文件。

## 第一阶段：数据库与迁移测试

- 为新世界建库补测试：所有 `@Table` 实体可建表，索引、外键、枚举 CHECK、UUID CHECK 可生成。
- 为旧世界迁移补测试：`schema_version` 幂等；重复执行 `TaskSqlHelper.migrateSchema()` 不重复改表。
- 为 `SQLiteManager.query/executeUpdate/transaction` 补测试：查询会关闭资源，事务成功提交、失败回滚。
- 为 `UtilHelper.convertValue` 补测试：`UUID`、`LocalDateTime`、`Duration`、枚举、Boolean、Integer、Long。
- 为 SQL Builder 补最小测试：`IN/NOT IN`、排序、分页、update-by-id、delete 防空条件。

验收：DB 层测试在纯内存 SQLite 下稳定通过，不依赖 Minecraft 世界目录。

## 第二阶段：任务服务测试

- 覆盖 `TaskService.createTask/updateTask/changeStatus/completeTask/deleteTask/createSubtask/getVisibleTasks`。
- 覆盖状态变更事务：任务状态改变与 `StatusChangeRecord` 同成同败。
- 覆盖 `Task.version` 乐观锁入口：网络 update 带旧版本时返回 `VERSION_CONFLICT`。
- 覆盖权限上下文：私有任务仅本人/OP，可见团队任务仅团队成员/OP，团队管理员可写团队任务。
- 覆盖子任务删除：删除父任务时子任务一并删除，或明确改为软删除策略。

验收：任务服务测试覆盖成功、拒绝、冲突、无权限、找不到对象五类路径。

## 第三阶段：团队服务测试

- 保留并扩充本地团队流程测试：创建、邀请、申请、接受、踢出、退出、转让队长、多团队成员关系。
- 覆盖非法操作：普通成员不得踢管理员、不得转让队长、不得编辑外部同步团队成员。
- 覆盖外部源降级：BQ/GTNHLib provider 不可用时不崩服，团队标为 `STALE` 并保留快照。
- 覆盖解除关联：owner 或 OP 可将 `STALE` 团队转回 `LOCAL`，成员快照继续可用。
- 覆盖登录轻量同步入口：`syncLinkedTeamsForPlayer` 不因外部 Mod 缺席抛异常。

验收：无 BetterQuesting、无 GTNHLib 时，团队服务测试仍可全过。

## 第四阶段：任务标签测试

- 保留并扩充 `TagService` 测试：创建标签、复用同名可见标签、绑定任务、解绑任务、去重、计数更新。
- 覆盖标签作用域：`PUBLIC`、`PRIVATE`、`TEAM`、`SYSTEM` 的创建与使用权限。
- 覆盖团队标签：团队标签只能绑定同团队任务；非成员不可读写。
- 覆盖按标签查任务：结果须再经 `TaskService.canViewTask` 过滤。
- 覆盖网络动作：`add_tag`、`add_tag_by_name`、`remove_tag`、`set_tags` 的成功与错误码。

验收：每个 task 可拥有多个分类标签；标签同步 NBT 可被客户端缓存读取。

## 第五阶段：网络协议测试

- 为 `PacketAssembly` 补单测：小包直发、大包切片、乱序分片、缺片不过早交付、过期清理。
- 为 `PacketTypeRegistry` 补单测：未知 `ID` 返回错误或安全忽略；server/client handler 不串侧。
- 为登录同步补集成测试或手测脚本：reset 后再推任务、团队、邀请；客户端缓存不留旧数据。
- 为错误包补测试：`PERMISSION_DENIED`、`INVALID_PAYLOAD`、`VERSION_CONFLICT`、`NOT_FOUND`、`SERVER_ERROR`。
- 检查 dedicated server 类加载：common/network/task/service 包不得直接引用 `net.minecraft.client`。

验收：网络层不再依赖 Kryo Object 大同步；无客户端类加载风险。

## 第六阶段：专服与实机验证

- 跑无 BQ、无 GTNHLib 的 dedicated server：启动、建库、玩家登录、创建任务、创建本地团队、添加标签。
- 跑有 BQ 的环境：关联 BQ party、成员/角色单向同步、BQ 缺席后标记 `STALE`。
- 跑有 GTNHLib Team API 的环境：按 `teamName` 关联、owner/officer/member 映射、旧版 GTNHLib 不崩。
- 跑客户端多人手测：两名玩家同时操作任务状态、团队成员、标签，确认服务端权威。
- 跑世界保存/重启：`main.db` 可保存、恢复、迁移，数据不丢。

验收：专服 30 分钟基础操作无崩溃；重启后任务、团队、标签仍在。

## 第七阶段：发布前清理

- 降低 SQL 高频日志：正式版中每条 SQL 的 `info` 改为 `debug` 或配置开关。
- 清理旧网络残件：确认 GUI 与命令不再使用旧 `NetWorkData/NetWorkHelper` 后删除或标注 deprecated。
- 复核命令入口：`/task add/list/remove/update/done` 与服务层权限一致。
- 复核语言文件：新增错误码、团队、标签相关文案进入 `zh_CN.lang` 与 `en_US.lang`。
- 复核许可证与依赖：SQLite JDBC、ModularUI2、Kryo、Gson、Reflections、Lombok 许可可发布。

验收：发布包无明显调试残留；用户可从日志看懂错误，不暴露内部噪音。

## 第八阶段：发布打包

- 更新 README：安装、依赖、快捷键、命令、数据库路径、备份、团队/BQ/GTNHLib 说明。
- 更新 `mcmod.info` 与版本号；写 changelog。
- 跑最终命令：`gradlew.bat clean build spotlessCheck --no-daemon`。
- 记录构建产物名、校验和、测试结论与已知问题。
- 打 tag 前检查：工作区干净、CI 可过、无临时测试文件。

验收：可交付 jar、changelog、README 与已知问题清单齐备。
