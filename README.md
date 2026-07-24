# 🍎 Diet — AppleSeed Edition

**Neoforge 1.21.1**  |  **Data-Driven**  |  **Fully Configurable**  |  **Modpack-Friendly**

---

### Eat balanced, stay healthy 🥗  |  膳食均衡，健康生活

Bring depth to Minecraft's food system! Get bonuses for eating varied diets, and negative effects from nutritional imbalance.

为 Minecraft 的饮食系统引入深度机制！通过多样化饮食获得奖励，饮食失衡时获得负面效果。

---

**Inspired by TheIllusiveC4's project Diet — https://github.com/illusivesoulworks/diet**

**灵感来自 TheIllusiveC4 的项目 Diet — https://github.com/illusivesoulworks/diet**

---

## 📖 About  |  关于

**Diet — AppleSeed Edition** is the spiritual successor of the famous **Diet** mod, completely rewritten from the ground up for Neoforge 1.21.1.

**Diet — AppleSeed Edition** 是著名膳食均衡模组 **Diet** 的精神续作，为 Neoforge 1.21.1 完全重写。

This mod introduces a complete nutrition system that encourages players to eat diverse foods instead of relying on golden carrots forever!

本模组引入了一套完整的饮食营养系统，鼓励玩家多样化饮食，而不是只吃金胡萝卜！

> 💡 **Features  |  特性：**
> - ✅ 100% Data-driven, zero hardcoding  |  100% 数据驱动，零硬编码
> - ✅ Powerful automatic nutrition calculation engine powered by CloudWorks API  |  基于 CloudWorks API 的强大自动营养计算引擎
> - ✅ Complete configuration system with per-group overrides  |  完整的配置系统，支持每组分段覆盖
> - ✅ Smart ingredient tracing via Minecraft recipes  |  通过 Minecraft 配方智能追溯原料
> - ✅ Supports all modded foods  |  支持所有 Mod 食物
> - ✅ Multi-language support  |  多语言支持
> - ✅ Soft dependency — works without FTB Library / SomeAssemblyRequired  |  软依赖 — 不安装 FTB Library / SomeAssemblyRequired 也能正常运行
> - ✅ Block food support (e.g., Cake) with per-bite nutrition  |  方块食物支持（如蛋糕），按口计算营养值
> - ✅ Simulated recipes for custom nutrition calculations  |  模拟配方系统，支持自定义营养计算
> - ✅ Configurable recipe recursion depth for non-food ingredients  |  可配置的非食物原料配方递归深度
> - ✅ Fluid nutrition support (per mB, not persisted)  |  流体营养支持（每 mB 计算，不持久化）
> - ✅ Tag-based food assignment with priority system  |  基于标签的食物分配及优先级系统
> - ✅ Three decay channels: hit, hunger, saturation  |  三种衰减渠道：受击、饥饿、饱和度

---

## 🎯 Five Food Groups  |  五大营养组

| Icon | Group | Debuff (0-25%) | Advanced (61-70%) | Max (71-80%) | Peak (81-100%) |
|:---:|:---:|:---:|:---:|:---:|:---:|
| 🌾 | **Grains** | Slowness I | Max Health +4 | Max Health +6<br>Regeneration I | Max Health +6<br>Attack Damage +1<br>Regeneration I |
| 🌾 | **谷物** | 缓慢 I | 生命上限 +4 | 生命上限 +6<br>生命恢复 I | 生命上限 +6<br>攻击伤害 +1<br>生命恢复 I |
| 🥬 | **Vegetables** | Nausea I | Max Health +2<br>Armor Toughness +3 | Max Health +2<br>Armor Toughness +4 | Max Health +2<br>Armor Toughness +4<br>Haste I |
| 🥬 | **蔬菜** | 反胃 I | 生命上限 +2<br>护甲韧性 +3 | 生命上限 +2<br>护甲韧性 +4 | 生命上限 +2<br>护甲韧性 +4<br>急迫 I |
| 🥩 | **Protein** | Weakness I | Max Health +2<br>Armor +1 | Max Health +4<br>Armor +2 | Max Health +6<br>Armor +4<br>Resistance I |
| 🥩 | **蛋白质** | 虚弱 I | 生命上限 +2<br>护甲值 +1 | 生命上限 +4<br>护甲值 +2 | 生命上限 +6<br>护甲值 +4<br>抗性提升 I |
| 🍎 | **Fruits** | Mining Fatigue I | Max Health +2<br>Attack Speed +0.05 | Max Health +4<br>Attack Speed +0.1 | Max Health +6<br>Attack Speed +0.2 |
| 🍎 | **水果** | 挖掘疲劳 I | 生命上限 +2<br>攻击速度 +0.05 | 生命上限 +4<br>攻击速度 +0.1 | 生命上限 +6<br>攻击速度 +0.2 |
| 🍬 | **Sugars** | None | Speed II | Speed II | Speed II<br>Hunger V |
| 🍬 | **糖类** | 无 | Speed II | Speed II | Speed II<br>饥饿 V |

> 💡 **26% – 60%: No effects for all groups.  |  26% – 60%：所有营养组均无效果。**
>
> 💡 **Sugars special: No effect at 0–50%, speed bonus starts at 51%.  |  糖类特殊：0–50% 无效果，51% 起提供速度加成。**

---

## 🎮 In-Game Features  |  游戏内功能

### 📱 Diet Balance Screen  |  膳食均衡界面

Open the diet screen via:

- **Inventory Button**: Press `E` → click the **Diet** button next to the Recipe Book (default mode, 20×18 px)
- **FTB Library Sidebar**: Button appears in the FTB Library sidebar (ftb_compact mode, requires FTB Library)
- **Key Binding**: Bind a key to "Open Diet GUI" in Controls → AppleSeed category
- **Command**: `/diet screen` (no permission required)

打开膳食均衡界面：

- **物品栏按钮**：按 `E` → 点击配方书旁的「膳食均衡」按钮（默认模式，尺寸 20×18 像素）
- **FTB Library 侧边栏**：按钮出现在 FTB Library 侧边栏中（ftb_compact 模式，需安装 FTB Library）
- **按键绑定**：在 控制 → AppleSeed 分类中为「Open Diet GUI」绑定按键
- **指令**：`/diet screen`（无需权限）

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

The screen supports **pagination** when there are more than 5 nutrition groups.

营养组超过 5 个时，界面支持**分页**浏览。

### 🔘 Entrance Visibility Modes  |  入口可见性模式

Controlled via config `entrance_visibility` or `/diet config set entranceVisibility <mode>`:

| Mode | Behavior |
|:---|:---|
| `default` | Shows a Diet button in the inventory screen, next to the Recipe Book |
| `ftb_compact` | Uses FTB Library's sidebar button (requires FTB Library installed). Falls back to `default` if FTB Library is absent |
| `invisible` | Hides all entrance buttons (still accessible via key bind or `/diet screen` command) |

通过配置 `entrance_visibility` 或 `/diet config set entranceVisibility <mode>` 控制：

| 模式 | 行为 |
|:---|:---|
| `default` | 在物品栏配方书旁显示「膳食均衡」按钮 |
| `ftb_compact` | 使用 FTB Library 侧边栏按钮（需安装 FTB Library）。未安装时自动回退为 `default` |
| `invisible` | 隐藏所有入口按钮（仍可通过按键绑定或 `/diet screen` 指令打开） |

> 💡 Switching to/from `ftb_compact` mode automatically triggers `/reload` to refresh FTB Library's sidebar.
> 💡 切换至/出 `ftb_compact` 模式时自动执行 `/reload` 以刷新 FTB Library 侧边栏。

### 💬 Item Tooltips  |  物品提示

**All edible items** show nutrition values when hovered.  |  **所有可食用物品**在鼠标悬停时显示营养值。

```
Cake / 蛋糕
──────
Nutrition: / 营养值：
  Grains: +3.2% / 谷物: +3.2%
  Sugars: +15.8% / 糖类: +15.8%
  Protein: +1.5% / 蛋白质: +1.5%
```

> 💡 Only nutrients with value > 0 are shown.  |  仅显示值大于 0 的营养素。
> 💡 SomeAssemblyRequired sandwiches are fully supported.  |  完整支持 SomeAssemblyRequired 的三明治。

### 🧱 Block Foods  |  方块食物

**Block foods** like Cake are fully supported. Nutrition is calculated per bite.  |  **方块食物**（如蛋糕）已完全支持，营养值按口计算。

```json
// diet/blocks/cake.json
{
    "source_block": "minecraft:cake",
    "bites": 7,
    "nutritions": {
        "grains": 0.03,
        "sugars": 0.15,
        "proteins": 0.02
    }
}
```

Each bite of cake adds 1/7th of the total nutrition values.  |  每口蛋糕增加总营养值的 1/7。

---

## ⚙️ Nutrition Mechanics  |  营养机制

### 📉 Nutrition Decay  |  营养衰减

Your nutrition decays based on your actions, controlled by gamerules:  |  营养值会根据玩家行为衰减，由游戏规则控制：

| Event / 事件 | Decay Amount / 衰减量 | Gamerule / 游戏规则 | Default / 默认值 |
|:---|:---:|:---|:---:|
| Per hunger point lost / 每失去 1 点饱食度 | multiplier × decay_multiplier | `nutritionDecayByHungerMultiplier` | 0.005 |
| Per damage instance taken / 每受到 1 次伤害 | multiplier × decay_multiplier | `nutritionDecayByHitMultiplier` | 0.001 |
| Per saturation point lost / 每失去 1 点饱和度 | multiplier × decay_multiplier | `nutritionDecayBySaturationMultiplier` | 0.0 |

All decay multipliers are part of the **PLAYER** game rule category.  |  所有衰减系数均属于 **PLAYER（玩家）** 分类的游戏规则。

```
/gamerule nutritionDecayByHungerMultiplier 0.01
/gamerule nutritionDecayByHitMultiplier 0.002
/gamerule nutritionDecayBySaturationMultiplier 0.001
```

### 🛡️ Decay Exemptions  |  衰减豁免

Each group can be configured to ignore decay from specific sources:

每个营养组可配置忽略特定来源的衰减：

| Config Field | Effect |
|:---|:---|
| `ignore_hunger` | Nutrition does NOT decay when hunger decreases |
| `ignore_attack` | Nutrition does NOT decay when taking damage |
| `ignore_saturation` | Nutrition does NOT decay when saturation decreases |

| 配置字段 | 效果 |
|:---|:---|
| `ignore_hunger` | 饱食度降低时该营养素不减少 |
| `ignore_attack` | 受到攻击时该营养素不减少 |
| `ignore_saturation` | 饱和度降低时该营养素不减少 |

These can be set per-group in `appleseed-common.toml` under `[Group_Overrides]`, or directly in the diet group JSON data files.

可在 `appleseed-common.toml` 的 `[Group_Overrides]` 中按组设置，或直接在营养组 JSON 数据文件中定义。

### 📈 Nutrition Gain  |  营养获取

When you eat a food item, nutrition values are added based on the food's nutritional composition, multiplied by the group's `gain_multiplier`.

食用食物时，根据食物的营养成分增加营养值，乘以该组的 `gain_multiplier`。

### 💀 Death Mechanics  |  死亡机制

Controlled by gamerule **`keepNutritions`**:  |  由游戏规则 **`keepNutritions`** 控制：

- **`false` (Default)**: Reset to initial values on respawn.  |  **`false`（默认）**：重生时重置为初始值。
- **`true`**: Keep nutrition values on death.  |  **`true`**：死亡后保留营养值。

```
/gamerule keepNutritions true
```

---

## 🚀 Smart Auto-Calculation Engine  |  智能自动计算引擎

### 🧠 CloudWorks-Powered Recipe Parsing  |  基于 CloudWorks 的配方解析

This is the mod's most powerful feature! **No manual data files required!**

这是模组最强大的功能！**无需手动编写数据文件！**

Powered by **CloudWorks API's RecipeParser** module, the mod uses async recipe queries to parse recipes and calculate nutrition values.

基于 **CloudWorks API 的 RecipeParser** 模块，模组使用异步配方查询来解析配方并计算营养值。

On world load, the mod will:  |  世界加载时，模组会：
1. 🔍 Submit async recipe queries for all items and fluids via CloudWorks RecipeParser  |  通过 CloudWorks RecipeParser 为所有物品和流体提交异步配方查询
2. 🍳 Identify all items with FoodProperties  |  识别所有带有 FoodProperties 的物品
3. 🔗 Recursively trace each food's ingredients (items and fluids)  |  递归追溯每个食物的原料（物品和流体）
4. 🧮 Automatically calculate nutritional composition  |  自动计算营养成分
5. 💾 Save results to `config/apple_seed_foods/` as JSON cache (items only)  |  将结果保存为 JSON 缓存至 `config/apple_seed_foods/`（仅物品）

### 🌊 Fluid Nutrition  |  流体营养

Fluids have nutrition values calculated **per mB** and are **not persisted** — they are recalculated on each server start / reload.

流体的营养值按**每 mB** 计算，且**不持久化保存**——每次服务器启动/重载时重新计算。

Fluid nutrition is derived from two sources in order:
1. From the corresponding bucket item: bucket nutrition ÷ 1000 (1 bucket = 1000 mB)
2. From recipes that produce the fluid: total input nutrition ÷ fluid output amount (mB)

流体营养从两个来源推导（按优先级）：
1. 从对应桶物品推导：桶物品营养 ÷ 1000（1 桶 = 1000 mB）
2. 从产出该流体的配方推导：总输入营养 ÷ 流体产出量（mB）

When fluids are used as ingredients in recipes, their nutrition contribution is calculated as:
`fluid nutrition per mB × fluid amount (mB)`

当流体作为配方原料时，其营养贡献计算公式为：
`流体每 mB 营养 × 流体用量（mB）`

### 📊 Calculation Example  |  计算示例

Take Cake for example:  |  以蛋糕为例：

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

### ✅ Supported Recipe Types  |  支持的配方类型

CloudWorks RecipeParser supports a wide range of recipe types natively, plus custom types via RPML templates.

CloudWorks RecipeParser 原生支持多种配方类型，并通过 RPML 模板支持自定义类型。

- ✅ Crafting (Shaped / Shapeless)  |  合成配方（有序 / 无序）
- ✅ Furnace / Smoker / Blast Furnace  |  熔炉 / 烟熏炉 / 高炉
- ✅ Campfire Cooking  |  营火烹饪
- ✅ Stonecutting  |  切石机
- ✅ Smithing  |  锻造
- ✅ Simulated recipes (`appleseed:simulate_recipe`)  |  模拟配方
- ✅ Fluid-containing recipes  |  含流体的配方

### 🔄 Recipe Cycle Handling  |  配方循环处理

The auto-calculator detects recipe cycles automatically:
- When a cycle is detected, it returns to the cycle start node
- If other recipes exist besides the cycle entry, it tries alternative recipes

自动计算器会自动检测配方循环：
- 检测到循环时，回到循环开始节点
- 如果有除循环入口以外的其它配方，则更换新的配方进行计算尝试

### 📁 Data Loading Priority  |  数据加载优先级

When multiple sources define nutrition for the same food, **priority-based overwriting** applies:

当多个来源为同一食物定义营养值时，采用**优先级覆盖机制**：

```
Highest Priority → Lowest Priority
最高优先级 → 最低优先级

1. Direct ID specification (source_item / source_block with enable_tag_search=false)
   直接指定 ID（enable_tag_search=false 的 source_item / source_block）

2. c: namespace tags (enable_tag_search=true, e.g., c:crops/wheat)
   c: 命名空间标签（enable_tag_search=true，如 c:crops/wheat）

3. Other namespace tags (enable_tag_search=true)
   其他命名空间标签（enable_tag_search=true）

Within the same priority: later-loaded files override earlier ones
同一优先级内：后加载的文件覆盖先加载的
```

File-level priority (highest → lowest):  |  文件级优先级（高 → 低）：

```
1. appleseed_data.json           ← Highest priority
2. Datapack diet/foods/*.json    ← Can override built-in
3. Other mods' diet/foods/*.json
4. Appleseed built-in foods
5. config/apple_seed_foods/*.json ← Auto-generated (lowest priority)
```

### 🧪 Simulated Recipes  |  模拟配方

Simulated recipes allow you to define custom nutrition calculations for items that don't have actual crafting recipes, or to override existing recipe-based calculations.

模拟配方允许你为没有实际合成配方的物品定义自定义营养计算，或覆盖现有的基于配方的计算。

Simulated recipes use the `appleseed:simulate_recipe` type and are processed by CloudWorks RecipeParser via an RPML template.

模拟配方使用 `appleseed:simulate_recipe` 类型，由 CloudWorks RecipeParser 通过 RPML 模板处理。

```json
// data/appleseed/recipes/simulate/custom_food.json
{
    "type": "appleseed:simulate_recipe",
    "inputs": [
        {
            "item": "minecraft:wheat",
            "count": 3
        },
        {
            "item": "minecraft:sugar",
            "count": 2
        }
    ],
    "outputs": [
        {
            "item": "minecraft:cake",
            "count": 1
        }
    ]
}
```

Simulated recipes support both items and fluids as inputs:

模拟配方支持物品和流体作为输入：

```json
{
    "type": "appleseed:simulate_recipe",
    "inputs": [
        {
            "fluid": "minecraft:water",
            "count": 1000
        },
        {
            "item": "minecraft:wheat",
            "count": 2
        }
    ],
    "outputs": [
        {
            "item": "minecraft:bread",
            "count": 1
        }
    ]
}
```

> 💡 Simulated recipes are processed alongside vanilla recipes during auto-calculation.  |  模拟配方在自动计算时与原版配方一起处理。

---

## 🔧 Commands  |  指令

### Nutrition Management  |  营养管理

| Command / 指令 | Permission | Description / 描述 |
|:---|:---:|:---|
| `/diet nutritions query <player>` | 2 | View a player's nutrition values / 查看玩家的营养值 |
| `/diet nutritions set <player> <id> <0.0–1.0>` | 2 | Set a specific nutrition value / 设置特定营养值 |
| `/diet nutritions add <player> <id> <0.0–1.0>` | 2 | Add to a nutrition value (capped at 1.0) / 增加营养值（上限 1.0） |
| `/diet nutritions remove <player> <id> <0.0–1.0>` | 2 | Remove from a nutrition value (capped at 0.0) / 减少营养值（下限 0.0） |

### Item Nutrition Management  |  物品营养管理

| Command / 指令 | Permission | Description / 描述 |
|:---|:---:|:---|
| `/diet item set <group> <value>` | 2 | Set nutrition for the held item / 设置手持物品的营养值 |
| `/diet item set <group> <value> <item>` | 2 | Set nutrition for a specific item / 设置特定物品的营养值 |

### Block Nutrition Management  |  方块营养管理

| Command / 指令 | Permission | Description / 描述 |
|:---|:---:|:---|
| `/diet block set <block> <group> <value>` | 2 | Set nutrition for a block food / 设置方块食物的营养值 |
| `/diet block set <block> <group> <value> <bites>` | 2 | Set nutrition with custom bite count / 设置方块食物的营养值和可食用次数 |

### Configuration  |  配置管理

| Command / 指令 | Permission | Description / 描述 |
|:---|:---:|:---|
| `/diet config set ignoreHunger <true\|false>` | 2 | Toggle whether eating when full counts nutrition / 切换饱食度满时是否计算营养 |
| `/diet config set entranceVisibility <mode>` | 2 | Set button visibility mode: `invisible` / `default` / `ftb_compact` / 设置按钮可见性模式 |
| `/diet config set craftChainSearchDepth <depth>` | 2 | Set recipe recursion depth for non-food ingredients / 设置非食物原料的配方递归搜索深度 |

### Cache Management  |  缓存管理

| Command / 指令 | Permission | Description / 描述 |
|:---|:---:|:---|
| `/diet cache clear` | 4 | Delete all auto-generated nutrition files / 删除所有自动生成的营养文件 |
| `/diet cache regenerate` | 4 | Clear cache + execute `/reload` to rebuild / 清空缓存 + 执行 `/reload` 重建 |
| `/diet cache reload` | 2 | Execute `/reload` to reload all nutrition data / 执行 `/reload` 重新加载全部营养数据 |

### Screen  |  界面

| Command / 指令 | Permission | Description / 描述 |
|:---|:---:|:---|
| `/diet screen` | None | Open the Diet Balance screen / 打开膳食均衡界面 |

> 💡 All nutrition ID arguments support Tab completion.  |  所有营养 ID 参数都支持 Tab 补全。
> 💡 Entrance visibility arguments support Tab completion.  |  入口可见性参数支持 Tab 补全。

---

## ⚙️ Configuration File  |  配置文件

Located at `config/appleseed-common.toml`.

位置：`config/appleseed-common.toml`。

### [General_Settings]

| Key | Type | Default | Description |
|:---|:---:|:---:|:---|
| `ignore_hunger` | bool | `false` | Whether eating when full still counts nutrition / 饱食度满时食用是否还计算营养 |
| `entrance_visibility` | string | `"default"` | Button visibility: `invisible`, `default`, `ftb_compact` / 按钮可见性模式 |
| `craft_chain_search_depth` | int | `3` | Maximum recursion depth for non-food ingredients in recipe tracing / 配方追溯中非食物原料的最大递归深度 |

### [Group_Overrides]

Override group-level behavior. Each preset group has:
- `*_is_negative` — Nutrition from this group is ignored during auto-calculation
- `*_ignore_attack` — Nutrition does NOT decay when taking damage
- `*_ignore_hunger` — Nutrition does NOT decay when hunger decreases
- `*_ignore_saturation` — Nutrition does NOT decay when saturation decreases

覆盖营养组级别的行为。每个预设组拥有：
- `*_is_negative` — 自动计算时忽略该营养组的贡献
- `*_ignore_attack` — 受到攻击时该营养素不衰减
- `*_ignore_hunger` — 饱食度降低时该营养素不衰减
- `*_ignore_saturation` — 饱和度降低时该营养素不衰减

Example:

```toml
[Group_Overrides]
    grains_is_negative = false
    grains_ignore_attack = false
    grains_ignore_hunger = false
    grains_ignore_saturation = false
    fruits_is_negative = false
    fruits_ignore_attack = false
    fruits_ignore_hunger = false
    fruits_ignore_saturation = false
    vegetables_is_negative = false
    vegetables_ignore_attack = false
    vegetables_ignore_hunger = false
    vegetables_ignore_saturation = false
    proteins_is_negative = false
    proteins_ignore_attack = false
    proteins_ignore_hunger = false
    proteins_ignore_saturation = false
    sugars_is_negative = false
    sugars_ignore_attack = false
    sugars_ignore_hunger = false
    sugars_ignore_saturation = false
```

### [Nutritions_Settings] > [Effects_Override]

Override effect ranges per group. Leave empty (default) to use the data file definitions.

覆盖每个营养组的效果范围。留空（默认）则使用数据文件中的定义。

```toml
[Nutritions_Settings.Effects_Override]
    # grains_ranges = ["0-25:effect(minecraft:slowness,0)"]
    # fruits_ranges = ["0-25:effect(minecraft:mining_fatigue,0)"]
```

### [Nutritions_Settings]

| Key | Type | Default | Description |
|:---|:---:|:---:|:---|
| `grains_initial` | double | `0.5` | Initial nutrition value for new players / 新玩家谷物初始值 |
| `fruits_initial` | double | `0.5` | Initial nutrition value for new players / 新玩家水果初始值 |
| `vegetables_initial` | double | `0.5` | Initial nutrition value for new players / 新玩家蔬菜初始值 |
| `proteins_initial` | double | `0.5` | Initial nutrition value for new players / 新玩家蛋白质初始值 |
| `sugars_initial` | double | `0.5` | Initial nutrition value for new players / 新玩家糖类初始值 |

---

## 📁 Data Pack Configuration  |  数据包配置

### 📂 Directory Structure  |  目录结构

```
data/
└── <namespace>/
    └── diet/
        ├── groups/               ← Group definitions (required / 必需)
        │   ├── grains.json
        │   ├── fruits.json
        │   ├── vegetables.json
        │   ├── proteins.json
        │   ├── sugars.json
        │   └── disabled_groups.json   ← Groups to disable (array / 数组)
        └── foods/                ← Food nutrition data (optional / 可选)
            ├── apple.json
            ├── bread.json
            └── ...
```

### 📄 Food Data Format  |  食物数据格式

Each file defines one or more food items. Supports both single-object and array formats. The `auto_calculated` field defaults to `false` when omitted.

每个文件定义一个或多个食物。支持单对象和数组两种格式。`auto_calculated` 字段缺失时默认为 `false`。

```json
// Single object format / 单对象格式 — diet/foods/apple.json
{
    "source_item": "minecraft:apple",
    "nutritions": {
        "fruits": 0.02
    }
}
```

```json
// Array format, only for file config/appleseed_data.json / 数组格式，仅可在config/appleseed_data.json中使用
[
    {
        "source_item": "minecraft:apple",
        "nutritions": {
            "fruits": 0.02
        }
    },
    {
        "source_item": "minecraft:bread",
        "nutritions": {
            "grains": 0.05
        }
    }
]
```

#### source_item / source_block — String or Array  |  字符串或数组

The `source_item` and `source_block` fields accept both a single string and an array of strings.

`source_item` 和 `source_block` 字段接受单独的字符串或字符串数组。

```json
// Single ID / 单个 ID
{
    "source_item": "minecraft:apple",
    "nutritions": { "fruits": 0.02 }
}
```

```json
// Array of IDs / 多个 ID
{
    "source_item": ["minecraft:apple", "minecraft:golden_apple"],
    "nutritions": { "fruits": 0.02 }
}
```

#### enable_tag_search — Tag-based Assignment  |  基于标签的分配

When `enable_tag_search` is `true`, `source_item` / `source_block` are treated as tag IDs. All items/blocks matching the tag will have the same nutrition data.

当 `enable_tag_search` 为 `true` 时，`source_item` / `source_block` 被视为标签 ID。匹配该标签的所有物品/方块都将获得相同的营养数据。

```json
// Tag-based assignment / 基于标签的分配
{
    "source_item": "c:crops/wheat",
    "enable_tag_search": true,
    "nutritions": { "grains": 0.03 }
}
```

Priority rules for tag-based data:  |  标签数据的优先级规则：
- Direct ID (`enable_tag_search=false`) → **Highest** / 直接指定 ID → **最高**
- `c:` namespace tags → **Medium** / `c:` 命名空间标签 → **中等**
- Other namespace tags → **Lowest** / 其他命名空间标签 → **最低**

| Field | Required | Type | Default | Description |
|:---|:---:|:---:|:---:|:---|
| `source_item` | Yes* | string or array | — | Item ID(s) / 物品 ID |
| `source_block` | Yes* | string or array | — | Block ID(s) / 方块 ID |
| `nutritions` | Yes | object | — | Map of group name → nutrition value (0.0 – 1.0) / 营养组名 → 营养值映射 |
| `auto_calculated` | No | bool | `false` | Whether this entry is auto-generated / 是否为自动生成 |
| `enable_tag_search` | No | bool | `false` | Whether source_item/source_block are tag IDs / source_item/source_block 是否为标签 ID |
| `bites` | No | int | `1` | (Block only) Number of bites / （仅方块）可食用次数 |

> *Either `source_item` or `source_block` must be present.  |  *`source_item` 或 `source_block` 必须存在其一。

### 📄 Block Food Data Format  |  方块食物数据格式

Block food files use `source_block` instead of `source_item`, and support an optional `bites` field.

方块食物文件使用 `source_block` 而非 `source_item`，并支持可选的 `bites` 字段。

```json
{
    "source_block": "minecraft:cake",
    "bites": 7,
    "nutritions": {
        "grains": 0.03,
        "sugars": 0.15,
        "proteins": 0.02
    }
}
```

Block foods also support arrays and `enable_tag_search`:

方块食物同样支持数组和 `enable_tag_search`：

```json
{
    "source_block": ["minecraft:cake", "minecraft:pumpkin_pie"],
    "enable_tag_search": false,
    "bites": 1,
    "nutritions": { "sugars": 0.1 }
}
```

### 📄 Group Definition Format  |  营养组定义格式

```json
{
    "icon": "minecraft:apple",
    "color": "#9e2a2b",
    "order": 1,
    "default_value": 0.5,
    "gain_multiplier": 1.0,
    "decay_multiplier": 1.0,
    "beneficial": true,
    "is_negative": false,
    "ignore_attack": false,
    "ignore_hunger": false,
    "ignore_saturation": false,
    "translation_key": "diet.group.fruits",
    "effects": [
        "0-25:effect(minecraft:mining_fatigue,0)",
        "61-70:attribute(minecraft:generic.max_health,2.0)",
        "71-80:attribute(minecraft:generic.max_health,4.0)",
        "81-100:attribute(minecraft:generic.max_health,6.0)"
    ]
}
```

| Field | Type | Default | Description |
|:---|:---:|:---:|:---|
| `icon` | string | — | Item ID used as the group icon / 用作图标的物品 ID |
| `color` | string | — | Hex color for UI display / UI 显示的十六进制颜色 |
| `order` | int | `0` | Sort order in the diet screen / 膳食界面中的排序 |
| `default_value` | float | `0.0` | Starting nutrition value for new players (0.0 – 1.0) / 新玩家初始营养值 |
| `gain_multiplier` | double | `1.0` | Multiplier for nutrition gained from food / 食物营养获取倍率 |
| `decay_multiplier` | double | `1.0` | Multiplier for nutrition decay rate / 营养衰减速率倍率 |
| `beneficial` | bool | `true` | Whether higher values are good (affects UI color) / 高值是否有益（影响UI颜色） |
| `is_negative` | bool | `false` | Whether this group is negative (penalizes player) / 该组是否为负面组 |
| `ignore_attack` | bool | `false` | Whether to ignore attack damage as decay source / 是否忽略受击衰减 |
| `ignore_hunger` | bool | `false` | Whether to ignore hunger as decay source / 是否忽略饥饿衰减 |
| `ignore_saturation` | bool | `false` | Whether to ignore saturation loss as decay source / 是否忽略饱和度衰减 |
| `translation_key` | string | — | Translation key for the group name / 营养组名称的本地化键 |
| `effects` | array | — | Effect range definitions / 效果范围定义 |

> 💡 If `ignore_saturation` is missing from the data file, a warning is logged and it defaults to `false`.
> 💡 如果数据文件中缺少 `ignore_saturation` 字段，会记录警告并默认为 `false`。

### 📄 Effects Format  |  效果格式

```
"min-max:effect(namespace:id,amplifier),attribute(namespace:id,amount),..."
```

| Element | Format | Example |
|:---|:---|:---|
| `effect(id,amplifier)` | `effect(modid:effect_id,level)` | `effect(minecraft:slowness,0)` = Slowness I |
| `attribute(id,amount)` | `attribute(modid:attr_id,value)` | `attribute(minecraft:generic.max_health,4.0)` = +4 Max Health |

Multiple effects/attributes in one range are separated by commas.

同一范围内的多个效果/属性用逗号分隔。

### 🚫 Disabling Groups  |  禁用营养组

`disabled_groups.json` — an array of group names to disable entirely:

`disabled_groups.json` — 一个包含要完全禁用的营养组名称的数组：

```json
["grains", "sugars"]
```

### 🍎 appleseed_data.json

A special config file at `config/appleseed_data.json` with the **highest priority**. Format is an array of food entries:

位于 `config/appleseed_data.json` 的特殊配置文件，拥有**最高优先级**。格式为食物条目数组：

```json
[
    {
        "source_item": "minecraft:golden_apple",
        "nutritions": {
            "fruits": 0.5,
            "sugars": 0.3
        }
    }
]
```

---

## 🔌 Dependencies  |  依赖

### Hard Dependency  |  硬依赖

- **CloudWorks API** — Required for recipe parsing and auto-calculation.  |  **CloudWorks API** — 配方解析和自动计算所必需。
  - CurseForge: https://www.curseforge.com/minecraft/mc-mods/cloudworks-api
  - Maven: `curse.maven:cloudworks-api-1621946:8498808`

### Soft Dependencies  |  软依赖

### FTB Library Compat  |  FTB Library 兼容

When FTB Library is installed, the mod supports registering a sidebar button to open the diet screen.
Set `entrance_visibility` to `ftb_compact` in config, or use `/diet config set entranceVisibility ftb_compact`.

当安装 FTB Library 时，支持在侧边栏注册按钮以打开膳食均衡界面。
在配置中设置 `entrance_visibility` 为 `ftb_compact`，或使用 `/diet config set entranceVisibility ftb_compact`。

The button is defined via the JSON asset at `assets/appleseed/sidebar_buttons/diet.json` and appears in FTB Library's sidebar as "Diet Balance".

按钮通过 `assets/appleseed/sidebar_buttons/diet.json` JSON 资源定义，在 FTB Library 侧边栏显示为「Diet Balance」。

### SomeAssemblyRequired Compat  |  SomeAssemblyRequired 兼容

Sandwiches from **SomeAssemblyRequired** are fully supported — their nutritional value is calculated from all ingredients inside the sandwich.

完整支持 **SomeAssemblyRequired** 的三明治 — 根据三明治内所有原料计算营养值。

---

## 🎮 Key Binding  |  按键绑定

A key binding is registered under **Controls → AppleSeed → Open Diet GUI**.
Can be used to open the diet screen regardless of entrance visibility mode.

在 **控制 → AppleSeed → Open Diet GUI** 中注册了按键绑定。
无论入口可见性模式如何，均可使用该按键打开膳食均衡界面。

---

## 🔌 API for Mod Developers  |  模组开发者 API

AppleSeed provides a public API for other mods to integrate with.

AppleSeed 提供公共 API 供其他模组集成。

### Query API  |  查询 API

#### Diet Query — Nutrition Data  |  膳食查询 — 营养数据

```java
IDietFoodQuery query = DietQuery.getInstance();
if (query != null) {
    Map<String, Float> nutritions = query.getNutritions(Items.APPLE);
    float fruitValue = query.getNutritionValue(Items.APPLE, "fruits");
    boolean hasData = query.hasNutritionData(Items.CAKE);
}
```

#### Decay Query — Decay Multipliers  |  衰减查询 — 衰减系数

```java
IDietDecayQuery query = DietDecayQuery.getInstance();
if (query != null) {
    double hitMult = query.getHitDecayMultiplier(player);
    double hungerMult = query.getHungerDecayMultiplier(player);
    double saturationMult = query.getSaturationDecayMultiplier(player);
}
```

### Hook API  |  钩子 API

| Hook Type | Logic | Description |
|:---|:---|:---|
| Item Food Eat | OR + Chain | Intercept eating, modify gains, after-eat callbacks |
| Block Food Eat | AND + Chain | Block food processing permission and gain modification |
| Recipe Filter | AND | Filter which recipes/items are processed |
| Tooltip Filter | AND + Chain | Control tooltip visibility and modify displayed values |
| Nutrition Rule | Chain | Modify nutrition add/set/decay values |

```java
// Example: Register an item food eat hook
DietHookRegistry.registerItemFoodEatHook(new IDietItemFoodEatHook() {
    @Override
    public boolean shouldIntercept(Player player, ItemStack stack) {
        return false;
    }
    
    @Override
    public Map<String, Float> modifyNutritionGains(Player player, ItemStack stack, 
                                                    Map<String, Float> originalGains) {
        return originalGains;
    }
    
    @Override
    public void onAfterEat(Player player, ItemStack stack) {
    }
});
```

### Type Interfaces  |  类型接口

- `IDietGroup` — Nutrition group interface
- `IDietSuite` — Suite of nutrition groups
- `IDietEffect` — Dietary effect with conditions and modifiers
- `IDietCondition` — Effect activation condition
- `IDietAttribute` — Attribute modifier
- `IDietStatusEffect` — Status effect modifier
- `IDietResult` — Immutable nutrition snapshot
- `DietColor` — RGBA color utility

---

## 📦 Modpack Usage  |  整合包使用

This mod is designed with modpack creators in mind:

- **Create custom food nutrition data** in your datapack under `data/<namespace>/diet/foods/`
- **Use tag-based assignment** with `enable_tag_search` for bulk food configuration
- **Override group behaviors** via `appleseed-common.toml` → `[Group_Overrides]`
- **Define highest-priority food data** in `config/appleseed_data.json`
- **Disable unwanted groups** via `disabled_groups.json`
- **Customize nutrition effects** in group JSON files under `diet/groups/`
- **Auto-calculation handles all modded foods** — no manual data needed for most items
- **Tune decay rates** via gamerules for balance customization
- **Control saturation decay** with `ignore_saturation` per group

本模组为整合包作者设计：

- **创建自定义食物营养数据**：在数据包的 `data/<namespace>/diet/foods/` 下
- **使用标签批量分配**：通过 `enable_tag_search` 批量配置食物
- **覆盖营养组行为**：通过 `appleseed-common.toml` → `[Group_Overrides]`
- **定义最高优先级食物数据**：在 `config/appleseed_data.json` 中
- **禁用不需要的营养组**：通过 `disabled_groups.json`
- **自定义营养效果**：在 `diet/groups/` 下的组 JSON 文件中
- **自动计算覆盖所有 Mod 食物** — 大多数物品无需手动编写数据
- **调整衰减速率**：通过游戏规则自定义平衡
- **控制饱和度衰减**：通过每组的 `ignore_saturation`

---

## 📝 License  |  许可证

LGPLv3
