# Restaurant Rush

A turn-based restaurant management game for the command line, written in Java for the SC2002
group assignment. You are the manager. Each turn you decide what the Waiter and the Chef do, and
you try to reach the targets by the end of turn 18.

- **Repository:** `<add the GitHub URL here>`
- **This version:** Stage 1, basic restaurant operation (tag `stage-1`)

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

## How to play

At the start of each turn the game shows the turn number, revenue, the average satisfaction of
paid customers, the waiting queue, every occupied table with that customer's satisfaction and
order status, and the tasks each staff member can do. Then it asks for one task per staff member,
**Waiter first, then Chef**. Type the task's number, or type the command:

| Staff | Commands |
|---|---|
| Waiter | `seat C1 T2` (or `seat C1` for the first free table), `order C1`, `serve C1`, `wait` |
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
2. **Manager choices.** Waiter: seat, take an order, serve a READY dish, or wait. Chef: one
   preparation unit, or wait. The Chef may cook an order the Waiter took this turn.
3. **Kitchen and serving.** The Waiter acts before the Chef, so a dish finished this turn can be
   served next turn at the earliest.
4. **Eating and payment.** The Cashier charges first: the oldest customer who became READY_TO_PAY
   on an earlier turn. Then customers served on an earlier turn finish eating and become
   READY_TO_PAY. Payment adds the bill to revenue, records satisfaction and frees the table.
5. **Waiting and abandonment.** Every customer who is queuing, seated without food, or waiting for
   food loses satisfaction (Regular: 8, minimum 0). This includes the turn they arrive (the brief's
   Section 3.3 example ends turn 1 at 92). At 0 they leave: any unfinished order is cancelled and
   the table freed.
6. **Result.** All 18 turns are always played. Then the game reports Victory or Defeat.

**Food choice rule.** Customers choose their own food; the manager never does. A Regular
customer picks uniformly at random from the menu. Each customer's random source is seeded from
the game seed and the customer's number, so choices are the same whatever order the manager
takes orders in. The choice is shown when the order is taken. With seed 2002: C1 Pasta, C2 Salad,
C3 Salad, C4 Pasta, C5 Burger, C6 Salad.

**Money** is stored as integer cents and shown as dollars with two decimals.

## Design overview

The code follows the Entity-Control-Boundary split used in SC2002:

| Package | Class | Responsibility |
|---|---|---|
| `app` | `Main` | Reads command-line options and starts the game. |
| `boundary` | `ConsoleUI` | Displays state and reads the manager's input. It contains no game rules. |
| `control` | `GameController` | Runs the six turn steps in order and passes each decision to the right staff member. |
| | `GameConfig` | The base setting: turns, tables, seed, arrivals, targets, menu. |
| | `ArrivalSchedule` | Which kind of customer arrives on which turn (factories such as `RegularCustomer::new`). |
| | `Objective` | The three victory targets and how they are checked. |
| `entity` | `Restaurant` | Tables, waiting queue, payment queue and ledger. Checks each request against the rules before changing any state. |
| | `Customer` (abstract), `RegularCustomer` | One visit's life cycle (private state) plus the behaviour that varies by kind of customer: patience loss, food choice, bill. |
| | `Order`, `OrderStatus` | Preparation progress, with the bill locked when the order is taken. |
| | `MenuItem`, `Menu` | What can be ordered: price and preparation units. |
| | `Table`, `CustomerState`, `Ledger`, `Money` | Seating, visit state, running totals and cents formatting. |
| | `Staff` (abstract), `Waiter`, `Chef`, `Cashier` | Staff and their one-task-per-turn budget. |
| | `Assignable`, `TaskCommand`, `TaskOption` | How the controller directs staff without knowing their roles. |

## Reproducible runs

See [TEST_NOTES.md](TEST_NOTES.md). `test-runs/stage1-victory.txt` and `test-runs/stage1-defeat.txt`
replay a victory and a defeat with the base setting and seed 2002.
