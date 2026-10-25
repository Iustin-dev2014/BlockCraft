# 🧱 BlockCraft — Windows Edition

**BlockCraft** is an independent voxel sandbox game in active development, built specifically for Windows desktop PCs using Java 21, LWJGL, and Gradle.

> 🚧 **Early development:** This repository is focused exclusively on the Windows desktop game. Roadmap items below are planned work, not claims that every feature is already implemented.

## Windows-first direction
- A Windows-native desktop application and standalone installer.
- Procedural terrain and a growing library of distinct block materials.
- Original texture families for stone, soil, ores, wood, plants, water, construction materials, and decorative blocks.
- Full in-game interfaces: title screen, world selection/creation, pause menu, settings, inventory, crafting, and accessibility options.
- Stable mining, building, world saving/loading, movement, and collision before advanced systems.
- Progressive visual improvements: textured geometry, lighting, sky/weather, particles, sound, and environmental details.
- Large content packs should come from useful, distinct game assets and implemented features—not filler files added only to inflate the installer.

## Development roadmap
1. [x] GitHub-hosted Windows build runner and Windows installer pipeline
2. [ ] Proper in-game title, pause, world selection, and settings menus
3. [ ] Real texture rendering and hundreds of distinct, original block textures
4. [ ] Chunk-based world rendering, terrain details, and performance improvements
5. [ ] Reliable block targeting, mining, placement, and collision
6. [ ] Inventory, hotbar, crafting, tools, and survival progression
7. [ ] Lighting, water, foliage, particles, sound, and environmental effects
8. [ ] Windows launcher, diagnostics, settings, and installer polish

## Build
The project uses Java 21 and Gradle. GitHub Actions runs the desktop build on `windows-latest`, packages a standalone Windows application image with `jpackage`, then creates a Windows installer with Inno Setup. The installer size is reported for measurement, not artificially forced to a target size.

A build is only considered verified after the Windows workflow completes successfully and the produced artifact is checked.

## License and ownership
BlockCraft is distributed under the **BlockCraft Source-Available Proprietary License 1.0**, included in `LICENSE`. Public visibility does not grant permission to copy, modify, redistribute, repackage, or create derivative works. Contact the copyright holder for written permission. Third-party dependencies remain under their respective licenses.

BlockCraft is an independent project inspired by the voxel sandbox genre. It is not an official Minecraft product and is not affiliated with or endorsed by Mojang or Microsoft.

---
**Windows only. Build worlds. Break limits. Make it yours.** 🧱✨
