// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets.commonMain.dependencies {
        implementation(project(":core"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    }

    sourceSets.jvmMain.dependencies {
        api(project(":api"))
        implementation(project(":core"))
        implementation(kotlin("stdlib"))
        implementation("org.slf4j:slf4j-api:1.7.36")
        implementation("org.mvel:mvel2:2.5.0.Final")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
        implementation("com.charleskorn.kaml:kaml:0.55.0")
    }
}

