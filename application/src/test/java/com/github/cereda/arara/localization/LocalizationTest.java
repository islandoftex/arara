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
package com.github.cereda.arara.localization;

import ch.qos.cal10n.verifier.Cal10nError;
import ch.qos.cal10n.verifier.IMessageKeyVerifier;
import ch.qos.cal10n.verifier.MessageKeyVerifier;
import com.github.cereda.arara.model.Messages;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Tests the localizated messages, checking if all keys are set.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class LocalizationTest {

    /**
     * Helper method, checks the provided locale.
     *
     * @param locale The locale.
     * @return The size of the error list.
     */
    private int check(Locale locale) {
        IMessageKeyVerifier verifier = new MessageKeyVerifier(Messages.class);
        List<Cal10nError> errors = verifier.verify(locale);
        for (Cal10nError error : errors) {
            System.err.println(error);
        }
        return errors.size();
    }

    /**
     * Verifies the English localization (provided by Paulo).
     */
    @Test
    public void verifyEnglish() {
        assertEquals(0, check(new Locale("en")));
    }

    /**
     * Verifies the German localization (provided by Marco).
     */
    @Test
    public void verifyGerman() {
        assertEquals(0, check(new Locale("de")));
    }

    /**
     * Verifies the Broad Norfolk localization (provided by Nicola).
     */
    @Test
    public void verifyBroadNorfolk() {
        assertEquals(0, check(new Locale("en", "QN")));
    }

    /**
     * Verifies the Dutch localization (provided by Marijn).
     */
    @Test
    public void verifyDutch() {
        assertEquals(0, check(new Locale("nl")));
    }

    /**
     * Verifies the Brazilian Portuguese localization (provided by Paulo).
     */
    @Test
    public void verifyBrazilianPortuguese() {
        assertEquals(0, check(new Locale("pt", "BR")));
    }

    /**
     * Verifies the Italian localization (provided by Enrico).
     */
    @Test
    public void verifyItalian() {
        assertEquals(0, check(new Locale("it")));
    }

}
