// SPDX-License-Identifier: BSD-3-Clause

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.islandoftex.arara.build.Versions
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class).kotlinPluginVersion
dependencies {
    api(project(":api"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = Versions.kotlinxSerialization)
    implementation(group = "com.charleskorn.kaml", name = "kaml", version = Versions.kaml)
    implementation(group = "org.zeroturnaround", name = "zt-exec", version = Versions.ztExec)

    testImplementation(group = "io.kotest", name = "kotest-runner-junit5-jvm", version = Versions.kotest)
    testImplementation(group = "io.kotest", name = "kotest-assertions-core-jvm", version = Versions.kotest)
}

java {
    withJavadocJar()
    withSourcesJar()
}

sourceSets {
    main {
        java { setSrcDirs(listOf("src/main/kotlin")) }
        resources { setSrcDirs(listOf("src/main/resources")) }
    }
    test {
        java { setSrcDirs(listOf("src/test/kotlin")) }
        resources { setSrcDirs(listOf("src/test/resources")) }
    }
}

val mainManifest: Manifest = DefaultManifest((project as ProjectInternal).fileResolver)
        .apply {
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = version
            if (java.sourceCompatibility < JavaVersion.VERSION_1_9) {
                attributes["Automatic-Module-Name"] = group
            }
        }

tasks {
    withType<Jar> {
        archiveBaseName.set("arara-core")
        manifest.attributes.putAll(mainManifest.attributes)
    }
    named<ShadowJar>("shadowJar") {
        manifest.attributes.putAll(mainManifest.attributes)
        archiveAppendix.set("with-deps")
        archiveClassifier.set("")
    }
}

publishing {
    publications {
        create<MavenPublication>("GitLab") {
            groupId = project.group.toString()
            artifactId = "arara-core"
            version = project.version.toString()

            pom {
                name.set("arara-core")
                description.set(
                        "arara's API component for use in applications " +
                                "interested in letting arara compile their TeX files."
                )
                inceptionYear.set("2020")
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
            artifact(tasks["javadocJar"])
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
