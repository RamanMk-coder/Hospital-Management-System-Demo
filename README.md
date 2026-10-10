# Hospital Management System

A Java console application for managing hospital patients, doctors and appointments, backed by MySQL through JDBC.

## Features

- Add a patient (with input validation)
- View all patients in a formatted table
- View doctors
- Book an appointment, with checks that the patient and doctor exist and that the doctor is free on that date

## Tech stack

- Java
- MySQL
- JDBC (MySQL Connector/J)

## Setup

1. **Create the database.** Run `schema.sql` in MySQL (Workbench or the `mysql` CLI).
2. **Add the MySQL driver.** Download MySQL Connector/J from the official MySQL site and add the jar to your project's libraries (in IntelliJ: File > Project Structure > Libraries > +).
3. **Set your database credentials as environment variables.** The password is never stored in the code.

   | Variable | Required | Default |
   | --- | --- | --- |
   | `DB_PASSWORD` | Yes | none |
   | `DB_USER` | No | `root` |
   | `DB_URL` | No | `jdbc:mysql://localhost:3306/hospital` |

   In IntelliJ: Run > Edit Configurations > Environment variables, then add `DB_PASSWORD=your_password`.
4. **Run** `HospitalManagementSystem` (in `src/HospitalManagementsystem`).

## Project structure

```
src/HospitalManagementsystem/
  HospitalManagementSystem.java   Entry point and menu
  Patient.java                    Patient operations
  Doctor.java                     Doctor operations
schema.sql                        Database schema
```
