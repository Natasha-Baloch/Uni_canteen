-- Queries used by the Reports module (JOIN / GROUP BY / HAVING / subquery) - handy for the viva demo.

-- Daily sales (JOIN + GROUP BY)
SELECT DATE(p.paid_at) AS sale_day, COUNT(*) AS orders, SUM(p.amount) AS total_sales
FROM payments p WHERE p.status = 'PAID' GROUP BY DATE(p.paid_at) ORDER BY 1 DESC;

-- Popular food (multi-table JOIN + GROUP BY + HAVING)
SELECT f.food_name, SUM(d.quantity) AS qty_sold
FROM order_details d JOIN food_items f ON f.food_id = d.food_id
JOIN orders o ON o.order_id = d.order_id
WHERE o.status NOT IN ('REJECTED','CANCELLED')
GROUP BY f.food_name HAVING SUM(d.quantity) >= 2 ORDER BY qty_sold DESC;

-- Customers who spent more than the average order value (subquery)
SELECT s.full_name, SUM(o.total_amount) AS spent
FROM students s JOIN orders o ON o.student_id = s.student_id
GROUP BY s.full_name HAVING SUM(o.total_amount) > (SELECT AVG(total_amount) FROM orders);

-- Food never ordered (NOT IN subquery)
SELECT food_name FROM food_items WHERE food_id NOT IN (SELECT food_id FROM order_details);

-- Order summary view
SELECT * FROM v_order_summary ORDER BY order_id DESC;
