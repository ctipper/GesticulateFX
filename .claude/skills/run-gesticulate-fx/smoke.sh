#!/usr/bin/env bash
#
# Launch GesticulateFX, wait for its window, screenshot it, then shut down.
# This is the agent-facing driver: it proves the app boots through Dagger DI
# (a runtime-only failure mode — see Gotchas in SKILL.md) and renders a window.
#
# Usage:  .claude/skills/run-gesticulate-fx/smoke.sh [output.png]
# Default output: ./target/gesticulate-fx-smoke.png
#
# macOS only. Requires a logged-in GUI session (the window is drawn on the
# real display; screencapture grabs the window region via System Events).

set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../../.." && pwd)"   # <root>/.claude/skills/run-gesticulate-fx -> <root>
cd "$ROOT"

OUT="${1:-$ROOT/target/gesticulate-fx-smoke.png}"
LOG="$(mktemp -t gesticulate-fx-smoke)"
MAIN_CLASS="net.perspective.draw.Gesticulate"

# --- Toolchain --------------------------------------------------------------
# setpath (repo root) exports JAVA_HOME and prepends it to PATH.
# JAVAFX_HOME matches the nbactions.xml run action.
# shellcheck disable=SC1091
source "$ROOT/setpath"
export JAVAFX_HOME="${JAVAFX_HOME:-$HOME/Applications/javafx-sdk}"

[ -d "$JAVA_HOME" ]   || { echo "JAVA_HOME not found: $JAVA_HOME"; exit 1; }
[ -d "$JAVAFX_HOME" ] || { echo "JAVAFX_HOME not found: $JAVAFX_HOME"; exit 1; }

cleanup() {
  pkill -f "$MAIN_CLASS" 2>/dev/null || true
  [ -n "${MVN_PID:-}" ] && kill "$MVN_PID" 2>/dev/null || true
}
trap cleanup EXIT

# --- Build ------------------------------------------------------------------
# exec:exec does NOT compile; ensure target/classes is current. Also
# regenerates the Dagger component from annotations.
echo "==> Compiling (offline)…"
mvn -o -q compile

# --- Launch -----------------------------------------------------------------
echo "==> Launching GesticulateFX…"
mvn -o exec:exec > "$LOG" 2>&1 &
MVN_PID=$!

# --- Wait for the window (or an early crash) --------------------------------
PID=""
for _ in $(seq 1 60); do
  if ! kill -0 "$MVN_PID" 2>/dev/null; then
    echo "!! Launch process exited before a window appeared. Log:"
    cat "$LOG"
    exit 1
  fi
  PID="$(pgrep -f "$MAIN_CLASS" | head -1 || true)"
  if [ -n "$PID" ] && \
     osascript -e "tell application \"System Events\" to tell (first process whose unix id is $PID) to get window 1" >/dev/null 2>&1; then
    break
  fi
  PID=""
  sleep 1
done

if [ -z "$PID" ]; then
  echo "!! No GesticulateFX window after 60s. Log:"
  cat "$LOG"
  exit 1
fi
echo "==> Window up (pid $PID)."

# --- Screenshot the window region -------------------------------------------
osascript -e "tell application \"System Events\" to set frontmost of (first process whose unix id is $PID) to true" >/dev/null 2>&1 || true
sleep 1
bounds="$(osascript -e "tell application \"System Events\" to tell (first process whose unix id is $PID) to get {position, size} of window 1")"
x="$(echo "$bounds" | awk -F', *' '{print $1}')"
y="$(echo "$bounds" | awk -F', *' '{print $2}')"
w="$(echo "$bounds" | awk -F', *' '{print $3}')"
h="$(echo "$bounds" | awk -F', *' '{print $4}')"
mkdir -p "$(dirname "$OUT")"
screencapture -x -R"${x},${y},${w},${h}" "$OUT"
echo "==> Screenshot saved: $OUT  (window ${w}x${h} @ ${x},${y})"

# --- Report any startup exceptions ------------------------------------------
if grep -Eiq 'Exception|Error|DoubleCheck' "$LOG"; then
  echo "!! Startup log contains errors:"
  grep -Ei 'Exception|Error|DoubleCheck' "$LOG" | head -20
  exit 1
fi

echo "==> OK: app launched, window rendered, no startup errors."
