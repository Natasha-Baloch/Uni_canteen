# Viva Questions and Answers

**1. What is the aim of the project?** To automate canteen ordering and payment: students order online, the owner accepts and prepares, and reports show sales.

**2. Why MySQL?** It is a robust RDBMS with strong constraints, transactions and SQL features (identity columns, GROUP_CONCAT, SHA-256 hashing in Java) that the project uses.

**3. How does Java connect to MySQL?** Through JDBC: `DriverManager.getConnection(url, user, password)` with the MySQL Connector/J driver; settings are in `config/db.properties` (class `Db`).

**4. What is a PreparedStatement and why use it?** A precompiled parameterised SQL statement. It prevents SQL injection and handles types/quotes safely.

**5. Name the tables and their keys.** STUDENTS(student_id), CATEGORIES(category_id), FOOD_ITEMS(food_id, FK category_id), ORDERS(order_id, FK student_id), ORDER_DETAILS(detail_id, FK order_id, food_id), PAYMENTS(payment_id, FK order_id), plus USERS for login.

**6. What is the relationship between ORDERS and FOOD_ITEMS?** Many-to-many, resolved by the junction table ORDER_DETAILS.

**7. Why is unit_price stored in ORDER_DETAILS when FOOD_ITEMS has price?** To keep the historical price paid even if the menu price changes later.

**8. Which constraints did you use?** PRIMARY KEY, FOREIGN KEY (with ON DELETE CASCADE where logical), NOT NULL, UNIQUE, CHECK (price>0, status lists, quantity>0), DEFAULT (CURRENT_TIMESTAMP, 'PENDING', 'Y').

**9. What is a transaction? Where did you use it?** A unit of work that is fully committed or fully rolled back. Placing an order inserts into ORDERS, ORDER_DETAILS and PAYMENTS; on any error `rollback()` is called.

**10. What is normalization? Is your design in 3NF?** Removing redundancy and anomalies. Yes: every non-key attribute depends on the key only; categories and foods are separate tables.

**11. Explain JOIN with an example from the project.** Owner orders: `orders JOIN students ... LEFT JOIN payments` to show customer name and payment status with each order. LEFT JOIN keeps orders even if a payment row is missing.

**12. Where did you use GROUP BY and HAVING?** Popular food: `GROUP BY food_name HAVING SUM(quantity) >= 1`; top customers: `HAVING SUM(total) > (subquery AVG)`.

**13. What is a subquery? Example?** A query inside another. `(SELECT AVG(total_amount) FROM orders)` in HAVING, and the correlated GROUP_CONCAT subquery that lists the items of each order.

**14. How are passwords protected?** Only the SHA-256 hash is stored; login hashes the typed password and compares hashes.

**15. How do customer and owner differ in the app?** The `role` column in USERS decides which window opens (CustomerFrame or OwnerFrame); the login tab must match the role.

**16. Explain the order status flow.** PENDING > ACCEPTED > PREPARING > READY > COMPLETED, REJECTED by owner, CANCELLED by customer while PENDING. The Java code validates transitions; the CHECK constraint validates values.

**17. How does the customer see live updates?** `MyOrdersPanel` polls the database every 4 seconds with a Swing Timer, updates the progress bar and shows a pop-up when the status changes.

**18. What happens to payment when an order is rejected?** PAID online payments become REFUNDED, PENDING cash payments become FAILED.

**19. Why identity columns instead of sequences/triggers?** MySQL 12c identity (`AUTO_INCREMENT`) auto-generates keys with less code.

**20. What is the difference between DELETE and DROP/TRUNCATE?** DELETE removes rows (can be rolled back, uses WHERE); TRUNCATE removes all rows quickly; DROP removes the table itself.

**21. What would you improve?** Real payment gateway, notifications, stock management, connection pooling, web/mobile version.

**22. Why does deleting a food item sometimes fail?** A FOREIGN KEY from ORDER_DETAILS references it (ORA-02292). The owner should mark it "not available" instead.

**23. What is the role of the `Db` class?** Central JDBC helper: opens connections, runs parameterised queries/updates, builds table models and converts MySQL error codes to readable messages.
