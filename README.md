# OneFizz

Minecraft 1.21 Fabric client mod with ImGui-based GUI.

## Build

```bash
./gradlew build
```

Output: `build/libs/onefizz-1.0.0.jar` (~8 MB, includes imgui-java natives)

## Requirements

- Java 21
- Minecraft 1.21
- Fabric Loader 0.15.11+
- Fabric API 0.100.4+

## Installation

1. Build the mod: `./gradlew build`
2. Copy `build/libs/onefizz-1.0.0.jar` to your Minecraft instance `mods/` folder
3. Launch Minecraft with Fabric Loader 0.15.11+

## Keybind

**Right Shift** — open/close main GUI

## Architecture

- **Rendering**: Full ImGui (imgui-java 1.86.11) via `ImGuiImplGl3`, no Minecraft `DrawContext`
- **Font**: Segoe UI 17px (system) with fallback to default ImGui font
- **GL Integration**: `ImGuiImplGl3` with explicit GL state save/restore per frame
- **Theme System**: 10 built-in themes, `Azure` default
- **Config**: JSON-based, per-profile (`onefizz-configs/`)
- **Alts**: `onefizz-alts.json` with Offline + Microsoft OAuth device-code support

## Modules

### Combat
| Module | Description |
|--------|-------------|
| KillAura | Target selection, aim, timing presets (Legacy/Modern) |
| AutoClicker | CPS/Jitter/Burst modes, auto-aim, weapon-only filter |
| Criticals | Auto-crit on hit |
| AutoGapple | Auto eat gapple at low HP |
| AutoTotem | Auto offhand totem |
| BowAimbot | Projectile prediction |
| Reach | Extend attack range |
| HitBox | Expand entity hitboxes |
| AntiBot | Filter non-player entities |
| AutoArmor | Auto equip best armor |
| Velocity | Cancel knockback |
| AntiKnockback | Reduce/block knockback |
| TriggerBot | Auto-attack on crosshair |
| AutoWeapon | Auto swap to best weapon |
| FakeLag | Choke packets |
| Backtrack | Position history for lag compensation |
| TargetStrafe | Circle strafe around target |

### Visuals
| Module | Description |
|--------|-------------|
| ESP | Player boxes, tracers, health, armor |
| MobESP | Hostile mob ESP |
| ChestESP | Container ESP |
| Fullbright | Night vision |
| XRay | See ores through walls |
| JumpCircles | Visual circles on jump |
| ChinaHat | Particle hat on players |
| ArrayList | Active module list HUD |
| PlayerHUD | Local player stats panel |
| Perspective | Third-person/freecam |
| Watermark | Top-left client tag + FPS |
| Trajectories | Projectile arc prediction |
| NoRender | Disable particles/fog/fire |
| BlockOutline | Highlight targeted block |

### Movement
| Module | Description |
|--------|-------------|
| Flight | Creative-like flight |
| ElytraFly | Elytra boost/glide |
| BoatFly | Boat flight |
| Speed | Ground speed boost |
| Sprint | Auto-sprint |
| Strafe | Air strafe optimization |
| Jesus | Walk on liquids |
| Step | Auto step up blocks |
| Timer | Game speed manipulation |
| NoFall | Cancel fall damage |
| NoSlowdown | Cancel web/soul sand slowdown |
| FastPlace | Instant block placement |
| Scaffold | Auto bridge |
| AutoBridge | Advanced bridging |
| InventoryMove | Move in inventory |
| ClickWarp | Teleport on click |
| AirJump | Double jump |
| Spider | Wall climb |
| Parkour | Auto parkour assist |

### Player
| Module | Description |
|--------|-------------|
| AutoEat | Auto eat food |
| FastBreak | Instant break |
| AntiVoid | Teleport up on void |
| NoHunger | Freeze hunger |
| NoHurt | Cancel damage |
| ChestStealer | Auto loot chests |
| AutoTool | Auto swap tool |
| Blink | Packet lag |
| AutoInvite | Auto accept invites |
| PacketInspector | Log packets |
| AntiAFK | Prevent kick |
| AutoLeave | Auto disconnect |
| AutoRespawn | Instant respawn |
| Spammer | Chat spammer with anti-spam |
| **AutoReg** | `/register 123777 123777` every 5s |
| **StaffList** | Posts `[OneFizz] На сервере замечено (N) модераторов/администраторов` every 30s |

### Bypass
| Module | Description |
|--------|-------------|
| DonkeyDupe | Container dupe |
| Disabler | Anti-cheat bypass |
| AntiCheatDetector | Detect AC packets |
| PacketLimiter | Limit packet rate |
| TimerBypass | Timer bypass |
| Phase | Wall phase |

## GUI

- **Right Shift** — toggle main window
- **Tabs**: Combat / Visuals / Movement / Player / Alts / Misc
- **Search** — filter modules by name
- **Right-click module** — open settings
- **Drag header** — move window
- **Misc tab** — Config manager (save/load/delete), Theme switcher
- **Alts tab** — Alt Manager (Offline/Microsoft OAuth device-code)

## Config System

- Saved to `onefizz-configs/<name>.json`
- Stores: enabled state, keybinds, all `@Setting` fields
- UI in Misc tab

## Themes (10)

`Amethyst`, `Azure` (default), `Aqua`, `Emerald`, `Lime`, `Amber`, `Rose`, `Crimson`, `Midnight`, `Mono`

## Alt Manager

- **Offline**: username only (cracked servers)
- **Microsoft**: Full OAuth device-code flow (browser)
- Saved to `onefizz-alts.json`

## Technical Details

- **ImGui Backend**: `imgui-java-app` + `imgui-java-lwjgl3` + `imgui-java-natives-windows`
- **GL Version**: Core profile 3.2+ (`#version 150`)
- **GL State**: Explicit save/restore (program, texture, VAO, buffers, blend, cull, depth, scissor)
- **Font**: `C:\Windows\Fonts\segoeui.ttf` 17px, 5x oversample, Cyrillic ranges
- **Input**: Minecraft `Screen` forwards GLFW events to ImGui `IO`

## License

Private use only.
