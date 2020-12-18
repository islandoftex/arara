// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.islandoftex.arara.api.files.MPPPath

class DatabaseTest : ShouldSpec({
    should("normalize paths") {
        val db = Database()
        db[MPPPath("./././")] = 1L
        (MPPPath("./") in db) shouldBe true
    }

    should("remove existent elements") {
        val db = Database()
        (MPPPath("") in db) shouldBe false
        db[MPPPath("")] = 1L
        (MPPPath("") in db) shouldBe true
        db.remove(MPPPath(""))
        (MPPPath("") in db) shouldBe false
    }
    should("throw when removing non-existent path") {
        shouldThrow<NoSuchElementException> { Database().remove(MPPPath("")) }
                .message shouldContain "non-existent path"
    }

    should("be able to perform save-load cycle") {
        withContext(Dispatchers.IO) {
            val tmp = MPPPath(tempfile("db").toPath())
            val db = Database()
            db[MPPPath("")] = 1L
            db[MPPPath("quack")] = 2L
            db.save(tmp)
            val dbCopy = Database.load(tmp)
            (MPPPath("") in db) shouldBe true
            (MPPPath("") in dbCopy) shouldBe true
            (MPPPath("quack") in db) shouldBe true
            (MPPPath("quack") in db) shouldBe true
            (MPPPath("no") in db) shouldBe false
            (MPPPath("no") in db) shouldBe false
        }
    }
})
