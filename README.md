# 🍎 Diet - AppleSeed Edition

**Neoforge 1.21.1** | **Data-Driven** | **Fully Configurable** | **Modpack-Friendly**

---

### Eat balanced, stay healthy 🥗 | 膳食均衡，健康生活

Bring depth to Minecraft's food system! Get bonuses for eating varied diets, and negative effects from nutritional imbalance.

为Minecraft的饮食系统引入深度机制！通过多样化饮食获得奖励，饮食失衡时获得负面效果。

---

**Inspired by TheIllusiveC4's project Diet - https://github.com/illusivesoulworks/diet**

**灵感来自 TheIllusiveC4 的项目 Diet - https://github.com/illusivesoulworks/diet**

---

## 📖 About | 关于

**Diet - AppleSeed Edition** is the spiritual successor of the famous **Diet** mod, completely rewritten from the ground up for Neoforge 1.21.1.
**Diet - AppleSeed Edition** 是著名膳食均衡模组 **Diet** 的精神续作，为 Neoforge 1.21.1 完全重写。

This mod introduces a complete nutrition system that encourages players to eat diverse foods instead of relying on golden carrots forever!
本模组引入了一套完整的饮食营养系统，鼓励玩家多样化饮食，而不是只吃金胡萝卜！

> 💡 **Features: | 特性：**
> - ✅ 100% Data-driven, zero hardcoding | 100% 数据驱动，零硬编码
> - ✅ Powerful automatic nutrition calculation engine | 强大的自动营养计算引擎
> - ✅ Developer tools built for modpack creators | 整合包开发者专属调试工具
> - ✅ Complete configuration system with per-group overrides | 完整的配置系统，支持每组分段覆盖
> - ✅ Supports all modded foods | 支持所有 Mod 食物
> - ✅ Multi-language support for command output | 指令输出支持多语言

---

## 🎯 Five Food Groups | 五大营养组

| Icon | Group | Debuff (0-25%) | Advanced (61-70%) | Max (71-80%) | Peak (81-100%) |
|:---:|:---:|:---:|:---:|:---:|:---:|
| 🌾 | **Grains** | Slowness I | Max Health +4 | Max Health +6<br>Attack Damage +1 | Max Health +6<br>Attack Damage +1<br>Regeneration I |
| 🌾 | **谷物** | 缓慢 I | 生命上限 +4 | 生命上限 +6<br>攻击伤害 +1 | 生命上限 +6<br>攻击伤害 +1<br>生命恢复 I |
| 🥬 | **Vegetables** | Nausea I | Max Health +2<br>Armor Toughness +3 | Max Health +2<br>Armor Toughness +4 | Max Health +2<br>Armor Toughness +4<br>Haste I |
| 🥬 | **蔬菜** | 反胃 I | 生命上限 +2<br>护甲韧性 +3 | 生命上限 +2<br>护甲韧性 +4 | 生命上限 +2<br>护甲韧性 +4<br>急迫 I |
| � | **Protein** | Weakness I | Max Health +2<br>Armor +1 | Max Health +4<br>Armor +2 | Max Health +6<br>Armor +4<br>Resistance I |
| 🥩 | **蛋白质** | 虚弱 I | 生命上限 +2<br>护甲值 +1 | 生命上限 +4<br>护甲值 +2 | 生命上限 +6<br>护甲值 +4<br>抗性提升 I |
| 🍎 | **Fruits** | Mining Fatigue I | Max Health +2<br>Attack Speed +0.05 | Max Health +4<br>Attack Speed +0.1 | Max Health +6<br>Attack Speed +0.2 |
| 🍎 | **水果** | 挖掘疲劳 I | 生命上限 +2<br>攻击速度 +0.05 | 生命上限 +4<br>攻击速度 +0.1 | 生命上限 +6<br>攻击速度 +0.2 |
| � | **Sugars** | None | Speed II | Speed II | Speed II<br>Hunger V |
| 🍬 | **糖类** | 无 | 速度 II | 速度 II | 速度 II<br>饥饿 V |

> 💡 **26% - 60%: No effects for all groups | 26% - 60%：所有营养组均无效果**
> 
> 💡 **Sugars Special: No effect 0-50%, speed bonus starts at 51% | 糖类特殊：0-50% 均无效果，51% 开始提供速度加成**

---

## 🎮 In-Game Features | 游戏内功能

### 📱 User Interface | 用户界面

Press `E` to open inventory → Click the **Diet button** next to the Recipe Book.
按 `E` 打开背包 → 点击配方书右侧的 **「膳食均衡」按钮**。

```
┌─────────────────────────────────────────────┐
│                  Diet / 膳食均衡             │
├─────────────────────────────────────────────┤
│  🌾 Grains   [████████░░░░]  80%            │
│  🥬 Veggies  [███████░░░░░]  70%            │
│  🥩 Protein  [████████████] 100%            │
│  🍎 Fruits   [████░░░░░░░░]  40%            │
│  🍬 Sugars   [█████████░░░]  90%            │
├─────────────────────────────────────────────┤
│  ✨ Active Effects:                         │
│     Resistance I                            │
│     +6.0 Max Health / 最大生命值             │
│     +4.0 Armor / 护甲值                      │
└─────────────────────────────────────────────┘
```

### 💬 Item Tooltips | 物品提示

**All edible items** show nutrition values. | **所有可食用物品**都会显示营养值。

```
Cake / 蛋糕
──────
Nutrition: / 营养值：
  Grains: +3.2% / 谷物: +3.2%
  Sugars: +15.8% / 糖类: +15.8%
  Protein: +1.5% / 蛋白质: +1.5%
```

> 💡 Only nutrients with value > 0 are shown. | 仅显示值大于 0 的营养素。

---

## ⚙️ Nutrition Mechanics | 营养机制

### 📉 Nutrition Decay | 营养衰减

Your nutrition decays based on your actions. | 营养值会根据玩家行为而衰减。

| Event / 事件 | Decay Amount / 衰减量 |
|:---|:---:|
| Per hunger point lost / 每失去1点饱食度 | 0.5% |
| Per damage instance taken / 每受到1次伤害 | 0.1% |

> 💡 Grains, Fruits, Vegetables, Proteins, and Sugars can be configured to ignore decay from attacks or hunger (see Group Configuration). | 谷物、水果、蔬菜、蛋白质和糖类可以配置为忽略攻击或饥饿导致的衰减（见营养组配置）。

### 💀 Death Mechanics | 死亡机制

Controlled by gamerule **`keepNutritions`**. | 由游戏规则 **`keepNutritions`** 控制。

- **`false` (Default)**: Reset to default values on respawn. | **`false` (默认)**: 重生时重置为默认值。
- **`true`**: Keep nutrition values on death. | **`true`**: 死亡后保留营养值。

```
/gamerule keepNutritions true
```

---

## 🚀 Smart Auto-Calculation Engine | 智能自动计算引擎

### 🧠 Recipe-Driven Nutrition | 配方驱动的营养计算

This is the mod's most powerful feature! **No manual data files required!**
这是模组最强大的功能！**无需手动编写数据文件！**

On world load, the mod will: | 世界加载时，模组会：
1. 🔍 Scan all registered recipes | 扫描所有已注册的配方
2. 🍳 Identify all items with FoodProperties | 识别所有带有 FoodProperties 的物品
3. 🔗 Recursively trace each food's ingredients | 递归追溯每个食物的原料
4. 🧮 Automatically calculate nutritional composition | 自动计算营养成分

### 📊 Calculation Example | 计算示例

Take Cake for example. | 以蛋糕为例。

| Ingredient / 原料 | Count / 数量 | Nutrition Contribution / 营养贡献 |
|:---|:---:|:---|
| Milk Bucket / 牛奶桶 | 3 | Protein / 蛋白质 |
| Sugar / 糖 | 2 | Sugars / 糖类 |
| Egg / 鸡蛋 | 1 | Protein / 蛋白质 |
| Wheat / 小麦 | 3 | Grains / 谷物 |

```
Cake Nutrition = 3×Milk + 2×Sugar + Egg + 3×Wheat
蛋糕营养值 = 3×牛奶 + 2×糖 + 鸡蛋 + 3×小麦
──────────────────────────────────────────────────
Result: Grains +3.2% / Sugars +15.8% / Protein +1.5%
结果: 谷物 +3.2% / 糖类 +15.8% / 蛋白质 +1.5%
```

### ✅ Supported Recipe Types | 支持的配方类型

- ✅ Crafting (Shaped / Shapeless) | 合成配方（有序/无序）
- ✅ Furnace / Smoker / Blast Furnace | 熔炉/烟熏炉/高炉
- ✅ Campfire Cooking | 营火烹饪
- ✅ Stonecutting | 切石机
- ✅ Smithing | 锻造

---

## 🔧 Commands | 指令

| Command / 指令 | Permission / 权限 | Description / 描述 |
|:---|:---:|:---|
| `/diet nutritions query <player>` | 2 | View a player's nutrition values / 查看玩家的营养值 |
| `/diet nutritions set <player> <id> <value>` | 2 | Set a specific nutrition value (0.0-1.0) / 设置特定营养值 (0.0-1.0) |
| `/diet nutritions add <player> <id> <amount>` | 2 | Add to a nutrition value (capped at 1.0) / 增加营养值（上限 1.0） |
| `/diet nutritions remove <player> <id> <amount>` | 2 | Remove from a nutrition value (capped at 0.0) / 减少营养值（下限 0.0） |
| `/diet config set ignoreHunger <bool>` | 2 | Toggle eating when full / 切换饱食度满时是否能进食 |
| `/diet cache clear` | 4 | Delete auto-generated nutrition files / 删除自动生成的营养文件 |
| `/diet cache regenerate` | 4 | Regenerate all auto-calculated nutrition data / 重新生成所有自动计算的营养数据 |
| `/diet cache reload` | 2 | Reload nutrition and group data / 重新加载营养和营养素数据 |

> 💡 All nutrition ID arguments support Tab completion. | 所有营养 ID 参数都支持 Tab 补全。

---

## 📁 Data Pack Configuration | 数据包配置

### 📂 Directory Structure | 目录结构

```
data/
└── appleseed/
    └── diet/
        ├── groups/        # 营养素组定义 (required / 必需)
        │   ├── grains.json
        │   ├── fruits.json
        │   ├── vegetables.json
        │   ├── proteins.json
        │   └── sugars.json
        └── foods/         # 食物营养值定义 (optional, auto-generated / 可选，自动生成)
            ├── apple.json
            └── bread.json
```

### 📋 Group Configuration Example | 营养组配置示例

**`data/appleseed/diet/groups/proteins.json`**

```json
{
  "icon": "minecraft:cooked_beef",
  "color": "#a35f39",
  "order": 3,
  "default_value": 0.5,
  "gain_multiplier": 1.0,
  "decay_multiplier": 1.0,
  "beneficial": true,
  "is_negative": false,
  "ignore_attack": false,
  "ignore_hunger": false,
  "translation_key": "diet.group.proteins",
  "effects": [
    "0-25:effect(minecraft:weakness,0)",
    "61-70:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.armor,1.0)",
    "71-80:attribute(minecraft:generic.max_health,4.0),attribute(minecraft:generic.armor,2.0)",
    "81-100:attribute(minecraft:generic.max_health,6.0),attribute(minecraft:generic.armor,4.0),effect(minecraft:resistance,0)"
  ]
}
```

#### Group Fields Reference | 营养组字段参考

| Field / 字段 | Type / 类型 | Default / 默认值 | Description / 描述 |
|:---|:---|:---|:---|
| `icon` | String | - | Item ID for the group icon / 组图标的物品ID |
| `color` | String | `#FFFFFF` | Hex color code for UI display / UI显示的十六进制颜色代码 |
| `order` | Integer | 0 | Display order in UI / UI中的显示顺序 |
| `default_value` | Float | 0.5 | Initial value when player joins / 玩家加入时的初始值 |
| `gain_multiplier` | Double | 1.0 | Multiplier for nutrition gain from eating / 进食获得营养的倍率 |
| `decay_multiplier` | Double | 1.0 | Multiplier for nutrition decay / 营养衰减的倍率 |
| `beneficial` | Boolean | true | Whether positive effects are applied / 是否应用正面效果 |
| `is_negative` | Boolean | false | If true, excluded from recipe-based calculation / 若为true，从配方计算中排除 |
| `ignore_attack` | Boolean | false | If true, no decay when taking damage / 若为true，受伤时不衰减 |
| `ignore_hunger` | Boolean | false | If true, no decay when hunger decreases / 若为true，饱食度降低时不衰减 |
| `translation_key` | String | - | Localization key for display name / 显示名称的本地化键 |
| `effects` | Array | [] | Effect definitions for nutrition ranges / 营养范围的效果定义 |

### 🍎 Food Configuration Example | 食物配置示例

**`data/appleseed/diet/foods/apple.json`**

```json
{
  "source_item": "minecraft:apple",
  "nutritions": {
    "fruits": 0.02
  }
}
```

### ✏️ Effect Syntax | 效果语法

Each range definition supports any number of stacked effects. | 每个范围定义支持任意数量的叠加效果。

| Syntax / 语法 | Example / 示例 |
|:---|:---|
| Status Effect / 状态效果 | `effect(minecraft:regeneration,0)` |
| Attribute Modifier / 属性修改 | `attribute(minecraft:generic.max_health,2.0)` |
| Multiple Effects / 多个效果 | `effect(...),attribute(...)` |

---

## ⚙️ Configuration File | 配置文件

Config location: `config/appleseed-common.toml` | 配置位置：`config/appleseed-common.toml`

```toml
[General_Settings]
    # Whether food counts as eaten even when full / 饱食度满时是否仍能进食获得营养
    ignore_hunger = false

[Initial_Values_Settings]
    grains_initial = 0.5
    fruits_initial = 0.5
    vegetables_initial = 0.5
    proteins_initial = 0.5
    sugars_initial = 0.5

[Nutritions_Settings]
    grains_ranges = [
        "0-25:effect(minecraft:slowness,0)",
        "61-70:attribute(minecraft:generic.max_health,4.0)",
        "71-80:attribute(minecraft:generic.max_health,6.0),attribute(minecraft:generic.attack_damage,1.0)",
        "81-100:attribute(minecraft:generic.max_health,6.0),attribute(minecraft:generic.attack_damage,1.0),effect(minecraft:regeneration,0)"
    ]
    fruits_ranges = [
        "0-25:effect(minecraft:mining_fatigue,0)",
        "61-70:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.attack_speed,0.05)",
        "71-80:attribute(minecraft:generic.max_health,4.0),attribute(minecraft:generic.attack_speed,0.1)",
        "81-100:attribute(minecraft:generic.max_health,6.0),attribute(minecraft:generic.attack_speed,0.2)"
    ]
    vegetables_ranges = [
        "0-25:effect(minecraft:nausea,0)",
        "61-70:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.armor_toughness,3.0)",
        "71-80:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.armor_toughness,4.0)",
        "81-100:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.armor_toughness,4.0),effect(minecraft:haste,0)"
    ]
    proteins_ranges = [
        "0-25:effect(minecraft:weakness,0)",
        "61-70:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.armor,1.0)",
        "71-80:attribute(minecraft:generic.max_health,4.0),attribute(minecraft:generic.armor,2.0)",
        "81-100:attribute(minecraft:generic.max_health,6.0),attribute(minecraft:generic.armor,4.0),effect(minecraft:resistance,0)"
    ]
    sugars_ranges = [
        "51-70:effect(minecraft:speed,1)",
        "71-100:effect(minecraft:speed,1),effect(minecraft:hunger,4)"
    ]

[Group_Overrides]
    # Override is_negative, ignore_attack, ignore_hunger for preset groups
    # 覆盖预设营养组的 is_negative, ignore_attack, ignore_hunger
    grains_is_negative = false
    grains_ignore_attack = false
    grains_ignore_hunger = false
    
    fruits_is_negative = false
    fruits_ignore_attack = false
    fruits_ignore_hunger = false
    
    vegetables_is_negative = false
    vegetables_ignore_attack = false
    vegetables_ignore_hunger = false
    
    proteins_is_negative = false
    proteins_ignore_attack = false
    proteins_ignore_hunger = false
    
    sugars_is_negative = false
    sugars_ignore_attack = false
    sugars_ignore_hunger = false
```

### ⚠️ Important Notes | 重要说明

- **`Group_Overrides` section** allows overriding `is_negative`, `ignore_attack`, and `ignore_hunger` for the 5 preset groups without modifying data packs.
- **`Group_Overrides` 节** 允许在不修改数据包的情况下覆盖5种预设营养组的 `is_negative`、`ignore_attack` 和 `ignore_hunger`。

- The `ignore_hunger` in `Group_Overrides` controls whether a nutrient decays when hunger decreases, which is **different** from `General_Settings.ignore_hunger` (which controls whether eating while full provides nutrition).
- `Group_Overrides` 中的 `ignore_hunger` 控制营养素在饱食度降低时是否衰减，这与 `General_Settings.ignore_hunger`（控制饱食度满时是否能获得营养）**是不同的设置**。

---

## 🔧 Modpack Developer Tools | 整合包开发者工具

### 🔄 /reload Live Debugging | /reload 实时调试

Built exclusively for modpack developers! | 专为整合包开发者设计！

```
1. Modify recipes (e.g., KubeJS)
   修改配方（如使用 KubeJS）
2. Type /reload in-game
   在游戏中输入 /reload
3. ⏳ Live progress updates (every 2 sec):
   ⏳ 实时进度更新（每2秒）：
   §e[AppleSeed]§r Calculating nutrition: 27/46 (32 succeeded)
   §e[AppleSeed]§r 正在计算营养值: 27/46 (32 成功)
4. ✅ Complete:
   ✅ 完成:
   §a[AppleSeed]§r Nutrition calculation complete!
   §a[AppleSeed]§r 营养值计算完成！
5. ✅ All data auto-reloaded, no restart needed!
   ✅ 所有数据自动重新加载，无需重启！
```

### 🎚️ Four-Tier Data Priority | 四层数据优先级

```
Priority (Highest → Lowest):
优先级 (最高 → 最低):

    🥇 World Datapacks (world/datapacks)
    🥇 世界数据包 (world/datapacks)
        ↓
    🥈 Other Mods' Built-in Data
    🥈 其他模组的内置数据
        ↓
    🥉 This Mod's Built-in Data
    🥉 本模组的内置数据
        ↓
    🏅 Config Auto-Generated Content
    🏅 配置文件自动生成内容
```

> 💡 This means: You can override auto-calculated values in datapacks - extremely useful for balance tweaks!
> 💡 这意味着：您可以在数据包中覆盖自动计算的值——非常适合平衡性调整！

### 📁 Data Locations | 数据位置

| Tier / 层级 | Path / 路径 |
|:---|:---|
| Auto-Generated / 自动生成 | `config/apple_seed_foods/*.json` |
| Mod Built-in / 模组内置 | `data/appleseed/diet/foods/*.json` |
| Datapack / 数据包 | `(datapack)/data/appleseed/diet/foods/*.json` |

---

## 🤝 Compatibility | 兼容性

### ✅ Auto-compatible Mods | 自动兼容模组

**All modded foods are automatically supported!** | **所有添加食物的模组都自动支持！**

- ✅ **Farmer's Delight** - All foods / 所有食物
- ✅ **Create** - All processed foods / 所有加工食物
- ✅ **Pam's HarvestCraft** - All crops / 所有作物
- ✅ **More Foods** mods / 更多食物类模组
- ✅ **ANY** mod that adds food with FoodProperties / 任何添加带 FoodProperties 食物的模组

### 🔄 Special Compatibility | 特殊兼容

#### SomeAssemblyRequired Sandwich Support | SomeAssemblyRequired 三明治支持

The mod dynamically calculates nutrition for `someassemblyrequired:sandwich` items based on their NBT contents (the ingredients inside the sandwich). No configuration required!
模组会根据 `someassemblyrequired:sandwich` 物品的 NBT 内容（三明治内的食材）动态计算营养值。无需任何配置！

> 💡 This is a soft dependency - no code is loaded if SomeAssemblyRequired is not present.
> 💡 这是软依赖——如果未安装 SomeAssemblyRequired，则不会加载相关代码。

---

## 📝 Credits & License | 致谢与许可证

### 💝 Acknowledgments | 致谢

Core design inspired by: | 核心设计灵感来自：
- **TheIllusiveC4** - Original **Diet** mod / 原版 **Diet** 模组

### 📜 License | 许可证

This project is licensed under **LGPLv3**. | 本项目采用 **LGPLv3** 许可证。
