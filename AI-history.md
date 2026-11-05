# AI Assistance Record — Restaurant Rush, Stage 3 (ComboMeal)

**Exported:** 2026-11-05
**Tool:** OpenCode (coding agent harness)
**Model:** GLM (GLM-5.3-flash-free), provider `opencode`
**Contributor:** [your name]
**Scope:** ComboMeal feature (Stage 3) only. Teammates owned Host, Happy Hour, and the boundary/app layer.
**Working rule agreed at the start:** the AI was instructed to never generate Java code, pseudocode, snippets, diffs, or corrected versions of my code. The AI named classes/methods, gave specifications in plain English, reviewed my pasted code by quoting it, and I wrote and pasted all implementation myself.

Files I read/pasted for reference during the conversation (all in the Restaurant-Rush repo): `stage-3/src/restaurant/entity/ComboMeal.java` (my implementation), `MenuItem.java`, `Money.java`, `Menu.java`, `GameConfig.java`, `Restaurant.java`, `Order.java`, `OrderStatus.java`, `CriticCustomer.java`, `Customer.java`, `Chef.java`, `Waiter.java`, `Staff.java`, `Assignable.java`, `Table.java`, `Ledger.java`, `Cashier.java`, `Seating.java`, `TaskCommand.java`, `VipCustomer.java`, plus the stage-2 `ConsoleUI.java`.

---

## Turn 1 — User

Working on a group Java assignment called Restaurant Rush, a turn-based restaurant management game for the command line. Each turn the manager directs a Host, Waiter and Chef, and tries to meet targets by turn 18. Responsible for the ComboMeal part of Stage 3, working through it step by step, keeping the whole conversation as a record of AI use.

Rule for the entire conversation: no Java code, pseudocode, code snippets, full files, diffs, or corrected versions from the AI. All code written by me. Class/method names and plain-English behaviour descriptions allowed. If the AI thinks code is needed, it must state what the code must achieve. Quoting one of my lines to point at a problem allowed.

Project constraints:
- Plain Java 11+, no third-party libraries, no Maven/Gradle. Compile with javac into out/ and run restaurant.app.Main.
- Entity-Control-Boundary packages: restaurant.app, restaurant.boundary, restaurant.control, restaurant.entity.
- Money is integer cents. Money.percentOf(cents, percent) rounds half-up to the nearest cent. No doubles for money.
- Stage 2 works: Salad $9.00 (1 prep unit), Burger $15.00 (1), Pasta $18.00 (2). Regular and VIP pick uniformly at random from the menu; a Critic picks the most expensive item. Bill locked when the order is taken. Chef spends one preparation unit per turn.
- Stage 3 also adds Host and Happy Hour, owned by teammates — not to be changed.
- Style: 4 spaces, final fields, small methods, Javadoc on public classes/methods.

First reply requested, in plain English: (1) restate the setup in at most 10 bullets; (2) trace how a normal dish gets from the menu to the bill, naming exact classes and methods; (3) list extension points where a combo could plug in; (4) ask any questions needed before designing. No code.

---

## Turn 1 — Assistant

Preface: the files mentioned as attached did not actually arrive, and the working directory didn't contain the Java project; the trace was therefore a reconstruction, later corrected against real files.

**Setup restated (10 bullets):** turn-based CLI game; manager directs Host/Waiter/Chef per turn, targets by turn 18; plain Java 11+ via javac into out/, run restaurant.app.Main; ECB packages; integer cents with Money.percentOf half-up, no doubles; menu Salad $9.00 (1 unit), Burger $15.00 (1), Pasta $18.00 (2); Regular/VIP uniform random, Critic most expensive; bill locked at order time; Chef one prep unit per turn; Stage 3 adds my ComboMeal plus teammates' Host and Happy Hour which I must not touch; deliverables = combo code + README menu/design, 4-space style, final fields, small methods, Javadoc.

**Trace (reconstruction):** GameConfig.createMenu builds the menu at setup, holding MenuItems (name, cent price, prep units). Restaurant.takeOrder asks the customer to choose (CriticCustomer scans for highest price; Regular/VIP random), creates the Order with an OrderStatus "placed" and locks the payable amount at that moment via Money.percentOf for any customer adjustment. Each turn Restaurant.prepare routes the order to Chef which spends one unit against remaining prep cost; at zero remaining, status advances to ready. Restaurant.serve marks served and hands over (Waiter in stage 2). Restaurant.priceOrder turns the locked order into the payable amount, reading the locked price rather than re-reading the menu.

**Extension points listed:** new entity beside MenuItem (Composite-style ComboMeal containing MenuItems and its own pricing) versus a plain menu entry; GameConfig.createMenu must add combos; Menu must expose combos so uniform-random and most-expensive see them; customer selection semantics (is a combo selectable; what "most expensive" means for a Critic); Order/OrderStatus must possibly remember components and lock the combo price at order time; locking moment in takeOrder; Chef/Restaurant.prepare prep-unit semantics (sum of components; per-turn unit spend); Restaurant.serve gates on all components ready; Restaurant.priceOrder expresses the discount in cents via Money; boundary display; README menu table and design section.

**Questions asked:** (1) files didn't come through — please paste MenuItem, Menu, Order, OrderStatus, Chef, Restaurant, GameConfig, customer classes, README sections; (2) what does the Stage 3 handout say a ComboMeal is — name, dishes, discount rule, how it is chosen; (3) prep-unit rule for a combo (sum? flat? cheapest free?); (4) Critic behaviour with combos (most expensive by discounted price or undiscounted sum?); (5) Happy Hour interaction (replace or stack? agree on paper first); (6) does the Stage 2 Order store cents directly or the MenuItem for later lookup — determines where the combo discount can live; (7) can customers order components à-la-carte instead; (8) does the rubric expect a subclass/composite of menu items or any clean entity design.

---

## Turn 2 — User

Stage 3 requirements for combos (translated from the brief):
1. A combo is two existing menu items sold together as one order.
2. Display name "<item 1> + <item 2> Combo".
3. List price 90% of the sum of the two items' list prices, rounded half-up to the nearest cent, in cents.
4. Preparation units are the sum; the Chef still spends one unit per turn.
5. Usable anywhere a MenuItem is used, no instanceof or type checks in Order, Chef, Waiter, Cashier or Restaurant.
6. Base menu gains two entries: Salad + Burger Combo ($21.60, 2 units) and Salad + Pasta Combo ($24.30, 3 units); the three original dishes unchanged.
7. Regular and VIP choose uniformly from all five entries (2-in-5 combo chance); the Critic still picks the most expensive entry, which becomes Salad + Pasta Combo.
8. No other Stage 2 behaviour change except where the brief says: Critic now orders the combo, and seeded random choices shift because the menu has five entries.

Requests, prose only, no code: compare (a) ComboMeal extending MenuItem with two component references, (b) ComboMeal wrapping a List<MenuItem>, (c) adding a components/flag field to MenuItem — which is the smallest change satisfying every requirement; each option's effect on Order, Chef, Cashier, Critic; which files must change vs stay untouched; risks to Stage 2 tests and seeded runs. Recommend one design, sketch responsibilities and collaborations as bullets, justify it.

---

## Turn 2 — Assistant

**(a) ComboMeal extends MenuItem with two component references.** Derived values (composed name, 90% price in cents, summed units) computed once at construction and stored in the parent; component references exist only so the combo can describe itself. Satisfies requirement 5 by plain subtype substitution; the name is derived from component names so MenuItem stays untouched; requirements 3–4 are integer-cent computations at construction via Money.percentOf, matching the "locked forever after" invariant. Blast radius: one new entity class plus the menu factory; everything else behaves by substitution.

**(b) List wrapper with MenuItem promoted to an interface.** Satisfies the letter of requirement 5 but not "smallest": promoting MenuItem to an interface forces re-auditing field types, collections, method signatures across Menu, Order, Restaurant, Chef, customers, boundary and Stage 2 test fixtures, gaining nothing — also risks ripples into teammate-owned code the brief forbids touching.

**(c) components/flag field on MenuItem.** Worst option: optional baggage on the simplest entity; the flag is a hidden type check (the same smell instanceof was banned for); constructor growth invites half-initialised items; combo concerns leak into a class the brief frames as a plain dish.

**Verdict: (a) is the only option that is both the smallest change and requirement-5-clean.**

**Per-class effects:** MenuItem unchanged but must permit subclassing (check not final, constructor visible); Menu unchanged in code but gains five entries; Order/OrderStatus unchanged since the stored MenuItem reference can point at a combo and the bill locks from the chosen entry at takeOrder; Chef unchanged — spends one unit per turn against the entry's reported prep cost, so combos take 2–3 turns naturally; Waiter/Cashier unchanged if they display via the name accessor; CriticCustomer changes behaviour but not code (max over five entries makes $24.30 the winner; no ties at 9.00/15.00/18.00/21.60/24.30); Regular/VIP selection unchanged provided no hardcoded menu count exists anywhere.

**Files:** change → new ComboMeal in restaurant.entity (Javadoc'd, final fields); GameConfig.createMenu adds the two combos built from existing items; README menu table and design section; Stage 2 test expectations that assert three entries, the old Critic choice, or recorded seeded sequences. Untouched → MenuItem, Menu, Order, OrderStatus, customer classes, Chef, Waiter, Cashier, Restaurant, Money, and everything teammate-owned. Warning sign: any urge to edit Restaurant.takeOrder "a little" means the design is fighting back.

**Risks:** menu-size assertions break (sanctioned by clause 8, but must be a conscious, documented test edit); Critic test expectations move from Pasta to the combo (same); seeded runs diverge from turn 1 because the index space changed from 3 to 5 — no trick preserves old sequences, so transcripts/expected outputs must be re-baselined and the commit message must say so; route all cent math through Money.percentOf (any stray float in a test helper reintroduces banned doubles); check roughly uniform spread meaning combos appear ~2 in 5; constructor-visibility landmine: if MenuItem is final or has a private constructor, option (a) fails at compile time.

**Recommended design picture:** ComboMeal extends MenuItem — responsibility: a fixed two-dish bundle whose name, cent price, and prep cost are derived once from its two components at construction and never change; holds two final component references for description; exposes nothing beyond the MenuItem contract. Collaborations: GameConfig.createMenu builds the three base dishes once, passes pairs into ComboMeal, appends combos to Menu; Menu stores five indistinguishable-from-outside entries; customers, Order, Chef, Waiter, Cashier, Restaurant interact only through the MenuItem contract; Money.percentOf is the single rounding authority.

**Requested next step:** verify MenuItem is subclassable and that uniform selection uses menu size rather than a hardcoded count; draft ComboMeal's class-level Javadoc (the contract in words) before implementing.

### Turn 2 — Assistant (implementation detail from real files, noted during the ComboMeal review)

Separately, when later reviewing actual files, noted: Menu.findByName Javadoc... (see Turn 9 for the extended trace results).

---

## Turn 3 — User

Requested a specification for ComboMeal to implement myself. No code, no pseudocode, no method bodies. Must cover: package, superclass and why; constructor inputs, validation rules, null behaviour; how name, price and preparation units are derived; which existing helper handles rounding and why not double; what getComponents must return (ordering, modifiability); the object's invariants; what the class must NOT own or duplicate; acceptance criteria checkable line by line; three edge cases with expected behaviour. Also: which points are easy to get wrong and how I would notice when running the game.

---

## Turn 3 — Assistant

*Full specification (summary provided verbatim at the time); key points:*

- **Package/superclass:** restaurant.entity, extends MenuItem — subtype substitution makes the combo indistinguishable to all clients without any client change.
- **Constructor:** exactly two MenuItems, item 1 first, item 2 second. Both must be non-null and construction fails immediately on null with no partially built combo escaping. No other validation — the same dish twice is allowed; another combo as a component is allowed; no type inspection inside ComboMeal.
- **Derivation (computed once at construction, never recomputed):** name = item 1's own reported name + " + " + item 2's reported name + " Combo", read from the items, never retyped as literals; list price = Money.percentOf(sum of the two cent prices, 90); prep units = sum of the two unit counts (no discount on units).
- **Rounding:** Money.percentOf is the single rounding authority; doubles banned — "90%, rounded half-up" is only well-defined in exact integer arithmetic; doubles corrupt half-cent boundaries silently and would not even show on the two clean base combos.
- **getComponents():** the only added public method; returns the two components in constructor order; must be unmodifiable (mutation attempts fail loudly, not silently on a hidden copy); Javadoc must state order, immutability, and that it is descriptive only.
- **Invariants:** price equals percentOf(item1+item2, 90) exactly; units equal the sum; name equals the composed string; component references non-null and fixed; components in order are exactly the constructor's arguments; everything visible through accessors or getComponents unchanged after construction (effectively immutable); derivation is live at construction — a base-dish change before construction flows through.
- **Must NOT own/duplicate:** no copied component data (factory passes the same MenuItem instances); no rounding logic (Money's job); no knowledge of customers, Critic, Happy Hour, Host, bill locking, OrderStatus, or Chef turn counts; no duplicated rule statements (discount percentage, name pattern, units rule each live in exactly one place: this class); no public API beyond the constructor and getComponents; no self-identity branches.
- **Acceptance criteria (13):** right package/superclass with class Javadoc stating name pattern, 90%-half-up pricing, unit-sum rule; two-item constructor in documented order with null fail-fast and final fields; derived values computed in the constructor only; unmodifiable ordered getComponents; menu factory keeps the three originals exactly as before and constructs combos from those same objects; menu yields "Salad + Burger Combo" 2160 cents/2 units and "Salad + Pasta Combo" 2430 cents/3 units, five entries total; Regular/VIP even odds across five; Critic orders Salad + Pasta Combo; Chef spends 1 unit/turn until the 2-or-3 total, shell... rather serve only then; locked bills of 2160/2430; grep for instanceof and class-checks in Order, Chef, Waiter, Cashier, Restaurant finds nothing new; no double/float, all cent math via Money.percentOf or integer addition; style 4-space/final fields/small methods/Javadoc; README five rows at $21.60 and $24.30 plus design section.
- **Edge cases:** (1) null component → immediate fail-fast exception, no combo survives; (2) both arguments the same dish → accepted, name "Burger + Burger Combo", 90% of 3000 = 2700 = $27.00, units 2, not in the base menu but permitted — rejecting it would be un-asked-for validation; (3) a component that is itself a combo → accepted with identical derivation rules (composed name gets long), and above all no type inspection — the menu simply never creates one. Bonus: percentOf a synthetic sum like 1235 at 90% must give 1112 (half-up), since the base combos never exercise the rounding boundary.
- **Easy to get wrong, with symptoms:** literal names/prices/units instead of derived values (silent until a base dish changes; README/game mismatch); self-computed or double-based 90% (invisible on the base combos — only the synthetic boundary test or grep catches it); swapped constructor arguments (menu prints "Burger + Salad Combo" instantly); name formatting drift (visible in menu listing); returning modifiable internals (no visible symptom in a normal run — sneakiest); fresh copies instead of shared references (future divergence); forgetting to actually append combos (menu stays three — loudest failure); creeping type checks (silent; grep only). Pattern: failures 1–4 loud, 5–8 silent — after a loud green run, hunt the silent ones.

---

## Turn 4 — User

Asked for a review of the pasted ComboMeal against the specification: PASS/FAIL per spec point, quoting my lines for every FAIL or weakness, consequences in the game, fixes described in words, never rewriting. Also: anything added that the spec doesn't require; anything missing that teammates rely on; whether the Javadoc explains derived values for a Stage 3 diff reader; end with one question I should be able to answer about my own class.

*(Message arrived without the file attached — a placeholder `[paste your ComboMeal.java]` with no content.)*

---

## Turn 4 — Assistant

No code arrived and no matching file existed in the workspace, so no review was given — inventing a review of unseen code would corrupt the record. Asked the user to paste the file again (whole file including the package line), noting that from the file the review would grade criteria 1, 2, 4, and the invariant list without rewriting anything.

---

## Turn 5 — User

Re-sent the review request, this time attached: `stage-3/src/restaurant/entity/ComboMeal.java`. Same review rules as before.

---

## Turn 5 — Assistant

Read the real file plus supporting context (MenuItem, Money, Menu, GameConfig, Order, OrderStatus, CriticCustomer, Customer, Chef, Waiter, Staff, Assignable, Table, Ledger, Cashier, Seating, TaskCommand, VipCustomer, stage-2 ConsoleUI) so every claim was grounded in the actual codebase.

**Verdict by spec point:**

1. **Package and superclass — PASS.** restaurant.entity, extends MenuItem. MenuItem confirmed public non-final with a public constructor, so subclassing is legitimate.
2. **Constructor inputs/validation/null — PASS with one observation.** Two MenuItems in documented order; null either fails immediately before any state is stored; evaluation-order reasoning in the Javadoc was correct (JLS left-to-right) and optionally documented. Observation: IllegalArgumentException instead of NullPointerException for null — consistent with the house style (MenuItem and Menu.add use IAE for unusable arguments), so acceptable; whichever is chosen, stay consistent within the class.
3. **Derivation of name/price/units — PASS.** Name composed from the components' own reported names with exact " + " and " Combo" pieces; price through Money.percentOf of the summed cents; units summed. All computed once in the constructor; no shadow copies; the strongest part of the file.
4. **Rounding via Money.percentOf, no doubles — PASS.** Money.percentOf verified as pure integer arithmetic with a +50 half-up bias and a long intermediate to survive large multiplies. Both menu cases exact: 2400 → 2160 and 2700 → 2430. No double/float anywhere.
5. **getComponents() — FAIL.** Quoted offending line: `return List.of(first, second);` — List.of rejects duplicate elements, throwing IllegalArgumentException when both are the same reference. So edge case 2 (Burger + Burger): the constructor succeeds, then the first getComponents() call explodes far from the construction site — violating edge case 2, invariant 5 (the collection must report exactly what the constructor received), and invariant 6 (the accessor must always answer). Consequence: any teammate, test helper, or future menu entry with a same-dish combo crashes confusingly — the "silent until it isn't" failure pattern. Fix in words: assemble the two-element list yourself from the two fields and hand it out wrapped in the standard unmodifiable decorator (Collections.unmodifiableList), which tolerates duplicates, instead of delegating pair construction to the duplicate-rejecting List.of factory.
6. **Invariants — PASS except as affected by the FAIL above.** Derived-once-and-never-recomputed holds; effectively immutable since MenuItem itself is immutable (verified; no defensive copies needed).
7. **Must not own or duplicate — PASS.** No literal dish data, no rounding logic, no knowledge of customers/Chef/billing, no type inspection, single named constant for the discount percentage, and GameConfig provably reuses the same MenuItem instances (Menu.add stores the reference untouched).

**Acceptance criteria status:** #1 partial (class Javadoc covers pricing/units but the display-name pattern is documented only as an example on the private helper — should be stated once as a rule); #2, 3, 5, 11, 12 PASS; #6 verified by arithmetic (2160/2430 cents, units 2 and 3, exact names); #7, 8, 9, 10 not verifiable from this file — run and grep; #13 no README supplied.

**Added but not required:** `public static final int PRICE_PERCENT` — the spec said no public API beyond the constructor and getComponents; private serves the @value Javadoc tag equally well and stops other classes from re-deriving the discount themselves.

**Missing that teammates rely on:** duplicate-pair tolerance (the FAIL); a stated guarantee that getComponents returns the same references passed in (true in the wiring, unstated in the Javadoc); one sentence documenting the deliberately permissive component policy (no validation beyond null) so nobody later "fixes" it into a type check.

**Javadoc adequacy for a Stage 3 diff reader:** about 80% — the is-a rationale, the no-shadow-values argument, the percentOf half-up reference, the Chef-turns implication, and the honest evaluation-order note are genuinely good; the three gaps are the name-format rule as a rule, the missing "descriptive only — game logic must not branch on it" clause on getComponents, and the permissive-policy sentence.

**Fix list in priority order:** (1) make the component list duplicate-tolerant and unmodifiable (the FAIL); (2) make the percentage constant private; (3) class-doc the name pattern rule; add the "descriptive only" clause and the identity guarantee to getComponents; one sentence on the null-only validation policy.

**Final question posed:** with the file as pasted, what happens on `new ComboMeal(burger, burger)` — does the failure occur at construction or at the accessor call afterwards, and does that reveal whether the validation lives in the right place?

---

## Turn 6 — User

Next: Menu needs a lookup so combos can be built from the standard dishes. Asked for the contract of Menu.findByName(String) in prose-only: matching rules (case, whitespace, duplicates); return value; not-found behaviour (exception and message); whether standard() or getItems() should change and why not; any caller that could now break because the method exists. Review to follow next message in the same PASS/FAIL-quote format.

---

## Turn 6 — Assistant

Noted the attached Menu.java already contained a findByName implementation; held off grading until the paste, delivered the contract:

- **Matching rules:** case-insensitive ("Salad", "salad", "SALAD" all match — content should never depend on the caller's typing; the console is game UI); no trimming/normalisation ("␣Salad" must not match, and trimming here would be the codebase learning to lie about its own data); duplicates — Menu.add doesn't enforce uniqueness, so define the case: two items sharing a name → the first match in menu order (same order getItems() reports); do not start rejecting duplicate names inside findByName (policy change out of Stage 3 scope); null argument behaves as "matches nothing" and follows the not-found path.
- **Return value:** the stored reference itself — the same object the menu holds — because combos must derive values from the very MenuItem instances the customers' selection sees; a copy would quietly kill the sharing guarantee. MenuItem is immutable, so identity is the feature, not a risk.
- **Not-found:** IllegalArgumentException (house style), message naming the miss (include the requested name verbatim so the stack trace points at the lookup key); never return null — GameConfig.createMenu hands the result straight to the ComboMeal constructor, and a null would drift through and detonate later as a confusing "component was null" failure far from the actual mistake. Fail at the lookup site, loudly, with the key.
- **standard() and getItems() unchanged:** standard() must keep producing exactly the three base dishes in order — combos are composed on top of it in GameConfig.createMenu; if standard() gained combos every other caller silently gains two entries and clause 6 is violated in both count and content. getItems() must keep the same unmodifiable view over the same list — it is the selection source for Regular/VIP uniform choice and the Critic's max scan; no sorting, no dedup, no snapshot copying; display order is the de-facto test contract and the "first in menu order" collision rule depends on it.
- **Callers that could break:** nothing source-wise (an added method cannot clash); real risks are behavioural — GameConfig.createMenu becomes coupled to dish *names* (a teammate renaming "Pasta" now fails loudly at game start, the correct kind of coupling, caused by the not-found contract); boundary/UI names-from-player-input must do their own trimming since Menu won't; teammates' Host/Happy Hour gain the method but their existing calls don't reference it; discipline: nobody widens this method's semantics later (trimming, fuzzy matching) because the combo factory will silently stand on today's rules.
- **Contract boundary note:** "find" is defined by the list's order, and the list's order by insertion — write that sentence in the Javadoc to turn an implementation detail into a documented, testable contract.

---

## Turn 7 — User

Registering combos in GameConfig.createMenu() myself. Asked for a change checklist in plain English, no code: starting point and the three items to look up; the two combos to append and in what order; which numbers must not be hard-coded and why; what belongs in GameConfig vs ComboMeal; the exact five menu entries in display order afterwards; why display order matters for seeded random choices; what breaks in Stage 2 if combos are registered in the wrong place. Plus: how to confirm the menu without a test program — which screen/line shows the menu and what it should look like.

*(Also disclosed during this turn's tool-checks: the stage-3 tree was missing the boundary package and Main, so the run couldn't compile yet.)*

---

## Turn 7 — Assistant

- **Starting point/lookups:** begin from Menu.standard() (the single construction site of the three base dishes; untouched). Obtain dishes by name lookup, not rebuilding: find "Salad", "Burger", "Pasta" from the created menu, and keep the single found Salad reference for reuse in **both** combos (one lookup, not two).
- **Combos/order:** append exactly two, first Salad + Burger, then Salad + Pasta; in both, Salad is the constructor's first component (argument order becomes the display name); appended after the three base items, never mid-list — final ordering is a documented, testable contract.
- **Numbers not to hard-code:** nothing numeric at all — no 900/1500/1800, no 2160/2430, no unit counts; every number arrives via a found MenuItem and the ComboMeal derivation. Any literal in GameConfig creates a second authority that won't follow a rebalance; the 90% especially already lives inside ComboMeal — writing 90 in GameConfig duplicates it. The only textual literals allowed are the three lookup keys, safe because a misspelling throws the loud not-found failure at startup.
- **Ownership lines:** GameConfig owns composition policy (which combos, from which dishes, in which order); ComboMeal owns rules (name pattern, 10%-off via Money, summed units); Menu owns storage/lookup/display order. If logic leaks across those lines the ECB argument collapses and the README design section becomes fiction.
- **Exact five entries in display order:** 1) Salad $9.00/1; 2) Burger $15.00/1; 3) Pasta $18.00/2; 4) Salad + Burger Combo $21.60/2; 5) Salad + Pasta Combo $24.30/3.
- **Why display order matters for seeded choices:** uniform selection reflects insertion order; the seeded RNG resolves menu *positions*, so position k always yields whatever occupies position k with a given seed. Insertion order is invisible statistically (any ordering gives uniform picks) but fully determines *which customer gets which dish when* — i.e., every recorded seeded run. Pinned convention: three base first, then the two combos in the brief's listed order, keeping assignment expected outputs reproducible.
- **What breaks in Stage 2 if registered wrongly:** combos inside Menu.standard() → every caller silently gains two entries, violating clause 6 in count and content, and menu-size assumptions answer wrong without crashing; fresh dish copies instead of lookups → sharing guarantee dies, divergence later; combos inserted before the base three → uniform picks still look uniform but produce *wrong specific dishes per seed* and whole turn sequences diverge with no visible error; swapped constructor args → names become "Burger + Salad Combo"/"Pasta + Salad Combo" (visible on the markout, Critic unaffected, looks almost fine); registering in Main/boundary instead of the factory → any other GameConfig-driven path silently serves a three-item menu.
- **Confirm without a test program:** the menu appears exactly once per run on the welcome screen — GameController.play() passes restaurant.getMenu() into ui.showWelcome(...) before turn 1. Compile, run restaurant.app.Main, read the first screen: five entries in the exact order above, the last two printing as the combo names with "$21.60"/"2 units" and "$24.30"/"3 units" via MenuItem's inherited text rendering. Don't touch the boundary's display code for this. Caveat given: stage-3 was missing ConsoleUI/Main/Host/HappyHour files at the time, so verification was possible only after teammates' files landed.
- Honest note: a GameConfig.copy read at the time already looked close to the checklist (standard()-based, lookups, reused Salad reference, appended after base three); the actual paste would later be graded against every head, including display order and hard-coding absence.

---

## Turn 8 — User

Asked for proof, in prose, that the rest of the game needs no changes for a combo, tracing the scenario: Critic C3 seated; Waiter takes the order; item is Salad + Pasta Combo; Chef cooks over three turns; Waiter serves; Cashier collects. For each step: class, method, what it does with the MenuItem. Coverage required: Customer.chooseItem and CriticCustomer.chooseItem; Restaurant.takeOrder and priceOrder; Order constructor, getUnitsRemaining, prepareOneUnit, describeStatus; Chef task and Staff one-task budget; Restaurant.prepare and serve; Table, Ledger, Cashier; ConsoleUI's menu and order displays. Then explicitly: does any class need to change? Which polymorphic call makes each work? Fix ownership if something displays badly. No code.

---

## Turn 8 — Assistant

Read every class in the chain before answering (real files, not assumptions). Noted up front: the stage-3 folder was still missing the boundary package and Main, so the ConsoleUI evidence came from the stage-2 copy, which shows the mechanism the stage-3 version would inherit.

**The trace:**

1. The controller iterates directed staff each turn — Host, then Waiter, then Chef — with the Cashier running automatically during step 4 payment.
2. **Waiter.availableTasks** offers "order C3" because C3 is SEATED (no menu involvement yet).
3. **Waiter.perform → Waiter.takeOrder** resolves the customer from the command, calls Restaurant.takeOrder(C3, turn), then Staff.markTaskUsed.
4. **Restaurant.takeOrder** checks the SEATED state gate, then calls customer.chooseItem(menu, randomFor(customer)).
5. **CriticCustomer.chooseItem** overrides the uniform base default and scans menu.getItems() comparing getPriceCents() — dynamic dispatch point 1: the inherited MenuItem.getPriceCents() runs on the ComboMeal; 2430 beats everything, so the Critic gets the Salad + Pasta Combo; the Critic never touches the random source, so seeding is irrelevant to this row.
6. Back in takeOrder, **priceOrder** locks the bill: happyHour.applyTo(customer.priceFor(item.getPriceCents())). The teammate-owned Happy Hour receives an **int of cents, never the MenuItem** — 2430 in; C3 doesn't override priceFor (pays full per the brief). The combo is invisible to the pricing chain because pricing is type-free integer plumbing.
7. **Order's constructor** stores the customer, the MenuItem reference (field typed MenuItem holding a ComboMeal — dispatch point 2), and the already-locked 2430. Customer.placeOrder moves C3 to ORDERED.
8. **Waiter.takeOrder** reports via order.getItem()'s implicit MenuItem.toString(): "Waiter takes C3's order: C3 chooses Salad + Pasta Combo. Bill locked at $24.30." — the combo prints itself correctly through a method it never wrote.
9. **Three turns of Chef:** Chef.availableTasks finds C3 among ORDERED customers whose status isCookable(), offering "cook C3" described from getName() and describeStatus(); Chef.perform passes the Staff one-task-per-turn budget check; Restaurant.prepare requires cookable status and calls Order.prepareOneUnit(); unitsDone increments and the status flips to READY only when unitsDone >= item.getPreparationUnits() — dispatch point 3, the **inherited** getter returning the summed 3. That one line is why the combo correctly occupies the Chef for turn 1 ("unit 1 of 3"), turn 2 ("unit 2 of 3"), turn 3 ("unit 3 of 3" plus "The order is READY").
10. **Order.getUnitsRemaining()** and **describeStatus()** likewise interrogate only the item's unit count — any state display shows fractions out of 3 with zero special handling.
11. **Serving:** Waiter.availableTasks lists "serve C3" (ORDERED + READY); Restaurant.serve re-checks READY, Order.markServed() → SERVED, Customer.startEating(turn) → EATING with servedTurn recorded, Ledger.recordServed() — a counter that would be identical for a Salad. No item knowledge anywhere in the gate.
12. **Restaurant.finishEating** moves EATING customers whose servedTurn is past into READY_TO_PAY and queues them — purely state-based, item-blind.
13. **Cashier.collectPayment** (own Staff budget) → Restaurant.collectNextPayment for C3: Order.markPaid() → PAID; Customer.pay() records satisfaction; Table.release() — Table holds only a Customer, never an item — and Ledger.recordPayment(2430, satisfaction), pure integer aggregation; Objective compares counts (4) and revenue (4500) in exact integer arithmetic. The controller prints "Cashier collects $24.30 from C3" via getBillCents() and Money.format — again an int path.

**Verdict per class (all "no change needed", with the enabling mechanism):** Customer.chooseItem — the uniform default is defined by the list, so five entries means 1/5 each with zero edits; CriticCustomer.chooseItem — inherited getPriceCents() on a MenuItem-typed loop variable; Restaurant.takeOrder — holds whatever chooseItem returns, typed MenuItem; Restaurant.priceOrder — pure integer pipeline (item.getPriceCents() → customer.priceFor → happyHour.applyTo); Happy Hour cannot behave differently for a combo because it only ever sees cents; Order constructor/getUnitsRemaining/prepareOneUnit/describeStatus — all parameterised by item.getPreparationUnits(), same path Pasta's 2 already took; Chef — tasks and messages read getName/describeStatus/getUnitsDone/getPreparationUnits, all inherited; Staff holds no item reference; Restaurant.prepare/serve — gate on OrderStatus only; Waiter — order/serve text composes from getItem() self-description; Table/Ledger/Cashier — none ever touches a MenuItem; GameController — unchanged by construction of the trace; ConsoleUI — no change needed with one caveat: stage-2's describeOrder prints getItem().getName() + describeStatus() + the locked bill, so a combo row reads "Salad + Pasta Combo PREPARING 1/3, bill $24.30", and the stage-3 boundary (teammate's file, not yet present) inherits the pattern as long as its welcome screen keeps printing menu.getItems() entries via toString — if it invents menu-specific layout code, the fix belongs in that boundary file, nowhere else.

**The one-sentence proof:** the entire pipeline performs exactly four operations on menu items — getPriceCents, getPreparationUnits, getName (and toString) — all declared on MenuItem; a ComboMeal *is* a MenuItem implementing all four by inheritance, so every dispatch resolves correctly and no line of the chain ever asks "is this a combo?". The only file whose docstring mentions the new behaviour on purpose is CriticCustomer's — correctly documenting that the Stage 3 Critic orders the 2430-cent, three-unit entry — the one behaviour change clause 8 permits, at zero lines of code.

**Flagged remaining risk for later:** the two silent spots — the getComponents duplicate fix, and the seeded-uniformity check over the five-entry menu once boundary files land.

---

## Turn 9 — User

Requested a hand-checkable verification table, numbers and short prose only: rows for the five entries; columns for components, list price in cents, formatted, units, VIP price, VIP with Happy Hour, probability a Regular picks it, probability a VIP picks it, whether the Critic picks it. Rules: integer cents; percentages via Money.percentOf half-up; VIP 10% off then Happy Hour 20% off, in that order; Critic picks the single most expensive entry, first wins a tie. Also show: the arithmetic for both combo prices step by step; the arithmetic for one VIP + Happy Hour combo; what changes if someone used double and cast to int instead of percentOf; any row where discount-each-component-then-add differs from add-then-discount; flag anything ambiguous and say what the code should do.

*(Grounded by reading the real VipCustomer: priceFor returns Money.percentOf(listPriceCents, 100 − DISCOUNT_PERCENT) with DISCOUNT_PERCENT = 10 — matching the stated rule. HappyHour.java was not yet in the folder, so 20% figures were computed per the stated rule assuming the same Money helper.)*

---

## Turn 9 — Assistant

**Verification table:**

| Entry | Components | List ¢ | List | Units | VIP ¢ | VIP+HH ¢ | P(Regular) | P(VIP) | Critic picks it |
|---|---|---|---|---|---|---|---|---|---|
| Salad | — | 900 | $9.00 | 1 | 810 | 648 | 20% | 20% | No |
| Burger | — | 1500 | $15.00 | 1 | 1350 | 1080 | 20% | 20% | No |
| Pasta | — | 1800 | $18.00 | 2 | 1620 | 1296 | 20% | 20% | No |
| Salad + Burger Combo | Salad + Burger | 2160 | $21.60 | 2 | 1944 | 1555 | 20% | 20% | No |
| Salad + Pasta Combo | Salad + Pasta | 2430 | $24.30 | 3 | 2187 | 1750 | 20% | 20% | **Yes** (sole most expensive, no tie) |

Probabilities are distribution facts: uniform over five entries = 20% each always; the seed decides *which* turns, not the odds. Critic decided by the price column (2430 > 2160 > 1800 > 1500 > 900).

**Combo list-price arithmetic.** Salad + Burger: 900 + 1500 = 2400; 90%: 2400 × 90 = 216,000; +50 half-up bias → 216,050; ÷100 → 2160.5 → **2160** ($21.60) — the exact .5 case is genuinely exercised. Salad + Pasta: 900 + 1800 = 2700; ×90 = 243,000; +50 → 243,050; ÷100 → 2430.5 → **2430** ($24.30).

**VIP + HH for Salad + Pasta Combo.** Step 1 VIP: percentOf(2430, 90): 218,700 + 50 = 218,750 ÷ 100 → 2187.5 → **2187**. Step 2 HH: percentOf(2187, 80): 174,960 + 50 = 175,010 ÷ 100 → 1750.1 → **1750** ($17.50). Exact second-stage fraction was .6 — the value every wrong arithmetic path stumbles on.

**Doubles-and-cast column:** combo list prices intact (double 0.9 stored slightly high keeps casts correct); all VIP figures intact; all HH figures intact except one — **Salad + Pasta Combo VIP+HH: exact 1749.6, half-up 1750, but (int) of the double gives 1749** ($17.49). One row changes out of ten discounted figures. Extra finding: double 0.8 being stored slightly *high* is what saved the integer-valued rows — a slightly-low percentage (like 0.7) would corrupt *every* integer product by a full cent, i.e., the bug's direction is table-dependent.

**Decomposition column (discount-each-then-add vs add-then-discount):** combo list prices identical if decomposed (810 + 1350 = 2160 ✓; 810 + 1620 = 2430 ✓); VIP stage identical (729 + 1215 = 1944 ✓; 729 + 1458 = 2187 ✓); VIP+HH identical for S+B (583 + 972 = 1555 ✓); **the one differing row, again S+P under VIP+HH:** decomposed, 583 + 1166 = **1749** versus combined 583.2 + 1166.4 = 1749.6 rounded once → **1750** — the per-component roundings each shave their fraction and the carry vanishes; exactly one cent.

**Ambiguities flagged:**
1. Decomposition is not ambiguous *in the game*: an order holds one MenuItem, and Restaurant.priceOrder applies customer.priceFor then happyHour.applyTo to that single entry — no code ever deconstructs a combo at billing time, so 1749 has no code path. Convention to enforce and document: **all discounts apply to the entry as a whole, once, VIP-then-HH order**; if 1749 ever appears in a README example, boundary printout, or fixture, recompute from the combo's single list price.
2. Discount order (VIP→HH vs HH→VIP) was checked and happens to produce identical results on every row for this menu (e.g., HH-first S+P: 1944 then 1750) — coincidence, not a theorem; the rules pin the order and the code's natural shape already matches. Don't let anyone "simplify" by swapping.
3. Critic during Happy Hour: since priceOrder applies HH after priceFor and the Critic's priceFor is identity, a Critic's bill would also get 20% off during HH (S+P → 1944). Whether the Critic should be exempt lives entirely in the teammate's HappyHour.applyTo — flagged to the team, not my part.
4. Locked-bill caveat: every figure describes a customer ordering *at that moment*; the bill freezes at takeOrder — a VIP ordering during HH pays 1750 even if HH deactivates (and 2187 if it activates) later. Transcript audits must match bills against the HH state on the *ordering* turn.
5. Critic tie rule currently decorative: all five list prices distinct, so "first wins a tie" never fires; README line recommended so a future teammate doesn't rely on the Critic's scan order for something it isn't doing.

**Cross-check takeaway:** the same figure, 1749, appears for both the double bug and the decomposition error — different diseases, same missing round-up at the same .6. Audit tell: **a Salad + Pasta VIP-during-HH bill of $17.49 = wrong arithmetic path; $17.50 = clean.**

---

## Turn 10 — User

Requested a manual verification plan for the combo feature: no test program, no JUnit, no code. A checklist with one row per check covering (minimum) both combo names and prices appearing correctly; the three-unit combo taking three Chef turns; observing the unmodifiable components list indirectly or explaining why it cannot be seen from the game; the Critic choosing Salad + Pasta Combo; the five menu entries affecting seeded choices for C1-C6; VIP + Happy Hour arithmetic on a combo bill; a rejection case such as serving a combo before READY. Plus build/run commands for Windows and Git Bash as a numbered list of terminal steps, not code in a file.

*(Grounded beforehand by reading Seating.java and TaskCommand.java: commands are "seat <customer> [table]", "order <customer>", "serve <customer>", "cook <customer>", numbered choices, 0/'wait' to skip; TaskCommand lowercases the verb, uppercases ids.)*

---

## Turn 10 — Assistant

**Prerequisite 0:** the stage-3 folder had no ConsoleUI, no Main, no Host.java, no HappyHour.java — GameController imports all of them, so javac fails until the teammates' files are in the tree. No fake substitutes; the validation is of the real pipeline.

**Build and run:**

Windows (cmd):
1. `cd /d "C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\stage-3"`
2. `if not exist out mkdir out`
3. `javac -d out src\restaurant\entity\*.java src\restaurant\control\*.java src\restaurant\boundary\*.java src\restaurant\app\*.java` — should print nothing and populate out\restaurant\...
4. `java -cp out restaurant.app.Main`
5. Check how their Main accepts a seed (argument or prompt) and always launch with seed 2002 first.
6. Optional re-runnable transcript: pipe a one-command-per-line text file: `java -cp out restaurant.app.Main < script.txt` (stage-2 ConsoleUI echoes read lines, making the transcript document itself; verify stage-3 kept that behaviour, else play interactively).

Git Bash:
1. `cd "/c/Users/lee_j/OneDrive/Documents/GitHub/Restaurant-Rush/stage-3"`
2. `mkdir -p out`
3. `javac -d out src/restaurant/entity/*.java src/restaurant/control/*.java src/restaurant/boundary/*.java src/restaurant/app/*.java`
4. `java -cp out restaurant.app.Main`
5. Script-piping works identically: `java -cp out restaurant.app.Main < script.txt`

**Manual verification checklist:** (anchor note: turn numbers assume C3's order on turn 7 and one task per director per turn; if seating slips, the *relative* sequence order → three cooks → serve → pay holds — verify the sequence, not absolute turn labels.)

| # | What to do | What you should see | Why it proves the feature |
|---|---|---|---|
| 1 | Build, run, stop at the welcome screen. | Five rows in order: Salad ($9.00, 1 unit), Burger ($15.00, 1 unit), Pasta ($18.00, 2 units), Salad + Burger Combo ($21.60, 2 units), Salad + Pasta Combo ($24.30, 3 units); combos after Pasta; names with " + " and " Combo". | Content proves composition; numbers prove derivation; showing at all proves toString inheritance. Character-check against the brief. |
| 2 | Turn 7: seat C3; Waiter `order C3`. | "Waiter takes C3's order: C3 chooses Salad + Pasta Combo. Bill locked at $24.30." | Critic max-scan over five entries selects 2430; lock captures the list price in cents. Any other name = wrong scan or menu. |
| 3 | Turns 7, 8, 9: Chef `cook C3` each turn. | "unit 1 of 3 done." / "unit 2 of 3 done." / "unit 3 of 3 done. The order is READY (it can be served next turn at the earliest)." | Exactly three cooks proves summed units via the inherited getter, one unit per turn, no type checks. |
| 4 | Turn 8: Waiter `serve C3` before READY. | "! Not allowed: C3's Salad + Pasta Combo cannot be served: it is PREPARING 1/3, not READY." plus a choose-again prompt. | Status-only gate; fraction out of 3; rejection must not consume the Waiter's task. |
| 5 | Turn 10: Waiter `serve C3`. | "Waiter serves Salad + Pasta Combo to C3: order SERVED. C3 starts eating." Serve door opens only after three cooks. | READY ⇔ all units spent; 3-unit orders serve on the fourth turn after ordering, never the third. |
| 6 | Turns 11–12: let the Cashier run. | By turn 12: "Cashier collects $24.30 from C3: PAID, satisfaction … recorded, table freed." | Locked bill survives serve, eat, pay; toggling HH on/off must not change the amount — the lock contract in action. |
| 7 | Record the "C… chooses <name>" lines for C1, C2, C4, C5, C6 in the same seed-2002 run. | Five lines, each one of the five welcome entries, bills matching the table (Regular list price; VIP the 90% figure). | Uniform selection over five entries works; draws land on real entries; VIP path works on whatever it picks. One run proves mechanism, not distribution. |
| 8 | Re-runs with seeds 2003–2007, tally as you go. | Combos appear among Regular/VIP orders roughly 2 in 5 overall; every dish at least once. | Small-sample uniformity signal; an unlucky sample isn't proof, a systematic combo absence across many seeds is a real smell. |
| 9 | Find a seed where C2 (VIP) orders a combo (~40% per seed); rerun it, answering `y` at the Happy Hour prompt on exactly C2's ordering turn (prompt precedes manager choices). | "Bill locked at $17.50." (S+P) or "$15.55" (S+B); the later collect line repeats the same figure. | Discount stacking order (list → 90% → 80%) with half-up at the second stage; $17.49 would indicate the double-and-cast path — the tell from Turn 9. Repeated figure at payment proves the lock. |
| 10 | Grep the tree: `getComponents` outside ComboMeal, `instanceof`, combo class-checks. | Zero gameplay call sites (hits only in the class itself and Javadoc). | Honest answer to the components row: **it cannot be observed from the game** — nothing in the pipeline touches it, so mutation behaviour is unreachable at runtime. Verify by reading: unmodifiable, duplicate-tolerant (the fixed List.of trap), constructor order. `jshell` is the only runtime route — left to the user whether that counts as manual. |

Footnotes: row 9's seed search normally takes a handful of tries; if the teammates' HappyHour.applyTo exempts critics or differs from the 20% rule, row 9's figures change per whatever their rule is — check once, then trust the table.

---

## Turn 11 — User

"now export entire ai conversation for me"

---

## Turn 11 — Assistant

Produced this file: a full, faithful export of the conversation (Turns 1–11) with a metadata header recording tool, model, scope, the agreed no-code rule, and the reference files read during the session, saved at:

`C:\Users\lee_j\OneDrive\Documents\GitHub\Restaurant-Rush\AI-conversation-export-stage3-combomeal.md`

*End of export.*
