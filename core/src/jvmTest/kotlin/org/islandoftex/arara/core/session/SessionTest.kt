// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.api.AraraException

class SessionTest : ShouldSpec({
    should("include all environment variables") {
        Session.clear()
        Session.updateEnvironmentVariables()
        // on Linux it is called PATH, on Windows it may be Path
        (Session.contains("environment:PATH") ||
                Session.contains("environment:Path")) shouldBe true
    }

    should("properly check existence") {
        Session.clear()
        Session.put("A", "B")
        Session.contains("A") shouldBe true
        Session.contains("C") shouldBe false
    }

    should("support insertion") {
        Session.clear()
        Session.put("A", "B")
        Session.contains("A") shouldBe true
    }

    should("support get") {
        Session.clear()
        Session.put("A", "B")
        Session["A"] shouldBe "B"
    }

    should("support removal") {
        Session.clear()
        Session.put("A", "B")
        Session.put("C", "D")
        Session.contains("A") shouldBe true
        Session.contains("C") shouldBe true
        Session.remove("A")
        Session.contains("A") shouldBe false
        Session.contains("C") shouldBe true
    }

    should("clear itself") {
        Session.clear()
        Session.put("A", "B")
        Session.put("C", "D")
        Session.contains("A") shouldBe true
        Session.contains("C") shouldBe true
        Session.clear()
        Session.contains("A") shouldBe false
        Session.contains("C") shouldBe false
    }

    should("not throw on known removal") {
        Session.clear()
        Session.put("A", "B")
        shouldNotThrow<AraraException> {
            Session.remove("A")
        }
    }

    should("throw on unknown removal") {
        Session.clear()
        shouldThrow<AraraException> {
            Session.remove("A")
        }
    }

    should("throw on unknown getter") {
        Session.clear()
        Session.put("A", "B")
        Session["A"] shouldBe "B"
        shouldThrow<AraraException> {
            Session["C"]
        }
    }
})
