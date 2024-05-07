// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets.commonMain.dependencies {
        implementation(project(":core"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerialization}")
    }

    sourceSets.jvmMain.dependencies {
        api(project(":api"))
        implementation(project(":core"))
        implementation(kotlin("stdlib"))
        implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
        implementation("org.mvel:mvel2:${Versions.mvel}")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerialization}")
        implementation("com.charleskorn.kaml:kaml:${Versions.kaml}")
    }
}

