# plan

## 目标

以现骨架为基，成一套可在多人服务器中可靠运行之待办事项系统。其核心原则为：服务端持有真相，SQLite 按世界持久化，客户端只发操作意图与展示同步结果；所有任务变更须经权限、事务、审计与同步。外部组队以 provider 隔离：本地多团队长期存在，BetterQuesting 与 GTNHLib Team 均只作为可选成员源。

## 第零阶段：基线整理

- 跑通 `./gradlew build`、`./gradlew spotlessCheck`，记录当前失败项。
- 明确 `docs/TaskSqlHelper使用指南.md` 之去留；若保留，改为现行 `SQLHelper` / `TaskService` 用法。
- 清点是否有生成产物误入仓库，如 `.gradle`、`bin`、`run`、IDE 文件等；若已被跟踪，另开清理提交。
- 确认 `usesMixins=false` 与 `mixins.wthaigd.json` 是否矛盾，决定删除、启用或迁至正确资源。
- 建 `docs/` 为正式文档目录，后续设计、协议、迁移记录均置此处。

验收：干净构建结果可复现；旧文档不再误导；仓库目录职责明确。

## 第一阶段：数据库层加固

- 增 `schema_version` 表，建立有序迁移列表；每次迁移须幂等、可日志追踪。
- 重构 `SQLiteManager.executeSafeSQL`：查询不要裸返 `ResultSet`；改为回调、收集器或由 DAO 层负责关闭资源。
- 为 `SQLHelper` 与 Builder 加最小测试：建表、枚举 CHECK、UUID、外键、索引、IN/NOT IN、排序、分页、update-by-id、delete 防空条件。
- 统一 ID 表示：优先定 `UUID` 为领域 ID，入库可为 `TEXT`；若保留 `String`，需写明转换边界。
- 增事务 API：`SQLiteManager.transaction(...)` 或 `SQLHelper.transaction(...)`，服务层多表写入必须使用。
- 梳理 `UtilHelper.convertValue`：覆盖 `UUID`、`LocalDateTime`、`Duration`、枚举、Boolean、Integer/Long。

验收：新世界建库、旧世界迁移、任务 CRUD、状态变更历史写入均有自动测试或可重复验证脚本。

## 第二阶段：服务层定型

- 定 `TaskService` 为唯一业务入口，DAO 只管持久化，不承载权限与流程。
- 为任务操作制定契约：创建、编辑、删除、完成、关闭、延期、阻塞、恢复、创建子任务、移动子任务。
- 引入操作者上下文：玩家 UUID、是否 OP、所在团队、团队角色。
- 实作权限校验：私人任务、团队任务、公开任务；团队成员、管理员、所有者权限。
- 状态变更必须同时写 `StatusChangeRecord`，并以事务保证原子性。
- 完成计数、关注计数、评论计数、团队统计、玩家统计须由服务层统一维护。
- `Task.version` 作为乐观锁：更新请求须带客户端所见版本，冲突时返回明确错误。

验收：所有写操作均经服务层；状态流转有历史；并发冲突不会静默覆盖。

## 第三阶段：多人网络协议

- 采用类 BetterQuesting 架构：单一 `WthaigdPacket` 承载 `NBTTagCompound`，以 `ResourceLocation ID` 分派至 `PacketTypeRegistry`。
- 大包由 `PacketAssembly` 压缩并按 `20480` 字节切片，收包后按玩家 UUID 重组；业务包不再使用 Kryo `Object`。
- 固定包族为 `wthaigd:main_sync`、`task_action`、`task_sync`、`team_action`、`team_sync`、`invite_sync`、`error`。
- 网络线程只解包和排队，业务写库经 `ServerTaskScheduler` 切回服务端 tick 执行。
- 登录同步流为：服务端发 reset，客户端回应 `main_sync`，服务端按权限推送团队、任务、邀请；客户端写入 `TaskClientStore`。
- 后续增强：给 `task_action`、`team_action` 增 `requestId`、`clientVersion`、错误码与冲突返回；任务同步改为可分页、可按 `update_time` 增量。

验收：远程客户端写操作只在服务端落库；小包与大包皆可同步；未知包 ID 与业务失败均返回 `error`。

## 第四阶段：GUI 接入同步状态

- 为客户端建 `TaskClientStore`：保存任务列表、子任务索引、加载状态、错误状态、待确认操作。
- `MainPanel`、`TaskFormPanel`、`TaskDetailPanel` 改走 client store 与网络请求，不直接调用 `TaskService`。
- GUI 增加载中、失败重试、版本冲突提示、无权限提示。
- 全部硬编码英文移入 `zh_CN.lang` / `en_US.lang`。
- 整理子任务树：支持懒加载、递归展开、排序缓存失效。
- 删除操作加确认面板，防误触。

验收：单机与多人服同一套 UI 逻辑可用；断线或请求失败不会造成假成功。

## 第五阶段：协作功能闭环

- 本地团队为长期模型：玩家可属多个团队；`TeamService` 统一处理创建、邀请、申请、接受、踢出、退出、转让队长。
- 团队成员权限先定三档：队长以 `Team.ownerId` 表示，成员表角色为 `ADMIN/MEMBER/GUEST`；管理员可管成员，普通成员可建团队任务。
- `Team.syncSource` 标记 `LOCAL/BETTER_QUESTING/GTNH_LIB`；本地团队可编辑成员，外部关联团队成员只读。
- `Team.externalPartyId` 保留 BQ int party 兼容；`Team.externalTeamKey` 为通用外部团队键，GTNHLib Team 使用 `teamName`。
- `TeamProvider` 对核心服务暴露通用 `teamKey` 接口，BQ 的 int party 仅作 provider 内部兼容，避免将来 GTNHLib 重写时牵动服务层。
- `TeamRequest` 承载本地申请与邀请；后续接入通知面板前，网络层先可推送空邀请列表与错误包。
- 标签、互动、评论、提醒、进度更新、积分奖励在团队权限稳定后再接入；这些写操作均须走服务层权限上下文。
- 周期任务先支持简单 interval，再扩展 RRULE。

验收：无 BQ 时，本地多团队可创建、申请、邀请、接受、踢出、退出、转让队长；团队任务按角色校验。

## 第六阶段：GTNHLib Team / BetterQuesting 联动与扩展

- GTNHLib Team 为未来权威方向：参考 GTNewHorizons/GTNHLib#297，`TeamManager` 以 `teamName` 查队伍，`Team` 持有 owners/officers/members，模组数据经 `TeamDataRegistry.register(key, Supplier)` 挂载。
- GTNHLib 接入置于 `integration.gtnhlib`，用 `Loader.isModLoaded("gtnhlib")` 与反射守门；PR 未合并或旧版 GTNHLib 缺 Team API 时，静默退化。
- 预留 `GtnhLibTeamDataBridge`，以 `wthaigd` 为 data key 注册空壳 team data；先只保 marker，后续可承载任务统计、团队设置或迁移游标。
- BQ 为可选旧集成：所有访问置于 `integration.betterquesting`，用 `Loader.isModLoaded("betterquesting")` 与反射守门，缺席时不得崩服。
- 队长可将 wthaigd 团队关联至 BQ party；操作者须为 wthaigd 队长，且为 BQ party OWNER，或为 OP。
- 队长亦可将 wthaigd 团队关联至 GTNHLib team；操作者须为 wthaigd 队长，且为 GTNHLib owner，或为 OP。
- 关联后外部队伍为成员与角色权威：BQ OWNER/ADMIN 与 GTNHLib owner/officer 映射 `ADMIN`，普通 member 映射 `MEMBER`。
- 同步时机：玩家登录、客户端请求团队同步、刚关联、服务端定时轮询；权限判断前可先做轻量同步。
- 外部源缺席、队伍消失或读取失败时，团队标为 `STALE`，保留最后快照；队长或 OP 可解除关联转回本地团队。
- 任务书进度暂不双向同步；后续只在此阶段之后设计 `TaskType.QUEST_FINISH` 或独立关联表。
- 对外暴露轻量 API，供其他 Mod 查询或创建待办；思维导图、甘特图、时间轴作为只读或服务层写入的独立视图。

验收：有 BQ 时可单向同步队员；无 BQ 时专服可启动；有 GTNHLib Team API 时可按 teamName 关联、同步 owner/officer/member；外部队伍变更后，wthaigd 下次同步反映变更。

## 第七阶段：发布前硬化

- 补集成测试：新世界、已有世界、客户端登录、服务端关闭保存、断线重连。
- 做长时间运行测试：高频任务创建、世界保存、多人同时操作。
- 压缩日志：移除正式版中每条 SQL 的 info 输出，改为 debug。
- 校验 dedicated server：不得加载客户端类，不得引用 client-only 资源。
- 完成 README 与使用文档：安装、命令、快捷键、权限、备份与迁移说明。
- 建 release checklist：版本号、changelog、许可证、依赖关系、CI 结果。

验收：专服可稳定启动；多人基础流程可连续操作；数据重启后仍在；发布包无明显调试残留。

## 近期实施顺序

1. 先修文档与构建基线，确认当前能否编译。
2. 其次补 DB 测试与迁移版本表。
3. 再将 `TaskService`/`TeamService` 全部写操作收束到操作者上下文、事务与权限。
4. 随后让 GUI 改走 `task_action`/`team_action` 与 `TaskClientStore`，不再直接写服务层。
5. 最后扩通知、标签、奖励、GTNHLib TeamData 内容、BetterQuesting 任务书关联与可视化视图。

此序之意，在先定地基，再筑楼阁。若先扩界面与玩法，多人同步与权限一改，前功多须重写。
