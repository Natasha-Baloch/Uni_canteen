# University Canteen Management System - Project Report

## 1. Introduction
Students waste lecture-break time queueing at the university canteen, and the owner tracks orders on paper.
This project provides a desktop application where students order food and the owner manages requests, payments
and sales, backed by an MySQL relational database.

## 2. Objectives
- Replace paper orders with a database-driven order and payment workflow.
- Give two roles (Customer, Owner) secure, separate interfaces.
- Show practical DBMS concepts: keys, constraints, normalization, joins, grouping, subqueries, transactions.

## 3. Technologies
Java 17 (Swing UI), JDBC, MySQL Database 12c+ (SQL), MySQL SQL Developer, IntelliJ IDEA / NetBeans, Maven.

## 4. Requirements
**Functional:** login and registration; student, food and category management; cart and order placement with
automatic total; order status workflow; cash/online payments; dashboard; reports; search.
**Non-functional:** easy to use, data integrity through constraints, passwords hashed, clear error messages.

## 5. System design
**Architecture (3 layers):** UI (`canteen.ui`) -> Data-access objects (`canteen.db.*Dao`) -> MySQL via JDBC (`Db`).
The UI never writes SQL; each DAO groups the queries of one module.

**Actors:** Customer (student) and Owner (admin).

**Order life cycle:** `PENDING -> ACCEPTED -> PREPARING -> READY -> COMPLETED`; `PENDING/ACCEPTED -> REJECTED` by owner;
`PENDING -> CANCELLED` by customer. The Java code enforces allowed transitions and the CHECK constraint enforces valid values.

**Payment life cycle:** online = `PAID` at checkout, cash = `PENDING` until the owner records it. Rejecting/cancelling:
`PAID -> REFUNDED`, `PENDING -> FAILED`.

## 6. Database design
ER diagram: `docs/ER_Diagram.png`.

| Table | Purpose | Key constraints |
|---|---|---|
| STUDENTS | customers | PK student_id; UNIQUE roll_no, email; CHECK email LIKE '%@%'; DEFAULT department |
| CATEGORIES | Fast Food, Snacks, Drinks, Meals, Desserts | PK; UNIQUE name |
| FOOD_ITEMS | menu | PK; FK category_id; UNIQUE name; CHECK price>0; CHECK availability IN ('Y','N') |
| ORDERS | one per checkout | PK; FK student_id; CHECK status IN (...); CHECK total>=0; DEFAULT status='PENDING', date=CURRENT_TIMESTAMP |
| ORDER_DETAILS | lines of an order | PK; FK order_id (ON DELETE CASCADE), FK food_id; CHECK quantity>0; UNIQUE(order_id, food_id) |
| PAYMENTS | one per order | PK; FK order_id UNIQUE; CHECK method IN ('CASH','ONLINE'); CHECK status IN (...) |
| USERS (extra) | login accounts | PK; UNIQUE username; CHECK role IN ('OWNER','CUSTOMER'); FK student_id |

**Relationships:** STUDENTS 1-N ORDERS; ORDERS 1-N ORDER_DETAILS; FOOD_ITEMS 1-N ORDER_DETAILS (so ORDERS and FOOD_ITEMS are
many-to-many through ORDER_DETAILS); ORDERS 1-1 PAYMENTS; CATEGORIES 1-N FOOD_ITEMS; STUDENTS 1-0..1 USERS.

**Normalization (3NF):** food name/category are stored once in FOOD_ITEMS/CATEGORIES; order lines reference food by id.
`unit_price` is copied into ORDER_DETAILS on purpose so old orders keep the price paid when the menu price changes (historical
snapshot, not redundancy). No partial or transitive dependencies remain.

## 7. Modules
1. **Login / Register** - SHA-256 hashed passwords, role check (a customer cannot enter the owner tab).
2. **Dashboard (owner)** - students, food items, orders, paid sales, pending orders + live incoming requests.
3. **Student management** - add, view, update, delete, search.
4. **Food management** - name, category, price, availability, image, update/delete, search.
5. **Categories** - add / rename / delete, with item counts.
6. **Customer menu** - category chips, search, food cards with images, cart, checkout.
7. **Orders** - place, owner workflow buttons, customer live tracking and cancel, history.
8. **Payments** - cash / online, paid / pending / failed / refunded, history, record cash.
9. **Reports** - daily sales, monthly sales, popular food, pending payments, top customers, sales by category.

## 8. SQL concepts used
- **JOIN:** orders-students-payments, food-categories, order_details-food.
- **GROUP BY / HAVING:** popular foods (`HAVING SUM(quantity) >= 1`), top customers.
- **Subqueries:** `GROUP_CONCAT` correlated subquery for the items in an order; `HAVING SUM(total) > (SELECT AVG(total_amount) FROM orders)`.
- **Transactions:** placing an order inserts ORDERS + ORDER_DETAILS + PAYMENTS and commits once; any failure rolls back everything.
- **Security:** prepared statements everywhere (no SQL injection), hashed passwords; the server re-reads prices from FOOD_ITEMS.

## 9. Testing
See `docs/TESTING.md` (functional test cases, constraint tests, expected results).

## 10. Limitations and future work
Payment gateway is simulated; one canteen only; status updates use polling (every 4-5 s) instead of push; could add
email/SMS notification, stock counts, discount vouchers, a web/mobile front end.

## 11. Conclusion
The system covers the complete order-to-payment cycle for a university canteen with a normalized MySQL schema, enforced
integrity, and a modern Swing interface for both customers and the owner.

## 12. Image credits
Add here the photographer name + URL of every food photo you download (Unsplash/Pexels require/appreciate credit).
