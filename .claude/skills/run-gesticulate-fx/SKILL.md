---
name: run-gesticulate-fx
description: Build, launch, screenshot, and verify the GesticulateFX desktop app (JavaFX/Maven). Use when asked to run, start, launch, smoke-test, or screenshot GesticulateFX, or to confirm the app boots (e.g. after Dagger/DI changes) on macOS.
---

# Run GesticulateFX

GesticulateFX is a JavaFX 27 desktop drawing app, built with Maven and wired
with Dagger 2. It is launched via the `exec-maven-plugin` (`mvn exec:exec`),
which runs `net.perspective.draw.Gesticulate` against the JavaFX module path.

The agent path is the **driver script**
[`.claude/skills/run-gesticulate-fx/smoke.sh`](.claude/skills/run-gesticulate-fx/smoke.sh):
it compiles, launches the app, waits for the window, screenshots it, checks the
startup log for exceptions, and shuts the app down. Launching is necessary
because the most significant failure mode here — a Dagger dependency cycle — is
a **runtime** error: it compiles cleanly and fails only at startup.

> Paths below are relative to the repo root (`<root>/`). The driver lives at
> `.claude/skills/run-gesticulate-fx/`. **macOS only** — the screenshot is
> captured from the live display via `screencapture` + System Events, so a
> logged-in GUI session is required.

## Prerequisites

- **macOS** with a GUI session (Apple Silicon — the resolved JavaFX jars are
  `mac-aarch64`).
- **JDK 27** at `$HOME/Applications/jdk-27.jdk` (exported by the repo's
  `setpath`).
- **JavaFX SDK 27** at `$HOME/Applications/javafx-sdk` (`$JAVAFX_HOME`; matches
  `nbactions.xml`).
- **Maven** on `PATH` (`mvn`). Dependencies resolve from `~/.m2` — runs offline
  (`-o`).

No additional packages are required. The driver assumes a configured macOS
workstation rather than a headless container, and installs nothing.

## Run (agent path) — preferred

```bash
.claude/skills/run-gesticulate-fx/smoke.sh
# optional: pass an output path for the screenshot
.claude/skills/run-gesticulate-fx/smoke.sh /tmp/gesticulate.png
```

What it does, and what success looks like:

```
==> Compiling (offline)…
==> Launching GesticulateFX…
==> Window up (pid NNNNN).
==> Screenshot saved: <root>/target/gesticulate-fx-smoke.png  (window 1070x990 @ ...)
==> OK: app launched, window rendered, no startup errors.
```

Then **look at the screenshot** (`target/gesticulate-fx-smoke.png` by default):
a healthy launch shows the "GesticulateFX" title bar, the top toolbar (line
styles, Fill/Color swatches, font family/size, **b** *i* <u>u</u>), and the
left tool rail (new/open/save, select, rotate, line, circle, rect, triangle,
hexagon, star, spiral, Text, Align, Grid) over a dark canvas. A blank frame or
a non-zero exit = failure; read the log the script dumps.

The script exits non-zero (and prints the launch log) if the window never
appears or if the startup log contains `Exception`/`Error`/`DoubleCheck`.

## Run (human path)

```bash
source ./setpath                                  # JAVA_HOME + PATH
export JAVAFX_HOME=$HOME/Applications/javafx-sdk
mvn -o compile        # exec:exec does NOT compile — do this first
mvn -o exec:exec      # opens the window; Cmd-Q or close the window to quit
```

This blocks until you close the window; use it for hands-on interaction, not
for automated verification.

## Gotchas

- **`mvn exec:exec` does not compile.** It only launches. Run `mvn compile`
  first, or stale or missing classes are launched. The driver always compiles.
- **A Dagger cycle compiles fine and only fails at runtime.** It surfaces as a
  stack trace through `DaggerDrawAppComponent…inject…` / `DoubleCheck` at
  startup. The smoke script greps the log for these — a green `mvn compile` is
  *not* proof the app boots. Break cycles into `@Singleton` classes with
  `Provider<T>` (lazy) injection.
- **`screencapture -R` grabs screen pixels by region, not a window.** Anything
  overlapping the app's rectangle is captured instead of the app — an editor
  window left on top yields a screenshot of the editor. The driver calls
  `set frontmost … to true` before capturing; leave the window uncovered for
  the duration of the run.
- **The background launcher "completes" while the app is still running.** When
  you launch in the background, the wrapper returns immediately; the JVM lives
  on. Check liveness with `pgrep -f net.perspective.draw.Gesticulate`, not the
  launcher's exit.
- **`JAVAFX_HOME` is not in `setpath`.** `setpath` only sets `JAVA_HOME`/`PATH`;
  the JavaFX module path must be exported separately (the driver defaults it to
  `$HOME/Applications/javafx-sdk`).

## Troubleshooting

| Symptom | Fix |
|---|---|
| `JAVAFX_HOME not found` | `export JAVAFX_HOME=$HOME/Applications/javafx-sdk` (or install the JavaFX 27 SDK there). |
| `JAVA_HOME not found` | Install JDK 27 at `$HOME/Applications/jdk-27.jdk`, or edit `setpath`. |
| Crash through `DaggerDrawAppComponent` / `DoubleCheck` at startup | Dagger dependency cycle — make the offending `@Inject` field a `Provider<T>`. |
| `Error: JavaFX runtime components are missing` | `--module-path $JAVAFX_HOME/lib` not applied; use the driver or the human-path command verbatim. |
| Screenshot shows the wrong window | Another window overlapped the capture region; re-run without covering the app (driver re-fronts it automatically). |
| `No GesticulateFX window after 60s` | Read the dumped log — usually a startup exception or a missing module path. |
