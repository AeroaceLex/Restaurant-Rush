1) I’m planning a Happy Hour feature for a restaurant game. It can be activated once, lasts two turns, gives a discount on new orders, and restores satisfaction for customers waiting for service. What design issues should I think about?

Answer:
A few timing and responsibility decisions will shape the feature:
- Track its state: Happy Hour can be used once, so it needs to remember whether it has been activated and how many active turns remain.
- Define when it starts and ends: A clear approach is to let the manager activate it at the start of a turn, apply its effects during that turn, then let it run for one more turn before it expires.
- Decide who gets satisfaction recovery: Apply recovery to customers still waiting for service, whether they are waiting for a table or have ordered but not yet been served. Customers who are eating or paying should not qualify.
- Set the discount order: If VIP customers also receive a discount, decide which discount is applied first. Apply both when the order is placed, then keep that bill fixed even after Happy Hour ends.
- Keep responsibilities clear: A HappyHour class could track availability and duration. The controller could handle when to offer activation, while the Restaurant applies the discount and satisfaction recovery through its existing rules.
For the project’s current design, the specific values are a 20% discount and 15 satisfaction points of recovery. It would also help to check the turn order carefully so the promotion lasts exactly two turns and cannot be activated again.

2) What are some pros and cons of putting Happy Hour’s activation and duration rules in a separate class rather than handling everything in the game controller?”

Answer:
A separate HappyHour class would keep the promotion’s state and rules together. It could track whether it has been used, how many turns remain, apply the discount, and describe its current status. That makes the controller’s turn loop easier to read and keeps promotion-specific logic out of the flow that coordinates the game.

The trade-off is that this adds another class and another object for the controller and Restaurant to coordinate. For a small feature, handling everything in the controller might seem simpler at first. But Happy Hour affects order prices and customer satisfaction as well as activation and duration, so putting all of that in the controller could make its turn logic crowded. A separate class for Happy Hour, with the controller deciding when to activate or advance it and the Restaurant applying its effects, is a reasonable balance.

3) Apply the VIP discount first, then the Happy Hour discount. That matches the project’s pricing rule: VIP pricing is calculated by the customer, and Happy Hour is applied to the resulting price.

Answer:
For an $18.00 Pasta order, the VIP discount makes it $16.20; the 20% Happy Hour discount then makes it $12.96. The order matters because each percentage is calculated from the price produced by the previous step. Calculate the final bill when the order is placed and store it with the order, so it doesn’t change when Happy Hour ends.

4) At what points in a turn should a limited-time promotion be activated, apply its effects, and expire? What timing edge cases should I consider?”

Answer:
A clear sequence is to handle Happy Hour at the start of the turn, before staff act:
1. Process arrivals, then let the manager choose whether to activate Happy Hour.
2. If it’s active, restore satisfaction for eligible customers who are still waiting for service.
3. Let staff take actions. Apply the discount to orders placed during this time, and lock each order’s final price.
4. At the end of the turn, reduce Happy Hour’s remaining duration. After its second active turn, it should no longer affect orders or customers.
Decide whether customers arriving on the activation turn qualify for recovery. In the project’s turn order, arrivals happen before activation and recovery, so a newly arrived customer can qualify if still waiting. Also check that recovery happens before the normal satisfaction penalty, that customers who have already been served don’t recover, and that the promotion cannot be activated again after it expires.

5) What scenarios should I manually check to make sure a once-per-game, two-turn promotion behaves as intended?”

Answer:
Manually check these cases:
- Activation: Start Happy Hour and confirm it shows two turns remaining. Try to activate it again while active and after it expires; it should not be offered again.
- Duration: Confirm the discount and recovery apply on the activation turn and the following turn, then stop.
- Order pricing: Place an order during Happy Hour and confirm the discount is applied after any VIP discount. Check that the bill stays fixed after Happy Hour ends. Also place an order before activation and confirm its price doesn’t change.
- Satisfaction recovery: Check that customers still waiting for a table or food recover 15 points at the start of each active turn. Customers who are eating, paying, or gone should not recover.
- Turn timing: Confirm recovery happens before the usual satisfaction loss, and check whether a customer arriving on the activation turn is eligible under the chosen turn order.
- Overall outcome: Compare otherwise identical runs with Happy Hour activated and unused. Check the bills, satisfaction totals, and final result.

