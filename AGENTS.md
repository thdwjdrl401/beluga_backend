# AGENTS.md

## Purpose
This repository contains [beluga].
The goal is to help Codex make safe, minimal, correct changes that match the team's conventions.

## Project overview
- Tech stack: Spring Boot4.0.6, Java 21, Gradle, MyBatis, MySQL
- Main app entry: src/main/java/com/thdwjdrl/yejeong/BelugaApplication.java
- Non-goals:
  - Do not introduce unrelated refactors.
  - Do not replace core libraries/frameworks unless explicitly asked.

## Working style
- Prefer the smallest correct change.
- Preserve existing architecture and naming unless the task requires otherwise.
- Follow nearby code style and patterns before inventing new ones.
- Explain important tradeoffs briefly when they matter.
- When the request is ambiguous, ask a focused question before making risky changes.

## Planning
- For small changes, act directly.
- For multi-file, risky, or architectural changes, first produce a short plan.
- If a task is likely to take many steps, keep a checklist in the response and update progress as work proceeds.

## Editing rules
- Do not rename public APIs, database columns, environment variables, or endpoints unless explicitly requested.
- Do not change lockfiles, generated files, or formatting across unrelated files unless necessary.
- Keep diffs tight and local.
- Prefer modifying existing files over creating new abstractions unless duplication is clearly harmful.

## Code quality
- Make code readable and boring.
- Prefer explicit names over clever ones.
- Add comments only where intent is not obvious from the code.
- Handle errors at the appropriate boundary.
- Avoid premature optimization.

## Testing and verification
- After code changes, run the smallest meaningful verification first.
- Prefer targeted tests before full test suites.
- If tests cannot be run, say exactly why.
- If a change affects behavior, describe how to verify it manually.

## Build and test commands
- Install: `./gradlew dependencies`
- Build: `./gradlew build`
- Test: `./gradlew test`
- Run app: `./gradlew bootRun`

## Repository conventions
- Package root: `com.thdwjdrl.beluga`
- Branch naming:
  - `feat/<short-description>` branches must be created from `dev` only.
  - `fix/<short-description>` branches may be created from either `main` (production) or `dev`.
- Commit style: 
  - feat: 새 기능
  - fix: 버그 수정
  - refactor: 기능 변화 없는 구조 개선
  - test: 테스트 코드 추가/수정
  - docs: 문서 수정
  - chore: 설정, 빌드, 의존성 같은 잡무성 변경
- API conventions:
  - REST JSON only
- Error response conventions:
  - standardized error body with code/message/timestamp
- Logging conventions:
  - use structured logs, no sensitive values

## Security and safety
- Never hardcode secrets, tokens, passwords, or private keys.
- Never print secrets in logs or tests.
- Treat auth, payments, and data deletion as high-risk areas; ask before making broad changes there.
- For migrations or destructive commands, explain impact before proceeding.

## Dependencies
- Prefer existing dependencies already used in the repo.
- Do not add new libraries unless necessary and justified.
- If adding a dependency, explain why the standard library or existing stack is insufficient.

## Database rules
- Assume production data must be preserved.
- Prefer additive schema changes.
- Keep DDL and schema changes under version control in the project repository.
- Store schema-related files in a dedicated database directory, organized for future incremental migration management.
- For destructive migrations, require explicit approval.

## Documentation updates
- If behavior, setup, or commands change, update the relevant docs or README.
- Include examples for new environment variables, endpoints, or scripts.

## Output expectations
- Summarize what changed.
- List files touched.
- Report verification performed.
- Mention follow-up risks or next steps only if they are real.

## Task-specific notes

## Absolute don'ts
- Do not fabricate test results.
- Do not claim code was run if it was not run.
- Do not edit unrelated files to “clean things up”.
- Do not silently ignore failing checks.