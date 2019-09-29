# CTAN builder

![Version: 1.0](https://img.shields.io/badge/current_version-1.0-blue.svg?style=flat-square)
![Language: Java](https://img.shields.io/badge/language-Java-blue.svg?style=flat-square)
![Minimum JRE: 7.0](https://img.shields.io/badge/minimum_JRE-7.0-blue.svg?style=flat-square)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://opensource.org/licenses/MIT)

The `ctanbuilder` tool, as the name implies, builds a proper CTAN release based on a directory 
containing the `arara` project. Be mindful that the entire process requires external tools from
the underlying operating system and may take several minutes to complete. For instance:

```
$ java -jar ctanbuilder.jar ~/arara/
 ______ _______ _______ _______   __           __ __     __             
|      |_     _|   _   |    |  | |  |--.--.--.|__|  |.--|  |.-----.----.
|   ---| |   | |       |       | |  _  |  |  ||  |  ||  _  ||  -__|   _|
|______| |___| |___|___|__|____| |_____|_____||__|__||_____||_____|__|  

Please wait while I try to build the CTAN release of arara. Be mindful
that the entire process requires external tools and may take several
minutes to complete. There we go...

Checking if the provided directory is valid ...................... SUCCESS

Let me test if your operating system provides the required tools to build
a CTAN release for arara. Please, wait...

--------------------------------------------------------------------------
                 System tools from your operating system                  
--------------------------------------------------------------------------
Apache Maven (mvn) ............................................... SUCCESS
Java virtual machine (java) ...................................... SUCCESS
Java compiler (javac) ............................................ SUCCESS
Zip archive utility (zip) ........................................ SUCCESS
Unzip archive extraction utility (unzip) ......................... SUCCESS
--------------------------------------------------------------------------

[...]

--------------------------------------------------------------------------
                      File sizes (friendly display)                       
--------------------------------------------------------------------------
arara.zip ......................................................... 13 MB
arara.tds.zip .....................................................  6 MB
--------------------------------------------------------------------------

Done! The current working directory should contain the TDS file and the
final archive file for a proper CTAN submission! Have fun!
```

# Rule converter

![Version: 1.0](https://img.shields.io/badge/current_version-1.0-blue.svg?style=flat-square)
![Language: Java](https://img.shields.io/badge/language-Java-blue.svg?style=flat-square)
![Minimum JRE: 7.0](https://img.shields.io/badge/minimum_JRE-7.0-blue.svg?style=flat-square)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://opensource.org/licenses/MIT)

The `ruleconverter` tool attempts to automatically convert rules in the old version 3.0 format
to the new version 4.0 one. It is important to note that, although the tool might indicate a
successful conversion, there are no guarantees that the resulting rule is fully compliant with
the new format, due to potential changes in the internal workings of our tool, so your mileage
may vary. In general, it should work. For instance:

```
$ java -jar rc.jar ls.yaml
         _                                _
 ___ _ _| |___    ___ ___ ___ _ _ ___ ___| |_ ___ ___
|  _| | | | -_|  |  _| . |   | | | -_|  _|  _| -_|  _|
|_| |___|_|___|  |___|___|_|_|\_/|___|_| |_| |___|_|

version 1.0 (rules < 4.0)

The provided YAML rule looks OK. I will try my best to
convert it to the new version 4.0 format adopted by arara.
The new rule name will be written in the same directory of
the original one and will have a '_v4' suffix to it. Keep in
mind that the base name must match the identifier!

YAY! -------------------------------------------------------
Good news, everybody! The provided YAML rule was updated
successfully to the new version 4.0 format of arara! Of
course, there are no guarantees this new rule will work out
of the box, so fingers crossed! Take a closer look at the
manual and update your rule to use the new enhancements of
arara. Have a great time!
```
