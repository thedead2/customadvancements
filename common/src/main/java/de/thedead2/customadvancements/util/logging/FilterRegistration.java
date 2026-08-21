package de.thedead2.customadvancements.util.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;

public class FilterRegistration {

    public static void registerLoggerFilters() {
        Logger rootLogger = LogManager.getRootLogger();

        if (rootLogger instanceof org.apache.logging.log4j.core.Logger logger) {
            logger.addFilter(new MissingAdvancementFilter());
            logger.addFilter(new UnknownRecipeCategoryFilter());
            logger.addFilter(new UnknownAdvancementFilter());

            LOGGER.debug("Registered logger filters.");
        }
        else {
            LOGGER.error("Unable to register filter for Logger with unexpected class: {}", rootLogger.getClass().getName());
        }
    }
}
