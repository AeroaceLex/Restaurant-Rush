# Restaurant Rush

A Java command-line restaurant management game for SC2002 Object-Oriented Design & Programming. Manage limited staff time across 18 turns: seat customers, take orders, prepare food and serve dishes while balancing revenue and customer satisfaction.

## Team

| Member | Main contribution |
|---|---|
| Lew En Xun | Stage 1 turn engine and testing |
| Tan Soon Kang | Stage 2 customer behaviour and testing |
| Liow Zi Hao Samuel | Stage 3 Host and class diagram |
| Harish Kumaran Solaiyan | Stage 3 Happy Hour and design discussion |
| Benjamin Lee Jun Hua | Stage 3 combos, sequence diagram and README |

## Three development stages

| Stage | Features | Source | Tagged version |
|---|---|---|---|
| 1 | Regular customers, menu, Waiter, Chef, Cashier, satisfaction, abandonment, payment and both endings | [Stage 1](stage-1/) | [stage-1](https://github.com/AeroaceLex/Restaurant-Rush/tree/stage-1) |
| 2 | VIP discount and patience; Critic order preference and patience | [Stage 2](stage-2/) | [stage-2](https://github.com/AeroaceLex/Restaurant-Rush/tree/stage-2) |
| 3 | Host, combo meals and once-per-game Happy Hour | [Stage 3](stage-3/) | [stage-3](https://github.com/AeroaceLex/Restaurant-Rush/tree/stage-3) |

Each stage has its own source and build scripts. The final game is in stage-3/.

## Build and run

Install JDK 11 or newer. The game requires no external libraries or network access.

From the repository root:

Windows Command Prompt:

```bat
cd stage-3
run.bat
```

Windows PowerShell:

```powershell
cd stage-3
.\run.bat
```

macOS, Linux or Git Bash:

```bash
cd stage-3
bash run.sh
```

Use stage-1 or stage-2 instead to run an earlier version. Build scripts compile Java source into out/ before launching the game.

| Option | Purpose |
|---|---|
| --seed N | Set the food-choice seed; default is 2002 |
| --echo | Print input lines in replay transcripts |

## How to play

The base setting has 18 turns, two tables and arrivals on turns 1, 4, 7, 10, 13 and 16. Customers choose their own food.

| Role | Available actions |
|---|---|
| Host — Stage 3 | Seat one queued customer |
| Waiter | Seat one customer, take one order or serve one READY dish |
| Chef | Advance one order by one preparation unit |
| Cashier | Automatically collect at most one eligible payment per turn |

At each staff prompt, enter a displayed task number or a command such as seat C1 T1, order C1, cook C1 or serve C1. Enter 0 or wait to skip.

The Host acts before the Waiter in Stage 3; the Waiter acts before the Chef. A dish completed this turn can be served next turn at the earliest. Customers finish eating on a later turn and pay no earlier than the turn after becoming READY_TO_PAY.

Victory requires all three conditions after turn 18:

- At least four paid customers.
- Revenue of at least $45.00.
- Average satisfaction of paid customers of at least 60.

See each stage's README for detailed rules, menu and design notes.

## Reproducible gameplay runs

Use the default seed 2002. Run these commands from the relevant stage folder.

Windows Command Prompt — Stage 3:

```bat
run.bat --echo < test-runs\stage3-victory.txt
run.bat --echo < test-runs\stage3-defeat.txt
```

In PowerShell, invoke Command Prompt for input redirection:

```powershell
cmd /c "run.bat --echo < test-runs\stage3-victory.txt"
cmd /c "run.bat --echo < test-runs\stage3-defeat.txt"
```

macOS, Linux or Git Bash — Stage 3:

```bash
bash run.sh --echo < test-runs/stage3-victory.txt
bash run.sh --echo < test-runs/stage3-defeat.txt
```

For earlier stages, use their folder and corresponding stage1- or stage2- input filenames.

The committed test notes and transcripts document these expected results; compare fresh runs against them:

| Stage | Run | Result | Paid | Revenue | Average satisfaction |
|---|---|---|---|---|---|
| 1 | stage1-victory.txt | VICTORY | 5 | $69.00 | 74.4 |
| 1 | stage1-defeat.txt | DEFEAT | 0 | $0.00 | N/A |
| 2 | stage2-victory.txt | VICTORY | 4 | $62.10 | 70.3 |
| 2 | stage2-defeat.txt | DEFEAT | 0 | $0.00 | N/A |
| 3 | stage3-victory.txt | VICTORY | 5 | $76.86 | 83.8 |
| 3 | stage3-defeat.txt | DEFEAT | 0 | $0.00 | N/A |

Evidence: [Stage 1 scripts and logs](stage-1/test-runs/), [Stage 2 test notes](stage-2/TEST_NOTES.md), [Stage 3 test notes](stage-3/TEST_NOTES.md).

## Design evolution

### Stage 1 to Stage 2

[Compare tagged versions](https://github.com/AeroaceLex/Restaurant-Rush/compare/stage-1...stage-2)

| File | Change and reason |
|---|---|
| VipCustomer.java | Added VIP patience and discounted pricing through customer overrides |
| CriticCustomer.java | Added Critic patience and a preference for the most expensive menu item |
| Customer.java | Moved shared random food selection into the base customer class |
| RegularCustomer.java | Reuses the inherited food-selection implementation |
| GameConfig.java | Registers VIP and Critic arrivals while preserving six total arrivals |

Customer behaviour is dispatched through customer objects. The controller, restaurant, orders and staff retain the existing service flow. Stage-specific replay scripts provide evidence of gameplay outcomes.

### Stage 2 to Stage 3

[Compare tagged versions](https://github.com/AeroaceLex/Restaurant-Rush/compare/stage-2...stage-3)

| File | Change and reason |
|---|---|
| Host.java | Added a role that seats one customer in addition to the Waiter's task |
| Seating.java | Extracted seating logic shared by Host and Waiter |
| ComboMeal.java | Added a menu-item subtype deriving price and preparation units from two components |
| HappyHour.java | Added activation, duration, discount and recovery policy |
| GameController.java | Registers Host and runs Happy Hour turn-start and turn-end steps |
| Restaurant.java | Applies Happy Hour pricing after customer pricing and restores waiting customers' satisfaction |
| Waiter.java | Uses the shared seating implementation |
| GameConfig.java | Adds combos to the menu |
| Menu.java | Adds item lookup for constructing combos |
| ConsoleUI.java | Displays the staff list, Happy Hour status and activation prompt |

The existing customer subclasses, Order, Chef and Cashier continue to use the same abstractions. ComboMeal works through MenuItem, and the Critic's existing preference selects the most expensive combo without a new customer-type check.

## Report and UML diagrams

Pending final upload:

- Final PDF report with individual reflections.
- Stage 1, Stage 2 and Stage 3 class diagrams.
- Stage 3 sequence diagram covering the VIP and Happy Hour scenario.

Replace this pending list with links to the submitted report and readable diagram exports before submission.

## Recorded presentation

Video link: **Pending final recording/upload.**

The following timestamps are planned in the current report. Verify them against the final recording before submission.

| Segment | Presenter | Planned timestamp |
|---|---|---|
| Launching the game | Lew En Xun | 02:00 |
| Meaningful manager choices and constrained staff decisions | Lew En Xun | 02:00–03:15 |
| READY → SERVED → eating → payment | Lew En Xun | 02:00–03:15 |
| VIP or Critic behaviour | Tan Soon Kang | 03:15–04:15 |
| Host, Happy Hour and combos | Liow Zi Hao Samuel, Harish Kumaran Solaiyan, Benjamin Lee Jun Hua | 04:15–06:00 |
| Victory and defeat | Lew En Xun | 02:00–03:15 |
| Class diagrams and stage comparisons | All members | 06:00–10:00 |
| Selected use case and sequence diagram | Benjamin Lee Jun Hua | 10:00–13:00 |

The approximately 15-minute recording must include a contribution from every member. Ensure the final recording also covers the overview, contributions and learning, and that the link remains accessible to graders.

## Individual AI communication histories

Each member keeps full project-related prompts, replies and follow-up exchanges in chronological order on their own branch.

| Member | Branch | History location |
|---|---|---|
| Lew En Xun | [AeroaceLex-patch-1](https://github.com/AeroaceLex/Restaurant-Rush/tree/AeroaceLex-patch-1) | [stage1-ai-history.md](https://github.com/AeroaceLex/Restaurant-Rush/blob/AeroaceLex-patch-1/stage1-ai-history.md) |
| Tan Soon Kang | [soonkang-patch-1](https://github.com/AeroaceLex/Restaurant-Rush/tree/soonkang-patch-1) | [stage2-ai-history](https://github.com/AeroaceLex/Restaurant-Rush/blob/soonkang-patch-1/stage2-ai-history) |
| Liow Zi Hao Samuel | [goodcodder-patch-1](https://github.com/AeroaceLex/Restaurant-Rush/tree/goodcodder-patch-1) | Pending upload or confirmed file link |
| Harish Kumaran Solaiyan | [harishkumaran1405-patch-1](https://github.com/AeroaceLex/Restaurant-Rush/tree/harishkumaran1405-patch-1) | [ai-history:stage-3:chat-01.md](https://github.com/AeroaceLex/Restaurant-Rush/blob/harishkumaran1405-patch-1/ai-history%3Astage-3%3Achat-01.md) |
| Benjamin Lee Jun Hua | [benjaminwe4-patch-1-1](https://github.com/AeroaceLex/Restaurant-Rush/tree/benjaminwe4-patch-1-1) | [transcript.md](https://github.com/AeroaceLex/Restaurant-Rush/blob/benjaminwe4-patch-1-1/transcript.md) |

The group must check completeness and update these links after any rename. Signed original-work and individual GAI declarations accompany the final submission.

## Submission checklist

- [ ] All three final stage versions compile and run independently.
- [ ] Victory and defeat replays match documented results.
- [ ] Report and diagrams match the submitted source.
- [ ] All five individual reflections are complete.
- [ ] Full AI histories are linked for every member.
- [ ] Final video link and actual timestamps are included.
- [ ] Signed course declarations are included.
- [ ] Final PDF, source, README and video/link are packaged in the required group archive.
- [ ] All submission links are accessible to graders.
