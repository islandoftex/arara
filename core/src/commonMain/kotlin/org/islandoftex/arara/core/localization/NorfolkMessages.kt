// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/** Norfolk message catalog. */
@Suppress("MaxLineLength", "TooManyFunctions")
object NorfolkMessages : AraraMessages {

    override fun errorScriptChoice(field: String, choices: String): String =
        "Unsupported value for $field; expected: $choices."

    override fun errorBuildDirectoryInvalid(path: String): String =
        "Invalid or overlapping build directory: $path."

    override fun errorBuildInputInvalid(path: String): String =
        "Input or destination escapes the workspace mapping: $path."

    override fun errorBuildWorkspaceUnmanaged(path: String): String =
        "The build directory is not managed by this project: $path."

    override fun errorBuildInputCollision(path: String): String =
        "Build input collides with another input or an existing output: $path."

    override fun errorBuildLinkCycle(path: String): String =
        "Symbolic link cycle in build inputs: $path."

    override fun errorBuildInputMissing(path: String): String =
        "Build input does not exist or is not a regular file or directory: $path."

    override val warningDeprecatedYaml: String =
        "Compatibility warning: you have used deprecated YAML rules which will be discontinued in v9."

    override val providedLocale: MPPLocale = MPPLocale("en-QN")

    override val translators: List<String> = listOf("Nicola Talbot", "Keith Skipper")

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "I come a-cropper extractin' the directives of '${file}': ${details}."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "That duzzy ole glob pattern \"${pattern}\" wouldn't turn inter a proper regular expression: ${details}. Make sure the pattern's put tergether right and hev another go."

    override val errorSafeModeClassLoading =
        "In the safe mode, rules ent allowed ter go loadin' no classes of their own choosin' from folks' archives."

    override val errorSafeModeSystemCommand =
        "In the safe mode, rules ent allowed ter go runnin' no system commands of their own choosin' inside a step o' the run."

    override val errorDialogInputClosed: String =
        "It do luke like thet terminal input went hully away whilst a dialog was a-waitin\u2019 on yer answer, so I can\u2019t read wot yew picked. Dew yew run arara in a proper interactive terminal, so the dialog can get its answer, that it can."

    override val errorDialogsNotConfigured: String =
        "It do luke like they ain\u2019t no dialog support rigged up fer this here run, so I can\u2019t show the dialog wot a rule were a-askin\u2019 fer. Yew best be providin\u2019 a dialog in the app wot runs arara, or keepin\u2019 away from they rules with interactive dialogs, that yew should."

    override fun errorScriptString(field: String): String =
        "${field} hev ter be a string, bor."

    override fun errorScriptTable(field: String): String =
        "${field} hev ter be a table, bor."

    override fun errorScriptBoolean(field: String): String =
        "${field} hev ter be true or false, bor."

    override fun errorScriptInteger(field: String): String =
        "${field} hev ter be a whole number, bor."

    override fun errorScriptList(field: String): String =
        "${field} hev ter be a list numbered from one with no gaps, bor."

    override fun errorScriptUnknownKey(field: String): String =
        "That key ${field} ent known, bor."

    override fun errorScriptRequired(field: String): String =
        "Yew need ${field}, bor."

    override fun errorScriptNonblank(field: String): String =
        "${field} mustn't be blank, bor."

    override fun errorScriptDuplicate(field: String, value: String): String =
        "${field} hev got '${value}' twice, bor."

    override fun errorScriptDirectory(field: String): String =
        "${field} be a directory, bor."

    override fun errorScriptEmpty(field: String): String =
        "${field} hev ter hold at least one entry, bor."

    override fun errorScriptStringKeys(field: String): String =
        "${field} hev ter use nonblank string keys, bor."

    override fun errorProjectInvalidLua(path: String, detail: String): String =
        "That Lua in ${path} ent right, bor: ${detail}"

    override fun errorProjectDuplicateNames(names: String): String =
        "Project names mustn't repeat, bor: ${names}."

    override fun errorProjectUnknownDependency(project: String, dependency: String): String =
        "Project '${project}' asks for '${dependency}', but that ent a project here, bor."

    override fun errorProjectCycle(cycle: String): String =
        "Them project dependencies go round in a circle, bor: ${cycle}."

    override fun errorProjectFileOwnership(path: String, owner: String, project: String): String =
        "That file '${path}' belongs to both '${owner}' and '${project}', bor."

    override fun errorProjectFiletype(path: String): String =
        "None o' them file types match '${path}', bor."

    override fun errorConfigurationPrepend(value: String): String =
        "That preamble.prepend '${value}' ent right; use 'always' or 'without-directives', bor."

    override fun errorScriptFunction(field: String): String =
        "${field} hev ter be a function, bor."

    override fun errorScriptLiteral(field: String): String =
        "${field} hev ter be a literal value, bor."

    override fun errorScriptAlternatives(field: String, alternatives: String): String =
        "${field} hev ter have at least one o' ${alternatives}, bor."

    override val errorLuaInterpolationFailure: String =
        "That 'options' field hev got a Lua expression or tag what ent right, bor."

    override val errorYamlTagsInLua: String =
        "Them MVEL tags need the YAML engine; Lua uses {{ expression }}, bor."

    override val errorLuaTagsInYaml: String =
        "Them Lua tags need a Lua rule, bor."

    override val errorLuaUnterminatedTag: String =
        "That interpolation tag ent closed, bor."

    override val errorLuaUnterminatedQuote: String =
        "That quoted string in the tag ent closed, bor."

    override val errorLuaUnterminatedLongString: String =
        "That long string in the tag ent closed, bor."

    override val errorLuaMapKeys: String =
        "Them map keys come out the same in Lua, bor."

    override val errorLuaTableKeys: String =
        "Them Lua table keys come out the same as strings, bor."

    override fun errorLuaExactInteger(value: String): String =
        "That whole number ${value} won't fit exactly in Lua, bor."

    override val errorLuaCyclicValues: String =
        "Them tables and collections go round in a circle and won't convert, bor."

    override fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String =
        "Argument #${index} to '${helper}' ent right, bor (${expected} wanted, got ${actual})"

    override fun errorLuaBooleanResult(helper: String, actual: String): String =
        "Result from '${helper}' ent right, bor (boolean wanted, got ${actual})"

    override fun errorLuaHelperNoArgs(helper: String): String =
        "${helper} needs at least one argument, bor."

    override fun errorLoadCouldNotLoadDatabase(name: String): String =
        "I couldn't load that database '${name}', bor. Check the file access and YAML cache content."

    override fun errorSaveCouldNotSaveDatabase(name: String): String =
        "I couldn't save that database '${name}', bor. Check its directory exists and yew can write there."

    override fun errorBasenameNotAFile(path: String): String =
        "Yew're gotta hev a file for the 'basename', not a directory. That '${path}' yew give me ent a file. Do yew need ter do jarbs on a directory, yew myte try the Java API."

    override val errorCalculateHashIoException: String =
        "Thass a rum ole dew, bor, I can't work out th' hash. I ent got no idea why thass gorn wrong. Praps the file was hulled abowt afore or time the hashing operation was doing else maybe I're gotta he'some proper permissions to snout abowt in the file."

    override fun errorCheckBooleanNotValidBoolean(value: String): String =
        "That look like '${value}' ent a proper boolean value but dunt yew git yerself inta a rite ole puckaterry. Do yew jist make sure to use proper boolean values (yes and no, true and false, 1 and 0, and on and off)."

    override fun errorCheckRegexIoException(file: String): String =
        "Cor blast me, but I feel a bit ona tewl. I can't read any ona file '${file}', thass giving me an IO error. I're got no idear but praps the file was hulled abowt afore or time the reading operation was doing else maybe I're gotta he'some proper permissions to snout abowt the file."

    override val errorConfigurationGenericError: String =
        "Thass hully gone wrong, ole partner. I coont unnerstand the configuration file. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, my bewty."

    override val errorConfigurationLoopsInvalidRange: String =
        "Atwin me, yew an' the geartepust that 'loops' key value ent rite. Yew're gotta giv' a number thass bigger than nuffin."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String): String =
        "Dunt yew git yarself inta a rite ole puckaterry, but I can't find yar file '${file}' ${extensions}. Yew're gotta find the file and do yew see that has a proper extension on the end onnit, tergether."

    override fun errorDiscoverFileSafeModeNoCompletion(file: String) =
        "Dunt yew git yarself inta a rite ole puckaterry, but I can't find yar file '${file}' nohow. Safe mode won't go a-guessin' at extensions, so yew're gotta give me the whole name, extension and all."

    override fun errorDiscoverFileUnknownExtension(
        file: String,
        extensions: String
    ) =
        "I coont make aht the file type of '${file}': none o' the file types in this here run carries its extension. The extensions o' this here run is ${extensions}. Yew put the missing extension in the 'file_types' (Lua) / 'filetypes' (YAML) o' the configyrashun."

    override fun errorDiscoverFileUnknownLuaExtension(
        file: String,
        extensions: String
    ) =
        "I coont make aht the file type of '${file}': none o' the file types in this here run carries its extension. The extensions o' this here run is ${extensions}. Put the extension in the 'file_types' (Lua) / 'filetypes' (YAML) o' the configyrashun - or, if'n thet file's a project specifikashun, it wears the '.proj.lua' on the end."

    override val errorEvaluateCompilationFailed: String =
        "Wuh, I ent got no idear wass gorn on but I coont git the hang o'that conditional. Yew're gotta he'sum idear wot this is orl abowt else yew mite be in a rite ole puckaterry but I'll help yew git it done if I can, ole partner."

    override val errorEvaluateNotBooleanValue: String =
        "That ent a yis or no answer for that conditional evaluation but dunt yew git in a puckaterry. Do yew jist git that conditional to giv a boolean value, my bewty."

    override val errorExtractorIoError: String =
        "Wuh, I was trying to get a hold onnem directives when suffin went hully wrong, howsomever I hent got no idear why thass gone wrong. Praps the file was hulled abowt afore or time the hashing operation was doing else maybe I're gotta he'sum proper permissions to read the file."

    override val errorExtractorInterpolationFailure: String =
        "That ole 'options' field what you got have got the MVEL parser into a roight ole puckaterry. Do you tricolate yar options so you ent got narthing on the huh in yar '@{…}' parts."

    override fun errorFiletypeInvalidPattern(pattern: String): String =
        "It do luke like thet pattern \"${pattern}\" yew wanted fer this here file type ent a proper regular expression. This should be a easy fix: make sure yew use a proper expression an' hev another go."

    override fun errorFiletypeNotAFile(path: String): String =
        "Yew gotta hev a file for the 'filetype', not a directory. That '${path}' yew giv me dunt look like a file to me. Do yew need to do jarbs on a directory, yew myte try the Java API"

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String): String =
        "Cor blast me, ole partner, but I ent never heard of '${extension}' as a default extension. Yew're gotta giv me yer know about the match and that ole extension on the end onnit do you want a new file type. I ony know abowt the default extensions: ${extensions}"

    override val errorGetApplicationPathEncodingException: String =
        "Dunt yew git yarself inta a rite ole puckaterry, my bewty, cos there's nuffin I can dew abowt it but I're gotta mobbing abowt th'encoding time I was trying to get the application path."

    override fun errorInterpreterArgumentIsRequired(argument: String): String =
        "That seem like the rule say yew gotta have '${argument}', but I coont find it in the directive parameters. Dew yew add it as a parameter for yar directive and try agin."

    override val errorInterpreterCommandRuntimeError: String =
        "That ent gorn right wi' one onnem commands you giv' me. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, my bewty."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String): String =
        "That ole command \"${command}\" dint go roight. Do you have a peek at the output do you want ter know wass doing."

    override val errorInterpreterUserRequestedHalt: String =
        "arara will stop do you (or yar rule) ask it to."

    override val errorInterpreterExitRuntimeError: String =
        "That ent gorn right wi' th' exit status of onnem commands you giv' me. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, my bewty."

    override val errorInterpreterFlagRuntimeException: String =
        "That ent gorn right wi' th' flag for onnem arguments you giv' me. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, my bewty."

    override fun errorLogFormatUnknown(value: String): String =
        "I don't know that log format '${value}', bor. Use none, text or json."

    override fun errorTerminalWidthInvalid(minimum: Int): String =
        "Terminal width hev ter be at least ${minimum} columns, bor."

    override fun errorConsoleModeUnknown(mode: String): String =
        "I don't know that console mode '${mode}', bor. Use plain or styled."

    override fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String =
        "I can't read that rule format for '${identifier}': '${format}', bor. Use a Lua or YAML rule file."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String): String =
        "Thass a rum ole dew but there ent no '${identifier}' in any onnem rule paths. Praps you ent spelled that right. I coont find '${expected}' in any onner paths: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String): String =
        "Atwin me, yew an' the geartepost I ent got no idear abowt these keys in the directive: ${keys}. Howsomever, dunt you git yarself inta a rite ole puckaterry. Do you jist hull them outta yar map."

    override val errorInterpreterWrongExitClosureReturn: String =
        "Wuh, ole partner, that 'exit' expression ent givin' me a yis or no answer. Thass allus got to dew that even if there ent nuffin else to dew. Howsomever, dunt you git yarself inta a rite ole puckaterry, dew you jist tricolate the return statement and try agin."

    override val errorInterpreterWrongReturnType: String =
        "A rule shud ony retarn a boolean or a command. Narthing else ent supported"

    override val errorInvalidProjectFile: String =
        "I have read a file that was supposed to be a project. Unfortunately, it turned out it was neither a project nor a list of projects. Please double-check that your project file follows the documented format."

    override fun errorLoadCouldNotLoadXml(name: String): String =
        "Cor blast me, ole partner, but that YAML database '${name}' ent half puttin' on its parts but I ent got no idear wass gone wrong. Praps the file was hulled abowt afore or time the reading operation was doing else maybe I're gotta he'sum proper permissions to snout abowt the file. Atwin yew, me an' the geartepost, do yew make sure the YAML is in good kelter."

    override fun errorParserInvalidPreamble(name: String): String =
        "Thass a rum ole dew, ole partner, but I can't find the preamble '${name}'. Do yew make sure that key exist in the configuration file."

    override val errorParseRuleGenericError: String =
        "Thass a rum ole dew, that rule is hully puttin' on its parts. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, tergether."

    override val errorReplicateListMissingFormatArgumentsException: String =
        "Blast, bor, but I coont replicate that duzzy ole list cors you ent giv' me a format argument. I rackon there's a slight more or less parameters than yew giv' me. Dew yew go an' check it and try agin."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String): String =
        "Wuh, that ent right, ole partner. That rule '${identifier}' have gone on the slosh at '${location}'."

    override val errorRunGenericException: String =
        "Thass a rum ole dew, my bewty, but I coont dew the system command but I ent got no idear wass gone wrong. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, tergether."

    override val errorRunInterruptedException: String =
        "Thass a rum ole dew, ole partner, suffin's hully thacked that system command aside the lug and thass croaked."

    override val errorRunInvalidExitValueException: String =
        "That system command is hully puttin' on its parts. That ent a proper exit value."

    override val errorRunIoException: String =
        "I rackon that system command have blundered over the troschel. Are yew sure that exist in yar path? Dew yew check the path, ole partner, to see where thass hidin'. Maybe thass gorn in the backus for some bread an' pullet."

    override val errorRunTimeoutException: String =
        "That slummockun gret system command have run on for too long so I gev it a clout round the lug. If thass hully short for yew, dew yew giv' me a longer timeout value."

    override fun errorSaveCouldNotSaveXml(name: String): String =
        "Thass a rum ole dew, ole partner. I coont save that ole YAML database that oughta be called '${name}'. Howsomever I hent got no idea why thass gone wrong. Praps I're gotta he'sum proper permissions to write the YAML file to disk."

    override fun errorSessionObtainUnknownKey(key: String): String =
        "Cor blast me, my bewty, but I hent got no idear wass that '${key}' key is dewun in that 'get' method. I can't giv' you suffin I ent got. Dew yew ax for suffin I can git yew."

    override fun errorSessionRemoveUnknownKey(key: String): String =
        "Cor blast me, ole partner, but I hent got no idear wass that '${key}' key is dewin in that 'remove' method. I can't remove suffin that ent there. Dew yew ax for suffin else."

    override fun errorValidateEmptyFilesList(lines: String): String =
        "Cor blast me, my bewty. I read a directive ${lines} but there ent nuffin in the provided 'files' list. Do yew just make sure there's at least one element in the list and try agin."

    override fun errorValidateFilesIsNotAList(lines: String): String =
        "That ent rite, ole partner. I read a directive ${lines} and found out that 'files' require a list. Do yew fix the type to a proper list and try agin."

    override fun errorValidateInvalidDirectiveFormat(lines: String): String =
        "That directive ${lines} in the provided file ent rite. But dunt git yarself into a puckaterry. Do yew just fix the directive and try agin."

    override val errorValidateNoDirectivesFound: String =
        "That look like no directives were found in the provided file. Do yew include at least one directive and try agin."

    override fun errorValidateOrphanLinebreak(line: String): String =
        "Thass a rum ole dew, ole partner. There's an orphan directive line break in line ${line}. I can't do nuffin abowt that. Dew yew giv' that directive a bit o' tricolatin' and try agin."

    override fun errorValidateReferenceIsReserved(lines: String): String =
        "That ain't right, my bewty. You can't use the key 'reference' in ${lines}. Thass a reserved key, but there ain't no use you gittin' in a rite ole puckaterry abowt it. Dew yew giv' it another name."

    override fun errorValidateYamlException(lines: String): String =
        "Thass a rum ole dew, my bewty. Suffin's gone wrong in the YAML map in a directive ${lines}. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String): String =
        "That ent rite, ole partner. Yew can't use '${identifier}'. Thass reserved, but dunt yew git yarself into a rite ole puckaterry, dew yew jist giv' it another name."

    override val errorValidateBodyDuplicateArgumentIdentifiers: String =
        "Thass a rum ole dew, ole partner. You've got duplicate argument identifiers in yar rule. Dew you fix that and try agin."

    override val errorValidateBodyMissingKeys: String =
        "That ent rite, ole partner. You gotta use at least 'flag' or 'default' when you define a rule argument scope. Do yew make sure to use at least one onnem."

    override val errorValidateBodyNullArgumentId: String =
        "That ent rite, my bewty. You gotta hev an identifier but one onnem arguments ent for one. Dew yew add a valid identifier to the argument and try agin."

    override val errorValidateBodyNullCommand: String =
        "Thass a rum dew, ole partner. Thass a null command in that ole rule. Do you add a proper command there."

    override val errorValidateHeaderNullName: String =
        "Cor blast me, bor, but that rule ent got no name, but dunt yew git yarself in a puckaterry, dew you jist giv' it a proper name and try agin."

    override fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String =
        "My heart alive, my bewty, but that rule ent got the right identifier. That oughta be '${expected}', not '${actual}', but dunt yew git yarself in a puckaterry. Dew you jist fix that."

    override val infoLabelProject = "Project:"

    override val infoDisplaySummaryTitle = "How it went"

    override val infoDisplaySummaryFile = "Yon file"

    override val infoDisplaySummaryTasks = "Jobs"

    override val infoDisplaySummaryTime = "While"

    override val infoDisplayFullOutputInLog =
        "the hull output’s in the log file, that it be"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "PROPER SUCCESS, THAT IS · ${if (files == 1) "1 file" else "$files files"}, " +
            "${if (tasks == 1) "1 job" else "$tasks jobs"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "THAT WENT WRONG, THAT DID · ${if (files == 1) "1 file" else "$files files"}, " +
            "${if (tasks == 1) "1 job" else "$tasks jobs"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "JIST A PRETEND RUN · ${if (tasks == 1) "1 job" else "$tasks jobs"}"

    override val infoConsoleSkipped: String =
        "Skipped, bor"

    override val infoConsoleUnprocessed: String =
        "Not processed"

    override val infoDisplayExceptionMoreDetails: String =
        "Hear's orl my know on that aggraweartin' exception:"

    override fun infoDisplayExecutionTime(time: String): String =
        "Wuh that took ${time} seconds but if thass a slight longer than you expected, dunt yew go mobbing me abowt it cors that ent my fault. My grandf'ar dint have none of these pearks. He had to use a pen and a bit o' pearper, but thass bin nice mardling wi' yew. Dew yew keep a troshin'!"

    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String =
        "Hold yew hard, ole partner, I'm gornta hev a look at '${name}' (thass ${size} big, that is, and that was last chearnged on ${lastModified} in case yew dunt remember)."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String =
        "Although executing in dry-run mode, this entry might be already processed since it returned a boolean value: ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String): String =
        "This is what I'm abowt to dew: ${command}"

    override val infoLabelAuthor: String =
        "Thass the one wot wrote this masterous jarb:"

    override val infoLabelAuthors: String =
        "Here's the ones wot wrote this masterous jarb:"

    override val infoLabelSystemCommand = "Yer command:"

    override val infoLabelConditional: String =
        "Conditional (thass a yis or no thing):"

    override val infoLabelNoAuthors: String =
        "Wuh thass a rum ole dew. There ent no names here. Yew're gotta guess who wrote this masterous jarb"

    override val infoLabelOnWarning: String =
        "MIND OUT, BOR"

    override val infoLabelOnQuestion: String =
        "AX ME THIS"

    override val infoLabelOnInfo: String =
        "JIST SO YEW KNOW"

    override val infoLabelDialogPrompt: String =
        "Pick one onnem"

    override val infoLabelOnDetails: String =
        "DETAILS"

    override val infoLabelOnError: String =
        "SUFFIN'S GORN WRONG"

    override val infoLabelOnFailure: String =
        "THAT ENT GORN RIGHT, OLE PARTNER"

    override val infoLabelOnSuccess: String =
        "THASS A MASTERLY JOB, MY BEWTY"

    override val infoLabelUnnamedTask: String =
        "That task ent got no name"

    override val infoParserNotes: String =
        "arara is released under the New BSD license."

    override val logInfoBeginBuffer: String =
        "BEGIN OUTPUT BUFFER"

    override fun logInfoBooleanMode(value: Boolean): String =
        "Boolean value: ${value}"

    override val logInfoDirectivesBlock: String =
        "DIRECTIVES"

    override val logInfoEndBuffer: String =
        "END OUTPUT BUFFER"

    override fun logInfoInterpretRule(rule: String): String =
        "I'm orl ready, ole parter, to dew the rule '${rule}'."

    override fun logInfoInterpretTask(task: String, rule: String): String =
        "I'm orl ready, my bewty, to dew '${task}' from that ole rule '${rule}'."

    override fun logInfoPotentialPatternFound(line: String, pattern: String): String =
        "I're found what might be a pattern in line ${line}: ${pattern}"

    override fun logInfoRuleLocation(path: String): String =
        "That ole rule come from: '${path}'"

    override fun logInfoSystemCommand(command: String): String =
        "System command: ${command}"

    override val logInfoTaskResult: String =
        "Here's what that jarb say:"

    override val logInfoValidatedDirectives: String =
        "Thass a masterous jarb, orl the directives are in good kelter. We can git troshin'."

    override fun logInfoWelcomeMessage(version: String): String =
        "Hello, my bewty. Welcome to arara ${version}! (Thass one onnem hully big birds they git in Brazil.)"
}
