// cli/build.gradle.kts
plugins {
    kotlin("multiplatform")
    application
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core"))
                implementation(project(":lua"))
                implementation("io.github.microutils:kotlin-logging:2.1.21")
            }
        }

        jvmMain{
            dependencies {
                implementation(project(":core"))
                implementation(project(":mvel"))

//                implementation(kotlin("reflect", "1.9.23"))
                implementation(kotlin("reflect", "2.1.20"))
                implementation("com.github.ajalt.clikt:clikt:4.4.0")
                implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
                implementation("org.mvel:mvel2:2.5.0.Final")
                implementation("org.slf4j:slf4j-api:1.7.36")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
                implementation("org.apache.logging.log4j:log4j-core:2.23.1")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.23.1")
            }
        }

        jvmTest {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5-jvm:4.6.3")
                implementation("io.kotest:kotest-assertions-core-jvm:4.6.3")
            }
        }
    }

}

application {
    applicationName = project.name
    mainClass.set("<TODO>")
}
