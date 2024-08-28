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
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * LogControlWatcherTest
 */
@ExtendWith(MockitoExtension.class)
class LogControlWatcherTest {
    public static final String LOGGER_NAME = "test";

    public static final String LEVEL_DEBUG = "DEBUG";

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);

    private static final String PATH_STRING = "/logcontrol/logcontrol.json";

    private LogControlWatcher logControlWatcher;

    /**
     * setUp
     */
    @BeforeEach
    public void setUp() {
        logControlWatcher = spy(new LogControlWatcher());
    }

    /**
     * TestSetLogControlPath
     */
    @Test
    void TestSetLogControlPath() {
        // Make sure the private attribute logControlPath is not set
        assert ((logControlWatcher.getLogControlPath()) == null);

        final Path expectedPath = Paths.get(PATH_STRING);

        // now call the method and check the attribute is set
        logControlWatcher.setLogControlPath();
        final Path newPath = logControlWatcher.getLogControlPath();
        assert ((expectedPath.compareTo(newPath) == 0));
    }

    /**
     * TestIncLogChangesCounter
     */
    @Test
    void TestIncLogChangesCounter() {
        assert ((logControlWatcher.getLoggerName() == null));

        logControlWatcher.incLogChangesCounter();
        assert ((logControlWatcher.getLogChangesCounter() == 1));
    }

    /**
     * TestResetLogChangesCounter
     */
    @Test
    void TestResetLogChangesCounter() {
        logControlWatcher.incLogChangesCounter();
        logControlWatcher.incLogChangesCounter();
        assert ((logControlWatcher.getLogChangesCounter() == 2));

        logControlWatcher.resetLogChangesCounter();
        assert ((logControlWatcher.getLogChangesCounter() == 0));
    }

    /**
     * TestStopWatching
     */
    @Test
    void TestStopWatching() {
        logControlWatcher.stopWatching();
        assert (logControlWatcher.getLogChangesCounter() == 0);
        assert (logControlWatcher.getInitialized() == false);
    }

    /**
     * TestWatchSuccess
     *
     * @throws InterruptedException -
     */
    @Test
    void TestWatchSuccess() throws InterruptedException {
        // Check success flow, when monitored file has been nodified
        final WatchKey watchKey = mock(WatchKey.class);
        @SuppressWarnings("unchecked")
        final WatchEvent<Path> pathChangedEvent = mock(WatchEvent.class);

        final Path path = Paths.get(PATH_STRING);
        final WatchService watchService = mock(WatchService.class);

        when(pathChangedEvent.context()).thenReturn(path);
        when(watchKey.pollEvents()).thenReturn(Arrays.asList(pathChangedEvent));
        when(watchKey.reset()).thenReturn(false);
        when(watchService.take()).thenReturn(watchKey);

        logControlWatcher.setLogControlJsonFilename(PATH_STRING);
        logControlWatcher.setLoggerName(LOGGER_NAME);
        logControlWatcher.setWatchService(watchService);
        doNothing().when(logControlWatcher).applyLogControlLogLevel();
        logControlWatcher.watch();

        verify(logControlWatcher, times(1)).applyLogControlLogLevel();
        verify(watchService, times(1)).take();
        verify(watchKey, times(1)).reset();
        verify(watchKey, times(1)).pollEvents();

        // Make sure loop repeats
        when(watchKey.reset()).thenReturn(true).thenReturn(false);
        logControlWatcher.watch();
        verify(logControlWatcher, times(3)).applyLogControlLogLevel();
    }

    /**
     * TestApplyLogControlLogLevel
     *
     * @throws IOException -
     */
    @Test
    public void TestApplyLogControlLogLevel() throws IOException {
        final BufferedReader mockBufferedFileReader = mock(BufferedReader.class);
        doReturn(mockBufferedFileReader).when(logControlWatcher).getBufferedFileReader();

        logControlWatcher.setLoggerName(LOGGER_NAME);
        logControlWatcher.setLogControlPath(PATH_STRING);
        String jsonString = "[{\"container\": \"test\", \"severity\": \"DEBUG\"}]";
        when(mockBufferedFileReader.readLine()).thenReturn(jsonString).thenReturn(null);

        final Logger oldLogger = logControlWatcher.getLoggerByName(LOGGER_NAME);
        oldLogger.setLevel(Level.toLevel(LEVEL_DEBUG));
        final Logger logger = logControlWatcher.getLoggerByName(LOGGER_NAME);
        logger.setLevel(Level.toLevel(LEVEL_DEBUG));
        when(logControlWatcher.getLoggerByName("com.ericsson")).thenReturn(oldLogger).thenReturn(logger);

        // Make sure counter do not change when log level remains the same
        assert ((logControlWatcher.getLogChangesCounter() == 0));
        logControlWatcher.applyLogControlLogLevel();
        assert ((logControlWatcher.getLogChangesCounter() == 0));

        // Make sure log level changes when file changes
        jsonString = "[{\"container\": \"test\", \"severity\": \"ERROR\"}]";
        when(mockBufferedFileReader.readLine()).thenReturn(jsonString).thenReturn(null);

        logControlWatcher.applyLogControlLogLevel();
        assert ((logControlWatcher.getLogChangesCounter() == 1));
    }

    /**
     * Test reading customFilters from JSON
     *
     * @throws IOException -
     */
    @Test
    public void TestApplyLogControlLogLevelFromJson() throws IOException {
        final BufferedReader mockBufferedFileReader = mock(BufferedReader.class);
        doReturn(mockBufferedFileReader).when(logControlWatcher).getBufferedFileReader();

        logControlWatcher.setLoggerName(LOGGER_NAME);
        logControlWatcher.setLogControlPath(PATH_STRING);
        String jsonString = "[{\"container\": \"test\", \"severity\": \"DEBUG\", \"customFilters\": \"com.ericsson.oss\"}]";
        when(mockBufferedFileReader.readLine()).thenReturn(jsonString).thenReturn(null);

        final Logger oldLogger = logControlWatcher.getLoggerByName(LOGGER_NAME);
        oldLogger.setLevel(Level.toLevel(LEVEL_DEBUG));
        final Logger logger = logControlWatcher.getLoggerByName(LOGGER_NAME);
        logger.setLevel(Level.toLevel(LEVEL_DEBUG));
        when(logControlWatcher.getLoggerByName("com.ericsson.oss")).thenReturn(oldLogger).thenReturn(logger);

        // Make sure counter do not change when log level remains the same
        assert ((logControlWatcher.getLogChangesCounter() == 0));
        logControlWatcher.applyLogControlLogLevel();
        assert ((logControlWatcher.getLogChangesCounter() == 0));

        // Make sure log level changes when file changes
        jsonString = "[{\"container\": \"test\", \"severity\": \"ERROR\", \"customFilters\": \"com.ericsson.oss\"}]";
        when(mockBufferedFileReader.readLine()).thenReturn(jsonString).thenReturn(null);

        logControlWatcher.applyLogControlLogLevel();
        assert ((logControlWatcher.getLogChangesCounter() == 1));
    }
}
