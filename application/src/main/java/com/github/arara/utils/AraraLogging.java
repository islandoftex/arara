package com.github.arara.utils;

// needed imports
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;

/**
 * Configures the logging features.
 *
 * @since 3.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraLogging {

    /**
     * Define the logging status via a boolean flag.
     *
     * @param enable A boolean flag indicating if the logging will be enabled.
     */
    public static void enableLogging(boolean enable) {

        // get the context
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {

            // create a new configurator
            JoranConfigurator configurator = new JoranConfigurator();

            // set context and reset it
            configurator.setContext(loggerContext);
            loggerContext.reset();

            // if logging should be enabled
            if (enable) {

                // add the correct properties and load the XML file
                loggerContext.putProperty("araraLogName", AraraConstants.LOGNAME);
                configurator.doConfigure(AraraLogging.class.getResourceAsStream("/com/github/arara/conf/logback.xml"));
            }

        } catch (JoranException joranException) {
            // do nothing
        }
    }
}
