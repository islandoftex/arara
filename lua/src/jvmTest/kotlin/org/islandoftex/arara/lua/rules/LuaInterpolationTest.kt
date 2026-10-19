// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException

class LuaInterpolationTest {
    @Test
    fun tagsSupportNestedTablesEscapesLongStringsAndComments() {
        assertEquals("literal", LuaInterpolation.interpolate("literal"))
        assertEquals("a3btruec", LuaInterpolation.interpolate("a{{1+2}}b{{true}}c{{nil}}"))
        assertEquals("a,b", LuaInterpolation.interpolate("{{ table.concat({'a','b'}, ',') }}"))
        assertEquals("}}", LuaInterpolation.interpolate("{{ [=[}}]=] }}"))
        assertEquals("a'}},b", LuaInterpolation.interpolate("{{ 'a\\\'}}' .. ',b' }}"))
        assertEquals("3", LuaInterpolation.interpolate("{{ 1 -- }} ignored\n + 2 }}"))
        assertEquals("3", LuaInterpolation.interpolate("{{ 1 --[=[ }} ignored ]=]\n + 2 }}"))
    }

    @Test
    fun malformedAndWrongEngineTagsFail() {
        listOf("{{ 1", "{{ 'abc }}", "{{ [=[ abc }}", "@{1}", "{{ missing(4) }}", "{{ error('broken') }}")
            .forEach { assertFailsWith<AraraException> { LuaInterpolation.interpolate(it) } }
    }
}
