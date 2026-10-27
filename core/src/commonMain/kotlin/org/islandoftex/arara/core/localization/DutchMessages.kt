// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/** Dutch message catalog. */
@Suppress("MaxLineLength", "TooManyFunctions")
object DutchMessages : AraraMessages {

    override fun infoBuildDirectoryValidating(path: String): String =
        "Bouwmap controleren: $path"

    override fun infoBuildDirectoryPreparing(path: String): String =
        "Bouwmap voorbereiden: $path"

    override fun infoBuildDirectoryCleaning(path: String): String =
        "Bouwmap opruimen: $path"

    override fun infoBuildDirectoryPrepared(files: Int, seconds: String): String =
        "Bouwmap gereed: $files invoerbestanden gekopieerd in $seconds s."

    override val infoBuildDirectoryValidated: String =
        "Bouwmap gecontroleerd."

    override val infoBuildDirectoryCleaned: String =
        "Bouwmap opgeruimd."

    override val errorBuildDirectoryOperationFailed: String =
        "Bewerking van de bouwmap mislukt."


    override fun errorScriptChoice(field: String, choices: String): String =
        "Niet-ondersteunde waarde voor $field; verwacht: $choices."

    override fun errorBuildDirectoryInvalid(path: String): String =
        "Ongeldige of overlappende bouwmap: $path."

    override fun errorBuildInputInvalid(path: String): String =
        "Invoer of bestemming valt buiten de toewijzing van de bouwmap: $path."

    override fun errorBuildWorkspaceUnmanaged(path: String): String =
        "De bouwmap wordt niet door dit project beheerd: $path."

    override fun errorBuildInputCollision(path: String): String =
        "Bouwinvoer conflicteert met andere invoer of bestaande uitvoer: $path."

    override fun errorBuildLinkCycle(path: String): String =
        "Cyclus van symbolische koppelingen in de bouwinvoer: $path."

    override fun errorBuildInputMissing(path: String): String =
        "Bouwinvoer ontbreekt of is geen gewoon bestand of map: $path."

    override val warningDeprecatedYaml: String =
        "Compatibiliteitswaarschuwing: u hebt verouderde YAML-regels gebruikt die in v9 worden verwijderd."

    override val providedLocale: MPPLocale = MPPLocale("nl")

    override val translators: List<String> = listOf("Marijn Schraagen", "Taco de Marie")

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "Er is een fout opgetreden bij het extraheren van de richtlijnen van '${file}': ${details}."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "Het globpatroon \"${pattern}\" kon niet worden omgezet in een reguliere expressie: ${details}. Zorg dat het patroon goed gevormd is en probeer het opnieuw."

    override val errorSafeModeClassLoading =
        "In de veilige modus mogen regels geen willekeurige klassen uit door de gebruiker geleverde archieven laden."

    override val errorSafeModeSystemCommand =
        "In de veilige modus mogen regels geen willekeurige systeemcommando's uitvoeren binnen een uitvoeringsstap."

    override val errorDialogInputClosed: String =
        "Het lijkt erop dat de terminalinvoer is be\u00ebindigd terwijl een dialoog op een antwoord wachtte, dus ik kan je keuze niet lezen. Voer arara uit in een interactieve terminal, zodat de dialoog beantwoord kan worden."

    override val errorDialogsNotConfigured: String =
        "Het lijkt erop dat er voor deze uitvoering geen dialoogondersteuning is geconfigureerd, dus ik kan de dialoog die een regel vroeg niet tonen. Geef een dialoogimplementatie op in de toepassing die arara uitvoert, of vermijd regels met interactieve dialogen."

    override fun errorScriptString(field: String): String =
        "${field} moet een tekenreeks zijn."

    override fun errorScriptTable(field: String): String =
        "${field} moet een tabel zijn."

    override fun errorScriptBoolean(field: String): String =
        "${field} moet een booleaanse waarde zijn."

    override fun errorScriptInteger(field: String): String =
        "${field} moet een geheel getal zijn."

    override fun errorScriptList(field: String): String =
        "${field} moet een lijst met opeenvolgende indices vanaf één zijn."

    override fun errorScriptUnknownKey(field: String): String =
        "Onbekende sleutel ${field}."

    override fun errorScriptRequired(field: String): String =
        "${field} is verplicht."

    override fun errorScriptNonblank(field: String): String =
        "${field} mag niet leeg zijn."

    override fun errorScriptDuplicate(field: String, value: String): String =
        "${field} herhaalt '${value}'."

    override fun errorScriptDirectory(field: String): String =
        "${field} is een map."

    override fun errorScriptEmpty(field: String): String =
        "${field} moet minstens één item bevatten."

    override fun errorScriptStringKeys(field: String): String =
        "${field} vereist niet-lege tekenreeksen als sleutels."

    override fun errorProjectInvalidLua(path: String, detail: String): String =
        "Ongeldige Lua in ${path}: ${detail}"

    override fun errorProjectDuplicateNames(names: String): String =
        "Projectnamen moeten uniek zijn: ${names}."

    override fun errorProjectUnknownDependency(project: String, dependency: String): String =
        "Project '${project}' heeft een onbekende afhankelijkheid '${dependency}'."

    override fun errorProjectCycle(cycle: String): String =
        "De projectafhankelijkheden bevatten een cyclus: ${cycle}."

    override fun errorProjectFileOwnership(path: String, owner: String, project: String): String =
        "Bestand '${path}' behoort tot zowel '${owner}' als '${project}'."

    override fun errorProjectFiletype(path: String): String =
        "Geen ingesteld bestandstype past bij '${path}'."

    override fun errorConfigurationPrepend(value: String): String =
        "Ongeldige preamble.prepend '${value}'; gebruik 'always' of 'without-directives'."

    override fun errorScriptFunction(field: String): String =
        "${field} moet een functie zijn."

    override fun errorScriptLiteral(field: String): String =
        "${field} moet een letterlijke waarde zijn."

    override fun errorScriptAlternatives(field: String, alternatives: String): String =
        "${field} vereist minstens één van ${alternatives}."

    override val errorLuaInterpolationFailure: String =
        "Het veld 'options' bevat een ongeldige Lua-expressie of interpolatietag."

    override val errorYamlTagsInLua: String =
        "MVEL-tags vereisen de YAML-engine; Lua gebruikt {{ expression }}."

    override val errorLuaTagsInYaml: String =
        "Lua-tags vereisen een Lua-regel."

    override val errorLuaUnterminatedTag: String =
        "Niet-afgesloten interpolatietag."

    override val errorLuaUnterminatedQuote: String =
        "Niet-afgesloten tekenreeks tussen aanhalingstekens in interpolatietag."

    override val errorLuaUnterminatedLongString: String =
        "Niet-afgesloten lange tekenreeks in interpolatietag."

    override val errorLuaMapKeys: String =
        "Mapsleutels vallen samen als Lua-tekenreeksen."

    override val errorLuaTableKeys: String =
        "Lua-tabelsleutels vallen samen als tekenreeksen."

    override fun errorLuaExactInteger(value: String): String =
        "Geheel getal ${value} kan niet exact als Lua-getal worden weergegeven."

    override val errorLuaCyclicValues: String =
        "Cyclische tabellen en verzamelingen kunnen niet worden geconverteerd."

    override fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String =
        "ongeldig argument nr. ${index} voor '${helper}' (${expected} verwacht, ${actual} gekregen)"

    override fun errorLuaBooleanResult(helper: String, actual: String): String =
        "ongeldig resultaat van '${helper}' (boolean verwacht, ${actual} gekregen)"

    override fun errorLuaHelperNoArgs(helper: String): String =
        "${helper} vereist minstens één argument."

    override fun errorLoadCouldNotLoadDatabase(name: String): String =
        "Database '${name}' kon niet worden geladen. Controleer bestandstoegang en de inhoud van de YAML-cache."

    override fun errorSaveCouldNotSaveDatabase(name: String): String =
        "Database '${name}' kon niet worden opgeslagen. Controleer of de bovenliggende map bestaat en schrijfbaar is."

    override fun errorBasenameNotAFile(path: String): String =
        "De 'basename'-methode vereist een bestand, geen directory. Het lijkt erop dat '${path}' geen bestand is. Als je taken wil uitvoeren met een directory kan je een aantal methodes uit de Java API gebruiken."

    override val errorCalculateHashIoException: String =
        "Om een of andere reden kan ik de hash niet berekenen. Ik weet niet waarom het precies fout ging. Misschien is het bestand verplaatst of verwijderd voor of tijdens de hashing, of misschien heb ik niet de juiste rechten om het bestand te lezen."

    override fun errorCheckBooleanNotValidBoolean(value: String): String =
        "Het lijkt erop dat '${value}' geen boolean-waarde is. Dit zou makkelijk op te lossen moeten zijn. Gebruik een geldige representatie voor een boolean (yes en no, true en false, 1 en 0, on en off)."

    override fun errorCheckRegexIoException(file: String): String =
        "Ik kon de inhoud van het bestand '${file}' niet lezen vanwege een IO-fout. Ik weet niet waarom het precies fout ging. Misschien is het bestand verplaatst of verwijderd voor of tijdens de leesoperatie, of misschien heb ik niet de juiste rechten om het bestand te lezen."

    override val errorConfigurationGenericError: String =
        "Ik kon het configuratiebestand niet verwerken, er is iets verkeerd gegaan. Dit probleem is ingewikkeld, omdat het te maken heeft met aspecten van de onderliggende serialisatiesyntax. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override val errorConfigurationLoopsInvalidRange: String =
        "De waarde gedefinieerd in de 'loops'-optie in het configuratiebestand om het maximumaantal herhalingen te specificeren heeft een ongeldig bereik. Geef alsjeblieft een positieve long integer-waarde op."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String): String =
        "Ik kon het opgegeven bestand '${file}' ${extensions} niet vinden. Zorg ervoor dat het bestand bestaat en dat het een geldige extensie heeft."

    override fun errorDiscoverFileSafeModeNoCompletion(file: String) =
        "Ik kon het opgegeven bestand '${file}' niet vinden. De veilige modus vult bestandsverwijzingen niet aan vanuit hun extensies, dus geef de volledige bestandsnaam op, extensie inbegrepen."

    override fun errorDiscoverFileUnknownExtension(
        file: String,
        extensions: String
    ) =
        "Ik kon het bestandstype van '${file}' niet bepalen: geen enkel bestandstype van deze run draagt zijn extensie. De extensies van deze run zijn ${extensions}. Voeg de ontbrekende extensie toe aan de 'file_types' (Lua) / 'filetypes' (YAML) van de configuratie."

    override fun errorDiscoverFileUnknownLuaExtension(
        file: String,
        extensions: String
    ) =
        "Ik kon het bestandstype van '${file}' niet bepalen: geen enkel bestandstype van deze run draagt zijn extensie. De extensies van deze run zijn ${extensions}. Voeg de extensie toe aan de 'file_types' (Lua) / 'filetypes' (YAML) van de configuratie - of, als het bestand een projectspecificatie is, draagt het het achtervoegsel '.proj.lua'."

    override val errorEvaluateCompilationFailed: String =
        "Om een of andere reden kon ik de expressie in de opgegeven voorwaarde niet compileren. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override val errorEvaluateNotBooleanValue: String =
        "De evaluatie van de voorwaarde verwachtte een boolean als resultaat. Dit zou makkelijk op te lossen moeten zijn. Zorg ervoor dat de evaluatie uiteindelijk een boolean oplevert."

    override val errorExtractorIoError: String =
        "Er was een IO-probleem bij het verwerken van de directives. Ik weet niet waarom het precies fout ging. Misschien is het bestand verplaatst of verwijderd voor of tijdens het hashen, of misschien heb ik niet de juiste rechten om het bestand te lezen."

    override val errorExtractorInterpolationFailure: String =
        "Uw 'opties'-veld bevat een item dat de MVEL-parser niet aankan. Herzie alstublieft uw opties, zodat deze geen foutieve '@ {…}' onderdelen meer bevatten."

    override fun errorFiletypeInvalidPattern(pattern: String): String =
        "Het lijkt erop dat het patroon \"${pattern}\" dat je voor dit bestandstype wilde gebruiken geen geldige reguliere expressie is. Dit zou makkelijk op te lossen moeten zijn: zorg dat je een geldige expressie gebruikt en probeer het opnieuw."

    override fun errorFiletypeNotAFile(path: String): String =
        "De 'filetype'-methode vereist een bestand, geen directory. Het lijkt erop dat '${path}' geen bestand is. Als je taken wil uitvoeren met een directory kan je een aantal methodes uit de Java API gebruiken."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String): String =
        "De extensie '${extension}' is niet herkend als een standaardextensie. Als je een nieuw bestandstype wil definiëren zorg dan dat de extensie en het patroon zijn opgegeven. De volgende extensies zijn standaard: ${extensions}"

    override val errorGetApplicationPathEncodingException: String =
        "Er was een coderingsprobleem bij het verkrijgen van het applicatiepad. Daar kan ik niet zo veel aan doen."

    override fun errorInterpreterArgumentIsRequired(argument: String): String =
        "Het argument '${argument}' is gemarkeerd als verplicht in de regel, maar ik kon het niet vinden in de parameters van de directive. Zorg ervoor dat het als parameter voor de directive wordt toegevoegd en probeer het opnieuw."

    override val errorInterpreterCommandRuntimeError: String =
        "Ik kon een van de opgegeven opdrachten niet evalueren. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String): String =
        "Het commando \"${command}\" is onverwacht beëindigd met een afsluitwaarde die niet gelijk is aan nul. Bekijk het resultaat van het commando en anaylseer het om uw volgende stappen te bepalen."

    override val errorInterpreterUserRequestedHalt: String =
        "arara word nu beëindigd op uw (of een regel) verzoek."

    override val errorInterpreterExitRuntimeError: String =
        "Ik kon de exitwaardemelding van een van de opgegeven opdrachten niet evalueren. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override val errorInterpreterFlagRuntimeException: String =
        "Ik kon de optie-expressie van een van de argumenten niet evalueren. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override fun errorLogFormatUnknown(value: String): String =
        "Onbekend logformaat '${value}'. Gebruik none, text of json."

    override fun errorLogLevelUnknown(value: String): String =
        "Onbekend logniveau '${value}'. Gebruik trace, debug, info, warn of error."

    override fun errorLogRetentionUnknown(value: String): String =
        "Onbekend logbewaarbeleid '${value}'. Gebruik always of on-failure."

    override fun errorLogCleanupFailed(reason: String): String =
        "Loguitvoer van de geslaagde uitvoering kon niet worden verwijderd: ${reason}"

    override fun errorTerminalWidthInvalid(minimum: Int): String =
        "De terminalbreedte moet minstens ${minimum} kolommen zijn."

    override fun errorConsoleModeUnknown(mode: String): String =
        "Onbekende consolemodus '${mode}'. Gebruik plain of styled."

    override fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String =
        "Niet-ondersteund regelformaat voor '${identifier}': '${format}'. Gebruik een Lua- of YAML-regelbestand."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String): String =
        "Ik kon de regel met de naam '${identifier}' niet vinden in de opgegeven regelpaden. Misschien een spelfout? Ik heb geprobeerd het bestand '${expected}' te vinden in de volgende paden in volgorde van prioriteit: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String): String =
        "Ik heb de volgende onbekende verwijzingen gevonden in de directive: ${keys}. Dit zou makkelijk op te lossen moeten zijn: verwijder deze uit de verwijzingstabel."

    override val errorInterpreterWrongExitClosureReturn: String =
        "De 'exit'-expressie moet altijd een boolean waarde teruggeven (zelfs als er geen berekening heeft plaatsgevonden). Dit zou makkelijk op te lossen moeten zijn: pas het type van de return-regel aan en probeer het opnieuw."

    override val errorInterpreterWrongReturnType: String =
        "Een regel mag alleen een, waar of niet waar (booleaanse waarde), of een commando als uitkomst hebben. Andere enter types worden niet ondersteund."

    override val errorInvalidProjectFile: String =
        "I have read a file that was supposed to be a project. Unfortunately, it turned out it was neither a project nor a list of projects. Please double-check that your project file follows the documented format."

    override fun errorLoadCouldNotLoadXml(name: String): String =
        "Ik kon de YAML-database met de naam '${name}' niet laden. Ik weet niet waarom het precies fout ging. Misschien was het bestand verplaatst of verwijderd tijdens de leesoperatie. Of misschien heb ik niet de juiste rechten om het bestand te lezen. Overigens: zorg ervoor dat het YAML-bestand welgevormd is."

    override fun errorParserInvalidPreamble(name: String): String =
        "Het spijt me, maar de preamble '${name}' is niet gevonden. Zorg ervoor dat deze verwijzing bestaat in het configuratiebestand."

    override val errorParseRuleGenericError: String =
        "Ik kon de regel niet verwerken, er is iets verkeerd gegaan. Dit probleem is ingewikkeld, omdat het te maken heeft met aspecten van de onderliggende dataserialisatiesyntax. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override val errorReplicateListMissingFormatArgumentsException: String =
        "Ik kon de lijst niet genereren vanwege een ontbrekend format-argument. Ik denk dat er minder (of meer) parameters zijn dan verwacht. Geef het juiste aantal argumenten op en probeer het opnieuw."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String): String =
        "Ik heb een fout gevonden in regel '${identifier}' op positie '${location}'."

    override val errorRunGenericException: String =
        "Ik kon het opgegeven systeemcommando niet uitvoeren, er is iets misgegaan. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override val errorRunInterruptedException: String =
        "Het uitvoeren van het opgegeven systeemcommando is onverwacht onderbroken. Misschien was er een externe onderbreking die ervoor gezorgd heeft dat het commando gestopt is."

    override val errorRunInvalidExitValueException: String =
        "Het opgegeven systeemcommando heeft een ongeldige exit-waarde teruggegeven."

    override val errorRunIoException: String =
        "Het uitvoeren van het systeemcommando is mislukt vanwege een IO-fout. Weet je zeker dat het commando bestaat in het pad? Het is misschien een goed idee om het pad te controleren en te kijken of het commando beschikbaar is."

    override val errorRunTimeoutException: String =
        "Het uitvoeren van het systeemcommando is onderbroken omdat de maximale uitvoeringsduur overschreden is. Als de tijd te kort was, pas dan de maximale uitvoeringsduur aan."

    override fun errorSaveCouldNotSaveXml(name: String): String =
        "Ik kon de YAML-database met de naam '${name}' niet opslaan. Ik weet niet precies waarom het fout ging. Misschien heb ik niet de juiste schrijfrechten om het YAML-bestand op te slaan."

    override fun errorSessionObtainUnknownKey(key: String): String =
        "De 'get'-methode heeft een onbekende verwijzing gevonden in de sessie: '${key}'. Als ik iets niet heb kan ik het ook niet gebruiken. Geef een bestaande verwijzing op en probeer het opnieuw."

    override fun errorSessionRemoveUnknownKey(key: String): String =
        "De 'remove'-methode heeft een onbekende verwijzing gevonden in de sessie: '${key}'. Als ik iets niet heb kan ik het ook niet verwijderen. Geef een bestaande verwijzing op en probeer het opnieuw."

    override fun errorValidateEmptyFilesList(lines: String): String =
        "Ik heb een directive ${lines} gelezen en kwam erachter dat de opgegeven 'files'-lijst leeg is. Dit zou makkelijk op te lossen moeten zijn: zorg ervoor dat de lijst tenminste een element heeft en probeer het opnieuw."

    override fun errorValidateFilesIsNotAList(lines: String): String =
        "Ik heb een directive ${lines} gelezen en kwam erachter dat 'files' een lijst nodig heeft. Zorg dat je het type aanpast naar een lijst en probeer het opnieuw."

    override fun errorValidateInvalidDirectiveFormat(lines: String): String =
        "Ik heb een ongeldige directive ${lines} gevonden in het opgegeven bestand. Pas de directive aan en probeer het opnieuw."

    override val errorValidateNoDirectivesFound: String =
        "Er zijn geen directives gevonden in het opgegeven bestand. Zorg ervoor dat er tenminste een directive in het bestand staat en probeer het opnieuw."

    override fun errorValidateOrphanLinebreak(line: String): String =
        "Er is een ongeldig directive-regeleinde in regel ${line}. Ik kan niet verder. Pas de directive aan en probeer het opnieuw."

    override fun errorValidateReferenceIsReserved(lines: String): String =
        "Ik heb een directive ${lines} gelezen en kwam erachter dat de verwijzing 'reference' gebruikt is. Deze verwijzing is gereserveerd, en kan dus niet gebruikt worden. Maar maak je geen zorgen, dit zou makkelijk op te lossen moeten zijn: gebruik een andere naam."

    override fun errorValidateYamlException(lines: String): String =
        "Er is een probleem met de opgegeven YAML-omzetting in directive ${lines}. Dit probleem is ingewikkeld, omdat het te maken heeft met aspecten van de onderliggende serialisatiesyntax."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String): String =
        "De argumentverwijzing '${identifier}' is gereserveerd, en kan dus niet gebruikt worden. Dit zou makkelijk op te lossen moeten zijn: gebruik een andere naam."

    override val errorValidateBodyDuplicateArgumentIdentifiers: String =
        "Blijkbaar zijn er dubbele argumentnamen in de regel. Pas dit aan en probeer het opnieuw."

    override val errorValidateBodyMissingKeys: String =
        "Bij het definiëren van het argumentbereik van een regel moet tenminste 'flag' of 'default' worden gebruikt. Zorg ervoor dat je in ieder geval een van deze gebruikt."

    override val errorValidateBodyNullArgumentId: String =
        "Ik kwam erachter dat een van de argumenten geen verwijsnaam heeft. Zorg ervoor dat een geldige naam aan het argument wordt toegewezen en probeer het opnieuw."

    override val errorValidateBodyNullCommand: String =
        "Ik heb een null-opdracht in de opgegeven regel gevonden. Dit zou makkelijk op te lossen moeten zijn: geef een geldige opdracht op voor deze regel."

    override val errorValidateHeaderNullName: String =
        "De opgegeven regel heeft geen naam. Dit zou makkelijk op te lossen moeten zijn: geef een geldige naam op en probeer het opnieuw."

    override fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String =
        "De regel heeft een verkeerde verwijsnaam. Ik verwachtte '${expected}', maar vond '${actual}'. Dit zou makkelijk op te lossen moeten zijn: vervang de verkeerde verwijsnaam met de correcte naam."

    override val infoLabelProject = "Project:"

    override val infoDisplaySummaryTitle = "Overzicht"

    override val infoDisplaySummaryFile = "Bestand"

    override val infoDisplaySummaryTasks = "Taken"

    override val infoDisplaySummaryTime = "Tijd"

    override val infoDisplayFullOutputInLog =
        "de volledige uitvoer staat in het logbestand"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "TOTAAL SUCCES · ${if (files == 1) "1 bestand" else "$files bestanden"}, " +
            "${if (tasks == 1) "1 taak" else "$tasks taken"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "TOTAAL MISLUKT · ${if (files == 1) "1 bestand" else "$files bestanden"}, " +
            "${if (tasks == 1) "1 taak" else "$tasks taken"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "SIMULATIE · ${if (tasks == 1) "1 taak" else "$tasks taken"}"

    override val infoConsoleSkipped: String =
        "Overgeslagen"

    override val infoConsoleUnprocessed: String =
        "Niet verwerkt"

    override val infoDisplayExceptionMoreDetails: String =
        "Er zijn meer details beschikbaar voor deze exception:"

    override fun infoDisplayExecutionTime(time: String): String =
        "Totaal: ${time} seconden"

    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String =
        "Verwerken van '${name}' (grootte: ${size}, laatst gewijzigd: ${lastModified}), een ogenblik geduld."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String =
        "Ondanks het uitvoeren in dry-runmodus is dit item mogelijk al verwerkt omdat het de booleaanse waarde terug heeft gegeven:  ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String): String =
        "Begin met uitvoeren: ${command}"

    override val infoLabelAuthor: String =
        "Auteur:"

    override val infoLabelAuthors: String =
        "Auteurs:"

    override val infoLabelSystemCommand = "Systeemopdracht:"

    override val infoLabelConditional: String =
        "Voorwaarde:"

    override val infoLabelNoAuthors: String =
        "Geen auteurs opgegeven"

    override val infoDisplayDiagnosticsTitle = "Gereedschapsdiagnostiek"

    override val infoDisplayDiagnosticLevel = "Niveau"

    override val infoDisplayDiagnosticTool = "Programma"

    override val infoDisplayDiagnosticMessage = "Melding"

    override val infoDisplayDiagnosticCount = "Aantal"

    override val infoLabelOnWarning: String =
        "WAARSCHUWING"

    override val infoLabelOnQuestion: String =
        "VRAAG"

    override val infoLabelOnInfo: String =
        "INFO"

    override val infoLabelDialogPrompt: String =
        "Kies een optie"

    override val infoLabelOnDetails: String =
        "DETAILS"

    override val infoLabelOnError: String =
        "FOUT"

    override val infoLabelOnFailure: String =
        "MISLUKT"

    override val infoLabelOnSuccess: String =
        "SUCCESVOL"

    override val infoLabelUnnamedTask: String =
        "Naamloze taak"

    override val infoParserNotes: String =
        "arara is uitgebracht onder de New BSD licentie."

    override val logInfoBeginBuffer: String =
        "OPEN UITVOER"

    override fun logInfoBooleanMode(value: Boolean): String =
        "Booleaanse waarde: ${value}"

    override val logInfoDirectivesBlock: String =
        "DIRECTIVES"

    override val logInfoEndBuffer: String =
        "SLUIT UITVOER"

    override fun logInfoInterpretRule(rule: String): String =
        "Ik ben klaar om regel '${rule}' te verwerken."

    override fun logInfoInterpretTask(task: String, rule: String): String =
        "Ik ben klaar om taak '${task}' uit regel '${rule}' te verwerken."

    override fun logInfoPotentialPatternFound(line: String, pattern: String): String =
        "Ik heb een mogelijk patroon gevonden in regel ${line}: ${pattern}"

    override fun logInfoRuleLocation(path: String): String =
        "Regellocatie: '${path}'"

    override fun logInfoSystemCommand(command: String): String =
        "Systeemopdracht: ${command}"

    override val logInfoTaskResult: String =
        "Taakresultaat:"

    override val logInfoValidatedDirectives: String =
        "Alle directives zijn gevalideerd, we kunnen beginnen."

    override fun logInfoWelcomeMessage(version: String): String =
        "Welkom bij arara ${version}!"
}
