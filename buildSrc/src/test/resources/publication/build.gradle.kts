plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven { url = uri(providers.gradleProperty("publicationRepository").get()) }
        }
        filter { includeGroup("org.islandoftex.arara") }
    }
}

val apiContracts by sourceSets.creating

dependencies {
    add(apiContracts.implementationConfigurationName,
        "org.islandoftex.arara:arara-api-jvm:${providers.gradleProperty("publicationVersion").get()}")
    implementation(apiContracts.output)
    implementation("org.islandoftex.arara:arara-core-jvm:${providers.gradleProperty("publicationVersion").get()}")
}

configurations.configureEach {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

kotlin {
    jvmToolchain(21)
}

tasks.compileJava { options.release.set(21) }

application { mainClass.set("PublicationConsumerKt") }
