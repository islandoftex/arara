/**
 * CTAN builder, a tool for Arara
 * Copyright (c) 2018, Paulo Roberto Massa Cereda
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated documentation  files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, sublicense,  and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and this  permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT  SHALL THE AUTHORS OR COPYRIGHT HOLDERS  BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR  OTHER LIABILITY, WHETHER IN AN  ACTION OF CONTRACT,
 * TORT OR  OTHERWISE, ARISING  FROM, OUT  OF OR  IN CONNECTION  WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.cereda.arara.ctanbuilder;

/**
 * The main application.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Application {

    public static void main(String[] args) {

        Utils.draw();
        Utils.space();
        
        if (args.length != 1) {
            Utils.message("No root directory was provided. In order to build "
                    + "a proper CTAN release, this tool requires a top level "
                    + "structure containing the arara project. Make sure to "
                    + "provide the correct location to the root directory. "
                    + "The easiest way is to clone the GitHub repository "
                    + "and provide the corresponding path. The application "
                    + "will halt now.");
            System.exit(0);
        }

        final String tmp = "tmp";
        final String root = args[0];

        try {

            Utils.message("Please wait while I try to build the CTAN "
                    + "release of arara. Be mindful that the entire "
                    + "process requires external tools and may take "
                    + "several minutes to complete. There we go...");
            Utils.space();

            Utils.task("Checking if the provided directory is valid");
            Utils.exists(Utils.Type.DIRECTORY, root);
            Utils.status(true);

            Utils.space();

            Utils.message("Let me test if your operating system "
                    + "provides the required tools to build a CTAN "
                    + "release for arara. Please, wait...");
            Utils.space();

            Utils.title("System tools from your operating system");

            Utils.task("Apache Maven (mvn)");
            Utils.available("mvn", "--version");
            Utils.status(true);

            Utils.task("Java virtual machine (java)");
            Utils.available("java", "-version");
            Utils.status(true);

            Utils.task("Java compiler (javac)");
            Utils.available("javac", "-version");
            Utils.status(true);

            Utils.task("Zip archive utility (zip)");
            Utils.available("zip", "-v");
            Utils.status(true);

            Utils.task("Unzip archive extraction utility (unzip)");
            Utils.available("unzip", "-v");
            Utils.status(true);

            Utils.line();
            Utils.space();

            Utils.message("Great, your operating system can properly "
                    + "build a CTAN release for arara! Now, let me check "
                    + "if the provided root directory contains the basic "
                    + "structure of the arara project. Please, wait...");
            Utils.space();

            Utils.title("File availability based on "
                    + "the provided root directory");

            Utils.task("The documentation");
            Utils.exists(Utils.Type.DIRECTORY, root, "docs");
            Utils.status(true);

            Utils.task("The application source code");
            Utils.exists(Utils.Type.DIRECTORY, root, "application");
            Utils.status(true);

            Utils.task("The official rule pack");
            Utils.exists(Utils.Type.DIRECTORY, root, "rules");
            Utils.status(true);

            Utils.task("The README file");
            Utils.exists(Utils.Type.FILE, root, "README.md");
            Utils.status(true);

            Utils.line();
            Utils.space();

            Utils.message("Everything looks fine, starting the build "
                    + "process. Two files will be created: an archive file "
                    + "with the basic structure and a TDS one, based on the "
                    + "CTAN guidelines. This may take several minutes. "
                    + "Please, wait...");
            Utils.space();

            Utils.title("Creating the TeX Directory Structure (TDS) archive");

            Utils.task("Compiling the application from source");
            Utils.execute(Utils.file(root, "application"), "mvn", "--file",
                    "pom.java5.xml", "compile", "assembly:single");
            Utils.status(true);

            Utils.task("Creating the temporary directory");
            Utils.mkdir(Utils.file(tmp));
            Utils.status(true);

            Utils.title("Building the documentation directory");

            Utils.task("Creating the documentation structure");
            Utils.mkdir(Utils.file(tmp, "doc"));
            Utils.mkdir(Utils.file(tmp, "doc", "support"));
            Utils.status(true);

            Utils.task("Copying the original documentation");
            Utils.copyDirectory(Utils.file(root, "docs"), Utils.file(tmp,
                    "doc", "support"));
            Utils.status(true);

            Utils.task("Renaming the documentation directory");
            Utils.file(tmp, "doc", "support", "docs").
                    renameTo(Utils.file(tmp, "doc", "support", "arara"));
            Utils.status(true);

            Utils.task("Copying the application binary");
            Utils.copyFile(Utils.file(root, "application", "target",
                    "arara-4.0-jar-with-dependencies.jar"),
                    Utils.file(tmp, "doc", "support", "arara", "arara.jar"));
            Utils.status(true);

            Utils.task("Compiling the documentation");
            Utils.execute(Utils.file(tmp, "doc", "support", "arara"),
                    "java", "-jar", "arara.jar", "arara-manual.tex");
            Utils.status(true);

            Utils.task("Copying the top level README file");
            Utils.copyFile(Utils.file(root, "README.md"), Utils.file(tmp,
                    "doc", "support", "arara", "README.md"));
            Utils.status(true);

            Utils.task("Removing the application binary");
            Utils.file(tmp, "doc", "support", "arara", "arara.jar").delete();
            Utils.status(true);

            Utils.title("Building the scripts directory");

            Utils.task("Creating the scripts structure");
            Utils.mkdir(Utils.file(tmp, "scripts"));
            Utils.mkdir(Utils.file(tmp, "scripts", "arara"));
            Utils.status(true);

            Utils.task("Copying the official rule pack directory");
            Utils.copyDirectory(Utils.file(root, "rules"), Utils.file(tmp,
                    "scripts", "arara"));
            Utils.status(true);

            Utils.task("Copying the application binary");
            Utils.copyFile(Utils.file(root, "application", "target",
                    "arara-4.0-jar-with-dependencies.jar"),
                    Utils.file(tmp, "scripts", "arara", "arara.jar"));
            Utils.status(true);

            Utils.task("Creating the shell script wrapper");
            Utils.createScript(Utils.file(tmp, "scripts",
                    "arara", "arara.sh"));
            Utils.status(true);

            Utils.title("Building the source code structure");

            Utils.task("Cleaning up the compilation");
            Utils.execute(Utils.file(root, "application"), "mvn", "clean");
            Utils.status(true);

            Utils.task("Creating the source code structure");
            Utils.mkdir(Utils.file(tmp, "source"));
            Utils.mkdir(Utils.file(tmp, "source", "support"));
            Utils.status(true);

            Utils.task("Copying the application source code directory");
            Utils.copyDirectory(Utils.file(root, "application"),
                    Utils.file(tmp, "source", "support"));
            Utils.status(true);

            Utils.task("Renaming the source code directory");
            Utils.file(tmp, "source", "support", "application").
                    renameTo(Utils.file(tmp, "source", "support", "arara"));
            Utils.status(true);

            //*******************************************************
            Utils.title("Building the TDS archive file");

            Utils.task("Creating the archive file");
            Utils.execute(Utils.file(tmp), "zip", "-r", "arara.tds.zip",
                    "doc", "scripts", "source");
            Utils.status(true);

            Utils.task("Moving the archive file to the top level directory");
            Utils.copyFile(Utils.file(tmp, "arara.tds.zip"),
                    Utils.file("arara.tds.zip"));
            Utils.status(true);

            Utils.task("Removing the temporary directory");
            Utils.removeDirectory(Utils.file(tmp));
            Utils.status(true);

            Utils.line();
            Utils.space();

            Utils.message("Great news, the TDS archive file was successfully "
                    + "created! Now, I will create the final archive file "
                    + "for CTAN submission. This may take several minutes. "
                    + "Please, wait...");
            Utils.space();

            Utils.title("Preparing the archive file for CTAN submission");

            Utils.task("Creating the temporary directory");
            Utils.mkdir(Utils.file(tmp));
            Utils.mkdir(Utils.file(tmp, "arara"));
            Utils.status(true);

            Utils.task("Copying the TDS archive file to "
                    + "the temporary directory");
            Utils.copyFile(Utils.file("arara.tds.zip"),
                    Utils.file(tmp, "arara.tds.zip"));
            Utils.status(true);

            Utils.task("Copying the temporary TDS structure");
            Utils.copyFile(Utils.file("arara.tds.zip"),
                    Utils.file(tmp, "arara", "arara.zip"));
            Utils.status(true);

            Utils.task("Extracting the temporary TDS structure");
            Utils.execute(Utils.file(tmp, "arara"), "unzip", "arara.zip");
            Utils.status(true);

            Utils.task("Removing the temporary TDS reference");
            Utils.file(tmp, "arara", "arara.zip").delete();
            Utils.status(true);

            Utils.task("Renaming the documentation structure");
            Utils.file(tmp, "arara", "doc").
                    renameTo(Utils.file(tmp, "arara", "doc-old"));
            Utils.status(true);

            Utils.task("Renaming the scripts structure");
            Utils.file(tmp, "arara", "scripts").
                    renameTo(Utils.file(tmp, "arara", "scripts-old"));
            Utils.status(true);

            Utils.task("Renaming the source code structure");
            Utils.file(tmp, "arara", "source").
                    renameTo(Utils.file(tmp, "arara", "source-old"));
            Utils.status(true);

            Utils.task("Copying the documentation directory");
            Utils.copyDirectory(Utils.file(tmp, "arara", "doc-old",
                    "support", "arara"), Utils.file(tmp, "arara"));
            Utils.status(true);

            Utils.task("Renaming the documentation directory");
            Utils.file(tmp, "arara", "arara").
                    renameTo(Utils.file(tmp, "arara", "doc"));
            Utils.status(true);

            Utils.task("Removing the old documentation structure");
            Utils.removeDirectory(Utils.file(tmp, "arara", "doc-old"));
            Utils.status(true);

            Utils.task("Copying the scripts directory");
            Utils.copyDirectory(Utils.file(tmp, "arara", 
                    "scripts-old", "arara"), Utils.file(tmp, "arara"));
            Utils.status(true);

            Utils.task("Renaming the scripts directory");
            Utils.file(tmp, "arara", "arara").
                    renameTo(Utils.file(tmp, "arara", "scripts"));
            Utils.status(true);

            Utils.task("Removing the old scripts structure");
            Utils.removeDirectory(Utils.file(tmp, "arara", "scripts-old"));
            Utils.status(true);

            Utils.task("Copying the source code directory");
            Utils.copyDirectory(Utils.file(tmp, "arara",
                    "source-old", "support", "arara"),
                    Utils.file(tmp, "arara"));
            Utils.status(true);

            Utils.task("Renaming the source code directory");
            Utils.file(tmp, "arara", "arara").
                    renameTo(Utils.file(tmp, "arara", "source"));
            Utils.status(true);

            Utils.task("Removing the old source code structure");
            Utils.removeDirectory(Utils.file(tmp, "arara", "source-old"));
            Utils.status(true);

            Utils.task("Copying the README file to the top level");
            Utils.copyFile(Utils.file(tmp, "arara", "doc", "README.md"),
                    Utils.file(tmp, "arara", "README.md"));
            Utils.status(true);

            Utils.task("Removing the original README file");
            Utils.file(tmp, "arara", "doc", "README.md").delete();
            Utils.status(true);

            Utils.task("Creating the archive file");
            Utils.execute(Utils.file(tmp), "zip", "-r", "arara.zip",
                    "arara.tds.zip", "arara");
            Utils.status(true);

            Utils.task("Copying archive file to top level");
            Utils.copyFile(Utils.file(tmp, "arara.zip"),
                    Utils.file("arara.zip"));
            Utils.status(true);

            Utils.task("Removing temporary directory");
            Utils.removeDirectory(Utils.file(tmp));
            Utils.status(true);

            Utils.line();
            Utils.space();

            Utils.title("File sizes (friendly display)");
            Utils.sizes(Utils.file("arara.zip"),
                    Utils.file("arara.tds.zip"));

            Utils.line();
            Utils.space();

            Utils.message("Done! The current working directory should "
                    + "contain the TDS file and the final archive file "
                    + "for a proper CTAN submission! Have fun!");

        } catch (Exception exception) {
            Utils.status(false);
            Utils.line();

            Utils.space();
            Utils.message(exception.getMessage());
        }
    }

}
