// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.model

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.AraraException

class SessionImplTest : ShouldSpec({
    should("include all environment variables") {
        SessionImpl.clear()
        SessionImpl.updateEnvironmentVariables()
        // on Linux it is called PATH, on Windows it may be Path
        (SessionImpl.contains("environment:PATH") ||
                SessionImpl.contains("environment:Path")) shouldBe true
    }

    should("properly check existence") {
        SessionImpl.clear()
        SessionImpl.put("A", "B")
        SessionImpl.contains("A") shouldBe true
        SessionImpl.contains("C") shouldBe false
    }

    should("support insertion") {
        SessionImpl.clear()
        SessionImpl.put("A", "B")
        SessionImpl.contains("A") shouldBe true
    }

    should("support get") {
        SessionImpl.clear()
        SessionImpl.put("A", "B")
        SessionImpl["A"] shouldBe "B"
    }

    should("support removal") {
        SessionImpl.clear()
        SessionImpl.put("A", "B")
        SessionImpl.put("C", "D")
        SessionImpl.contains("A") shouldBe true
        SessionImpl.contains("C") shouldBe true
        SessionImpl.remove("A")
        SessionImpl.contains("A") shouldBe false
        SessionImpl.contains("C") shouldBe true
    }

    should("clear itself") {
        SessionImpl.clear()
        SessionImpl.put("A", "B")
        SessionImpl.put("C", "D")
        SessionImpl.contains("A") shouldBe true
        SessionImpl.contains("C") shouldBe true
        SessionImpl.clear()
        SessionImpl.contains("A") shouldBe false
        SessionImpl.contains("C") shouldBe false
    }

    should("not throw on known removal") {
        SessionImpl.clear()
        SessionImpl.put("A", "B")
        shouldNotThrow<AraraException> {
            SessionImpl.remove("A")
        }
    }

    should("throw on unknown removal") {
        SessionImpl.clear()
        shouldThrow<AraraException> {
            SessionImpl.remove("A")
        }
    }

    should("throw on unknown getter") {
        SessionImpl.clear()
        SessionImpl.put("A", "B")
        SessionImpl["A"] shouldBe "B"
        shouldThrow<AraraException> {
            SessionImpl["C"]
        }
    }
})
