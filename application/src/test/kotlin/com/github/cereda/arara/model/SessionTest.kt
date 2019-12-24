package com.github.cereda.arara.model

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ShouldSpec

class SessionTest : ShouldSpec({
    should("include all environment variables") {
        Session.clear()
        Session.updateEnvironmentVariables()
        Session.contains("environment:PATH") shouldBe true
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

    should("throw on unknown removal") {
        Session.clear()
        Session.put("A", "B")
        shouldNotThrow<AraraException> {
            Session.remove("A")
        }
        shouldThrow<AraraException> {
            Session.remove("C")
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
