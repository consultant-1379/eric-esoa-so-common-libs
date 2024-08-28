/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.bos.so.common.logging.utils;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * This class monitors the logcontrol.json file using the java.nio WatchService API and applies the
 * log level in the file.
 */
@Component
@ConditionalOnProperty(prefix = "ericsson.logging.runtime-level-control", name = "enabled", havingValue = "true")
@EnableScheduling
public class LogControlWatcher {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(LogControlWatcher.class);

    private static LogControlWatcher instance;

    private static Level newLevel;

    private Integer logChangesCounter = 0;
    private boolean initialized;
    private String loggerName;

    private Path logControlPath;
    private String logControlJsonFilename;
    private WatchService watchService;


    /**
     * LogControlWatcher constructor.
     */
    public LogControlWatcher() {
    }

    /**
     * getInstance
     *
     * @return LogControlWatcher
     */
    public static LogControlWatcher getInstance() {
        if (instance == null) {
            synchronized (LogControlWatcher.class) {
                if (instance == null) {
                    instance = new LogControlWatcher();
                }
            }
        }
        return instance;
    }

    public static Level getCurrentSeverityLevel() {
        return Optional.ofNullable(newLevel).orElse(Level.INFO);
    }

    /**
     * setLogControlPath
     */
    public void setLogControlPath() {
        logControlPath =
                Paths.get(
                        Optional.ofNullable(System.getenv("LOG_CTRL_FILE"))
                                .orElse("/logcontrol/logcontrol.json"));
    }

    public void setLoggerName(final String name) {
        loggerName = name;
    }

    public void setLogControlPath(final String filePath) {
        logControlPath = Paths.get(filePath);
    }

    public void setLogControlJsonFilename(final String jsonFilename) {
        logControlJsonFilename = jsonFilename;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * incLogChangesCounter
     */
    public void incLogChangesCounter() {
        if (logChangesCounter == null) {
            logChangesCounter = 0;
        }
        logChangesCounter = logChangesCounter + 1;
    }

    /**
     * resetLogChangesCounter
     */
    public void resetLogChangesCounter() {
        logChangesCounter = 0;
    }

    /**
     * stopWatching
     */
    public void stopWatching() {
        resetLogChangesCounter();
        initialized = false;
    }

    private void init() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        final Path logControlPathParent = logControlPath.getParent();
        logControlPathParent.register(watchService, ENTRY_MODIFY);
    }

    /**
     * Start monitoring the logcontrol.json file for changes.
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 0)
    public static void startWatching() {
        try {
            final LogControlWatcher logConfigWatcher = LogControlWatcher.getInstance();
            logConfigWatcher.setLogControlPath();
            logConfigWatcher.setLogControlJsonFilename(logConfigWatcher.logControlPath.getFileName().toString());
            logConfigWatcher.init();
            logConfigWatcher.watch();
        } catch (Exception e) {
            final Level currentLogLevel = getCurrentSeverityLevel();
            if (currentLogLevel.levelInt != Level.INFO.levelInt &&
                currentLogLevel.levelInt != Level.OFF.levelInt) {
                LOGGER.error("Error initializing LogControlWatcher.", e);
            } else {
                LOGGER.error("Error initializing LogControlWatcher. {}", e.toString());;
            }
        }
    }

    //The if clause checking whether the event change is related to the logControl.json file has been
    //removed. That because for some microservices using the library, the change event for that file
    //is not received (only the one for the mounted folder in deployment.yaml).
    //We need to consume all the events in any case to prevent watch() method from looping forever.
    private void logControlChangeEventsConsumer(final WatchKey watchKey) {
        for (final WatchEvent<?> event : watchKey.pollEvents()) {
            final Path changedFile = (Path) event.context();
            LOGGER.trace("logControlChangeEventsConsumer eventChange {}", changedFile.toString());
        }
    }

    /**
     * watch
     */
    @SuppressWarnings("java:S2696")
    public void watch() {
        initialized = true;
        while (initialized) {
            try {
                final WatchKey watchKey = watchService.take();
                logControlChangeEventsConsumer(watchKey);
                applyLogControlLogLevel();
                if (!watchKey.reset()) {
                    break;
                }
            } catch (final InterruptedException e) {
                final Level currentLogLevel = getCurrentSeverityLevel();
                if (currentLogLevel.levelInt != Level.INFO.levelInt &&
                    currentLogLevel.levelInt != Level.OFF.levelInt) {
                    LOGGER.error("LogControlWatcher has been interrupted.", e);
                } else {
                    LOGGER.error("LogControlWatcher has been interrupted. {}", e.toString());;
                }
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * getLoggerByName
     *
     * @param name -
     * @return Logger
     */
    public Logger getLoggerByName(final String name) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLogger(name);
    }

    /**
     * applyLogControlLogLevel
     */
    public void applyLogControlLogLevel() {
        final List<LogControl> logControls = readLogControlJson();
        if (logControls.size() == 0) {
            return;
        }

        // Take the first record only for now
        final LogControl logControl = logControls.get(0);

        newLevel = Level.toLevel(logControl.severity);
        if (newLevel == null) {
            // Set default log level INFO if log level not configured or not properly read.
            newLevel = Level.INFO;
        }

        // Take first filter for now
        final List<String> filters = logControl.getCustomFilters();
        if (filters != null && filters.size() > 0) {
            setLoggerName(filters.get(0));
        } else {
            // Package level logging
            setLoggerName("com.ericsson");
        }

        final Logger logger = getLoggerByName(loggerName);
        final Level oldLevel = logger.getLevel();

        if (oldLevel == null || !oldLevel.equals(newLevel)) {
            if (logger != null) {
                logger.setLevel(newLevel);
                incLogChangesCounter();
                LOGGER.info("log level of {} changed from {} to {}", logger.getName(), oldLevel, newLevel);
            }
        }
    }

    /**
     * getBufferedFileReader
     *
     * @return BufferedReader
     * @throws FileNotFoundException -
     */
    public BufferedReader getBufferedFileReader() throws FileNotFoundException {
        return (new BufferedReader(new FileReader(logControlPath.toFile())));
    }

    /**
     * readLogControlJson
     *
     * @return List<LogControl>
     */
    public List<LogControl> readLogControlJson() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<LogControl> result = Collections.emptyList();
        try {
            final BufferedReader reader = getBufferedFileReader();
            final StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            if (jsonString.length() == 0) {
                return Collections.emptyList();
            }
            result = Arrays.asList(mapper.readValue(jsonString.toString(), LogControl[].class));
        } catch (IOException e) {
            LOGGER.error("Could not read logcontrol.json.", e);
            final Level currentLogLevel = getCurrentSeverityLevel();
            if (currentLogLevel.levelInt != Level.INFO.levelInt &&
                    currentLogLevel.levelInt != Level.OFF.levelInt) {
                LOGGER.error("Could not read logcontrol.json.", e);
            } else {
                LOGGER.error("Could not read logcontrol.json. {}", e.toString());;
            }
            return Collections.emptyList();
        }
        return result;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public Path getLogControlPath() {
        return logControlPath;
    }

    public Integer getLogChangesCounter() {
        return logChangesCounter;
    }

    public boolean getInitialized() {
        return initialized;
    }

    public void setWatchService(final WatchService watchService) {
        this.watchService = watchService;
    }
}
