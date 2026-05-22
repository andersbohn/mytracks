---
name: feedback-build-tool
description: This project uses Maven (mvn), not Gradle — always use mvn commands
metadata:
  type: feedback
---

Use `mvn` for all build/test commands in mytracks. There is no gradlew.

**Why:** User corrected when Gradle was used — repo is Maven-based.
**How to apply:** Replace any `./gradlew` with `mvn` equivalents (e.g. `mvn verify`, `mvn test`, `mvn spotless:check`).
