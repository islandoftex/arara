// cli/build.gradle.kts
plugins {
    kotlin("multiplatform")
    application
}

kotlin {
    jvm()

    sourceSets.commonMain.dependencies {
        implementation(project(":core"))
        implementation(project(":lua"))
        implementation("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")
    }

    sourceSets.jvmMain.dependencies {
        implementation(project(":core"))
        implementation(project(":mvel"))

        implementation(kotlin("reflect", Versions.kotlin))
        implementation("com.github.ajalt.clikt:clikt:${Versions.clikt}")
        implementation("net.mamoe.yamlkt:yamlkt:${Versions.yamlkt}")
        implementation("org.mvel:mvel2:${Versions.mvel}")
        implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
        implementation("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")
        implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
        implementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4j}")
    }

    sourceSets.jvmTest.dependencies {
        implementation("io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
        implementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
    }
}

application {
    applicationName = project.name
    mainClass.set("<TODO>")
}
