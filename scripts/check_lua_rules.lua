-- SPDX-License-Identifier: BSD-3-Clause
-- Smoke test for the converted Lua rules: loads every rule, checks its
-- structure and evaluates flags and commands against dummy values using
-- stub implementations of the helper bindings.

local checked, failures = 0, 0

local function stub_environment()
  -- pure-Lua helpers (mirroring the prelude in LuaRuleEnvironment)
  function contains(list, value)
    for _, v in ipairs(list) do
      if v == value then return true end
    end
    return false
  end
  function keys(map)
    local result = {}
    for k in pairs(map) do result[#result + 1] = tostring(k) end
    table.sort(result)
    return result
  end
  function join(list, separator)
    local strings = {}
    for i, v in ipairs(list) do strings[i] = tostring(v) end
    return table.concat(strings, separator or " ")
  end
  function isList(v) return type(v) == "table" end
  function isMap(v) return type(v) == "table" end
  function isString(v) return type(v) == "string" end
  function isBoolean(v) return type(v) == "boolean" end

  -- Kotlin bridge stubs
  local function is_empty(v)
    if v == nil then return true end
    if type(v) == "table" then return #v == 0 end
    return tostring(v) == ""
  end
  local function truthy(v)
    if type(v) == "boolean" then return v end
    local yes = { yes = true, ["true"] = true, ["1"] = true, on = true }
    return yes[tostring(v)] or false
  end
  local function conditional(positive)
    return function(v, yes, no, fallback)
      if yes == nil then
        return not is_empty(v) and truthy(v) == positive
      end
      no = no == nil and "" or no
      if is_empty(v) then return fallback ~= nil and fallback or no end
      if truthy(v) == positive then return yes end
      return no
    end
  end
  isTrue = conditional(true)
  isFalse = conditional(false)
  function isEmpty(v, yes, no)
    if yes == nil then return is_empty(v) end
    if is_empty(v) then return yes end
    return no == nil and "" or no
  end
  function isNotEmpty(v, yes, no)
    if yes == nil then return not is_empty(v) end
    if not is_empty(v) then return yes end
    return no == nil and "" or no
  end
  function throwError(message) error(message, 0) end
  function getCommand(...) return { __command = true, ... } end
  function getCommandWithWorkingDirectory(...) return getCommand(...) end
  function getBasename(name)
    return (name:gsub("^.*/", ""):gsub("%.[^.]*$", ""))
  end
  function getFiletype(name) return name:match("%.([^.]*)$") or "" end
  function getOriginalFile() return "file.tex" end
  function getOriginalReference() return "/tmp/file.tex" end
  function halt(status) end
  function buildString(...) return join({...}, " ") end
  function trimSpaces(s) return (s:gsub("^%s+", ""):gsub("%s+$", "")) end
  function replicatePattern(pattern, values)
    local result = {}
    for i, v in ipairs(values) do
      result[i] = pattern:gsub("%%s", tostring(v))
    end
    return result
  end
  function isVerboseMode() return false end
  function isOnPath(command) return true end
  local function os_check(result)
    return function(yes, no)
      if yes == nil then return result end
      if result then return yes end
      return no
    end
  end
  isWindows = os_check(false)
  isLinux = os_check(true)
  isMac = os_check(false)
  isUnix = os_check(true)
  isCygwin = os_check(false)
  function unsafelyExecuteSystemCommand(command)
    return { first = 0, second = "output" }
  end
  function listFilesByExtensions(...) return {} end
  function listFilesByPatterns(...) return {} end
  function writeToFile(...) return true end
  function readFromFile(...) return { "line one", "line two" } end
  function isSubdirectory(...) return true end
  function showMessage(...) end
  function normalizePath(p) return p end
  function createDirectories(p) return true end
  function removeDirectory(p) return true end
end

local function dummy_value(identifier)
  -- lists for the well-known list arguments, strings otherwise
  local lists = {
    options = true, options1 = true, options2 = true, extensions = true,
    modules = true, environments = true, margins = true, bbox = true,
    trackers = true, directives = true, arguments = true, input = true,
    targets = true, commands = true, pages = true, errors = true,
    mode = true, path = true, silent = true
  }
  if lists[identifier] then return { "--dummy" } end
  return "dummy"
end

local reference = {
  fileName = "file.tex",
  path = "/tmp/file.tex",
  parent = "/tmp"
}

local function check_rule(path)
  local chunk, err = loadfile(path)
  if not chunk then return "load: " .. err end
  local ok, rule = pcall(chunk)
  if not ok then return "eval: " .. tostring(rule) end
  if type(rule) ~= "table" then return "rule is not a table" end
  if type(rule.identifier) ~= "string" then return "missing identifier" end
  if type(rule.name) ~= "string" then return "missing name" end
  if type(rule.commands) ~= "table" or #rule.commands == 0 then
    return "missing commands"
  end

  local ctx = { parameters = {}, reference = reference }
  for _, argument in ipairs(rule.arguments or {}) do
    if type(argument.identifier) ~= "string" then
      return "argument without identifier"
    end
    if argument.flag == nil and argument.default == nil then
      return "argument '" .. argument.identifier ..
        "' without flag and default"
    end
    local value = dummy_value(argument.identifier)
    ctx.parameters[argument.identifier] = value
    if argument.flag then
      local flag_ctx = { parameters = ctx.parameters, reference = reference }
      local ok, result = pcall(argument.flag, flag_ctx, value)
      -- flags may legitimately throw (validation); only hard errors count
      if not ok and tostring(result):match("attempt to") then
        return "flag '" .. argument.identifier .. "': " .. tostring(result)
      end
      if ok and result ~= nil then
        ctx[argument.identifier] =
          type(result) == "table" and result or { tostring(result) }
      else
        ctx[argument.identifier] = { "--dummy" }
      end
    else
      ctx[argument.identifier] = { tostring(argument.default) }
    end
  end

  for index, command in ipairs(rule.commands) do
    if type(command.command) ~= "function" then
      return "command " .. index .. " without function"
    end
    local ok, result = pcall(command.command, ctx)
    if not ok and tostring(result):match("attempt to") then
      return "command " .. index .. ": " .. tostring(result)
    end
  end
  return nil
end

stub_environment()

for _, path in ipairs(arg) do
  checked = checked + 1
  local failure = check_rule(path)
  if failure then
    failures = failures + 1
    print("FAIL " .. path .. ": " .. failure)
  end
end

print(string.format("checked %d rules, %d failures", checked, failures))
if failures > 0 then os.exit(1) end
