# Test notes: reproducible runs

Every run uses the **base setting** (18 turns, 2 tables, one Waiter, Chef and Cashier, arrivals on
turns 1, 4, 7, 10, 13, 16) and the **default seed 2002**. Each run is a plain input file in
`test-runs/` with one line per prompt the game shows. A staff member with no possible task is
skipped automatically, so it gets no line.

Replay one run (the transcript shows each typed line because of `--echo`):

```bash
./run.sh --echo < test-runs/stage1-victory.txt
```

```bat
run.bat --echo < test-runs\stage1-victory.txt
```

Replay every run of the current stage and save transcripts as `test-runs/*.log`:

```bash
./test-runs/run-all.sh
```

Each stage's scripts belong to that stage, because later stages change who arrives and what is on
the menu. The earlier stages' scripts are in the `stage-1` and `stage-2` folders next to this one; the
notes for them are kept below for reference.

With seed 2002 the random food choices are: C1 Pasta, C2 Salad, C3 Salad, C4 Pasta, C5 Burger,
C6 Salad. From Stage 2, C3 is a Critic and ignores the random choice. Stage 3 adds two combos to
the menu, so the random draws pick from five entries and the choices change (see Stage 3 below).

## Stage 1

### Victory: `test-runs/stage1-victory.txt`

Strategy: give each customer full attention in turn and serve a READY dish before seating a new
arrival.

| Turn | Waiter | Chef | Automatic | Notes |
|---|---|---|---|---|
| 1 | seat C1 T1 | - | | |
| 2 | ~~serve C1~~ then order C1 (Pasta) | cook C1 (1/2) | | `serve C1` is rejected: no order yet. The task is not used up. |
| 3 | - | cook C1 (2/2) READY | | |
| 4 | ~~dance~~ then serve C1 | - | | C2 arrives. Unknown command rejected. Serving C1 is chosen over seating C2 (a constrained choice). |
| 5 | seat C2 T2 | - | C1 READY_TO_PAY | |
| 6 | order C2 (Salad) | cook C2 READY | Cashier: C1 pays $18.00, satisfaction 76 | Matches the brief's Section 3.3 example. |
| 7 | serve C2 | - | | C3 arrives |
| 8 | seat C3 T1 | - | C2 READY_TO_PAY | |
| 9 | order C3 (Salad) | cook C3 READY | C2 pays $9.00 (76) | |
| 10 | serve C3 | - | | C4 arrives |
| 11 | seat C4 T2 | - | C3 READY_TO_PAY | |
| 12 | order C4 (Pasta) | cook C4 (1/2) | C3 pays $9.00 (76) | |
| 13 | seat C5 T1 | cook C4 (2/2) READY | | C5 arrives |
| 14 | serve C4 | - | | Serve first: C4 keeps 68 instead of dropping to 60 |
| 15 | order C5 (Burger) | cook C5 READY | C4 READY_TO_PAY | |
| 16 | serve C5 | - | C4 pays $18.00 (68) | C6 arrives; both tables are busy |
| 17 | seat C6 T2 | - | C5 READY_TO_PAY | |
| 18 | order C6 (Salad) | cook C6 | C5 pays $15.00 (76) | C6 cannot finish in time (intended) |

**Result: VICTORY.** 5 paid, revenue $69.00, average satisfaction 74.4.

### Defeat: `test-runs/stage1-defeat.txt`

The manager answers `wait` at every prompt. Nobody is seated: C1 walks out on turn 13 and C2 on
turn 16, and the rest are still queuing at the end.

**Result: DEFEAT.** 0 paid, revenue $0.00, average satisfaction N/A, 2 unhappy departures.

### How one choice changes the result: `stage1-greedy-victory.txt` vs `stage1-greedy-defeat.txt`

The greedy policy "always pick option 1" (seat first, then take orders, then serve) is a narrow
**victory** with these seed values: 4 paid, $54.00, average 64.0. The second file is the same, except
that its 5th line is `0`, so the Waiter idles once on turn 4, when C1's Pasta is READY and C2 has
just arrived. C1 is served later, the average drops to 56.0 and the game is a **defeat**, with the
same revenue.

## Stage 2

Arrivals: C1 Regular (turn 1), **C2 VIP** (turn 4), **C3 Critic** (turn 7), C4 Regular (turn 10),
C5 Regular (turn 13), C6 Regular (turn 16). The VIP loses 5 per waiting turn and gets 10% off. The
Critic loses 12 per waiting turn and always orders the most expensive item (Pasta).

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

### Defeat: `test-runs/stage2-defeat.txt`

The manager answers `wait` at every prompt. Customer behaviour differs visibly:

- The **Critic** C3 arrives on turn 7, three turns *after* the VIP, but walks out first, on
  turn 15 (9 turns x 12).
- The **VIP** C2 is still queuing at the end of the game with satisfaction 25 (15 turns x 5).
- The Regular C1 walks out on turn 13.

**Result: DEFEAT.** 0 paid, revenue $0.00, average satisfaction N/A, 2 unhappy departures.

## Stage 3

Menu: Salad, Burger, Pasta, **Salad + Burger Combo** ($21.60, 2 units), **Salad + Pasta Combo**
($24.30, 3 units). With seed 2002 the customers order: C1 Salad, C2 (VIP) Pasta, C3 (Critic) Salad
+ Pasta Combo (now the most expensive item), C4 Salad, C5 Salad + Burger Combo, C6 Salad + Pasta
Combo.

Each turn starts with the Happy Hour prompt (`y`/`n`) until Happy Hour has been used. Then the
Host, Waiter and Chef are asked in that order.

### Victory: `test-runs/stage3-victory.txt`

Strategy: the Host seats each arrival on their arrival turn, so the Waiter can take the order in
the same turn and the Chef can start immediately. Happy Hour is switched on at turn 4, as the VIP
arrives.

| Turn | Happy Hour | Host | Waiter | Chef | Automatic / notes |
|---|---|---|---|---|---|
| 1 | n | seat C1 T1 | order C1 (Salad) | cook C1 READY | Seat, order and cook in one turn |
| 2 | n | - | serve C1 | - | C1 served at 92 |
| 3 | n | - | - | - | C1 READY_TO_PAY |
| 4 | **y** (2 turns left) | seat C2 T2 | order C2 (Pasta): **$18 x 0.9 x 0.8 = $12.96** | cook C2 (1/2) | VIP discount first, then Happy Hour. C1 pays $9.00 (92) |
| 5 | active, 1 turn left: C2 recovers to 100 | - | - | cook C2 (2/2) READY | |
| 6 | used | - | serve C2 | - | C2 served at 95 |
| 7 | | seat C3 T1 | order C3 (Salad + Pasta Combo, $24.30) | cook C3 (1/3) | Critic arrives. C2 READY_TO_PAY |
| 8 | | - | - | cook C3 (2/3) | C2 pays $12.96 (95) |
| 9 | | - | - | cook C3 (3/3) READY | |
| 10 | | seat C4 T2 | serve C3 | - | Host and Waiter both work this turn. Critic served at 64 |
| 11 | | - | order C4 (Salad) | cook C4 READY | C3 READY_TO_PAY |
| 12 | | - | serve C4 | - | C3 pays $24.30 (64) |
| 13 | | seat C5 T1 | order C5 (Salad + Burger Combo, $21.60) | cook C5 (1/2) | C4 READY_TO_PAY |
| 14 | | - | - | cook C5 (2/2) READY | C4 pays $9.00 (84) |
| 15 | | - | serve C5 | - | |
| 16 | | seat C6 T2 | order C6 (Salad + Pasta Combo) | cook C6 (1/3) | C5 READY_TO_PAY |
| 17 | | - | - | cook C6 (2/3) | C5 pays $21.60 (84) |
| 18 | | - | - | cook C6 (3/3) READY | C6 cannot finish in time (intended) |

**Result: VICTORY.** 5 paid, revenue $76.86, average satisfaction 83.8 (92 + 95 + 64 + 84 + 84).

The Happy Hour bill is locked when the order is taken: C2 pays $12.96 on turn 8, after Happy Hour
has ended. C3's order on turn 7 was taken after Happy Hour ended, so it is full price.

### Defeat: `test-runs/stage3-defeat.txt`

The manager declines Happy Hour and answers `wait` for the Host and the Waiter every turn.
C1 (Regular) walks out on turn 13 and C3 (Critic) on turn 15.

**Result: DEFEAT.** 0 paid, revenue $0.00, average satisfaction N/A, 2 unhappy departures.

## Robustness check

Sixty games were played with 400 random input lines each (valid and invalid commands, unknown
ids, out-of-range numbers, empty lines, nonsense words, random `y`/`n`), each with a different
seed. Every game completed all 18 turns with no exception. Rejected input was explained and
asked again without using up the staff task.
