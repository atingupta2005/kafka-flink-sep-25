# 🧪 ksqlDB End-to-End

---

## 🔹 Step 1: Create Tables & Streams (Topics auto-created)

### Users (Source Table)

```sql
CREATE or REPLACE TABLE users (
    user_id VARCHAR PRIMARY KEY,
    name VARCHAR,
    email VARCHAR,
    country VARCHAR
) WITH (
    KAFKA_TOPIC='user_events',
    VALUE_FORMAT='JSON',
    PARTITIONS=3
);
```

### Orders Stream

```sql
CREATE or REPLACE STREAM orders_stream (
    order_id VARCHAR KEY,
    user_id VARCHAR,
    item VARCHAR,
    price DOUBLE,
    status VARCHAR
) WITH (
    KAFKA_TOPIC='orders',
    VALUE_FORMAT='JSON',
    PARTITIONS=3
);
```

### Payments Stream + Table

```sql
CREATE or REPLACE STREAM payments_stream (
    payment_id VARCHAR KEY,
    order_id VARCHAR,
    user_id VARCHAR,
    amount DOUBLE,
    status VARCHAR
) WITH (
    KAFKA_TOPIC='payments',
    VALUE_FORMAT='JSON',
    PARTITIONS=3
);

CREATE or REPLACE TABLE payments_table (
    payment_id VARCHAR PRIMARY KEY,
    order_id VARCHAR,
    user_id VARCHAR,
    amount DOUBLE,
    status VARCHAR
) WITH (
    KAFKA_TOPIC='payments',
    VALUE_FORMAT='JSON',
    PARTITIONS=3
);
```

---

## 🔹 Step 2: Insert Sample Data (Producer Window)

> **Important:** Insert after creating tables/streams to ensure materialization.

```sql
-- Users
INSERT INTO users (user_id, name, email, country) VALUES ('U1', 'Alice', 'alice@example.com', 'US');
INSERT INTO users (user_id, name, email, country) VALUES ('U2', 'Bob', 'bob@example.com', 'UK');
INSERT INTO users (user_id, name, email, country) VALUES ('U3', 'Charlie', 'charlie@example.com', 'IN');


-- Orders
INSERT INTO orders_stream (order_id, user_id, item, price, status) VALUES ('O1', 'U1', 'Laptop', 1200.00, 'CREATED');
INSERT INTO orders_stream (order_id, user_id, item, price, status) VALUES ('O2', 'U2', 'Headphones', 150.00, 'CREATED');
INSERT INTO orders_stream (order_id, user_id, item, price, status) VALUES ('O3', 'U1', 'Mouse', 50.00, 'CREATED');


-- Payments
INSERT INTO payments_stream (payment_id, order_id, user_id, amount, status) VALUES ('P1', 'O1', 'U1', 1200.00, 'SUCCESS');
INSERT INTO payments_stream (payment_id, order_id, user_id, amount, status) VALUES ('P2', 'O2', 'U2', 150.00, 'FAILED');
INSERT INTO payments_stream (payment_id, order_id, user_id, amount, status) VALUES ('P3', 'O3', 'U1', 50.00, 'SUCCESS');
```

---

## 🔹 Step 3: Materialized Tables for Querying

### Queryable Users Table

```sql
CREATE or REPLACE TABLE queryable_users
WITH (
    KAFKA_TOPIC='queryable_users_topic',
    PARTITIONS=3,
    VALUE_FORMAT='JSON'
) AS
SELECT * FROM users;
```

### Payments By User Table (For Table-Table Join)

> **Key must be `user_id`** for table-table join.

```sql
CREATE or REPLACE TABLE payments_by_user
WITH (
    KAFKA_TOPIC='payments_by_user_topic',
    PARTITIONS=3,
    VALUE_FORMAT='JSON'
) AS
SELECT user_id,
       SUM(amount) AS total_amount,
       MAX(status) AS last_status
FROM payments_stream
GROUP BY user_id;
```

---

## 🔹 Step 4: Queries (Consumer Window)

### A. Filter Table (Queryable Users)

```sql
SELECT * FROM queryable_users WHERE country = 'US' LIMIT 5;
```

### B. Filter Stream (Orders)

```sql
SELECT * FROM orders_stream WHERE price > 100 LIMIT 5;
```

### C. Query Both Stream and Table (Payments)

```sql
SELECT * FROM payments_stream LIMIT 5;
SELECT * FROM payments_by_user LIMIT 5;
```

### D. Table-Table Join (Users + Payments By User)

```sql
SELECT u.user_id, u.name, p.total_amount, p.last_status
FROM queryable_users u
JOIN payments_by_user p
  ON u.user_id = p.user_id
EMIT CHANGES
LIMIT 5;
```

### E. Stream-Table Join (Orders + Users)

```sql
SELECT o.order_id, o.item, o.price, u.name, u.country
FROM orders_stream o
JOIN queryable_users u
  ON o.user_id = u.user_id
EMIT CHANGES
LIMIT 5;
```

---

## 🔹 Step 5: Advanced Queries

### Aggregation: Orders Per User

```sql
CREATE or REPLACE TABLE orders_per_user AS
SELECT user_id, COUNT(*) AS order_count
FROM orders_stream
GROUP BY user_id;

SELECT * FROM orders_per_user LIMIT 5;
```

### Windowed Aggregation: Sales Per User

```sql
CREATE or REPLACE TABLE sales_per_user AS
SELECT user_id,
       SUM(price) AS total_sales
FROM orders_stream
WINDOW TUMBLING (SIZE 1 MINUTE)
GROUP BY user_id;

SELECT * FROM sales_per_user LIMIT 5;
```

---

## 🔹 Step 6: Metadata

```sql
SHOW STREAMS;
SHOW TABLES;
DESCRIBE orders_stream;
DESCRIBE queryable_users;
SHOW QUERIES;
```

---

## 🔹 Step 9: Cheat Sheet Diagrams

### Streams vs Tables

```
orders_stream (STREAM)        users (TABLE)
--------------------------    -----------------------------
O1: U1 buys Laptop $1200      U1 → Alice (US)
O2: U2 buys Headphones $150   U2 → Bob (UK)
O3: U1 buys Mouse $50         U3 → Charlie (IN)
```

### Stream-Table Join

```
orders_stream ⨝ queryable_users
O1 (Laptop, U1) + Alice → Enriched order
O2 (Headphones, U2) + Bob → Enriched order
```

### Table-Table Join

```
queryable_users ⨝ payments_by_user
Alice (U1) + total_amount=1250 → Payment summary
Bob (U2) + total_amount=150   → Payment summary
```

### Aggregation Example

```
orders_stream → orders_per_user
U1 buys Laptop → U1 → 2 orders
U2 buys Headphones → U2 → 1 order
U1 buys Mouse
```

### Windowed Aggregation Example

```
[00:00-00:01] U1 total = 1250
[00:00-00:01] U2 total = 150
```

---
