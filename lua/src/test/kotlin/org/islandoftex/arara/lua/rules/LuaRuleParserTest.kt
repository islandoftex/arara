// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath

class LuaRuleParserTest {
    private fun <T> withRule(body: String, block: (LuaRule) -> T): T {
        val path = MPPPath(Files.createTempFile("arara-rule", ".lua").toString())
        try {
            path.writeText(body)
            return block(LuaRuleParser.parse(path, "test"))
        } finally {
            Files.deleteIfExists(java.nio.file.Path.of(path.toString()))
        }
    }

    @Test
    fun closuresReceiveProcessedAndRawParameters() {
        withRule("""
            return {identifier='test', name='Test', authors={'A'},
              commands={{command=function(ctx) return {true, {ctx.mode[1] == '--draft'}} end,
                         exit=function(code) return code == 2 end}},
              arguments={{identifier='mode', default='draft', flag=function(ctx, value)
                assert(ctx.parameters.mode == value)
                assert(ctx.reference.file_name == 'test.tex')
                return '--' .. value
              end}}}
        """.trimIndent()) { rule ->
            val argument = rule.arguments.single()
            val reference = MPPPath("test.tex")
            assertEquals(listOf("--draft"), argument.processor(argument.defaultValue,
                mapOf("parameters" to emptyMap<String, Any>(), "reference" to reference)))
            val command = rule.commands.single()
            assertEquals(listOf(true, true), command.evaluate(mapOf("mode" to listOf("--draft"))))
            assertTrue(command.isSuccessful(2))
            assertFalse(command.isSuccessful(0))
        }
    }

    @Test
    fun invalidSchemaAndClosuresFailWithSourceContext() {
        val arguments = listOf(
            "{identifier='files', default='x'}", "{identifier='x', flag=3}",
            "{identifier='x', default='x', required='yes'}", "{identifier='x'}",
            "{identifier='x', default='x'}, {identifier='x', default='y'}"
        )
        arguments.forEach { entries ->
            val error = assertFailsWith<AraraException> {
                withRule("return {identifier='test', name='Test', commands={{command=function() end}}, " +
                    "arguments={$entries}}") { }
            }
            assertTrue(error.message.orEmpty().contains("test"))
        }
        withRule("return {identifier='test', name='Test', " +
            "commands={{command=function() error('broken') end, exit=function() return 'yes' end}}}") {
            assertFailsWith<AraraException> { it.commands.single().evaluate(emptyMap()) }
            assertFailsWith<AraraException> { it.commands.single().isSuccessful(0) }
        }
    }
}
