/***
 * Copyright (c) 2009-2019 Jean-François Lamy
 * 
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)  
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */
package app.owlcms.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//import org.concordiainternational.competition.spreadsheet.ExtenXLSReader;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

@RunWith(Suite.class)
@Suite.SuiteClasses( { AthleteSorterTest.class, AthleteTest.class, TwoMinutesRuleTest.class, RegistrationReaderTest.class })
public class AllTests {

    final private static Logger logger = (Logger)LoggerFactory.getLogger(AllTests.class);

    /**
     * Compare actual with expected that is read from a file (a resource found
     * on the class path)
     *
     * @param referenceFilePath
     *            a path of the form /filename where filename is located in a
     *            directory that is found on the class path.
     * @param actual
     */
    static public void assertEqualsToReferenceFile(final String referenceFilePath, String actual) {
        String name = "/testData" + referenceFilePath;
		InputStream is = AllTests.class.getResourceAsStream(name); 
		logger.info("comparing results to reference file {}", name);
        if (is != null) {
            String expected = getContents(is);
            assertEquals(referenceFilePath, expected, actual);
        } else {
            System.out.println("------ if ok, copy following to " + referenceFilePath);
            System.out.println(actual);
            System.out.println("------");
            fail(referenceFilePath + " not found");
        }
    }

    static public String getContents(InputStream is) {
        StringBuilder contents = new StringBuilder();

        try {
            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            try {
                String line = null; // not declared within while loop
                /*
                 * readLine is a bit quirky : it returns the content of a line
                 * MINUS the newline. it returns null only for the END of the
                 * stream. it returns an empty String if two newlines appear in
                 * a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return contents.toString();
    }

}
