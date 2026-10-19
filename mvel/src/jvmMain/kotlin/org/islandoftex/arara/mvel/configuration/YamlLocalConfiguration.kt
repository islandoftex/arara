// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.configuration

import com.charleskorn.kaml.Yaml
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.localization.LanguageController

/** Reads the YAML configuration format into the shared configuration model. */
object YamlLocalConfiguration {
    /** Requires the historical `!config` header. */
    fun load(file: MPPPath): LocalConfiguration = runCatching {
        val text = file.readText()
        if (!text.startsWith("!config")) throw AraraException("Configuration should start with !config")
        Yaml.default.decodeFromString(LocalConfiguration.serializer(), text)
    }.getOrElse {
        throw AraraException(LanguageController.messages.ERROR_CONFIGURATION_GENERIC_ERROR, it)
    }
}
