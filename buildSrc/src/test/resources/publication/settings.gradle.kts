pluginManagement {
    plugins {
        kotlin("jvm") version providers.gradleProperty("consumerKotlinVersion").get()
    }
}

rootProject.name = "publication-consumer"
