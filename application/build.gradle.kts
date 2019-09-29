import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest

plugins {
  application
  `maven-publish`
  id("com.github.johnrengelman.shadow")
}

dependencies {
  implementation("ch.qos.cal10n:cal10n-api:0.8.1")
  implementation("ch.qos.logback:logback-classic:1.2.3") {
    exclude("org.slf4j:slf4j-api")
  }
  implementation("ch.qos.logback:logback-core:1.2.3")
  implementation("commons-cli:commons-cli:1.4")
  implementation("commons-io:commons-io:2.6")
  implementation("commons-lang:commons-lang:2.6")
  implementation("org.apache.commons:commons-collections4:4.4")
  implementation("org.apache.velocity:velocity:1.7") {
    exclude("commons-lang:commons-lang")
  }
  implementation("org.mvel:mvel2:2.4.4.Final")
  implementation("org.simpleframework:simple-xml:2.7.1")
  implementation("org.slf4j:slf4j-api:1.7.28")
  implementation("org.yaml:snakeyaml:1.25")
  implementation("org.zeroturnaround:zt-exec:1.11") {
    exclude("commons-io:commons-io")
    exclude("org.slf4j:slf4j-api")
  }
  testImplementation("junit:junit:4.12")
  testImplementation("com.e-movimento.tinytools:privilegedaccessor:1.2.2")
}

version = "5.0.0-SNAPSHOT"
val projectName = project.name.toLowerCase()
val moduleName = group
val mainClass = "$moduleName.Arara"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = sourceCompatibility
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
  register<Jar>("javadocJar") {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Create JAR with javadoc documentation"
    archiveClassifier.set("javadoc")
    from(javadoc)
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
  withType<Jar>() {
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
tasks.named<Task>("assembleDist").configure { dependsOn("shadowJar") }

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
