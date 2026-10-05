// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Italian localization of the arara messages.
 * Translated by Enrico Gregorio, Carla Maggi and Paulinho van Duck.
 * The documentation of each member is inherited from the
 * [AraraMessages] interface, the single source of truth for
 * what every message says.
 */
@Suppress("MaxLineLength", "TooManyFunctions")
object ItalianMessages : AraraMessages {
    override val providedLocale = MPPLocale("it")

    override fun errorBasenameNotAFile(path: String) =
        "Il metodo 'basename' richiede un file, non una directory. '${path}' non sembra proprio essere un file. Se devi eseguire un compito su una directory, puoi adoperare uno dei metodi delle API Java."

    override val errorCalculateHashIoException =
        "Per qualche motivo, non ho potuto calcolare un hash. Purtroppo non ho idea del perché sia andata male. Forse il file è stato spostato o cancellato durante l'operazione di hashing. O forse non ho i permessi necessari per leggere il file."

    override fun errorCheckBooleanNotValidBoolean(value: String) =
        "Sembra che '${value}' non sia un valore booleano valido. Penso che sia facile correggerlo. Assicurati di adoperare una stringa valida che rappresenti un valore booleano (yes e no, true e false, 1 e 0, oppure on e off)."

    override fun errorCheckRegexIoException(file: String) =
        "Non sono riuscito a leggere il contenuto del file '${file}' e ho ricevuto un errore di I/O. Purtroppo non ho idea del perché sia andata male. Forse il file è stato spostato o cancellato durante l'operazione di hashing. O forse non ho i permessi necessari per leggere il file."

    override val errorConfigurationGenericError =
        "Non sono riuscito a leggere il file di configurazione, è successo qualcosa che non va. Questa parte è un po' complicata, perché riguarda aspetti del formato interno di serializzazione dei dati. Farò del mio meglio per darti una mano, per quanto posso."

    override val errorConfigurationLoopsInvalidRange =
        "Il valore definito nella chiave 'loops' del file di configurazione per stabilire il massimo numero di cicli ha un intervallo non valido. Assicurati che sia un valore 'lungo' e positivo."

    override val errorConfigurationLuaReturnNotATable =
        "Un file di configurazione Lua deve restituire una tabella."

    override fun errorConfigurationUnknownKeys(keys: String) =
        "La configurazione contiene chiavi che il formato non accetta: ${keys}."

    override fun errorConfigurationKeyRequiresString(key: String) =
        "La chiave '${key}' richiede un valore stringa."

    override fun errorConfigurationKeyRequiresInteger(key: String) =
        "La chiave '${key}' richiede un valore intero."

    override fun errorConfigurationKeyRequiresBoolean(key: String) =
        "La chiave '${key}' richiede un valore booleano."

    override fun errorConfigurationEntryNotAString(key: String) =
        "Ogni voce di '${key}' deve essere una stringa."

    override fun errorConfigurationKeyRequiresStringList(key: String) =
        "La chiave '${key}' richiede una lista di stringhe."

    override fun errorConfigurationKeyRequiresTable(key: String) =
        "La chiave '${key}' richiede una tabella."

    override fun errorConfigurationKeyRequiresStringKeys(key: String) =
        "La chiave '${key}' richiede chiavi di tipo stringa."

    override fun errorConfigurationKeyRequiresStringValues(key: String) =
        "La chiave '${key}' richiede valori di tipo stringa."

    override val errorConfigurationFileTypeEntryNotATable =
        "Ogni voce di 'file_types' richiede una tabella con una 'extension' e un 'pattern' facoltativo."

    override val errorConfigurationFileTypeExtensionMissing =
        "Ogni voce di 'file_types' richiede un'estensione ('extension')."

    override val errorConfigurationFileTypeKeyRequiresTableList =
        "La chiave 'file_types' richiede una lista di tabelle."

    override val errorConfigurationFileTypePatternNotAString =
        "La chiave 'pattern' di una voce 'file_types' richiede un valore stringa."

    override fun errorConfigurationPreamblePrependInvalid(value: String) =
        "La chiave 'prepend' del gruppo 'preamble' accetta 'always' o 'without-directives', non '${value}'."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String) =
        "Non sono riuscito a trovare il file '${file}' con nessuna delle estensioni ${extensions}. Assicurati che il file esista e abbia un'estensione valida."

    override val errorEvaluateCompilationFailed =
        "Per qualche ragione, non sono riuscito a compilare l'espressione del condizionale fornito. Questa parte è un po' complicata, perché riguarda aspetti del linguaggio interno per le espressioni. Farò del mio meglio per darti una mano, per quanto posso."

    override val errorEvaluateNotBooleanValue =
        "La valutazione del condizionale si aspettava un valore booleano come risultato. Dovrebbe essere facile correggerlo. Assicurati che la valutazione del condizionale fornisca alla fine un valore booleano."

    override val errorExtractorIoError =
        "C'è stato un errore di I/O mentre provavo a estrarre le direttive. Purtroppo non ho idea del perché sia andata male. Forse il file è stato spostato o cancellato prima o durante l'operazione di hashing. O forse non ho i permessi necessari per leggere il file."

    override val errorExtractorInterpolationFailure =
        "Il campo 'options' ha un contenuto che il parser MVEL non riesce a gestire. Correggi le opzioni in modo che non contengano parti di forma scorretta con '@{...}'."

    override val errorExtractorOrbTagInLuaEngine =
        "Il campo 'options' contiene un tag orb del motore YAML deprecato, che il motore Lua non può interpolare. Correggi la voce riscrivendola con i tag '{{ … }}'."

    override fun errorFiletypeInvalidPattern(pattern: String) =
        "Sembra che il pattern \"${pattern}\" che volevi usare per questo tipo di file non sia un'espressione regolare valida. Dovrebbe essere facile da sistemare: assicurati di usare un'espressione valida e riprova."

    override fun errorFiletypeNotAFile(path: String) =
        "Il metodo 'filetype' richiede un file, non una directory. '${path}' non sembra proprio essere un file. Se devi eseguire un compito su una directory, puoi adoperare uno dei metodi delle API Java."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String) =
        "Non riconosco '${extension}' come un'estensione standard. Se vuoi definire un nuovo tipo di file, assicurati di fornire l'estensione e lo schema. Queste sono le estensioni standard: ${extensions}"

    override val errorGetApplicationPathEncodingException =
        "C'è stato un problema di codifica mentre tentavo di ottenere il percorso dell'applicazione. Non c'è molto che possa fare al riguardo."

    override fun errorInterpreterArgumentIsRequired(argument: String) =
        "Sembra che '${argument}' sia contrassegnato come obbligatorio nella regola, ma non l'ho trovato nei parametri della direttiva. Assicurati di aggiungerlo come parametro nella direttiva e riprova."

    override val errorInterpreterCommandRuntimeError =
        "Non ho potuto valutare uno dei comandi forniti. Questa parte è un po' complicata perché coinvolge aspetti del linguaggio per le espressioni sottostante. Farò del mio meglio per darti una mano, per quanto posso."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String) =
        "Il comando \"${command}\" ha dato un esito inaspettato, con codice di ritorno non a zero. Guarda l'output per valutare le eventuali azioni da intraprendere."

    override val errorInterpreterUserRequestedHalt =
        "Su richiesta di una regola, il file verrà ora interrotto e le sue direttive rimanenti verranno saltate."

    override val errorInterpreterExitRuntimeError =
        "Non sono riuscito a valutare lo stato di uscita di uno dei comandi forniti. Questa parte è un po' complicata perché coinvolge aspetti del linguaggio per le espressioni sottostante. Farò del mio meglio per darti una mano, per quanto posso."

    override val errorInterpreterFlagRuntimeException =
        "Non ho potuto valutare la flag di uno degli argomenti. Questa parte è un po' complicata perché coinvolge aspetti del linguaggio per le espressioni sottostante. Farò del mio meglio per darti una mano, per quanto posso."

    override fun errorInterpreterHaltStatusNotANumber(key: String) =
        "Sembra che il valore contenuto nella chiave di sessione di arresto \"${key}\" non sia un numero valido, quindi non posso riportare lo stato di arresto richiesto. Dovrebbe essere facile correggere: assicurati che lo stato di arresto sia un numero e riprova."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String) =
        "Non sono riuscito a trovare una regola chiamata '${identifier}' nei percorsi per le regole impostati. Forse una parola scritta sbagliata? Stavo cercando un file con il nome '${expected}' nei seguenti percorsi in ordine di priorità: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String) =
        "Ho trovato queste chiavi sconosciute nella direttiva: ${keys}. Dovrebbe essere facile correggere, toglile dalla mappa."

    override fun errorConsoleModeUnknown(mode: String) =
        "Non posso impostare la console su '${mode}', perché non è una delle presentazioni disponibili. Dovrebbe essere facile correggere: scegli semplicemente una delle modalità disponibili."

    override val errorInterpreterWrongExitClosureReturn =
        "L'espressione di 'uscita' deve essere sempre un valore booleano (anche se non c'è alcun calcolo nella parte di chiusura). Dovrebbe essere facile correggere: assicurati che l'asserzione del 'type return' sia giusta e riprova."

    override val errorInterpreterWrongReturnType =
        "Una regola dovrebbe restituire solo un booleano o un comando. Altri tipi di risposta non sono supportati."

    override val errorInvalidProjectFile =
        "Ho letto un file che avrebbe dovuto essere un progetto. Purtroppo si è rivelato né un progetto né una lista di progetti. Per favore, controlla che il tuo file di progetto segua il formato documentato."

    override fun errorLoadCouldNotLoadDatabase(name: String) =
        "Non ho potuto caricare il database dei file di nome '${name}'. Purtroppo non ho idea del perché sia andata male. Forse il file è stato spostato o cancellato prima o durante l'operazione di lettura, o forse non ho i permessi necessari per leggerlo. Già che ci siamo: se il file contiene i dati di una versione precedente, basta rimuoverlo o rinominarlo, e alla prossima esecuzione costruisco un database nuovo."

    override fun errorParserInvalidPreamble(name: String) =
        "Mi dispiace, ma non è stato possibile trovare il preambolo '${name}'. Assicurati che questa chiave esista nel file di configurazione."

    override val errorParseRuleGenericError =
        "Non ho potuto analizzare la regola, qualcosa è andato storto. Questa parte è un po' complicata, perché riguarda aspetti del formato interno di serializzazione dei dati. Farò del mio meglio per darti una mano, per quanto posso."

    override fun errorParseRuleArgumentIdNotAString() =
        "L'identificatore dell'argomento richiede un valore stringa."

    override fun errorParseRuleArgumentNeedsFlagOrDefault(identifier: String) =
        "L'argomento '${identifier}' richiede almeno uno tra 'flag' e 'default'."

    override fun errorParseRuleArgumentRequiredNotABoolean(identifier: String) =
        "La chiave 'required' dell'argomento '${identifier}' richiede un valore booleano."

    override val errorParseRuleCommandFunctionMissing =
        "Ogni comando richiede una funzione 'command'."

    override val errorParseRuleCommandNameNotAString =
        "Il nome del comando richiede un valore stringa."

    override fun errorParseRuleEntryNotAString(key: String) =
        "Ogni voce di '${key}' deve essere una stringa."

    override fun errorParseRuleEntryNotATable(key: String) =
        "Ogni voce di '${key}' deve essere una tabella."

    override val errorParseRuleIdentifierMissing =
        "La regola non fornisce un identificatore."

    override val errorParseRuleIdentifierNotAString =
        "L'identificatore della regola richiede un valore stringa."

    override fun errorParseRuleKeyRequiresStringList(key: String) =
        "La chiave '${key}' richiede una lista di stringhe."

    override fun errorParseRuleKeyRequiresTableList(key: String) =
        "La chiave '${key}' richiede una lista di tabelle."

    override val errorParseRuleNameNotAString =
        "Il nome della regola richiede un valore stringa."

    override fun errorParseRuleUnsupportedFormat(rule: String, location: String, format: String) =
        "Ho trovato un errore nella regola \"${rule}\" situata in \"${location}\". Il formato della regola \"${format}\" non è supportato da arara."

    override fun errorProjectCycle(cycle: String) =
        "Le dipendenze del progetto contengono un ciclo che coinvolge ${cycle}. I cicli non possono essere risolti. Per favore, rivedi la specifica del tuo progetto."

    override fun errorProjectStrayDependency(dependency: String) =
        "La specifica di progetto indica il progetto \"${dependency}\" come dipendenza, ma nessun progetto dell'esecuzione porta quel nome. Rivedi la specifica di progetto e riprova."

    override val errorProjectDependenciesKeyRequiresStringList =
        "La chiave 'dependencies' richiede una lista di stringhe."

    override val errorProjectDependencyEntryNotAString =
        "Ogni voce di dipendenza deve essere una stringa."

    override val errorProjectDirectiveEntryNotAString =
        "Ogni voce di direttiva deve essere una stringa."

    override val errorProjectDirectivesKeyRequiresTable =
        "La chiave 'directives' richiede una tabella."

    override fun errorProjectDuplicateName(name: String) =
        "Due progetti della specifica condividono il nome '${name}'. In un'unica esecuzione, un progetto è identificato dal suo nome, quindi ogni progetto di una lista ha bisogno di un nome tutto suo."

    override fun errorProjectDuplicatePath(name: String) =
        "Il progetto elenca il file '${name}' più di una volta, forse con due grafie dello stesso percorso. In un progetto, un file è elencato una sola volta."

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "Si è verificato un errore durante l'estrazione delle direttive specificate per '${file}' nel file di progetto: ${details}."

    override val errorProjectFileEntryKeyNotAString =
        "Le voci dei file di progetto devono essere indirizzate dal loro nome di file."

    override val errorProjectFileIsADirectory =
        "Un file di progetto non può essere una cartella."

    override val errorProjectFileSpecNotATable =
        "Una specifica di file di progetto deve essere una tabella."

    override val errorProjectFilesKeyRequiresTable =
        "La chiave 'files' richiede una tabella di specifiche di file."

    override val errorProjectFileTypeExtensionRequired =
        "È richiesta un'estensione per identificare il tipo di file."

    override val errorProjectFileTypeKeyRequiresTable =
        "La chiave 'file_type' richiede una tabella."

    override val errorProjectFileTypePatternMissing =
        "Manca un pattern e l'estensione non è tra i tipi di file predefiniti."

    override val errorProjectFileTypePatternNotAString =
        "La chiave 'pattern' del tipo di file richiede un valore stringa."

    override fun errorProjectFileTypeUnknownExtension(file: String) =
        "Non sono riuscito a determinare il tipo di file di '${file}': nessun tipo di file di questa esecuzione corrisponde alla sua estensione. Dichiara un 'file_type' per la voce, oppure aggiungi l'estensione ai 'file_types' della configurazione."

    override fun errorProjectInvalidLua(details: String) =
        "La specifica di progetto non è Lua valido: ${details}."

    override val errorProjectListEntryNotATable =
        "Una voce di una lista di progetti deve essere una tabella."

    override val errorProjectListEntryNeedsName =
        "Ogni progetto in una lista di progetti richiede un nome esplicito, dato che le dipendenze identificano i progetti per nome."

    override val errorProjectNameKeyRequiresString =
        "La chiave 'name' del progetto richiede un valore stringa."

    override val errorProjectNeedsAFile =
        "Un progetto deve contenere almeno un file."

    override val errorProjectPriorityKeyRequiresNumber =
        "La chiave 'priority' richiede un numero."

    override val errorProjectReturnNotATable =
        "Il tipo di ritorno della specifica di progetto Lua deve essere una tabella, contenente un progetto o una lista di progetti."

    override val errorProjectSingleProjectHasDependencies =
        "Una specifica di progetto singolo non può dichiarare dipendenze."

    override fun errorProjectUnknownKey(name: String) =
        "La specifica di progetto contiene chiavi che il formato non accetta: ${name}."

    override val errorProjectWorkingDirectoryKeyRequiresString =
        "La chiave 'working_directory' richiede un valore stringa."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "Il pattern glob \"${pattern}\" non ha potuto essere convertito in un'espressione regolare: ${details}. Assicurati che il pattern sia ben formato e riprova."

    override val errorReplicateListMissingFormatArgumentsException =
        "Non sono riuscito a replicare la lista perché manca un argomento per il formato. Penso che si tratti di un numero sbagliato di parametri. Assicurati che il numero di parametri sia corretto e riprova."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String) =
        "Ho trovato un errore nella regola '${identifier}' alla posizione '${location}'."

    override val errorRunGenericException =
        "Non ho potuto lanciare il comando di sistema richiesto, qualcosa è andato storto. Questa parte è un po' complicata, perché riguarda aspetti del linguaggio interno per le espressioni. Farò del mio meglio per darti una mano, per quanto posso."

    override val errorRunInterruptedException =
        "L'esecuzione del comando di sistema richiesto si è interrotta inaspettatamente. Forse un'interruzione esterna ha forzato il comando a terminare."

    override val errorRunInvalidExitValueException =
        "Il comando di sistema richiesto ha restituito un valore di uscita non valido."

    override val errorRunIoException =
        "L'esecuzione del comando di sistema è fallita per via di un errore di I/O. Sei sicuro che il programma esista nei tuoi percorsi? Meglio controllare se il programma è davvero disponibile."

    override val errorRunTimeoutException =
        "L'esecuzione del comando di sistema ha raggiunto il valore di timeout impostato ed è stata interrotta. Se il tempo è troppo breve, assicurati di impostarne uno più lungo."

    override val errorSafeModeClassLoading =
        "In modalità sicura, alle regole non è consentito caricare classi arbitrarie da archivi forniti dall'utente."

    override val errorSafeModeSystemCommand =
        "In modalità sicura, alle regole non è consentito eseguire comandi di sistema arbitrari all'interno di un passo di esecuzione."

    override fun errorSaveCouldNotSaveDatabase(name: String) =
        "Non ho potuto salvare il database dei file con il nome '${name}'. Purtroppo non ho idea del perché sia successo. Forse non ho i permessi appropriati per scrivere il file sul disco."

    override fun errorSessionObtainUnknownKey(key: String) =
        "Il metodo 'get' ha trovato una chiave sconosciuta '${key}' nell'ambito della sessione. Non posso ottenere qualcosa che non ho da nessuna parte. Specifica una chiave valida e riprova."

    override fun errorSessionRemoveUnknownKey(key: String) =
        "Il metodo 'remove' ha trovato una chiave sconosciuta '${key}' nell'ambito della sessione. Non posso ottenere qualcosa che non ho da nessuna parte. Specifica una chiave valida e riprova."

    override fun errorValidateEmptyFilesList(lines: String) =
        "Ho letto la direttiva ${lines}, ma la lista 'files' fornita è vuota. Puoi correggerlo facilmente assicurandoti che la lista abbia almeno un elemento, poi riprova."

    override fun errorValidateFilesIsNotAList(lines: String) =
        "Ho letto la direttiva ${lines} e ho scoperto che 'files' richiede una lista. Assicurati di correggere il tipo a una lista appropriata e riprova."

    override fun errorValidateInvalidDirectiveFormat(lines: String) =
        "Ho visto nel file fornito la direttiva ${lines} che non è valida. Assicurati di correggere la direttiva e riprova."

    override val errorValidateNoDirectivesFound =
        "Sembra che nel file fornito non ci siano direttive. Assicurati che ce ne sia almeno una e riprova."

    override fun errorValidateOrphanLinebreak(line: Int) =
        "Sembra che ci sia un fine riga che lascia orfana una direttiva alla riga ${line}. Non posso andare avanti. Correggi la direttiva e riprova."

    override fun errorValidateReferenceIsReserved(lines: String) =
        "Ho letto la direttiva ${lines} dove è stata usata la chiave 'reference'. Questa chiave è riservata e non puoi usarla. Non preoccuparti, lo metti facilmente a posto dando un altro nome."

    override fun errorValidateYamlException(lines: String) =
        "C'è stato un problema con la mappa YAML fornita con la direttiva ${lines}. Questa parte è un po' complicata, perché riguarda aspetti del formato interno di serializzazione dei dati. Farò del mio meglio per darti una mano, per quanto posso."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String) =
        "L'identificatore dell'argomento '${identifier}' è riservato, quindi non puoi adoperarlo. È facile correggerlo: chiamalo in modo diverso."

    override val errorValidateBodyDuplicateArgumentIdentifiers =
        "Sembra che tu abbia identificatori di argomenti duplicati nella tua regola. Correggi e riprova."

    override val errorValidateBodyMissingKeys =
        "Quando si definisce un ambito per l'argomento in una regola bisogna adoperare almeno 'flag' o 'default'. Assicurati che ci sia uno di questi."

    override val errorValidateBodyNullArgumentId =
        "Ho trovato che uno degli argomenti non ha identificatore. Assicurati di assegnare un identificatore all'argomento e riprova."

    override val errorValidateBodyNullCommand =
        "Ho trovato un comando 'nullo' nella regola fornita. Dovrebbe essere facile correggere: aggiungi un comando valido alla regola."

    override val errorValidateHeaderNullName =
        "La regola fornita non ha nome. È facile correggere: specifica un nome valido e riprova."

    override fun errorValidateHeaderWrongIdentifier(expected: String, found: String) =
        "La regola ha un identificatore sbagliato. Mi sarei aspettato '${expected}', ma ho trovato '${found}'. È facile correggere: correggi l'identificatore sbagliato con quello giusto."

    override val infoDisplayExceptionMoreDetails =
        "Ci sono altri dettagli riguardo a questa eccezione:"

    override val infoDisplayExecutionTime = "Tempo trascorso:"
    override val infoDisplayTimeUnit = "secondi"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "SUCCESSO TOTALE · ${if (files == 1) "1 file" else "$files file"}, " +
            "${if (tasks == 1) "1 attività" else "$tasks attività"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "FALLIMENTO TOTALE · ${if (files == 1) "1 file" else "$files file"}, " +
            "${if (tasks == 1) "1 attività" else "$tasks attività"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "SIMULAZIONE · ${if (tasks == 1) "1 attività" else "$tasks attività"}"
    override fun infoDisplaySkippedConditional(name: String) =
        "${name} ignorato · la condizione ha restituito falso"

    override val infoDisplayFullOutputInLog =
        "l’output completo è nel file di log"


    override val infoDisplaySummaryTitle = "Riepilogo"
    override val infoDisplaySummaryFile = "File"
    override val infoDisplaySummaryTasks = "Attività"
    override val infoDisplaySummaryTime = "Tempo"

    override fun infoDisplayUnprocessedFiles(count: Int) =
    if (count == 1) "File non elaborato: " else "File non elaborati: "


    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String) =
        "Elaborazione '${name}' (totale: ${size}, modificato il: ${lastModified}), attendere."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean) =
        "Sebbene stia eseguendo in modo 'dry-run', questo elemento potrebbe essere già stato elaborato, perché ha restituito un valore booleano: ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String) =
        "Sto per lanciare: ${command}"

    override val infoInterpreterLuaFileEvaluationFailed =
        "Sembra che la valutazione del file Lua fornito come specifica di progetto abbia sollevato un errore imprevisto, quindi lo tratterò come un normale file di input. Se doveva essere una specifica di progetto, consulta il registro per i dettagli su cosa è andato storto."

    override val infoInterpreterLuaFileNotAProject =
        "Sembra che il file Lua fornito non possa essere interpretato come specifica di progetto, quindi lo tratterò come un normale file di input. Se doveva essere una specifica di progetto, consulta il registro per i dettagli su cosa è andato storto."

    override val infoLabelAuthor =
        "Autore:"

    override val infoLabelAuthors =
        "Autori:"

    override val infoLabelConditional =
        "Condizionale:"

    override val infoLabelNoAuthors =
        "Nessun autore specificato"

    override val infoLabelOnDetails =
        "Dettagli"

    override val infoLabelOnError =
        "ERROR"

    override val infoLabelOnFailure =
        "FAILURE"

    override val infoLabelOnSuccess =
        "SUCCESS"
    override val infoLabelOnWarning = "AVVISO"
    override val infoLabelOnQuestion = "DOMANDA"
    override val infoLabelOnInfo = "INFO"
    override val infoLabelDialogPrompt = "Scegli un\u2019opzione"
    override val infoLabelAboutToRun =
        "Da eseguire:"
    override val infoLabelSystemCommand = "Comando di sistema:"

    override val errorDialogInputClosed =
        "Sembra che l\u2019input del terminale si sia chiuso mentre un dialogo attendeva una risposta, quindi non posso leggere la tua scelta. Esegui arara in un terminale interattivo, cos\u00ec il dialogo pu\u00f2 ricevere una risposta."
    override val errorDialogsNotConfigured =
        "Sembra che questa esecuzione non abbia alcun supporto per i dialoghi configurato, quindi non posso mostrare il dialogo richiesto da una regola. Fornisci un\u2019implementazione dei dialoghi nell\u2019applicazione che esegue arara, oppure evita regole con dialoghi interattivi."
    override val infoLabelUnnamedTask =
        "Compito senza nome"

    override val infoParserNotes =
        "arara stesso è rilasciato in accordo con la licenza New BSD."
}
