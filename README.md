# Chest Cavity Legacy

作者: Shiver211  
原作者: [TigerOfTroy](https://www.curseforge.com/members/tigeroftroy/projects)  
许可证: Apache License 2.0

## 简介

Chest Cavity Legacy 为 Minecraft 1.12.2 的生物加入可交互的器官系统。生物拥有胸腔，玩家可以用开胸器打开、查看和替换器官；器官会提供 score，score 会进一步影响生命、战斗、移动、食物、药水、主动能力等行为。

当前版本已经完成一次大规模架构重构：器官、胸腔类型、实体分配、胸腔布局和槽位规则统一进入内容声明层，reload 后编译成运行期只读快照。CrT 和 JSON 不再直接修改旧 registry，运行时实体状态也统一通过 mutation 提交。

## 核心机制

### 胸腔状态

每个 `EntityLivingBase` 会通过 Forge Capability 挂载胸腔状态。状态层只保存实体自己的数据：

- 是否已开胸
- 器官槽位中的 `ItemStack`
- 兼容性 ID
- 计时器和运行时版本号
- 打开后的 layout slot 数量

未开胸实体不会常驻 27 个空槽，也不会在 NBT 中写入空 `Inventory`。

### 器官和 Score

器官由 JSON 或 CrT 声明，每个器官绑定一个物品 ID 和一组 score。运行时会根据胸腔类型、器官、堆叠数量和兼容性构建 `ChestCavityRuntime` 快照。

重构后 score / baseline / delta 以数组作为运行时真源，字符串 Map 只在 API、UI、CrT 需要展示时懒生成。

### 兼容性

- 无兼容标记的器官可以通用移植。
- 被绑定到宿主的器官只完全兼容原宿主。
- 不兼容器官会提供 `incompatibility`，进而触发器官排斥。
- O Negative 附魔仍提供通用兼容和死亡保留相关行为。

### Layout 和 Slot Rules

胸腔不再固定为 27 格。每个 body type 可以绑定 layout，layout 控制：

- 槽位数量
- 每行槽位数
- ModularUI 面板尺寸和标题位置
- 第一个槽位位置和槽位间距
- layout 变化时的迁移策略
- 每个槽位的规则

slot rule 支持：

- 禁用槽位
- 只允许指定物品
- 只允许带指定 score 的器官
- 限制最小/最大堆叠数

服务端插入、ItemHandler 和 ModularUI 使用同一套规则。

## 获取和使用

### 工具

- 开胸器: 打开符合条件的生物胸腔。
- 剁刀: 用于屠宰和提高器官获取效率。

### 附魔

| 附魔 | 最高等级 | 效果 |
|---|---:|---|
| O Negative | II | I 级提高兼容性，II 级可在死亡时保留器官 |
| Surgical | III | 提高器官掉落收益 |
| Malpractice | I | 掉落器官带不兼容风险 |
| Tomophobia | I | 阻止器官掉落 |

### 快捷键

类别: `Chest Cavity Organ Abilities`

| 快捷键 | 默认按键 |
|---|---|
| 释放能力 | X |
| 打开能力轮盘 | R |

## 数据驱动

内置数据位于：

```text
assets/chestcavity/chestcavity_data/
├── organs/
├── types/
├── layouts/
└── entity_assignment/
```

整合包可在以下目录放置自定义 JSON：

```text
config/chestcavity/data/
```

### Organ JSON

```json
{
  "itemID": "minecraft:apple",
  "pseudoOrgan": false,
  "organScores": [
    { "id": "health", "value": 2.0 },
    { "id": "nutrition", "value": 1.0 }
  ]
}
```

### Type JSON

```json
{
  "layoutId": "chestcavity:wide",
  "playerChestCavity": false,
  "bossChestCavity": false,
  "dropRateMultiplier": 1.0,
  "baseOrganScores": [
    { "id": "health", "value": 20.0 }
  ],
  "defaultChestCavity": [
    { "position": 0, "item": "chestcavity:heart", "count": 1 }
  ]
}
```

### Layout JSON

```json
{
  "id": "chestcavity:wide",
  "slotCount": 36,
  "slotsPerRow": 12,
  "panelWidth": 230,
  "panelHeight": 168,
  "titleX": 8,
  "titleY": 6,
  "firstSlotX": 8,
  "firstSlotY": 18,
  "slotSpacingX": 18,
  "slotSpacingY": 18,
  "migrationStrategy": "KEEP_BY_INDEX",
  "slotRules": [
    {
      "slot": 0,
      "allowedScores": ["health"],
      "minStackSize": 1,
      "maxStackSize": 1
    }
  ]
}
```

迁移策略：

- `KEEP_BY_INDEX`
- `DROP_OVERFLOW`
- `MOVE_TO_PLAYER`
- `CLEAR`
- `SCRIPTED_MIGRATION` 当前降级为 keep-by-index

### Entity Assignment JSON

```json
{
  "chestcavity": "human",
  "entities": [
    "minecraft:player",
    "minecraft:villager"
  ]
}
```

## CraftTweaker

CrT API 见 [ChestCavity-CRT-API.md](ChestCavity-CRT-API.md)。

当前 CrT 入口主要用于声明内容和提交实体 mutation：

- `mods.chestcavity.OrganData`
- `mods.chestcavity.ChestCavityType`
- `mods.chestcavity.ChestLayout`
- `mods.chestcavity.EntityAssignment`
- `mods.chestcavity.DropManager`
- `mods.chestcavity.ScoreManager`
- `mods.chestcavity.AbilityManager`
- `mods.chestcavity.ChestCavityHelper`

## 本次重构的主要改进

- 内容层统一: JSON、内置数据和 CrT 都写入 `ContentManifest`，reload 后编译为 `CompiledContent`。
- 删除旧 registry 直写路径: 不再依赖 `GeneratedChestCavityType`、`FallbackChestCavityType`、`RUNTIME_OVERRIDES` 或 `OrganData` 静态 registry。
- 运行期只读快照: runtime 从实体状态和 compiled content 构建，减少 reload、缓存和同步风险。
- mutation 入口统一: 槽位修改、开胸、死亡重置和 UI 操作统一经 `ChestCavityMutations` 提交。
- score 更轻量: 运行时用数组保存 score / baseline / delta，Map 只做外部视图。
- 行为 pipeline 化: 战斗、食物、药水和 passive tick 已大幅收束为 spec/effect pipeline。
- layout 规则化: 胸腔 layout 不再只是 UI 坐标，而是决定 slot 数量、可放置内容和迁移策略。
- 网络同步瘦身: chest cavity sync 发送 `BodyUiSnapshot`，不再直接全量传 capability NBT。

## 仍在规划中的工作

- 将 builtin ability/effect spec 进一步注册到 `ContentManifest`，开放 JSON/CrT 声明。
- 在 `BodyUiSnapshot` 基础上继续拆 slot delta / score delta message。
- 将 score version 比较从 Map 视图比较改为数组版本比较或 dirty bit。

## 前置 Mod

- [CraftTweaker](https://www.curseforge.com/minecraft/mc-mods/crafttweaker)
- [CleanroomMC ModularUI](https://github.com/CleanroomMC/ModularUI)
