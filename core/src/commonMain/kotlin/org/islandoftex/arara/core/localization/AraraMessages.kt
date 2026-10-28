// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Complete message catalog composed from focused, typed message domains. */
interface AraraMessages :
    WatchMessages,
    WorkspaceMessages,
    ScriptStructureMessages,
    TaskMessages,
    CatalogMetadata,
    ProjectMessages,
    FileLookupMessages,
    ConfigurationMessages,
    ScriptTypeMessages,
    LuaMessages,
    FileDataMessages,
    InterpreterMessages,
    LoggingMessages,
    TerminalMessages,
    RuleValidationMessages,
    ExecutionMessages,
    DirectiveMessages,
    ReportingMessages,
    LogEventMessages
