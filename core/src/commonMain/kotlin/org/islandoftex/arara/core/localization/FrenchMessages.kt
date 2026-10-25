// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/** French message catalog. */
@Suppress("MaxLineLength", "TooManyFunctions")
object FrenchMessages : AraraMessages {
    override val warningDeprecatedYaml: String =
        "Avertissement de compatibilité : vous avez utilisé des règles YAML obsolètes qui seront supprimées dans la version 9."

    override val providedLocale: MPPLocale = MPPLocale("fr")

    override val translators: List<String> = emptyList()

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "Une erreur est survenue lors de l’extraction des directives de « ${file} » : ${details}."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "Le motif glob « ${pattern} » n’a pas pu être converti en expression régulière : ${details}. Vérifiez sa syntaxe et réessayez."

    override val errorSafeModeClassLoading =
        "En mode sécurisé, les règles ne peuvent pas charger de classes arbitraires depuis des archives fournies par l’utilisateur."

    override val errorSafeModeSystemCommand =
        "En mode sécurisé, les règles ne peuvent pas exécuter de commandes système arbitraires pendant une étape d’exécution."

    override val errorDialogInputClosed: String =
        "L’entrée du terminal a été fermée pendant l’attente d’une réponse à une boîte de dialogue. Veuillez lancer arara dans un terminal interactif pour pouvoir répondre."

    override val errorDialogsNotConfigured: String =
        "Aucune prise en charge des boîtes de dialogue n’est configurée pour cette exécution. Fournissez une implémentation dans l’application qui utilise arara, ou évitez les règles avec des dialogues interactifs."

    override fun errorScriptString(field: String): String =
        "${field} doit être une chaîne de caractères."

    override fun errorScriptTable(field: String): String =
        "${field} doit être une table."

    override fun errorScriptBoolean(field: String): String =
        "${field} doit être un booléen."

    override fun errorScriptInteger(field: String): String =
        "${field} doit être un entier."

    override fun errorScriptList(field: String): String =
        "${field} doit être une liste positionnelle sans interruption des indices."

    override fun errorScriptUnknownKey(field: String): String =
        "Clé inconnue : ${field}."

    override fun errorScriptRequired(field: String): String =
        "${field} est obligatoire."

    override fun errorScriptNonblank(field: String): String =
        "${field} ne doit pas être vide ni composé uniquement d’espaces."

    override fun errorScriptDuplicate(field: String, value: String): String =
        "${field} contient plusieurs fois « ${value} »."

    override fun errorScriptDirectory(field: String): String =
        "${field} est un répertoire."

    override fun errorScriptEmpty(field: String): String =
        "${field} doit contenir au moins une entrée."

    override fun errorScriptStringKeys(field: String): String =
        "${field} exige des clés de type chaîne non vides et non composées uniquement d’espaces."

    override fun errorProjectInvalidLua(path: String, detail: String): String =
        "Code Lua non valide dans ${path} : ${detail}"

    override fun errorProjectDuplicateNames(names: String): String =
        "Les noms des projets doivent être uniques : ${names}."

    override fun errorProjectUnknownDependency(project: String, dependency: String): String =
        "Le projet « ${project} » dépend du projet inconnu « ${dependency} »."

    override fun errorProjectCycle(cycle: String): String =
        "Les dépendances des projets contiennent un cycle : ${cycle}."

    override fun errorProjectFileOwnership(path: String, owner: String, project: String): String =
        "Le fichier « ${path} » appartient à la fois à « ${owner} » et à « ${project} »."

    override fun errorProjectFiletype(path: String): String =
        "Aucun type de fichier configuré ne correspond à « ${path} »."

    override fun errorConfigurationPrepend(value: String): String =
        "Valeur preamble.prepend non valide : « ${value} ». Utilisez 'always' ou 'without-directives'."

    override fun errorScriptFunction(field: String): String =
        "${field} doit être une fonction."

    override fun errorScriptLiteral(field: String): String =
        "${field} doit être une valeur littérale."

    override fun errorScriptAlternatives(field: String, alternatives: String): String =
        "${field} exige au moins l’un des éléments suivants : ${alternatives}."

    override val errorLuaInterpolationFailure: String =
        "Le champ 'options' contient une expression Lua ou une balise d’interpolation non valide."

    override val errorYamlTagsInLua: String =
        "Les balises MVEL nécessitent le moteur YAML ; Lua utilise {{ expression }}."

    override val errorLuaTagsInYaml: String =
        "Les balises Lua nécessitent --engine lua."

    override val errorLuaUnterminatedTag: String =
        "Balise d’interpolation non terminée."

    override val errorLuaUnterminatedQuote: String =
        "Chaîne entre guillemets non terminée dans une balise d’interpolation."

    override val errorLuaUnterminatedLongString: String =
        "Chaîne longue non terminée dans une balise d’interpolation."

    override val errorLuaMapKeys: String =
        "Des clés de la table de correspondance deviennent identiques après conversion en chaînes Lua."

    override val errorLuaTableKeys: String =
        "Des clés de la table Lua deviennent identiques après conversion en chaînes."

    override fun errorLuaExactInteger(value: String): String =
        "L’entier ${value} ne peut pas être représenté exactement par un nombre Lua."

    override val errorLuaCyclicValues: String =
        "Les tables et les collections cycliques ne peuvent pas être converties."

    override fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String =
        "argument n° ${index} non valide pour '${helper}' (${expected} attendu, ${actual} reçu)"

    override fun errorLuaBooleanResult(helper: String, actual: String): String =
        "résultat non valide de '${helper}' (boolean attendu, ${actual} reçu)"

    override fun errorLuaHelperNoArgs(helper: String): String =
        "${helper} exige au moins un argument."

    override fun errorLoadCouldNotLoadDatabase(name: String): String =
        "Impossible de charger la base de données « ${name} ». Vérifiez les droits d’accès et l’intégrité du cache YAML."

    override fun errorSaveCouldNotSaveDatabase(name: String): String =
        "Impossible d’enregistrer la base de données « ${name} ». Vérifiez que son répertoire parent existe et est accessible en écriture."

    override fun errorBasenameNotAFile(path: String): String =
        "La méthode 'basename' exige un fichier et non un répertoire. « ${path} » ne semble pas être un fichier. Pour effectuer des opérations sur un répertoire, vous pouvez utiliser les méthodes de l’API Java."

    override val errorCalculateHashIoException: String =
        "Impossible de calculer l’empreinte. Le fichier a peut-être été déplacé ou supprimé avant ou pendant le calcul, ou les droits de lecture sont insuffisants."

    override fun errorCheckBooleanNotValidBoolean(value: String): String =
        "« ${value} » n’est pas une valeur booléenne valide. Utilisez une chaîne représentant un booléen : yes et no, true et false, 1 et 0, ou on et off."

    override fun errorCheckRegexIoException(file: String): String =
        "Une erreur d’entrée-sortie empêche la lecture du fichier « ${file} ». Le fichier a peut-être été déplacé ou supprimé avant ou pendant la lecture, ou les droits de lecture sont insuffisants."

    override val errorConfigurationGenericError: String =
        "Impossible d’analyser le fichier de configuration. Vérifiez que son contenu respecte le format de sérialisation utilisé. Des détails sur l’erreur sont fournis lorsqu’ils sont disponibles."

    override val errorConfigurationLoopsInvalidRange: String =
        "La valeur de la clé 'loops' du fichier de configuration, qui définit le nombre maximal d’itérations, n’est pas valide. Utilisez un entier positif dans la plage autorisée."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String): String =
        "Impossible de trouver le fichier « ${file} » ${extensions}. Vérifiez qu’il existe et possède une extension valide."

    override fun errorDiscoverFileSafeModeNoCompletion(file: String) =
        "Impossible de trouver le fichier « ${file} ». Le mode sécurisé ne complète pas les extensions : fournissez le nom complet du fichier, extension comprise."

    override fun errorDiscoverFileUnknownExtension(file: String, extensions: String) =
        "Impossible de déterminer le type du fichier « ${file} » : son extension n’est pas configurée. Extensions disponibles : ${extensions}. Ajoutez l’extension manquante à la configuration (file_types en Lua, filetypes en YAML)."

    override fun errorDiscoverFileUnknownLuaExtension(file: String, extensions: String) =
        "Impossible de déterminer le type du fichier « ${file} » : son extension n’est pas configurée. Extensions disponibles : ${extensions}. Ajoutez l’extension à la configuration (file_types en Lua, filetypes en YAML), ou utilisez le suffixe .proj.lua s’il s’agit d’une spécification de projet."

    override val errorEvaluateCompilationFailed: String =
        "Impossible de compiler l’expression de la condition. Vérifiez sa syntaxe dans le langage d’expression utilisé."

    override val errorEvaluateNotBooleanValue: String =
        "L’évaluation de la condition doit renvoyer un booléen. Vérifiez que son résultat est bien une valeur booléenne."

    override val errorExtractorIoError: String =
        "Une erreur d’entrée-sortie empêche l’extraction des directives. Le fichier a peut-être été déplacé ou supprimé, n’est pas encodé en UTF-8, ou les droits de lecture sont insuffisants."

    override val errorExtractorInterpolationFailure: String =
        "Le champ 'options' contient une entrée que l’analyseur MVEL ne peut pas traiter. Vérifiez qu’aucune partie '@{…}' n’est mal formée."

    override fun errorFiletypeInvalidPattern(pattern: String): String =
        "Le motif « ${pattern} » de ce type de fichier n’est pas une expression régulière valide. Corrigez l’expression et réessayez."

    override fun errorFiletypeNotAFile(path: String): String =
        "La méthode 'filetype' exige un fichier et non un répertoire. « ${path} » ne semble pas être un fichier. Pour effectuer des opérations sur un répertoire, vous pouvez utiliser les méthodes de l’API Java."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String): String =
        "« ${extension} » n’est pas une extension prédéfinie. Pour définir un nouveau type de fichier, fournissez son extension et son motif. Extensions prédéfinies : ${extensions}"

    override val errorGetApplicationPathEncodingException: String =
        "Un problème d’encodage empêche de déterminer le chemin de l’application."

    override fun errorInterpreterArgumentIsRequired(argument: String): String =
        "L’argument « ${argument} » est obligatoire dans la règle, mais absent des paramètres de la directive. Ajoutez-le à la directive et réessayez."

    override val errorInterpreterCommandRuntimeError: String =
        "Impossible d’évaluer l’une des commandes fournies. Vérifiez son expression dans le langage utilisé."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String): String =
        "La commande « ${command} » s’est terminée avec un code de retour non nul inattendu. Consultez sa sortie pour déterminer la marche à suivre."

    override val errorInterpreterUserRequestedHalt: String =
        "arara va s’arrêter à votre demande ou à celle d’une règle."

    override val errorInterpreterExitRuntimeError: String =
        "Impossible d’évaluer l’expression du code de retour de l’une des commandes. Vérifiez son expression dans le langage utilisé."

    override val errorInterpreterFlagRuntimeException: String =
        "Impossible d’évaluer l’expression 'flag' de l’un des arguments. Vérifiez son expression dans le langage utilisé."

    override fun errorLogFormatUnknown(value: String): String =
        "Format de journal inconnu : '${value}'. Utilisez none, text ou json."

    override fun errorConsoleModeUnknown(mode: String): String =
        "Mode de console inconnu : '${mode}'. Utilisez plain ou styled."

    override fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String =
        "Format de règle non pris en charge pour « ${identifier} » : « ${format} ». Utilisez un fichier de règle Lua ou YAML."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String): String =
        "Impossible de trouver une règle nommée « ${identifier} » dans les chemins fournis. Son nom est peut-être mal orthographié. Fichier recherché : « ${expected} ». Chemins, par ordre de priorité : ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String): String =
        "Clés inconnues dans la directive : ${keys}. Supprimez-les de la table de correspondance."

    override val errorInterpreterWrongExitClosureReturn: String =
        "L’expression 'exit' doit toujours renvoyer un booléen, même si le corps de la fonction n’effectue aucun calcul. Corrigez le type de la valeur renvoyée et réessayez."

    override val errorInterpreterWrongReturnType: String =
        "Une règle ne peut renvoyer qu’un booléen ou une commande. Les autres types ne sont pas pris en charge."

    override val errorInvalidProjectFile: String =
        "Le fichier lu ne contient ni un projet ni une liste de projets. Vérifiez qu’il respecte le format documenté des fichiers de projet."

    override fun errorLoadCouldNotLoadXml(name: String): String =
        "Impossible de charger la base de données YAML « ${name} ». Le fichier a peut-être été déplacé ou supprimé, ou les droits de lecture sont insuffisants. Vérifiez également que le fichier YAML est bien formé."

    override fun errorParserInvalidPreamble(name: String): String =
        "Le préambule « ${name} » est introuvable. Vérifiez que cette clé existe dans le fichier de configuration."

    override val errorParseRuleGenericError: String =
        "Impossible d’analyser la règle. Vérifiez que son contenu respecte le format de sérialisation utilisé. Des détails sur l’erreur sont fournis lorsqu’ils sont disponibles."

    override val errorReplicateListMissingFormatArgumentsException: String =
        "Impossible de reproduire la liste : un argument de format manque. Le nombre de paramètres est peut-être incorrect. Corrigez-le et réessayez."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String): String =
        "Une erreur a été détectée dans la règle « ${identifier} » située dans « ${location} »."

    override val errorRunGenericException: String =
        "Impossible d’exécuter la commande système fournie. Vérifiez la commande et son expression dans le langage utilisé."

    override val errorRunInterruptedException: String =
        "L’exécution de la commande système a été interrompue, peut-être par une intervention extérieure."

    override val errorRunInvalidExitValueException: String =
        "La commande système a renvoyé un code de retour non valide."

    override val errorRunIoException: String =
        "L’exécution de la commande système a échoué à cause d’une erreur d’entrée-sortie. Vérifiez que la commande existe et est accessible dans le chemin de recherche."

    override val errorRunTimeoutException: String =
        "La commande système a dépassé le délai imparti et a été arrêtée. Si ce délai est trop court, augmentez-le."

    override fun errorSaveCouldNotSaveXml(name: String): String =
        "Impossible d’enregistrer la base de données YAML « ${name} ». Vérifiez que vous avez les droits nécessaires pour écrire le fichier sur le disque."

    override fun errorSessionObtainUnknownKey(key: String): String =
        "La méthode 'get' a reçu une clé inconnue dans la session : « ${key} ». Impossible de lire une valeur absente. Indiquez une clé valide et réessayez."

    override fun errorSessionRemoveUnknownKey(key: String): String =
        "La méthode 'remove' a reçu une clé inconnue dans la session : « ${key} ». Impossible de supprimer une valeur absente. Indiquez une clé valide et réessayez."

    override fun errorValidateEmptyFilesList(lines: String): String =
        "La liste 'files' de la directive ${lines} est vide. Ajoutez au moins un élément et réessayez."

    override fun errorValidateFilesIsNotAList(lines: String): String =
        "Le champ 'files' de la directive ${lines} exige une liste. Corrigez le type de la valeur et réessayez."

    override fun errorValidateInvalidDirectiveFormat(lines: String): String =
        "La directive ${lines} n’est pas valide. Corrigez-la et réessayez."

    override val errorValidateNoDirectivesFound: String =
        "Aucune directive n’a été trouvée dans le fichier fourni. Ajoutez au moins une directive et réessayez."

    override fun errorValidateOrphanLinebreak(line: String): String =
        "Une continuation de directive isolée a été trouvée à la ligne ${line}. Corrigez la directive avant de poursuivre."

    override fun errorValidateReferenceIsReserved(lines: String): String =
        "La directive ${lines} utilise la clé réservée 'reference'. Remplacez-la par un autre nom."

    override fun errorValidateYamlException(lines: String): String =
        "La table de correspondance YAML de la directive ${lines} pose problème. Vérifiez qu’elle respecte le format de sérialisation utilisé."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String): String =
        "L’identifiant d’argument « ${identifier} » est réservé. Remplacez-le par un autre nom."

    override val errorValidateBodyDuplicateArgumentIdentifiers: String =
        "La règle contient des identifiants d’arguments en double. Corrigez-les et réessayez."

    override val errorValidateBodyMissingKeys: String =
        "Lors de la définition d’un argument de règle, au moins l’une des clés 'flag' ou 'default' doit être présente."

    override val errorValidateBodyNullArgumentId: String =
        "L’un des arguments n’a pas d’identifiant. Ajoutez un identifiant valide et réessayez."

    override val errorValidateBodyNullCommand: String =
        "La règle fournie contient une commande nulle. Ajoutez une commande valide et réessayez."

    override val errorValidateHeaderNullName: String =
        "La règle fournie n’a pas de nom. Ajoutez un nom valide et réessayez."

    override fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String =
        "L’identifiant de la règle est incorrect. Identifiant attendu : « ${expected} » ; identifiant trouvé : « ${actual} ». Remplacez l’identifiant incorrect par celui attendu."

    override val infoLabelProject =
        "Projet :"

    override val infoDisplaySummaryTitle =
        "Bilan de l’exécution"

    override val infoDisplaySummaryFile =
        "Fichier"

    override val infoDisplaySummaryTasks =
        "Tâches"

    override val infoDisplaySummaryTime =
        "Durée"

    override val infoDisplayFullOutputInLog =
        "la sortie complète se trouve dans le fichier journal"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "SUCCÈS TOTAL · ${files} ${if (files > 1) "fichiers" else "fichier"}, " +
            "${tasks} ${if (tasks > 1) "tâches" else "tâche"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "ÉCHEC TOTAL · ${files} ${if (files > 1) "fichiers" else "fichier"}, " +
            "${tasks} ${if (tasks > 1) "tâches" else "tâche"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "SIMULATION · ${tasks} ${if (tasks > 1) "tâches" else "tâche"}"

    override val infoConsoleSkipped: String =
        "Ignoré"

    override val infoConsoleUnprocessed: String =
        "Non traité"

    override val infoDisplayExceptionMoreDetails: String =
        "Détails supplémentaires sur cette exception :"

    override fun infoDisplayExecutionTime(time: String): String =
        "Total : ${time} secondes"

    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String =
        "Traitement de « ${name} » (taille : ${size}, dernière modification : ${lastModified}), veuillez patienter."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String =
        "Même en mode simulation, cette entrée peut déjà avoir été traitée car elle a renvoyé un booléen : ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String): String =
        "Commande à exécuter : ${command}"

    override val infoLabelAuthor: String =
        "Auteur :"

    override val infoLabelAuthors: String =
        "Auteurs :"

    override val infoLabelSystemCommand =
        "Commande système :"

    override val infoLabelConditional: String =
        "Condition :"

    override val infoLabelNoAuthors: String =
        "Aucun auteur indiqué"

    override val infoLabelOnWarning: String =
        "AVERTISSEMENT"

    override val infoLabelOnQuestion: String =
        "QUESTION"

    override val infoLabelOnInfo: String =
        "INFORMATION"

    override val infoLabelDialogPrompt: String =
        "Choisissez une option"

    override val infoLabelOnDetails: String =
        "DÉTAILS"

    override val infoLabelOnError: String =
        "ERREUR"

    override val infoLabelOnFailure: String =
        "ÉCHEC"

    override val infoLabelOnSuccess: String =
        "SUCCÈS"

    override val infoLabelUnnamedTask: String =
        "Tâche sans nom"

    override val infoParserNotes: String =
        "arara est distribué sous la licence New BSD."

    override val logInfoBeginBuffer: String =
        "DÉBUT DE LA SORTIE CAPTURÉE"

    override fun logInfoBooleanMode(value: Boolean): String =
        "Valeur booléenne : ${value}"

    override val logInfoDirectivesBlock: String =
        "DIRECTIVES"

    override val logInfoEndBuffer: String =
        "FIN DE LA SORTIE CAPTURÉE"

    override fun logInfoInterpretRule(rule: String): String =
        "Prêt à interpréter la règle « ${rule} »."

    override fun logInfoInterpretTask(task: String, rule: String): String =
        "Prêt à interpréter la tâche « ${task} » de la règle « ${rule} »."

    override fun logInfoPotentialPatternFound(line: String, pattern: String): String =
        "Motif possible à la ligne ${line} : ${pattern}"

    override fun logInfoRuleLocation(path: String): String =
        "Emplacement de la règle : « ${path} »"

    override fun logInfoSystemCommand(command: String): String =
        "Commande système : ${command}"

    override val logInfoTaskResult: String =
        "Résultat de la tâche :"

    override val logInfoValidatedDirectives: String =
        "Toutes les directives ont été validées. L’exécution peut commencer."

    override fun logInfoWelcomeMessage(version: String): String =
        "Bienvenue dans arara ${version} !"
}
