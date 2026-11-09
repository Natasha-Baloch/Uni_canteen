@echo off
REM Run UniCanteen with Maven and the MySQL JDBC driver.
mvn clean compile exec:java
pause
