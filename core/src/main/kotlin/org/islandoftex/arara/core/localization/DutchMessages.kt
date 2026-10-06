// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Dutch localization of the arara messages.
 * Translated by Marijn Schraagen and Taco de Marie.
 * The documentation of each member is inherited from the
 * [AraraMessages] interface, the single source of truth for
 * what every message says.
 */
@Suppress("MaxLineLength", "TooManyFunctions")
object DutchMessages : AraraMessages {
    override val providedLocale = MPPLocale("nl")

    override fun errorBasenameNotAFile(path: String) =
        "De 'basename'-methode vereist een bestand, geen directory. Het lijkt erop dat '${path}' geen bestand is. Als je taken wil uitvoeren met een directory kan je een aantal methodes uit de Java API gebruiken."

    override val errorCalculateHashIoException =
        "Om een of andere reden kan ik de hash niet berekenen. Ik weet niet waarom het precies fout ging. Misschien is het bestand verplaatst of verwijderd voor of tijdens de hashing, of misschien heb ik niet de juiste rechten om het bestand te lezen."

    override fun errorCheckBooleanNotValidBoolean(value: String) =
        "Het lijkt erop dat '${value}' geen boolean-waarde is. Dit zou makkelijk op te lossen moeten zijn. Gebruik een geldige representatie voor een boolean (yes en no, true en false, 1 en 0, on en off)."

    override fun errorCheckRegexIoException(file: String) =
        "Ik kon de inhoud van het bestand '${file}' niet lezen vanwege een IO-fout. Ik weet niet waarom het precies fout ging. Misschien is het bestand verplaatst of verwijderd voor of tijdens de leesoperatie, of misschien heb ik niet de juiste rechten om het bestand te lezen."

    override val errorConfigurationGenericError =
        "Ik kon het configuratiebestand niet verwerken, er is iets verkeerd gegaan. Dit probleem is ingewikkeld, omdat het te maken heeft met aspecten van de onderliggende serialisatiesyntax. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override val errorConfigurationLoopsInvalidRange =
        "De waarde gedefinieerd in de 'loops'-optie in het configuratiebestand om het maximumaantal herhalingen te specificeren heeft een ongeldig bereik. Geef alsjeblieft een positieve long integer-waarde op."

    override val errorConfigurationLuaReturnNotATable =
        "Een Lua-configuratiebestand moet een tabel retourneren."

    override fun errorConfigurationUnknownKeys(keys: String) =
        "De configuratie bevat sleutels die het formaat niet toestaat: ${keys}."

    override fun errorConfigurationKeyRequiresString(key: String) =
        "De sleutel '${key}' vereist een tekenreekswaarde."

    override fun errorConfigurationKeyRequiresInteger(key: String) =
        "De sleutel '${key}' vereist een geheel getal."

    override fun errorConfigurationKeyRequiresBoolean(key: String) =
        "De sleutel '${key}' vereist een booleaanse waarde."

    override fun errorConfigurationEntryNotAString(key: String) =
        "Elke '${key}'-waarde moet een tekenreeks zijn."

    override fun errorConfigurationKeyRequiresStringList(key: String) =
        "De sleutel '${key}' vereist een lijst met tekenreeksen."

    override fun errorConfigurationKeyRequiresTable(key: String) =
        "De sleutel '${key}' vereist een tabel."

    override fun errorConfigurationKeyRequiresStringKeys(key: String) =
        "De sleutel '${key}' vereist tekenreeksen als sleutels."

    override fun errorConfigurationKeyRequiresStringValues(key: String) =
        "De sleutel '${key}' vereist tekenreeksen als waarden."

    override val errorConfigurationFileTypeEntryNotATable =
        "Elke 'file_types'-waarde vereist een tabel met een 'extension' en een optioneel 'pattern'."

    override val errorConfigurationFileTypeExtensionMissing =
        "Elke 'file_types'-waarde vereist een 'extension'."

    override val errorConfigurationFileTypeKeyRequiresTableList =
        "De sleutel 'file_types' vereist een lijst met tabellen."

    override val errorConfigurationFileTypePatternNotAString =
        "De sleutel 'pattern' van een 'file_types'-waarde vereist een tekenreekswaarde."

    override fun errorConfigurationPreamblePrependInvalid(value: String) =
        "De sleutel 'prepend' van de groep 'preamble' accepteert 'always' of 'without-directives', niet '${value}'."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String) =
        "Ik kon het opgegeven bestand '${file}' niet vinden met een van de extensies ${extensions}. Zorg ervoor dat het bestand bestaat en dat het een geldige extensie heeft."

    override fun errorDiscoverFileUnknownExtension(
        file: String,
        extensions: String
    ) =
        "Ik kon het bestandstype van '${file}' niet bepalen: geen enkel bestandstype van deze run draagt zijn extensie. De extensies van deze run zijn ${extensions}. Voeg de ontbrekende extensie toe aan de 'file_types' van de configuratie."

    override fun errorDiscoverFileUnknownLuaExtension(
        file: String,
        extensions: String
    ) =
        "Ik kon het bestandstype van '${file}' niet bepalen: geen enkel bestandstype van deze run draagt zijn extensie. De extensies van deze run zijn ${extensions}. Voeg de extensie toe aan de 'file_types' van de configuratie - of, als het bestand een projectspecificatie is, draagt het het achtervoegsel '.proj.lua'."

    override fun errorDiscoverFileIsAProjectSpecification(file: String) =
        "Het achtervoegsel '.proj.lua' benoemt een projectspecificatie, dus '${file}' kan geen gewoon invoerbestand zijn. Geef de specificatie als enige argument van de run mee."

    override val errorEvaluateCompilationFailed =
        "Om een of andere reden kon ik de expressie in de opgegeven voorwaarde niet compileren. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override val errorEvaluateNotBooleanValue =
        "De evaluatie van de voorwaarde verwachtte een boolean als resultaat. Dit zou makkelijk op te lossen moeten zijn. Zorg ervoor dat de evaluatie uiteindelijk een boolean oplevert."

    override val errorExtractorIoError =
        "Er was een IO-probleem bij het verwerken van de directives. Ik weet niet waarom het precies fout ging. Misschien is het bestand verplaatst of verwijderd voor of tijdens het hashen, of misschien heb ik niet de juiste rechten om het bestand te lezen."

    override val errorExtractorInterpolationFailure =
        "Uw 'opties'-veld bevat een item dat de MVEL-parser niet aankan. Herzie alstublieft uw opties, zodat deze geen foutieve '@ {…}' onderdelen meer bevatten."

    override val errorExtractorOrbTagInLuaEngine =
        "Uw 'opties'-veld bevat een orb-tag van de verouderde YAML-engine, die de Lua-engine niet kan interpoleren. Herschrijf alstublieft de invoer met '{{ … }}'-tags."

    override fun errorFiletypeInvalidPattern(pattern: String) =
        "Het lijkt erop dat het patroon \"${pattern}\" dat je voor dit bestandstype wilde gebruiken geen geldige reguliere expressie is. Dit zou makkelijk op te lossen moeten zijn: zorg dat je een geldige expressie gebruikt en probeer het opnieuw."

    override fun errorFiletypeNotAFile(path: String) =
        "De 'filetype'-methode vereist een bestand, geen directory. Het lijkt erop dat '${path}' geen bestand is. Als je taken wil uitvoeren met een directory kan je een aantal methodes uit de Java API gebruiken."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String) =
        "De extensie '${extension}' is niet herkend als een standaardextensie. Als je een nieuw bestandstype wil definiëren zorg dan dat de extensie en het patroon zijn opgegeven. De volgende extensies zijn standaard: ${extensions}"

    override val errorGetApplicationPathEncodingException =
        "Er was een coderingsprobleem bij het verkrijgen van het applicatiepad. Daar kan ik niet zo veel aan doen."

    override fun errorInterpreterArgumentIsRequired(argument: String) =
        "Het argument '${argument}' is gemarkeerd als verplicht in de regel, maar ik kon het niet vinden in de parameters van de directive. Zorg ervoor dat het als parameter voor de directive wordt toegevoegd en probeer het opnieuw."

    override val errorInterpreterCommandRuntimeError =
        "Ik kon een van de opgegeven opdrachten niet evalueren. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String) =
        "Het commando \"${command}\" is onverwacht beëindigd met een afsluitwaarde die niet gelijk is aan nul. Bekijk het resultaat van het commando en analyseer het om uw volgende stappen te bepalen."

    override val errorInterpreterExitRuntimeError =
        "Ik kon de exitwaardemelding van een van de opgegeven opdrachten niet evalueren. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override val errorInterpreterFlagRuntimeException =
        "Ik kon de optie-expressie van een van de argumenten niet evalueren. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override fun errorInterpreterHaltStatusNotANumber(key: String) =
        "Het lijkt erop dat de waarde onder de halteringssleutel \"${key}\" van de sessie geen geldig getal is, dus ik kan de gevraagde haltstatus niet melden. Dit zou makkelijk te verhelpen moeten zijn: zorg dat de haltstatus een getal is en probeer het opnieuw."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String) =
        "Ik kon de regel met de naam '${identifier}' niet vinden in de opgegeven regelpaden. Misschien een spelfout? Ik heb geprobeerd het bestand '${expected}' te vinden in de volgende paden in volgorde van prioriteit: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String) =
        "Ik heb de volgende onbekende verwijzingen gevonden in de directive: ${keys}. Dit zou makkelijk op te lossen moeten zijn: verwijder deze uit de verwijzingstabel."

    override fun errorConsoleModeUnknown(mode: String) =
        "Ik kan de console niet op '${mode}' zetten, omdat dat geen van de beschikbare weergaven is. Dit zou makkelijk op te lossen moeten zijn: kies gewoon een van de beschikbare modi."

    override val errorInterpreterUserRequestedHalt =
        "Het bestand stopt nu op verzoek van een regel, en de resterende richtlijnen worden overgeslagen."

    override val errorInterpreterWrongExitClosureReturn =
        "De 'exit'-expressie moet altijd een boolean waarde teruggeven (zelfs als er geen berekening heeft plaatsgevonden). Dit zou makkelijk op te lossen moeten zijn: pas het type van de return-regel aan en probeer het opnieuw."

    override val errorInterpreterWrongReturnType =
        "Een regel mag alleen een, waar of niet waar (booleaanse waarde), of een commando als uitkomst hebben. Andere enter types worden niet ondersteund."

    override val errorInvalidProjectFile =
        "Ik heb een bestand gelezen dat een project zou moeten zijn. Helaas bleek het geen project en ook geen lijst van projecten te zijn. Controleer of je projectbestand het gedocumenteerde formaat volgt."

    override fun errorLoadCouldNotLoadDatabase(name: String) =
        "Ik kon de bestandsdatabase met de naam '${name}' niet laden. Ik weet niet waarom het precies fout ging. Misschien was het bestand verplaatst of verwijderd voor of tijdens de leesoperatie, of misschien heb ik niet de juiste rechten om het te lezen. Overigens: als het bestand inhoud van een oudere versie van mij bevat, verwijder of hernoem het dan gerust, dan bouw ik bij de volgende run een nieuwe database."

    override fun errorParserInvalidPreamble(name: String) =
        "Het spijt me, maar de preamble '${name}' is niet gevonden. Zorg ervoor dat deze verwijzing bestaat in het configuratiebestand."

    override val errorParseRuleGenericError =
        "Ik kon de regel niet verwerken, er is iets verkeerd gegaan. Dit probleem is ingewikkeld, omdat het te maken heeft met aspecten van de onderliggende dataserialisatiesyntax. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override fun errorParseRuleArgumentIdNotAString() =
        "De argumentidentificatie vereist een tekenreekswaarde."

    override fun errorParseRuleArgumentNeedsFlagOrDefault(identifier: String) =
        "Het argument '${identifier}' vereist minstens een 'flag' of een 'default'."

    override fun errorParseRuleArgumentRequiredNotABoolean(identifier: String) =
        "De sleutel 'required' van argument '${identifier}' vereist een booleaanse waarde."

    override val errorParseRuleCommandFunctionMissing =
        "Elk commando vereist een 'command'-functie."

    override val errorParseRuleCommandNameNotAString =
        "De commandonaam vereist een tekenreekswaarde."

    override fun errorParseRuleEntryNotAString(key: String) =
        "Elke '${key}'-waarde moet een tekenreeks zijn."

    override fun errorParseRuleEntryNotATable(key: String) =
        "Elke '${key}'-waarde moet een tabel zijn."

    override val errorParseRuleIdentifierMissing =
        "De regel bevat geen identificatie."

    override val errorParseRuleIdentifierNotAString =
        "De regelidentificatie vereist een tekenreekswaarde."

    override fun errorParseRuleKeyRequiresStringList(key: String) =
        "De sleutel '${key}' vereist een lijst met tekenreeksen."

    override fun errorParseRuleKeyRequiresTableList(key: String) =
        "De sleutel '${key}' vereist een lijst met tabellen."

    override val errorParseRuleNameNotAString =
        "De regelnaam vereist een tekenreekswaarde."

    override fun errorParseRuleUnsupportedFormat(rule: String, location: String, format: String) =
        "Ik heb een fout gevonden in regel \"${rule}\", gelegen in \"${location}\". Het regelformaat van \"${format}\" wordt niet ondersteund door arara."

    override fun errorProjectCycle(cycle: String) =
        "De projectafhankelijkheden bevatten een cyclus waarin ${cycle} voorkomt. Cycles kunnen niet worden opgelost. Controleer je projectspecificatie."

    override fun errorProjectStrayDependency(dependency: String) =
        "De projectspecificatie noemt het project \"${dependency}\" als afhankelijkheid, maar geen enkel project van de run draagt die naam. Herstel je projectspecificatie en probeer het opnieuw."

    override val errorProjectDependenciesKeyRequiresStringList =
        "De sleutel 'dependencies' vereist een lijst met tekenreeksen."

    override val errorProjectDependencyEntryNotAString =
        "Elke afhankelijkheidswaarde moet een tekenreeks zijn."

    override val errorProjectDirectiveEntryNotAString =
        "Elke richtlijnwaarde moet een tekenreeks zijn."

    override val errorProjectDirectivesKeyRequiresTable =
        "De sleutel 'directives' vereist een tabel."

    override fun errorProjectDuplicateName(name: String) =
        "Twee projecten in de specificatie delen de naam '${name}'. Binnen één run wordt een project geïdentificeerd aan de hand van zijn naam, dus elk project in een lijst heeft een eigen naam nodig."

    override fun errorProjectDuplicatePath(name: String) =
        "Het project noemt het bestand '${name}' meer dan eens, misschien onder twee schrijfwijzen van dezelfde locatie. Binnen een project wordt een bestand precies één keer genoemd."

    override fun infoDisplayProject(name: String, directory: String) =
        "Project: ${name} (${directory})"

    override val infoLabelSingleFiles = "Losse bestanden"

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "Er is een fout opgetreden bij het extraheren van de richtlijnen die voor '${file}' in het projectbestand zijn opgegeven: ${details}."

    override val errorProjectFileEntryKeyNotAString =
        "Projectbestandwaarden moeten via hun bestandsnaam aangeduid zijn."

    override fun errorProjectFileInMultipleProjects(
        file: String,
        projects: String
    ) =
        "Het bestand '${file}' komt voor in de projecten ${projects}. Binnen één run hoort een bestand bij exact één project. Verwijder het uit alle op één na."

    override val errorProjectFileIsADirectory =
        "Een projectbestand mag geen map zijn."

    override val errorProjectFileSpecNotATable =
        "Een projectbestandspecificatie moet een tabel zijn."

    override val errorProjectFilesKeyRequiresTable =
        "De sleutel 'files' vereist een tabel met bestandsspecificaties."

    override val errorProjectFileTypeExtensionRequired =
        "Een extensie is vereist om het bestandstype te bepalen."

    override val errorProjectFileTypeKeyRequiresTable =
        "De sleutel 'file_type' vereist een tabel."

    override val errorProjectFileTypePatternMissing =
        "Er ontbreekt een patroon en de extensie hoort niet bij de standaardbestandstypen."

    override val errorProjectFileTypePatternNotAString =
        "De sleutel 'pattern' van het bestandstype vereist een tekenreekswaarde."

    override fun errorProjectFileTypeUnknownExtension(file: String) =
        "Ik kon het bestandstype van '${file}' niet bepalen: geen enkel bestandstype van deze run past bij zijn extensie. Declareer een 'file_type' voor de waarde, of voeg de extensie toe aan de 'file_types' van de configuratie."

    override fun errorProjectInvalidLua(details: String) =
        "De projectspecificatie is geen geldige Lua: ${details}."

    override val errorProjectListEntryNotATable =
        "Elke waarde in een projectenlijst moet een tabel zijn."

    override val errorProjectListEntryNeedsName =
        "Elk project in een lijst met projecten vereist een expliciete naam, omdat afhankelijkheden projecten bij naam identificeren."

    override val errorProjectNameKeyRequiresString =
        "De sleutel 'name' van het project vereist een tekenreekswaarde."

    override val errorProjectNeedsAFile =
        "Een project moet minstens één bestand bevatten."

    override val errorProjectPriorityKeyRequiresNumber =
        "De sleutel 'priority' vereist een getal."

    override val errorProjectReturnNotATable =
        "Het retourtype van de Lua-projectspecificatie moet een tabel zijn met daarin een project of een lijst met projecten."

    override val errorProjectSingleProjectHasDependencies =
        "Een enkele projectspecificatie kan geen afhankelijkheden bevatten."

    override fun errorProjectUnknownKey(name: String) =
        "De projectspecificatie bevat sleutels die het formaat niet toestaat: ${name}."

    override val errorProjectWorkingDirectoryKeyRequiresString =
        "De sleutel 'working_directory' vereist een tekenreekswaarde."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "Het globpatroon \"${pattern}\" kon niet worden omgezet in een reguliere expressie: ${details}. Zorg dat het patroon goed gevormd is en probeer het opnieuw."

    override val errorReplicateListMissingFormatArgumentsException =
        "Ik kon de lijst niet genereren vanwege een ontbrekend format-argument. Ik denk dat er minder (of meer) parameters zijn dan verwacht. Geef het juiste aantal argumenten op en probeer het opnieuw."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String) =
        "Ik heb een fout gevonden in regel '${identifier}' op positie '${location}'."

    override val errorRunGenericException =
        "Ik kon het opgegeven systeemcommando niet uitvoeren, er is iets misgegaan. Dit is een ingewikkeld probleem, omdat het te maken heeft met aspecten van de onderliggende expressietaal. Ik zal mijn best doen om je zoveel mogelijk hiermee te helpen."

    override val errorRunInterruptedException =
        "Het uitvoeren van het opgegeven systeemcommando is onverwacht onderbroken. Misschien was er een externe onderbreking die ervoor gezorgd heeft dat het commando gestopt is."

    override val errorRunInvalidExitValueException =
        "Het opgegeven systeemcommando heeft een ongeldige exit-waarde teruggegeven."

    override val errorRunIoException =
        "Het uitvoeren van het systeemcommando is mislukt vanwege een IO-fout. Weet je zeker dat het commando bestaat in het pad? Het is misschien een goed idee om het pad te controleren en te kijken of het commando beschikbaar is."

    override val errorRunTimeoutException =
        "Het uitvoeren van het systeemcommando is onderbroken omdat de maximale uitvoeringsduur overschreden is. Als de tijd te kort was, pas dan de maximale uitvoeringsduur aan."

    override val errorSafeModeClassLoading =
        "In de veilige modus mogen regels geen willekeurige klassen uit door de gebruiker geleverde archieven laden."

    override val errorSafeModeSystemCommand =
        "In de veilige modus mogen regels geen willekeurige systeemcommando's uitvoeren binnen een uitvoeringsstap."

    override fun errorSaveCouldNotSaveDatabase(name: String) =
        "Ik kon de bestandsdatabase met de naam '${name}' niet opslaan. Ik weet niet precies waarom het fout ging. Misschien heb ik niet de juiste schrijfrechten om het bestand op te slaan."

    override fun errorSessionObtainUnknownKey(key: String) =
        "De 'get'-methode heeft een onbekende verwijzing gevonden in de sessie: '${key}'. Als ik iets niet heb kan ik het ook niet gebruiken. Geef een bestaande verwijzing op en probeer het opnieuw."

    override fun errorSessionRemoveUnknownKey(key: String) =
        "De 'remove'-methode heeft een onbekende verwijzing gevonden in de sessie: '${key}'. Als ik iets niet heb kan ik het ook niet verwijderen. Geef een bestaande verwijzing op en probeer het opnieuw."

    override fun errorValidateEmptyFilesList(lines: String) =
        "Ik heb een directive ${lines} gelezen en kwam erachter dat de opgegeven 'files'-lijst leeg is. Dit zou makkelijk op te lossen moeten zijn: zorg ervoor dat de lijst tenminste een element heeft en probeer het opnieuw."

    override fun errorValidateFilesIsNotAList(lines: String) =
        "Ik heb een directive ${lines} gelezen en kwam erachter dat 'files' een lijst nodig heeft. Zorg dat je het type aanpast naar een lijst en probeer het opnieuw."

    override fun errorValidateInvalidDirectiveFormat(lines: String) =
        "Ik heb een ongeldige directive ${lines} gevonden in het opgegeven bestand. Pas de directive aan en probeer het opnieuw."

    override val errorValidateNoDirectivesFound =
        "Er zijn geen directives gevonden in het opgegeven bestand. Zorg ervoor dat er tenminste een directive in het bestand staat en probeer het opnieuw."

    override fun errorValidateOrphanLinebreak(line: Int) =
        "Er is een ongeldig directive-regeleinde in regel ${line}. Ik kan niet verder. Pas de directive aan en probeer het opnieuw."

    override fun errorValidateReferenceIsReserved(lines: String) =
        "Ik heb een directive ${lines} gelezen en kwam erachter dat de verwijzing 'reference' gebruikt is. Deze verwijzing is gereserveerd, en kan dus niet gebruikt worden. Maar maak je geen zorgen, dit zou makkelijk op te lossen moeten zijn: gebruik een andere naam."

    override fun errorValidateYamlException(lines: String) =
        "Er is een probleem met de opgegeven YAML-omzetting in directive ${lines}. Dit probleem is ingewikkeld, omdat het te maken heeft met aspecten van de onderliggende serialisatiesyntax."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String) =
        "De argumentverwijzing '${identifier}' is gereserveerd, en kan dus niet gebruikt worden. Dit zou makkelijk op te lossen moeten zijn: gebruik een andere naam."

    override val errorValidateBodyDuplicateArgumentIdentifiers =
        "Blijkbaar zijn er dubbele argumentnamen in de regel. Pas dit aan en probeer het opnieuw."

    override val errorValidateBodyMissingKeys =
        "Bij het definiëren van het argumentbereik van een regel moet tenminste 'flag' of 'default' worden gebruikt. Zorg ervoor dat je in ieder geval een van deze gebruikt."

    override val errorValidateBodyNullArgumentId =
        "Ik kwam erachter dat een van de argumenten geen verwijsnaam heeft. Zorg ervoor dat een geldige naam aan het argument wordt toegewezen en probeer het opnieuw."

    override val errorValidateBodyNullCommand =
        "Ik heb een null-opdracht in de opgegeven regel gevonden. Dit zou makkelijk op te lossen moeten zijn: geef een geldige opdracht op voor deze regel."

    override val errorValidateHeaderNullName =
        "De opgegeven regel heeft geen naam. Dit zou makkelijk op te lossen moeten zijn: geef een geldige naam op en probeer het opnieuw."

    override fun errorValidateHeaderWrongIdentifier(expected: String, found: String) =
        "De regel heeft een verkeerde verwijsnaam. Ik verwachtte '${expected}', maar vond '${found}'. Dit zou makkelijk op te lossen moeten zijn: vervang de verkeerde verwijsnaam met de correcte naam."

    override val infoDisplayExceptionMoreDetails =
        "Er zijn meer details beschikbaar voor deze exception:"

    override val infoDisplayExecutionTime = "Verstreken tijd:"
    override val infoDisplayTimeUnit = "seconden"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "TOTAAL SUCCES · ${if (files == 1) "1 bestand" else "$files bestanden"}, " +
            "${if (tasks == 1) "1 taak" else "$tasks taken"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "TOTAAL MISLUKT · ${if (files == 1) "1 bestand" else "$files bestanden"}, " +
            "${if (tasks == 1) "1 taak" else "$tasks taken"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "SIMULATIE · ${if (tasks == 1) "1 taak" else "$tasks taken"}"
    override fun infoDisplaySkippedConditional(name: String) =
        "${name} overgeslagen · de voorwaarde evalueerde naar onwaar"

    override val infoDisplayFullOutputInLog =
        "de volledige uitvoer staat in het logbestand"


    override val infoDisplaySummaryTitle = "Overzicht"
    override val infoDisplaySummaryFile = "Bestand"
    override val infoDisplaySummaryTasks = "Taken"
    override val infoDisplaySummaryTime = "Tijd"

    override fun infoDisplayUnprocessedFiles(count: Int) =
    if (count == 1) "Niet verwerkt bestand: " else "Niet verwerkte bestanden: "


    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String) =
        "Verwerken van '${name}' (grootte: ${size}, laatst gewijzigd: ${lastModified}), een ogenblik geduld."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean) =
        "Ondanks het uitvoeren in dry-runmodus is dit item mogelijk al verwerkt omdat het de booleaanse waarde terug heeft gegeven:  ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String) =
        "Begin met uitvoeren: ${command}"

    override val infoLabelAuthor =
        "Auteur:"

    override val infoLabelAuthors =
        "Auteurs:"

    override val infoLabelConditional =
        "Voorwaarde:"

    override val infoLabelNoAuthors =
        "Geen auteurs opgegeven"

    override val infoLabelOnDetails =
        "Details"

    override val infoLabelOnError =
        "FOUT"

    override val infoLabelOnFailure =
        "MISLUKT"

    override val infoLabelOnSuccess =
        "SUCCESVOL"
    override val infoLabelOnWarning = "WAARSCHUWING"
    override val infoLabelOnQuestion = "VRAAG"
    override val infoLabelOnInfo = "INFO"
    override val infoLabelDialogPrompt = "Kies een optie"
    override val infoLabelAboutToRun =
        "Uit te voeren:"
    override val infoLabelSystemCommand = "Systeemopdracht:"

    override val errorDialogInputClosed =
        "Het lijkt erop dat de terminalinvoer is be\u00ebindigd terwijl een dialoog op een antwoord wachtte, dus ik kan je keuze niet lezen. Voer arara uit in een interactieve terminal, zodat de dialoog beantwoord kan worden."
    override val errorDialogsNotConfigured =
        "Het lijkt erop dat er voor deze uitvoering geen dialoogondersteuning is geconfigureerd, dus ik kan de dialoog die een regel vroeg niet tonen. Geef een dialoogimplementatie op in de toepassing die arara uitvoert, of vermijd regels met interactieve dialogen."
    override val infoLabelUnnamedTask =
        "Naamloze taak"

    override val infoParserNotes =
        "arara is uitgebracht onder de New BSD licentie."
}
