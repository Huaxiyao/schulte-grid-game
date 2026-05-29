# 🧠 舒尔特方格

一个零依赖、纯前端的舒尔特方格专注力训练游戏，单 HTML 文件，双击即用。

## 快速开始

```bash
# 克隆仓库
git clone https://github.com/Huaxiyao/schulte-grid-game.git
cd schulte-grid-game

# 直接打开
start schulte-grid.html   # Windows
open schulte-grid.html    # macOS
```

或直接通过 [GitHub Pages](https://huaxiyao.github.io/schulte-grid-game/schulte-grid.html) 在线使用。

## 功能

| 功能 | 说明 |
|---|---|
| 🎮 **多难度** | 3×3 到 7×7 五档可选 |
| 🔄 **反向模式** | 从大到小点击，训练不同脑区 |
| ⏱ **实时计时** | 首击自动启表，结束自动停止 |
| 🎯 **目标提示** | 当前应点击的数字实时高亮 |
| 🔊 **音效反馈** | Web Audio 合成音，支持静音 |
| 🌙 **暗色模式** | 跟随系统偏好或手动切换 |
| 👁 **隐藏计时器** | 减少焦虑，专注训练本身 |
| ⏳ **倒计时启动** | 可选 3-2-1 准备阶段 |
| ⌨️ **键盘操作** | 方向键移动焦点，空格/回车点击 |
| 🏆 **成绩记录** | 最佳记录 + 最近趋势，localStorage 持久化 |
| 📱 **响应式** | 桌面和移动端均可使用 |

## 玩法

舒尔特方格是一种经典的专注力训练方法：

1. 网格中随机分布数字 1 到 N
2. 按顺序依次点击（1 → 2 → 3 …）
3. 用时越短，说明注意力越集中

持续练习可提升 **视觉搜索速度**、**注意力广度** 和 **周边视觉敏感度**。

## 技术

- 纯 HTML + CSS + JavaScript，零外部依赖
- 计时器基于 `requestAnimationFrame`，后台标签页自动暂停
- 音效使用 Web Audio API，零音频文件
- 所有设置和成绩记录通过 `localStorage` 持久化
