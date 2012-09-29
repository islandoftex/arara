!config
# LuaLaTeX rule for arara
# author: Marco Daniel
# last edited by: Paulo Cereda
# requires arara 3.0+
identifier: lualatex
name: LuaLaTeX
command: <arara> lualatex @{action} @{draft} @{shell} @{synctex} @{options} "@{file}"
arguments:
- identifier: action
  flag: <arara> --interaction=@{parameters.action}
- identifier: shell
  flag: <arara> @{isTrue(parameters.shell,"--shell-escape","--no-shell-escape")}
- identifier: synctex
  flag: <arara> @{isTrue(parameters.synctex,"--synctex=1","--synctex=0")}
- identifier: draft
  flag: <arara> @{isTrue(parameters.draft,"--draftmode")}
- identifier: options
  flag: <arara> @{parameters.options}
