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

Ensure waiting for acknowledgment before proceeding to the next step. Answering any questions related to the request is 
not in and of itself to be seen as an acknowledgment, but rather a part of the process to ensure clarity and 
understanding of the task at hand.

**Definition of Done:**
Before declaring a change done on this task plan, you must ensure:
1. All tests run green (including integration tests).
2. Code format is verified by running `spotlessCheck`.
3. Task list is checked off according to the implementation progress.