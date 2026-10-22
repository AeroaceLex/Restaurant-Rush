#!/bin/sh
# Replays every scripted run, saves each transcript as a .log next to its script
# and prints the final result line of each.
set -e
cd "$(dirname "$0")/.."
rm -rf out
javac -d out $(find src -name "*.java")
for script in test-runs/*.txt; do
    log="${script%.txt}.log"
    java -cp out restaurant.app.Main --echo < "$script" > "$log"
    printf '%-40s %s\n' "$script" "$(grep 'GAME OVER' "$log")"
done
