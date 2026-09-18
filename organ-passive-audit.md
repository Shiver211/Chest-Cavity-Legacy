# 器官被动效果审查

审查日期：2026-09-18  
范围：当前仓库内全部器官分数被动效果的触发路径、与原版 Chest Cavity 语义的对照，以及已修复项。

---

## 已修复（审查条目 1–7）

### 1. 抗冲击 / 跳跃减摔落伤 / 着火持续伤害被 `isUnblockable` 挡掉

**原问题：** `OrganCombatController.applyDefense()` 对 `DamageSource.isUnblockable()` 整段提前返回。1.12 中 `FALL`、`FLY_INTO_WALL`、`ON_FIRE` 都无视护甲，导致：

- `impact_resistant` 对摔落和撞墙无效
- `leaping` 的落地减伤无效
- `fire_resistant` 对身上着火的 DoT 无效（站在火里 / 岩浆仍可能生效）

骨头防御本应是唯一被「无视护甲」跳过的项。

另外 `LivingHurtEvent` 发生在护甲之前，减伤打在裸伤上，比原版「护甲后再减」更强。玩家走 `ISpecialArmor.applyArmor`，生物走 `applyArmorCalculations`，两条路径不一致。

**修复：**

- 仅对骨头防御跳过 unblockable；火焰、摔落、撞墙抗性始终计算
- 生物：混入 `applyArmorCalculations` 返回值（护甲之后）
- 玩家：Redirect `ISpecialArmor.ArmorProperties.applyArmor` 之后再套器官防御
- 移除 `LivingHurtEvent` 上的重复挂钩

### 2. 耐力 `endurance` 挂钩时机错误

**原问题：** 在 `LivingUpdateEvent` 里对 `foodExhaustionLevel` 做差值缩放。1.12 同一 tick 顺序是：LivingUpdate → 疾跑/跳跃 `addExhaustion` → `FoodStats.onUpdate` 在 exhaustion>4 时扣饱食。真正造成饥饿消耗的那一次增量会在同 tick 被原版吃掉，耐力几乎不生效。

**修复：** 混入 `FoodStats.addExhaustion`，在消耗被加入时按耐力差缩放，与原版一致。

### 3. 轻量化 / 浮力只在服务端改 `motionY`

**原问题：** `tickPassiveEffects()` 包在 `!world.isRemote` 里，每 tick 乘 `motionY` 并改 `fallDistance`。客户端预测仍是原版坠落，玩家会橡皮筋。负轻量化还会指数放大摔落距离。原版是改 `travel()` 重力，并用 `Entity.fall` 修正摔落高度。

**修复：**

- 轻量化：混入 `EntityLivingBase.travel` 的 `0.08D` 重力常量（双端）
- 摔落距离：混入 `Entity.updateFallState` 的竖直位移，按轻量化+浮力缩放
- 浮力：双端 `motionY += lift`，创造飞行 / 着地 / 无重力时跳过

### 4. 亲水过敏对未开胸生物也会跳

**原问题：** 原版器官 tick 只在 `opened == true` 时运行。当前 `tickPassiveEffects()` 不看开胸状态，未开胸实体使用默认分数。末影人/烈焰人默认布局带大量 `hydroallergenic`，淋雨会被挂上 `WaterVulnerability`，与原版水伤叠加。

**修复：** 被动 tick 要求胸腔已打开；未开胸时清掉已有的 `WaterVulnerability`。

### 5. 神经为 0 时不会锁移动

**原问题：** 原版脊柱分数 ≤ 0 时给移动速度挂 `MULTIPLY_TOTAL = -1`（不能走）。当前只改了攻击速度和挖掘速度，拆掉脊柱后仍能走路。

**修复：** 当种族默认神经不为 0 且当前神经 ≤ 0 时，对移动速度施加 `operation 2`（Multiply Total）`-1`。

### 6. 击退抗性不能低于种族默认值

**原问题：** 差值被 `Math.max(0, …)` 钳住。铁傀儡拆掉核心后不会变好推。

**修复：** 使用 `当前分数 - 默认分数`，允许负修正。

### 7. 消化会错误地缩放饱和度

**原问题：** `addStats(digestedHunger, satMod)` 让饱和增量也乘上消化后的饥饿。原版饱和按「原始饥饿 × 营养」计算，与消化独立。胃很好时食物过饱和，胃很差时饱和被额外削弱。

**修复：** 按原版公式反推 `addStats` 的饱和修正值，使最终饱和增量 = `nutrition/4 × 原版饥饿 × 2`。

---

## 仍可正确触发的被动

胸腔已打开、且分数相对默认有差（或绝对分 > 0）时：

| 分数 | 触发 | 状态 |
|---|---|---|
| `health` / `strength` / `speed` / `luck` / `nerves`（攻速） | 属性修正 | 正常 |
| `swim_speed` | Forge `SWIM_SPEED` | 正常 |
| `mining_speed` + 神经挖掘 | `BreakSpeed` | 正常（默认器官几乎不带 `mining_speed`） |
| `defense` | 非 unblockable 伤害，护甲之后 | 已修挂钩顺序 |
| `fire_resistant` | 火焰伤害，含 `ON_FIRE` | 已修 |
| `impact_resistant` / `leaping` 落地减伤 | 摔落、撞墙 | 已修 |
| `leaping` 起跳 | `LivingJumpEvent` | 正常 |
| 缺心出血 / 肾毒 / 排异 | 已开胸 tick | 正常 |
| 消化 / 营养 / 肉食 / 草食 / 腐烂 | 吃 `ItemFood` | 饥饿与反胃正常；饱和已修 |
| `metabolism` | `FoodStats.onUpdate` | 正常 |
| `endurance` | `addExhaustion` | 已修 |
| `breath_*` / `water_breath` | 水下 `decreaseAirSupply`、陆地替换 `setAir(300)` | 正常 |
| 陆地氧气条 | `MixinGuiIngameForge` | 没肺时可见 |
| `glowing` | 已开胸 tick | 正常 |
| `photosynthesis` | 光照累积 | 正常 |
| `crystalsynthesis` | 附近末地水晶 | 能绑能回；实体 ID 不写 NBT |
| `arrow_dodging` | `LivingAttackEvent` | 正常（原版 1.16 投射物判断是反的，这边是修过的） |
| `buff_purging` / `withered` / `detoxification` | `PotionApplicableEvent` | 正常 |
| `launching` | 近战 `LivingDamageEvent` | 正常 |
| `venomous` | 近战命中 | 能触发；原版还要求空手 |
| 水瓶溅射过敏/恐水 | 喷溅药水 | 只对已开胸 |
| 丝腺挤奶/剪毛 | 桶/剪刀 | 正常 |
| 苦力怕无 `creepy` | 已开胸强制灭引线 | 正常 |
| `ease_of_access` | 开胸条件 | 正常 |
| `lightweight` / `buoyant` | 重力 / 浮力，双端 | 已修 |

---

## 未改的已知差异与次要问题

这些不在本次 1–7 修复范围内，记录备查。

1. **亲水过敏首伤延迟**  
   现为无限时长药水 DoT，首次伤害要等 `duration % rate == 1`（大约十几秒）。原版是一沾水立刻打一下再进冷却。

2. **恐水传送节流**  
   现为每 20 tick 试一次；原版湿身每 tick 都试。能触发，只是没那么频繁。

3. **毒腺触发条件**  
   现为任意近战（`immediateSource == trueSource`）。原版要求空手，羊驼唾液例外。拿武器也会上毒。

4. **浮力主动呼气**  
   仍是主动技能，只消耗空气以降低浮力；被动浮力已双端生效。

5. **结晶合成**  
   连接用实体 ID，不写入 NBT。区块卸载/重进会断线。水晶被摧毁时按原版会挨一次饥饿伤害。满饥饿且不需治疗时不再空 `heal`。

6. **`destructive_collisions`**  
   代码存在，默认器官 JSON 没有这个分数，除非 CRT 添加否则不会出现。

7. **主动技能（不按被动判）**  
   `creepy` / `explosive`、`pyromancy`、`dragon_bombs` / `dragon_breath`、`forceful_spit`、`ghastly`、`shulker_bullets`、主动吐丝、`furnace_powered`、`iron_repair`、`grazing`。  
   `explosive` 只在自爆主动技里消耗器官；苦力怕自然爆炸不会按该分数缩放（原版对应逻辑也是注释掉的）。

8. **陆地呼吸客户端 RNG**  
   水下呼吸附魔的陆地判定用实体 RNG，极端情况下可能双端气值不同步。伤害仍只在服务端结算。

---

## 涉及的主要代码

| 区域 | 文件 |
|---|---|
| 防御公式 | `organ/OrganCombatController.java` |
| 防御挂钩 | `mixin/MixinEntityLivingBase.java`、`mixin/MixinEntityPlayer.java` |
| 耐力 / 进食 | `organ/OrganFoodController.java`、`mixin/MixinFoodStats.java` |
| 重力 / 浮力 / 摔落 | `organ/OrganMovementController.java`、`mixin/MixinEntity.java`、`mixin/MixinEntityLivingBase.java` |
| 被动 tick / 开胸门闩 | `organ/OrganTickController.java` |
| 属性（神经锁移动、击退） | `organ/OrganAttributeController.java` |
| 纯公式 | `organ/OrganFormulas.java` |
| 公式测试 | `src/test/java/.../OrganFormulasTest.java` |
