dependencies {
  project(":application")
}

tasks.create<JavaExec>("buildManual") {
  group = "documentation"
  description = "Compile the manual's TeX file to PDF."
  
  classpath = files(project(":application").tasks.findByPath("shadowJar"))
  args = listOf("-l", "-v", "arara-manual.tex")
  inputs.files("arara-manual.tex")
  outputs.files("arara-manual.pdf")
}
