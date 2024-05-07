// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets.all {
        dependencies {
            api(project(":api"))
            implementation("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")
        }
    }

    sourceSets.commonMain.dependencies {
        implementation("com.soywiz.korlibs.korio:korio:${Versions.korlibs}")
        implementation("net.mamoe.yamlkt:yamlkt:${Versions.yamlkt}")
    }

    sourceSets.jvmMain.dependencies {
        implementation("org.zeroturnaround:zt-exec:${Versions.ztExec}")
        implementation("com.soywiz.korlibs.korio:korio:${Versions.korlibs}")
    }

    sourceSets.jvmTest.dependencies {
        implementation("io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
        implementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
        runtimeOnly("org.slf4j:slf4j-simple:${Versions.slf4j}")
    }
}

