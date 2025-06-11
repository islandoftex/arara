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
            implementation("io.github.microutils:kotlin-logging:2.1.21")
        }
    }

    sourceSets.commonMain.dependencies {
        implementation("com.soywiz.korlibs.korio:korio:2.7.0")
        implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
    }

    sourceSets.jvmMain.dependencies {
        implementation("org.zeroturnaround:zt-exec:1.12")
        implementation("com.soywiz.korlibs.korio:korio:2.7.0")
    }

    sourceSets.jvmTest.dependencies {
        implementation("io.kotest:kotest-runner-junit5-jvm:4.6.3")
        implementation("io.kotest:kotest-assertions-core-jvm:4.6.3")
        runtimeOnly("org.slf4j:slf4j-simple:1.7.36")
    }
}

