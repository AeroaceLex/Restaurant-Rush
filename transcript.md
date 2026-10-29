# Restaurant Rush — ComboMeal AI conversation

Exported from OpenCode session `ses_e7957ea0effeeR7Jv2x0UkTyCs` on 2026-10-22.
The conversation ran from 2026-10-22 15:55 to 2026-10-22 23:15
and contains 12 prompts with the replies that followed them.

Each prompt and reply below is verbatim. Tool calls, internal reasoning traces and
system messages are omitted, because they are not part of the conversation itself;
the replies still describe what each tool produced. Replies keep their original
Markdown, so a reply may contain its own `##` headings inside its turn.

The final request in this session — *"export the entire conversation in order"* — is
the instruction that produced this file, so its reply is not reproduced here.

---

## Index

1. I'm working on a group Java assignment called Restaurant Rush. It's a turn-based restau...
2. i have uploaded the relevants files as part of mine to do. Rerun the previous prompt
3. Here is what Stage 3 requires for combos (translated from the assignment brief):
4. Now implement the design we agreed on.
5. Next, Menu needs a way to look up an existing item so combos can be built from the stan...
6. Now register the combos in GameConfig.createMenu().
7. Now let's prove the rest of the game needs no changes for combos.
8. Before I run anything, build a verification table I can check by hand.
9. Now give me a way to verify the code without JUnit (the project has no test framework).
10. I ran the game with the Stage 3 victory input and seed 2002. Here is the real terminal ...
11. Update the project documentation for my part.
12. Last step: help me make sure I can defend this code.

---

## Turn 1 — Prompt

I'm working on a group Java assignment called Restaurant Rush. It's a turn-based restaurant management game for the command line. Each turn the manager directs a Host, Waiter and Chef, and tries to meet targets by turn 18.

I'm responsible for the ComboMeal part of Stage 3. I want to build it with you step by step, and I'm keeping this whole conversation as my record of AI use, so please explain your reasoning and don't skip steps.

Project constraints:
- Plain Java 11+, no third-party libraries, no Maven/Gradle. Compile with javac into out/ and run restaurant.app.Main.
- Entity-Control-Boundary packages: restaurant.app, restaurant.boundary, restaurant.control, restaurant.entity.
- Money is integer cents. Money.percentOf(cents, percent) rounds half-up to the nearest cent. No doubles for money.
- Stage 2 already works: Salad $9.00 (1 prep unit), Burger $15.00 (1), Pasta $18.00 (2). Regular and VIP pick uniformly at random from the menu; a Critic picks the most expensive item. The bill is locked when the order is taken. The Chef spends one preparation unit per turn.
- Stage 3 also adds Host and Happy Hour, which teammates own. I must not change their behaviour.
- Code style: 4 spaces, final fields, small methods, Javadoc on public classes/methods.

The files my part touches are attached/pasted below: MenuItem, Menu, Order, OrderStatus, Customer, CriticCustomer, Chef, Restaurant (takeOrder/prepare/serve/priceOrder), GameConfig (createMenu), and the menu/design parts of README.md.

For this first reply, please:
1. Restate the setup in at most 10 bullets so I can check you understood it.
2. Trace how a normal dish currently gets from the menu to the customer's bill, naming the exact classes and methods.
3. List the extension points where a combo could plug in.
4. Ask me any questions you need answered before we design it.

Do not write any code yet.

## Turn 1 — Reply

## Before anything else

The files you meant to attach didn't come through — this conversation has no pasted code, and the working directory only contains two unrelated markdown documents. So parts 2 and 3 below are built from your description plus the method names you listed, not from reading your actual source. I've marked what's inferred. Paste or attach the files and I'll correct anything I've guessed wrong before we design.

---

## 1. Setup as I understand it

1. Group Java assignment: **Restaurant Rush**, a turn-based CLI restaurant management game. The manager directs a Host, a Waiter and a Chef, chasing targets by **turn 18**.
2. My scope is **ComboMeal only, Stage 3**. **Host and Happy Hour belong to teammates** and I must not change their behaviour.
3. Build: plain **Java 11+**, no third-party libraries, no Maven/Gradle. `javac` into `out/`, run `restaurant.app.Main`.
4. Packages: `restaurant.app`, `restaurant.boundary`, `restaurant.control`, `restaurant.entity` (Entity-Control-Boundary).
5. Money is **integer cents**, never doubles. `Money.percentOf(cents, percent)` rounds **half-up** to the nearest cent.
6. Stage 2 menu: Salad **900** (1 prep unit), Burger **1500** (1), Pasta **1800** (2).
7. Regular and VIP customers pick **uniformly at random** from the menu; a **Critic picks the most expensive** item.
8. The **bill is locked when the order is taken**.
9. The **Chef spends one preparation unit per turn**.
10. Style: 4 spaces, `final` fields, small methods, Javadoc on public classes and methods.

---

## 2. How a normal dish currently reaches the bill (*inferred — verify against your code*)

1. **`GameConfig.createMenu()`** constructs the three `MenuItem`s and the `Menu` that holds them. Price lives on the `MenuItem` in cents; prep units live there too.
2. **`Restaurant.takeOrder(...)`** asks the customer for a choice. The *policy* sits in the entity: `Customer` picks uniformly at random from the menu, `CriticCustomer` picks the highest-priced item. `Restaurant` orchestrates; it doesn't decide.
3. An **`Order`** is created holding the chosen item (or items) plus an `OrderStatus`, initially something like `WAITING`. This is where the **price snapshot** happens — the locked amount must be copied onto the order, not re-read from the menu later.
4. **`Chef.prepare(...)`** spends one prep unit per turn against the order's outstanding units; Pasta takes two turns, Salad and Burger take one. When units hit zero the status moves to `READY`.
5. **`Restaurant.serve(...)`** hands the ready order to the customer and moves the status to `SERVED`.
6. **`Restaurant.priceOrder(...)`** turns the locked order into cents and adds it to the takings. If any modifier exists (discount, surcharge), this is the only place it should be applied.
7. The **boundary** renders menu, order queue and receipt text. No money maths belongs there.

The point worth flagging: steps 3 and 6 are the only two places money is decided, and they are **separated by several turns**. Any combo design has to keep those two consistent.

---

## 3. Extension points where a combo could plug in

1. **`MenuItem` / `Menu`** — is a combo a fourth kind of menu entry, or a rule layered over existing entries? This is the fork in the road for everything else.
2. **`GameConfig.createMenu()`** — if combos are data, this is where the bundle definition (contents, discount, price) is declared.
3. **`Customer` / `CriticCustomer`** — random selection needs to know whether combos are in the pool; "most expensive" needs to know whether a combo competes with Pasta at 1800.
4. **`Order` / `OrderStatus`** — a combo is several items under one bill, so the order needs a contents list and possibly a new status (partially prepared, or awaiting a missing component).
5. **`Chef.prepare(...)`** — prep-unit accounting. A three-item combo could be 4+ units, i.e. several turns for one order, which changes throughput against the turn-18 target.
6. **`Restaurant.priceOrder(...)`** — the single place a discount should be computed, via `Money.percentOf`, so half-up rounding is applied once to one base amount.
7. **`README.md`** — menu and design sections need the combo documented alongside the Stage 2 items.

---

## 4. Questions before we design

**Blocking**
1. Can you paste the actual files? I'd rather design against real signatures than plausible ones.

**What a combo *is***
2. What does the Stage 3 brief say verbatim about ComboMeal — fixed named bundles, or a discount triggered by what's in the order? Which bundles and what discount?
3. **Who decides on a combo:** does the customer randomly choose a combo *instead of* a dish, does the manager/waiter offer it as an upsell, or is it automatic when the order contains the right items? These give three quite different systems.
4. Does a combo have a **fixed bundle price**, or is it a **percentage off the sum**? If a percentage: is it applied to the combined total (one rounding) or per item (three roundings, different answer)? Example: Salad + Burger = 2400; 20% off the total is 480 → **1920**. Twenty percent off each item separately is 180 + 300 → **1920** here, but it diverges on amounts that don't divide evenly — which convention does the brief want?

**Integration risk**
5. **Happy Hour + combo:** does Happy Hour discount stack with the combo discount, and in what order? More importantly, if Happy Hour is a per-turn state and `priceOrder` runs at serve time, the "locked at order time" rule is already under strain. Does your teammate's Happy Hour apply the discount at take-order time or at serve time?
6. Does the combo change **how revenue is counted** toward the turn-18 targets — pre-discount or post-discount?
7. Do combos apply to **Critics** too, and does a Critic's "most expensive" rule consider combo entries?

**Practical**
8. Can a combo be ordered alongside à la carte items in the same order, and can it contain the same item twice?
9. How do you currently verify Stage 2 behaviour — a test class, a scripted run, or by hand? I'd like a way to prove the combo didn't break existing pricing.
10. Are you and your teammates editing `Restaurant.java` on separate branches, or all in one working copy? That decides how we sequence the combo changes against Happy Hour.

Answer what you can and paste the files — then we can settle the combo's shape before touching any code.


## Turn 2 — Prompt

i have uploaded the relevants files as part of mine to do. Rerun the previous prompt
Attached file: `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3\src\restaurant\entity\Restaurant.java`
Attached file: `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3\src\restaurant\entity\Chef.java`
Attached file: `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3\src\restaurant\entity\CriticCustomer.java`
Attached file: `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3\src\restaurant\entity\Customer.java`
Attached file: `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3\src\restaurant\entity\Menu.java`
Attached file: `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3\src\restaurant\entity\MenuItem.java`
Attached file: `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3\src\restaurant\entity\Order.java`
Attached file: `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3\src\restaurant\entity\OrderStatus.java`
Attached file: `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3\src\restaurant\control\GameController.java`

## Turn 2 — Reply

## Two things I found before answering

I read the files, the git history, and compiled both your branch and `origin/main`. Two surprises:

**1. Your branch does not compile.** You're on `benjaminwe4-patch-1-1` at `d439b3c` ("Add combo meal"). It is missing three files that exist on `origin/main`:

| Missing file | Referenced by |
|---|---|
| `restaurant/app/Main.java` | the entry point |
| `restaurant/boundary/ConsoleUI.java` | `GameController` line 9 |
| `restaurant/entity/HappyHour.java` | `GameController` line 14, `Restaurant` line 25 |

`javac` fails with `package restaurant.boundary does not exist` / `cannot find symbol: class HappyHour`. I extracted `origin/main` into a temp folder and it compiles cleanly (`exit=0`), so `origin/main` is the working baseline. Your commit was merged upstream as `c7afd88`, and a teammate added Happy Hour afterwards as `2bf1b8c`.

**2. ComboMeal is already written, on your branch, and merged.** `entity/ComboMeal.java` exists in your commit, `GameConfig.createMenu()` already adds two combos, and README sections 100–120 already document them. So "build the ComboMeal part" is mostly done; what's left is verification, edge cases, and confirming it matches the brief. I'll treat it as existing work to review rather than a blank page — tell me if that's not how you remember it.

---

## 1. Setup as understood (corrected against the code)

1. **Restaurant Rush**, turn-based CLI game, 18 turns, base setting: 2 tables, 6 arrivals, target 4 paid / $45.00 / average satisfaction 60.
2. My scope: **ComboMeal, Stage 3**. **Host and Happy Hour are teammates' work** and I must not change their behaviour.
3. `Restaurant` lives in **`restaurant.entity`**, not `restaurant.control`. There is no `GameConfig.createMenu` building items from scratch — it calls `Menu.standard()` and appends combos.
4. Packages: `app`, `boundary`, `control`, `entity`. Plain Java 11+, no libraries, no build tool.
5. Money is integer cents. `Money.percentOf(cents, percent)` = `(cents * percent + 50) / 100` in `long`, half-up.
6. Stage 2 dishes: Salad 900/1, Burger 1500/1, Pasta 1800/2.
7. `Customer` defaults to uniform random over `menu.getItems()`; `CriticCustomer` overrides to the highest `getPriceCents()`, first-listed wins a tie.
8. `Order` stores an immutable `billCents` at construction, so the bill is genuinely locked.
9. `Chef` spends one unit: `Restaurant.prepare` → `Order.prepareOneUnit()`.
10. Style: 4 spaces, `final` fields, small methods, Javadoc on public APIs.

---

## 2. Menu to bill, exactly as the code does it

1. **`Menu.standard()`** creates the three `MenuItem`s and appends them in display order. **`GameConfig.createMenu()`** then calls `menu.findByName("Salad")` once, and adds `new ComboMeal(salad, burger)` and `new ComboMeal(salad, pasta)`.
2. `ComboMeal`'s constructor delegates to `super(name, Money.percentOf(sum, 90), unitsSum)` — so the combo's price is **computed once, at construction**, and is a plain list price from then on. `2100 → 2160`; `2700 → 2430`.
3. **`GameController`** constructs `new Restaurant(config.createMenu(), tables, seed)`.
4. Arrival: `ArrivalSchedule` factory → `Restaurant.admit` → `customer.arrive(turn)`, queued.
5. Seating: `Host`/`Waiter` → `Restaurant.seat` → `customer.seatAt(table)` (`SEATED`).
6. Ordering: `Waiter` → **`Restaurant.takeOrder(customer, turn)`**:
   - `customer.chooseItem(menu, randomFor(customer))` picks the item (random or Critic's max);
   - `priceOrder(customer, item)` → `happyHour.applyTo(customer.priceFor(item.getPriceCents()))`, i.e. **VIP 90% first, Happy Hour 80% second**;
   - `new Order(customer, item, billCents, turn)` freezes the amount;
   - `customer.placeOrder(order)` → `ORDERED`.
7. Cooking: `Chef.availableTasks` lists `ORDERED` customers whose status `isCookable()`; `Restaurant.prepare` → `Order.prepareOneUnit()` increments `unitsDone`, flips to `PREPARING`, then `READY` when `unitsDone >= item.getPreparationUnits()`.
8. Serving: `Restaurant.serve` requires `READY` → `markServed()`, `customer.startEating(turn)`, `ledger.recordServed()`.
9. Next turn `finishEating` → `READY_TO_PAY`; then `Cashier` → `Restaurant.collectNextPayment` → `markPaid()`, `customer.pay()`, `table.release()`, and **`ledger.recordPayment(order.getBillCents(), satisfaction)`**.
10. The UI only formats: `MenuItem.toString()` and `Money.format(billCents)`.

Because a combo **is a** `MenuItem`, steps 6–10 are identical for a combo. That is the whole design idea, and it works.

---

## 3. Extension points

The route already taken is **subclassing `MenuItem`** (combo = a menu entry with a derived price and unit count). Other routes were possible; these are the points that decide:

1. **`MenuItem` (constructor contract)** — price and units are `final` and validated positive. `ComboMeal` feeds derived values through it, so any future change to `MenuItem` must keep this legal. A combo of a combo (legal today, since `ComboMeal` is a `MenuItem`) would apply 90% twice and add units — probably worth a guard.
2. **`Menu` (flat list, no ordering metadata)** — a combo is a peer of its components. Consequences: a customer never buys "combo + extra dish" in one order, and a Critic's max-price scan now sees combos.
3. **`GameConfig.createMenu()`** — the only place combos are declared. Adding a third combo is a one-line change here.
4. **`Customer.chooseItem` / `CriticCustomer.chooseItem`** — unchanged, and this is the load-bearing bet: the Critic now orders **Salad + Pasta Combo (2430)** instead of Pasta. That silently changes Stage 2 Critic behaviour, which the README justifies but the brief may not.
5. **`Order` (one `MenuItem`, one bill)** — a combo is **atomic**: 3 prep units on one order, served once. There is no "salad ready, pasta still cooking". If the brief wants components plated separately, this is the point that must change, and it is the biggest fork in the design.
6. **`Restaurant.priceOrder`** — the single pricing choke point, applied at take-order time. Combo discount is already baked into `getPriceCents()`, so it composes with VIP then Happy Hour. Rounding happens at each step.
7. **`Restaurant.prepare` / `Chef.perform`** — both read `order.getItem().getPreparationUnits()`, so combos need no change; a 3-unit combo simply occupies the Chef for 3 turns.
8. **`ComboMeal.getComponents()`** — currently **dead code**: nothing calls it. Either the boundary should use it (show components separately on the ticket) or it should go.
9. **`README.md`** — menu table, combo choice rule and design table are already updated. `TEST_NOTES.md` Stage 3 tables exist, but `test-runs/stage3-*.txt` are only on `origin/main`, not your branch.

---

## 4. Questions

**Blocking**
1. Shall I merge `origin/main` into your branch (or check out `main`) so the project compiles again? I'd do it as a fast-forward-safe merge, no rebase, so your history stays reviewable. I won't touch it without your go-ahead.

**Brief conformance** — this is what I most need
2. What does the Stage 3 brief say about ComboMeal, verbatim? Specifically: does it mandate the **90%**, the **two named combos**, **summed prep units**, and **one atomic order**? The current code invents all four.
3. Is a combo supposed to be orderable **alongside** other dishes, or is it a single menu choice as implemented?
4. Should the **Critic** be allowed to pick a combo? Right now the 2430 combo beats Pasta, so the Critic's behaviour changes from Stage 2 and gets materially harder (3 Chef turns at 12 patience per turn). Is that intended, or should the Critic's rule look only at the three base dishes?
5. Stacking: is "combo price, then VIP 10%, then Happy Hour 20%" the required order? A VIP combo during Happy Hour currently costs 2430 → 2187 → 1750 (`Money.percentOf` each step).

**Verification**
6. Do you have the Stage 3 replay scripts and expected log on `main`? If so, after merging I'd run `stage3-victory.txt` and `stage3-defeat.txt` and diff against the committed `.log` — that's the cheapest proof nothing regressed.
7. No test framework is allowed, so do you want a small `ComboMeal`-specific check (a `main` class or a temporary harness) proving 2160/2430, units 2/3, and the discounted bills? Or is the replay transcript enough for your report?

**Small design calls**
8. Should `ComboMeal` reject a null component and reject another `ComboMeal` as a component, in line with `MenuItem`'s existing validation? Both currently pass silently (null gives an NPE in the constructor).
9. Keep or remove `getComponents()`? If kept, I'd surface it in the receipt/boundary; if not, it's unused public API.

Answer 1–4 and I can tell you whether the existing `ComboMeal` is final or needs rework — before we change a single line.


## Turn 3 — Prompt

Here is what Stage 3 requires for combos (translated from the assignment brief):

1. A combo is two existing menu items sold together as one order.
2. Its display name is "<item 1> + <item 2> Combo".
3. Its list price is 90% of the sum of the two items' list prices, rounded half-up to the nearest cent, stored in cents.
4. Its preparation units are the sum of the two items' units; the Chef still spends one unit per turn.
5. It must be usable anywhere a MenuItem is used, with no instanceof or type checks in Order, Chef, Waiter, Cashier or Restaurant.
6. The base menu must gain two entries: Salad + Burger Combo ($21.60, 2 units) and Salad + Pasta Combo ($24.30, 3 units). The three original dishes stay unchanged.
7. Regular and VIP customers choose uniformly from all five entries, so each has a 2-in-5 chance of a combo. The Critic still chooses the most expensive entry, which becomes Salad + Pasta Combo.
8. Existing Stage 2 behaviour must not change except where the brief says it should: the Critic now orders the combo, and seeded random choices shift because the menu has five entries.

Design questions:
- Compare (a) ComboMeal extends MenuItem with two component references, (b) a ComboMeal that wraps a List<MenuItem>, and (c) adding a components/flag field to MenuItem. Which is the smallest change that satisfies every requirement?
- Which files must change, and which must stay untouched?
- What are the risks to existing Stage 2 tests and seeded runs?

Recommend one design with a short text UML sketch and justify it. No code yet.

## Turn 3 — Reply

## Verdict up front

Requirement 1–8 are satisfied by **(a)**, the design already in `ComboMeal.java`. Your choice is not which design to build; it's whether to keep (a) or replace it with something more general. My recommendation is **keep (a)**, with two small guards. Nothing in the trace below needs `instanceof`.

---

## 1. The three designs against the eight requirements

| | (a) `extends MenuItem`, two fields | (b) wraps `List<MenuItem>` | (c) components/flag field on `MenuItem` |
|---|---|---|---|
| Is a `MenuItem`? | Yes, by inheritance | **Only if it also extends `MenuItem`** — otherwise it can't enter `Menu`, `Order` or `Chef` without a new interface or casts | Yes, same class |
| Req 5 (no type checks anywhere) | Holds automatically: it *is* a `MenuItem` | Holds only in the `extends` variant | Holds only if `MenuItem` computes everything internally and nobody reads the flag |
| Req 1 (exactly two items) | **Encoded by the type**: two non-null fields | Allows 0, 1 or 5 components — must validate anyway | Allows any shape; must validate |
| Req 2–4 (name, 90%, summed units) | Four lines in `super(...)` | Loop + `String.join` + sum | Two constructor paths, or a factory/flag branch |
| Files changed | 1 new + 1 touched | 1 new + 1 touched (but ~40 lines, more validation) | 1 new (gone) + `MenuItem` rewritten, plus 2 constructors |
| Blast radius | zero existing call sites | zero in the `extends` variant | every construction site of `MenuItem`: `Menu.standard`, `GameConfig` |
| Wrong shape possible? | Only a combo-of-a-combo or duplicate pair | Duplicate items, single-item bundle, empty bundle | Nullable components, flag/price disagreement |

**Why (b) is not actually smaller.** A free-standing `ComboMeal` holding a `List<MenuItem>` cannot be added to `Menu` (which is `List<MenuItem>`) without either changing `Menu`, `Order`, `Chef` and `Restaurant` to a common supertype, or casting — which is exactly what requirement 5 forbids. So (b) collapses into "(a) with a loop", i.e. *more* code for generality the brief never asks for. The brief says **two**, full stop; two fields encode that better than a list.

**Why (c) is the worst fit.** It turns `MenuItem` — currently a clean value object with three `final` fields and an invariant enforced in its constructor — into a union: "either a dish with a price, or a bundle with components". You then need either nullable fields plus a `boolean`, or two constructors with a mode. The flag is the real danger: once `isCombo` exists, the next feature will read it in `Chef` or `Restaurant`, and requirement 5 is broken in spirit even if no `instanceof` appears. It also touches the one class every other class depends on, for zero behavioural gain.

**Smallest change that satisfies all eight: (a).** It is one new 28-line class plus four lines in `GameConfig`, and every existing file compiles and behaves identically.

---

## 2. Recommended design

```
            MenuItem                     <<entity>>
            -------------------------
            - name : String              {final}
            - priceCents : int           {final}
            - preparationUnits : int     {final}
            -------------------------
            + MenuItem(name, cents, units)
            + getName() / getPriceCents() / getPreparationUnits()
                     ^
                     |  extends
            ComboMeal                    <<entity>>
            -------------------------
            - first : MenuItem           {final}
            - second : MenuItem          {final}
            -------------------------
            + ComboMeal(first, second)
                super(first.name + " + " + second.name + " Combo",
                      Money.percentOf(first.price + second.price, 90),
                      first.units + second.units)
            + getComponents() : List<MenuItem>
```

Relationships: `Menu 1 --- 0..* MenuItem`; `Order 1 --- 1 MenuItem`; `ComboMeal 1 --- 2 MenuItem` (its components). `ComboMeal` adds **no** new state that the rest of the game can observe — price, name and units are baked into the `MenuItem` half at construction, which is why `Order`, `Chef`, `Waiter`, `Cashier` and `Restaurant` never learn that combos exist.

The two justifications that matter for your report:

1. **Substitutability.** The base contract is "a name, a positive price, a positive unit count". A combo honours it exactly. The 90% and the sum are computed *once*, in the constructor, so every later read is just a `MenuItem` read. That is why requirement 5 passes by construction rather than by discipline.
2. **The type carries the rule.** Requirement 1 is arity two. Two fields make an invalid combo unrepresentable at the type level; a list does not.

Two small guards I'd add when we get to code, both consistent with `MenuItem`'s existing style:

- reject `null` components (today a null throws `NullPointerException` deep in the super-constructor argument list, which is a poor message);
- reject a `ComboMeal` as a component (today `new ComboMeal(combo, salad)` silently applies 90% twice and triples the units).

`getComponents()` is currently unreferenced. Either keep it for the receipt/boundary, or drop it — it is not needed by any of the eight requirements.

---

## 3. Files that must change, and files that must not

**Must change (3 code files):**

| File | Change | Why |
|---|---|---|
| `entity/ComboMeal.java` | new class | the combo itself |
| `control/GameConfig.java` | `createMenu()` appends two combos | requirement 6; registration only, no logic |
| `entity/Menu.java` | `findByName` already added | lets `GameConfig` reuse the three standard items instead of duplicating prices |

**Must stay untouched — this is the evidence for requirement 5 and 8:**

- `entity`: `MenuItem`, `Order`, `OrderStatus`, `Chef`, `Waiter`, `Cashier`, `Restaurant`, `Customer`, `RegularCustomer`, `VipCustomer`, **`CriticCustomer`**, `Host`, `HappyHour`, `Seating`, `Staff`, `Table`, `CustomerState`, `Ledger`, `Money`, `Assignable`, `TaskCommand`, `TaskOption`, `InvalidActionException`
- `control`: `GameController`, `ArrivalSchedule`, `Objective`
- `app`: `Main` — `boundary`: `ConsoleUI`

The striking one is **`CriticCustomer`**: requirement 7 changes what the Critic *does* without changing a line of its code, because `chooseItem` scans `menu.getItems()` for the highest `getPriceCents()`. If you find yourself editing it, the design has sprung a leak.

`Restaurant` and `HappyHour` are being edited by your teammate for Happy Hour. This change does not touch them, which keeps your merge clean.

---

## 4. Risks to Stage 2 tests and seeded runs

1. **Seeded choices break at the first order — by design.** `Customer.chooseItem` calls `random.nextInt(items.size())`. Going from bound 3 to bound 5 does not remap the three old values; it draws different numbers. Your own `TEST_NOTES.md` shows this: C1 is Pasta at Stage 1/2 and Salad at Stage 3, with the same seed 2002. Every customer's dish changes. Requirement 8 permits it, but it means **Stage 2 transcripts are only valid under the `stage-2` tag** — don't "fix" them in place, and don't replay `stage2-victory.txt` at Stage 3 and call the divergence a regression.
2. **The Critic gets more expensive and one turn slower.** Pasta: 2 units, $18.00. Salad + Pasta Combo: 3 units, $24.30. One extra Chef turn, +$6.30 revenue. If service slips one turn, the Critic loses 12 satisfaction − the Stage 3 run expects 64. This is the one place where Stage 2 balance genuinely moves.
3. **Victory/defeat numbers no longer match Stage 2.** $62.10 / 70.3 becomes $76.86 / 83.8 in the documented Stage 3 run. Anyone comparing across tags must compare *outcomes*, not figures.
4. **Stacking is sequential, not compounded as one rounding.** Combo price is rounded once at construction ($24.30), then VIP ($21.87), then Happy Hour during active turns ($17.50 from 1749.6 half-up). Each is a separate `Money.percentOf` call, consistent with the README rule "rounded at the step where it is applied". The final bill cannot be used to reconstruct the 90%, which is fine because the bill is locked as an integer.
5. **Menu order is load-bearing.** Combos must be **appended after** the three dishes, so `Menu.standard()` keeps its three items unchanged (req 6) and the Critic's first-listed tie-break stays stable. If two entries ever tie on price, the earlier one wins — worth a sentence in the design notes.
6. **Uniformity is exact.** `nextInt(5)` gives 2/5 = 0.4 for combos with no weighting code. Adding a third combo later would change that to 3/6; that is expected, not a bug.
7. **Display is safe.** `ConsoleUI` prints menu entries as `"  - " + item`, and the table row's `%-25s` applies to the customer *state*, not the dish, so the longer combo name simply extends the line. No truncation, no alignment break.
8. **`findByName` is a stringly-typed dependency.** Renaming "Salad" or "Pasta" in `Menu.standard()` makes `GameConfig.createMenu()` throw `IllegalArgumentException` at startup. Acceptable, but it should be in your risk table.
9. **The real blocker is still the branch.** Your working copy lacks `Main`, `ConsoleUI` and `HappyHour`, and `test-runs/stage3-*` exist only on `origin/main`, so none of this can be verified until you merge. Merge first, then treat the committed `stage3-victory.log` as the expected output and diff against it.

---

## 5. Suggested next step

Merge `origin/main` into `benjaminwe4-patch-1-1` (it will be conflict-free: your commit is already merged upstream and the later commits touch disjoint files), then run the Stage 3 victory script and diff the transcript. If that matches, the design question above is settled and the only remaining work is the two constructor guards and the README wording. Say the word and I'll do the merge.


## Turn 4 — Prompt

Now implement the design we agreed on.

Write restaurant/entity/ComboMeal.java in full.
Requirements:
- public class ComboMeal extends MenuItem in package restaurant.entity.
- Constructor: ComboMeal(MenuItem first, MenuItem second). Reject null components with a clear IllegalArgumentException.
- Pass the derived name, price and preparation units to super(...). Derive the price with Money.percentOf on the sum of the two list prices at 90%. Never use double or float.
- Name format exactly: first.getName() + " + " + second.getName() + " Combo".
- Preparation units: first.getPreparationUnits() + second.getPreparationUnits().
- getComponents() returns the two items; the returned list must not be modifiable.
- Javadoc explaining that the combo is a MenuItem and that price/units are derived, not stored separately.
- Match the existing code style.

Before the code, explain in 3-4 sentences why the derived values must be computed before calling super(...). After the code, tell me one alternative implementation and why you did not choose it.

## Turn 4 — Reply

## Why the values must be computed before `super(...)`

Java requires a subclass constructor's `super(...)` call to be its very first statement, so there is no place to declare a local variable, validate an argument, or do arithmetic before `MenuItem`'s constructor runs. `MenuItem` then stores the name, price and unit count in `final` fields with no setters, so whatever reaches that constructor is fixed for the object's lifetime and can never be corrected afterwards. Because the combo's price and units are functions of its components, they have to be evaluated as expressions *inside* the `super(...)` argument list, and doing it there also guarantees the 90% rounding happens exactly once, in integer cents, rather than being recomputed (and possibly recomputed differently) on every read. The one consequence worth noting is that argument validation must live in a static helper called from that list, which is why `comboName(...)` is the first argument and does the null check.

## `stage-3/src/restaurant/entity/ComboMeal.java`

```java
package restaurant.entity;

import java.util.List;

/**
 * Two existing menu items sold together as one order.
 *
 * <p>A combo <em>is a</em> {@link MenuItem}: it has a name, a list price and a number of
 * preparation units, so the {@link Order}, the {@link Chef}, the {@link Waiter}, the
 * {@link Cashier} and the {@link Restaurant} treat it exactly like a single dish and never
 * need a type check. Those three values are derived once from the two components and passed
 * to {@link MenuItem}; the combo stores no price or unit count of its own, so it cannot fall
 * out of step with the items it is made of.
 *
 * <p>The list price is {@value #PRICE_PERCENT}% of the components' list prices added
 * together, rounded half-up to the nearest cent by {@link Money#percentOf(int, int)}. The
 * preparation units are the components' units added together, so the Chef still spends one
 * unit per turn and a longer combo simply takes more turns.
 */
public class ComboMeal extends MenuItem {

    /** The combo costs this percentage of its components' combined list price. */
    public static final int PRICE_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;

    /**
     * Builds a combo from two existing menu items.
     *
     * <p>Java requires the {@code super(...)} call to be the first statement, so the name,
     * price and units are worked out inside the argument list. Arguments are evaluated left
     * to right, and {@link #comboName(MenuItem, MenuItem)} rejects a null component before
     * the other two helpers read it.
     *
     * @param first  the first component; not null
     * @param second the second component; not null
     * @throws IllegalArgumentException if either component is null
     */
    public ComboMeal(MenuItem first, MenuItem second) {
        super(comboName(first, second),
                Money.percentOf(combinedPriceCents(first, second), PRICE_PERCENT),
                combinedPreparationUnits(first, second));
        this.first = first;
        this.second = second;
    }

    /** The two components, first then second, in a list that cannot be modified. */
    public List<MenuItem> getComponents() {
        return List.of(first, second);
    }

    /** The display name, e.g. "Salad + Pasta Combo"; also rejects null components. */
    private static String comboName(MenuItem first, MenuItem second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("A combo needs two menu items, but one was null");
        }
        return first.getName() + " + " + second.getName() + " Combo";
    }

    private static int combinedPriceCents(MenuItem first, MenuItem second) {
        return first.getPriceCents() + second.getPriceCents();
    }

    private static int combinedPreparationUnits(MenuItem first, MenuItem second) {
        return first.getPreparationUnits() + second.getPreparationUnits();
    }
}
```

**Verification.** I compiled the four relevant files standalone with `-Xlint:all` (exit 0) and ran a throwaway harness kept outside the repo in `%TEMP%\opencode\combo-check`. All checks passed: name `Salad + Burger Combo`, 2160c / 2 units; name `Salad + Pasta Combo`, 2430c / 3 units; `toString()` = `Salad + Pasta Combo ($24.30, 3 units)`; `getComponents()` throws `UnsupportedOperationException` on `add`; null in either position throws `IllegalArgumentException` with the message above; and half-up rounding is exercised by a 5c + 10c pair, where 90% = 13.5c and the result is 14c. I did **not** add the combo-of-a-combo guard we discussed: your requirement list specified only the null check, so I left that behaviour unimplemented rather than inventing it. Say the word if you want it added.

## One alternative, and why I rejected it

A tempting alternative is to keep the components as the source of truth and **override `getName()`, `getPriceCents()` and `getPreparationUnits()`** to derive their answers lazily, passing placeholder values to `super(...)`. I rejected it because `MenuItem` reads its own private fields directly in places the subclass cannot intercept: `toString()` formats `priceCents` and `preparationUnits` from the fields, not the getters. The welcome screen would therefore print the placeholder price while `Restaurant.priceOrder` billed the derived one — a silent inconsistency between what the customer is shown and what they pay. It would also recompute `Money.percentOf` on every read and force placeholder values through `MenuItem`'s "price and units must be positive" validation, which is a lie the constructor is designed to catch. Deriving once and handing the result to `super(...)` keeps a single, printed, billed number.


## Turn 5 — Prompt

Next, Menu needs a way to look up an existing item so combos can be built from the standard dishes instead of duplicating prices.

Add Menu.findByName(String name):
- case-insensitive exact match on the item name
- returns the MenuItem
- throws IllegalArgumentException("No menu item called " + name) if there is no match
- keep standard() exactly as it is (Salad, Burger, Pasta) and keep getItems() unmodifiable
- update the class Javadoc if it helps

Show me the changed Menu.java (or a diff) and a 2-line example of how createMenu() will use it. Do not change any other class yet.

## Turn 5 — Reply

`findByName` is already present in your working copy — it came in with your combo commit — and it already matches the spec exactly (`equalsIgnoreCase`, returns the item, throws `IllegalArgumentException("No menu item called " + name)`). So there is nothing behavioural to add. The only gap against your style rule is that the public method has no Javadoc, which I'll add, plus a line in the class Javadoc saying why lookup exists.

`findByName` was already implemented in your working copy (it landed in your `Add combo meal` commit), so the behaviour you specified needed no new code. I added the missing Javadoc and left everything else alone.

## Diff

```diff
-/** The items customers can choose from, in a fixed display order. */
+/**
+ * The items customers can choose from, in a fixed display order. An item can also be looked
+ * up by name, so something built from existing dishes (such as a combo) reuses their prices
+ * and preparation units instead of repeating them.
+ */
 public class Menu {

     ...

+    /**
+     * Finds an existing item by its exact name, ignoring case.
+     *
+     * @param name the item name to look for
+     * @return the matching item
+     * @throws IllegalArgumentException if no item has that name
+     */
     public MenuItem findByName(String name) {
```

## Changed `Menu.java` in full

```java
package restaurant.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The items customers can choose from, in a fixed display order. An item can also be looked
 * up by name, so something built from existing dishes (such as a combo) reuses their prices
 * and preparation units instead of repeating them.
 */
public class Menu {

    private final List<MenuItem> items = new ArrayList<>();

    /** The three dishes of the base setting. */
    public static Menu standard() {
        Menu menu = new Menu();
        menu.add(new MenuItem("Salad", 900, 1));
        menu.add(new MenuItem("Burger", 1500, 1));
        menu.add(new MenuItem("Pasta", 1800, 2));
        return menu;
    }

    public void add(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add a null menu item");
        }
        items.add(item);
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Finds an existing item by its exact name, ignoring case.
     *
     * @param name the item name to look for
     * @return the matching item
     * @throws IllegalArgumentException if no item has that name
     */
    public MenuItem findByName(String name) {
        for (MenuItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        throw new IllegalArgumentException("No menu item called " + name);
    }
}
```

## How `createMenu()` uses it — two lines

```java
MenuItem salad = menu.findByName("Salad");
menu.add(new ComboMeal(salad, menu.findByName("Pasta")));
```

That is already exactly what `GameConfig.createMenu()` does today (lines 47–49: it looks up `Salad` once, then pairs it with `Burger` and `Pasta`), so **no change is needed in `GameConfig`**, and I have not touched it or any other class.

Two small notes for the record:

- `findByName(null)` does not throw `NullPointerException`: `equalsIgnoreCase(null)` returns `false`, so it falls through to `IllegalArgumentException("No menu item called null")`. That is acceptable for an internal lookup, but worth knowing if you ever feed it user input.
- `standard()` and `getItems()` are byte-for-byte unchanged; lookup reads the same private list and the returned list is still `Collections.unmodifiableList(items)`.

Verified by compiling `MenuItem`, `Money`, `Menu` and `ComboMeal` together with `-Xlint:all`: exit 0, no warnings. I did not rerun the harness, since the lookup logic itself was already covered by `ComboCheck` (it calls `findByName("Salad")` etc. for every case).


## Turn 6 — Prompt

Now register the combos in GameConfig.createMenu().

Requirements:
- start from Menu.standard()
- look up Salad, Burger and Pasta with findByName
- append new ComboMeal(salad, burger) and new ComboMeal(salad, pasta), in that order
- do not hard-code 2160, 2430, 2 or 3 anywhere; the combo derives them
- registration only: do not change GameConfig's other methods
- keep imports tidy

Show the full createMenu() method and the imports. Then list the exact five menu entries in display order and explain why the order matters for the seeded random choices. If you can infer the C1-C6 choices from the README, list them; otherwise tell me the command I should run to observe them.

## Turn 6 — Reply

`GameConfig.createMenu()` already does exactly this — it came in with your combo commit and I have not edited it. Rather than restate it from memory, here is the file as it stands, followed by proof that it produces the required five entries.

## Imports

```java
import restaurant.entity.ComboMeal;
import restaurant.entity.CriticCustomer;
import restaurant.entity.Menu;
import restaurant.entity.MenuItem;
import restaurant.entity.RegularCustomer;
import restaurant.entity.VipCustomer;
```

All six are used: `ComboMeal`, `Menu` and `MenuItem` in `createMenu()`; `CriticCustomer`, `RegularCustomer` and `VipCustomer` as method references in `baseSetting()`. No unused import, none missing.

## `createMenu()` in full

```java
    /** The three dishes plus two combos built from them. */
    public Menu createMenu() {
        Menu menu = Menu.standard();
        MenuItem salad = menu.findByName("Salad");
        menu.add(new ComboMeal(salad, menu.findByName("Burger")));
        menu.add(new ComboMeal(salad, menu.findByName("Pasta")));
        return menu;
    }
```

Check against your requirements: starts from `Menu.standard()`; all three dishes fetched by `findByName`; the two combos appended in the order Salad+Burger then Salad+Pasta; **no 2160, 2430, 2 or 3 anywhere** — the price and unit count cannot be typed by hand because `ComboMeal` only accepts two `MenuItem`s; nothing outside `createMenu()` touched.

## The five entries, in display order

| # | Index | Entry | Price | Units | Source |
|---|---|---|---|---|---|
| 1 | 0 | Salad | $9.00 | 1 | `Menu.standard()` |
| 2 | 1 | Burger | $15.00 | 1 | `Menu.standard()` |
| 3 | 2 | Pasta | $18.00 | 2 | `Menu.standard()` |
| 4 | 3 | Salad + Burger Combo | $21.60 | 2 | 90% of 2400 = 2160 |
| 5 | 4 | Salad + Pasta Combo | $24.30 | 3 | 90% of 2700 = 2430 |

## Why the order matters

`Customer.chooseItem` is `items.get(random.nextInt(items.size()))`: the drawn integer is an **index into the list**, so the list order is the mapping from random number to dish.

1. **Appending preserves the base menu.** Requirement 6 says the three dishes stay unchanged; building them via `Menu.standard()` and only appending leaves indices 0–2 exactly as Stage 1/2, which is also what keeps the class's documented "fixed display order" true and the README menu table readable.
2. **The bound changes too, not just the mapping.** Going from `nextInt(3)` to `nextInt(5)` draws different integers from each customer's seeded `Random`, so Stage 2 choices are not merely relabelled — they are replaced. This is the mechanism behind requirement 8's "seeded random choices shift".
3. **Ties depend on order.** `CriticCustomer` uses a strict `>`, so the *earlier-listed* item wins a price tie. The two combos (2160, 2430) do not tie, but if a future entry equals another's price, position decides.
4. **Declaration order is stable and testable.** Two combos, appended in a fixed sequence, mean the same seed always yields the same bill of fare; swapping them would change indices 3 and 4 and silently rewrite every seeded transcript.

## C1–C6 with seed 2002

The README states these, and I verified them rather than trusting the text: I compiled `origin/main` in a temp folder and replayed `test-runs/stage3-victory.txt`. The run finished VICTORY with $76.86 and average 83.8, matching the README exactly, and the final customer list reports each dish.

| Customer | Type | Choice | Bill |
|---|---|---|---|
| C1 | Regular | Salad | $9.00 |
| C2 | VIP | Pasta | $12.96 (VIP 10% then Happy Hour 20%) |
| C3 | Critic | Salad + Pasta Combo | $24.30 |
| C4 | Regular | Salad | $9.00 |
| C5 | Regular | Salad + Burger Combo | $21.60 |
| C6 | Regular | Salad + Pasta Combo | $24.30 |

So C1 and C4 draw index 0, C2 draws index 2, C3 is the Critic's max-price scan, C5 draws index 3, C6 draws index 4. Combo picks: C5 and C6, i.e. 2 of the 4 randomly-choosing customers.

**Command to reproduce** (from `stage-3/`, after your branch is merged so it compiles):

```bash
./run.sh --echo < test-runs/stage3-victory.txt
```

On Windows: `run.bat --echo < test-runs\stage3-victory.txt`. To just watch the choices without the full script, run `java -cp out restaurant.app.Main --seed 2002` and read the "Waiter takes ... chooses ..." lines.


## Turn 7 — Prompt

Now let's prove the rest of the game needs no changes for combos.

Trace this exact scenario: the Critic C3 is seated, the Waiter takes the order, the item is Salad + Pasta Combo, the Chef cooks it over three turns, the Waiter serves it, and the Cashier collects payment.

For each step, name the class and method and say what it does with the MenuItem. Cover at least:
- Customer.chooseItem / CriticCustomer.chooseItem
- Restaurant.takeOrder and priceOrder
- the Order constructor, getUnitsRemaining, prepareOneUnit and describeStatus
- the Chef's task and the Staff one-task budget
- Restaurant.prepare and Restaurant.serve
- Table, Ledger, Cashier
- ConsoleUI's menu and order displays

Then answer explicitly: does any class need to change to support a combo? If not, say "no change needed" for each and explain which polymorphic call makes it work. If something would break or display badly, give the smallest fix and say which file it belongs to.

## Turn 7 — Reply

Trace verified against the real game: I replayed `test-runs/stage3-victory.txt` on a compiled copy of `origin/main` and pulled C3's actual lines. Your working branch still can't compile (missing `Main`, `ConsoleUI`, `HappyHour`), so the quotes below come from the merged code, which has the same `ComboMeal` and `createMenu`.

---

## Turn 7 — arrival and seating

1. **`ArrivalSchedule.arrivalFor(7)`** returns the `CriticCustomer::new` factory; **`Restaurant.admit`** calls `customer.arrive(7)` and queues them. No `MenuItem` exists yet.
2. **`Host.perform` → `Seating.seat` → `Restaurant.seat`** → `Table.assign(customer)` and `Customer.seatAt(table)` (`SEATED`). `Table` stores a **`Customer`**, never a dish, so combos are invisible here.

## Turn 7 — ordering

3. **`Waiter.availableTasks`** builds `TaskOption("order C3", …)` from `restaurant.getCustomersIn(CustomerState.SEATED)`. No dish involved.
4. **`Waiter.perform` → `Waiter.takeOrder`** → `command.customer(restaurant)` → `Restaurant.findCustomer`, then **`Restaurant.takeOrder(customer, 7)`**:
   - guards `customer.getState() != SEATED`;
   - **`customer.chooseItem(menu, randomFor(customer))`** dispatches to **`CriticCustomer.chooseItem`**, which loops `menu.getItems()` comparing `item.getPriceCents()` with a strict `>`. Because a `ComboMeal`'s price lives in `MenuItem`'s own `priceCents` field, the scan needs no combo case: 2430 > 2160 > 1800, so the **Salad + Pasta Combo** wins. The Critic ignores the `Random`, so the seed is irrelevant here.
   - **`priceOrder(customer, item)`** → `customer.priceFor(item.getPriceCents())` (Critic: full price) → `happyHour.applyTo(2430)` (inactive, returns 2430 unchanged).
   - **`new Order(customer, item, 2430, 7)`** stores the `MenuItem` reference and freezes `billCents`. Note the declared type is `MenuItem`, the object is a `ComboMeal`.
   - **`customer.placeOrder(order)`** → `ORDERED`.
5. `Waiter.takeOrder` formats `order.getItem()`, i.e. `MenuItem.toString()` reading the base fields: **"Salad + Pasta Combo ($24.30, 3 units)"**. Then **`Staff.markTaskUsed()`**.

## Turns 7–9 — cooking

6. **`Chef.availableTasks`** filters `CustomerState.ORDERED` and `order.getStatus().isCookable()`, and describes the task with `order.getItem().getName()` + `order.describeStatus()`.
7. **`Chef.perform`**: `Staff.checkTaskAvailable()` → `requireArguments(1, 1, "cook <customer>")` → `restaurant.prepare(customer)`:
   - **`Restaurant.prepare`** checks `order != null` and `order.getStatus().isCookable()`, then calls **`Order.prepareOneUnit()`**.
   - **`Order.prepareOneUnit()`** increments `unitsDone` and sets `status = unitsDone >= item.getPreparationUnits() ? READY : PREPARING`. `item.getPreparationUnits()` returns **3**, inherited from `MenuItem`, filled by `ComboMeal`'s `super(...)`.
8. **`Staff.markTaskUsed()`** consumes the Chef's single task. **`Staff.startTurn()`**, called for every `Assignable` in `GameController.playTurn`, resets it next turn. So the Chef spends exactly **one of the three units per turn** — the unit budget belongs to the staff member, not the dish; the dish only decides how many turns it takes.
9. **`Order.describeStatus()`** returns `status + " " + unitsDone + "/" + item.getPreparationUnits()`: `PLACED 0/3` → `PREPARING 1/3` → `PREPARING 2/3` → `READY`. Actual transcript:

```
1) cook C3        work on C3's Salad + Pasta Combo (PLACED 0/3)
Chef works on C3's Salad + Pasta Combo: unit 1 of 3 done.
T1  C3  Critic   satisfaction  88  waiting for food   Salad + Pasta Combo PREPARING 1/3, bill $24.30
Chef works on C3's Salad + Pasta Combo: unit 3 of 3 done. The order is READY ...
```

`Order.getUnitsRemaining()` (3, 2, 1, 0) is defined and correct but **called nowhere** in the current code — worth knowing if you cite it.

## Turn 10 — serving

10. **`Waiter.availableTasks`** lists `serve C3` because `order.getStatus() == READY`.
11. **`Restaurant.serve`** requires `READY`, then `order.markServed()` (`SERVED`), `customer.startEating(10)` (`EATING`), **`Ledger.recordServed()`** (one order served, whatever its size).
12. `Waiter.serve` formats `order.getItem().getName()`: *"Waiter serves Salad + Pasta Combo to C3"*.

## Turns 10–12 — eating and payment

13. On turn 11, **`Restaurant.finishEating(11)`** sees `servedTurn 10 < 11` → `Customer.finishEating(11)` → `READY_TO_PAY`.
14. On turn 12, **`Cashier.collectPayment`** → **`Restaurant.collectNextPayment(12)`** → `peekNextPayer` (needs `readyToPayTurn < 12`) → `order.markPaid()`, `customer.pay()` (records satisfaction 64), **`Table.release()`**, **`Ledger.recordPayment(2430, 64)`**. The ledger adds integer cents and never looks at the dish.

## Display

15. **`ConsoleUI.showWelcome`** prints `"  - " + item` for each `menu.getItems()` entry, so the combo appears as an ordinary fifth line.
16. **`ConsoleUI.showState` → `describeOrder(order)`** = `item.getName() + " " + order.describeStatus() + ", bill " + Money.format(billCents)`. `showFinalResult` uses the same helper per customer.

---

## Does any class need to change?

**No change needed anywhere.** The exact transcript above shows the full C3 journey working on the merged code.

| Class | Change? | What it does with the `MenuItem` |
|---|---|---|
| `MenuItem` | no change needed | base contract: name, price, units |
| `ComboMeal` | already written | supplies derived values through `super(...)` |
| `Menu` | no change needed | stores `ComboMeal` as a `MenuItem` (upcast at `add`) |
| `GameConfig` | no change needed | registration only |
| `Order` | no change needed | one `MenuItem` field, `getPreparationUnits()` drives progress |
| `OrderStatus` | no change needed | `PLACED/PREPARING/READY` unchanged |
| `Chef` | no change needed | prints `getPreparationUnits()`, spends one unit per turn |
| `Staff` | no change needed | one task per turn, independent of dish size |
| `Waiter` | no change needed | `getItem().getName()` only |
| `Cashier` | no change needed | moves the customer, not the dish |
| `Restaurant` | no change needed | `takeOrder`/`prepare`/`serve`/`priceOrder` all speak `MenuItem` |
| `Customer` / `CriticCustomer` | no change needed | choice returns a `MenuItem`; Critic scans by price |
| `Table` | no change needed | holds a `Customer`, never a dish |
| `Ledger` | no change needed | adds the locked `billCents` |
| `ConsoleUI` | no change needed | formats whatever `toString()`/`getName()` give it |
| `Host`, `HappyHour` | untouched | unrelated to combos |

**The call that makes it work:** the upcast at `Menu.add(MenuItem)` is the only "polymorphic" moment. Afterwards, `Order.prepareOneUnit()`'s `item.getPreparationUnits()`, `Chef`'s `order.getItem().getPreparationUnits()` and `Restaurant.priceOrder`'s `item.getPriceCents()` are virtual calls on a `MenuItem` reference. `ComboMeal` **overrides none of them** — the calls resolve to `MenuItem`'s inherited getters, which read the fields that `ComboMeal`'s constructor filled. So the combo works because its distinct values are *already stored in the base object*, not because anything is special-cased at the call site. That is exactly requirement 5.

## Anything that breaks or displays badly?

Nothing functional. Two observations, neither requiring a change:

- **Layout only.** A combo name is ~20 characters longer than "Pasta", so the table row's bill column shifts right. The width specifier (`%-25s`) is applied to the customer's *state*, not the dish, so nothing is truncated or misaligned in a broken way — it just makes the line longer. If you ever want strict columns, the smallest fix is in **`ConsoleUI.describeOrder`** (shorten/abbreviate the dish there); not needed now.
- **Counting semantics.** `Ledger.recordServed()` counts one combo as one served order, and `recordPayment` adds one revenue figure. That matches "a combo is one order" in the brief. If a marker instead expects a combo to count as two dishes served, the only file that would change is **`Ledger`** (or the `recordServed()` call in `Restaurant.serve`) — but I would not change it without checking the brief's wording, and the current behaviour is the natural reading.


## Turn 8 — Prompt

Before I run anything, build a verification table I can check by hand.

Rows: Salad, Burger, Pasta, Salad + Burger Combo, Salad + Pasta Combo.
Columns: components, list price in cents, list price formatted, preparation units, VIP price, VIP price with Happy Hour active, probability a Regular picks it, probability a VIP picks it, whether the Critic picks it.

Rules:
- money is integer cents; apply each percentage with Money.percentOf (round half-up)
- VIP discount is 10% off, then Happy Hour 20% off, in that order
- the Critic picks the single most expensive entry (first one wins a tie)

Also show:
- the arithmetic for both combo prices
- the arithmetic for one VIP + Happy Hour combo
- what the same values would be if someone used double and cast to int instead of percentOf, and whether any row changes
- any row where "discount each component then add" gives a different answer from "add then discount"

Flag anything in my rules that is ambiguous, and tell me what the code should do in that case.

## Turn 8 — Reply

## Verification table

Every money cell is `cents (formatted)`. "VIP + HH" assumes the order is **taken while Happy Hour is active**.

| Item | Components | List price | Formatted | Units | VIP (−10%) | VIP + HH (−10%, then −20%) | P(Regular) | P(VIP) | Critic? |
|---|---|---|---|---|---|---|---|---|---|
| Salad | — | 900 | $9.00 | 1 | 810 ($8.10) | 648 ($6.48) | 1/5 = 20% | 1/5 = 20% | no |
| Burger | — | 1500 | $15.00 | 1 | 1350 ($13.50) | 1080 ($10.80) | 1/5 = 20% | 1/5 = 20% | no |
| Pasta | — | 1800 | $18.00 | 2 | 1620 ($16.20) | 1296 ($12.96) | 1/5 = 20% | 1/5 = 20% | no |
| Salad + Burger Combo | Salad, Burger | 2160 | $21.60 | 2 | 1944 ($19.44) | 1555 ($15.55) | 1/5 = 20% | 1/5 = 20% | no |
| Salad + Pasta Combo | Salad, Pasta | 2430 | $24.30 | 3 | 2187 ($21.87) | 1750 ($17.50) | 1/5 = 20% | 1/5 = 20% | **yes** |

Probabilities sum to 1 in both random columns (5 × 0.2). The Critic's column has exactly one "yes": the most expensive entry.

## Combo price arithmetic

```
Salad + Burger:  (900 + 1500) = 2400
  percentOf(2400, 90) = (2400 * 90 + 50) / 100
                      = (216000 + 50) / 100
                      = 216050 / 100 = 2160        -> $21.60

Salad + Pasta:   (900 + 1800) = 2700
  percentOf(2700, 90) = (2700 * 90 + 50) / 100
                      = (243000 + 50) / 100
                      = 243050 / 100 = 2430        -> $24.30
```

Both are exact in cents before the `+50`; the offset is discarded by the integer division, which is what you want.

## One VIP + Happy Hour combo, step by step

Take the Salad + Pasta Combo (2430):

```
VIP:          percentOf(2430, 90) = (218700 + 50) / 100 = 218750 / 100 = 2187  -> $21.87
Happy Hour:   percentOf(2187, 80) = (174960 + 50) / 100 = 175010 / 100 = 1750  -> $17.50

Exact value before rounding: 2187 * 0.8 = 1749.6  -> half-up = 1750
```

Effective discount versus the components' 2700: 2430 is 90%, 2187 is 81%, 1750 is 64.8% (1749.6 rounded up). So a VIP combo in Happy Hour is roughly a third off the two dishes bought separately.

## Same values with `double` and a cast to `int`

`(int)` truncates toward zero; `percentOf` adds 50 and truncates, which is half-up for positive amounts. They agree unless the true fractional part is ≥ 0.5 — and floating point can nudge a value either way near an exact integer.

| Item | VIP via `(int)(p * 0.9)` | VIP + HH via `(int)(p * 0.9 * 0.8)` | Matches `percentOf`? |
|---|---|---|---|
| Salad | 648 → 648 | 648.0000000000001 → 648 | yes |
| Burger | 1350 → 1350 | 1080.0000000000002 → 1080 | yes |
| Pasta | 1620 → 1620 | 1296.0000000000002 → 1296 | yes |
| Salad + Burger Combo | 1944 → 1944 | 1555.2000000000003 → **1555** | yes |
| Salad + Pasta Combo | 2187 → 2187 | 1749.6000000000001 → **1749** | **no — 1 cent low** |

Also checked the combo prices themselves: `(int)(2400 * 0.9)` = 2160 and `(int)(2700 * 0.9)` = 2430, exactly the same, because the products land on whole numbers and the representation error is far below half a ULP.

**So exactly one row changes: Salad + Pasta Combo, VIP + Happy Hour, $17.49 instead of $17.50.** That is the only place in this menu where a real half-cent rounding decision occurs; with `double` you silently get truncation and lose a cent. It would also be lost if someone used `Math.round` on a value that floating point had pushed to 1749.4999…, which is the usual failure mode. Integer cents remove the question.

## "Discount each component then add" vs "add then discount"

Interpreted as: `percentOf(salad, 90) + percentOf(combo partner, 90)` versus `percentOf(salad + partner, 90)`.

| Combo | Each component, then add | Add, then discount | Same? |
|---|---|---|---|
| Salad + Burger | 810 + 1350 = 2160 | 2160 | yes |
| Salad + Pasta | 810 + 1620 = 2430 | 2430 | yes |

**No row differs**, and the reason is structural: every base price is a multiple of 100 cents, so 90% of each is a whole number of cents, and whole cents add the same either way.

They would differ for prices that are not multiples of 10. Example: two items at 905 each.

```
Add then discount:  percentOf(1810, 90) = (162900 + 50) / 100 = 1629
Each then add:      815 + 815 = 1630        (percentOf(905, 90) = 814.5 -> 815)
```

One cent apart. If the menu ever gains a price like $9.05, the two rules stop being interchangeable, so the brief's wording — "90% of the sum" — should stay the documented rule. The code does add-then-discount, which matches it.

## Ambiguities in your rules, and what the code should do

1. **When does Happy Hour apply — order time or payment time?** Your column says "with Happy Hour active", which is undefined if the order is taken during Happy Hour but paid after it ends. The code applies it in `Restaurant.priceOrder` at `takeOrder`, and the bill is frozen there, so a VIP combo ordered on an active turn pays $17.50 even if Happy Hour has ended by the time the Cashier charges. **Keep it: take-order time.** That is also what makes "the bill is locked" meaningful.

2. **How many roundings when two discounts stack?** Sequential (`round(round(base × 0.9) × 0.8)`) or one combined 72%? Your rule says in that order, which implies sequential, and the code does sequential. On this menu both give the same answer for all five rows; they can differ on other numbers. **Keep sequential and state it explicitly**, because it is the only version that matches "apply each percentage with `Money.percentOf`".

3. **"The Critic pays full price" versus Happy Hour.** If "full price" means *always*, the Critic should ignore Happy Hour; if it means "no VIP-style discount", Happy Hour still applies. The code does the latter: `CriticCustomer` inherits `priceFor` (full price) but `Restaurant.priceOrder` still calls `happyHour.applyTo(...)` afterwards. **The code should apply Happy Hour to Critics**, because Happy Hour is a restaurant-wide promotion and only the customer-specific discount is what "full price" describes. Worth one sentence in the README so a marker does not read it the other way.

4. **Rounding granularity of the combo discount.** "90% of the sum" (code, add-then-discount) versus "90% of each, then sum". Identical today, different in general, as shown above. **Keep add-then-discount**, which is the literal reading and also the only one that guarantees the combo's single `priceCents` is the number the customer is charged.

5. **Tie for "most expensive".** "First one wins" — first in `Menu`'s display order, which is Salad, Burger, Pasta, then the two combos. So if a combo ever ties a dish, the **dish** wins, and if the two combos tie, Salad + Burger wins. Today's prices never tie (2430 > 2160 > 1800). **No change needed; document the rule as "earliest in the menu wins"**, which is what `>` in the Critic's loop already does.

6. **What "probability" means.** It is per customer, per order, not per game: each customer independently draws 1 of 5, so a run can show 0, 1 or 4 combos. Seed 2002 happens to give 2 combos among C1, C2, C4, C5, C6. **Do not use "2 in 5" as a per-game assertion** in your test notes; assert the draw, not the frequency.

7. **Stacking VIP on a combo at all.** The combo is already 90% of the components, so a VIP combo is 81% of the components and a VIP combo in Happy Hour is 64.8%. Nothing in the brief forbids it, and `VipCustomer` discounts "the bill", which is the combo price. **Keep it**, but if a marker asks "is that double discounting?", the answer is yes by design: the combo discount is a pricing rule, the VIP discount is a customer entitlement, and they are applied in that order.

8. **Happy Hour and customers already seated.** An order taken during Happy Hour gets the discount regardless of when the customer arrived or sat down. That follows from rule 1; worth one line in the README so it is not mistaken for "only new arrivals".

Nothing in this table requires a code change. The only value I would change is the wording in the README about the Critic and Happy Hour (item 3).


## Turn 9 — Prompt

Now give me a way to verify the code without JUnit (the project has no test framework).

Write a small self-contained check program in package restaurant.entity (same package, so it can use package-private members if needed), with a main method. It must verify:
- the ComboMeal name for both combos is exact
- prices are 2160 and 2430 cents
- preparation units are 2 and 3
- getComponents() returns the two original objects in order and is unmodifiable
- constructing with a null component throws IllegalArgumentException
- Menu.findByName is case-insensitive and throws for an unknown name
- the menu has exactly 5 entries in the expected order
- a CriticCustomer picks the Salad + Pasta Combo
- an Order for a combo needs 3 preparation units and is not READY after 2

Print one PASS/FAIL line per check and exit with a non-zero status if any fail.

Give me the exact file path, the full file, and the javac/java commands for both Windows (run.bat style) and Git Bash. Tell me where to put it so it does not become part of the submitted game source if the assignment does not want test code.

## Turn 9 — Reply

## File path

```
stage-3/check/restaurant/entity/ComboMealCheck.java
```

It sits **outside `src/`**, so `run.bat`, `run.sh` and the manual `javac src` build never compile it. It is still declared `package restaurant.entity`, which is what lets it call the package-private `Order(...)` constructor and `Order.prepareOneUnit()`.

## Full file

```java
package restaurant.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import restaurant.control.GameConfig;

/**
 * Standalone checks for the combo meal feature. This is not part of the game: it has its own
 * {@code main}, lives outside {@code src/} and is compiled into a separate folder, so the
 * normal build never sees it.
 *
 * <p>Compile and run from {@code stage-3/}:
 * <pre>
 * javac -cp out -d check-out check/restaurant/entity/ComboMealCheck.java
 * java -cp "out;check-out" restaurant.entity.ComboMealCheck
 * </pre>
 *
 * <p>Prints one PASS or FAIL line per check and exits with status 1 if any check failed.
 */
public final class ComboMealCheck {

    private static final List<String> EXPECTED_MENU_ORDER = List.of(
            "Salad",
            "Burger",
            "Pasta",
            "Salad + Burger Combo",
            "Salad + Pasta Combo");

    private static int checksRun;
    private static int checksFailed;

    private ComboMealCheck() {
    }

    /**
     * Runs every check against the real menu and the real entity classes.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        Menu menu = GameConfig.baseSetting(2002).createMenu();
        MenuItem salad = menu.findByName("Salad");
        MenuItem burger = menu.findByName("Burger");
        MenuItem pasta = menu.findByName("Pasta");

        ComboMeal saladBurger = new ComboMeal(salad, burger);
        ComboMeal saladPasta = new ComboMeal(salad, pasta);

        // ----- Name, price and preparation units -----
        check("Salad + Burger Combo name is exact",
                "Salad + Burger Combo".equals(saladBurger.getName()));
        check("Salad + Pasta Combo name is exact",
                "Salad + Pasta Combo".equals(saladPasta.getName()));
        check("Salad + Burger Combo costs 2160 cents", saladBurger.getPriceCents() == 2160);
        check("Salad + Pasta Combo costs 2430 cents", saladPasta.getPriceCents() == 2430);
        check("Salad + Burger Combo needs 2 units", saladBurger.getPreparationUnits() == 2);
        check("Salad + Pasta Combo needs 3 units", saladPasta.getPreparationUnits() == 3);

        // ----- Components -----
        List<MenuItem> firstComponents = saladBurger.getComponents();
        check("components are the two originals, in order",
                firstComponents.size() == 2
                        && firstComponents.get(0) == salad
                        && firstComponents.get(1) == burger);
        List<MenuItem> secondComponents = saladPasta.getComponents();
        check("second combo's components are the two originals, in order",
                secondComponents.size() == 2
                        && secondComponents.get(0) == salad
                        && secondComponents.get(1) == pasta);
        checkThrows("components list cannot be modified", UnsupportedOperationException.class,
                () -> saladBurger.getComponents().add(burger));

        // ----- Null components -----
        checkThrows("null first component is rejected", IllegalArgumentException.class,
                () -> new ComboMeal(null, pasta));
        checkThrows("null second component is rejected", IllegalArgumentException.class,
                () -> new ComboMeal(salad, null));

        // ----- Menu lookup and contents -----
        check("findByName ignores case", menu.findByName("sAlAd") == salad);
        checkThrows("findByName rejects an unknown name", IllegalArgumentException.class,
                () -> menu.findByName("Sushi"));

        List<MenuItem> items = menu.getItems();
        check("menu has exactly 5 entries", items.size() == 5);
        List<String> names = new ArrayList<>();
        for (MenuItem item : items) {
            names.add(item.getName());
        }
        check("menu order is " + EXPECTED_MENU_ORDER, names.equals(EXPECTED_MENU_ORDER));
        check("entry 4 is the Salad + Burger Combo at 2160 cents",
                items.get(3).getName().equals("Salad + Burger Combo")
                        && items.get(3).getPriceCents() == 2160);
        check("entry 5 is the Salad + Pasta Combo at 2430 cents",
                items.get(4).getName().equals("Salad + Pasta Combo")
                        && items.get(4).getPriceCents() == 2430);

        // ----- The Critic still picks the most expensive entry -----
        MenuItem criticChoice = new CriticCustomer(3).chooseItem(menu, new Random(1));
        check("Critic picks the Salad + Pasta Combo", criticChoice == items.get(4));

        // ----- A combo order needs three units and is not ready after two -----
        Order order = new Order(new RegularCustomer(1), saladPasta, saladPasta.getPriceCents(), 1);
        check("combo order needs 3 preparation units", order.getUnitsRemaining() == 3);
        order.prepareOneUnit();
        order.prepareOneUnit();
        check("combo order is not READY after 2 units", order.getStatus() != OrderStatus.READY);
        check("combo order has 1 unit left after 2 units", order.getUnitsRemaining() == 1);
        check("combo order is PREPARING after 2 units", order.getStatus() == OrderStatus.PREPARING);
        order.prepareOneUnit();
        check("combo order is READY after 3 units", order.getStatus() == OrderStatus.READY);

        // ----- Result -----
        System.out.println();
        if (checksFailed == 0) {
            System.out.println("ALL " + checksRun + " CHECKS PASSED");
            System.exit(0);
        }
        System.out.println(checksFailed + " OF " + checksRun + " CHECKS FAILED");
        System.exit(1);
    }

    private static void check(String label, boolean passed) {
        checksRun++;
        if (!passed) {
            checksFailed++;
        }
        System.out.println((passed ? "PASS  " : "FAIL  ") + label);
    }

    private static void checkThrows(String label, Class<? extends Throwable> expected, Runnable action) {
        try {
            action.run();
            check(label, false);
        } catch (Throwable thrown) {
            boolean rightType = expected.isInstance(thrown);
            check(label, rightType);
            if (!rightType) {
                System.out.println("      expected " + expected.getSimpleName()
                        + " but got " + thrown.getClass().getSimpleName());
            }
        }
    }
}
```

## Commands

**Windows `cmd` (the same style as `run.bat`)**, from `stage-3`:

```bat
if exist out rmdir /s /q out
(for /r src %%f in (*.java) do @echo "%%f") > sources.txt
javac -d out @sources.txt
del sources.txt

javac -cp out -d check-out check\restaurant\entity\ComboMealCheck.java
java -cp "out;check-out" restaurant.entity.ComboMealCheck
```

**Windows PowerShell**, from `stage-3`:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
javac -cp out -d check-out check\restaurant\entity\ComboMealCheck.java
java -cp "out;check-out" restaurant.entity.ComboMealCheck
```

**Git Bash**, from `stage-3`:

```bash
javac -d out $(find src -name "*.java")
javac -cp out -d check-out check/restaurant/entity/ComboMealCheck.java
java -cp "out:check-out" restaurant.entity.ComboMealCheck
echo "exit status: $?"
```

The only platform difference is the classpath separator — `;` on Windows, `:` on Git Bash — because `out` and `check-out` are two separate roots.

## Where it lives, and keeping it out of the submission

- Keep it in **`stage-3/check/`**, not `stage-3/src/`. Nothing in the normal build references `check/`, so `run.bat`, `run.sh` and the README's `javac -d out $(find src …)` never compile or ship it.
- Add `check-out/` (and optionally `out/`) to `.gitignore` so the compiled checker never gets committed.
- If the assignment must contain **only** game source, delete `check/` and `check-out/` before zipping; the checks are reproducible from this conversation, and your real evidence of behaviour is the `test-runs/stage3-*.log` transcripts.
- If the marker *likes* seeing tests, keep `check/` in the repo but out of `src/` and mention it in `TEST_NOTES.md` — it is still not part of the program.

## Result of running it

I copied your current `ComboMeal.java` and `Menu.java` into a full compiled copy of `origin/main` (your branch still lacks `Main`, `ConsoleUI` and `HappyHour`), then ran exactly the commands above:

```
PASS  Salad + Burger Combo name is exact
...
PASS  combo order is READY after 3 units

ALL 23 CHECKS PASSED
run exit=0
```

23 checks, zero failures, exit status 0. To see the failure path, change any expectation (for example `== 2161`); that check prints `FAIL`, the summary prints `N OF 23 CHECKS FAILED`, and the process exits 1, so it can gate a script with `&&`.

Two notes for the record: the counters are `static` and mutable, which is the one place the "final fields" style rule is deliberately not followed (a running total cannot be final). And the Critic check passes `new Random(1)` on purpose — `CriticCustomer.chooseItem` ignores the random source, so the seed must not affect the result.


## Turn 10 — Prompt

I ran the game with the Stage 3 victory input and seed 2002. Here is the real terminal output (pasted below). Please:
1. Check that both combos appear where expected.
2. Confirm C3 ordered the Salad + Pasta Combo and that it took 3 Chef turns.
3. Check the bills against the pricing table, including the VIP + Happy Hour order.
4. Check the final result (paid customers, revenue, average satisfaction) against the expected Stage 3 result.
5. Point out anything inconsistent, and if something is wrong, give the smallest fix and which file it goes in.

Only use the output I pasted; do not invent lines. If the output is too short to decide something, say exactly which lines you need me to re-run and capture.

[paste terminal output]

## Turn 10 — Reply

The terminal output didn't come through — your message ends at the literal text `[paste terminal output]`, so there are no lines for me to check. I won't reconstruct it from memory, because then I'd just be grading my own expectations.

There's a second problem worth flagging. The checkout I can see still cannot build:

```
app\Main.java:        False
boundary\ConsoleUI.java: False
entity\HappyHour.java:   False
```

`git status` also shows only `ComboMeal.java` and `Menu.java` modified plus the new `check/` folder, still at `d439b3c`. So a run from `C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3` is not possible yet. If you ran it from another copy — a teammate's tree, a separate clone of `main`, or an IDE project — that's fine, but tell me which, so the transcript I analyse is the code we've been discussing.

## What to paste

Easiest is the whole transcript:

```bat
run.bat --echo < test-runs\stage3-victory.txt > my-run.log 2>&1
```

```bash
./run.sh --echo < test-runs/stage3-victory.txt > my-run.log 2>&1
```

Then paste `my-run.log`, or attach it. If you'd rather keep it short, these are the minimum spans for your five questions:

| Your question | Lines I need |
|---|---|
| 1. Both combos on the menu | the welcome block, first ~10 lines (`Menu:` through the five `- ` entries) |
| 2. C3's combo and 3 Chef turns | every line containing `C3`, from `Arrival: C3` to `Cashier collects … C3` |
| 3. Bills, incl. VIP + Happy Hour | C2's `Waiter takes C2's order … Bill locked at …`, C2's payment line, C5's order and payment, C6's order |
| 4. Final result | the `End of turn 18:` line, the `GAME OVER` block with the three `[ MET ]/[MISSED]` rows, and the per-customer list under `Customers:` |
| 5. Anything inconsistent | the above is enough; no extra lines needed |

## What I'll be checking against, so you can eyeball it meanwhile

These are expectations from the table we built, not observations:

- Menu: 5 entries, in order Salad, Burger, Pasta, Salad + Burger Combo ($21.60, 2 units), Salad + Pasta Combo ($24.30, 3 units).
- C3: `chooses Salad + Pasta Combo ($24.30, 3 units)`, then exactly three Chef lines — `unit 1 of 3`, `unit 2 of 3`, `unit 3 of 3 … READY`.
- C2 (VIP, Happy Hour active): order bill `$12.96`, payment `$12.96`.
- C3 pays `$24.30`; C5 pays `$21.60`; C6 ends `READY`, unpaid.
- Final: `paid 5`, `revenue $76.86`, `avg satisfaction 83.8`, all three targets `[ MET ]`.

Send the log and I'll do the five checks line by line.


## Turn 11 — Prompt

Update the project documentation for my part.

1. In README.md, give me the exact text/diff for:
   - the menu table (add both combos with price and units)
   - the combo choice rule (5 entries, 2-in-5 chance, Critic picks the most expensive)
   - the design overview row for MenuItem/ComboMeal/Menu
   - the Stage 2 -> Stage 3 new-files list (ComboMeal.java, plus the Menu.findByName change)
2. Find any comment or Javadoc that still says the Critic always orders Pasta, and give me the corrected sentence (CriticCustomer.java is the likely place).
3. Give me git commands to create a branch called feature/combo-meal, stage only my files, commit with a clear message, and push it.
4. Suggest a folder layout under docs/ai-history/combo-meal/ for the exported AI conversation (prompts and replies, in order) plus a one-paragraph note saying what the conversation covers. Do not write the note as a substitute for the transcript.

Show diffs, not whole files, except where a whole file is short.

## Turn 11 — Reply

## 1. README.md — all four sections are already correct

I checked each against the file at HEAD (`d439b3c`). All four exist, and all four match the brief and the verified run, so **there is no diff to apply**. Here is the exact committed text. (These blocks arrived in the earlier Stage 3 documentation commit `0fe2039`; your `Add combo meal` commit touched only `ComboMeal.java` and `TEST_NOTES.md`.)

**Menu table** (lines 102–108) — correct: 2160 and 2430 with units 2 and 3.

```markdown
| Item | Price | Preparation units |
|---|---|---|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Burger Combo | ($9 + $15) x 0.9 = $21.60 | 1 + 1 = 2 |
| Salad + Pasta Combo | ($9 + $18) x 0.9 = $24.30 | 1 + 2 = 3 |
```

**Combo choice rule** (lines 110–114) — correct, and the C1–C6 list matches the run verified earlier.

```markdown
**Combo choice rule.** A combo is an ordinary entry on the menu. Regular and VIP customers pick
uniformly from all five entries, so each customer has a 2-in-5 chance of choosing a combo. The
Critic's rule picks the Salad + Pasta Combo because it is the most expensive entry. With seed 2002:
C1 Salad, C2 (VIP) Pasta, C3 (Critic) Salad + Pasta Combo, C4 Salad, C5 Salad + Burger Combo,
C6 Salad + Pasta Combo.
```

**Design overview row** (line 137) — correct: it states the *is-a* relationship and that price and units are derived.

```markdown
| | `MenuItem`, `ComboMeal`, `Menu` | What can be ordered. A `ComboMeal` *is a* `MenuItem` made of two other items, with price and units derived from them. |
```

**Stage 2 -> Stage 3 new-files list** (lines 195–204) — `ComboMeal.java` is there.

```markdown
**New files**

- `entity/Host.java`: a new `Staff` that implements `Assignable` and can only seat.
- `entity/Seating.java`: seating options and the `seat` task, moved out of `Waiter` so the Host and
  the Waiter share one implementation.
- `entity/ComboMeal.java`: a `MenuItem` subclass composed of two items. It derives its price
  (90%, rounded) and its preparation units from them.
- `entity/HappyHour.java`: the once-per-game policy (activation, remaining turns, 20% discount,
  15-point recovery).
- `test-runs/stage3-*.txt` / `.log`: reproducible Stage 3 victory and defeat.
```

**One correction to your request:** `Menu.findByName` should **not** go in the *new files* list. `Menu.java` already existed in Stage 2; only a method was added, so it belongs under **Changed files**, where it already is (line 218):

```markdown
- `entity/Menu.java`: new `findByName`, so combos can be built from existing items.
```

Putting it under "New files" would claim a file was added that was not. If you want the link to be stronger, the smallest honest edit is to extend that changed-files line rather than add a new-files entry:

```diff
-- `entity/Menu.java`: new `findByName`, so combos can be built from existing items.
+- `entity/Menu.java`: new `findByName` (case-insensitive, throws for an unknown name), so
+  `GameConfig.createMenu()` can build combos from the existing dishes instead of repeating prices.
```

That is optional; the current line is not wrong.

## 2. The stale Critic Javadoc

Grep found exactly one stale source comment: `stage-3/src/restaurant/entity/CriticCustomer.java` lines 8–11. I applied the fix:

```diff
--- a/stage-3/src/restaurant/entity/CriticCustomer.java
+++ b/stage-3/src/restaurant/entity/CriticCustomer.java
@@ -7,8 +7,9 @@ import java.util.Random;
  *
  * <p>Order preference: a critic always reviews the kitchen's showpiece, so they order the
  * most expensive item on the menu (the earlier-listed item wins a tie) and ignore the
- * random source. With the standard menu this is always Pasta, the dish that takes the
- * Chef longest, so a critic is the hardest customer to keep happy.
+ * random source. With the Stage 3 menu this is the Salad + Pasta Combo, the entry that
+ * takes the Chef longest (three preparation units), so a critic is the hardest customer to
+ * keep happy.
  */
```

The corrected sentence on its own:

> With the Stage 3 menu this is the Salad + Pasta Combo, the entry that takes the Chef longest (three preparation units), so a critic is the hardest customer to keep happy.

Two places that look stale but are **not**, so leave them alone:

- `README.md` line 91 already distinguishes the stages: "With the Stage 2 menu this is Pasta; with the Stage 3 menu it is the Salad + Pasta Combo."
- `TEST_NOTES.md` lines 28 and 81 say the Critic orders "(Pasta)" — but both sit inside the **Stage 2** section, which documents the Stage 2 menu. Rewriting them would falsify history.

One optional follow-on, not applied: the class summary says the Critic "pays full price", which a marker could read as "exempt from Happy Hour". If you want it airtight, change it to "pays full price (no customer discount; Happy Hour still applies)". Your call — it is a one-line edit in the same file.

## 3. Git commands

Your combo commit `d439b3c` is already merged upstream (`c7afd88`), so branch from `origin/main` rather than from the stale local branch — that way the branch compiles (it has `Main`, `ConsoleUI`, `HappyHour`) and the diff contains only your follow-up work.

```bash
cd "C:/Users/lee_j/OneDrive/Documents/GitHub/Restaurant-Rush"
git fetch origin
git switch -c feature/combo-meal origin/main
```

Your uncommitted edits to `ComboMeal.java`, `Menu.java` and `CriticCustomer.java` come across with the switch, because those files are identical in both commits. If git refuses (it should not), use `git stash` before the switch and `git stash pop` after.

Stage **only** these, by path — never `git add .`, which would sweep in `check/`, `out/`, `check-out/`, `sources.txt` and regenerated logs:

```bash
git add stage-3/src/restaurant/entity/ComboMeal.java
git add stage-3/src/restaurant/entity/Menu.java
git add stage-3/src/restaurant/entity/CriticCustomer.java
```

Optionally include the checker if you want it in the repo:

```bash
git add stage-3/check/restaurant/entity/ComboMealCheck.java
```

Check before committing:

```bash
git diff --cached --stat
git status --short
```

Commit and push:

```bash
git commit -m "Harden and document ComboMeal for Stage 3" \
            -m "Reject null components, document the derived price and units, correct the Critic Javadoc for the five-entry menu, and add a standalone ComboMealCheck outside src so the normal build ignores it."
git push -u origin feature/combo-meal
```

PowerShell does not understand `\` line continuation; use one `-m` per line instead:

```powershell
git commit -m "Harden and document ComboMeal for Stage 3" -m "Reject null components, document the derived price and units, correct the Critic Javadoc for the five-entry menu, and add a standalone ComboMealCheck outside src so the normal build ignores it."
```

Then open the PR against `main`. `README.md` is unchanged, so it should not appear in the commit at all — if it does, something else edited it.

## 4. AI history folder

```
docs/ai-history/combo-meal/
├── README.md
├── 01-setup-and-trace.md
├── 02-requirements-and-design.md
├── 03-combomeal-implementation.md
├── 04-menu-findbyname.md
├── 05-gameconfig-registration.md
├── 06-no-change-trace.md
├── 07-pricing-verification-table.md
├── 08-combomealcheck.md
├── 09-run-log-review.md
├── 10-documentation-and-git.md
└── attachments/
    ├── stage3-victory.log
    └── combomealcheck-output.txt
```

Each numbered file holds **one exchange**, in this shape — prompt verbatim, then reply verbatim, nothing paraphrased:

```markdown
# 03 — ComboMeal implementation

## Prompt

<the exact text you sent>

## Reply

<the exact text that came back>
```

If your group prefers separate files per side, use `01-prompt.md` / `01-reply.md` pairs instead; one file per exchange is easier to read in order. The zero-padded numbers make `ls` sort chronologically. Put the raw terminal transcript and checker output in `attachments/` and reference them from `09` rather than pasting them twice.

`docs/ai-history/combo-meal/README.md` — the scope note, one paragraph:

> This folder records the AI-assisted work on the ComboMeal part of Stage 3: mapping the brief's eight combo requirements to the code, comparing subclass, list-wrapper and flag-field designs, implementing `ComboMeal` and `Menu.findByName`, registering the two combos in `GameConfig.createMenu()`, proving that `Order`, `Chef`, `Waiter`, `Cashier`, `Restaurant` and the UI need no changes, and building the hand-checkable pricing table and standalone `ComboMealCheck`. Each numbered file contains one prompt and the reply that followed it, verbatim and in chronological order; `attachments/` holds the raw run log and checker output. This paragraph states the scope only — it is a summary, not a substitute for the transcript, and the numbered files are the record.

Two practical notes: keep the transcript unedited (typos included) so it reads as a genuine record, and confirm with your group whether the module expects the log in the repository or only on request — if it is not wanted in the submission, keep this folder in the repo but out of the zipped deliverable, the same way as `check/`.


## Turn 12 — Prompt

Last step: help me make sure I can defend this code.

Quiz me on the ComboMeal part, one question at a time. Wait for my answer before giving feedback, then ask the next question. Cover:
- why inheritance from MenuItem was the right call
- how price and preparation units are derived and where rounding happens
- why the bill is locked at order time
- how the Critic ends up choosing the combo without any type checks
- why the seeded customer choices changed in Stage 3
- what would break if ComboMeal had its own Order or its own cooking logic
- one question about the exact cents arithmetic for a VIP combo during Happy Hour

After 8 questions, give me a short list of the points I explained well and the points I should review again. Do not just give me the answers up front.

## Turn 12 — Reply

We'll do 8, one at a time. Answer in your own words — no need for polish. I'll give feedback on each, then a short "solid / review" list at the end. I won't reveal any answers along the way.

---

**Question 1 of 8 — Why inheritance**

A marker says: *"Subclassing `MenuItem` is the lazy choice; composition would have been cleaner."*

Defend `ComboMeal extends MenuItem` in two or three sentences. Name the concrete requirement or code fact that breaks under the composition alternative — not just "inheritance is fine here".
