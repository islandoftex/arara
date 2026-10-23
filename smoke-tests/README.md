# Process smoke tests

Subprocess checks for the packaged arara jar; no TeX dependency.

| Property | Value |
| --- | --- |
| Gradle task | `./gradlew processSmoke` |
| Runtime | JDK 21+ with `javac`; override with `-PsmokeJava=/path/to/jdk/bin/java` |
| Coverage | CLI options, both rule engines, configuration and projects, execution failures, dialogs, console/log parity and persistent file databases |
| Process deadline | 30 seconds |
| JUnit report | `build/test-results/processSmoke/results.xml` |
| Standalone CLI | `java -jar smoke-tests/build/libs/process-smoke.jar --jar APP_JAR --java JAVA` |
| Optional CLI arguments | `--javac JAVAC`, `--report XML`, `--jna JNA_JAR` (default: beside `APP_JAR`) |
| CI runtimes | GitLab: Java 21/25; GitHub: Java 21/25 on Linux/macOS/Windows |
| CI artifacts | `stageProcessSmokeJar`: `build/processSmoke/{arara,jna,runner}.jar` |

`./gradlew texIntegration` tests the official YAML and Lua rules with `pdflatex`
and `bibtex` on `PATH`: PDFs, repeated passes with SyncTeX, bibliography resolution,
database-driven rebuilds and TeX failures. Each invocation has a 120-second deadline.
Fixtures and tool output remain in `build/texIntegration`; the JUnit report is
`build/test-results/texIntegration/results.xml`. Missing TeX tools fail the suite.
The standalone runner selects it with `--suite tex` and accepts `--work-dir DIR`.

`./gradlew :smoke-tests:allTests` tests the runner's timeout, exit status, cleanup
and JUnit reporting. These tests also run under `./gradlew allTests`.
