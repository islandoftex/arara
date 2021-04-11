// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn("This feature is incubating and has an " +
        "undecided status as part of the public API. Do not use outside of " +
        "the core project for now.", RequiresOptIn.Level.WARNING)
internal annotation class Incubating

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn("arara's messages are not considered stable API. Only " +
        "use them in testing scenarios and arara's core projects.",
        RequiresOptIn.Level.WARNING)
internal annotation class AraraMessages
