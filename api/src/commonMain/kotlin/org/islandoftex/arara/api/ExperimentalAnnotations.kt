// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn("This feature is incubating and has an " +
        "undecided status as part of the public API. Do not use outside of " +
        "the core project for now.", RequiresOptIn.Level.WARNING)
internal annotation class Incubating
