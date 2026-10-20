# Process smoke tests

Subprocess checks for the packaged arara jar; no TeX dependency.

| Property | Value |
| --- | --- |
| Gradle task | `./gradlew processSmoke` |
| Runtime | JDK 11+ with `javac`; override with `-PsmokeJava=/path/to/jdk/bin/java` |
| Coverage | Shipped rules, YAML/Lua configuration and dispatch, interpolation, custom exits, process exits/output, timeouts, halts, terminal input/EOF, safe mode, file-change cache persistence, plain/styled output and log parity, project configuration, specification recognition and mixed-input ownership |
| Process deadline | 30 seconds |
| JUnit report | `build/test-results/processSmoke/results.xml` |
| Standalone CLI | `java -jar smoke-tests/build/libs/process-smoke.jar --jar APP_JAR --java JAVA` |
| Optional CLI arguments | `--javac JAVAC`, `--report XML` |
| CI runtimes | GitLab: Java 11/21; GitHub: Java 21 on Linux/macOS/Windows |
| CI artifacts | `stageProcessSmokeJar`: `build/processSmoke/{arara,runner}.jar` |
