#!/bin/sh
# Replays the scripted runs of one stage (default: the stage this commit implements),
# saves each transcript as a .log next to its script and prints each final result.
#   ./test-runs/run-all.sh       runs test-runs/stage3-*.txt
#   ./test-runs/run-all.sh 1     runs the Stage 1 scripts (check out tag stage-1 first)
set -e
cd "$(dirname "$0")/.."
STAGE="${1:-3}"
rm -rf out
javac -d out $(find src -name "*.java")
for script in test-runs/stage"$STAGE"-*.txt; do
    log="${script%.txt}.log"
    java -cp out restaurant.app.Main --echo < "$script" > "$log"
    printf '%-40s %s\n' "$script" "$(grep 'GAME OVER' "$log")"
done
