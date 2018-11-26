package aspect.logger;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import static aspect.logger.LogLevel.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = NONE,
        classes = LoggedTest.LoggedTestConfiguration.class
)
public class LoggedTest {

    public static final String LOGGER_NAME = TestService.class.getName();
    public static final String TEST_PARAMETER = "Test parameter";

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected TestService testService;

    @Autowired
    protected CustomizableLoggedInterceptor loggedInterceptor;

    protected Appender appender;

    @EnableAutoConfiguration
    public static class LoggedTestConfiguration {
        @Bean
        public TestService testService() {
            return new TestService();
        }

    }

    @Logged
    public static class TestService {

        public String successMethod(int intParam, String stringParam) {
            return "Success!!!";
        }

        public String errorMethod(int intParam, String stringParam) {
            throw new RuntimeException();
        }
    }

    @Before
    public void before() {
        appender = Mockito.mock(Appender.class);
        when(appender.getName()).thenReturn("TestAppender");
        when(appender.isStarted()).thenReturn(true);
        getLoggerConfig().addAppender(appender, Level.ALL, null);
    }

    @After
    public void after() {
        getLoggerConfig().removeAppender(appender.getName());
    }

    @Test
    public void shouldDebugExit() {
        Level originalLevel = setLogLevel(Level.DEBUG);
        loggedInterceptor.setExitMessageLogLevel(DEBUG);

        testService.successMethod(1, TEST_PARAMETER);

        setLogLevel(originalLevel);

        ArgumentCaptor<LogEvent> argumentCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(appender, times(1)).append(argumentCaptor.capture());

        assertThat(argumentCaptor.getAllValues().size()).isEqualTo(1);

        assertThat(argumentCaptor.getAllValues()).element(0)
                .hasFieldOrPropertyWithValue("level", Level.DEBUG)
                .extracting("message")
                .extractingResultOf("toString")
                .containsExactly("Finished successMethod(1,Test parameter), returned Success!!!");
    }

    @Test
    public void shouldTraceEntranceAndDebugExit() {
        Level originalLevel = setLogLevel(Level.TRACE);
        loggedInterceptor.setEnterMessageLogLevel(TRACE);
        loggedInterceptor.setExitMessageLogLevel(DEBUG);

        testService.successMethod(1, "Test parameter");

        ArgumentCaptor<LogEvent> argumentCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(appender, times(2)).append(argumentCaptor.capture());

        assertThat(argumentCaptor.getAllValues().size()).isEqualTo(2);

        assertThat(argumentCaptor.getAllValues()).element(0)
                .hasFieldOrPropertyWithValue("level", Level.TRACE)
                .extracting("message")
                .extractingResultOf("toString")
                .containsExactly("Started successMethod(1,Test parameter)");
        assertThat(argumentCaptor.getAllValues()).element(1)
                .hasFieldOrPropertyWithValue("level", Level.DEBUG)
                .extracting("message")
                .extractingResultOf("toString")
                .containsExactly("Finished successMethod(1,Test parameter), returned Success!!!");

        setLogLevel(originalLevel);
    }

    @Test
    public void shouldNotLogObjectMethods() {
        Level originalLevel = setLogLevel(Level.TRACE);

        testService.toString();

        setLogLevel(originalLevel);

        verify(appender, times(0)).append(Matchers.any());
    }

    @Test(expected = RuntimeException.class)
    public void shouldLogErrorOnException() {
        Level originalLevel = setLogLevel(Level.INFO);
        loggedInterceptor.setExceptionMessageLogLevel(ERROR);

        try {
            testService.errorMethod(1, "Test parameter");

            setLogLevel(originalLevel);
        } finally {
            ArgumentCaptor<LogEvent> argumentCaptor = ArgumentCaptor.forClass(LogEvent.class);
            verify(appender, times(1)).append(argumentCaptor.capture());

            assertThat(argumentCaptor.getAllValues().size()).isEqualTo(1);

            assertThat(argumentCaptor.getAllValues()).element(0)
                    .hasFieldOrPropertyWithValue("level", Level.ERROR)
                    .extracting("message")
                    .extractingResultOf("toString")
                    .containsExactly("Exception in errorMethod(1,Test parameter)");
        }
    }

    @Test(expected = RuntimeException.class)
    public void shouldntLogErrorWhenErrorLevelIsOFF() {
        Level originalLevel = setLogLevel(Level.INFO);
        loggedInterceptor.setExceptionMessageLogLevel(OFF);

        try {
            testService.errorMethod(1, "Test parameter");

            setLogLevel(originalLevel);
        } finally {
            ArgumentCaptor<LogEvent> argumentCaptor = ArgumentCaptor.forClass(LogEvent.class);
            verify(appender, times(0)).append(argumentCaptor.capture());
        }
    }

    private static Level setLogLevel(Level logLevel) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        LoggerConfig loggerConfig = configuration.getLoggerConfig(LOGGER_NAME);
        Level previousLevel = loggerConfig.getLevel();
        loggerConfig.setLevel(logLevel);
        loggerContext.updateLoggers();
        return previousLevel;
    }

    private static LoggerConfig getLoggerConfig() {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        return configuration.getLoggerConfig(LOGGER_NAME);
    }

}
