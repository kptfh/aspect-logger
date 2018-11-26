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

    protected TestAppender appender;

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
        appender = new TestAppender();
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

        assertThat(appender.getEvents().size()).isEqualTo(1);
        assertThat(appender.getEvents()).element(0)
                .hasFieldOrPropertyWithValue("level", Level.DEBUG)
                .extracting("formattedMessage")
                .containsExactly("Finished successMethod(1,Test parameter), returned Success!!!");
    }

    @Test
    public void shouldTraceEntranceAndDebugExit() {
        Level originalLevel = setLogLevel(Level.TRACE);
        loggedInterceptor.setEnterMessageLogLevel(TRACE);
        loggedInterceptor.setExitMessageLogLevel(DEBUG);

        testService.successMethod(1, "Test parameter");

        assertThat(appender.getEvents().size()).isEqualTo(2);
        assertThat(appender.getEvents()).element(0)
                .hasFieldOrPropertyWithValue("level", Level.TRACE)
                .extracting("formattedMessage")
                .containsExactly("Started successMethod(1,Test parameter)");
        assertThat(appender.getEvents()).element(1)
                .hasFieldOrPropertyWithValue("level", Level.DEBUG)
                .extracting("formattedMessage")
                .containsExactly("Finished successMethod(1,Test parameter), returned Success!!!");

        setLogLevel(originalLevel);
    }

    @Test
    public void shouldNotLogObjectMethods() {
        Level originalLevel = setLogLevel(Level.TRACE);

        testService.toString();

        setLogLevel(originalLevel);

        assertThat(appender.getEvents()).isEmpty();
    }

    @Test(expected = RuntimeException.class)
    public void shouldLogErrorOnException() {
        Level originalLevel = setLogLevel(Level.INFO);
        loggedInterceptor.setExceptionMessageLogLevel(ERROR);

        try {
            testService.errorMethod(1, "Test parameter");

            setLogLevel(originalLevel);
        } finally {
            verify(appender, times(1)).append(any());

            assertThat(appender.getEvents().size()).isEqualTo(1);

            assertThat(appender.getEvents()).element(0)
                    .hasFieldOrPropertyWithValue("level", Level.ERROR)
                    .extracting("formattedMessage")
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
            verifyZeroInteractions(appender);
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
