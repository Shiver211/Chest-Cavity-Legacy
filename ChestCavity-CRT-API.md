# Chest Cavity Legacy - CraftTweaker API

本文档描述当前重构后可用的 CraftTweaker API。旧文档中的 score 覆盖入口已经移除；CrT 现在主要用于声明内容、绑定实体、定义 layout/slotRules，以及通过胸腔对象提交实体 mutation。

所有接受 `float[string]` 的 API 都建议显式写 `as float[string]`，避免 ZenScript 将小数推断为 double。

## 内容注册行为

重构后 CrT 不再直接写旧 registry。脚本注册 organ/type/layout/entity assignment 时，会写入内容操作流，并发布到 `ContentManifest -> CompiledContent`。

这带来几个变化：

- JSON 和 CrT 共用同一条内容编译路径。
- reload 后内容顺序更稳定。
- runtime 查询只读 compiled snapshot。
- CrT 不再需要旧的 runtime override replay。

## 1. OrganData

ZenClass: `mods.chestcavity.OrganData`

### 方法

| API | 说明 |
|---|---|
| `OrganData.register(item, scores)` | 注册普通器官 |
| `OrganData.registerPseudo(item, scores)` | 注册伪器官，不参与归属/排斥 |
| `OrganData.addScore(item, scoreId, value)` | 为器官增加或覆盖一个 score |
| `OrganData.removeScore(item, scoreId)` | 移除器官 score |
| `OrganData.remove(item)` | 移除器官定义 |
| `OrganData.setPseudo(item, value)` | 设置是否为伪器官 |
| `OrganData.get(item)` | 获取 `OrganDataView`，不存在则返回 null |

### OrganDataView

ZenClass: `mods.chestcavity.OrganDataView`

| Getter | 类型 | 说明 |
|---|---|---|
| `isPseudoOrgan` | bool | 是否为伪器官 |
| `organScores` | float[string] | 器官 score 视图 |

### 示例

```zenscript
import mods.chestcavity.OrganData;

OrganData.register(<minecraft:apple>, {
    "health": 2.0,
    "nutrition": 1.0
} as float[string]);

OrganData.registerPseudo(<minecraft:gold_nugget>, {
    "ease_of_access": 4.0
} as float[string]);

OrganData.addScore(<minecraft:apple>, "luck", 0.5);

var data = OrganData.get(<minecraft:apple>);
if (!isNull(data)) {
    print("pseudo = " ~ data.isPseudoOrgan);
}
```

## 2. ChestCavityType

ZenClass: `mods.chestcavity.ChestCavityType`

### 方法

| API | 说明 |
|---|---|
| `ChestCavityType.register(typeId)` | 注册或取得一个 body type 声明 |
| `ChestCavityType.addBaseScore(typeId, scoreId, value)` | 增加基础 score |
| `ChestCavityType.removeBaseScore(typeId, scoreId)` | 移除基础 score |
| `ChestCavityType.setSlot(typeId, index, stack)` | 设置默认胸腔槽位 |
| `ChestCavityType.clearSlots(typeId)` | 清空默认胸腔 |
| `ChestCavityType.addForbiddenSlot(typeId, slot)` | 添加 type 级禁用槽位 |
| `ChestCavityType.removeForbiddenSlot(typeId, slot)` | 移除 type 级禁用槽位 |
| `ChestCavityType.setDropRateMultiplier(typeId, value)` | 设置器官掉落倍率 |
| `ChestCavityType.setBossChestCavity(typeId, value)` | 设置 Boss 胸腔标记 |
| `ChestCavityType.setPlayerChestCavity(typeId, value)` | 设置玩家胸腔标记 |
| `ChestCavityType.setLayout(typeId, layoutId)` | 绑定 layout |
| `ChestCavityType.addExceptionalOrgan(typeId, item, scores)` | 为指定物品添加特殊器官 score |
| `ChestCavityType.addExceptionalOrganByOre(typeId, oreName, scores)` | 为矿辞添加特殊器官 score |
| `ChestCavityType.clearExceptionalOrgans(typeId)` | 清空特殊器官规则 |
| `ChestCavityType.isBossChestCavity(typeId)` | 查询 Boss 标记 |
| `ChestCavityType.isPlayerChestCavity(typeId)` | 查询玩家标记 |
| `ChestCavityType.getDropRateMultiplier(typeId)` | 查询掉落倍率 |

### 示例

```zenscript
import mods.chestcavity.ChestCavityType;
import mods.chestcavity.EntityAssignment;

ChestCavityType.register("demon");
ChestCavityType.setBossChestCavity("demon", true);
ChestCavityType.setDropRateMultiplier("demon", 3.0);
ChestCavityType.setLayout("demon", "chestcavity:demon_grid");

ChestCavityType.addBaseScore("demon", "health", 20.0);
ChestCavityType.addBaseScore("demon", "fire_resistant", 1.0);

ChestCavityType.setSlot("demon", 0, <chestcavity:dragon_heart>);
ChestCavityType.setSlot("demon", 1, <chestcavity:dragon_lung>);

ChestCavityType.addExceptionalOrgan("demon", <minecraft:nether_star>, {
    "health": 10.0,
    "regeneration": 2.0
} as float[string]);

EntityAssignment.register("minecraft:wither_skeleton", "demon");
```

## 3. ChestLayout

ZenClass: `mods.chestcavity.ChestLayout`

Layout 控制 ModularUI 尺寸、槽位坐标、slot 数量和 per-slot rule。type 通过 `ChestCavityType.setLayout(typeId, layoutId)` 绑定 layout。

### 方法

| API | 说明 |
|---|---|
| `ChestLayout.registerGrid(layoutId, slotCount, slotsPerRow, panelWidth, panelHeight, titleX, titleY, firstSlotX, firstSlotY, slotSpacingX, slotSpacingY)` | 注册网格 layout |
| `ChestLayout.registerGrid(..., migrationStrategy)` | 注册网格 layout，并指定迁移策略 |
| `ChestLayout.setSlotRule(layoutId, slot, forbidden, allowedItems, allowedScores, minStackSize, maxStackSize)` | 设置槽位规则 |

### migrationStrategy

- `KEEP_BY_INDEX`
- `DROP_OVERFLOW`
- `MOVE_TO_PLAYER`
- `CLEAR`
- `SCRIPTED_MIGRATION` 当前降级为 keep-by-index

### Slot Rule

`allowedItems` 是 string 数组，元素为物品 ID。  
`allowedScores` 是 string 数组，元素为 score ID。

如果 `allowedItems` 非空，槽位只允许这些物品。  
如果 `allowedScores` 非空，槽位只允许带有任意指定 score 的器官。  
`forbidden = true` 会禁用该槽位。

### 示例

```zenscript
import mods.chestcavity.ChestLayout;
import mods.chestcavity.ChestCavityType;

ChestLayout.registerGrid(
    "chestcavity:demon_grid",
    36, 12,
    230, 168,
    8, 6,
    8, 18,
    18, 18,
    "DROP_OVERFLOW"
);

ChestLayout.setSlotRule(
    "chestcavity:demon_grid",
    0,
    false,
    [] as string[],
    ["health"] as string[],
    1,
    1
);

ChestLayout.setSlotRule(
    "chestcavity:demon_grid",
    35,
    true,
    [] as string[],
    [] as string[],
    0,
    0
);

ChestCavityType.setLayout("demon", "chestcavity:demon_grid");
```

## 4. EntityAssignment

ZenClass: `mods.chestcavity.EntityAssignment`

| API | 说明 |
|---|---|
| `EntityAssignment.register(entityId, typeId)` | 为实体绑定 body type |
| `EntityAssignment.unregister(entityId)` | 移除实体绑定 |
| `EntityAssignment.getAssignedType(entityId)` | 查询实体绑定 |

```zenscript
import mods.chestcavity.EntityAssignment;

EntityAssignment.register("twilightforest:wild_boar", "carnivore");
EntityAssignment.unregister("minecraft:zombie");

var typeId = EntityAssignment.getAssignedType("minecraft:villager");
```

## 5. DropManager

ZenClass: `mods.chestcavity.DropManager`

注意：DropManager 仍是独立掉落 API，尚未进入 `CompiledContent`。

| API | 说明 |
|---|---|
| `DropManager.addOrganDrop(entityId, stack, weight)` | 添加额外掉落条目 |
| `DropManager.setDropProbability(entityId, value)` | 设置额外掉落概率 |
| `DropManager.removeOrganDrop(entityId, stack)` | 移除指定掉落 |
| `DropManager.removeAllOrganDrops(entityId)` | 清空实体额外掉落 |

```zenscript
import mods.chestcavity.DropManager;

DropManager.addOrganDrop("minecraft:zombie", <chestcavity:heart>, 5);
DropManager.setDropProbability("minecraft:zombie", 0.25);
```

## 6. ScoreManager

ZenClass: `mods.chestcavity.ScoreManager`

| API | 说明 |
|---|---|
| `ScoreManager.addScore(scoreId, displayName)` | 注册 score 显示名 |

```zenscript
import mods.chestcavity.ScoreManager;

ScoreManager.addScore("spell_power", "Spell Power");
```

## 7. AbilityManager

ZenClass: `mods.chestcavity.AbilityManager`

Ability 使用 scoreId 作为能力 ID。注册后会进入能力轮盘；带 handler 的能力激活时可取消。

| API | 说明 |
|---|---|
| `AbilityManager.registerAbility(scoreId, displayName)` | 注册能力，无冷却 |
| `AbilityManager.registerAbility(scoreId, displayName, cooldownTicks)` | 注册能力并设置冷却 |
| `AbilityManager.registerAbility(scoreId, displayName, handler)` | 注册能力并设置回调 |
| `AbilityManager.registerAbility(scoreId, displayName, cooldownTicks, handler)` | 注册能力、冷却和回调 |

```zenscript
import mods.chestcavity.AbilityManager;
import mods.chestcavity.event.AbilityActivatedEvent;

AbilityManager.registerAbility("fire_blast", "Fire Blast", 120, function(event as AbilityActivatedEvent) {
    if (event.score < 1.0) {
        event.cancel();
        return;
    }
    event.entity.sendMessage("Fire Blast score: " ~ event.score);
});
```

## 8. ChestCavityHelper / ChestCavityData

ZenClass: `mods.chestcavity.ChestCavityHelper`

| API | 返回 | 说明 |
|---|---|---|
| `ChestCavityHelper.get(entity)` | `ChestCavityData` | 获取实体胸腔，无法获取时返回 null |

ZenClass: `mods.chestcavity.ChestCavityData`

### Getter

| Getter | 类型 | 说明 |
|---|---|---|
| `isOpened` | bool | 是否已开胸 |
| `slotCount` | int | 当前 layout 槽位数 |

### 查询

| API | 返回 | 说明 |
|---|---|---|
| `cc.getOrganScore(scoreId)` | float | 查询 score |
| `cc.getOrganScores()` | float[string] | 查询全部非零 score |
| `cc.hasScore(scoreId)` | bool | 是否有该 score |
| `cc.hasOrgan(item)` | bool | 是否含指定器官 |
| `cc.getOrganCount(item)` | int | 指定器官总数量 |
| `cc.getOrganSlots(item)` | int[] | 指定器官所在槽位 |
| `cc.getOrganSlotsByScore(scoreId)` | int[] | 带指定 score 的器官所在槽位 |
| `cc.getOccupiedSlots()` | int[] | 非空槽位 |
| `cc.getOrgan(slot)` | IItemStack | 获取槽位物品 |

### 修改

| API | 说明 |
|---|---|
| `cc.setOrgan(slot, stack)` | 设置槽位物品，内部通过 mutation 提交 |
| `cc.recalculateScores()` | 强制重建 runtime 并同步 |
| `cc.openChestCavity()` | 强制打开胸腔 |

已移除的旧入口：

- `setOrganScore`
- `addOrganScore`
- 任何直接覆盖 runtime score Map 的 API

```zenscript
import mods.chestcavity.ChestCavityHelper;

events.onPlayerTick(function(event as crafttweaker.event.PlayerTickEvent) {
    if (event.player.world.remote || event.phase != "END") {
        return;
    }

    var cc = ChestCavityHelper.get(event.player);
    if (isNull(cc)) {
        return;
    }

    var health = cc.getOrganScore("health");
    var occupied = cc.getOccupiedSlots();

    if (cc.isOpened && cc.slotCount > 0) {
        cc.setOrgan(0, <minecraft:apple>);
        cc.recalculateScores();
    }
});
```

## 9. Events

CrT events 是 `crafttweaker.events.IEventManager` 的扩展方法。

### onAbilityActivated

```zenscript
events.onAbilityActivated(function(event as mods.chestcavity.event.AbilityActivatedEvent) {
    if (event.abilityId == "fire_blast" && event.score < 2.0) {
        event.cancel();
    }
});
```

| Getter | 类型 | 说明 |
|---|---|---|
| `entity` | IEntityLivingBase | 触发实体 |
| `player` | IPlayer | 玩家，非玩家时可能为 null |
| `world` | IWorld | 世界 |
| `abilityId` | string | ability/score ID |
| `score` | float | 当前 score 值 |
| `isServer` | bool | 是否服务端 |
| `canceled` | bool | 是否取消 |

| 方法 | 说明 |
|---|---|
| `cancel()` | 取消本次能力激活 |

### onOrganEquipped

```zenscript
events.onOrganEquipped(function(event as mods.chestcavity.event.OrganEquippedEvent) {
    print("Equipped " ~ event.organId ~ " at slot " ~ event.slot);
});
```

### onOrganUnequipped

```zenscript
events.onOrganUnequipped(function(event as mods.chestcavity.event.OrganUnequippedEvent) {
    print("Unequipped " ~ event.organId ~ " from slot " ~ event.slot);
});
```

Organ change event getter：

| Getter | 类型 | 说明 |
|---|---|---|
| `entity` | IEntityLivingBase | 拥有胸腔的实体 |
| `player` | IPlayer | 如果实体是玩家则返回玩家，否则可能为 null |
| `world` | IWorld | 世界 |
| `slot` | int | 槽位 |
| `organ` | IItemStack | 器官物品 |
| `organId` | string | 器官物品 ID |
| `isPseudoOrgan` | bool | 是否伪器官 |
| `isServer` | bool | 是否服务端 |

## 10. 完整示例

```zenscript
import mods.chestcavity.OrganData;
import mods.chestcavity.ChestLayout;
import mods.chestcavity.ChestCavityType;
import mods.chestcavity.EntityAssignment;
import mods.chestcavity.ScoreManager;

ScoreManager.addScore("arcane_core", "Arcane Core");

OrganData.register(<minecraft:ender_eye>, {
    "arcane_core": 1.0,
    "health": 2.0
} as float[string]);

ChestLayout.registerGrid(
    "chestcavity:arcane_layout",
    18, 9,
    176, 132,
    8, 6,
    8, 18,
    18, 18,
    "KEEP_BY_INDEX"
);

ChestLayout.setSlotRule(
    "chestcavity:arcane_layout",
    4,
    false,
    [] as string[],
    ["arcane_core"] as string[],
    1,
    1
);

ChestCavityType.register("arcane_construct");
ChestCavityType.setLayout("arcane_construct", "chestcavity:arcane_layout");
ChestCavityType.addBaseScore("arcane_construct", "health", 10.0);
ChestCavityType.setSlot("arcane_construct", 4, <minecraft:ender_eye>);

EntityAssignment.register("minecraft:evocation_illager", "arcane_construct");
```
