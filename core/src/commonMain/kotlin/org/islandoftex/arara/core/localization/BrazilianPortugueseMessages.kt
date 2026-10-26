// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/** Brazilian Portuguese message catalog. */
@Suppress("MaxLineLength", "TooManyFunctions")
object BrazilianPortugueseMessages : AraraMessages {

    override fun infoBuildDirectoryValidating(path: String): String =
        "Validando o diretório de compilação: $path"

    override fun infoBuildDirectoryPreparing(path: String): String =
        "Preparando o diretório de compilação: $path"

    override fun infoBuildDirectoryCleaning(path: String): String =
        "Limpando o diretório de compilação: $path"

    override fun infoBuildDirectoryPrepared(files: Int, seconds: String): String =
        "Diretório de compilação pronto: $files arquivos de entrada copiados em $seconds s."

    override val infoBuildDirectoryValidated: String =
        "Diretório de compilação validado."

    override val infoBuildDirectoryCleaned: String =
        "Diretório de compilação limpo."

    override val errorBuildDirectoryOperationFailed: String =
        "A operação no diretório de compilação falhou."


    override fun errorScriptChoice(field: String, choices: String): String =
        "Valor não suportado para $field; esperado: $choices."

    override fun errorBuildDirectoryInvalid(path: String): String =
        "Diretório de compilação inválido ou sobreposto: $path."

    override fun errorBuildInputInvalid(path: String): String =
        "Entrada ou destino fora do mapeamento do diretório de compilação: $path."

    override fun errorBuildWorkspaceUnmanaged(path: String): String =
        "O diretório de compilação não é gerenciado por este projeto: $path."

    override fun errorBuildInputCollision(path: String): String =
        "Entrada de compilação conflita com outra entrada ou saída existente: $path."

    override fun errorBuildLinkCycle(path: String): String =
        "Ciclo de links simbólicos nas entradas de compilação: $path."

    override fun errorBuildInputMissing(path: String): String =
        "A entrada de compilação não existe ou não é um arquivo regular ou diretório: $path."

    override val warningDeprecatedYaml: String =
        "Aviso de compatibilidade: você utilizou regras YAML obsoletas que serão removidas na v9."

    override val providedLocale: MPPLocale = MPPLocale("pt-BR")

    override val translators: List<String> = listOf("Paulo Roberto Massa Cereda")

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "Encontrei um erro ao extrair as diretivas de '${file}': ${details}."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "O padrão glob \"${pattern}\" não pôde ser convertido em uma expressão regular: ${details}. Certifique-se de que o padrão esteja bem formado e tente novamente."

    override val errorSafeModeClassLoading =
        "No modo seguro, as regras não têm permissão para carregar classes arbitrárias de arquivos fornecidos pelo usuário."

    override val errorSafeModeSystemCommand =
        "No modo seguro, as regras não têm permissão para executar comandos de sistema arbitrários dentro de uma etapa de execução."

    override val errorDialogInputClosed: String =
        "Parece que a entrada do terminal foi encerrada enquanto um di\u00e1logo aguardava uma resposta; portanto, n\u00e3o consigo ler sua escolha. Execute o arara em um terminal interativo para que o di\u00e1logo possa ser respondido."

    override val errorDialogsNotConfigured: String =
        "Parece que esta execu\u00e7\u00e3o n\u00e3o tem suporte a di\u00e1logos configurado; portanto, n\u00e3o posso exibir o di\u00e1logo que uma regra pediu. Forne\u00e7a uma implementa\u00e7\u00e3o de di\u00e1logos na aplica\u00e7\u00e3o que executa o arara, ou evite regras com di\u00e1logos interativos."

    override fun errorScriptString(field: String): String =
        "${field} requer um valor de texto."

    override fun errorScriptTable(field: String): String =
        "${field} requer uma tabela."

    override fun errorScriptBoolean(field: String): String =
        "${field} requer um valor lógico."

    override fun errorScriptInteger(field: String): String =
        "${field} requer um número inteiro."

    override fun errorScriptList(field: String): String =
        "${field} requer uma lista com índices consecutivos a partir de um."

    override fun errorScriptUnknownKey(field: String): String =
        "Chave desconhecida ${field}."

    override fun errorScriptRequired(field: String): String =
        "${field} é obrigatório."

    override fun errorScriptNonblank(field: String): String =
        "${field} não pode estar vazio."

    override fun errorScriptDuplicate(field: String, value: String): String =
        "${field} repete '${value}'."

    override fun errorScriptDirectory(field: String): String =
        "${field} é um diretório."

    override fun errorScriptEmpty(field: String): String =
        "${field} requer ao menos um elemento."

    override fun errorScriptStringKeys(field: String): String =
        "${field} requer chaves de texto não vazias."

    override fun errorProjectInvalidLua(path: String, detail: String): String =
        "Lua inválido em ${path}: ${detail}"

    override fun errorProjectDuplicateNames(names: String): String =
        "Os nomes dos projetos devem ser únicos: ${names}."

    override fun errorProjectUnknownDependency(project: String, dependency: String): String =
        "O projeto '${project}' possui a dependência desconhecida '${dependency}'."

    override fun errorProjectCycle(cycle: String): String =
        "As dependências dos projetos contêm um ciclo: ${cycle}."

    override fun errorProjectFileOwnership(path: String, owner: String, project: String): String =
        "O arquivo '${path}' pertence a '${owner}' e a '${project}'."

    override fun errorProjectFiletype(path: String): String =
        "Nenhum tipo de arquivo configurado corresponde a '${path}'."

    override fun errorConfigurationPrepend(value: String): String =
        "preamble.prepend '${value}' inválido; use 'always' ou 'without-directives'."

    override fun errorScriptFunction(field: String): String =
        "${field} requer uma função."

    override fun errorScriptLiteral(field: String): String =
        "${field} requer um valor literal."

    override fun errorScriptAlternatives(field: String, alternatives: String): String =
        "${field} requer ao menos um de ${alternatives}."

    override val errorLuaInterpolationFailure: String =
        "O parâmetro 'options' contém uma expressão Lua ou uma tag de interpolação inválida."

    override val errorYamlTagsInLua: String =
        "Tags MVEL requerem o motor YAML; Lua usa {{ expression }}."

    override val errorLuaTagsInYaml: String =
        "Tags Lua requerem uma regra Lua."

    override val errorLuaUnterminatedTag: String =
        "Tag de interpolação não terminada."

    override val errorLuaUnterminatedQuote: String =
        "Texto entre aspas não terminado na tag de interpolação."

    override val errorLuaUnterminatedLongString: String =
        "Texto longo não terminado na tag de interpolação."

    override val errorLuaMapKeys: String =
        "As chaves do mapa colidem como textos Lua."

    override val errorLuaTableKeys: String =
        "As chaves da tabela Lua colidem como textos."

    override fun errorLuaExactInteger(value: String): String =
        "O número inteiro ${value} não pode ser representado exatamente como número Lua."

    override val errorLuaCyclicValues: String =
        "Tabelas e coleções cíclicas não podem ser convertidas."

    override fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String =
        "argumento nº ${index} inválido para '${helper}' (esperado ${expected}, recebido ${actual})"

    override fun errorLuaBooleanResult(helper: String, actual: String): String =
        "resultado inválido de '${helper}' (esperado boolean, recebido ${actual})"

    override fun errorLuaHelperNoArgs(helper: String): String =
        "${helper} requer ao menos um argumento."

    override fun errorLoadCouldNotLoadDatabase(name: String): String =
        "Não foi possível carregar o banco de dados '${name}'. Verifique o acesso ao arquivo e a integridade do cache YAML."

    override fun errorSaveCouldNotSaveDatabase(name: String): String =
        "Não foi possível salvar o banco de dados '${name}'. Verifique se o diretório existe e permite escrita."

    override fun errorBasenameNotAFile(path: String): String =
        "O método 'basename' requer um arquivo, não um diretório. Observe que '${path}' não parece ser um arquivo. Se você precisa realizar alguma tarefa em um diretório, experimente os métodos disponíveis na API Java."

    override val errorCalculateHashIoException: String =
        "Por alguma razão, eu não pude calcular o hash. Não tenho ideia porque não deu certo. Talvez o arquivo tenha sido movido ou removido antes ou durante a operação de hash. Ou talvez eu não tenha as permissões corretas para leitura do arquivo."

    override fun errorCheckBooleanNotValidBoolean(value: String): String =
        "Observe que '${value}' não é um valor lógico. Isso é fácil de resolver. Certifique-se de usar uma palavra válida que represente valores lógicos ('yes' e 'no', 'true' e 'false', '1' e '0', e 'on' e 'off')."

    override fun errorCheckRegexIoException(file: String): String =
        "Não consegui ler o conteúdo do arquivo '${file}', houve um erro de entrada e saída. Não tenho ideia porque não deu certo. Talvez o arquivo tenha sido movido ou removido antes ou durante a operação de leitura. Ou talvez eu não tenha as permissões corretas para leitura do arquivo."

    override val errorConfigurationGenericError: String =
        "Não consegui analisar o arquivo de configuração, alguma coisa de errado aconteceu. Esta parte é complicada, pois envolve aspectos do formato de serialização de dados subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override val errorConfigurationLoopsInvalidRange: String =
        "O valor definido na chave 'loops' no arquivo de configuração, que define o número máximo de iterações, possui um intervalo inválido. Por favor, certifique-se de especificar um valor inteiro positivo."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String): String =
        "Não consegui encontrar o arquivo especificado '${file}' ${extensions}. Por favor, certifique-se de que o arquivo existe e que possui uma extensão válida."

    override fun errorDiscoverFileSafeModeNoCompletion(file: String) =
        "Não consegui encontrar o arquivo especificado '${file}'. O modo seguro não completa referências de arquivos a partir de suas extensões, por favor, forneça o nome completo do arquivo, com a extensão."

    override fun errorDiscoverFileUnknownExtension(
        file: String,
        extensions: String
    ) =
        "Não consegui determinar o tipo de arquivo '${file}': nenhum tipo de arquivo desta execução corresponde à extensão dele. As extensões desta execução são ${extensions}. Adicione a extensão que falta aos 'file_types' (Lua) / 'filetypes' (YAML) da configuração."

    override fun errorDiscoverFileUnknownLuaExtension(
        file: String,
        extensions: String
    ) =
        "Não consegui determinar o tipo de arquivo '${file}': nenhum tipo de arquivo desta execução corresponde à extensão dele. As extensões desta execução são ${extensions}. Adicione a extensão aos 'file_types' (Lua) / 'filetypes' (YAML) da configuração - ou, se o arquivo for uma especificação de projeto, o sufixo dele é '.proj.lua'."

    override val errorEvaluateCompilationFailed: String =
        "Por alguma razão, eu não consegui compilar a expressão condicional especificada. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override val errorEvaluateNotBooleanValue: String =
        "A avaliação da expressão condicional deveria retornar um valor lógico como resultado. Isso é fácil de resolver. Apenas certifique-se de que a avaliação da expressão condicional retorne um valor lógico no final."

    override val errorExtractorIoError: String =
        "Ocorreu um erro de entrada e saída enquanto eu tentava extrair as diretivas. Não tenho ideia porque não deu certo. Talvez o arquivo tenha sido movido ou removido antes ou durante a operação de extração. Ou talvez eu não tenha as permissões corretas para leitura do arquivo."

    override val errorExtractorInterpolationFailure: String =
        "O parâmetro 'options' contém expressões que o interpretador MVEL não é capaz de avaliar. Por favor, verifique se 'options' contém expressões '@{…}' válidas."

    override fun errorFiletypeInvalidPattern(pattern: String): String =
        "Parece que o padrão \"${pattern}\" que você quis usar para este tipo de arquivo não é uma expressão regular válida. Isto deve ser fácil de resolver: certifique-se de usar uma expressão válida e tente novamente."

    override fun errorFiletypeNotAFile(path: String): String =
        "O método 'filetype' requer um arquivo, não um diretório. Observe que '${path}' não parece ser um arquivo. Se você precisa realizar alguma tarefa em um diretório, experimente os métodos disponíveis na API Java."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String): String =
        "Não consegui reconhecer '${extension}' como uma extensão padrão. Se você quiser definir um novo tipo de arquivo, certifique-se de especificar a extensão e o padrão. Estas são as extensões definidas por padrão: ${extensions}"

    override val errorGetApplicationPathEncodingException: String =
        "Houve um problema de codificação enquanto eu tentava obter o caminho da aplicação. Não há muito o que eu possa fazer sobre isso."

    override fun errorInterpreterArgumentIsRequired(argument: String): String =
        "Parece que o argumento '${argument}' está marcado como obrigatório na regra, mas eu não consegui encontrá-lo nos parâmetros da diretiva correspondente. Por favor, certifique-se de adicioná-lo como parâmetro em sua diretiva e tente novamente."

    override val errorInterpreterCommandRuntimeError: String =
        "Não consegui avaliar um dos comandos disponibilizados. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String): String =
        "O comando \"${command}\" inesperadamente encerrou com um código de saída diferente de zero. Por favor, verifique a saída do comando para avaliar os próximos passos."

    override val errorInterpreterUserRequestedHalt: String =
        "arara será interrompida neste momento conforme sua requisição (ou de uma regra)."

    override val errorInterpreterExitRuntimeError: String =
        "Não consegui avaliar a expressão associada ao código de saída de um dos comandos disponibilizados. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override val errorInterpreterFlagRuntimeException: String =
        "Não consegui avaliar a expressão associada à presença de um dos argumentos. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override fun errorLogFormatUnknown(value: String): String =
        "Formato de log desconhecido '${value}'. Use none, text ou json."

    override fun errorTerminalWidthInvalid(minimum: Int): String =
        "A largura do terminal deve ser de pelo menos ${minimum} colunas."

    override fun errorConsoleModeUnknown(mode: String): String =
        "Modo de console desconhecido '${mode}'. Use plain ou styled."

    override fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String =
        "Formato de regra não suportado para '${identifier}': '${format}'. Use um arquivo de regra Lua ou YAML."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String): String =
        "Não consegui encontrar uma regra chamada '${identifier}' nos caminhos de regras especificados. Talvez uma palavra digitada incorretamente? Eu estava procurando por um arquivo chamado '${expected}' nos seguintes caminhos, em ordem de prioridade: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String): String =
        "Encontrei as seguintes chaves desconhecidas na diretiva: ${keys}. Isso é fácil de resolver, apenas remova essas chaves do seu mapa."

    override val errorInterpreterWrongExitClosureReturn: String =
        "A expressão 'exit' referente ao código de saída deve sempre retornar um valor lógico (mesmo se não há computação propriamente dita). Isso é fácil de resolver: apenas certifique-se de corrigir o tipo de retorno e tente novamente."

    override val errorInterpreterWrongReturnType: String =
        "Uma regra deve retornar como tipo um valor lógico ou de comando. Outros tipos de retorno não são suportados."

    override val errorInvalidProjectFile: String =
        "I have read a file that was supposed to be a project. Unfortunately, it turned out it was neither a project nor a list of projects. Please double-check that your project file follows the documented format."

    override fun errorLoadCouldNotLoadXml(name: String): String =
        "Não consegui carregar o banco de dados YAML chamado '${name}'. Não tenho ideia porque não deu certo. Talvez o arquivo tenha sido movido ou removido antes ou durante a operação de leitura. Ou talvez eu não tenha as permissões corretas para leitura do arquivo. A propósito, certifique-se de que o arquivo YAML esteja correto."

    override fun errorParserInvalidPreamble(name: String): String =
        "Sinto muito, mas o preâmbulo '${name}' não foi encontrado. Por favor, certifique-se de que esta chave existe no arquivo de configuração."

    override val errorParseRuleGenericError: String =
        "Não consegui analisar a regra, alguma coisa de errado aconteceu. Esta parte é complicada, pois envolve aspectos do formato de serialização de dados subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override val errorReplicateListMissingFormatArgumentsException: String =
        "Não consegui replicar a lista por causa de um formato de argumento faltante. Meu palpite é que existem menos (ou mais) parâmetros do que o esperado. Certifique-se de corrigir o número de parâmetros e tente novamente."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String): String =
        "Encontrei um erro na regra '${identifier}' localizada em '${location}'."

    override val errorRunGenericException: String =
        "Não consegui executar o comando de sistema disponibilizado, alguma coisa de errado aconteceu. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override val errorRunInterruptedException: String =
        "A execução do comando de sistema disponibilizado foi inesperadamente interrompida. Talvez tenha ocorrido uma interrupção externa que forçou o comando a encerrar abruptamente."

    override val errorRunInvalidExitValueException: String =
        "A execução do comando de sistema retornou um código de saída inválido."

    override val errorRunIoException: String =
        "A execução do comando de sistema falhou por causa de um erro de entrada e saída. Você tem certeza de que o comando de sistema especificado existe no seu caminho? Pode ser uma boa ideia verificar o caminho do sistema e confirmar se o comando realmente está disponível."

    override val errorRunTimeoutException: String =
        "A execução do comando de sistema atingiu o timeout especificado e foi abortada. Se o tempo foi muito curto, certifique-se de especificar um valor maior."

    override fun errorSaveCouldNotSaveXml(name: String): String =
        "Não consegui salvar o banco de dados YAML chamado '${name}'.  Não tenho ideia porque não deu certo. Talvez eu não tenha as permissões corretas para escrever o arquivo YAML no disco."

    override fun errorSessionObtainUnknownKey(key: String): String =
        "O método 'get' encontrou a chave desconhecida '${key}' no escopo de sessão. Não posso obter um valor que não existe na sessão. Por favor, especifique uma chave válida e tente novamente."

    override fun errorSessionRemoveUnknownKey(key: String): String =
        "O método 'remove' encontrou a chave desconhecida '${key}' no escopo de sessão. Não posso remover um valor que não existe na sessão. Por favor, especifique uma chave válida e tente novamente."

    override fun errorValidateEmptyFilesList(lines: String): String =
        "Li uma diretiva ${lines} e descobri que a lista 'files' especificada está vazia. Isso é fácil de resolver: certifique-se de que a lista possua, pelo menos, um elemento e tente novamente."

    override fun errorValidateFilesIsNotAList(lines: String): String =
        "Li uma diretiva ${lines} e descobri que 'files' requer uma lista. Por favor, certifique-se de corrigir o tipo para uma lista adequada e tente novamente."

    override fun errorValidateInvalidDirectiveFormat(lines: String): String =
        "Encontrei uma diretiva inválida ${lines} no arquivo especificado. Certifique-se de corrigir a diretiva e tente novamente."

    override val errorValidateNoDirectivesFound: String =
        "Parece que o arquivo especificado não possui diretivas! Certifique-se de incluir, pelo menos, uma diretiva e tente novamente."

    override fun errorValidateOrphanLinebreak(line: String): String =
        "Aparentemente, existe uma quebra de linha de uma diretiva órfã na linha ${line}. Não posso continuar. Por favor, corrija a diretiva e tente novamente."

    override fun errorValidateReferenceIsReserved(lines: String): String =
        "Li uma diretiva ${lines} e descobri que a chave 'reference' foi utilizada. Esta chave é reservada, portanto você não pode utilizá-la! Mas não se preocupe, isso é fácil de resolver. Apenas substitua esse nome por outro."

    override fun errorValidateYamlException(lines: String): String =
        "Ocorreu um problema com o mapa YAML especificado em uma diretiva ${lines}. Esta parte é complicada, pois envolve aspectos do formato de serialização de dados subjacente."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String): String =
        "O identificador de argumento '${identifier}' está reservado, portanto você não pode utilizá-lo. Isso é fácil de resolver. Apenas substitua esse nome por outro."

    override val errorValidateBodyDuplicateArgumentIdentifiers: String =
        "Aparentemente, você possui identificadores de argumentos duplicados em sua regra. Certifique-se de corrigir esse erro e tente novamente."

    override val errorValidateBodyMissingKeys: String =
        "Ao definir um escopo de argumento de regra, ao menos a chave 'flag' ou 'default' deve ser utilizada. Por favor, certifique-se de utilizar, pelo menos, uma delas."

    override val errorValidateBodyNullArgumentId: String =
        "Descobri que um dos argumentos não possui identificador associado. Por favor, certifique-se de adicionar um identificador válido ao argumento e tente novamente."

    override val errorValidateBodyNullCommand: String =
        "Encontrei um comando nulo na regra especificada. Isso é fácil de resolver. Certifique-se de adicionar um comando válido na regra."

    override val errorValidateHeaderNullName: String =
        "A regra especificada não possui nome. Isso é fácil de resolver. Certifique-se de adicionar um nome válido e tente novamente."

    override fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String =
        "A regra possui um identificador incorreto. Eu estava esperando '${expected}', mas encontrei '${actual}'. Isso é fácil de resolver: apenas substitua o identificador incorreto pelo valor correto."

    override val infoLabelProject = "Projeto:"

    override val infoDisplaySummaryTitle = "Resumo da execução"

    override val infoDisplaySummaryFile = "Arquivo"

    override val infoDisplaySummaryTasks = "Tarefas"

    override val infoDisplaySummaryTime = "Tempo"

    override val infoDisplayFullOutputInLog =
        "a saída completa está no arquivo de log"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "SUCESSO TOTAL · ${if (files == 1) "1 arquivo" else "$files arquivos"}, " +
            "${if (tasks == 1) "1 tarefa" else "$tasks tarefas"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "FALHA TOTAL · ${if (files == 1) "1 arquivo" else "$files arquivos"}, " +
            "${if (tasks == 1) "1 tarefa" else "$tasks tarefas"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "EXECUÇÃO SIMULADA · ${if (tasks == 1) "1 tarefa" else "$tasks tarefas"}"

    override val infoConsoleSkipped: String =
        "Ignorada"

    override val infoConsoleUnprocessed: String =
        "Não processado"

    override val infoDisplayExceptionMoreDetails: String =
        "Detalhes adicionais estão disponíveis para esta exceção:"

    override fun infoDisplayExecutionTime(time: String): String =
        "Total: ${time} segundos"

    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String =
        "Processando '${name}' (tamanho: ${size}, última modificação: ${lastModified}), por favor, aguarde."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String =
        "Apesar da execução em modo dry-run, é provável que esta entrada já foi processada, dado que o seguinte valor lógico foi retornado: ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String): String =
        "A executar: ${command}"

    override val infoLabelAuthor: String =
        "Autor:"

    override val infoLabelAuthors: String =
        "Autores:"

    override val infoLabelSystemCommand = "Comando do sistema:"

    override val infoLabelConditional: String =
        "Expressão condicional:"

    override val infoLabelNoAuthors: String =
        "Nenhum autor informado"

    override val infoLabelOnWarning: String =
        "AVISO"

    override val infoLabelOnQuestion: String =
        "PERGUNTA"

    override val infoLabelOnInfo: String =
        "INFO"

    override val infoLabelDialogPrompt: String =
        "Escolha uma op\u00e7\u00e3o"

    override val infoLabelOnDetails: String =
        "DETALHES"

    override val infoLabelOnError: String =
        "ERRO"

    override val infoLabelOnFailure: String =
        "FALHA"

    override val infoLabelOnSuccess: String =
        "SUCESSO"

    override val infoLabelUnnamedTask: String =
        "Tarefa sem nome"

    override val infoParserNotes: String =
        "arara é distribuída sob a licença New BSD."

    override val logInfoBeginBuffer: String =
        "INÍCIO DO BUFFER DE SAÍDA"

    override fun logInfoBooleanMode(value: Boolean): String =
        "Valor lógico: ${value}"

    override val logInfoDirectivesBlock: String =
        "DIRETIVAS"

    override val logInfoEndBuffer: String =
        "FINAL DO BUFFER DE SAÍDA"

    override fun logInfoInterpretRule(rule: String): String =
        "Estou pronto para interpretar a regra '${rule}'."

    override fun logInfoInterpretTask(task: String, rule: String): String =
        "Estou pronto para interpretar a tarefa '${task}' da regra '${rule}'."

    override fun logInfoPotentialPatternFound(line: String, pattern: String): String =
        "Encontrei um padrão em potencial na linha ${line}: ${pattern}"

    override fun logInfoRuleLocation(path: String): String =
        "Localização da regra: '${path}'"

    override fun logInfoSystemCommand(command: String): String =
        "Comando de sistema: ${command}"

    override val logInfoTaskResult: String =
        "Resultado da tarefa:"

    override val logInfoValidatedDirectives: String =
        "Todas as diretivas foram validadas. Estamos prontos para continuar."

    override fun logInfoWelcomeMessage(version: String): String =
        "Olá, eu sou o arara ${version}!"
}
