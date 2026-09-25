// SPDX-License-Identifier: BSD-3-Clause
plugins {
    `kotlin-dsl`
}

// the Kotlin plugin's own stdlib and reflect coordinates resolve
// through here, so the repositories declaration is load-bearing even
// though this build declares no dependencies of its own
repositories {
    mavenCentral()
}
