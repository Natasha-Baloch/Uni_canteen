# Testing Examples

## A. Functional tests
| # | Steps | Expected result |
|---|---|---|
| 1 | Login owner / owner123 on **Owner** tab | Owner panel opens, dashboard cards show numbers |
| 2 | Login owner / owner123 on **Customer** tab | "This is an owner account" warning |
| 3 | Wrong password | "Wrong username or password" |
| 4 | Register new customer, then login | Rows appear in STUDENTS and USERS |
| 5 | Register with an existing username | Message: value already exists (ORA-00001) |
| 6 | Customer: pick category Drinks | Only drink cards are shown |
| 7 | Customer: type "burger" in search | Only burger items shown |
| 8 | Add Zinger x2 + Cola x1, cart total | Rs 780 (2x350 + 80) |
| 9 | Place order with Cash | ORDERS row PENDING, PAYMENTS row PENDING/CASH, appears in owner dashboard |
| 10 | Place order with Online | PAYMENTS row PAID/ONLINE immediately |
| 11 | Owner: Accept > Start Preparing > Mark Ready | Customer's progress bar moves; pop-up "Order #n is READY" |
| 12 | Owner: try Complete on a PENDING order | Warning: workflow not allowed |
| 13 | Owner: Complete a cash order with pending payment | Asks "cash received?" then payment becomes PAID |
| 14 | Owner: Reject a paid online order | Order REJECTED, payment REFUNDED |
| 15 | Customer: Cancel a PENDING order | Order CANCELLED, payment FAILED |
| 16 | Customer: Cancel an ACCEPTED order | Refused, only PENDING can be cancelled |
| 17 | Customer: "Pay Online Now" on a pending cash order | Payment PAID, method ONLINE |
| 18 | Owner: set a food to not available | Customer card shows SOLD OUT, button disabled |
| 19 | Owner: delete a food that was ordered | Friendly message (FK violation ORA-02292) |
| 20 | Owner: Reports > each report | Tables load, totals match PAYMENTS |

## B. Constraint tests (run in SQL Developer, each must FAIL)
```sql
INSERT INTO food_items (food_name, category_id, price) VALUES ('Test', 1, -5);        -- ORA-02290 price check
INSERT INTO food_items (food_name, category_id, price) VALUES ('Test', 99, 50);       -- ORA-02291 FK category
INSERT INTO students (roll_no, full_name, email) VALUES ('CS-2023-001','X','x@y.com'); -- ORA-00001 unique roll_no
INSERT INTO orders (student_id, status) VALUES (1, 'DONE');                           -- ORA-02290 status check
INSERT INTO order_details (order_id, food_id, quantity, unit_price, line_total) VALUES (1,1,0,350,0); -- quantity check
INSERT INTO payments (order_id, amount, method) VALUES (1, 100, 'CARD');              -- method check / unique order
```

## C. Verification queries
```sql
SELECT order_id, status, total_amount FROM orders ORDER BY order_id DESC;
SELECT * FROM payments ORDER BY payment_id DESC;
-- every order total must equal the sum of its lines:
SELECT o.order_id FROM orders o
WHERE o.total_amount <> (SELECT SUM(line_total) FROM order_details d WHERE d.order_id = o.order_id);   -- expect no rows
```
