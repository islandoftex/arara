dependencies {
  project(":application")
}

tasks.create<JavaExec>("build") {
  group = "documentation"
  classpath = files(project(":application").tasks.findByPath("shadowJar"))
  args = listOf("-l", "-v", "arara-manual.tex")
}
