package com.github.cereda.arara.utils

import com.github.cereda.arara.configuration.Configuration
import com.github.cereda.arara.localization.Language
import io.kotlintest.shouldBe
import io.kotlintest.specs.ShouldSpec
import java.io.File

class CommonUtilsTest : ShouldSpec({
    should("format bytes correctly") {
        Configuration.put("execution.language", Language("en"))
        mapOf(800 to "800 B",
                1000 to "1.0 kB",
                1024 to "1.0 kB",
                1000000 to "1.0 MB").forEach { (key, value) ->
            CommonUtils.byteSizeToString(key.toLong()) shouldBe value
        }
    }

    should("generate correct CRC sums") {
        CommonUtils.calculateHash(File("../LICENSE")) shouldBe "17f430a5"
        CommonUtils.calculateHash(File("../CODE_OF_CONDUCT.md")) shouldBe "536c426f"
    }
})
