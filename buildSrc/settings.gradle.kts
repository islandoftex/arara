// SPDX-License-Identifier: BSD-3-Clause

// the helper build shares the root catalog, so the version of its one
// dependency lives beside every other version the tree manages
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
