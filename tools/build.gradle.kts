import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest

subprojects {  
  val moduleName = "${project.group}.${project.projectDir.name}"
  val mainClass = "$moduleName.Application"
  
  apply(plugin = "application")
  apply(plugin = "com.github.johnrengelman.shadow")
  
  configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility
  }
  
  configure<ApplicationPluginConvention> {
    applicationName = project.name
    mainClassName = mainClass
  }
  
  val mainManifest: Manifest = DefaultManifest((project as ProjectInternal).fileResolver)
    .apply {
      attributes["Implementation-Title"] = project.name
      attributes["Implementation-Version"] = project.version
      attributes["Main-Class"] = mainClass
      attributes["Automatic-Module-Name"] = moduleName
    }
  
  tasks {
    named<Jar>("jar") {
      manifest.attributes.putAll(mainManifest.attributes)
    }
    named<ShadowJar>("shadowJar") {
      manifest.attributes.putAll(mainManifest.attributes)
      archiveAppendix.set("with-deps")
      archiveClassifier.set("")
    }
  }
  tasks.named<Task>("assembleDist").configure { dependsOn("shadowJar") }
}
