# BlockCraft — Architecture and Implementation Plan

Status: approved planning baseline; features listed below are targets, not claims of implementation.

## Product decisions

- Target: Windows desktop first, Java 21, LWJGL 3, Gradle, Inno Setup.
- Gameplay: Survival, Creative, Adventure and Spectator; Minecraft Java-inspired controls and UI, with original BlockCraft content.
- World: procedural chunk-based overworld, expandable built-in dimensions, varied terrain/biomes, day/night, weather, caves and structures.
- Content: real, distinct block/item definitions and texture assets; selectable resource packs; original mobs with models, animations, AI, drops and spawn rules.
- Multiplayer: server-authoritative protocol; LAN hosting; direct address/port connection; dedicated server support; local favorites plus an optional public server directory; target 32 players, subject to performance testing; global/private/team chat; permissions and moderation.
- Offline: local profile without mandatory account or internet for single-player. Internet multiplayer and public directory features naturally require connectivity.
- Modding: BlockCraft-specific API plus resource-pack support. Minecraft Forge/Fabric binary compatibility is not promised.
- Dimensions: ship built-in dimensions first; design dimension definitions so future extensions can add more.
- Delivery: Windows installer and launcher; phased changes; each phase must build and test before being considered complete.
- Legacy Android source: retain separately as archived reference, not as an active Gradle subproject.

## Current baseline verified from repository inspection

The current main branch is `692ed9aeb2f89ff0cb0a486ce183e2c09ac8232b`. The latest Windows workflow run inspected during planning is run #20:
https://github.com/Iustin-dev2014/BlockCraft/actions/runs/40192765022

The current project is a small LWJGL prototype. It has basic movement and cube rendering, a fixed-size world, a generated block catalog, a local properties-based save format, basic configuration, and an early chat input field. It does not yet implement a functional LAN/server networking stack, synchronized multiplayer chat, a real mob/AI system, chunk-based infinite terrain, real block texture rendering, a full survival/inventory/crafting loop, or a complete world manager. A successful build proves compilation/package pipeline only, not gameplay completeness.

## Architecture principles

1. Keep simulation and game state independent from OpenGL rendering.
2. Use chunk coordinates and deterministic world seeds; generate chunks asynchronously and mesh only visible faces.
3. Give blocks/items stable namespaced IDs and explicit definitions, drops, hardness, collision, transparency, and texture references. Never inflate counts with fake color variants.
4. Store worlds in a versioned, migration-capable format with metadata, seed, player state, chunk data, game mode, time, weather, and dimension.
5. Use a resource-pack resolver with a documented folder/manifest format and fallback assets.
6. Use an entity model with stable entity IDs, component/state data, movement/collision, AI ticks, animation state, health, damage and drops.
7. Keep the authoritative simulation on the host/server in multiplayer. Validate movement, block edits, inventory, chat and commands. Do not trust clients to decide shared state.
8. Make Open to LAN a real host lifecycle: choose port/options, start listener, advertise local address where possible, show join details, handle joins/leaves, and stop safely. Router/firewall/NAT restrictions must be explained rather than bypassed magically.
9. The public server directory is optional online infrastructure, separate from direct-IP connection and local favorites.
10. Use local offline profiles for single-player identity. Do not represent offline names as authenticated online accounts.
11. Keep a BlockCraft mod API and resource-pack API versioned. Treat Minecraft Forge/Fabric compatibility as out of scope unless a separate feasibility project proves it realistic.
12. Each implementation phase must add tests or reproducible checks and update this plan with actual status.

## Phased roadmap

### Phase 0 — Baseline and project hygiene
- Preserve a working Windows build.
- Add architecture and feature status documentation.
- Keep Android sources archived outside active source sets and Gradle settings.
- Add basic unit-test infrastructure for deterministic world and save logic.

### Phase 1 — Core engine foundation
- Separate game loop, input, simulation, rendering, and world state.
- Implement deterministic chunk coordinates, chunk storage, procedural terrain and safe chunk boundaries.
- Correct targeting, mining, placement, player collision, gravity and camera controls.
- Introduce real block definitions and texture atlas/resource loading.
- Add chunk mesh generation, face culling and a configurable render distance.

### Phase 2 — Worlds and persistence
- Create/select/rename/delete worlds.
- Seed, game mode, difficulty, dimension, time and weather metadata.
- Versioned chunk/player save format, autosave, manual save, backup and migration tests.
- Built-in dimension framework; add dimensions only once world transitions and persistence are stable.

### Phase 3 — Gameplay
- Inventory, hotbar, item stacks, tools, durability, drops and pickups.
- Crafting stations and data-driven recipes.
- Health, hunger, armor, death/respawn and Hardcore rules.
- Creative, Survival, Adventure and Spectator modes.
- Achievements and statistics.

### Phase 4 — Visuals and atmosphere
- Selectable resource packs and original distinct texture assets.
- Day/night sky, sun/moon, stars, weather, fog and lighting settings.
- Water/foliage treatment, particles, sound and music controls.
- Low/Medium/High/Ultra graphics profiles, FPS cap, VSync and resolution/fullscreen options.

### Phase 5 — Entities and combat
- Entity registry and original mob definitions.
- Model/animation pipeline, spawn rules, pathfinding, hostile/passive AI, combat, drops and sound.
- Bosses and biome-specific mobs after baseline entity behavior is stable.

### Phase 6 — Multiplayer
- Protocol/version handshake and authoritative server simulation.
- Join/leave lifecycle, player replication, chunk delivery and block edit synchronization.
- Open to LAN host UI and reliable shutdown.
- Direct IP/port connection and dedicated server.
- Global/private/team chat, permissions, operator commands, mute/kick/ban and whitelist.
- Local favorites and optional public server directory service.
- Load/latency tests; 32-player support remains a target until measured.

### Phase 7 — Extensibility and offline identity
- Local offline profile and player name/settings persistence.
- Resource pack manifest and compatibility validation.
- Versioned BlockCraft mod API and examples.
- Public server directory integration only after a service endpoint and operational model exist.

### Phase 8 — Launcher and release
- Launcher with Play, local profile, settings, diagnostics and version display.
- Installer includes required runtime, resources and documentation.
- Validate clean Windows installation, first launch, save/load, uninstall behavior and artifact hashes.
- Publish only features verified in game, not merely compiled.

## Definition of done

A feature is complete only when the code exists, relevant automated tests pass, the Windows GitHub Actions workflow succeeds, and the feature has been manually exercised in-game where automation cannot validate it. Workflow success alone is not evidence that all gameplay works.

## Explicit non-promises

- Minecraft Forge/Fabric mods will not load directly unless a dedicated compatibility implementation is proven.
- A public server list requires a reachable service; it cannot be implemented solely as a local UI.
- Internet hosting may require port forwarding or a relay; LAN hosting does not automatically make a game reachable across the internet.
- A 32-player limit is a performance target until stress-tested.
