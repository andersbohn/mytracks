# Project Coding Guidelines

## General Principles

- Use **constructor-based injection** rather than field injection.
- Prefer **composition over inheritance**.
- Maintain a **bespoke, humble tone** in documentation (no "bragging").
- Avoid the comment line stating what the next line (or couple of lines) obviously does. Except when counter intuitive
  or when the code is otherwise not self-explanatory.

## Technology Stack

- Framework: Spring Boot
- Test Framework: JUnit 5 with AssertJ
- Database: PostgreSQL

## File Organization

- Place all unit tests in `src/test/java`.
- Integration tests must be suffixed with `IT.java`.

## Task Execution Protocol

- Before writing code, Agent must upsert a plan markdown file in the `docs/` folder. Preferable starting with yyyy-MM-dd
  of the day and naming based on the requested changes.
- When asked to prepare a plan of tasks, just present the plan and await instructions to execute!
- Before declaring a change done, make sure that tests run green, including integration tests AND code format with
  spotlessCheck.