// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

/**
 * Verifies the directive extraction and validation: multi-line
 * directives through the continuation marker, the until and unless
 * conditionals, the file-list replication and its degenerate
 * shapes, and the rejections for orphan markers, reserved
 * parameters and unparseable values.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DirectiveParsingTest {
    @BeforeAll
    fun setUp() {
        DirectiveUtils.initializeDirectiveCore()
    }

    private fun extract(content: String): List<org.islandoftex.arara.api.rules.Directive> {
        val file = Files.createTempFile("arara-parse-", ".tex")
        Files.writeString(file, content)
        val projectFile = ProjectFile(MPPPath(file.toString()), FileType("tex", "^%.*"))
        val directives = projectFile.fetchDirectives(false)
        Files.deleteIfExists(file)
        return directives
    }

    @Test
    fun `assembles multi-line directives through the continuation marker`() {
        val directives = extract(
            """
            % arara: dummy: {
            % arara: -->   files: [ 'a.tex', 'b.tex' ] }
            """.trimIndent()
        )
        assertEquals(1, directives.size)
        assertEquals("dummy", directives[0].identifier)
    }

    @Test
    fun `rejects orphan continuation markers`() {
        assertFailsWith<AraraException> {
            extract("% arara: --> stray continuation\n")
        }
    }

    @Test
    fun `rejects invalid directive formats`() {
        assertFailsWith<AraraException> {
            extract("% arara: dummy with stray words\n")
        }
    }

    @Test
    fun `parses the until and unless conditionals`() {
        val until = extract("% arara: dummy until found('log', 'x')\n")
        assertEquals(DirectiveConditionalType.UNTIL, until[0].conditional.type)
        assertEquals("found('log', 'x')", until[0].conditional.condition)
        val unless = extract("% arara: dummy unless found('log', 'x')\n")
        assertEquals(DirectiveConditionalType.UNLESS, unless[0].conditional.type)
    }

    @Test
    fun `replicates directives over file lists`() {
        val projectFile = ProjectFile(
            MPPPath(Files.createTempDirectory("arara-replicate").toString()),
            FileType("tex", "^%.*")
        )
        val directives = extract(
            "% arara: dummy: { files: [ 'a.tex', 'b.tex' ] }\n"
        )
        val replicated = DirectiveUtils.process(projectFile, directives)
        assertEquals(2, replicated.size, "the directive replicates over each file")
        assertTrue(
            replicated.all { it.parameters.containsKey("reference") },
            "each replica carries its own file reference"
        )
    }

    @Test
    fun `rejects degenerate file lists`() {
        val projectFile = ProjectFile(
            MPPPath(Files.createTempDirectory("arara-degenerate").toString()),
            FileType("tex", "^%.*")
        )
        assertFailsWith<AraraException> {
            DirectiveUtils.process(projectFile, extract("% arara: dummy: { files: [] }\n"))
        }
        assertFailsWith<AraraException> {
            DirectiveUtils.process(
                projectFile,
                extract("% arara: dummy: { files: 'a.tex' }\n")
            )
        }
    }

    @Test
    fun `rejects reserved parameters`() {
        val projectFile = ProjectFile(
            MPPPath(Files.createTempDirectory("arara-reserved").toString()),
            FileType("tex", "^%.*")
        )
        assertFailsWith<AraraException> {
            DirectiveUtils.process(
                projectFile,
                extract("% arara: dummy: { reference: 'x' }\n")
            )
        }
    }

    @Test
    fun `rejects unparseable directive parameters`() {
        assertFailsWith<AraraException> {
            extract("% arara: dummy: { broken: [ }\n")
        }
    }

    @Test
    fun `rejects unsupported rule formats`() {
        assertFailsWith<AraraException>(
            "rule files of unknown formats must be rejected"
        ) {
            RuleUtils.parseRule(MPPPath("not-a-rule.quack"), "dummy")
        }
    }

    @Test
    fun `rejects uninterpolable orb tags in directive options`() {
        assertFailsWith<AraraException>(
            "an orb tag that cannot be evaluated must be rejected"
        ) {
            extract("% arara: dummy: { options: \"@{ 1 + }\" }\n")
        }
    }

    @Test
    fun `converts plain numeric scalars into numbers`() {
        val directive = extract("% arara: dummy: { answer: 42, ratio: 1.5 }\n").single()
        assertEquals(42, directive.parameters["answer"], "an integer scalar converts")
        assertEquals(1.5, directive.parameters["ratio"], "a float scalar converts")
    }

    @Test
    fun `rejects null values in directive parameters`() {
        assertFailsWith<AraraException>(
            "a null value in a directive parameter is rejected"
        ) {
            extract("% arara: dummy: { answer: ~ }\n")
        }
    }
}
