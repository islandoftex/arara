// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    // the Lua-based rule, configuration and conditional formats reuse
    // helper methods, the configuration model and the conditional helpers
    // that historically live in the MVEL module; they will move to a
    // dedicated module once the YAML format is removed
    // (api scope because LuaLocalConfiguration returns the shared model
    // and LuaConditionalEnvironment mirrors the conditional registry)
    api(project(":mvel"))
    implementation(libs.luaj)
    implementation(libs.kotlin.logging)

}
