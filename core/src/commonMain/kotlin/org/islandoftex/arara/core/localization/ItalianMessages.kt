// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/** Italian message catalog. */
@Suppress("MaxLineLength", "TooManyFunctions")
object ItalianMessages : AraraMessages {
    override val warningDeprecatedYaml: String =
        "Avviso di compatibilità: sono state utilizzate regole YAML deprecate che saranno rimosse nella v9."

    override val providedLocale: MPPLocale = MPPLocale("it")

    override val translators: List<String> = listOf("Enrico Gregorio", "Carla Maggi", "Paulinho van Duck")

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "Si è verificato un errore durante l'estrazione delle direttive di '${file}': ${details}."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "Il pattern glob \"${pattern}\" non ha potuto essere convertito in un'espressione regolare: ${details}. Assicurati che il pattern sia ben formato e riprova."

    override val errorSafeModeClassLoading =
        "In modalità sicura, alle regole non è consentito caricare classi arbitrarie da archivi forniti dall'utente."

    override val errorSafeModeSystemCommand =
        "In modalità sicura, alle regole non è consentito eseguire comandi di sistema arbitrari all'interno di un passo di esecuzione."

    override val errorDialogInputClosed: String =
        "Sembra che l\u2019input del terminale si sia chiuso mentre un dialogo attendeva una risposta, quindi non posso leggere la tua scelta. Esegui arara in un terminale interattivo, cos\u00ec il dialogo pu\u00f2 ricevere una risposta."

    override val errorDialogsNotConfigured: String =
        "Sembra che questa esecuzione non abbia alcun supporto per i dialoghi configurato, quindi non posso mostrare il dialogo richiesto da una regola. Fornisci un\u2019implementazione dei dialoghi nell\u2019applicazione che esegue arara, oppure evita regole con dialoghi interattivi."

    override fun errorScriptString(field: String): String =
        "${field} deve essere una stringa."

    override fun errorScriptTable(field: String): String =
        "${field} deve essere una tabella."

    override fun errorScriptBoolean(field: String): String =
        "${field} deve essere un valore booleano."

    override fun errorScriptInteger(field: String): String =
        "${field} deve essere un numero intero."

    override fun errorScriptList(field: String): String =
        "${field} deve essere una lista con indici consecutivi a partire da uno."

    override fun errorScriptUnknownKey(field: String): String =
        "Chiave sconosciuta ${field}."

    override fun errorScriptRequired(field: String): String =
        "${field} è obbligatorio."

    override fun errorScriptNonblank(field: String): String =
        "${field} non può essere vuoto."

    override fun errorScriptDuplicate(field: String, value: String): String =
        "${field} ripete '${value}'."

    override fun errorScriptDirectory(field: String): String =
        "${field} è una cartella."

    override fun errorScriptEmpty(field: String): String =
        "${field} deve contenere almeno una voce."

    override fun errorScriptStringKeys(field: String): String =
        "${field} richiede chiavi di testo non vuote."

    override fun errorProjectInvalidLua(path: String, detail: String): String =
        "Lua non valido in ${path}: ${detail}"

    override fun errorProjectDuplicateNames(names: String): String =
        "I nomi dei progetti devono essere univoci: ${names}."

    override fun errorProjectUnknownDependency(project: String, dependency: String): String =
        "Il progetto '${project}' ha una dipendenza sconosciuta '${dependency}'."

    override fun errorProjectCycle(cycle: String): String =
        "Le dipendenze dei progetti contengono un ciclo: ${cycle}."

    override fun errorProjectFileOwnership(path: String, owner: String, project: String): String =
        "Il file '${path}' appartiene sia a '${owner}' che a '${project}'."

    override fun errorProjectFiletype(path: String): String =
        "Nessun tipo di file configurato corrisponde a '${path}'."

    override fun errorConfigurationPrepend(value: String): String =
        "preamble.prepend '${value}' non valido; usare 'always' o 'without-directives'."

    override fun errorScriptFunction(field: String): String =
        "${field} deve essere una funzione."

    override fun errorScriptLiteral(field: String): String =
        "${field} deve essere un valore letterale."

    override fun errorScriptAlternatives(field: String, alternatives: String): String =
        "${field} richiede almeno uno tra ${alternatives}."

    override val errorLuaInterpolationFailure: String =
        "Il campo 'options' contiene un'espressione Lua o un tag di interpolazione non valido."

    override val errorYamlTagsInLua: String =
        "I tag MVEL richiedono il motore YAML; Lua usa {{ expression }}."

    override val errorLuaTagsInYaml: String =
        "I tag Lua richiedono una regola Lua."

    override val errorLuaUnterminatedTag: String =
        "Tag di interpolazione non terminato."

    override val errorLuaUnterminatedQuote: String =
        "Stringa tra virgolette non terminata nel tag di interpolazione."

    override val errorLuaUnterminatedLongString: String =
        "Stringa lunga non terminata nel tag di interpolazione."

    override val errorLuaMapKeys: String =
        "Le chiavi della mappa coincidono come stringhe Lua."

    override val errorLuaTableKeys: String =
        "Le chiavi della tabella Lua coincidono come stringhe."

    override fun errorLuaExactInteger(value: String): String =
        "Il numero intero ${value} non può essere rappresentato esattamente come numero Lua."

    override val errorLuaCyclicValues: String =
        "Le tabelle e le collezioni cicliche non possono essere convertite."

    override fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String =
        "argomento n. ${index} non valido per '${helper}' (atteso ${expected}, ricevuto ${actual})"

    override fun errorLuaBooleanResult(helper: String, actual: String): String =
        "risultato non valido da '${helper}' (atteso boolean, ricevuto ${actual})"

    override fun errorLuaHelperNoArgs(helper: String): String =
        "${helper} richiede almeno un argomento."

    override fun errorLoadCouldNotLoadDatabase(name: String): String =
        "Impossibile caricare il database '${name}'. Verificare l'accesso al file e l'integrità della cache YAML."

    override fun errorSaveCouldNotSaveDatabase(name: String): String =
        "Impossibile salvare il database '${name}'. Verificare che la cartella esista e sia scrivibile."

    override fun errorBasenameNotAFile(path: String): String =
        "Il metodo 'basename' richiede un file, non una directory. '${path}' non sembra proprio essere un file. Se devi eseguire un compito su una directory, puoi adoperare uno dei metodi delle API Java."

    override val errorCalculateHashIoException: String =
        "Per qualche motivo, non ho potuto calcolare un hash. Purtroppo non ho idea del perché sia andata male. Forse il file è stato spostato o cancellato durante l'operazione di hashing. O forse non ho i permessi necessari per leggere il file."

    override fun errorCheckBooleanNotValidBoolean(value: String): String =
        "Sembra che '${value}' non sia un valore booleano valido. Penso che sia facile correggerlo. Assicurati di adoperare una stringa valida che rappresenti un valore booleano (yes e no, true e false, 1 e 0, oppure on e off)."

    override fun errorCheckRegexIoException(file: String): String =
        "Non sono riuscito a leggere il contenuto del file '${file}' e ho ricevuto in errore di I/O. Purtroppo non ho idea del perché sia andata male. Forse il file è stato spostato o cancellato durante l'operazione di hashing. O forse non ho i permessi necessari per leggere il file."

    override val errorConfigurationGenericError: String =
        "Non sono riuscito a leggere il file di configurazione, è successo qualcosa che non va. Questa parte è un po' complicata, perché riguarda aspetti del formato interno di serializzazione dei dati. Farò del mio meglio per darti una mano, per quanto posso."

    override val errorConfigurationLoopsInvalidRange: String =
        "Il valore definito nella chiave 'loops' del file di configurazione per stabilire il massimo numero di cicli ha un intervallo non valido. Assicurati che sia un valore 'lungo' e positivo."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String): String =
        "Non sono riuscito a trovare il file '${file}' ${extensions}. Assicurati che il file esista e abbia un'estensione valida."

    override fun errorDiscoverFileSafeModeNoCompletion(file: String) =
        "Non sono riuscito a trovare il file '${file}'. La modalità sicura non completa i riferimenti ai file dalle loro estensioni, quindi fornisci il nome completo del file, estensione inclusa."

    override fun errorDiscoverFileUnknownExtension(
        file: String,
        extensions: String
    ) =
        "Non sono riuscito a determinare il tipo di file di '${file}': nessun tipo di file di questa esecuzione porta la sua estensione. Le estensioni di questa esecuzione sono ${extensions}. Aggiungi l'estensione mancante ai 'file_types' (Lua) / 'filetypes' (YAML) della configurazione."

    override fun errorDiscoverFileUnknownLuaExtension(
        file: String,
        extensions: String
    ) =
        "Non sono riuscito a determinare il tipo di file di '${file}': nessun tipo di file di questa esecuzione porta la sua estensione. Le estensioni di questa esecuzione sono ${extensions}. Aggiungi l'estensione ai 'file_types' (Lua) / 'filetypes' (YAML) della configurazione - oppure, se il file è una specifica di progetto, il suo suffisso è '.proj.lua'."

    override val errorEvaluateCompilationFailed: String =
        "Per qualche ragione, non sono riuscito a compilare l'espressione del condizionale fornito. Questa parte è un po' complicata, perché riguarda aspetti del linguaggio interno per le espressioni. Farò del mio meglio per darti una mano, per quanto posso."

    override val errorEvaluateNotBooleanValue: String =
        "La valutazione del condizionale si aspettava un valore booleano come risultato. Dovrebbe essere facile correggerlo. Assicurati che la valutazione del condizionale fornisca alla fine un valore booleano."

    override val errorExtractorIoError: String =
        "C'è stato un errore di I/O mentre provavo a estrarre le direttive. Purtroppo non ho idea del perché sia andata male. Forse il file è stato spostato o cancellato prima o durante l'operazione di hashing. O forse non ho i permessi necessari per leggere il file."

    override val errorExtractorInterpolationFailure: String =
        "Il campo 'options' ha un contenuto che il parser MVEL non riesce a gestire. Correggi le opzioni in modo che non contengano parti di forma scorretta con '@{...}'."

    override fun errorFiletypeInvalidPattern(pattern: String): String =
        "Sembra che il pattern \"${pattern}\" che volevi usare per questo tipo di file non sia un'espressione regolare valida. Dovrebbe essere facile da sistemare: assicurati di usare un'espressione valida e riprova."

    override fun errorFiletypeNotAFile(path: String): String =
        "Il metodo 'filetype' richiede un file, non una directory. '${path}' non sembra proprio essere un file. Se devi eseguire un compito su una directory, puoi adoperare uno dei metodi delle API Java."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String): String =
        "Non riconosco '${extension}' come un'estensione standard. Se vuoi definire un nuovo tipo di file, assicurati di fornire l'estensione e lo schema. Queste sono le estensioni standard: ${extensions}"

    override val errorGetApplicationPathEncodingException: String =
        "C'è stato un problema di codifica mentre tentavo di ottenere il percorso dell'applicazione. Non c'è molto che possa fare al riguardo."

    override fun errorInterpreterArgumentIsRequired(argument: String): String =
        "Sembra che '${argument}' sia contrassegnato come obbligatorio nella regola, ma non l'ho trovato nei parametri della direttiva. Assicurati di aggiungerlo come parametro nella direttiva e riprova."

    override val errorInterpreterCommandRuntimeError: String =
        "Non ho potuto valutare uno dei comandi forniti. Questa parte è un po' complicata perché coinvolge aspetti del linguaggio per le espressioni sottostante. Farò del mio meglio per darti una mano, per quanto posso."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String): String =
        "Il comando \"${command}\" ha dato un esito inaspettato, con codice di ritorno non a zero. Guarda l'output per valutare le eventuali azioni da intraprendere."

    override val errorInterpreterUserRequestedHalt: String =
        "A seguito di una tua richiesta (o di una regola), arara ora interromperà l'esecuzione."

    override val errorInterpreterExitRuntimeError: String =
        "Non sono riuscito a valutare lo stato di uscita di uno dei comandi forniti. Questa parte è un po' complicata perché coinvolge aspetti del linguaggio per le espressioni sottostante. Farò del mio meglio per darti una mano, per quanto posso."

    override val errorInterpreterFlagRuntimeException: String =
        "Non ho potuto valutare la flag di uno degli argomenti. Questa parte è un po' complicata perché coinvolge aspetti del linguaggio per le espressioni sottostante. Farò del mio meglio per darti una mano, per quanto posso."

    override fun errorLogFormatUnknown(value: String): String =
        "Formato di log sconosciuto '${value}'. Usare none, text o json."

    override fun errorConsoleModeUnknown(mode: String): String =
        "Modalità console sconosciuta '${mode}'. Usare plain o styled."

    override fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String =
        "Formato di regola non supportato per '${identifier}': '${format}'. Usare un file di regola Lua o YAML."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String): String =
        "Non sono riuscito a trovare una regola chiamata '${identifier}' nei percorsi per le regole impostati. Forse una parola scritta sbagliata? Stavo cercando un file con il nome '${expected}' nei seguenti percorsi in ordine di priorità: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String): String =
        "Ho trovato queste chiavi sconosciute nella direttiva: ${keys}. Dovrebbbe essere facile correggere, toglile dalla mappa."

    override val errorInterpreterWrongExitClosureReturn: String =
        "L'espressione di 'uscita' deve essere sempre un valore booleano (anche se non c'è alcun calcolo nella parte di chiusura). Dovrebbe essere facile correggere: assicurati che l'asserzione del 'type return' sia giusta e riprova."

    override val errorInterpreterWrongReturnType: String =
        "Una regola dovrebbe restituire solo un booleano o un comando. Altri tipi di risposta non sono supportati."

    override val errorInvalidProjectFile: String =
        "I have read a file that was supposed to be a project. Unfortunately, it turned out it was neither a project nor a list of projects. Please double-check that your project file follows the documented format."

    override fun errorLoadCouldNotLoadXml(name: String): String =
        "Non ho potuto caricare il database YAML di nome '${name}'. Purtroppo non ho idea del perché sia andata male. Forse il file è stato spostato o cancellato prima o durante l'operazione di lettura. O forse non ho i permessi necessari per leggere il file. Già che ci siamo, assicurati che il file YAML sia ben formato."

    override fun errorParserInvalidPreamble(name: String): String =
        "Mi dispiace, ma non è stato possibile trovare il preambolo '${name}'. Assicurati che questa chiave esista nel file di configurazione."

    override val errorParseRuleGenericError: String =
        "Non ho potuto analizzare la regola, qualcosa è andato storto. Questa parte è un po' complicata, perché riguarda aspetti del formato interno di serializzazione dei dati. Farò del mio meglio per darti una mano, per quanto posso."

    override val errorReplicateListMissingFormatArgumentsException: String =
        "Non sono riuscito a replicare la lista perché manca un argomento per il formato. Penso che si tratti di un numero sbagliato di parametri. Assicurati che il numero di parametri sia corretto e riprova."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String): String =
        "Ho trovato un errore nella regola '${identifier}' alla posizione '${location}'."

    override val errorRunGenericException: String =
        "Non ho potuto lanciare il comando di sistema richiesto, qualcosa è andato storto. Questa parte è un po' complicata, perché riguarda aspetti del linguaggio interno per le espressioni. Farò del mio meglio per darti una mano, per quanto posso."

    override val errorRunInterruptedException: String =
        "L'esecuzione del comando di sistema richiesto si è interrotta inaspettatamente. Forse un'interruzione esterna ha forzato il comando a terminare."

    override val errorRunInvalidExitValueException: String =
        "Il comando di sistema richiesto ha restituito un valore di uscita non valido."

    override val errorRunIoException: String =
        "L'esecuzione del comando di sistema è fallita per via di un errore di I/O. Sei sicuro che il programma esista nei tuoi percorsi? Meglio controllare se il programma è davvero disponibile."

    override val errorRunTimeoutException: String =
        "L'esecuzione del comando di sistema ha raggiunto il valore di timeout impostato ed è stata interrotta. Se il tempo è troppo breve, assicurati di impostarne uno più lungo."

    override fun errorSaveCouldNotSaveXml(name: String): String =
        "Non ho potuto salvare il database YAML con il nome '${name}'. Purtroppo non ho idea del perché sia successo. Forse non ho i permessi appropriati per scrivere il file YAML sul disco."

    override fun errorSessionObtainUnknownKey(key: String): String =
        "Il metodo 'get' ha trovato una chiave sconosciuta '${key}' nell'ambito della sessione. Non posso ottenere qualcosa che non ho da nessuna parte. Specifica una chiave valida e riprova."

    override fun errorSessionRemoveUnknownKey(key: String): String =
        "Il metodo 'remove' ha trovato una chiave sconosciuta '${key}' nell'ambito della sessione. Non posso ottenere qualcosa che non ho da nessuna parte. Specifica una chiave valida e riprova."

    override fun errorValidateEmptyFilesList(lines: String): String =
        "Ho letto la direttiva ${lines}, ma la lista 'files' fornita è vuota. Puoi correggerlo facilmente assicurandoti che la lista abbia almeno un elemento, poi riprova."

    override fun errorValidateFilesIsNotAList(lines: String): String =
        "Ho letto la direttiva ${lines} e ho scoperto che 'files' richiede una lista. Assicurati di correggere il tipo a una lista appropriata e riprova."

    override fun errorValidateInvalidDirectiveFormat(lines: String): String =
        "Ho visto nel file fornito la direttiva ${lines} che non è valida. Assicurati di correggere la direttiva e riprova."

    override val errorValidateNoDirectivesFound: String =
        "Sembra che nel file fornito non ci siano direttive. Assicurati che ce ne sia almeno una e riprova."

    override fun errorValidateOrphanLinebreak(line: String): String =
        "Sembra che ci sia un fine riga che lascia orfana una direttiva alla riga ${line}. Non posso andare avanti. Correggi la direttiva e riprova."

    override fun errorValidateReferenceIsReserved(lines: String): String =
        "Ho letto la direttiva ${lines} dove è stata usata la chiave 'reference'. Questa chiave è riservata e non puoi usarla. Non preoccuparti, lo metti facilmente a posto dando un altro nome."

    override fun errorValidateYamlException(lines: String): String =
        "C'è stato un problema con la mappa YAML fornita con la direttiva ${lines}. Questa parte è un po' complicata, perché riguarda aspetti del formato interno di serializzazione dei dati. Farò del mio meglio per darti una mano, per quanto posso."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String): String =
        "L'identificatore dell'argomento '${identifier}' è riservato, quindi non puoi adoperarlo. È facile correggerlo: chiamalo in modo diverso."

    override val errorValidateBodyDuplicateArgumentIdentifiers: String =
        "Sembra che tu abbia identificatori di argomenti duplicati nella tua regola. Correggi e riprova."

    override val errorValidateBodyMissingKeys: String =
        "Quando si definisce un ambito per l'argomento in una regola bisogna adoperare almeno 'flag' o 'default'. Assicurati che ci sia uno di questi."

    override val errorValidateBodyNullArgumentId: String =
        "Ho trovato che uno degli argomenti non ha identificatore. Assicurati di assegnare un identificatore all'argomento e riprova."

    override val errorValidateBodyNullCommand: String =
        "Ho trovato un comando 'nullo' nella regola fornita. Dovrebbe essere facile correggere: aggiungi un comando valido alla regola."

    override val errorValidateHeaderNullName: String =
        "La regola fornita non ha nome. È facile correggere: specifica un nome valido e riprova."

    override fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String =
        "La regola ha un identificatore sbagliato. Mi sarei aspettato '${expected}', ma ho trovato '${actual}'. È facile correggere: correggi l'identificatore sbagliato con quello giusto."

    override val infoLabelProject = "Progetto:"

    override val infoDisplaySummaryTitle = "Riepilogo"

    override val infoDisplaySummaryFile = "File"

    override val infoDisplaySummaryTasks = "Attività"

    override val infoDisplaySummaryTime = "Tempo"

    override val infoDisplayFullOutputInLog =
        "l’output completo è nel file di log"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "SUCCESSO TOTALE · ${if (files == 1) "1 file" else "$files file"}, " +
            "${if (tasks == 1) "1 attività" else "$tasks attività"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "FALLIMENTO TOTALE · ${if (files == 1) "1 file" else "$files file"}, " +
            "${if (tasks == 1) "1 attività" else "$tasks attività"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "SIMULAZIONE · ${if (tasks == 1) "1 attività" else "$tasks attività"}"

    override val infoConsoleSkipped: String =
        "Saltata"

    override val infoConsoleUnprocessed: String =
        "Non elaborato"

    override val infoDisplayExceptionMoreDetails: String =
        "Ci sono altri dettagli riguardo a questa eccezione:"

    override fun infoDisplayExecutionTime(time: String): String =
        "Totale: ${time} secondi"

    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String =
        "Elaborazione '${name}' (totale: ${size}, modificato il: ${lastModified}), attendere."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String =
        "Sebbene stia eseguendo in modo 'dry-run', questo elemento potrebbe essere già stato elaborato, perché ha restituito un valore booleano: ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String): String =
        "Sto per lanciare: ${command}"

    override val infoLabelAuthor: String =
        "Autore:"

    override val infoLabelAuthors: String =
        "Autori:"

    override val infoLabelSystemCommand = "Comando di sistema:"

    override val infoLabelConditional: String =
        "Condizionale:"

    override val infoLabelNoAuthors: String =
        "Nessun autore specificato"

    override val infoLabelOnWarning: String =
        "AVVISO"

    override val infoLabelOnQuestion: String =
        "DOMANDA"

    override val infoLabelOnInfo: String =
        "INFO"

    override val infoLabelDialogPrompt: String =
        "Scegli un\u2019opzione"

    override val infoLabelOnDetails: String =
        "DETAILS"

    override val infoLabelOnError: String =
        "ERROR"

    override val infoLabelOnFailure: String =
        "FAILURE"

    override val infoLabelOnSuccess: String =
        "SUCCESS"

    override val infoLabelUnnamedTask: String =
        "Compito senza nome"

    override val infoParserNotes: String =
        "arara stesso è rilasciato in accordo con la licenza New BSD."

    override val logInfoBeginBuffer: String =
        "BEGIN OUTPUT BUFFER"

    override fun logInfoBooleanMode(value: Boolean): String =
        "Valore booleano: ${value}"

    override val logInfoDirectivesBlock: String =
        "DIRECTIVES"

    override val logInfoEndBuffer: String =
        "END OUTPUT BUFFER"

    override fun logInfoInterpretRule(rule: String): String =
        "Sono pronto a interpretare la regola '${rule}'."

    override fun logInfoInterpretTask(task: String, rule: String): String =
        "Sono pronto a interpretare il compito '${task}' della regola '${rule}'."

    override fun logInfoPotentialPatternFound(line: String, pattern: String): String =
        "Ho trovato un potenziale schema alla riga ${line}: ${pattern}"

    override fun logInfoRuleLocation(path: String): String =
        "Posizione della regola: '${path}'"

    override fun logInfoSystemCommand(command: String): String =
        "Comando di sistema: ${command}"

    override val logInfoTaskResult: String =
        "Risultato del compito:"

    override val logInfoValidatedDirectives: String =
        "Tutte le direttive erano valide. Pronti a partire."

    override fun logInfoWelcomeMessage(version: String): String =
        "Benvenuto in arara ${version}!"
}
