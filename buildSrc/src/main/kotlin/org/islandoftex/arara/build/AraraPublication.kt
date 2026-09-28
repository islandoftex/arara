// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.net.URI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials

/**
 * A simple wrapper around arara's publication logic. The plugin covers
 * the library modules alone: the application module ships as the
 * runnable jar, and its thin jar is a build intermediate no consumer
 * depends on, so it never reaches a Maven repository.
 */
open class AraraPublication : Plugin<Project> {
    @Suppress("LongMethod")
    override fun apply(target: Project) {
        target.apply(plugin = "maven-publish")
        // the sources and javadoc jars are part of the conventional
        // library artifact set; the javadoc jar content is repointed at
        // the Dokka javadoc generation in the main build script
        target.extensions.configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }
        target.configure<PublishingExtension> {
            // the project is JVM-only, hence a single publication derived
            // from the java component is sufficient
            publications.create<MavenPublication>("maven") {
                from(target.components.getByName("java"))
            }
            publications.configureEach {
                this as MavenPublication

                artifactId = "arara-" + target.name

                groupId = target.group.toString()
                version = target.version.toString()

                pom {
                    name.set("arara (${target.name} module)")
                    description.set(
                            "arara is a TeX automation tool based on rules and directives. " +
                                    "It gives you a way to enhance your TeX experience."
                    )
                    inceptionYear.set("2012")
                    url.set("https://gitlab.com/islandoftex/arara")
                    organization {
                        name.set("Island of TeX")
                        url.set("https://gitlab.com/islandoftex")
                    }
                    licenses {
                        license {
                            name.set("""BSD 3-clause "New" or "Revised" License""")
                            url.set("https://opensource.org/licenses/BSD-3-Clause")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            name.set("Paulo Roberto Massa Cereda")
                            email.set("cereda.paulo@gmail.com")
                            id.set("cereda")
                            url.set("https://gitlab.com/cereda")
                            roles.set(listOf("Developer", "Tester"))
                        }

                        developer {
                            name.set("Ben Frank")
                            id.set("benfrank")
                            url.set("https://gitlab.com/benfrank")
                            roles.set(listOf("Developer", "Technical lead", "Code reviewer"))
                        }

                        developer {
                            name.set("Nicola Talbot")
                            email.set("nicola.lc.talbot@gmail.com")
                            id.set("nlct")
                            url.set("https://www.dickimaw-books.com")
                            roles.set(listOf("Developer", "Contributor", "Tester"))
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
            }

            System.getenv("CI_PROJECT_ID")?.let { projectId ->
                repositories {
                    maven {
                        name = "GitLab"
                        url = URI(
                            "https://gitlab.com/api/v4/projects/" +
                                    "$projectId/packages/maven"
                        )
                        // the standard job token of the GitLab runner is
                        // the intended credential; it is only registered
                        // when present so a missing token fails the
                        // publication early instead of as a bare 401
                        System.getenv("CI_JOB_TOKEN")?.let { token ->
                            credentials(HttpHeaderCredentials::class) {
                                name = "Job-Token"
                                value = token
                            }
                            authentication {
                                create<HttpHeaderAuthentication>("header")
                            }
                        }
                    }
                }
            }
        }
    }
}
