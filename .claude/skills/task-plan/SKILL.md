---
name: task-plan
description: Creates a markdown task list and enforces test and format checks
---
Generate a markdown task list based on the current objective.
Save the file in the `tasks/` directory.
Include the request details after the header in the markdown file.

Use the following dynamic data for the filename and header:
Timestamp: !`date +"%Y-%m-%d_%H-%M"`
Git Tag: !`git describe --tags --always`

Format the file name as: `tasks/tasklist-[Timestamp]-[Git Tag].md`.
Ensure the content is strictly a markdown checklist.

After saving the task plan file, STOP. Do not implement anything.
Reply with the plan and any open questions, then wait for the user to explicitly say "go ahead" before executing the 
task-plan.

**Definition of Done:**
Before declaring a change done on this task plan, you must ensure:
1. All tests run green (including integration tests).
2. Code format is verified by running `spotlessCheck`.
3. Task list is checked off according to the implementation progress.