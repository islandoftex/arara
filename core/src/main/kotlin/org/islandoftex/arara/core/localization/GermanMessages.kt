// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/**
 * German localization of the arara messages.
 * Translated by Marco Daniel, Ben Frank and Gert Fischer.
 * The documentation of each member is inherited from the
 * [AraraMessages] interface, the single source of truth for
 * what every message says.
 */
@Suppress("MaxLineLength", "TooManyFunctions")
object GermanMessages : AraraMessages {
    override val providedLocale = MPPLocale("de")

    override fun errorBasenameNotAFile(path: String) =
        "Die Methode des \"Dateinamens\" (\"basename\") benötigt eine Datei und kein Verzeichnis. Es scheint, als sei \"${path}\" keine Datei. Falls du Aufgaben auf ein Verzeichnis bzw. Ordner anwenden willst, kannst du eine Vielzahl von JAVA API-Methoden verwenden."

    override val errorCalculateHashIoException =
        "Aus was für einem Grund auch immer kann ich die Prüfziffer (checksum) nicht berechnen. Ich habe keine Idee, warum es fehlschlägt. Vielleicht wurde die Datei bevor oder während der Prüfziffer-Operation bewegt oder gelöscht. Möglicherweise habe ich nicht die geeigneten Rechte, um die Datei zu lesen."

    override fun errorCheckBooleanNotValidBoolean(value: String) =
        "Es scheint, als sei \"${value}\" kein logischer Ausdruck (boolean value). Das sollte leicht zu beheben sein. Stelle sicher, dass der Eingabestring ein zugelassener logischer Ausdruck ist (\"yes\" oder \"no\", \"true\" oder \"false\", \"1\" oder \"0\" sowie \"on\" oder \"off\")."

    override fun errorCheckRegexIoException(file: String) =
        "Ich konnte den Inhalt der Datei \"${file}\" nicht lesen. Ich erhielt einen IO-Fehler. Ich habe keine Idee für die Fehlerursache. Vielleicht wurde die Datei bevor oder während des Leseprozesses bewegt oder gelöscht. Möglicherweise habe ich nicht die geeigneten Rechte, um die Datei zu lesen."

    override val errorConfigurationGenericError =
        "Ich konnte die Konfigurationsdatei nicht analysieren, etwas schlechtes passierte. Dieser Teil ist ziemlich knifflig, denn er bezieht das zugrundeliegende Datenserialisierungsformat mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override val errorConfigurationLoopsInvalidRange =
        "In der Konfigurationsdatei hat der eingegebene Wert 'loops' zur Festlegung der maximalen Anzahl an Schleifendurchgängen einen ungültigen Bereich. Bitte stelle sicher, dass eine positive Zahl eingetragen wird."

    override val errorConfigurationLuaReturnNotATable =
        "Eine Lua-Konfigurationsdatei muss eine Tabelle zurückgeben."

    override fun errorConfigurationUnknownKeys(keys: String) =
        "Die Konfiguration enthält Schlüssel, die das Format nicht erlaubt: ${keys}."

    override fun errorConfigurationKeyRequiresString(key: String) =
        "Der Schlüssel '${key}' erfordert einen Zeichenkettenwert."

    override fun errorConfigurationKeyRequiresInteger(key: String) =
        "Der Schlüssel '${key}' erfordert einen ganzzahligen Wert."

    override fun errorConfigurationKeyRequiresBoolean(key: String) =
        "Der Schlüssel '${key}' erfordert einen Wahrheitswert."

    override fun errorConfigurationEntryNotAString(key: String) =
        "Jeder Eintrag von '${key}' muss eine Zeichenkette sein."

    override fun errorConfigurationKeyRequiresStringList(key: String) =
        "Der Schlüssel '${key}' erfordert eine Liste von Zeichenketten."

    override fun errorConfigurationKeyRequiresTable(key: String) =
        "Der Schlüssel '${key}' erfordert eine Tabelle."

    override fun errorConfigurationKeyRequiresStringKeys(key: String) =
        "Der Schlüssel '${key}' erfordert Zeichenketten als Schlüssel."

    override fun errorConfigurationKeyRequiresStringValues(key: String) =
        "Der Schlüssel '${key}' erfordert Zeichenketten als Werte."

    override val errorConfigurationFileTypeEntryNotATable =
        "Jeder 'file_types'-Eintrag erfordert eine Tabelle mit einer 'extension' und einem optionalen 'pattern'."

    override val errorConfigurationFileTypeExtensionMissing =
        "Jeder 'file_types'-Eintrag erfordert eine 'extension'."

    override val errorConfigurationFileTypeKeyRequiresTableList =
        "Der Schlüssel 'file_types' erfordert eine Liste von Tabellen."

    override val errorConfigurationFileTypePatternNotAString =
        "Der Schlüssel 'pattern' eines 'file_types'-Eintrags erfordert einen Zeichenkettenwert."

    override fun errorConfigurationPreamblePrependInvalid(value: String) =
        "Der Schlüssel 'prepend' der Gruppe 'preamble' akzeptiert 'always' oder 'without-directives', nicht '${value}'."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String) =
        "Ich konnte die vorausgesetzte Datei \"${file}\" mit keiner der Erweiterungen ${extensions} finden. Bitte stelle sicher, dass die Datei existiert und eine gültige Dateiendung hat."

    override fun errorDiscoverFileUnknownExtension(
        file: String,
        extensions: String
    ) =
        "Ich konnte den Dateityp von '${file}' nicht bestimmen: Kein Dateityp dieses Durchlaufs trägt seine Erweiterung. Die Erweiterungen dieses Durchlaufs sind ${extensions}. Ergänze die fehlende Erweiterung in den 'file_types' der Konfiguration."

    override fun errorDiscoverFileUnknownLuaExtension(
        file: String,
        extensions: String
    ) =
        "Ich konnte den Dateityp von '${file}' nicht bestimmen: Kein Dateityp dieses Durchlaufs trägt seine Erweiterung. Die Erweiterungen dieses Durchlaufs sind ${extensions}. Ergänze die Erweiterung in den 'file_types' der Konfiguration - oder, falls die Datei eine Projektspezifikation ist, trägt sie das Suffix '.proj.lua'."

    override fun errorDiscoverFileIsAProjectSpecification(file: String) =
        "Das Suffix '.proj.lua' benennt eine Projektspezifikation, daher kann '${file}' keine gewöhnliche Eingabedatei sein. Übergebe die Spezifikation als einziges Argument des Durchlaufs."

    override val errorEvaluateCompilationFailed =
        "Aus was für einen Grund auch immer kann ich den Ausdruck in der bereitgestellten Bedingung nicht kompilieren. Dieser Teil ist ziemlich knifflig, denn er bezieht die zugrundeliegende Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override val errorEvaluateNotBooleanValue =
        "Die Auswertung der Bedingung (conditional) hat einen boolschen Ausdruck als Ergebnis erwartet. Das sollte leicht zu beheben sein. Stelle einfach sicher, dass die Bedingung einen booleschen Ausdruck ('yes' oder 'no', 'true' oder 'false', '1' oder '0' sowie 'on' oder 'off') als Ergebnis erhält."

    override val errorExtractorIoError =
        "Es gab einen IO-Fehler während ich versuchte, die Direktive zu extrahieren. Ich habe keine Idee, warum es fehlschlug. Vielleicht wurde die Datei bevor oder während der Extraktions-Operation bewegt oder gelöscht. Möglicherweise habe ich nicht die geeigneten Rechte, um die Datei zu lesen."

    override val errorExtractorInterpolationFailure =
        "Das 'Options'-Feld enthält einen Eintrag, mit dem der MVEL-Parser nicht umgehen kann. Bitte überprüfen Sie alle '@{…}'-Teile im 'Options'-Feld."

    override val errorExtractorOrbTagInLuaEngine =
        "Das 'Options'-Feld enthält einen Orb-Tag der veralteten YAML-Engine, den die Lua-Engine nicht interpolieren kann. Bitte schreiben Sie den Eintrag stattdessen mit '{{ … }}'-Tags um."

    override fun errorFiletypeInvalidPattern(pattern: String) =
        "Es sieht so aus, als sei das Muster \"${pattern}\", das du für diesen Dateityp festlegen wolltest, kein gültiger regulärer Ausdruck. Das sollte leicht zu beheben sein: Stelle sicher, dass du einen gültigen Ausdruck verwendest, und versuche es erneut."

    override fun errorFiletypeNotAFile(path: String) =
        "Die Methode des Dateityps \"filetype\" fordert als Eingabe eine Datei und kein Verzeichnis. Es scheint, als sei \"${path}\" keine Datei. Falls du Aufgaben auf ein Verzeichnis bzw. Ordner anwenden willst, kannst du eine Vielzahl von JAVA API-Methoden verwenden."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String) =
        "Ich kann \"${extension}\" als keine vorgegebene Dateiendung erkennen. Falls du einen neuen Dateityp definieren möchtest, stelle sicher, dass die Dateiendung sowie die entsprechende Struktur bereitgestellt wird. Das sind die vordefinierten Dateiendungen: ${extensions}"

    override val errorGetApplicationPathEncodingException =
        "Es gab ein Kodierungsproblem während ich versuchte den Anwendungspfad zu erhalten. Es gibt leider nicht viel, was ich tun kann."

    override fun errorInterpreterArgumentIsRequired(argument: String) =
        "Es scheint, als sei \"${argument}\" eine geforderte Eingabe in der Regel, aber ich kann sie in den Eingabeparametern nicht finden. Bitte stelle sicher, dass der Parameter in deiner Direktive ergänzt wird und versuche es erneut."

    override val errorInterpreterCommandRuntimeError =
        "Ich konnte eines der genutzten Kommandos nicht auswerten. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String) =
        "Das Kommando \"${command}\" wurde unerwarteterweise mit einem Nicht-Null-Rückgabewert beendet. Bitte analysieren Sie den Output des Kommandos und ziehen Sie daraus Schlüsse auf mögliche Lösungen des Problems."

    override val errorInterpreterExitRuntimeError =
        "Ich kann den Status der Fertigmeldung (exit status) von einer der genutzten Anweisungen nicht auswerten. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override val errorInterpreterFlagRuntimeException =
        "Ich kann den Statusindikator (flag expression) eines Argumentes nicht auswerten. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override fun errorInterpreterHaltStatusNotANumber(key: String) =
        "Es scheint, als sei der Wert, den der Sitzungsschlüssel \"${key}\" für das Anhalten enthält, keine gültige Zahl, daher kann ich den angeforderten Haltestatus nicht melden. Das sollte leicht zu beheben sein: Stelle sicher, dass der Haltestatus eine Zahl ist, und versuche es erneut."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String) =
        "Ich konnte keine Regel mit dem Namen \"${identifier}\" in den hinterlegten Regelverzeichnissen finden. Vielleicht ein falsch geschriebenes Wort? Ich habe nach dem Dateinamen \"${expected}\" in den nachstehenden Verzeichnissen mit entsprechender Priorität gesucht: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String) =
        "Ich habe folgende unbekannte Keys in der Direktive gefunden: ${keys}. Das sollte leicht zu beheben sein, entferne sie einfach."

    override fun errorConsoleModeUnknown(mode: String) =
        "Ich kann die Konsole nicht auf '${mode}' setzen, da es keine der verfügbaren Darstellungen ist. Das sollte leicht zu beheben sein, wähle einfach einen der verfügbaren Modi."

    override val errorInterpreterUserRequestedHalt =
        "Die Datei wird auf Anforderung einer Regel nun angehalten, und ihre restlichen Anweisungen werden übersprungen."

    override val errorInterpreterWrongExitClosureReturn =
        "Der Ausdruck 'exit' muss immer einen logischen Wert (boolean value) zurückgeben (sogar wenn es keine Berechnung in der Prozedur gibt). Das sollte leicht zu beheben sein: Stelle einen korrekten Rückgabewert sicher und versuche es erneut."

    override val errorInterpreterWrongReturnType =
        "Eine Regel darf nur entweder einen logischen Wert (boolean value) oder ein Kommando zurückgeben. Andere Rückgaben werden nicht unterstützt."

    override val errorInvalidProjectFile =
        "Ich habe eine Datei gelesen, die ein Projekt sein sollte. Leider stellte sich heraus, dass sie weder ein Projekt noch eine Liste von Projekten ist. Bitte überprüfe, dass deine Projektdatei dem dokumentierten Format entspricht."

    override fun errorLoadCouldNotLoadDatabase(name: String) =
        "Ich konnte die Datei-Datenbank mit dem Namen \"${name}\" nicht laden. Ich habe keine Idee für die Fehlerursache. Vielleicht wurde die Datei vor oder während der Leseoperation verschoben oder gelöscht, oder möglicherweise habe ich nicht die geeigneten Rechte, um die Datei zu lesen. Übrigens: Falls die Datei Inhalte einer älteren Version von mir enthält, löschen Sie sie einfach oder benennen Sie sie um, und ich baue beim nächsten Lauf eine frische Datenbank auf."

    override fun errorParserInvalidPreamble(name: String) =
        "Leider konnte die Präambel \"${name}\" nicht gefunden werden. Bitte stelle sicher, dass dieser Schlüssel in der Konfigurationsdatei existiert."

    override val errorParseRuleGenericError =
        "Ich konnte die Regel nicht auswerten, etwas schlechtes passierte. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Serialisierung mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override fun errorParseRuleArgumentIdNotAString() =
        "Der Argumentbezeichner erfordert einen Zeichenkettenwert."

    override fun errorParseRuleArgumentNeedsFlagOrDefault(identifier: String) =
        "Das Argument '${identifier}' erfordert mindestens eines von 'flag' oder 'default'."

    override fun errorParseRuleArgumentRequiredNotABoolean(identifier: String) =
        "Der Schlüssel 'required' des Arguments '${identifier}' erfordert einen Wahrheitswert."

    override val errorParseRuleCommandFunctionMissing =
        "Jedes Kommando erfordert eine 'command'-Funktion."

    override val errorParseRuleCommandNameNotAString =
        "Der Kommandoname erfordert einen Zeichenkettenwert."

    override fun errorParseRuleEntryNotAString(key: String) =
        "Jeder Eintrag von '${key}' muss eine Zeichenkette sein."

    override fun errorParseRuleEntryNotATable(key: String) =
        "Jeder Eintrag von '${key}' muss eine Tabelle sein."

    override val errorParseRuleIdentifierMissing =
        "Die Regel enthält keinen Bezeichner."

    override val errorParseRuleIdentifierNotAString =
        "Der Regelbezeichner erfordert einen Zeichenkettenwert."

    override fun errorParseRuleKeyRequiresStringList(key: String) =
        "Der Schlüssel '${key}' erfordert eine Liste von Zeichenketten."

    override fun errorParseRuleKeyRequiresTableList(key: String) =
        "Der Schlüssel '${key}' erfordert eine Liste von Tabellen."

    override val errorParseRuleNameNotAString =
        "Der Regelname erfordert einen Zeichenkettenwert."

    override fun errorParseRuleUnsupportedFormat(rule: String, location: String, format: String) =
        "Ich habe einen Fehler in der Regel \"${rule}\" gefunden, die sich in \"${location}\" befindet. Das Regelformat von \"${format}\" wird von arara nicht unterstützt."

    override fun errorProjectCycle(cycle: String) =
        "Die Projektabhängigkeiten enthalten einen Zyklus, an dem ${cycle} beteiligt ist. Zyklen lassen sich nicht auflösen. Bitte überarbeite deine Projektspezifikation."

    override fun errorProjectStrayDependency(dependency: String) =
        "Die Projektspezifikation nennt das Projekt \"${dependency}\" als Abhängigkeit, aber kein Projekt des Durchlaufs trägt diesen Namen. Bitte überarbeite deine Projektspezifikation und versuche es erneut."

    override val errorProjectDependenciesKeyRequiresStringList =
        "Der Schlüssel 'dependencies' erfordert eine Liste von Zeichenketten."

    override val errorProjectDependencyEntryNotAString =
        "Jeder Abhängigkeitseintrag muss eine Zeichenkette sein."

    override val errorProjectDirectiveEntryNotAString =
        "Jeder Direktiveneintrag muss eine Zeichenkette sein."

    override val errorProjectDirectivesKeyRequiresTable =
        "Der Schlüssel 'directives' erfordert eine Tabelle."

    override fun errorProjectDuplicateName(name: String) =
        "Zwei Projekte der Spezifikation teilen sich den Namen '${name}'. Innerhalb eines Durchlaufs wird ein Projekt über seinen Namen identifiziert, daher braucht jedes Projekt einer Liste einen eigenen Namen."

    override fun errorProjectDuplicatePath(name: String) =
        "Das Projekt führt die Datei '${name}' mehr als einmal auf, möglicherweise unter zwei Schreibweisen desselben Ortes. Innerhalb eines Projekts wird eine Datei genau einmal aufgeführt."

    override fun infoDisplayProject(name: String, directory: String) =
        "Projekt ${name} (${directory})"

    override val infoLabelSingleFiles = "Einzelne Dateien"

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "Beim Extrahieren der für '${file}' angegebenen Direktiven in der Projektdatei ist ein Fehler aufgetreten: ${details}."

    override val errorProjectFileEntryKeyNotAString =
        "Projektdateieinträge müssen über ihren Dateinamen adressiert sein."

    override fun errorProjectFileInMultipleProjects(
        file: String,
        projects: String
    ) =
        "Die Datei '${file}' erscheint in den Projekten ${projects}. Innerhalb eines Durchlaufs gehört eine Datei zu genau einem Projekt. Bitte entferne sie aus allen außer einem."

    override val errorProjectFileIsADirectory =
        "Eine Projektdatei darf kein Verzeichnis sein."

    override val errorProjectFileSpecNotATable =
        "Eine Projektdateispezifikation muss eine Tabelle sein."

    override val errorProjectFilesKeyRequiresTable =
        "Der Schlüssel 'files' erfordert eine Tabelle von Dateispezifikationen."

    override val errorProjectFileTypeExtensionRequired =
        "Eine Erweiterung ist erforderlich, um den Dateityp zu bestimmen."

    override val errorProjectFileTypeKeyRequiresTable =
        "Der Schlüssel 'file_type' erfordert eine Tabelle."

    override val errorProjectFileTypePatternMissing =
        "Es fehlt ein Muster, und die Erweiterung gehört nicht zu den Standarddateitypen."

    override val errorProjectFileTypePatternNotAString =
        "Der Schlüssel 'pattern' des Dateityps erfordert einen Zeichenkettenwert."

    override fun errorProjectFileTypeUnknownExtension(file: String) =
        "Ich konnte den Dateityp von '${file}' nicht bestimmen: Kein Dateityp dieses Durchlaufs passt zu seiner Erweiterung. Deklariere einen 'file_type' für den Eintrag, oder ergänze die Erweiterung in den 'file_types' der Konfiguration."

    override fun errorProjectInvalidLua(details: String) =
        "Die Projektspezifikation ist kein gültiges Lua: ${details}."

    override val errorProjectListEntryNotATable =
        "Ein Eintrag einer Projektliste muss eine Tabelle sein."

    override val errorProjectListEntryNeedsName =
        "Jedes Projekt in einer Liste von Projekten braucht einen ausdrücklichen Namen, da Abhängigkeiten Projekte über ihren Namen identifizieren."

    override val errorProjectNameKeyRequiresString =
        "Der Schlüssel 'name' des Projekts erfordert einen Zeichenkettenwert."

    override val errorProjectNeedsAFile =
        "Ein Projekt muss mindestens eine Datei enthalten."

    override val errorProjectPriorityKeyRequiresNumber =
        "Der Schlüssel 'priority' erfordert eine Zahl."

    override val errorProjectReturnNotATable =
        "Der Rückgabetyp der Lua-Projektspezifikation muss eine Tabelle sein, die ein Projekt oder eine Liste von Projekten enthält."

    override val errorProjectSingleProjectHasDependencies =
        "Eine einzelne Projektspezifikation kann keine Abhängigkeiten deklarieren."

    override fun errorProjectUnknownKey(name: String) =
        "Die Projektspezifikation enthält Schlüssel, die das Format nicht erlaubt: ${name}."

    override val errorProjectWorkingDirectoryKeyRequiresString =
        "Der Schlüssel 'working_directory' erfordert einen Zeichenkettenwert."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "Das Glob-Muster \"${pattern}\" ließ sich nicht in einen regulären Ausdruck umwandeln: ${details}. Bitte stelle sicher, dass das Muster wohlgeformt ist, und versuche es erneut."

    override val errorReplicateListMissingFormatArgumentsException =
        "Ich konnte die Liste auf Grund eines fehlendes Formatargumentes nicht replizieren. Meine Vermutung ist, dass es weniger (oder mehr) Parameter als erwartet gibt. Stelle sicher, dass die korrekte Anzahl an Parametern übergeben wird und versuche es erneut."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String) =
        "Ich habe einen Fehler in der Regel \"${identifier}\" an der Stelle \"${location}\" entdeckt."

    override val errorRunGenericException =
        "Ich konnte das geforderte Systemkommando nicht ausführen, etwas schlechtes passierte. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Sprache mit ein. Ich werde mein Bestes tun, um dir in jeglicher Art und Weise zu helfen."

    override val errorRunInterruptedException =
        "Das aufgerufene Systemkommando ist plötzlich unterbrochen worden. Vielleicht gab es eine externe Unterbrechung, die das Kommando abrupt zur Beendigung zwang."

    override val errorRunInvalidExitValueException =
        "Das aufgerufene Systemkommando hat einen ungültigen Abschluss gemeldet."

    override val errorRunIoException =
        "Der Aufruf des Systemkommandos misslang auf Grund eines IO-Fehlers. Bist du dir sicher, dass das Systemkommando in deinem Pfad existiert? Es sollte eine gute Idee sein, die Systempfade sowie die Verfügbarkeit des Kommandos zu prüfen."

    override val errorRunTimeoutException =
        "Die Ausführung des Systemkommandos hat die Zeitbegrenzung erreicht und wurde abgebrochen. Falls die Zeit etwas zu kurz war, kannst du diese erhöhen."

    override val errorSafeModeClassLoading =
        "Im sicheren Modus dürfen Regeln keine beliebigen Klassen aus benutzerseitigen Archiven laden."

    override val errorSafeModeSystemCommand =
        "Im sicheren Modus dürfen Regeln keine beliebigen Systembefehle innerhalb eines Ausführungsschritts ausführen."

    override fun errorSaveCouldNotSaveDatabase(name: String) =
        "Ich konnte die Datei-Datenbank mit dem Namen \"${name}\" nicht speichern. Ich habe keine Idee für die Fehlerursache. Möglicherweise habe ich nicht die geeigneten Rechte, um die Datenbankdatei auf der Festplatte zu speichern."

    override fun errorSessionObtainUnknownKey(key: String) =
        "Die \"get\" Methode hat den unbekannten Eintrag (key) \"${key}\" in der Session gefunden. Ich konnte nichts erhalten, was ich nicht bereits habe. Bitte gebe einen gültigen Key ein und versuche es erneut."

    override fun errorSessionRemoveUnknownKey(key: String) =
        "Die \"remove\" Methode hat den unbekannten Eintrag (key) \"${key}\" in der Session gefunden. Ich konnte nichts erhalten, was ich nicht bereits habe. Bitte gebe einen gültigen Key ein und versuche es erneut."

    override fun errorValidateEmptyFilesList(lines: String) =
        "Ich habe die Direktive ${lines} gelesen und fand heraus, dass die bereitgestellte Dateienliste leer ist. Das sollte leicht zu beheben sein. Bitte stelle sicher, dass die Liste mindestens ein Element hat und versuche es erneut."

    override fun errorValidateFilesIsNotAList(lines: String) =
        "Ich habe die Direktive ${lines} gelesen und fand heraus, dass der Eintrag \"files\" keine Liste übergeben bekommen hat. Bitte stelle sicher, dass du eine passende Liste verwendest und versuche es erneut."

    override fun errorValidateInvalidDirectiveFormat(lines: String) =
        "Ich entdecke eine ungültige Direktive namens ${lines} in der bereitgestellten Datei. Stelle sicher, dass die Direktive gültig ist und versuche es erneut."

    override val errorValidateNoDirectivesFound =
        "Es sieht so aus, als wurden keine Direktiven in der bereitgestellten Datei gefunden. Bitte stelle sicher, dass wenigstens eine Direktive enthalten ist und versuche es erneut."

    override fun errorValidateOrphanLinebreak(line: Int) =
        "Anscheinend gibt es einen verwaisten Zeilenumbruch in der Direktive in der Zeile ${line}. Ich kann nicht fortfahren. Bitte korrigiere die Direktive und versuche es erneut."

    override fun errorValidateReferenceIsReserved(lines: String) =
        "Ich las die Direktive ${lines} und fand heraus, dass der Key \"reference\" genutzt wurde. Dieser Key ist reserviert, deshalb kannst du ihn nicht nutzen. Aber keine Sorge, das sollte einfach zu beheben sein. Du musst nur einen anderen Namen verwenden."

    override fun errorValidateYamlException(lines: String) =
        "Es gab ein Problem mit dem bereitgestellten YAML-Ausdruck in der Direktive ${lines}. Dieser Teil ist ziemlich knifflig, denn er bezieht Aspekte der zugrundeliegenden Serialisierung mit ein."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String) =
        "Die Argumentenbezeichnung \"${identifier}\" ist reserviert, somit kannst du sie nicht nutzen. Dies sollte einfach zu beheben sein. Du musst nur einen anderen Namen verwenden."

    override val errorValidateBodyDuplicateArgumentIdentifiers =
        "Anscheinend hast du Duplikate von Bezeichnern in deiner Regel. Bitte behebe diesen Fehler und versuche es erneut."

    override val errorValidateBodyMissingKeys =
        "Wenn du ein Argument einer Regel definierst, musst du mindestens 'flag' oder 'default' nutzen. Stelle bitte sicher, dass du mindestens eines nutzt."

    override val errorValidateBodyNullArgumentId =
        "Ich habe herausgefunden, dass eines der Argumente keinen Bezeichner hat. Bitte ergänze einen gültigen Bezeichner im Argument und versuche es erneut."

    override val errorValidateBodyNullCommand =
        "Ich habe eine Nullanweisung in der vorgegebenen Regel gefunden. Bitte stelle sicher, dass ein gültiges Kommando zur Regel hinzugefügt wird."

    override val errorValidateHeaderNullName =
        "Die gewünschte Regel hat keinen Namen. Das sollte leicht zu beheben sein. Ergänze einen gültigen Namen und versuche es erneut."

    override fun errorValidateHeaderWrongIdentifier(expected: String, found: String) =
        "Die Regel hat einen falschen Bezeichner. Ich erwartete \"${expected}\" fand aber \"${found}\". Das solle leicht zu beheben sein: Ersetze einfach den falschen durch einen korrekten Bezeichner."

    override val infoDisplayExceptionMoreDetails =
        "Es sind mehr Details für diese Ausnahme verfügbar."

    override val infoDisplayExecutionTime = "Verstrichene Zeit:"
    override val infoDisplayTimeUnit = "Sekunden"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "GESAMTERFOLG · ${if (files == 1) "1 Datei" else "$files Dateien"}, " +
            "${if (tasks == 1) "1 Aufgabe" else "$tasks Aufgaben"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "GESAMTFEHLER · ${if (files == 1) "1 Datei" else "$files Dateien"}, " +
            "${if (tasks == 1) "1 Aufgabe" else "$tasks Aufgaben"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "PROBELAUF · ${if (tasks == 1) "1 Aufgabe" else "$tasks Aufgaben"}"
    override fun infoDisplaySkippedConditional(name: String) =
        "${name} übersprungen · die Bedingung ergab falsch"

    override val infoDisplayFullOutputInLog =
        "die vollständige Ausgabe steht in der Logdatei"


    override val infoDisplaySummaryTitle = "Zusammenfassung"
    override val infoDisplaySummaryFile = "Datei"
    override val infoDisplaySummaryTasks = "Aufgaben"
    override val infoDisplaySummaryTime = "Zeit"

    override fun infoDisplayUnprocessedFiles(count: Int) =
    if (count == 1) "Nicht verarbeitete Datei: " else "Nicht verarbeitete Dateien: "


    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String) =
        "Verarbeitung \"${name}\" (Größe: ${size}, letzte Modifikation: ${lastModified}), bitte warten."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean) =
        "Trotz dry-run Modus wurde dieser Eintrag wahrscheinlich bereits verarbeitet, da er den boolschen Wert ${value} zurückgegeben hat."

    override fun infoInterpreterDryRunModeSystemCommand(command: String) =
        "Los geht\"s: ${command}"

    override val infoLabelAuthor =
        "Autor:"

    override val infoLabelAuthors =
        "Autoren:"

    override val infoLabelConditional =
        "Bedingung:"

    override val infoLabelNoAuthors =
        "Kein Autor berücksichtigt"

    override val infoLabelOnDetails =
        "Details"

    override val infoLabelOnError =
        "FEHLER"

    override val infoLabelOnFailure =
        "MISSERFOLG"

    override val infoLabelOnSuccess =
        "ERFOLGREICH"
    override val infoLabelOnWarning = "WARNUNG"
    override val infoLabelOnQuestion = "FRAGE"
    override val infoLabelOnInfo = "INFO"
    override val infoLabelDialogPrompt = "W\u00e4hle eine Option"
    override val infoLabelAboutToRun =
        "Auszuführen:"
    override val infoLabelSystemCommand = "Systembefehl:"

    override val errorDialogInputClosed =
        "Es scheint, als sei die Terminaleingabe beendet worden, w\u00e4hrend ein Dialog auf eine Antwort wartete, daher kann ich Ihre Auswahl nicht lesen. Bitte f\u00fchre arara in einem interaktiven Terminal aus, damit der Dialog beantwortet werden kann."
    override val errorDialogsNotConfigured =
        "Es scheint, als sei f\u00fcr diese Ausf\u00fchrung keine Dialogunterst\u00fctzung konfiguriert, daher kann ich den von einer Regel angeforderten Dialog nicht anzeigen. Bitte stelle eine Dialog-Implementierung in der Anwendung bereit, die arara ausf\u00fchrt, oder vermeide Regeln mit interaktiven Dialogen."
    override val infoLabelUnnamedTask =
        "Namenlose Aufgabe"

    override val infoParserNotes =
        "arara ist unter der New BSD license veröffentlicht."
}
