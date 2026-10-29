# Restaurant Rush

A turn-based restaurant management game for the command line, written in Java for the SC2002
group assignment. You are the manager. Each turn you decide what the Host, Waiter and Chef do, and
whether to start Happy Hour, and you try to reach the targets by the end of turn 18.

- **Repository:** `<add the GitHub URL here>`
- **This version:** Stage 3, new role and menu policy (tag `stage-3`)

## Build and run

You need JDK 11 or newer (tested with JDK 16). No libraries and no network access are needed.

```bash
./run.sh                 # macOS / Linux / Git Bash
```

```bat
run.bat                  :: Windows cmd / PowerShell
```

Or compile and run by hand:

```bash
javac -d out $(find src -name "*.java")
java -cp out restaurant.app.Main
```

Options:

| Option | Meaning |
|---|---|
| `--seed N` | Seed for customers' food choices (default `2002`). The same seed always gives the same choices. |
| `--echo` | Print every line read from input. Useful when input is piped from a script. |

To run an earlier stage, check out its tag (`git checkout stage-1`) and build the same way.

## How to play

At the start of each turn the game shows the turn number, revenue, the average satisfaction of
paid customers, the waiting queue, every occupied table with that customer's satisfaction and
order status, the tasks each staff member can do, and the Happy Hour status. While Happy Hour is
unused, the game asks whether to start it (`y`/`n`). Then it asks for one task per staff member,
**Host, then Waiter, then Chef**. Type the task's number, or type the command:

| Staff | Commands |
|---|---|
| Host | `seat C1 T2` (or `seat C1` for the first free table), `wait` |
| Waiter | `seat C1 T2` / `seat C1`, `order C1`, `serve C1`, `wait` |
| Chef | `cook C1` (one preparation unit on C1's order), `wait` |
| Cashier | Automatic: charges one READY_TO_PAY customer per turn, oldest first |

`0` or `wait` skips a task. If a task is not allowed, the game explains why and asks again; the
staff member's task is not used up. A staff member with nothing they could do waits automatically.

**Goal (base setting):** after turn 18, at least 4 paid customers AND revenue at least $45.00 AND
average satisfaction of paid customers at least 60.

## Rules as implemented

The turn order follows Section 3 of the brief:

1. **Arrivals.** The scheduled customer joins the queue with satisfaction 100, *before* the state is
   displayed, so the manager can see them.
   **Happy Hour.** After the state is shown, the manager may start Happy Hour if it has not been
   used. While it is active, everyone still waiting for a table or for food recovers 15 satisfaction
   (maximum 100), at the start of each of its two turns.
2. **Manager choices.** Host: seat one queued customer, or wait. Waiter: seat, take an order,
   serve a READY dish, or wait. Chef: one preparation unit, or wait. The Waiter may take the order
   of a customer the Host seated this turn, and the Chef may cook an order the Waiter took this turn.
3. **Kitchen and serving.** The Waiter acts before the Chef, so a dish finished this turn can be
   served next turn at the earliest.
4. **Eating and payment.** The Cashier charges first: the oldest customer who became READY_TO_PAY
   on an earlier turn. Then customers served on an earlier turn finish eating and become
   READY_TO_PAY. Payment adds the bill to revenue, records satisfaction and frees the table.
5. **Waiting and abandonment.** Every customer who is queuing, seated without food, or waiting for
   food loses satisfaction at their type's rate (Regular 8, VIP 5, Critic 12; minimum 0). This
   includes the turn they arrive (the brief's Section 3.3 example ends turn 1 at 92). At 0 they
   leave: any unfinished order is cancelled and the table freed.
6. **Result.** All 18 turns are always played. Then the game reports Victory or Defeat.

**Money** is stored as integer cents and shown as dollars with two decimals. Every percentage is
rounded half-up to the nearest cent at the step where it is applied.

## Customers

| Customer | Satisfaction loss per waiting turn | Bill | Food choice |
|---|---|---|---|
| Regular | 8 | Full price | Uniformly random from the menu |
| VIP | 5 | 10% off | Uniformly random from the menu |
| Critic | 12 | Full price | **Always the most expensive item on the menu.** A critic reviews the kitchen's showpiece. With the Stage 2 menu this is Pasta; with the Stage 3 menu it is the Salad + Pasta Combo. |

The base setting's arrivals are C1 Regular (turn 1), **C2 VIP** (turn 4), **C3 Critic** (turn 7), and
C4, C5, C6 Regular (turns 10, 13, 16). All kinds follow the same order and service flow.

**Food choice rule.** Customers choose their own food; the manager never does. Random choices
come from a source seeded with the game seed and the customer's number, so they are the same
whatever order the manager takes orders in. The choice is shown when the order is taken.

## Menu, combos and Happy Hour

| Item | Price | Preparation units |
|---|---|---|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Burger Combo | ($9 + $15) x 0.9 = $21.60 | 1 + 1 = 2 |
| Salad + Pasta Combo | ($9 + $18) x 0.9 = $24.30 | 1 + 2 = 3 |

**Combo choice rule.** A combo is an ordinary entry on the menu. Regular and VIP customers pick
uniformly from all five entries, so each customer has a 2-in-5 chance of choosing a combo. The
Critic's rule picks the Salad + Pasta Combo because it is the most expensive entry. With seed 2002:
C1 Salad, C2 (VIP) Pasta, C3 (Critic) Salad + Pasta Combo, C4 Salad, C5 Salad + Burger Combo,
C6 Salad + Pasta Combo.

**Happy Hour** can be started once per game, at the start of a turn, and lasts for that turn and
the next. The state display shows how many turns are left. Orders *taken* while it is active get
20% off, applied after the customer's own discount. For example, a VIP's Burger costs
$15 x 0.9 x 0.8 = $10.80. The bill is locked when the order is taken, so Happy Hour never changes
an existing order, and customers who have left are unaffected.

## Design overview

The code follows the Entity-Control-Boundary split used in SC2002:

| Package | Class | Responsibility |
|---|---|---|
| `app` | `Main` | Reads command-line options and starts the game. |
| `boundary` | `ConsoleUI` | Displays state and reads the manager's input. It contains no game rules. |
| `control` | `GameController` | Runs the turn steps in order and passes each decision to the right staff member or to Happy Hour. |
| | `GameConfig` | The base setting: turns, tables, seed, arrivals, targets, menu (with combos). |
| | `ArrivalSchedule` | Which kind of customer arrives on which turn (factories such as `VipCustomer::new`). |
| | `Objective` | The three victory targets and how they are checked. |
| `entity` | `Restaurant` | Tables, waiting queue, payment queue, ledger and Happy Hour. Checks each request against the rules before changing any state, and prices each order. |
| | `Customer` (abstract), `RegularCustomer`, `VipCustomer`, `CriticCustomer` | One visit's life cycle (private state) plus the behaviour that varies by kind of customer: patience loss, food choice, bill. The controller never checks a customer's type. |
| | `Order`, `OrderStatus` | Preparation progress, with the bill locked when the order is taken. |
| | `MenuItem`, `ComboMeal`, `Menu` | What can be ordered. A `ComboMeal` *is a* `MenuItem` made of two other items, with price and units derived from them. |
| | `HappyHour` | The once-per-game pricing policy: availability, remaining turns, discount and recovery. |
| | `Table`, `CustomerState`, `Ledger`, `Money` | Seating, visit state, running totals and cents arithmetic. |
| | `Staff` (abstract), `Host`, `Waiter`, `Chef`, `Cashier` | Staff and their one-task-per-turn budget. |
| | `Assignable`, `TaskCommand`, `TaskOption`, `Seating` | How the controller directs staff without knowing their roles; seating shared by Host and Waiter. |

## Reproducible runs

See [TEST_NOTES.md](TEST_NOTES.md). Each stage has a victory and a defeat script in `test-runs/`
(for example `stage3-victory.txt` and `stage3-defeat.txt`) that replay with the base setting and
seed 2002:

```bash
./run.sh --echo < test-runs/stage3-victory.txt
```

## Stage history

| Stage | Tag | Adds |
|---|---|---|
| 1 | `stage-1` | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, both endings |
| 2 | `stage-2` | VIP and Critic customers |
| 3 | `stage-3` | Host, ComboMeal, Happy Hour |

Compare links (replace `<owner>/<repo>` once the repository is on GitHub):

- Stage 1 -> 2: `https://github.com/<owner>/<repo>/compare/stage-1...stage-2`
- Stage 2 -> 3: `https://github.com/<owner>/<repo>/compare/stage-2...stage-3`

Locally: `git diff --stat stage-1 stage-2` and `git diff --stat stage-2 stage-3`.

### Stage 1 -> Stage 2

**New files**

- `entity/VipCustomer.java`: loses 5 per turn and overrides `priceFor` to give 10% off.
- `entity/CriticCustomer.java`: loses 12 per turn and overrides `chooseItem` to always order the
  most expensive item.
- `test-runs/stage2-*.txt` / `.log`: reproducible Stage 2 victory and defeat.

**Changed files**

- `entity/Customer.java`: `chooseItem` was abstract. It is now a concrete default (uniform random
  choice) that was moved up from `RegularCustomer`. The VIP shares this rule, so moving it up
  avoids copying the code into `VipCustomer`; the Critic overrides it. Behaviour is unchanged for
  Regular customers (same seed, same choices).
- `entity/RegularCustomer.java`: the random-choice method was removed (it now lives in
  `Customer`).
- `control/GameConfig.java`: registration only. Turns 4 and 7 now use `VipCustomer::new` and
  `CriticCustomer::new` instead of `RegularCustomer::new`. There are still six arrivals.
- `test-runs/run-all.sh`: takes the stage number, since each stage's scripts belong to its tag.

**Unchanged:** `GameController`, `Restaurant`, `Order`, all staff classes and `ConsoleUI`. The new
behaviour is reached through the existing `Customer` methods (`getSatisfactionLossPerTurn`,
`priceFor`, `chooseItem`), with no type checks in the turn controller.

### Stage 2 -> Stage 3

**New files**

- `entity/Host.java`: a new `Staff` that implements `Assignable` and can only seat.
- `entity/Seating.java`: seating options and the `seat` task, moved out of `Waiter` so the Host and
  the Waiter share one implementation.
- `entity/ComboMeal.java`: a `MenuItem` subclass composed of two items. It derives its price
  (90%, rounded) and its preparation units from them.
- `entity/HappyHour.java`: the once-per-game policy (activation, remaining turns, 20% discount,
  15-point recovery).
- `test-runs/stage3-*.txt` / `.log`: reproducible Stage 3 victory and defeat.

**Changed files**

- `control/GameController.java`: registers the Host (`List.of(new Host(), new Waiter(), new Chef())`)
  and adds the Happy Hour step at the start of each turn, plus `endTurn()` at the end. The six
  rule steps are unchanged. The Host is directed by the same loop as every other `Assignable`.
- `control/GameConfig.java`: `createMenu()` adds the two combos to the standard menu
  (registration only).
- `entity/Restaurant.java`: owns a `HappyHour`. `priceOrder` now applies Happy Hour *after* the
  customer's own `priceFor` (VIP first, then Happy Hour, as the brief requires). The new
  `recoverWaitingCustomers` applies the turn-start recovery. Pricing stays in the one method where
  the bill is locked, so existing orders are never repriced.
- `entity/Waiter.java`: uses `Seating` instead of its own seating code. Behaviour is unchanged.
- `entity/Menu.java`: new `findByName`, so combos can be built from existing items.
- `boundary/ConsoleUI.java`: the welcome screen lists the staff passed in, instead of a hard-coded
  list. New `showHappyHourStatus` and `askYesNo` for the Happy Hour prompt.
- `test-runs/run-all.sh`: defaults to the Stage 3 scripts.

**Unchanged:** all customer classes (the Critic's rule picks a combo without any change), `Order`,
`Chef`, `Cashier`, `Staff`, `Assignable`, `Objective`, `ArrivalSchedule`. `ComboMeal` works
everywhere a `MenuItem` did, so ordering, cooking and billing needed no changes.

## Video

| # | Segment | Timestamp |
|---|---|---|
| 1 | The game launching from the submitted project | `mm:ss` |
| 2 | Meaningful manager choices, including a decision where staff are constrained | `mm:ss` |
| 3 | A dish going READY -> SERVED -> payment | `mm:ss` |
| 4 | A VIP or Critic behaviour | `mm:ss` |
| 5 | A Stage 3 feature | `mm:ss` |
| 6 | One victory and one defeat | `mm:ss` |
| 7 | The stage tag comparisons and the diagrams | `mm:ss` |
