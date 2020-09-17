// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain
import java.nio.file.Paths

class DatabaseTest : ShouldSpec({
    should("throw when removing non-existent path") {
        shouldThrow<NoSuchElementException> { Database().remove(Paths.get("")) }
                .message shouldContain "non-existent path"
    }
})
