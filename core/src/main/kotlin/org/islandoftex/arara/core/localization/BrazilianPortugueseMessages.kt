// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Brazilian Portuguese localization of the arara messages.
 * Translated by Paulo Cereda.
 * The documentation of each member is inherited from the
 * [AraraMessages] interface, the single source of truth for
 * what every message says.
 */
@Suppress("MaxLineLength", "TooManyFunctions")
object BrazilianPortugueseMessages : AraraMessages {
    override val providedLocale = MPPLocale("pt-BR")

    override fun errorBasenameNotAFile(path: String) =
        "O método 'basename' requer um arquivo, não um diretório. Observe que '${path}' não parece ser um arquivo. Se você precisa realizar alguma tarefa em um diretório, experimente os métodos disponíveis na API Java."

    override val errorCalculateHashIoException =
        "Por alguma razão, eu não pude calcular o hash. Não tenho ideia porque não deu certo. Talvez o arquivo tenha sido movido ou removido antes ou durante a operação de hash. Ou talvez eu não tenha as permissões corretas para leitura do arquivo."

    override fun errorCheckBooleanNotValidBoolean(value: String) =
        "Observe que '${value}' não é um valor lógico. Isso é fácil de resolver. Certifique-se de usar uma palavra válida que represente valores lógicos ('yes' e 'no', 'true' e 'false', '1' e '0', e 'on' e 'off')."

    override fun errorCheckRegexIoException(file: String) =
        "Não consegui ler o conteúdo do arquivo '${file}', houve um erro de entrada e saída. Não tenho ideia porque não deu certo. Talvez o arquivo tenha sido movido ou removido antes ou durante a operação de leitura. Ou talvez eu não tenha as permissões corretas para leitura do arquivo."

    override val errorConfigurationGenericError =
        "Não consegui analisar o arquivo de configuração, alguma coisa de errado aconteceu. Esta parte é complicada, pois envolve aspectos do formato de serialização de dados subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override val errorConfigurationLoopsInvalidRange =
        "O valor definido na chave 'loops' no arquivo de configuração, que define o número máximo de iterações, possui um intervalo inválido. Por favor, certifique-se de especificar um valor inteiro positivo."

    override val errorConfigurationLuaReturnNotATable =
        "Um arquivo de configuração Lua precisa retornar uma tabela."

    override fun errorConfigurationUnknownKeys(keys: String) =
        "A configuração contém chaves que o formato não aceita: ${keys}."

    override fun errorConfigurationKeyRequiresString(key: String) =
        "A chave '${key}' requer um valor de texto."

    override fun errorConfigurationKeyRequiresInteger(key: String) =
        "A chave '${key}' requer um valor inteiro."

    override fun errorConfigurationKeyRequiresBoolean(key: String) =
        "A chave '${key}' requer um valor booleano."

    override fun errorConfigurationEntryNotAString(key: String) =
        "Cada entrada de '${key}' precisa ser um texto."

    override fun errorConfigurationKeyRequiresStringList(key: String) =
        "A chave '${key}' requer uma lista de textos."

    override fun errorConfigurationKeyRequiresTable(key: String) =
        "A chave '${key}' requer uma tabela."

    override fun errorConfigurationKeyRequiresStringKeys(key: String) =
        "A chave '${key}' requer textos como chaves."

    override val errorConfigurationFileTypeEntryNotATable =
        "Cada entrada de 'file_types' requer uma tabela com uma 'extension' e um 'pattern' opcional."

    override val errorConfigurationFileTypeExtensionMissing =
        "Cada entrada de 'file_types' requer uma 'extension'."

    override val errorConfigurationFileTypeKeyRequiresTableList =
        "A chave 'file_types' requer uma lista de tabelas."

    override val errorConfigurationFileTypePatternNotAString =
        "A chave 'pattern' de uma entrada de 'file_types' requer um valor de texto."

    override fun errorConfigurationPreamblePrependInvalid(value: String) =
        "A chave 'prepend' do grupo 'preamble' aceita 'always' ou 'without-directives', não '${value}'."

    override fun errorDiscoverFileFileNotFound(file: String, extensions: String) =
        "Não consegui encontrar o arquivo especificado '${file}' com qualquer uma das extensões ${extensions}. Por favor, certifique-se de que o arquivo existe e que possui uma extensão válida."

    override fun errorDiscoverFileSafeModeNoCompletion(file: String) =
        "Não consegui encontrar o arquivo especificado '${file}'. O modo seguro não completa referências de arquivos a partir de suas extensões, por favor, forneça o nome completo do arquivo, com a extensão."

    override fun errorDiscoverFileDottedExtension(
        file: String,
        extension: String
    ) =
        "Não consegui determinar o tipo de arquivo '${file}': a extensão configurada '${extension}' contém um ponto, e a busca de extensões lê apenas o último segmento de um nome. Declare o tipo com uma extensão sem ponto."

    override fun errorDiscoverFileUnknownExtension(
        file: String,
        extensions: String
    ) =
        "Não consegui determinar o tipo de arquivo '${file}': nenhum tipo de arquivo desta execução corresponde à extensão dele. As extensões desta execução são ${extensions}. Adicione a extensão que falta aos 'file_types' da configuração."

    override fun errorDiscoverFileUnknownLuaExtension(
        file: String,
        extensions: String
    ) =
        "Não consegui determinar o tipo de arquivo '${file}': nenhum tipo de arquivo desta execução corresponde à extensão dele. As extensões desta execução são ${extensions}. Adicione a extensão aos 'file_types' da configuração - ou, se o arquivo for uma especificação de projeto, o sufixo dele é '.proj.lua'."

    override val errorEvaluateCompilationFailed =
        "Por alguma razão, eu não consegui compilar a expressão condicional especificada. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override val errorEvaluateNotBooleanValue =
        "A avaliação da expressão condicional deveria retornar um valor lógico como resultado. Isso é fácil de resolver. Apenas certifique-se de que a avaliação da expressão condicional retorne um valor lógico no final."

    override val errorExtractorIoError =
        "Ocorreu um erro de entrada e saída enquanto eu tentava extrair as diretivas. Não tenho ideia porque não deu certo. Talvez o arquivo tenha sido movido ou removido antes ou durante a operação de extração. Ou talvez eu não tenha as permissões corretas para leitura do arquivo."

    override val errorExtractorInterpolationFailure =
        "O parâmetro 'options' contém expressões que o interpretador MVEL não é capaz de avaliar. Por favor, verifique se 'options' contém expressões '@{…}' válidas."

    override val errorExtractorOrbTagInLuaEngine =
        "O parâmetro 'options' contém uma orb tag do motor YAML obsoleto, que o motor Lua não consegue interpolar. Por favor, reescreva a entrada com tags '{{ … }}'."

    override fun errorFiletypeInvalidPattern(pattern: String) =
        "Parece que o padrão \"${pattern}\" que você quis usar para este tipo de arquivo não é uma expressão regular válida. Isto deve ser fácil de resolver: certifique-se de usar uma expressão válida e tente novamente."

    override fun errorFiletypeNotAFile(path: String) =
        "O método 'filetype' requer um arquivo, não um diretório. Observe que '${path}' não parece ser um arquivo. Se você precisa realizar alguma tarefa em um diretório, experimente os métodos disponíveis na API Java."

    override fun errorFiletypeUnknownExtension(extension: String, extensions: String) =
        "Não consegui reconhecer '${extension}' como uma extensão padrão. Se você quiser definir um novo tipo de arquivo, certifique-se de especificar a extensão e o padrão. Estas são as extensões definidas por padrão: ${extensions}"

    override val errorGetApplicationPathEncodingException =
        "Houve um problema de codificação enquanto eu tentava obter o caminho da aplicação. Não há muito o que eu possa fazer sobre isso."

    override fun errorInterpreterArgumentIsRequired(argument: String) =
        "Parece que o argumento '${argument}' está marcado como obrigatório na regra, mas eu não consegui encontrá-lo nos parâmetros da diretiva correspondente. Por favor, certifique-se de adicioná-lo como parâmetro em sua diretiva e tente novamente."

    override val errorInterpreterCommandRuntimeError =
        "Não consegui avaliar um dos comandos disponibilizados. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override val errorInterpreterExitRuntimeError =
        "Não consegui avaliar a expressão associada ao código de saída de um dos comandos disponibilizados. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override fun errorInterpreterCommandUnsuccessfulExit(command: String) =
        "O comando \"${command}\" não passou na verificação de saída da sua regra. Por favor, verifique a saída do comando para avaliar os próximos passos."

    override val errorInterpreterUserRequestedHalt =
        "O arquivo será interrompido agora por requisição de uma regra, e suas diretivas restantes serão ignoradas."

    override val errorInterpreterFlagRuntimeException =
        "Não consegui avaliar a expressão associada à presença de um dos argumentos. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override fun errorInterpreterHaltStatusNotANumber(key: String) =
        "Parece que o valor armazenado na chave de sessão de interrupção \"${key}\" não é um número válido; portanto, não consigo informar o status de interrupção solicitado. A correção deve ser simples: certifique-se de que o status de interrupção seja numérico e tente novamente."

    override fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String) =
        "Não consegui encontrar uma regra chamada '${identifier}' nos caminhos de regras especificados. Talvez uma palavra digitada incorretamente? Eu estava procurando por um arquivo chamado '${expected}' nos seguintes caminhos, em ordem de prioridade: ${paths}"

    override fun errorInterpreterUnknownKeys(keys: String) =
        "Encontrei as seguintes chaves desconhecidas na diretiva: ${keys}. Isso é fácil de resolver, apenas remova essas chaves do seu mapa."

    override fun errorConsoleModeUnknown(mode: String) =
        "Não consigo definir o console como '${mode}', pois não é uma das apresentações disponíveis. Isso é fácil de resolver, apenas escolha um dos modos disponíveis."

    override val errorInterpreterWrongExitClosureReturn =
        "A expressão 'exit' referente ao código de saída deve sempre retornar um valor lógico (mesmo se não há computação propriamente dita). Isso é fácil de resolver: apenas certifique-se de corrigir o tipo de retorno e tente novamente."

    override val errorInterpreterWrongReturnType =
        "Uma regra deve retornar como tipo um valor lógico ou de comando. Outros tipos de retorno não são suportados."

    override fun errorLoadCouldNotLoadDatabase(name: String) =
        "Não consegui carregar o banco de dados de arquivos chamado '${name}'. Não tenho ideia porque não deu certo. Talvez o arquivo tenha sido movido ou removido antes ou durante a operação de leitura, ou talvez eu não tenha as permissões corretas para lê-lo. A propósito, se o arquivo tiver conteúdo de uma versão anterior minha, simplesmente remova ou renomeie o arquivo, e eu construirei um banco de dados novo na próxima execução."

    override fun errorParserInvalidPreamble(name: String) =
        "Sinto muito, mas o preâmbulo '${name}' não foi encontrado. Por favor, certifique-se de que esta chave existe no arquivo de configuração."

    override val errorParseRuleGenericError =
        "Não consegui analisar a regra, alguma coisa de errado aconteceu. Esta parte é complicada, pois envolve aspectos do formato de serialização de dados subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override fun errorParseRuleArgumentIdNotAString() =
        "O identificador do argumento requer um valor de texto."

    override fun errorParseRuleArgumentNeedsFlagOrDefault(identifier: String) =
        "O argumento '${identifier}' requer pelo menos um entre 'flag' e 'default'."

    override fun errorParseRuleArgumentRequiredNotABoolean(identifier: String) =
        "A chave 'required' do argumento '${identifier}' requer um valor booleano."

    override val errorParseRuleCommandFunctionMissing =
        "Cada comando requer uma função 'command'."

    override val errorParseRuleCommandNameNotAString =
        "O nome do comando requer um valor de texto."

    override fun errorParseRuleEntryNotAString(key: String) =
        "Cada entrada de '${key}' precisa ser um texto."

    override fun errorParseRuleEntryNotATable(key: String) =
        "Cada entrada de '${key}' precisa ser uma tabela."

    override val errorParseRuleIdentifierMissing =
        "A regra não fornece um identificador."

    override val errorParseRuleIdentifierNotAString =
        "O identificador da regra requer um valor de texto."

    override fun errorParseRuleKeyRequiresStringList(key: String) =
        "A chave '${key}' requer uma lista de textos."

    override fun errorParseRuleKeyRequiresTableList(key: String) =
        "A chave '${key}' requer uma lista de tabelas."

    override val errorParseRuleNameNotAString =
        "O nome da regra requer um valor de texto."

    override fun errorParseRuleUnsupportedFormat(rule: String, location: String, format: String) =
        "Encontrei um erro na regra \"${rule}\" localizada em \"${location}\". O formato da regra \"${format}\" não é suportado pelo arara."

    override fun errorProjectCycle(cycle: String) =
        "As dependências do projeto contêm um ciclo envolvendo ${cycle}. Dependências cíclicas não podem ser resolvidas. Por favor, revise a especificação do seu projeto."

    override fun errorProjectStrayDependency(dependency: String) =
        "As especificações de projeto listam ${dependency} como dependências, mas nenhum projeto da execuição possui esses nomes. Por favor, revise as especificações de projeto e tente novamente."

    override val errorProjectDependenciesKeyRequiresStringList =
        "A chave 'dependencies' requer uma lista de textos."

    override val errorProjectDependencyEntryNotAString =
        "Cada entrada de dependência precisa ser um texto."

    override val errorProjectDirectiveEntryNotAString =
        "Cada entrada de diretiva precisa ser um texto."

    override val errorProjectDirectivesKeyRequiresTable =
        "A chave 'directives' requer uma tabela."

    override fun errorProjectDuplicateName(name: String) =
        "Dois projetos compartilham o nome '${name}'. Dentro de uma mesma execução, um projeto é identificado pelo seu nome, então cada projeto precisa de um nome próprio."

    override fun errorProjectDuplicatePath(name: String) =
        "O projeto lista o arquivo '${name}' mais de uma vez, possivelmente sob duas grafias do mesmo local. Dentro de um projeto, um arquivo é listado uma única vez."

    override fun infoDisplayProject(name: String, directory: String) =
        "Projeto: ${name} (${directory})"

    override val infoLabelProject = "Projeto:"

    override val infoLabelSingleFiles = "Arquivos avulsos"

    override fun errorProjectFileDirectivesExtraction(
        file: String,
        details: String
    ) =
        "Encontrei um erro ao extrair as diretivas de '${file}': ${details}."

    override val errorProjectFileEntryKeyNotAString =
        "As entradas de arquivos de projeto precisam ser endereçadas pelo seu nome de arquivo."

    override fun errorProjectFileInMultipleProjects(
        file: String,
        projects: String
    ) =
        "O arquivo '${file}' aparece nos projetos ${projects}. Dentro de uma mesma execução, um arquivo pertence a exatamente um projeto. Por favor, remova-o de todos exceto um."

    override val errorProjectFileIsADirectory =
        "Um arquivo de projeto não pode ser um diretório."

    override val errorProjectFileSpecNotATable =
        "Uma especificação de arquivo de projeto precisa ser uma tabela."

    override val errorProjectFilesKeyRequiresTable =
        "A chave 'files' requer uma tabela de especificações de arquivos."

    override val errorProjectFileTypeExtensionRequired =
        "Uma extensão é necessária para identificar o tipo de arquivo."

    override val errorProjectFileTypeExtensionNotAString =
        "A chave 'extension' do tipo de arquivo requer um valor de texto."

    override val errorProjectFileTypeKeyRequiresTable =
        "A chave 'file_type' requer uma tabela."

    override val errorProjectFileTypePatternMissing =
        "Falta um padrão, e a extensão não está entre os tipos de arquivo padrão."

    override val errorProjectFileTypePatternNotAString =
        "A chave 'pattern' do tipo de arquivo requer um valor de texto."

    override fun errorProjectFileTypeUnknownExtension(file: String) =
        "Não consegui determinar o tipo de arquivo '${file}': nenhum tipo de arquivo desta execução corresponde à extensão dele. Declare um 'file_type' para a entrada, ou adicione a extensão aos 'file_types' da configuração."

    override fun errorProjectSpecificationNotFound(file: String) =
        "Não consegui encontrar o arquivo de especificação \"${file}\". Por favor, verifique se o arquivo existe."

    override fun errorProjectInvalidLua(details: String) =
        "Não consegui avaliar a especificação de projeto: ${details}."

    override val errorProjectListEntryNotATable =
        "Cada entrada de uma lista de projetos precisa ser uma tabela."

    override val errorProjectListEntryNeedsName =
        "Cada projeto em uma lista de projetos precisa de um nome explícito, pois as dependências identificam projetos pelo nome."

    override val errorProjectNameKeyRequiresString =
        "A chave 'name' do projeto requer um valor de texto."

    override val errorProjectNameCannotBeBlank =
        "O nome de um projeto não pode ser vazio."

    override val errorProjectNeedsAFile =
        "Um projeto precisa conter pelo menos um arquivo."

    override val errorProjectPriorityKeyRequiresInteger =
        "A chave 'priority' requer um número inteiro."

    override val errorProjectReturnNotATable =
        "O tipo de retorno da especificação de projeto Lua precisa ser uma tabela, contendo um projeto ou uma lista de projetos."

    override val errorProjectSingleProjectHasDependencies =
        "Uma especificação de projeto único não pode declarar dependências."

    override fun errorProjectUnknownKey(name: String) =
        "A especificação de projeto contém chaves que o formato não aceita: ${name}."

    override val errorProjectWorkingDirectoryKeyRequiresString =
        "A chave 'working_directory' requer um valor de texto."

    override fun errorGlobPatternInvalid(pattern: String, details: String) =
        "O padrão glob \"${pattern}\" não pôde ser convertido em uma expressão regular: ${details}. Certifique-se de que o padrão esteja bem formado e tente novamente."

    override val errorReplicateListMissingFormatArgumentsException =
        "Não consegui replicar a lista por causa de um formato de argumento faltante. Meu palpite é que existem menos (ou mais) parâmetros do que o esperado. Certifique-se de corrigir o número de parâmetros e tente novamente."

    override fun errorRuleIdentifierAndPath(identifier: String, location: String) =
        "Encontrei um erro na regra '${identifier}' localizada em '${location}'."

    override val errorRunGenericException =
        "Não consegui executar o comando de sistema disponibilizado, alguma coisa de errado aconteceu. Esta parte é complicada, pois envolve aspectos da linguagem de expressão subjacente. Eu farei o possível para te ajudar da melhor forma que puder."

    override val errorRunInterruptedException =
        "A execução do comando de sistema disponibilizado foi inesperadamente interrompida. Talvez tenha ocorrido uma interrupção externa que forçou o comando a encerrar abruptamente."

    override val errorRunInvalidExitValueException =
        "A execução do comando de sistema retornou um código de saída inválido."

    override val errorRunIoException =
        "A execução do comando de sistema falhou por causa de um erro de entrada e saída. Você tem certeza de que o comando de sistema especificado existe no seu caminho? Pode ser uma boa ideia verificar o caminho do sistema e confirmar se o comando realmente está disponível."

    override val errorRunTimeoutException =
        "A execução do comando de sistema atingiu o timeout especificado e foi abortada. Se o tempo foi muito curto, certifique-se de especificar um valor maior."

    override val errorSafeModeClassLoading =
        "No modo seguro, as regras não têm permissão para carregar classes arbitrárias de arquivos fornecidos pelo usuário."

    override val errorSafeModeSystemCommand =
        "No modo seguro, as regras não têm permissão para executar comandos de sistema arbitrários dentro de uma etapa de execução."

    override fun errorSaveCouldNotSaveDatabase(name: String) =
        "Não consegui salvar o banco de dados de arquivos chamado '${name}'. Não tenho ideia porque não deu certo. Talvez eu não tenha as permissões corretas para escrever o arquivo no disco."

    override fun errorSessionObtainUnknownKey(key: String) =
        "O método 'get' encontrou a chave desconhecida '${key}' no escopo de sessão. Não posso obter um valor que não existe na sessão. Por favor, especifique uma chave válida e tente novamente."

    override fun errorSessionRemoveUnknownKey(key: String) =
        "O método 'remove' encontrou a chave desconhecida '${key}' no escopo de sessão. Não posso remover um valor que não existe na sessão. Por favor, especifique uma chave válida e tente novamente."

    override fun errorValidateEmptyFilesList(lines: String) =
        "Li uma diretiva ${lines} e descobri que a lista 'files' especificada está vazia. Isso é fácil de resolver: certifique-se de que a lista possua, pelo menos, um elemento e tente novamente."

    override fun errorValidateFilesIsNotAList(lines: String) =
        "Li uma diretiva ${lines} e descobri que 'files' requer uma lista. Por favor, certifique-se de corrigir o tipo para uma lista adequada e tente novamente."

    override fun errorValidateInvalidDirectiveFormat(lines: String) =
        "Encontrei uma diretiva inválida ${lines} no arquivo especificado. Certifique-se de corrigir a diretiva e tente novamente."

    override val errorValidateNoDirectivesFound =
        "Parece que o arquivo especificado não possui diretivas! Certifique-se de incluir, pelo menos, uma diretiva e tente novamente."

    override fun errorValidateOrphanLinebreak(line: Int) =
        "Aparentemente, existe uma quebra de linha de uma diretiva órfã na linha ${line}. Não posso continuar. Por favor, corrija a diretiva e tente novamente."

    override fun errorValidateReferenceIsReserved(lines: String) =
        "Li uma diretiva ${lines} e descobri que a chave 'reference' foi utilizada. Esta chave é reservada, portanto você não pode utilizá-la! Mas não se preocupe, isso é fácil de resolver. Apenas substitua esse nome por outro."

    override fun errorValidateYamlException(lines: String) =
        "Ocorreu um problema com o mapa YAML especificado em uma diretiva ${lines}. Esta parte é complicada, pois envolve aspectos do formato de serialização de dados subjacente."

    override fun errorValidateBodyArgumentIdIsReserved(identifier: String) =
        "O identificador de argumento '${identifier}' está reservado, portanto você não pode utilizá-lo. Isso é fácil de resolver. Apenas substitua esse nome por outro."

    override val errorValidateBodyDuplicateArgumentIdentifiers =
        "Aparentemente, você possui identificadores de argumentos duplicados em sua regra. Certifique-se de corrigir esse erro e tente novamente."

    override val errorValidateBodyMissingKeys =
        "Ao definir um escopo de argumento de regra, ao menos a chave 'flag' ou 'default' deve ser utilizada. Por favor, certifique-se de utilizar, pelo menos, uma delas."

    override val errorValidateBodyNullArgumentId =
        "Descobri que um dos argumentos não possui identificador associado. Por favor, certifique-se de adicionar um identificador válido ao argumento e tente novamente."

    override val errorValidateBodyNullCommand =
        "Encontrei um comando nulo na regra especificada. Isso é fácil de resolver. Certifique-se de adicionar um comando válido na regra."

    override val errorValidateHeaderNullName =
        "A regra especificada não possui nome. Isso é fácil de resolver. Certifique-se de adicionar um nome válido e tente novamente."

    override fun errorValidateHeaderWrongIdentifier(expected: String, found: String) =
        "A regra possui um identificador incorreto. Eu estava esperando '${expected}', mas encontrei '${found}'. Isso é fácil de resolver: apenas substitua o identificador incorreto pelo valor correto."

    override val infoDisplayExceptionMoreDetails =
        "Detalhes adicionais estão disponíveis para esta exceção:"

    override val infoDisplayExecutionTime = "Tempo decorrido:"
    override val infoDisplayTimeUnit = "segundos"

    override fun infoDisplayTotalSuccess(files: Int, tasks: Int) =
        "SUCESSO TOTAL · ${if (files == 1) "1 arquivo" else "$files arquivos"}, " +
            "${if (tasks == 1) "1 tarefa" else "$tasks tarefas"}"

    override fun infoDisplayTotalFailure(files: Int, tasks: Int) =
        "FALHA TOTAL · ${if (files == 1) "1 arquivo" else "$files arquivos"}, " +
            "${if (tasks == 1) "1 tarefa" else "$tasks tarefas"}"

    override fun infoDisplayDryRun(tasks: Int) =
        "EXECUÇÃO SIMULADA · ${if (tasks == 1) "1 tarefa" else "$tasks tarefas"}"
    override fun infoDisplaySkippedConditional(name: String) =
        "${name} ignorado · a condicional avaliou como falso"

    override val infoDisplayFullOutputInLog =
        "a saída completa está no arquivo de log"


    override val infoDisplaySummaryTitle = "Resumo da execução"
    override val infoDisplaySummaryFile = "Arquivo"
    override val infoDisplaySummaryTasks = "Tarefas"
    override val infoDisplaySummaryTime = "Tempo"

    override fun infoDisplayUnprocessedFiles(count: Int) =
    if (count == 1) "Arquivo n\u00e3o processado: " else "Arquivos n\u00e3o processados: "


    override fun infoDisplayFileInformation(name: String, size: String, lastModified: String) =
        "Processando '${name}' (tamanho: ${size}, última modificação: ${lastModified}), por favor, aguarde."

    override fun infoInterpreterDryRunModeBooleanMode(value: Boolean) =
        "Apesar da execução em modo dry-run, é provável que esta entrada já foi processada, dado que o seguinte valor lógico foi retornado: ${value}"

    override fun infoInterpreterDryRunModeSystemCommand(command: String) =
        "A executar: ${command}"

    override val infoLabelAuthor =
        "Autor:"

    override val infoLabelAuthors =
        "Autores:"

    override val infoLabelConditional =
        "Expressão condicional:"

    override val infoLabelNoAuthors =
        "Nenhum autor informado"

    override val infoLabelOnDetails =
        "Detalhes"

    override val infoLabelOnError =
        "ERRO"

    override val infoLabelOnFailure =
        "FALHA"

    override val infoLabelOnSuccess =
        "SUCESSO"
    override val infoLabelOnWarning = "AVISO"
    override val infoLabelOnQuestion = "PERGUNTA"
    override val infoLabelOnInfo = "INFO"
    override val infoLabelDialogPrompt = "Escolha uma op\u00e7\u00e3o"
    override val infoLabelAboutToRun =
        "A executar:"
    override val infoLabelSystemCommand = "Comando do sistema:"

    override val errorDialogInputClosed =
        "Parece que a entrada do terminal foi encerrada enquanto um di\u00e1logo aguardava uma resposta; portanto, n\u00e3o consigo ler sua escolha. Execute o arara em um terminal interativo para que o di\u00e1logo possa ser respondido."
    override val errorDialogsNotConfigured =
        "Parece que esta execu\u00e7\u00e3o n\u00e3o tem suporte a di\u00e1logos configurado; portanto, n\u00e3o posso exibir o di\u00e1logo que uma regra pediu. Forne\u00e7a uma implementa\u00e7\u00e3o de di\u00e1logos na aplica\u00e7\u00e3o que executa o arara, ou evite regras com di\u00e1logos interativos."
    override val infoLabelUnnamedTask =
        "Tarefa sem nome"

    override val infoParserNotes =
        "arara é distribuída sob a licença New BSD."
}
