# Process smoke tests

Subprocess checks for the packaged arara jar; no TeX dependency.

| Property | Value |
| --- | --- |
| Gradle task | `./gradlew processSmoke` |
| Runtime | JDK 11+ with `javac`; override with `-PsmokeJava=/path/to/jdk/bin/java` |
| Coverage | Process exits/output, timeouts, halts, terminal input/EOF, safe mode, project configuration |
| Process deadline | 30 seconds |
| JUnit report | `build/test-results/processSmoke/results.xml` |
| Standalone CLI | `java -jar smoke-tests/build/libs/process-smoke.jar --jar APP_JAR --java JAVA` |
| Optional CLI arguments | `--javac JAVAC`, `--report XML` |
