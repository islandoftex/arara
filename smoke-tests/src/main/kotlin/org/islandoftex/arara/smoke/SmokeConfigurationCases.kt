// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath

internal fun SmokeContext.configurationCases(): Map<String, (MPPPath) -> Unit> =
    LocalConfigurationChecks(this).cases +
        ProjectConfigurationChecks(this).cases

private class LocalConfigurationChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "lua_configuration_precedes_yaml_independently_of_rule_engine" to
            ::luaConfigurationPrecedesYamlIndependentlyOfRuleEngine,
        "help_and_empty_invocation_ignore_invalid_configuration" to ::helpAndEmptyInvocationIgnoreInvalidConfiguration,
        "call_properties_override_property_files_in_both_engines" to ::callPropertiesOverridePropertyFilesInBothEngines,
        "lua_configuration_defines_custom_file_types" to ::luaConfigurationDefinesCustomFileTypes,
        "verbosity_flags_override_yaml_and_lua_configuration" to ::verbosityFlagsOverrideYamlAndLuaConfiguration,
        "lua_configuration_supports_both_rule_engines" to ::luaConfigurationSupportsBothRuleEngines,
        "invalid_lua_configuration_is_an_application_error" to ::invalidLuaConfigurationIsAnApplicationError,
        "database_changes_persist_between_processes" to ::databaseChangesPersistBetweenProcesses,
        "configured_database_rejects_malformed_yaml_without_overwriting" to
            ::configuredDatabaseRejectsMalformedYamlWithoutOverwriting,
    )

    private fun luaConfigurationPrecedesYamlIndependentlyOfRuleEngine(
        root: MPPPath
    ) {

        root.resolve("arararc.yaml").writeText("!config\nloops: invalid\n")
        root.resolve("arararc.lua").writeText("return { paths = { cwd } }\n")
        rule(root, "return true;")
        luaRule(root, "return true")
        for (engine in listOf("lua", "yaml")) execute(root, 0, listOf("--preferred-engine", engine))
    }

    private fun helpAndEmptyInvocationIgnoreInvalidConfiguration(
        root: MPPPath
    ) {

        root.resolve("arararc.yaml").writeText("!config\nloops: not-a-number\n")
        for (flags in listOf(emptyList(), listOf("--help"))) {
            val result = run((launcher + listOf("-Duser.home=$root", "-jar", jar.toString())) + flags, root)
            check(result.status == 0) { result.output }
            contains(result.output, "--preferred-engine")
            contains(result.output, "--whole-file")
            check(!root.resolve("arara.log").exists)
        }
    }

    private fun callPropertiesOverridePropertyFilesInBothEngines(
        root: MPPPath
    ) {

        root.resolve("arguments.properties").writeText("marker=from-file\n")
        listOf("lua", "yaml").forEach { engine ->
            if (engine == "lua") {
                luaRule(root, "return get_session().get('arg:marker') == 'from-flag'")
            } else {
                rule(root, "return getSession().get('arg:marker').equals('from-flag');")
            }
            execute(root, 0, listOf("--preferred-engine", engine, "--call-property", "marker=from-flag",
                "--properties-file", "arguments.properties"))
        }
    }

    private fun luaConfigurationDefinesCustomFileTypes(
        root: MPPPath
    ) {

        root.resolve("arararc.lua").writeText(
            "return { paths = { cwd }, file_types = { { extension = 'src', pattern = '^@ +' } } }\n"
        )
        root.resolve("custom.src").writeText("@ arara: probe\n")
        rule(root, "return true;")
        contains(execute(root, 0, reference = "custom.src"), "SUCCESS")
    }

    private fun verbosityFlagsOverrideYamlAndLuaConfiguration(
        root: MPPPath
    ) {

        rule(root, native("0"))
        listOf("lua", "yaml").forEach { format ->
            val config = root.resolve("arararc.$format")
            listOf(false, true).forEach { configured ->
                config.writeText(if (format == "yaml") "!config\npaths: ['.']\nverbose: $configured\n"
                else "return { paths = { cwd }, verbose = $configured }\n")
                val cases = listOf(
                    emptyList<String>() to configured,
                    listOf("-v") to true,
                    listOf("--verbose") to true,
                    listOf("-s") to false,
                    listOf("--silent") to false,
                    listOf("-v", "-s") to false,
                    listOf("-s", "-v") to false
                )
                cases.forEach { (flags, expected) ->
                    val output = execute(root, 0, flags)
                    assertToolStreams(output, expected, "$format verbose=$configured flags=$flags")
                }
            }
            Files.delete(config.toJVMPath())
        }
    }

    private fun assertToolStreams(output: String, expected: Boolean, description: String) {
        listOf("SMOKE_STDOUT", "SMOKE_STDERR").forEach { marker ->
            check((marker in output) == expected) { "$description: unexpected tool output\n$output" }
        }
    }

    private fun luaConfigurationSupportsBothRuleEngines(
        root: MPPPath
    ) {

        root.resolve("arararc.lua").writeText("""
            return { paths = { cwd }, preamble = { default = 'build',
                definitions = { build = { '% arara: probe' } } } }
        """.trimIndent())
        root.resolve("run.tex").writeText("plain input\n")
        rule(root, "return true;")
        luaRule(root, "return true")
        contains(execute(root, 0), "Smoke probe")
        contains(execute(root, 0, listOf("--preferred-engine", "lua")), "Lua probe")
    }

    private fun invalidLuaConfigurationIsAnApplicationError(
        root: MPPPath
    ) {

        root.resolve("arararc.lua").writeText("return { loops = 0 }\n")
        rule(root, "return true;")
        contains(execute(root, APPLICATION_ERROR), "arararc.lua")
    }

    private fun databaseChangesPersistBetweenProcesses(
        root: MPPPath
    ) {

        val existing = "!database\nmap: {}\n"
        root.resolve("arara.yaml").writeText(existing)
        luaRule(root, "if not changed(current_file()) then halt($FAILURE_STATUS) end; return true")
        val flags = listOf("--preferred-engine", "lua")
        execute(root, 0, flags)
        check(root.resolve("arara.yaml").isRegularFile) { "Missing default arara.yaml cache" }
        execute(root, FAILURE_STATUS, flags)
        root.resolve("run.tex").writeText("\n% updated source\n", append = true)
        execute(root, 0, flags)
    }

    private fun configuredDatabaseRejectsMalformedYamlWithoutOverwriting(
        root: MPPPath
    ) {

        root.resolve("cache").createDirectories()
        root.resolve("arararc.lua").writeText(
            "return { paths = { cwd }, database = 'cache/custom.yaml' }\n"
        )
        luaRule(root, "changed(current_file()); return true")
        val flags = listOf("--preferred-engine", "lua")
        execute(root, 0, flags)
        val cache = root.resolve("cache/custom.yaml")
        check(cache.isRegularFile) { "Configured database path was not used" }
        check(!root.resolve("custom.yaml").exists) { "Database parent directory was discarded" }
        val malformed = "!database\nmap: [unterminated\n"
        cache.writeText(malformed)
        val output = execute(root, APPLICATION_ERROR, flags).replace(Regex("\\s+"), " ")
        contains(output, "YAML cache integrity")
        check(cache.readText() == malformed) { "Malformed cache was overwritten" }
    }

}

private class ProjectConfigurationChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "project_build_directory_staging_native_cwd_and_clean" to ::projectBuildDirectoryStagingNativeCwdAndClean,
        "localized_configuration_and_project_errors" to ::localizedConfigurationAndProjectErrors,
        "malformed_project_is_an_application_error" to ::malformedProjectIsAnApplicationError,
        "mixed_projects_and_loose_inputs_run_with_separate_configuration" to
            ::mixedProjectsAndLooseInputsRunWithSeparateConfiguration,
        "verbosity_overrides_apply_to_each_project_without_leaking_configuration" to
            ::verbosityOverridesApplyToEachProjectWithoutLeakingConfiguration,
        "shared_project_input_is_rejected_before_execution" to ::sharedProjectInputIsRejectedBeforeExecution,
        "project_and_lua_configuration_share_script_context" to ::projectAndLuaConfigurationShareScriptContext,
        "project_loads_configuration_from_its_working_directory" to ::projectLoadsConfigurationFromItsWorkingDirectory,
    )

    private fun projectBuildDirectoryStagingNativeCwdAndClean(
        root: MPPPath
    ) {

        root.resolve("project.proj.lua").writeText(
            "return {files={['run.tex']={}},build={directory='build/document'}}")
        root.resolve(".gitignore").writeText("events.jsonl\n")
        root.resolve("arararc.lua").writeText(
            "return {paths={cwd},log={format='json',file=cwd .. '/events.jsonl'}}")
        luaRule(root, luaNative("0"))
        val output = execute(root, 0, listOf("--preferred-engine", "lua", "--verbose"),
            reference = "project.proj.lua")
        contains(output, "Validating build directory")
        contains(output, "Preparing build directory")
        contains(output, "Build directory ready: copied")
        contains(output, "SMOKE_CWD=${root.resolve("build/document")}")
        check(root.resolve("build/document/run.tex").exists)
        check(!root.resolve("build/document/build/document").exists)
        execute(root, 0, listOf("--preferred-engine", "lua"), reference = "project.proj.lua")
        val events = root.resolve("events.jsonl").readLines().map { Json.parseToJsonElement(it).jsonObject }
        val prepared = events.single { it["event"]?.jsonPrimitive?.content == "build_directory.completed" &&
            it.getValue("data").jsonObject["operation"]?.jsonPrimitive?.content == "prepare" }
            .getValue("data").jsonObject
        check(prepared.getValue("copiedFiles").jsonPrimitive.int == 0)
        check(prepared.getValue("durationMs").jsonPrimitive.long >= 0)
        val cleaned = execute(root, 0, listOf("--clean-build"), reference = "project.proj.lua")
        contains(cleaned, "Cleaning build directory")
        contains(cleaned, "Build directory cleaned")
        check(!root.resolve("build/document").exists)
        check(root.resolve("run.tex").exists)
    }

    private fun localizedConfigurationAndProjectErrors(
        root: MPPPath
    ) {

        root.resolve("arararc.lua").writeText("return { verbose = 'true' }\n")
        contains(execute(root, APPLICATION_ERROR, listOf("--language", "de")),
            "verbose muss ein boolescher Wert sein.")
        Files.delete(root.resolve("arararc.lua").toJVMPath())
        root.resolve("invalid.proj.lua").writeText("return { files = false }\n")
        contains(execute(root, APPLICATION_ERROR, listOf("--language", "de"), "invalid.proj.lua"),
            "project.files muss eine Tabelle sein.")
    }

    private fun malformedProjectIsAnApplicationError(
        root: MPPPath
    ) {

        root.resolve("invalid.proj.lua").writeText("return { files = false }\n")
        contains(execute(root, APPLICATION_ERROR, reference = "invalid.proj.lua"), "project.files")
    }

    private fun mixedProjectsAndLooseInputsRunWithSeparateConfiguration(
        root: MPPPath
    ) {

        val project = root.resolve("named").apply { createDirectories() }
        initialize(project)
        rule(project, "getSession().put('named', true); return true;")
        rule(root, "getSession().get('named'); return true;")
        root.resolve("named.proj.lua").writeText(
            "return { working_directory = 'named', files = { ['run.tex'] = {} } }\n"
        )
        execute(root, 0, flags = listOf("named.proj.lua"))
    }

    private fun verbosityOverridesApplyToEachProjectWithoutLeakingConfiguration(
        root: MPPPath
    ) {

        val project = root.resolve("verbose project").apply { createDirectories() }
        initialize(project)
        project.resolve("arararc.lua").writeText("return { paths = { cwd }, verbose = true }\n")
        root.resolve("mixed.proj.lua").writeText(
            "return { working_directory = 'verbose project', files = { ['run.tex'] = {} } }\n"
        )
        rule(root, native("0"))
        rule(project, native("0"))
        luaRule(root, luaNative("0"))
        luaRule(project, luaNative("0"))
        listOf("lua", "yaml").forEach { engine ->
            listOf(
                emptyList<String>() to 1,
                listOf("--silent") to 0,
                listOf("--verbose") to 2,
                listOf("--silent", "--verbose") to 0
            ).forEach { (flags, expected) ->
                val output = execute(root, 0, listOf("--preferred-engine", engine) + flags + "mixed.proj.lua")
                val directories = output.lineSequence().filter { it.startsWith("SMOKE_CWD=") }.toSet()
                val expectedDirectories = when (expected) {
                    0 -> emptySet()
                    1 -> setOf("SMOKE_CWD=$project")
                    else -> setOf("SMOKE_CWD=$project", "SMOKE_CWD=$root")
                }
                check(directories == expectedDirectories) {
                    "$engine flags=$flags: unexpected verbose project directories $directories\n$output"
                }
                listOf("SMOKE_STDOUT", "SMOKE_STDERR").forEach { marker ->
                    val count = Regex(marker).findAll(output).count()
                    check(count == expected) {
                        "$engine flags=$flags: expected $expected tool output records, got $count\n$output"
                    }
                }
            }
        }
    }

    private fun sharedProjectInputIsRejectedBeforeExecution(
        root: MPPPath
    ) {

        rule(root, "halt($FAILURE_STATUS);")
        root.resolve("named.proj.lua").writeText("return { files = { ['run.tex'] = {} } }\n")
        val output = execute(root, APPLICATION_ERROR, flags = listOf("named.proj.lua"))
        contains(output.replace(Regex("\\s+"), " "), "belongs to both")
    }

    private fun projectAndLuaConfigurationShareScriptContext(
        root: MPPPath
    ) {

        val project = root.resolve("sources").apply { createDirectories() }
        rule(project, "return true;")
        project.resolve("run.tex").writeText("% arara: probe\n")
        project.resolve("arararc.lua").writeText("""
            assert(cwd ~= user.home and cwd:sub(-7) == 'sources')
            return { paths = { cwd } }
        """.trimIndent())
        root.resolve("context.proj.lua").writeText("""
            assert(cwd == user.home)
            return { name = user.name, working_directory = cwd .. '/sources',
                files = { ['run.tex'] = {} } }
        """.trimIndent())
        contains(execute(root, 0, reference = "context.proj.lua"), "Smoke probe")
    }

    private fun projectLoadsConfigurationFromItsWorkingDirectory(
        root: MPPPath
    ) {

        val project = root.resolve("project with spaces").apply { createDirectories() }
        initialize(project)
        rule(project, "return true;")
        root.resolve("project.proj.lua").writeText(
            "return { name = 'Smoke', working_directory = ${quoted(project.toString().replace('\\', '/'))}, " +
                "files = { ['run.tex'] = { file_type = { extension = 'tex' } } } }\n"
        )
        execute(root, 0, reference = "project.proj.lua")
    }

}
