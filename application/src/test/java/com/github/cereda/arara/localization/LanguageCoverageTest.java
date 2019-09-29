package com.github.cereda.arara.localization;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LanguageCoverageTest {
    @Test
    public void checkLanguageCoverage() {
        // get all files
        List<File> files = new ArrayList<>();
        try {
            files = Files.list(Paths.get("src/main/resources/com/github/cereda/arara/localization"))
                    .map((Path p) -> {
                        File f = p.toFile();
                        if (f.getName().endsWith("properties") && !f.isDirectory())
                            return f;
                        else return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            Assert.fail("Could not read directory.");
        }
        assertFalse(files.isEmpty());

        // the resulting list of reports
        List<LanguageReport> reports = new ArrayList<>();
        files.forEach((File file) -> {
            try {
                // read each file into a list
                // of strings
                List<String> lines = Files.readAllLines(file.toPath());
                // get the line analysis
                LanguageReport report = LanguageUtils.analyze(lines);
                // set the file reference
                report.setReference(file);
                // add to the list
                reports.add(report);
            } catch (IOException exception) {
                Assert.fail("Fatal exception: an error was raised while "
                        + "trying to read one of the languages. Please "
                        + "make sure all languages in the provided "
                        + "directory have read permission.");
            }
        });

        // for each report, print
        // the corresponding entry
        for (LanguageReport language : reports) {
            // debug output
            System.out.println(language.getReference().getName() +
                    "\t" + String.format(" %2.2f%%", language.getCoverage()));

            // if there are problematic lines,
            // add the current language report
            if (!language.getLines().isEmpty()) {
                // legend: S = Simple message, single quotes should not be doubled
                //         P = Parametrized message, single quotes must be doubled

                // build the beginning of the line
                System.out.println(language.getReference().getName());
                // print error lines
                System.out.println(language.getLines());
            }

            assertEquals(100.0f, language.getCoverage(), 0f);
        }
    }
}
