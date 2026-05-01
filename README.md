<div align="center">

# 🍎 Diet - AppleSeed Edition

**Neoforge 1.21.1** | **数据驱动** | **完全可配置** | **整合包友好**

[![](https://img.shields.io/badge/Mod%20Loader-Neoforge-%2347B04B?style=flat-square)](https://neoforged.net/)
[![](https://img.shields.io/badge/Minecraft-1.21.1-%23737373?style=flat-square)](https://minecraft.net/)
[![](https://img.shields.io/badge/Version-1.0.0-%232563EB?style=flat-square)]()
[![](https://img.shields.io/badge/License-MIT-green?style=flat-square)]()

---

### 膳食均衡，健康生活 🥗

让 Minecraft 的饮食系统更有深度！吃不同种类的食物获得加成，饮食不均衡获得负面效果。

---

<img src="src/main/resources/META-INF/logo.png" alt="Diet - AppleSeed Edition Logo" width="256"/>

---

</div>

## 📖 关于本模组

**Diet - AppleSeed Edition** 是著名膳食均衡模组 **Diet** 的精神续作，为 Neoforge 1.21.1 完全重写。本模组引入了一套完整的饮食营养系统，鼓励玩家多样化饮食，而不是只吃金胡萝卜！

> 💡 **本模组特性：**
> - ✅ 与原版 Diet 数据格式兼容
> - ✅ 100% 数据驱动，零硬编码
> - ✅ 强大的自动营养计算引擎
> - ✅ 整合包开发者专属调试工具
> - ✅ 完整的配置系统
> - ✅ 支持所有 Mod 食物

---

## 🎯 五大营养组

| 图标 | 营养组 | 负面效果 (0-25%) | 进阶效果 (61-80%) | 满级效果 (81-100%) |
|:---:|:---:|:---:|:---:|:---:|
| 🌾 | **谷物** | 缓慢 I | 生命上限 +6<br>生命恢复 I | 生命上限 +6<br>攻击伤害 +1<br>生命恢复 I |
| 🥬 | **蔬菜** | 反胃 I | 生命上限 +2<br>护甲韧性 +3 | 生命上限 +2<br>护甲韧性 +4<br>急迫 I |
| 🥩 | **蛋白质** | 虚弱 I | 生命上限 +4<br>护甲值 +2 | 生命上限 +6<br>护甲值 +4<br>抗性提升 I |
| 🍎 | **水果** | 挖掘疲劳 I | 生命上限 +4<br>攻击速度 +0.1 | 生命上限 +6<br>攻击速度 +0.2 |
| 🍬 | **糖类** | 无 | 速度 II | 速度 II<br>饥饿 V |

> 💡 **26% - 60%：所有营养组均无效果**
> 
> 💡 **糖类特殊：0-50% 均无效果，51% 开始提供速度加成**

---

## 🎮 游戏内功能

### 📱 用户界面

按 `E` 打开背包 → 点击配方书右侧的 **「膳食均衡」按钮**：

```
┌─────────────────────────────────────────────┐
│                  膳食均衡                   │
├─────────────────────────────────────────────┤
│  🌾 谷物     [████████░░░░]  80%            │
│  🥬 蔬菜     [███████░░░░░]  70%            │
│  🥩 蛋白质   [████████████] 100%            │
│  🍎 水果     [████░░░░░░░░]  40%            │
│  🍬 糖类     [█████████░░░]  90%            │
├─────────────────────────────────────────────┤
│  ✨ 当前激活效果：                          │
│     抗性提升 I                              │
│     +6.0 最大生命值                        │
│     +4.0 护甲值                            │
└─────────────────────────────────────────────┘
```

### 💬 物品提示

**所有可食用物品**都会在 tooltip 中显示营养值：

```
蛋糕
──────
营养值：
  谷物: +3.2%
  糖分: +15.8%
  蛋白质: +1.5%
```

> 💡 仅显示数值 > 0 的营养素

---

## ⚙️ 营养机制

### 📉 营养衰减

你的营养值会随时间和行动衰减：

| 事件 | 所有营养素减少 |
|:---|:---:|
| 每失去 1 点饱食度 | 0.5% |
| 每受到 1 次伤害 | 0.1% |

> 💡 这意味着：即使你只吃一种食物堆满了营养，也会很快因为营养不均衡吃到负面效果！

### 💀 死亡机制

通过游戏规则 **`keepNutritions`** 控制：

- **`false` (默认)**：死亡后重置为 50% 初始值
- **`true`**：死亡保留当前营养值

```
/gamerule keepNutritions true
```

---

## 🚀 智能自动计算引擎

### 🧠 配方驱动的营养计算

这是本模组最强大的特性！**不需要手动写任何数据文件！**

模组启动时会：
1. 🔍 扫描所有已注册的配方
2. 🍳 识别所有具有食物属性的物品
3. 🔗 递归追溯每个食物的原料
4. 🧮 自动计算每种食物的营养构成

### 📊 计算示例

以蛋糕为例：

| 原料 | 数量 | 营养素贡献 |
|:---|:---:|:---|
| 牛奶桶 | 3 | 蛋白质 |
| 糖 | 2 | 糖分 |
| 鸡蛋 | 1 | 蛋白质 |
| 小麦 | 3 | 谷物 |

```
蛋糕营养 = 3×牛奶 + 2×糖 + 鸡蛋 + 3×小麦
────────────────────────────────────────
最终产出：谷物 +3.2% / 糖分 +15.8% / 蛋白质 +1.5%
```

### ✅ 支持的配方类型

- ✅ 工作台合成（Shaped / Shapeless）
- ✅ 熔炉 / 烟熏炉 / 高炉
- ✅ 营火烹饪
- ✅ 切石机
- ✅ 锻造台
- ✅ 机械动力加工
- ✅ 农夫乐事烹饪

---

## 🔧 整合包开发者工具

### 🔄 /reload 动态调试

这是为整合包作者专门设计的功能！

```
1. 修改配方（如 KubeJS）
2. 游戏内输入 /reload
3. ⏳ 实时进度显示（每 2 秒）：
   §e[苹果籽]§r 正在计算食物营养：27/46 (32 项成功)
4. ✅ 完成时：
   §a[苹果籽]§r 食物营养计算完成！
5. ✅ 所有数据自动重载，不需要重启游戏！
```

> 💡 **重载模式会强制覆盖所有已生成的 config 文件，确保你的修改总是生效**

### 🎚️ 四层数据优先级

```
优先级从高到低：

    🥇 世界数据包 (world/datapacks)
        ↓
    🥈 其他模组内置数据
        ↓
    🥉 本模组内置数据
        ↓
    🏅 Config 自动生成内容
```

> 💡 这意味着：你可以在数据包中定义的营养值会覆盖自动计算的结果，对于平衡调整非常有用！

### 📁 数据位置

| 层级 | 路径 |
|:---|:---|
| 自动生成 | `config/apple_seed_foods/*.json` |
| 模组内置 | `data/*/diet/foods/*.json` |
| 数据包 | `(datapack)/data/*/diet/foods/*.json` |

### 📄 数据文件格式

```json
{
  "source_item": "minecraft:cake",
  "nutritions": {
    "grains": 0.032,
    "sugars": 0.158,
    "proteins": 0.015
  }
}
```

---

## 📐 配置文件

配置文件位置：`config/appleseed-server.toml`

```toml
[General_Settings]
    # 控制饱食度满时食用食物是否还计算营养值
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
        "..."
    ]
    ...
```

### ✏️ 效果语法

配置中的每一条范围定义都支持任意数量的效果叠加：

| 语法 | 示例 |
|:---|:---|
| 状态效果 | `effect(minecraft:regeneration,0)` |
| 属性修改 | `attribute(minecraft:generic.max_health,6.0)` |
| 多效果 | `effect(...),attribute(...)` |

---

## 🏗️ 技术架构

### 🎯 Neoforge 21.1 原生特性

本模组完全使用 Neoforge 最新的原生 API：

- ✅ 使用 **NBT Attachment** 存储玩家数据
- ✅ 原生 **AddReloadListener** 系统
- ✅ **OnDatapackSyncEvent** 数据同步
- ✅ 不需要 Mixin！不需要网络包！

> 💡 这意味着极佳的跨模组兼容性！几乎不会和任何模组冲突！

### 📊 性能特性

| 特性 | 实现 |
|:---|:---|
| 异步计算 | ✅ 配方处理完全异步，不卡主线程 |
| 循环检测 | ✅ 自动检测死循环配方，避免卡死 |
| 重复处理 | ✅ 已处理物品跳过机制 |
| 边界优化 | ✅ 非食物材料直接跳过递归 |

> 💡 即使有 100+ 模组的大型整合包，首次计算也只需要几秒钟！

---

## 🆚 与原版 Diet 的对比

| 特性 | Diet (Forge) | Diet - AppleSeed Edition |
|:---|:---:|:---:|
| 核心营养系统 | ✅ | ✅ |
| 数据驱动 | ✅ | ✅ |
| 配方自动计算 | ❌ | ✅ |
| /reload 动态重载 | ❌ | ✅ |
| 实时进度显示 | ❌ | ✅ |
| 四层优先级机制 | ❌ | ✅ |
| 内置所有原版食物数据 | ❌ | ✅ |
| 伤害触发营养衰减 | ❌ | ✅ |
| 支持 Neoforge 1.21.1 | ❌ | ✅ |
| 不需要 Mixin | ❌ | ✅ |

---

## 🤝 兼容性

✅ **所有模组的食物都自动支持！**

- ✅ **农夫乐事** - 所有食物
- ✅ **机械动力** - 所有加工食品
- ✅ **新农合** - 所有作物食物
- ✅ **更多食物** 类模组
- ✅ **任何**添加食物的模组

> 💡 只要你的食物有 FoodProperties，我们就能处理！

---

## 📝 致谢与许可

### 💝 致谢

本模组的核心设计灵感来源于：
- **TheIllusiveC4** 的原版 **Diet** 模组

### 📜 许可

本项目使用 **MIT** 许可。

---

<div align="center">

## ❤️ Enjoy your healthy diet!

### *Made with 💚 for the Minecraft modding community*

[![](https://img.shields.io/badge/GitHub-View_on_GitHub-%23181717?style=for-the-badge&logo=github)]()
[![](https://img.shields.io/badge/CurseForge-Download-%23F16436?style=for-the-badge)]()
[![](https://img.shields.io/badge/Modrinth-Download-%2300AF5C?style=for-the-badge)]()

</div>
