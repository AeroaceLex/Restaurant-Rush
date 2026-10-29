#!/bin/sh
# Compiles the game into ./out and starts it. Extra arguments go to the game,
# e.g.  ./run.sh --seed 2002   or   ./run.sh --echo < test-runs/stage1-victory.txt
set -e
cd "$(dirname "$0")"
rm -rf out
javac -d out $(find src -name "*.java")
java -cp out restaurant.app.Main "$@"
