// SPDX-License-Identifier: BSD-3-Clause

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class).kotlinPluginVersion
dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
}

val projectName = project.name.toLowerCase()
val moduleName = group

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility

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
                attributes["Automatic-Module-Name"] = moduleName
            }
        }

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    withType<Jar> {
        manifest.attributes.putAll(mainManifest.attributes)
    }
    named<ShadowJar>("shadowJar") {
        manifest.attributes.putAll(mainManifest.attributes)
        archiveAppendix.set("with-deps")
        archiveClassifier.set("")
    }

    withType<Test> {
        useJUnitPlatform()
    }
}

/*
publishing {
    publications {
        create<MavenPublication>("GitLab") {
            groupId = project.group.toString()
            artifactId = "arara-api"
            version = project.version.toString()

            pom {
                name.set("arara-api")
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
                }
                scm {
                    connection.set("scm:git:https://github.com/cereda/arara.git")
                    developerConnection.set("scm:git:https://github.com/cereda/arara.git")
                    url.set("https://github.com/cereda/arara")
                }
                ciManagement {
                    system.set("GitLab")
                    url.set("https://gitlab.com/islandoftex/arara/pipelines")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/cereda/arara/issues")
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
                }
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}
*/
