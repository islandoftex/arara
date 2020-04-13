// SPDX-License-Identifier: BSD-3-Clause

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.islandoftex.arara.build.Versions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    `maven-publish`
    id("com.github.johnrengelman.shadow")
    jacoco
}

dependencies {
    implementation(project(":core"))
    implementation(project(":mvel"))

    implementation(kotlin("reflect", Versions.kotlin))
    implementation(group = "com.uchuhimo", name = "konf-core", version = Versions.konf)
    implementation(group = "com.github.ajalt", name = "clikt", version = Versions.clikt)
    implementation(group = "ch.qos.logback", name = "logback-classic", version = Versions.logback)
    implementation(group = "ch.qos.logback", name = "logback-core", version = Versions.logback)
    implementation(group = "org.mvel" , name = "mvel2", version = Versions.mvel)
    implementation(group = "org.slf4j", name = "slf4j-api", version = Versions.slf4j)
    implementation(group = "com.fasterxml.jackson.dataformat", name = "jackson-dataformat-yaml", version = Versions.jackson)
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = Versions.jackson)
    implementation(group = "org.zeroturnaround", name = "zt-exec", version = Versions.ztExec)

    testImplementation(group = "io.kotest", name = "kotest-runner-junit5-jvm", version = Versions.kotest)
    testImplementation(group = "io.kotest", name = "kotest-assertions-core-jvm", version = Versions.kotest)
}

status = "development"
val projectName = project.name.toLowerCase()
val moduleName = group
val mainClass = "$moduleName.Arara"

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
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xopt-in=org.islandoftex.arara.api.localization.AraraMessages,kotlin.time.ExperimentalTime,kotlin.RequiresOptIn")
        }
    }

    withType<Jar> {
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
                description.set("arara is a TeX automation tool based on " +
                        "rules and directives. It gives you a way to enhance " +
                        "your TeX experience.")
                inceptionYear.set("2012")
                url.set("https://gitlab.com/islandoftex/arara")
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
                        name.set("Ben Frank")
                        id.set("benfrank")
                        url.set("https://gitlab.com/benfrank")
                        roles.set(listOf("Release coordinator v5"))
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
                    connection.set("scm:git:https://gitlab.com/islandoftex/arara.git")
                    developerConnection.set("scm:git:https://gitlab.com/islandoftex/arara.git")
                    url.set("https://gitlab.com/islandoftex/arara")
                }
                ciManagement {
                    system.set("GitLab")
                    url.set("https://gitlab.com/islandoftex/arara/pipelines")
                }
                issueManagement {
                    system.set("GitLab")
                    url.set("https://gitlab.com/islandoftex/arara/issues")
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
                } else {
                    logger.warn("Will be unable to publish (jobToken missing)\n" +
                            "Ignore this warning if you are not running the publish task " +
                            "for the GitLab package repository.")
                }
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}
