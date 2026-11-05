# Restaurant Rush — Key Ideas from AI Discussions

Prepared for review: 5 November 2026 (Asia/Singapore).

## Editorial note

This document is an edited, ideas-only summary of the supplied `Restaurant_Rush_Chat_History.md`, originally exported on 31 October 2026. It is not a verbatim conversation transcript. The prompts below are reproduced from that file; the responses are newly written summaries of the design ideas discussed.

The supplied transcript includes AI-generated Java examples and Mermaid diagram source. Those examples have been omitted here to focus on concepts. Their omission does not mean the original assistance was limited to ideas. This summary should accompany, rather than replace, the full transcript wherever the assignment requires complete prompts, replies and follow-up exchanges.

The original downloaded transcript has been retained. This draft is saved locally as `ai_history_stage3chat.md` for review before any commit or push.

## Scope

Stage 3 Host design, shared seating, turn integration, and project class diagrams. The initial attachment acknowledgement and final export request are omitted because they contain no substantive design guidance. The six design exchanges retain their original order.

## 1. Host structure and shared seating

### user prompt

> I need to add a Host to Restaurant Rush who can seat one customer per turn, in addition to the Waiter’s task. Given the existing Staff, Assignable and Waiter classes, suggest how to structure Host and reuse the seating logic without duplicating code.

### AI response

Host and Waiter should be sibling subclasses of Staff. Staff supplies behaviour common to staff members, while Assignable provides the contract for roles the manager directs. Host should not inherit the Waiter's unrelated ordering and serving responsibilities.

Use a shared seating helper so both roles follow the same seating procedure. The initial response called this helper SeatingService; the later source-based discussion identified the project's actual helper as Seating. Keep seating coordination separate from each staff member's task allowance, and keep validation and state changes in the appropriate restaurant entity methods.

Each staff object needs its own allowance. A successful Host action consumes only the Host's task, leaving the Waiter's task available. Reuse the existing Staff budget instead of creating another flag in Host. Consume the task only after seating succeeds.

## 2. Inherited behaviour and role-specific responsibilities

### follow up prompt

> Can you explain which methods Host should inherit from Staff and which it needs to implement from Assignable? Also, show how Host and Waiter can share Seating while keeping their own task limits.

### AI response

Staff should hold common staff identity and task-budget behaviour. In the actual project, public methods such as getName() and startTurn() are inherited, while protected helpers allow subclasses to check and consume their task allowance.

Host supplies its own command help, available seating choices and task execution through Assignable. The Waiter has additional ordering and serving responsibilities, but both roles reuse Seating for seating options and command handling.

The shared helper does not hold the staff task budget. The Host and Waiter are separate Staff instances, so each has an independent task-used flag. This separates the question of whether a seating action is valid from whether the particular staff member still has a task available.

The original early reply used illustrative method names and instance-based helper examples. Later replies corrected the explanation to the repository's actual API and package-private static Seating utility. Those examples are omitted from this summary.

## 3. Turn integration and failed actions

### user prompt

> Help me integrate Host into GameController so the order is Host, Waiter, then Chef. The Waiter should be able to take an order from a customer seated by Host in the same turn. Check that invalid seating attempts do not consume the Host’s task, and explain the changes needed.

### AI response

Direct the staff in Host, Waiter, then Chef order. Reset each staff member's allowance at the start of the turn and direct all three through the same Assignable-based controller loop.

Calculate available tasks when each role acts, using the current restaurant state. The Waiter can then see a customer the Host has just seated, and the Chef can see an order the Waiter has just taken. Choices displayed at the beginning of the turn should not be treated as a permanently fixed list.

For seating, check the staff budget before attempting the action and consume it only after success. Validate arguments, customer identity, customer state and table availability before changing restaurant state. When an InvalidActionException occurs, the controller should explain the issue and let the same staff member choose again.

The original reply reported that the inspected Stage 3 source already supported this flow and needed no source changes. It described source inspection and stated that compilation and runtime checks could not be completed because javac was unavailable in that session. This summary does not claim new execution or verification.

## 4. Same-turn walkthrough and retry example

### follow up prompt

> Walk me through a turn where Host seats C1, Waiter takes C1’s order, and Chef starts cooking. Then explain what happens if I select an occupied table and retry with a free table in the same turn.

### AI response

At turn start, Host, Waiter and Chef each have a task available. Host seats C1 at a free table, updating the waiting queue, table occupancy and customer state. Only the Host's task is consumed.

The Waiter's choices are then calculated from the updated state. The Waiter can take C1's order, after which the Chef can advance that order by one preparation unit. A one-unit dish may become ready immediately, but serving must wait until a later turn because the Waiter has already acted.

If the Host first selects an occupied table, seating should be rejected before any queue, table or customer state changes. The Host's task remains available. The controller asks again, and a subsequent selection of a free table can succeed in the same turn. The Waiter and Chef then proceed normally.

The behavioural limit is one successful staff task per turn. An invalid attempt does not consume that allowance.

## 5. Planning the project class diagrams

### user prompt

> Inspect the project’s Java classes and help me create UML class diagrams showing the Entity-Control-Boundary structure, important attributes and methods, inheritance, interface implementation, and associations. Split them into a package overview, domain model, and control/boundary/staff view so they remain readable. Flag missing source files rather than guessing.

### AI response

Organise the diagrams into three related views:

- A package overview showing the application's Entity-Control-Boundary structure and package dependencies.
- A domain view showing restaurant state, customers, tables, orders, menu items and related policies.
- A control, boundary and staff view showing turn coordination, presentation, staff inheritance, Assignable and shared seating.

Derive names, members, visibility and relationships from the available declarations. Include important attributes and operations, and state when details or supporting utilities are omitted for readability. Identify unresolved references instead of inventing missing classes.

Distinguish class inheritance from interface realization and dependencies. Host, Waiter and Chef implement Assignable; Staff and Cashier do not. Seating is a package-private static utility. Objective.TargetCheck is a nested classifier, rather than a separate top-level source file. Shared MenuItems do not automatically imply exclusive ownership by a ComboMeal.

The original assistant reply reported creating editable Mermaid diagrams in a UML.md artifact and inspecting 31 Java files in that session. The diagram source and artifact-delivery material are omitted here. Those were substantive AI outputs in the original exchange, not merely suggestions. This draft does not independently verify their availability in the current checkout.

## 6. Checking diagram relationships and notation

### follow up prompt

> Check the diagrams against the available source code, especially the relationships between Staff, Assignable, Host, Waiter and Seating. Explain any corrections to the arrows, visibility symbols or multiplicities, and identify details that cannot be verified because files are missing.

### AI response

Use inheritance arrows from Host and Waiter towards Staff, and interface realization arrows towards Assignable. Show dependencies towards Seating because the roles call its static methods. Do not show Host inheriting from Waiter or Staff implementing Assignable.

The public getName() and startTurn() methods inherited from Staff can satisfy those interface requirements for Host and Waiter without redundant implementations.

Represent private fields with a minus sign, protected members with a hash sign, public members with a plus sign and package-private members with a tilde. Mark abstract and static operations appropriately. Seating's private constructor and package-private, final class declaration are separate facts from its static operations.

Apply multiplicities to associations where appropriate, rather than to inheritance, realization or utility dependencies. A staff member's one-task allowance is a behavioural constraint, not an association multiplicity. The controller's registered staff objects should also be distinguished from task counts.

Check Customer–Table and Customer–Order relationships against the lifecycle and stored references. Shared combo components need evidence of exclusive ownership before composition is appropriate. Diagram review should distinguish source inspection from successful compilation or visual rendering.

The original reply reported updating UML.md and finding no missing internal imports among 31 available Java files in that session. This is a summary of that reply, not a fresh verification of the current repository.

## Review status

Local draft only. No commit or push has been made for this revision. The existing GitHub version remains unchanged pending the user's review.
