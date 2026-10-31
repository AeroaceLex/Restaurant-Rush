\# AI Use and Communication History

\*\*Name:\*\* Lew En Xun  

\*\*Matric No.:\*\* U2522615A  

\*\*Course:\*\* SC2002 Object-Oriented Design \& Programming  

\*\*Project:\*\* Restaurant Rush  

\*\*Contribution:\*\* Stage 1 turn engine, testing



\## Note

I used AI mainly for planning Stage 1, clarifying rule interpretations, checking turn-flow logic, and suggesting test cases. I reviewed and verified all code and report content before using it in the project.



My package structure followed this organisation:



\- `src/restaurant/app`

\- `src/restaurant/boundary`

\- `src/restaurant/control`

\- `src/restaurant/entity`



The compiled output also reflected this structure under `out/restaurant/`.



\---



\## Prompt 1

\*\*User:\*\*  

I am starting an SC2002 Java project for a command-line restaurant management game. Stage 1 includes regular customers, menu, waiter, chef, cashier, two tables, waiting queue, satisfaction loss, abandonment, payment, and victory/defeat. How should I start this project?



\*\*AI:\*\*  

A good way to start is to build the smallest playable version first, then grow it gradually. For Stage 1, begin by identifying the core game loop and the main domain objects.



Suggested first steps:

1\. Write down the turn flow clearly before coding.  

2\. Identify key entities such as Customer, Table, Order, MenuItem, and Restaurant.  

3\. Separate game flow from data classes. For example, let one controller class run the turn order, while entity classes store state.  

4\. Keep input/output in a separate UI class so that game logic is not mixed with printing and scanning.  

5\. Implement a minimal version that can seat, order, cook, serve, and pay before adding edge cases like abandonment and invalid input.



This helps you get a working base early and makes debugging easier.



\---



\## Prompt 2

\*\*User:\*\*  

For this Java project, what packages or folders should I create if I want the code to stay organised? It is a command-line game and I may extend it later in Stage 2 and Stage 3.



\*\*AI:\*\*  

A clean structure would be to separate the program into packages based on responsibility. For example:



\- `app` – entry point such as `Main`

\- `boundary` – console input/output classes

\- `control` – controller and configuration classes

\- `entity` – domain objects such as customer, order, table, menu, and staff



This works well for a command-line object-oriented project because it keeps responsibilities clear and makes later extensions easier. For example, new customer types or staff roles can be added without mixing them into the UI or entry-point code.



\---



\## Prompt 3

\*\*User:\*\*  

I organised my project into `app`, `boundary`, `control`, and `entity`. Does this make sense for a Stage 1 restaurant game? If yes, what kinds of classes should go into each folder?



\*\*AI:\*\*  

Yes, that structure makes sense and is a good fit for an Entity-Control-Boundary style design.



You could organise it like this:



\### `app`

\- `Main`  

&#x20; Starts the program and wires the main objects together.



\### `boundary`

\- `ConsoleUI`  

&#x20; Handles all printing and user input.  

&#x20; Shows the current state, prompts for commands, and displays errors/results.



\### `control`

\- `GameController`  

&#x20; Runs the 18-turn loop and controls the order of events.

\- `GameConfig`  

&#x20; Stores setup data such as turns, tables, menu, and arrival schedule.

\- possibly helper classes for schedule or objectives later



\### `entity`

\- `Restaurant`  

&#x20; Holds the main restaurant state and validates transitions.

\- `Customer`

\- `Order`

\- `Table`

\- `Menu`

\- `MenuItem`

\- `Staff`, `Waiter`, `Chef`, `Cashier`

\- enums such as customer state or order status if needed



This layout should scale well when later stages add more roles and customer types.



\---



\## Prompt 4

\*\*User:\*\*  

What files/classes should I create first so I do not get stuck? I am worried about making too many classes too early.



\*\*AI:\*\*  

Start with only the classes needed to support one basic end-to-end scenario. A practical order is:



1\. `Main`  

2\. `GameController`  

3\. `ConsoleUI`  

4\. `Restaurant`  

5\. `Customer`  

6\. `Table`  

7\. `Order`  

8\. `MenuItem` / `Menu`  

9\. `Waiter`, `Chef`, `Cashier`



Then add supporting enums or helper classes only when the need becomes clear.



A good target for the first milestone is:

\- one customer can arrive

\- be seated

\- place an order

\- have the order prepared

\- be served

\- pay



Once that works, add satisfaction loss, queue logic, abandonment, invalid input handling, and final result checking.



\---



\## Prompt 5

\*\*User:\*\*  

I am implementing Stage 1 of a turn-based restaurant game in Java. The main flow each turn includes customer arrival, player actions for staff, automatic cashier payment, satisfaction changes, and checking for win/loss after 18 turns. Can you suggest a clean turn order for the controller so that the rules are consistent?



\*\*AI:\*\*  

A clean approach is to keep the controller responsible only for sequencing, while the restaurant/domain classes enforce the rules. A possible turn order is:



1\. Add the scheduled arriving customer for this turn to the waiting queue.  

2\. Display the current state to the player.  

3\. Let each staff member act in the required order.  

4\. Run automatic cashier payment if any customer is ready to pay.  

5\. Apply end-of-turn updates such as eating progress and satisfaction loss for customers still waiting for service.  

6\. Advance the turn counter.  

7\. After turn 18, evaluate victory or defeat.



This structure keeps turn timing explicit and makes rule-related bugs easier to reason about.



\---



\## Prompt 6

\*\*User:\*\*  

For a command-line restaurant management game, if a dish becomes READY during the Chef’s action in the current turn, should the Waiter be allowed to serve it in the same turn?



\*\*AI:\*\*  

Usually no, if the Waiter has already taken their action earlier in the same turn. In that case, a dish becoming READY during the Chef’s action should only be eligible to be served starting from the next turn. This makes the turn sequence easier to understand and avoids staff effectively acting twice in one round.



If your brief does not explicitly state otherwise, document this rule clearly and keep it consistent across gameplay and testing.



\---



\## Prompt 7

\*\*User:\*\*  

I need to decide how to handle invalid input in my Stage 1 game. If the player enters an invalid task like serving a customer with no order, should the staff member lose their turn?



\*\*AI:\*\*  

A good rule is that invalid input should not consume the staff member’s action. Instead:



\- show an error message explaining why the action is invalid

\- let the player choose again for that same staff member

\- only consume the task once a valid action succeeds or the player chooses to wait



This is user-friendly and avoids punishing input mistakes. It also matches many assignment-style interactive systems where rejected commands leave the game state unchanged.



\---



\## Prompt 8

\*\*User:\*\*  

In Stage 1, a customer arrives at the start of a turn. If they are still unserved by the end of that same turn, should they already lose satisfaction?



\*\*AI:\*\*  

Yes, that is a reasonable interpretation if satisfaction loss is checked at the end of every turn for customers still waiting for service. Under this rule, a newly arrived customer can lose satisfaction on their arrival turn if they remain unserved at end of turn.



The important thing is to apply the rule consistently and document it, because this affects reproducible outputs and sample scenarios.



\---



\## Prompt 9

\*\*User:\*\*  

Can you suggest test cases for Stage 1 of a restaurant game with waiter, chef, cashier, two tables, waiting queue, satisfaction loss, abandonment, payment, and victory/defeat?



\*\*AI:\*\*  

You can cover Stage 1 with a mix of normal-flow and edge-case tests:



1\. \*\*Basic victory run\*\*  

&#x20;  - seat customers efficiently  

&#x20;  - take orders, cook, serve, and collect payment  

&#x20;  - confirm that victory is reached when all targets are met



2\. \*\*Basic defeat run\*\*  

&#x20;  - delay service or idle staff  

&#x20;  - confirm that customers abandon and targets are missed



3\. \*\*Dish timing check\*\*  

&#x20;  - verify that a dish finished this turn is only served next turn



4\. \*\*Payment timing check\*\*  

&#x20;  - verify that a customer who becomes ready to pay this turn is charged next turn



5\. \*\*Invalid command handling\*\*  

&#x20;  - try serving before ordering, cooking before ordering, unknown commands  

&#x20;  - confirm state is unchanged and the same staff can choose again



6\. \*\*Queue and table handling\*\*  

&#x20;  - test seating when both tables are full  

&#x20;  - test that occupied tables cannot be reused until freed



7\. \*\*Revenue correctness\*\*  

&#x20;  - confirm only paid customers contribute to revenue  

&#x20;  - abandoned customers should not contribute



8\. \*\*Turn limit check\*\*  

&#x20;  - confirm game always ends after 18 turns and result is evaluated only then



Using scripted inputs and fixed random seeds will make these tests reproducible.



\---



\## Prompt 10

\*\*User:\*\*  

I want reproducible runs for testing my Java game. Is using a fixed seed enough, and what else should I record?



\*\*AI:\*\*  

A fixed seed is a good start, especially if any customer choices depend on randomness. For strong reproducibility, also record:



\- the exact input sequence used by the player

\- the game version or stage/tag being tested

\- the final outcomes, such as paid customers, revenue, and average satisfaction

\- important intermediate events if they are part of the rules, such as order status changes or abandonment



A practical setup is to store input scripts as text files and save the resulting console output as transcripts. That makes it easy to replay and compare runs after changes.



\---

