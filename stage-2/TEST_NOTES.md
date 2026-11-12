# Test notes: reproducible runs (Stage 2)

Every run uses the **base setting** (18 turns, 2 tables, one Waiter, Chef and Cashier, arrivals on
turns 1, 4, 7, 10, 13, 16) and the **default seed 2002**. Each run is a plain input file in
`test-runs/` with one line per prompt the game shows. A staff member with no possible task is
skipped automatically, so it gets no line.

Replay one run from this folder (the transcript shows each typed line because of `--echo`):

```bash
bash run.sh --echo < test-runs/stage2-victory.txt
bash run.sh --echo < test-runs/stage2-defeat.txt
```

```bat
run.bat --echo < test-runs\\stage2-victory.txt
run.bat --echo < test-runs\\stage2-defeat.txt
```

Replay both runs and save the transcripts as `test-runs/\*.log`:

```bash
bash test-runs/run-all.sh
```

This folder holds only the Stage 2 scripts, because each stage changes who arrives and what is on
the menu. The Stage 1 scripts and their notes are in [`../stage-1`](../stage-1/TEST_NOTES.md).

|Script|Result|Paid|Revenue|Average satisfaction|Unhappy departures|
|-|-|-|-|-|-|
|`stage2-victory.txt`|VICTORY|4|$62.10|70.3|0|
|`stage2-defeat.txt`|DEFEAT|0|$0.00|N/A|2|

## Setting

Arrivals: C1 Regular (turn 1), **C2 VIP** (turn 4), **C3 Critic** (turn 7), C4 Regular (turn 10),
C5 Regular (turn 13), C6 Regular (turn 16). The VIP loses 5 per waiting turn and gets 10% off. The
Critic loses 12 per waiting turn and always orders the most expensive item (Pasta).

With seed 2002 the random food choices are C1 Pasta, C2 Salad, C3 Salad, C4 Pasta, C5 Burger,
C6 Salad. C3 is a Critic and ignores the random choice, so C3 orders Pasta.

### Victory: `test-runs/stage2-victory.txt`

Strategy: the same "finish one customer before starting the next" approach, but the Critic's
Pasta gets the Chef's full attention on turns 9-10, because every waiting turn costs 12.

| Turn | Waiter | Chef | Automatic | Notes |
|---|---|---|---|---|
| 1 | seat C1 T1 | - | | |
| 2 | order C1 (Pasta) | cook C1 (1/2) | | |
| 3 | - | cook C1 (2/2) READY | | |
| 4 | serve C1 | - | | VIP C2 arrives |
| 5 | seat C2 T2 | - | C1 READY_TO_PAY | |
| 6 | order C2 (Salad, **$8.10** after VIP 10% off) | cook C2 READY | C1 pays $18.00 (76) | |
| 7 | serve C2 | - | | Critic C3 arrives |
| 8 | seat C3 T1 | - | C2 READY_TO_PAY | |
| 9 | order C3 (**Pasta**, the Critic's preference) | cook C3 (1/2) | C2 pays $8.10 (85) | VIP lost only 5 per turn |
| 10 | seat C4 T2 | cook C3 (2/2) READY | | C4 arrives |
| 11 | serve C3 | - | | Critic served at 52: four waiting turns x 12 |
| 12 | order C4 (Pasta) | cook C4 (1/2) | C3 READY_TO_PAY | |
| 13 | - (no free table, nothing READY) | cook C4 (2/2) READY | C3 pays $18.00 (52) | C5 arrives |
| 14 | serve C4 | - | | Serving C4 beats seating C5 |
| 15 | seat C5 T1 | - | C4 READY_TO_PAY | |
| 16 | order C5 (Burger) | cook C5 READY | C4 pays $18.00 (68) | C6 arrives |
| 17 | serve C5 | - | | C5 would pay on turn 19, too late |
| 18 | seat C6 T2 | - | C5 READY_TO_PAY | |

**Result: VICTORY.** 4 paid, revenue $62.10, average satisfaction 70.3 (76 + 85 + 52 + 68).

What this run shows:

* **VIP discount:** C2's Salad is billed at $8.10 instead of $9.00, and the bill is locked when the
order is taken.
* **VIP patience:** C2 pays with 85 after three waiting turns (3 x 5), where a Regular would have 76.
* **Critic preference:** C3 orders Pasta although the seeded random choice for C3 is Salad.
* **Critic patience:** C3 is served at 52 after four waiting turns (4 x 12).


### Defeat: `test-runs/stage2-defeat.txt`

The manager answers `wait` at every prompt. Customer behaviour differs visibly:

- The **Critic** C3 arrives on turn 7, three turns *after* the VIP, but walks out first, on
  turn 15 (9 turns x 12).
- The **VIP** C2 is still queuing at the end of the game with satisfaction 25 (15 turns x 5).
- The Regular C1 walks out on turn 13.

**Result: DEFEAT.** 0 paid, revenue $0.00, average satisfaction N/A, 2 unhappy departures.
