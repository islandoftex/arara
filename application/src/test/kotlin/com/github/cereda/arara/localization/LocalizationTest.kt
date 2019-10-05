/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.localization

import ch.qos.cal10n.verifier.MessageKeyVerifier
import com.github.cereda.arara.model.AraraException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

/**
 * Tests the localizated messages, checking if all keys are set.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class LocalizationTest {
    /**
     * Helper method, checks the provided locale.
     *
     * @param locale The locale.
     * @return The size of the error list.
     */
    private fun check(locale: Locale): Int {
        val errors = MessageKeyVerifier(Messages::class.java).verify(locale)
        errors.forEach(System.err::println)
        return errors.size
    }

    /**
     * Verifies the English localization (provided by Paulo).
     */
    @Test
    fun verifyEnglish() {
        assertEquals(0, check(Locale("en")).toLong())
    }

    /**
     * Verifies the German localization (provided by Marco).
     */
    @Test
    fun verifyGerman() {
        assertEquals(0, check(Locale("de")).toLong())
    }

    /**
     * Verifies the Broad Norfolk localization (provided by Nicola).
     */
    @Test
    fun verifyBroadNorfolk() {
        assertEquals(0, check(Locale("en", "QN")).toLong())
    }

    /**
     * Verifies the Dutch localization (provided by Marijn).
     */
    @Test
    fun verifyDutch() {
        assertEquals(0, check(Locale("nl")).toLong())
    }

    /**
     * Verifies the Brazilian Portuguese localization (provided by Paulo).
     */
    @Test
    fun verifyBrazilianPortuguese() {
        assertEquals(0, check(Locale("pt", "BR")).toLong())
    }

    /**
     * Verifies the Italian localization (provided by Enrico).
     */
    @Test
    fun verifyItalian() {
        assertEquals(0, check(Locale("it")).toLong())
    }

    @Test
    fun checkLocaleInstantiation() {
        assertEquals("en", Language("en").locale.language)
        assertThrows<AraraException> {
            Language("quack")
        }
    }
}
