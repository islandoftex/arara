import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    `maven-publish`
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jetbrains.dokka")
    jacoco
}

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class).kotlinPluginVersion
dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation("com.uchuhimo:konf-core:0.22.1")
    implementation("com.github.ajalt:clikt:2.3.0")
    implementation("ch.qos.cal10n:cal10n-api:0.8.1")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("org.mvel:mvel2:2.4.5.Final")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.yaml:snakeyaml:1.25")
    implementation("org.zeroturnaround:zt-exec:1.11")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

version = "5.0.0-SNAPSHOT"
val projectName = project.name.toLowerCase()
val moduleName = group
val mainClass = "$moduleName.Arara"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility
}

sourceSets {
    main {
        java { setSrcDirs(listOf("src/main/java", "src/main/kotlin")) }
        resources { setSrcDirs(listOf("src/main/resources")) }
    }
    test {
        java { setSrcDirs(listOf("src/test/kotlin")) }
        resources { setSrcDirs(listOf("src/test/resources")) }
    }
}

application {
    applicationName = project.name
    mainClassName = mainClass
}

val mainManifest: Manifest = DefaultManifest((project as ProjectInternal).fileResolver)
        .apply {
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = version
            attributes["Main-Class"] = mainClass
            if (java.sourceCompatibility < JavaVersion.VERSION_1_9) {
                attributes["Automatic-Module-Name"] = moduleName
            }
        }

tasks {
    register<Jar>("dokkaJar") {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Create JAR with dokka documentation"
        archiveClassifier.set("dokka")
        from(dokka)
    }
    register<Jar>("sourcesJar") {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles sources JAR"
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    named<JavaCompile>("compileJava") {
        if (java.sourceCompatibility > JavaVersion.VERSION_1_8) {
            inputs.property("moduleName", moduleName)
            options.compilerArgs = listOf(
                    // include Gradle dependencies as modules
                    "--module-path", sourceSets["main"].compileClasspath.asPath)
        }
    }
    withType<KotlinCompile>() {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    withType<Jar>() {
        archiveBaseName.set("arara")
        manifest.attributes.putAll(mainManifest.attributes)
    }
    named<ShadowJar>("shadowJar") {
        manifest.attributes.putAll(mainManifest.attributes)
        archiveAppendix.set("with-deps")
        archiveClassifier.set("")
    }
    named<JavaExec>("run") {
        if (JavaVersion.current() > JavaVersion.VERSION_1_8) {
            doFirst {
                jvmArgs = listOf(
                        "--module-path", classpath.asPath
                )
            }
        }
    }

    withType<Test>() {
        useJUnitPlatform()

        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR,
                    TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED)
        }
    }
}
tasks.named<Task>("assembleDist").configure {
    dependsOn("shadowJar", "jacocoTestReport")
}

publishing {
    publications {
        create<MavenPublication>("GitLab") {
            groupId = project.group.toString()
            artifactId = "arara"
            version = project.version.toString()

            pom {
                name.set("arara")
                description.set(
                        "Arara is a TeX automation tool based on rules and directives."
                )
                inceptionYear.set("2012")
                url.set("https://github.com/cereda/arara")
                organization {
                    name.set("Island of TeX")
                    url.set("https://gitlab.com/islandoftex")
                }
                licenses {
                    license {
                        name.set("New BSD License")
                        url.set("http://www.opensource.org/licenses/bsd-license.php")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("Paulo Roberto Massa Cereda")
                        email.set("cereda@users.sf.net")
                        id.set("cereda")
                        url.set("https://tex.stackexchange.com/users/3094")
                        roles.set(listOf("Lead developer", "Creator", "Duck enthusiast"))
                    }
                    developer {
                        name.set("Marco Daniel")
                        email.set("marco.daniel@mada-nada.de")
                        id.set("marcodaniel")
                        url.set("https://tex.stackexchange.com/users/5239")
                        roles.set(listOf("Contributor", "Tester", "Fast driver"))
                    }
                    developer {
                        name.set("Brent Longborough")
                        email.set("brent@longborough.org")
                        id.set("brent")
                        url.set("https://tex.stackexchange.com/users/344")
                        roles.set(listOf("Developer", "Contributor", "Tester",
                                "Haskell fanatic"))
                    }
                    developer {
                        name.set("Nicola Talbot")
                        email.set("nicola.lc.talbot@gmail.com")
                        id.set("nlct")
                        url.set("https://tex.stackexchange.com/users/19862")
                        roles.set(listOf("Developer", "Contributor", "Tester",
                                "Hat enthusiast"))
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/cereda/arara.git")
                    developerConnection.set("scm:git:https://github.com/cereda/arara.git")
                    url.set("https://github.com/cereda/arara")
                }
                ciManagement {
                    system.set("GitLab")
                    url.set("https://gitlab.com/islandoftex/arara-v5/pipelines")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/cereda/arara/issues")
                }
            }

            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaJar"])
        }
    }

    repositories {
        maven {
            url = uri("https://gitlab.com/api/v4/projects/14349047/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                if (project.hasProperty("jobToken")) {
                    name = "Job-Token"
                    value = project.property("jobToken").toString()
                }
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}
