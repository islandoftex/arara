import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest

plugins {
  application
  id("com.github.johnrengelman.shadow")
}

dependencies {
  implementation("ch.qos.cal10n:cal10n-api:0.8.1")
  implementation("ch.qos.logback:logback-classic:1.1.2") {
    exclude("org.slf4j:slf4j-api")
  }
  implementation("ch.qos.logback:logback-core:1.1.2")
  implementation("commons-cli:commons-cli:1.3.1")
  implementation("commons-io:commons-io:2.2")
  implementation("commons-lang:commons-lang:2.6")
  implementation("org.apache.commons:commons-collections4:4.0")
  implementation("org.apache.velocity:velocity:1.7") {
    exclude("commons-lang:commons-lang")
  }
  implementation("org.mvel:mvel2:2.2.8.Final")
  implementation("org.simpleframework:simple-xml:2.7.1")
  implementation("org.slf4j:slf4j-api:1.7.7")
  implementation("org.yaml:snakeyaml:1.17")
  implementation("org.zeroturnaround:zt-exec:1.9") {
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
  named<JavaCompile>("compileJava") {
    if (java.sourceCompatibility > JavaVersion.VERSION_1_8) {
      inputs.property("moduleName", moduleName)
      options.compilerArgs = listOf(
          // include Gradle dependencies as modules
          "--module-path", sourceSets["main"].compileClasspath.asPath)
    }
  }
  named<Jar>("jar") {
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
