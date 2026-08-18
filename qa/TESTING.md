# Modern Minecarts QA runner

Run the test matrix from the NeoForge 1.21.11 checkout.  It uses detached,
temporary worktrees for every selected version, so it never switches the
current checkout and every Gradle invocation gets its own `build` and `run`
directories.  Versions are intentionally run one at a time: NeoForm generates
and replaces large source trees, which is unsafe to run in parallel on Windows.
The default temporary locations are `C:\mmqa-worktrees` and `C:\mmqa-gradle`.
Keeping them short avoids Windows path-length failures in generated Forge and
NeoForm files.

```powershell
# Compile all supported versions in isolated worktrees.
.\qa\Invoke-ModernMinecartsTests.ps1

# Run a headless GameTest server for one version once that branch has GameTests.
.\qa\Invoke-ModernMinecartsTests.ps1 -Mode GameTest -Branches forge/1.20.1

# Launch one isolated development client for the visual checks below.
.\qa\Invoke-ModernMinecartsTests.ps1 -Mode Client -Branches fabric/1.21.11 -KeepWorktrees
```

Set `MODERNMINECARTS_JAVA17_HOME`, `MODERNMINECARTS_JAVA21_HOME`, and
`MODERNMINECARTS_JAVA25_HOME` if the default JDK locations are different on
another machine.  `MODERNMINECARTS_WORKTREE_ROOT` and
`MODERNMINECARTS_GRADLE_USER_HOME` override the short temporary locations.
Every run produces a timestamped log and `summary.json` in `qa/results/`, which
is ignored by Git.

## In-game release checklist

Run this after launching a development client for a branch.  Use a fresh
creative world and default Modern Minecarts settings.

1. **Copper-rail propulsion.** Place a redstone block next to a copper rail,
   give a minecart a small push, and confirm that it accelerates.  Repeat with
   each oxidation state that branch supports.
2. **Fabric rail networks.** On Fabric, create both `powered rail -> copper
   rail` and `copper rail -> powered rail` chains.  Power either end and verify
   that every rail's powered state updates and a minecart accelerates across the
   transition.
3. **Furnace minecart on a powered rail.** Link a furnace minecart to another
   cart, load the furnace cart with coal, and power the rail.  It must consume
   fuel, choose the linked-cart direction, and move at the configured furnace
   speed.
4. **Unpowered powered/copper rail.** Turn the rail off while the furnace cart
   is burning.  Fuel continues to count down, but the cart must coast/brake like
   a normal minecart.  With no fuel, it must not consume a new item.  Turn the
   rail on again and confirm linked-cart propulsion resumes.
5. **Normal/off-rail behavior.** An unlit furnace minecart must coast like an
   ordinary minecart; a burning one must not propel itself when it leaves rails.
   On Fabric 26.1 and 26.2 this also verifies that the cart does not crash the
   game off rails.
6. **Configured speed and chains.** Change the furnace and powered-rail speed
   options, restart the world, and confirm a linked train uses the selected cap.
7. **Fabric names (1.21.11).** Hover every Modern Minecarts item in the creative
   menu.  Names such as `Copper Rail` must display, never a translation key or
   registry id.
8. **Porting Lib (Fabric 1.20.1 and 1.21.1).** Repeat the minecart checks with
   the supported Porting Lib version installed; the game must reach the title
   screen and the checks above must still pass.

GameTest is appropriate for the deterministic server-side portions of this
checklist.  Client localisation and third-party-mod compatibility still need a
development-client check because they depend on the client resource and mod
loaders.
