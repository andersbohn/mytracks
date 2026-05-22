# Project Coding Guidelines

## General Principles

- Use **constructor-based injection** rather than field injection.
- Prefer **composition over inheritance**.
- Maintain a **bespoke, humble tone** in documentation.
- Stay ultra concise in all comments - only what is required to understand otherwise non-obvious code. 

## Technology Stack

- Framework: Spring Boot
- Test Framework: JUnit 5 with AssertJ
- Database: PostgreSQL

## File Organization

- Place all unit tests in `src/test/java`.
- Integration tests must be suffixed with `IT.java`.
