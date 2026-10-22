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
