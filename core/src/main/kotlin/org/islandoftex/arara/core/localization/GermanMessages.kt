// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/** German message catalog. */
@Suppress("MaxLineLength", "TooManyFunctions")
object GermanMessages : AraraMessages {

    override fun infoBuildDirectoryValidating(path: String): String =
        "Build-Verzeichnis wird geprüft: $path"

    override fun infoBuildDirectoryPreparing(path: String): String =
        "Build-Verzeichnis wird vorbereitet: $path"

    override fun infoBuildDirectoryCleaning(path: String): String =
        "Build-Verzeichnis wird bereinigt: $path"

    override fun infoBuildDirectoryPrepared(files: Int, seconds: String): String =
        "Build-Verzeichnis bereit: $files Eingabedateien in $seconds s kopiert."

    override val infoBuildDirectoryValidated: String =
        "Build-Verzeichnis geprüft."

    override val infoBuildDirectoryCleaned: String =
        "Build-Verzeichnis bereinigt."

    override val errorBuildDirectoryOperationFailed: String =
        "Vorgang im Build-Verzeichnis fehlgeschlagen."


    override fun errorScriptChoice(field: String, choices: String): String =
        "Ungültiger Wert für $field; erwartet: $choices."

    override fun errorBuildDirectoryInvalid(path: String): String =
        "Ungültiges oder überlappendes Build-Verzeichnis: $path."

    override fun errorBuildInputInvalid(path: String): String =
        "Eingabe oder Ziel liegt außerhalb der Arbeitsverzeichnis-Zuordnung: $path."

    override fun errorBuildWorkspaceUnmanaged(path: String): String =
        "Das Build-Verzeichnis wird nicht von diesem Projekt verwaltet: $path."

    override fun errorBuildInputCollision(path: String): String =
        "Build-Eingabe kollidiert mit einer anderen Eingabe oder einer vorhandenen Ausgabe: $path."

    override fun errorBuildLinkCycle(path: String): String =
        "Zyklus symbolischer Links in den Build-Eingaben: $path."

    override fun errorBuildInputMissing(path: String): String =
        "Build-Eingabe fehlt oder ist keine reguläre Datei und kein Verzeichnis: $path."

    override val warningDeprecatedYaml: String =
        "Kompatibilitätswarnung: Sie haben veraltete YAML-Regeln verwendet, die in v9 entfernt werden."

    override val providedLocale: MPPLocale = MPPLocale("de")

    override val translators: List<String> = listOf("Marco Daniel", "Ben Frank", "Gert Fischer")

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "Beim Extrahieren der Direktiven von '${file}' ist ein Fehler aufgetreten: ${details}."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "Das Glob-Muster \"${pattern}\" ließ sich nicht in einen regulären Ausdruck umwandeln: ${details}. Bitte stelle sicher, dass das Muster wohlgeformt ist, und versuche es erneut."

    override val errorSafeModeClassLoading =
        "Im sicheren Modus dürfen Regeln keine beliebigen Klassen aus benutzerseitigen Archiven laden."

    override val errorSafeModeSystemCommand =
        "Im sicheren Modus dürfen Regeln keine beliebigen Systembefehle innerhalb eines Ausführungsschritts ausführen."

    override val errorDialogInputClosed: String =
        "Es scheint, als sei die Terminaleingabe beendet worden, w\u00e4hrend ein Dialog auf eine Antwort wartete, daher kann ich Ihre Auswahl nicht lesen. Bitte f\u00fchre arara in einem interaktiven Terminal aus, damit der Dialog beantwortet werden kann."

    override val errorDialogsNotConfigured: String =
        "Es scheint, als sei f\u00fcr diese Ausf\u00fchrung keine Dialogunterst\u00fctzung konfiguriert, daher kann ich den von einer Regel angeforderten Dialog nicht anzeigen. Bitte stelle eine Dialog-Implementierung in der Anwendung bereit, die arara ausf\u00fchrt, oder vermeide Regeln mit interaktiven Dialogen."

    override fun errorScriptString(field: String): String =
        "${field} muss eine Zeichenkette sein."

    override fun errorScriptTable(field: String): String =
        "${field} muss eine Tabelle sein."

    override fun errorScriptBoolean(field: String): String =
        "${field} muss ein boolescher Wert sein."

    override fun errorScriptInteger(field: String): String =
        "${field} muss eine ganze Zahl sein."

    override fun errorScriptList(field: String): String =
        "${field} muss eine lückenlose Liste mit Indizes ab eins sein."

    override fun errorScriptUnknownKey(field: String): String =
        "Unbekannter Schlüssel ${field}."

    override fun errorScriptRequired(field: String): String =
        "${field} ist erforderlich."

    override fun errorScriptNonblank(field: String): String =
        "${field} darf nicht leer sein."

    override fun errorScriptDuplicate(field: String, value: String): String =
        "${field} wiederholt '${value}'."

    override fun errorScriptDirectory(field: String): String =
        "${field} ist ein Verzeichnis."

    override fun errorScriptEmpty(field: String): String =
        "${field} muss mindestens einen Eintrag enthalten."

    override fun errorScriptStringKeys(field: String): String =
        "${field} erfordert nichtleere Zeichenketten als Schlüssel."

    override fun errorProjectInvalidLua(path: String, detail: String): String =
        "Ungültiges Lua in ${path}: ${detail}"

    override fun errorProjectDuplicateNames(names: String): String =
        "Projektnamen müssen eindeutig sein: ${names}."

    override fun errorProjectUnknownDependency(project: String, dependency: String): String =
        "Projekt '${project}' hat die unbekannte Abhängigkeit '${dependency}'."

    override fun errorProjectCycle(cycle: String): String =
        "Die Projektabhängigkeiten enthalten einen Zyklus: ${cycle}."

    override fun errorProjectFileOwnership(path: String, owner: String, project: String): String =
        "Datei '${path}' gehört sowohl zu '${owner}' als auch zu '${project}'."

    override fun errorProjectFiletype(path: String): String =
        "Kein konfigurierter Dateityp passt zu '${path}'."

    override fun errorConfigurationPrepend(value: String): String =
        "Ungültiges preamble.prepend '${value}'; verwenden Sie 'always' oder 'without-directives'."

    override fun errorScriptFunction(field: String): String =
        "${field} muss eine Funktion sein."

    override fun errorScriptLiteral(field: String): String =
        "${field} muss ein literaler Wert sein."

    override fun errorScriptAlternatives(field: String, alternatives: String): String =
        "${field} erfordert mindestens eines von ${alternatives}."

    override val errorLuaInterpolationFailure: String =
        "Das Feld 'options' enthält einen ungültigen Lua-Ausdruck oder Interpolationstag."

    override val errorYamlTagsInLua: String =
        "MVEL-Tags erfordern die YAML-Engine; Lua verwendet {{ expression }}."

    override val errorLuaTagsInYaml: String =
        "Lua-Tags erfordern eine Lua-Regel."

    override val errorLuaUnterminatedTag: String =
        "Interpolations-Tag nicht abgeschlossen."

    override val errorLuaUnterminatedQuote: String =
        "Nicht abgeschlossene Zeichenkette im Interpolationstag."

    override val errorLuaUnterminatedLongString: String =
        "Nicht abgeschlossene lange Zeichenkette im Interpolationstag."

    override val errorLuaMapKeys: String =
        "Map-Schlüssel ergeben gleiche Lua-Zeichenketten."

    override val errorLuaTableKeys: String =
        "Lua-Tabellenschlüssel ergeben gleiche Zeichenketten."

    override fun errorLuaExactInteger(value: String): String =
        "Die ganze Zahl ${value} kann nicht exakt als Lua-Zahl dargestellt werden."

    override val errorLuaCyclicValues: String =
        "Zyklische Tabellen und Sammlungen können nicht konvertiert werden."

    override fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String =
        "Ungültiges Argument Nr. ${index} für '${helper}' (${expected} erwartet, ${actual} erhalten)"

    override fun errorLuaBooleanResult(helper: String, actual: String): String =
        "Ungültiges Ergebnis von '${helper}' (boolean erwartet, ${actual} erhalten)"

    override fun errorLuaHelperNoArgs(helper: String): String =
        "${helper} erfordert mindestens ein Argument."

    override fun errorLoadCouldNotLoadDatabase(name: String): String =
        "Die Datenbank '${name}' konnte nicht geladen werden. Prüfen Sie Zugriffsrechte und den Inhalt des YAML-Caches."

    override fun errorSaveCouldNotSaveDatabase(name: String): String =
        "Die Datenbank '${name}' konnte nicht gespeichert werden. Prüfen Sie, ob das übergeordnete Verzeichnis existiert und beschreibbar ist."

    override fun errorBasenameNotAFile(path: String): String =
        "Die Methode des \"Dateinamens\" (\"basename\") benötigt eine Datei und kein Verzeichnis. Es scheint, als sei \"${path}\" keine Datei. Falls du Aufgaben auf ein Verzeichnis bzw. Ordner anwenden willst, kannst du eine Vielzahl von JAVA API-Methoden verwenden."

    override val errorCalculateHashIoException: String =
        "Aus was für einem Grund auch immer kann ich die Prüfziffer (checksum) nicht berechnen. Ich habe keine Idee, warum es fehlschlägt. Vielleicht wurde die Datei bevor oder während der Prüfziffer-Operation bewegt oder gelöscht. Möglicherweise habe ich nicht die geeigneten Rechte, um die Datei zu lesen."

    override fun errorCheckBooleanNotValidBoolean(value: String): String =
        "Es scheint, als sei \"${value}\" kein logischer Ausdruck (boolean value). Das sollte leicht zu beheben sein. Stelle sicher, dass der Eingabestring ein zugelassener logischer Ausdruck ist (\"yes\" oder \"no\", \"true\" oder \"false\", \"1\" oder \"0\" sowie \"on\" oder \"off\")."

    override fun errorCheckRegexIoException(file: String): String =
        "Ich konnte den Inhalt der Datei \"${file}\" nicht lesen. Ich erhielt einen IO-Fehler. Ich habe keine Idee für die Fehlerursache, obwohl?. Vielleicht wurde die Datei bevor oder während des Leseprozesses bewegt oder gelöscht. Möglicherweise habe ich nicht die geeigneten Rechte, um die Datei zu lesen."

    override val errorConfigurationGenericError: String =
        "Ich konnte die Konfigurationsdatei nicht analysieren, etwas schlechtes passierte. Dieser Teil ist ziemlich knifflig, denn er bezieht das zugrundeliegende Datenserialisierungsformat mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override val errorConfigurationLoopsInvalidRange: String =
        "In der Konfigurationsdatei hat der eingegebenen Wert 'loops' zur Festlegung der maximalen Anzahl an Schleifendurchgängen einen ungültigen Bereich. Bitte stelle sicher, dass eine positive Zahl eingetragen wird."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String): String =
        "Ich konnte die vorausgesetzte Datei \"${file}\" ${extensions} nicht finden. Bitte stelle sicher, dass die Datei existiert und eine gültige Dateiendung hat."

    override fun errorDiscoverFileSafeModeNoCompletion(file: String) =
        "Ich konnte die vorausgesetzte Datei \"${file}\" nicht finden. Der sichere Modus vervollständigt Dateiverweise nicht aus ihren Dateiendungen, daher gib bitte den vollständigen Dateinamen mit Dateiendung an."

    override fun errorDiscoverFileUnknownExtension(
        file: String,
        extensions: String
    ) =
        "Ich konnte den Dateityp von '${file}' nicht bestimmen: Kein Dateityp dieses Durchlaufs trägt seine Erweiterung. Die Erweiterungen dieses Durchlaufs sind ${extensions}. Ergänze die fehlende Erweiterung in den 'file_types' (Lua) / 'filetypes' (YAML) der Konfiguration."

    override fun errorDiscoverFileUnknownLuaExtension(
        file: String,
        extensions: String
    ) =
        "Ich konnte den Dateityp von '${file}' nicht bestimmen: Kein Dateityp dieses Durchlaufs trägt seine Erweiterung. Die Erweiterungen dieses Durchlaufs sind ${extensions}. Ergänze die Erweiterung in den 'file_types' (Lua) / 'filetypes' (YAML) der Konfiguration - oder, falls die Datei eine Projektspezifikation ist, trägt sie das Suffix '.proj.lua'."

    override val errorEvaluateCompilationFailed: String =
        "Aus was für einen Grund auch immer kann ich den Ausdruck in der bereitgestellten Bedingung nicht kompilieren. Dieser Teil ist ziemlich knifflig, denn er bezieht die zugrundeliegende Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override val errorEvaluateNotBooleanValue: String =
        "Die Auswertung der Bedingung (conditional) hat einen boolschen Ausdruck als Ergebnis erwartet. Das sollte leicht zu beheben sein. Stelle einfach sicher, dass die Bedingung einen booleschen Ausdruck ('yes' oder 'no', 'true' oder 'false', '1' oder '0' sowie 'on' oder 'off') als Ergebnis erhält."

    override val errorExtractorIoError: String =
        "Es gab einen IO-Fehler während ich versuchte, die Direktive zu extrahieren. Ich habe keine Idee, warum es fehlschlug. Vielleicht wurde die Datei bevor oder während der Prüfziffer-Operation bewegt oder gelöscht. Möglicherweise habe ich nicht die geeigneten Rechte, um die Datei zu lesen."

    override val errorExtractorInterpolationFailure: String =
        "Das 'Options'-Feld enthält einen Eintrag, mit dem der MVEL-Parser nicht umgehen kann. Bitte überprüfen Sie alle '@{…}'-Teile im 'Options'-Feld."

    override fun errorFiletypeInvalidPattern(pattern: String): String =
        "Es sieht so aus, als sei das Muster \"${pattern}\", das du für diesen Dateityp festlegen wolltest, kein gültiger regulärer Ausdruck. Das sollte leicht zu beheben sein: Stelle sicher, dass du einen gültigen Ausdruck verwendest, und versuche es erneut."

    override fun errorFiletypeNotAFile(path: String): String =
        "Die Methode der Dateitypen \"filetype\" fordert als Eingabe eine Datei und kein Verzeichnis. Es scheint, als sei \"${path}\" keine Datei. Falls du Aufgaben auf ein Verzeichnis bzw. Ordner anwenden willst, kannst du eine Vielzahl von JAVA API-Methoden verwenden."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String): String =
        "Ich kann \"${extension}\" als keine vorgegebene Dateiendung erkennen. Falls du einen neuen Dateitypen definieren möchtest, stelle sich, dass die Dateiendung sowie die entsprechende Struktur bereitgestellt wird. Das sind die vordefinierten Dateiendungen: ${extensions}"

    override val errorGetApplicationPathEncodingException: String =
        "Es gab ein Kodierungsproblem während ich versuchte den Anwendungspfad zu erhalten. Es gibt leider nicht viel, was ich tun kann."

    override fun errorInterpreterArgumentIsRequired(argument: String): String =
        "Es scheint, als sei \"${argument}\" eine geforderte Eingabe in der Regel, aber ich kann sie in den Eingabeparametern nicht finden. Bitte stelle sicher, dass der Parameter in deiner Direktive ergänzt wird und versuche es erneut."

    override val errorInterpreterCommandRuntimeError: String =
        "Ich konnte eines der genutzten Kommandos nicht auswerten. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String): String =
        "Das Kommando \"${command}\" wurde unerwarteterweise mit einem Nicht-Null-Rückgabewert beendet. Bitte analysieren Sie den Output des Kommandos und ziehen Sie daraus Schlüsse auf mögliche Lösungen des Problems."

    override val errorInterpreterUserRequestedHalt: String =
        "arara wird auf Ihren Wunsch gestoppt."

    override val errorInterpreterExitRuntimeError: String =
        "Ich kann den Status der Fertigmeldung (exit status) von einer der genutzten Anweisungen nicht auswerten. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override val errorInterpreterFlagRuntimeException: String =
        "Ich kann den Statusindikator (flag expression) eines Argumentes nicht auswerten. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override fun errorLogFormatUnknown(value: String): String =
        "Unbekanntes Protokollformat '${value}'. Verwenden Sie none, text oder json."

    override fun errorTerminalWidthInvalid(minimum: Int): String =
        "Die Terminalbreite muss mindestens ${minimum} Spalten betragen."

    override fun errorConsoleModeUnknown(mode: String): String =
        "Unbekannter Konsolenmodus '${mode}'. Verwenden Sie plain oder styled."

    override fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String =
        "Nicht unterstütztes Regelformat für '${identifier}': '${format}'. Verwenden Sie eine Lua- oder YAML-Regeldatei."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String): String =
        "Ich konnte keine Regel mit dem Namen \"${identifier}\" in den hinterlegten Regelverzeichnissen finden. Vielleicht ein falsch geschriebenes Wort? Ich habe nach dem Dateinamen \"${expected}\" in den nachstehenden Verzeichnissen mit entsprechender Priorität gesucht: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String): String =
        "Ich habe folgende unbekannte Keys in der Direktive gefunden: ${keys}. Das sollte leicht zu beheben sein, entferne sie einfach."

    override val errorInterpreterWrongExitClosureReturn: String =
        "Der Ausdruck 'exit' muss immer einen logischen Wert (boolean value) zurückgeben (sogar wenn es keine Berechnung in der Prozedur gibt). Das sollte leicht zu beheben sein: Stelle einen korrekten Rückgabewert sicher und versuche es erneut."

    override val errorInterpreterWrongReturnType: String =
        "Eine Regel darf nur entweder einen logischen Wert (boolean value) oder ein Kommando zurückgeben. Andere Rückgaben werden nicht unterstützt."

    override val errorInvalidProjectFile: String =
        "I have read a file that was supposed to be a project. Unfortunately, it turned out it was neither a project nor a list of projects. Please double-check that your project file follows the documented format."

    override fun errorLoadCouldNotLoadXml(name: String): String =
        "Ich konnte die YAML-Datenbank mit dem Namen \"${name}\" nicht laden. Ich habe keine Idee für die Fehlerursache, obwohl? Vielleicht wurde die Datei bevor oder während der Leseoperation bewegt oder gelöscht. Möglicherweise habe ich nicht die geeigneten Rechte, um die Datei zu lesen."

    override fun errorParserInvalidPreamble(name: String): String =
        "Leider konnte die Präambel \"${name}\" nicht gefunden werden. Bitte stelle sicher, dass dieser Schlüssel in der Konfigurationsdatei existiert."

    override val errorParseRuleGenericError: String =
        "Ich konnte die Regel nicht auswerten, etwas schlechtes passierte. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Serialisierung mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override val errorReplicateListMissingFormatArgumentsException: String =
        "Ich konnte die Liste auf Grund eines fehlendes Formatargumentes nicht replizieren. Meine Vermutung ist, dass es weniger (oder mehr) Parameter als erwartet gibt. Stelle sicher, dass die korrekte Anzahl an Parametern übergeben wird und versuche es erneut."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String): String =
        "Ich habe einen Fehler in der Regel \"${identifier}\" an der Stellte \"${location}\" entdeckt."

    override val errorRunGenericException: String =
        "Ich konnte das geforderte Systemkommando nicht ausführen, etwas schlechtes passierte. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override val errorRunInterruptedException: String =
        "Das aufgerufene Systemkommando ist plötzlich unterbrochen worden. Vielleicht gab es eine externe Unterbrechung, die das Kommando abrupt zur Beendigung zwang."

    override val errorRunInvalidExitValueException: String =
        "Das aufgerufene Systemkommando hat einen ungültigen Abschluss gemeldet."

    override val errorRunIoException: String =
        "Der Aufruf des Systemkommandos misslang auf Grund eines IO-Fehlers. Bist du dir sicher, dass das Systemkommando in deinem Pfad existiert? Es sollte eine gute Idee sein, die Systempfade sowie die Verfügbarkeit des Kommandos zu prüfen."

    override val errorRunTimeoutException: String =
        "Die Ausführung des Systemkommandos hat die Zeitbegrenzung erreicht und wurde abgebrochen. Falls die Zeit etwas zu kurz war, kannst du diese erhöhen."

    override fun errorSaveCouldNotSaveXml(name: String): String =
        "Ich konnte die YAML-Datenbank mit dem Namen \"${name}\" nicht speichern. Ich habe keine Idee für die Fehlerursache, obwohl? Möglicherweise habe ich nicht die geeigneten Rechte, um die Datei YAML-Datei auf der Festplatte zu speichern."

    override fun errorSessionObtainUnknownKey(key: String): String =
        "Die \"get\" Methode hat den unbekannten Eintrag (key) \"${key}\" in der Session gefunden. Ich konnte nichts erhalten, was ich nicht bereits habe. Bitte gebe einen gültigen Key ein und versuche es erneut."

    override fun errorSessionRemoveUnknownKey(key: String): String =
        "Die \"remove\" Methode hat den unbekannten Eintrag (key) \"${key}\" in der Session gefunden. Ich konnte nichts erhalten, was ich nicht bereits habe. Bitte gebe einen gültigen Key ein und versuche es erneut."

    override fun errorValidateEmptyFilesList(lines: String): String =
        "Ich habe die Direktive ${lines} gelesen und fand heraus, dass die bereitgestellte Dateienliste leer ist. Das sollte leicht zu beheben sein. Bitte stelle sicher, dass die Liste mindestens ein Element hat und versuche es erneut."

    override fun errorValidateFilesIsNotAList(lines: String): String =
        "Ich habe die Direktive ${lines} gelesen und fand heraus, dass der Eintrag \"files\" keine Liste übergeben bekommen hat. Bitte stelle sicher, dass du eine passende Liste verwendest und versuche es erneut."

    override fun errorValidateInvalidDirectiveFormat(lines: String): String =
        "Ich entdecke eine ungültige Direktive namens ${lines} in der bereitgestellten Datei. Stellte sicher, dass die Direktive gültig ist und versuche es erneut."

    override val errorValidateNoDirectivesFound: String =
        "Es sieht so aus, als wurden keine Direktiven in der bereitgestellten Datei gefunden. Bitte stelle sicher, dass wenigstens eine Direktive enthalten ist und versuche es erneut."

    override fun errorValidateOrphanLinebreak(line: String): String =
        "Anscheinend gibt es einen verwaisten Zeilenumbruch in der Direktive in der Zeile ${line}. Ich kann nicht fortfahren. Bitte korrigiere die Direktive und versuche es erneut."

    override fun errorValidateReferenceIsReserved(lines: String): String =
        "Ich las die Direktive ${lines} und fand heraus, dass der Key \"reference\" genutzt wurde. Dieser Key ist reserviert, deshalb kannst du ihn nicht nutzen. Aber keine Sorge, das sollte einfach zu beheben sein. Du musst nur einen anderen Namen verwenden."

    override fun errorValidateYamlException(lines: String): String =
        "Es gab ein Problem mit dem bereitgestellten YAML-Ausdruck in der Direktive ${lines}. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Serialisierung mit ein."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String): String =
        "Die Argumentenbezeichnung \"${identifier}\" ist reserviert, somit kannst du sie nicht nutzen. Dies sollte einfach zu beheben sein. Du musst nur einen anderen Namen verwenden."

    override val errorValidateBodyDuplicateArgumentIdentifiers: String =
        "Anscheinend hast du Duplikate von Bezeichnern in deiner Regel. Bitte behebe diesen Fehler und versuche es erneut."

    override val errorValidateBodyMissingKeys: String =
        "Wenn du ein Argument einer Regel definierst, musst du mindestens 'flag' oder 'default' nutzen. Stelle bitte sicher, dass du mindestens eines nutzt."

    override val errorValidateBodyNullArgumentId: String =
        "Ich habe herausgefunden, dass eines der Argumente keinen Bezeichner hat. Bitte ergänze einen gültigen Bezeichner im Argument und versuche es erneut."

    override val errorValidateBodyNullCommand: String =
        "Ich habe eine Nullanweisung in der vorgegebenen Regel gefunden. Bitte stelle sicher, dass ein gültiges Kommando zur Regel hinzugefügt wird."

    override val errorValidateHeaderNullName: String =
        "Die gewünschte Regel hat keinen Namen. Das sollte leicht zu beheben sein. Ergänze einen gültigen Namen und versuche es erneut."

    override fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String =
        "Die Regel hat einen falschen Bezeichner. Ich erwartete \"${expected}\" fand aber \"${actual}\". Das solle leicht zu beheben sein: Ersetze einfach den falschen durch einen korrekten Bezeichner."

    override val infoLabelProject = "Projekt:"

    override val infoDisplaySummaryTitle = "Zusammenfassung"

    override val infoDisplaySummaryFile = "Datei"

    override val infoDisplaySummaryTasks = "Aufgaben"

    override val infoDisplaySummaryTime = "Zeit"

    override val infoDisplayFullOutputInLog =
        "die vollständige Ausgabe steht in der Logdatei"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "GESAMTERFOLG · ${if (files == 1) "1 Datei" else "$files Dateien"}, " +
            "${if (tasks == 1) "1 Aufgabe" else "$tasks Aufgaben"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "GESAMTFEHLER · ${if (files == 1) "1 Datei" else "$files Dateien"}, " +
            "${if (tasks == 1) "1 Aufgabe" else "$tasks Aufgaben"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "PROBELAUF · ${if (tasks == 1) "1 Aufgabe" else "$tasks Aufgaben"}"

    override val infoConsoleSkipped: String =
        "Übersprungen"

    override val infoConsoleUnprocessed: String =
        "Nicht verarbeitet"

    override val infoDisplayExceptionMoreDetails: String =
        "Es sind mehr Details für diese Ausnahme verfügbar."

    override fun infoDisplayExecutionTime(time: String): String =
        "Gesamt: ${time} Sekunden"

    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String =
        "Verarbeitung \"${name}\" (Größe: ${size}, letzte Modifikation: ${lastModified}), bitte warten."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String =
        "Trotz dry-run Modus wurde dieser Eintrag wahrscheinlich bereits verarbeitet, da er den boolschen Wert ${value} zurückgegeben hat."

    override fun infoInterpreterDryRunModeSystemCommand(command: String): String =
        "Los geht\"s: ${command}"

    override val infoLabelAuthor: String =
        "Autor:"

    override val infoLabelAuthors: String =
        "Autoren:"

    override val infoLabelSystemCommand = "Systembefehl:"

    override val infoLabelConditional: String =
        "Bedingung:"

    override val infoLabelNoAuthors: String =
        "Kein Autor berücksichtigt"

    override val infoLabelOnWarning: String =
        "WARNUNG"

    override val infoLabelOnQuestion: String =
        "FRAGE"

    override val infoLabelOnInfo: String =
        "INFO"

    override val infoLabelDialogPrompt: String =
        "W\u00e4hle eine Option"

    override val infoLabelOnDetails: String =
        "DETAILS"

    override val infoLabelOnError: String =
        "FEHLER"

    override val infoLabelOnFailure: String =
        "MISSERFOLG"

    override val infoLabelOnSuccess: String =
        "ERFOLGREICH"

    override val infoLabelUnnamedTask: String =
        "Namenlose Aufgabe"

    override val infoParserNotes: String =
        "arara ist unter der New BSD license veröffentlicht."

    override val logInfoBeginBuffer: String =
        "BEGIN OUTPUT BUFFER"

    override fun logInfoBooleanMode(value: Boolean): String =
        "Boolscher Wert: ${value}"

    override val logInfoDirectivesBlock: String =
        "DIREKTIVEN"

    override val logInfoEndBuffer: String =
        "END OUTPUT BUFFER"

    override fun logInfoInterpretRule(rule: String): String =
        "Ich bin bereit, die Regel \"${rule}\" zu interpretieren."

    override fun logInfoInterpretTask(task: String, rule: String): String =
        "Ich bin bereit, die Aufgabe \"${task}\" von der Regel \"${rule}\" zu interpretieren."

    override fun logInfoPotentialPatternFound(line: String, pattern: String): String =
        "Ich habe ein potentielles Muster in der Linie ${line} gefunden: ${pattern}"

    override fun logInfoRuleLocation(path: String): String =
        "Verzeichnis der Regel: \"${path}\""

    override fun logInfoSystemCommand(command: String): String =
        "Systemkommando: ${command}"

    override val logInfoTaskResult: String =
        "Resultat der Aufgabe:"

    override val logInfoValidatedDirectives: String =
        "Alle Direktiven sind gültig. Wir sind guter Dinge."

    override fun logInfoWelcomeMessage(version: String): String =
        "Willkommen bei arara ${version}!"
}
