// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseTest : ShouldSpec({
    should("normalize paths") {
        val db = Database()
        db[Paths.get("./././")] = 1L
        (Paths.get("./") in db) shouldBe true
    }

    should("remove existent elements") {
        val db = Database()
        (Paths.get("") in db) shouldBe false
        db[Paths.get("")] = 1L
        (Paths.get("") in db) shouldBe true
        db.remove(Paths.get(""))
        (Paths.get("") in db) shouldBe false
    }
    should("throw when removing non-existent path") {
        shouldThrow<NoSuchElementException> { Database().remove(Paths.get("")) }
                .message shouldContain "non-existent path"
    }

    should("be able to perform save-load cycle") {
        withContext(Dispatchers.IO) {
            val tmp = Files.createTempFile("db", null)
            val db = Database()
            db[Paths.get("")] = 1L
            db[Paths.get("quack")] = 2L
            db.save(tmp)
            val dbCopy = Database.load(tmp)
            (Paths.get("") in db) shouldBe true
            (Paths.get("") in dbCopy) shouldBe true
            (Paths.get("quack") in db) shouldBe true
            (Paths.get("quack") in db) shouldBe true
            (Paths.get("no") in db) shouldBe false
            (Paths.get("no") in db) shouldBe false
        }
    }
})
