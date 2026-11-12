# Test notes: reproducible runs (Stage 3)

Every run uses the **base setting** (18 turns, 2 tables, one Host, Waiter, Chef and Cashier,
arrivals on turns 1, 4, 7, 10, 13, 16) and the **default seed 2002**. Each run is a plain input file
in `test-runs/` with one line per prompt the game shows. A staff member with no possible task is
skipped automatically, so it gets no line.

Replay one run from this folder (the transcript shows each typed line because of `--echo`):

```bash
bash run.sh --echo < test-runs/stage3-victory.txt
bash run.sh --echo < test-runs/stage3-defeat.txt
```

```bat
run.bat --echo < test-runs\\stage3-victory.txt
run.bat --echo < test-runs\\stage3-defeat.txt
```

Replay both runs and save the transcripts as `test-runs/\*.log`:

```bash
bash test-runs/run-all.sh
```

This folder holds only the Stage 3 scripts, because each stage changes who arrives and what is on
the menu. The earlier scripts and their notes are in [`../stage-1`](../stage-1/TEST_NOTES.md) and
[`../stage-2`](../stage-2/TEST_NOTES.md).

|Script|Result|Paid|Revenue|Average satisfaction|Unhappy departures|
|-|-|-|-|-|-|
|`stage3-victory.txt`|VICTORY|5|$76.86|83.8|0|
|`stage3-defeat.txt`|DEFEAT|0|$0.00|N/A|2|

## Setting

Arrivals are the same as Stage 2: C1 Regular (turn 1), C2 VIP (turn 4), C3 Critic (turn 7), C4,
C5, C6 Regular (turns 10, 13, 16).

Menu: Salad, Burger, Pasta, **Salad + Burger Combo** ($21.60, 2 units) and **Salad + Pasta Combo**
($24.30, 3 units). The random draws now pick from five entries, so the choices differ from the
earlier stages even with the same seed. With seed 2002 the customers order: C1 Salad, C2 (VIP)
Pasta, C3 (Critic) Salad + Pasta Combo (now the most expensive item), C4 Salad, C5 Salad + Burger
Combo, C6 Salad + Pasta Combo.

Each turn starts with the Happy Hour prompt (`y`/`n`) until Happy Hour has been used. Then the
Host, Waiter and Chef are asked in that order.

## Victory: `test-runs/stage3-victory.txt`

Strategy: the Host seats each arrival on their arrival turn, so the Waiter can take the order in
the same turn and the Chef can start immediately. Happy Hour is switched on at turn 4, as the VIP
arrives.

|Turn|Happy Hour|Host|Waiter|Chef|Automatic / notes|
|-|-|-|-|-|-|
|1|n|seat C1 T1|order C1 (Salad)|cook C1 READY|Seat, order and cook in one turn|
|2|n|-|serve C1|-|C1 served at 92|
|3|n|-|-|-|C1 READY\_TO\_PAY|
|4|**y** (2 turns left)|seat C2 T2|order C2 (Pasta): **$18 x 0.9 x 0.8 = $12.96**|cook C2 (1/2)|VIP discount first, then Happy Hour. C1 pays $9.00 (92)|
|5|active, 1 turn left: C2 recovers to 100|-|-|cook C2 (2/2) READY||
|6|used|-|serve C2|-|C2 served at 95|
|7||seat C3 T1|order C3 (Salad + Pasta Combo, $24.30)|cook C3 (1/3)|Critic arrives. C2 READY\_TO\_PAY|
|8||-|-|cook C3 (2/3)|C2 pays $12.96 (95)|
|9||-|-|cook C3 (3/3) READY||
|10||seat C4 T2|serve C3|-|Host and Waiter both work this turn. Critic served at 64|
|11||-|order C4 (Salad)|cook C4 READY|C3 READY\_TO\_PAY|
|12||-|serve C4|-|C3 pays $24.30 (64)|
|13||seat C5 T1|order C5 (Salad + Burger Combo, $21.60)|cook C5 (1/2)|C4 READY\_TO\_PAY|
|14||-|-|cook C5 (2/2) READY|C4 pays $9.00 (84)|
|15||-|serve C5|-||
|16||seat C6 T2|order C6 (Salad + Pasta Combo)|cook C6 (1/3)|C5 READY\_TO\_PAY|
|17||-|-|cook C6 (2/3)|C5 pays $21.60 (84)|
|18||-|-|cook C6 (3/3) READY|C6 cannot finish in time (intended)|

**Result: VICTORY.** 5 paid, revenue $76.86, average satisfaction 83.8 (92 + 95 + 64 + 84 + 84).

What this run shows:

* **Host:** on turns 1, 4, 7, 13 and 16 the Host seats the arrival and the Waiter takes the order
in the same turn. On turn 10 the Host seats C4 while the Waiter serves C3.
* **Combo meals:** C3, C5 and C6 order combos, which are cooked, served and billed like any other
item. The Critic's rule picks the Salad + Pasta Combo because it is now the most expensive entry.
* **Happy Hour pricing:** the VIP discount is applied first, then Happy Hour, each rounded to the
cent. The bill is locked when the order is taken: C2 pays $12.96 on turn 8, after Happy Hour has
ended. C3's order on turn 7 was taken after Happy Hour ended, so it is full price.
* **Happy Hour recovery:** C2 is still waiting for food on turn 5 and recovers to 100.
* **Once per game:** after turn 5 the status shows "used" and the prompt is no longer offered.

### Happy Hour timing

Keeping every staff decision above the same and changing only the turn on which Happy Hour
starts gives:

|Happy Hour starts|Revenue|Average satisfaction|
|-|-|-|
|Never|$80.10|82.8|
|Turn 4 (the script)|$76.86|83.8|
|Turn 7|$75.24|85.2|
|Turn 9|$80.10|88.8|
|Turn 16, 17 or 18|$80.10|82.8|

Only the turn-4 row is a committed script; the others are the same input with the `y` moved.

## Defeat: `test-runs/stage3-defeat.txt`

The manager declines Happy Hour every turn and answers `wait` for the Host and the Waiter. Nobody
is seated: C1 (Regular) walks out on turn 13 and C3 (Critic) on turn 15, and the rest are still
queuing at the end.

**Result: DEFEAT.** 0 paid, revenue $0.00, average satisfaction N/A, 2 unhappy departures.

## Robustness check

Sixty games were played with 400 random input lines each (valid and invalid commands, unknown
ids, out-of-range numbers, empty lines, nonsense words, random `y`/`n`), each with a different
seed. Every game completed all 18 turns with no exception. Rejected input was explained and
asked again without using up the staff task.

