# UniCanteen — University Canteen Management System

Java 17 Swing application using JDBC and **MySQL 8** (no Oracle dependency). Includes Customer and Owner roles, menu/category management, ordering, payments, reports and sample data.

## Requirements
- JDK 17 or newer
- MySQL Server 8.0+
- Maven 3.8+
- Visual Studio Code with the Extension Pack for Java (recommended)

## 1. Automatic database setup (first launch)
1. Start **MySQL Server** (the MySQL80 Windows service in Services, or your MySQL service manager).
2. Set your real MySQL username/password in `config/db.properties` as described below.
3. Run the app from this project folder. On first launch the application connects to the MySQL server, creates `unicanteen` if needed, creates all tables and loads demo data automatically when the database has no tables.

The MySQL account must have permission to connect to the server and `CREATE DATABASE`. The app does not delete or overwrite existing tables/data. If you prefer manual setup, run `sql/01_schema.sql` followed by `sql/02_sample_data.sql` in MySQL Workbench **only on a fresh database**; the schema script drops existing project tables.

The schema script drops existing UniCanteen tables before recreating them. Do not run it on a database containing data you need to keep.

## 2. Configure the connection
Edit `config/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/unicanteen?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_ACTUAL_MYSQL_PASSWORD
```
Replace `YOUR_ACTUAL_MYSQL_PASSWORD` with the password you use in MySQL Workbench. If your MySQL root account has no password, leave the value after `=` empty. Never leave `CHANGE_ME` or the placeholder text. The sample assumes MySQL is running on `localhost:3306`.

## 3. Run in VS Code
1. Extract the ZIP file.
2. In VS Code choose **File → Open Folder** and select the extracted `UniCanteen` folder (the one containing `pom.xml`).
3. Open **Terminal → New Terminal**.
4. Run:
   ```bash
   mvn clean compile exec:java
   ```
Maven downloads the MySQL JDBC driver automatically. You can also open `src/canteen/Main.java` and use **Run Java** after the Maven project finishes importing. Keep the terminal working directory at the project root so `config/` and `images/` are found.

### Windows quick run
Double-click `run.bat` only if you have configured its classpath as needed; Maven commands above are the recommended cross-platform method.

## Demo accounts
| Role | Username | Password |
|---|---|---|
| Owner | `owner` | `owner123` |
| Customer | `ali` | `student123` |
| Customer | `sara` | `student123` |

New customers can register from the login screen. Passwords are stored as SHA-256 hashes.

## SQL files
- `sql/01_schema.sql` — database, tables, constraints, indexes and summary view.
- `sql/02_sample_data.sql` — demo categories, students, accounts, food, orders and payments.
- `sql/03_useful_queries.sql` — reporting/query examples.

## Notes
- SQL syntax and JDBC driver are configured for MySQL 8.
- Online payment is a simulated gateway, not a real payment processor.
- Food images are optional; see `images/README.txt`.
