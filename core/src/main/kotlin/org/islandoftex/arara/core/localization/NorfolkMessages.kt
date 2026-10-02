// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Norfolk (en-QN) localization of the arara messages.
 * Translated by Nicola Talbot and Keith Skipper.
 */
@Suppress("MaxLineLength", "TooManyFunctions")
object NorfolkMessages : AraraMessages {
    override val providedLocale = MPPLocale("en-QN")

    override fun errorBasenameNotAFile(path: String) =
        "Yew're gotta hev a file for the 'basename', not a directory. That '${path}' yew give me ent a file. Do yew need ter do jarbs on a directory, yew myte try the Java API."

    override val errorCalculateHashIoException =
        "Thass a rum ole dew, bor, I can't work out th' hash. I ent got no idea why thass gorn wrong. Praps the file was hulled abowt afore or time the hashing operation was doing else maybe I're gotta he'some proper permissions to snout abowt in the file."

    override fun errorCheckBooleanNotValidBoolean(value: String) =
        "That look like '${value}' ent a proper boolean value but dunt yew git yerself inta a rite ole puckaterry. Do yew jist make sure to use proper boolean values (yes and no, true and false, 1 and 0, and on and off)."

    override fun errorCheckRegexIoException(file: String) =
        "Cor blast me, but I feel a bit ona tewl. I can't read any ona file '${file}', thass giving me an IO error. I're got no idear but praps the file was hulled abowt afore or time the reading operation was doing else maybe I're gotta he'some proper permissions to snout abowt the file."

    override val errorConfigurationGenericError =
        "Thass hully gone wrong, ole partner. I coont unnerstand the configuration file. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, my bewty."

    override val errorConfigurationLoopsInvalidRange =
        "Atwin me, yew an' the geartepust that 'loops' key value ent rite. Yew're gotta giv' a number thass bigger than nuffin."

    override fun errorDiscoverFileFileNotFound(file: String, details: String) =
        "Dunt yew git yarself inta a rite ole puckaterry, but I can't find yar file '${file}' ${details}. Yew're gotta find the file and do yew see that has a proper extension on the end onnit, tergether."

    override val errorEvaluateCompilationFailed =
        "Wuh, I ent got no idear wass gorn on but I coont git the hang o'that conditional. Yew're gotta he'sum idear wot this is orl abowt else yew mite be in a rite ole puckaterry but I'll help yew git it done if I can, ole partner."

    override val errorEvaluateNotBooleanValue =
        "That ent a yis or no answer for that conditional evaluation but dunt yew git in a puckaterry. Do yew jist git that conditional to giv a boolean value, my bewty."

    override val errorExtractorIoError =
        "Wuh, I was trying to get a hold onnem directives when suffin went hully wrong, howsomever I hent got no idear why thass gone wrong. Praps the file was hulled abowt afore or time the hashing operation was doing else maybe I're gotta he'sum proper permissions to read the file."

    override val errorExtractorInterpolationFailure =
        "That ole 'options' field what you got have got the MVEL parser into a roight ole puckaterry. Do you tricolate yar options so you ent got narthing on the huh in yar '@{…}' parts."

    override val errorExtractorOrbTagInLuaEngine =
        "That ole 'options' field of yorn hev got an orb tag from the ole YAML engine in it, an' the Lua engine ent able to interperlate thet. Best you rewrite thet entry with '{{ … }}' tags instead."

    override fun errorFiletypeNotAFile(path: String) =
        "Yew gotta hev a file for the 'filetype', not a directory. That '${path}' yew giv me dunt look like a file to me. Do yew need to do jarbs on a directory, yew myte try the Java API"

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String) =
        "Cor blast me, ole partner, but I ent never heard of '${extension}' as a default extension. Yew're gotta giv me yer know about the match and that ole extension on the end onnit do you want a new file type. I ony know abowt the default extensions: ${extensions}"

    override val errorGetApplicationPathEncodingException =
        "Dunt yew git yarself inta a rite ole puckaterry, my bewty, cos there's nuffin I can dew abowt it but I're gotta mobbing abowt th'encoding time I was trying to get the application path."

    override fun errorInterpreterArgumentIsRequired(argument: String) =
        "That seem like the rule say yew gotta have '${argument}', but I coont find it in the directive parameters. Dew yew add it as a parameter for yar directive and try agin."

    override val errorInterpreterCommandRuntimeError =
        "That ent gorn right wi' one onnem commands you giv' me. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, my bewty."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String) =
        "That ole command \"${command}\" dint go roight. Do you have a peek at the output do you want ter know wass doing."

    override val errorInterpreterExitRuntimeError =
        "That ent gorn right wi' th' exit status of onnem commands you giv' me. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, my bewty."

    override val errorInterpreterFlagRuntimeException =
        "That ent gorn right wi' th' flag for onnem arguments you giv' me. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, my bewty."

    override fun errorInterpreterHaltStatusNotANumber(key: String) =
        "It do luke like thet value yew put under the halt session key \"${key}\" ent a proper number, so I can't tell yew wot halt status yew wanted. This should be a easy fix: make sure thet halt status is a number an' hev another go."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String) =
        "Thass a rum ole dew but there ent no '${identifier}' in any onnem rule paths. Praps you ent spelled that right. I coont find '${expected}' in any onner paths: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String) =
        "Atwin me, yew an' the geartepost I ent got no idear abowt these keys in the directive: ${keys}. Howsomever, dunt you git yarself inta a rite ole puckaterry. Do you jist hull them outta yar map."

    override fun errorConsoleModeUnknown(mode: String) =
        "Atwin me, the console '${mode}' ent one of the presentations wot's on offer, that it ent. Dunt you git yarself inta a rite ole puckaterry: yew jist picks one of the modes wot's available."

    override val errorInterpreterUserRequestedHalt =
        "arara will stop do you (or yar rule) ask it to."

    override val errorInterpreterWrongExitClosureReturn =
        "Wuh, ole partner, that 'exit' expression ent givin' me a yis or no answer. Thass allus got to dew that even if there ent nuffin else to dew. Howsomever, dunt you git yarself inta a rite ole puckaterry, dew you jist tricolate the return statement and try agin."

    override val errorInterpreterWrongReturnType =
        "A rule shud ony retarn a boolean or a command. Narthing else ent supported"

    override val errorInvalidProjectFile =
        "I've read a file that were supposed ter be a project. Unluckily, it turned aht it weren't no project, nor a list of projects neither. Do please double-check that your project file folla the documented format."

    override fun errorLoadCouldNotLoadDatabase(name: String) =
        "Cor blast me, ole partner, but that YAML database '${name}' ent half puttin' on its parts but I ent got no idear wass gone wrong. Praps the file was hulled abowt afore or time the reading operation was doing else maybe I're gotta he'sum proper permissions to snout abowt the file. Atwin yew, me an' the geartepost, do yew make sure the YAML is in good kelter."

    override fun errorParserInvalidPreamble(name: String) =
        "Thass a rum ole dew, ole partner, but I can't find the preamble '${name}'. Do yew make sure that key exist in the configuration file."

    override val errorParseRuleGenericError =
        "Thass a rum ole dew, that rule is hully puttin' on its parts. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, tergether."

    override fun errorParseRuleUnsupportedFormat(rule: String, location: String, format: String) =
        "I've found a error in the rule \"${rule}\" located at \"${location}\". The rule format of \"${format}\" ent supported by arara, I'm afraid."

    override fun errorProjectCycle(cycle: String) =
        "Them project dependencies hev got a cycle in 'em, a-involvun' ${cycle}. Cycle dependencies can't be sorted aht, no how. Do please hev another look at your project specification."

    override val errorReplicateListMissingFormatArgumentsException =
        "Blast, bor, but I coont replicate that duzzy ole list cors you ent giv' me a format argument. I rackon there's a slight more or less parameters than yew giv' me. Dew yew go an' check it and try agin."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String) =
        "Wuh, that ent right, ole partner. That rule '${identifier}' have gone on the slosh at '${location}'."

    override val errorRunGenericException =
        "Thass a rum ole dew, my bewty, but I coont dew the system command but I ent got no idear wass gone wrong. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry but I'll help yew git it done if I can, tergether."

    override val errorRunInterruptedException =
        "Thass a rum ole dew, ole partner, suffin's hully thacked that system command aside the lug and thass croaked."

    override val errorRunInvalidExitValueException =
        "That system command is hully puttin' on its parts. That ent a proper exit value."

    override val errorRunIoException =
        "I rackon that system command have blundered over the troschel. Are yew sure that exist in yar path? Dew yew check the path, ole partner, to see where thass hidin'. Maybe thass gorn in the backus for some bread an' pullet."

    override val errorRunTimeoutException =
        "That slummockun gret system command have run on for too long so I gev it a clout round the lug. If thass hully short for yew, dew yew giv' me a longer timeout value."

    override fun errorSaveCouldNotSaveDatabase(name: String) =
        "Thass a rum ole dew, ole partner. I coont save that ole YAML database that oughta be called '${name}'. Howsomever I hent got no idea why thass gone wrong. Praps I're gotta he'sum proper permissions to write the YAML file to disk."

    override fun errorSessionObtainUnknownKey(key: String) =
        "Cor blast me, my bewty, but I hent got no idear wass that '${key}' key is dewun in that 'get' method. I can't giv' you suffin I ent got. Dew yew ax for suffin I can git yew."

    override fun errorSessionRemoveUnknownKey(key: String) =
        "Cor blast me, ole partner, but I hent got no idear wass that '${key}' key is dewin in that 'remove' method. I can't remove suffin that ent there. Dew yew ax for suffin else."

    override fun errorValidateEmptyFilesList(lines: String) =
        "Cor blast me, my bewty. I read a directive ${lines} but there ent nuffin in the provided 'files' list. Do yew just make sure there's at least one element in the list and try agin."

    override fun errorValidateFilesIsNotAList(lines: String) =
        "That ent rite, ole partner. I read a directive ${lines} and found out that 'files' require a list. Do yew fix the type to a proper list and try agin."

    override fun errorValidateInvalidDirectiveFormat(lines: String) =
        "That directive ${lines} in the provided file ent rite. But dunt git yarself into a puckaterry. Do yew just fix the directive and try agin."

    override val errorValidateNoDirectivesFound =
        "That look like no directives were found in the provided file. Do yew include at least one directive and try agin."

    override fun errorValidateOrphanLinebreak(line: Int) =
        "Thass a rum ole dew, ole partner. There's an orphan directive line break in line ${line}. I can't do nuffin abowt that. Dew yew giv' that directive a bit o' tricolatin' and try agin."

    override fun errorValidateReferenceIsReserved(lines: String) =
        "That ain't right, my bewty. You can't use the key 'reference' in ${lines}. Thass a reserved key, but there ain't no use you gittin' in a rite ole puckaterry abowt it. Dew yew giv' it another name."

    override fun errorValidateYamlException(lines: String) =
        "Thass a rum ole dew, my bewty. Suffin's gone wrong in the YAML map in a directive ${lines}. Wuh, yew're gotta he'sum idear wot this is orl abowt ter git th' hang o'this bit else yew myte be in a rite ole puckaterry."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String) =
        "That ent rite, ole partner. Yew can't use '${identifier}'. Thass reserved, but dunt yew git yarself into a rite ole puckaterry, dew yew jist giv' it another name."

    override val errorValidateBodyDuplicateArgumentIdentifiers =
        "Thass a rum ole dew, ole partner. You've got duplicate argument identifiers in yar rule. Dew you fix that and try agin."

    override val errorValidateBodyMissingKeys =
        "That ent rite, ole partner. You gotta use at least 'flag' or 'default' when you define a rule argument scope. Do yew make sure to use at least one onnem."

    override val errorValidateBodyNullArgumentId =
        "That ent rite, my bewty. You gotta hev an identifier but one onnem arguments ent for one. Dew yew add a valid identifier to the argument and try agin."

    override val errorValidateBodyNullCommand =
        "Thass a rum dew, ole partner. Thass a null command in that ole rule. Do you add a proper command there."

    override val errorValidateHeaderNullName =
        "Cor blast me, bor, but that rule ent got no name, but dunt yew git yarself in a puckaterry, dew you jist giv' it a proper name and try agin."

    override fun errorValidateHeaderWrongIdentifier(expected: String, found: String) =
        "My heart alive, my bewty, but that rule ent got the right identifier. That oughta be '${expected}', not '${found}', but dunt yew git yarself in a puckaterry. Dew you jist fix that."

    override val infoDisplayExceptionMoreDetails =
        "Hear's orl my know on that aggraweartin' exception:"

    override fun infoDisplayExecutionTime(time: String) =
        "Wuh that took ${time} seconds but if thass a slight longer than you expected, dunt yew go mobbing me abowt it cors that ent my fault. My grandf'ar dint have none of these pearks. He had to use a pen and a bit o' pearper, but thass bin nice mardling wi' yew. Dew yew keep a troshin'!"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "PROPER SUCCESS, THAT IS · ${if (files == 1) "1 file" else "$files files"}, " +
            "${if (tasks == 1) "1 job" else "$tasks jobs"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "THAT WENT WRONG, THAT DID · ${if (files == 1) "1 file" else "$files files"}, " +
            "${if (tasks == 1) "1 job" else "$tasks jobs"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "JIST A PRETEND RUN · ${if (tasks == 1) "1 job" else "$tasks jobs"}"
    override fun infoDisplaySkippedConditional(name: String) =
        "${name} skipped, that were · the condition said no, it did"

    override val infoDisplayFullOutputInLog =
        "the hull output’s in the log file, that it be"


    override val infoDisplaySummaryTitle = "How it went"
    override val infoDisplaySummaryFile = "Yon file"
    override val infoDisplaySummaryTasks = "Jobs"
    override val infoDisplaySummaryTime = "While"

        override fun infoDisplayUnprocessedFiles(count: Int) =
        "Wot didn\u2019t get a go: "


    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String) =
        "Hold yew hard, ole partner, I'm gornta hev a look at '${name}' (thass ${size} big, that is, and that was last chearnged on ${lastModified} in case yew dunt remember)."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean) =
        "Although executing in dry-run mode, this entry might be already processed since it returned a boolean value: ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String) =
        "This is what I'm abowt to dew: ${command}"

    override val infoInterpreterLuaFileNotAProject =
        "It do luke like thet there Lua file yew giv' me ent a proper project specifikashun, so I'll tread it as a ordinary input file. If it wuz meant ter be a project specifikashun, hev a gander in the log fer the details on wot went wrong."

    override val infoLabelAuthor =
        "Thass the one wot wrote this masterous jarb:"

    override val infoLabelAuthors =
        "Here's the ones wot wrote this masterous jarb:"

    override val infoLabelConditional =
        "Conditional (thass a yis or no thing):"

    override val infoLabelNoAuthors =
        "Wuh thass a rum ole dew. There ent no names here. Yew're gotta guess who wrote this masterous jarb"

    override val infoLabelOnDetails =
        "Details"

    override val infoLabelOnError =
        "SUFFIN'S GORN WRONG"

    override val infoLabelOnFailure =
        "THAT ENT GORN RIGHT, OLE PARTNER"

    override val infoLabelOnSuccess =
        "THASS A MASTERLY JOB, MY BEWTY"
    override val infoLabelOnWarning = "MIND OUT, BOR"
    override val infoLabelOnQuestion = "AX ME THIS"
    override val infoLabelOnInfo = "JIST SO YEW KNOW"
    override val infoLabelDialogPrompt = "Pick one onnem"
    override val infoLabelAboutToRun =
        "About to go a-runnin’:"
    override val infoLabelSystemCommand = "Yer command:"

    override val errorDialogInputClosed =
        "It do luke like thet terminal input went hully away whilst a dialog was a-waitin\u2019 on yer answer, so I can\u2019t read wot yew picked. Dew yew run arara in a proper interactive terminal, so the dialog can get its answer, that it can."
    override val errorDialogsNotConfigured =
        "It do luke like they ain\u2019t no dialog support rigged up fer this here run, so I can\u2019t show the dialog wot a rule were a-askin\u2019 fer. Yew best be providin\u2019 a dialog in the app wot runs arara, or keepin\u2019 away from they rules with interactive dialogs, that yew should."
    override val infoLabelUnnamedTask =
        "That task ent got no name"

    override val infoParserNotes =
        "arara is released under the New BSD license."
}
