// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import java.net.URI

/**
 * A simple wrapper around arara's publication logic.
 */
open class AraraPublication : Plugin<Project> {
    @Suppress("LongMethod")
    override fun apply(target: Project) {
        target.apply(plugin = "maven-publish")
        target.configure<PublishingExtension> {
            publications.configureEach {
                this as MavenPublication

                artifactId = "arara-" + when (name) {
                    "kotlinMultiplatform" ->
                        target.name

                    "metadata" ->
                        "${target.name}-metadata"

                    "jvm" ->
                        "${target.name}-jvm"

                    else ->
                        throw IllegalArgumentException("Unknown publication type $name")
                }

                groupId = target.group.toString()
                version = target.version.toString()

                pom {
                    name.set(artifactId)
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
                            name.set("New BSD License")
                            url.set("http://www.opensource.org/licenses/bsd-license.php")
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

            System.getenv("CI_PROJECT_ID")?.let {
                repositories {
                    maven {
                        name = "GitLab"
                        url = URI("https://gitlab.com/api/v4/projects/$it/packages/maven")
                        credentials(HttpHeaderCredentials::class) {
                            if (target.hasProperty("jobToken")) {
                                name = "Job-Token"
                                value = target.property("jobToken").toString()
                            }
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
