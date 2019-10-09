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
package com.github.cereda.arara.model

import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.ruleset.Directive
import com.github.cereda.arara.ruleset.DirectiveUtils
import com.github.cereda.arara.utils.CommonUtils
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

/**
 * It extracts directives from the provided main file.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object Extractor {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    /**
     * Extracts a list of directives from the provided main file, obtained from
     * the configuration controller.
     * @param file The file to extract the directives from.
     * @param charset The charset of the file.
     * @return A list of directives.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun extract(file: File, charset: Charset = Charsets.UTF_8):
            List<Directive> {
        try {
            val content = CommonUtils.preambleContent.toMutableList()
            content.addAll(file.readLines(charset))
            return DirectiveUtils.extractDirectives(content)
        } catch (ioexception: IOException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_EXTRACTOR_IO_ERROR
                    ),
                    ioexception
            )
        }

    }
}
