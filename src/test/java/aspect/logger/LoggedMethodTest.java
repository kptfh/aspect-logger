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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = NONE,
        classes = LoggedMethodTest.LoggedTestConfiguration.class
)
public class LoggedMethodTest {

    @Autowired
    protected ApplicationContext applicationContext;


    @Autowired
    protected TestServiceMethod testServiceMethod;

    protected Appender appender;

    @Before
    public void before() {
        appender = Mockito.mock(Appender.class);
        when(appender.getName()).thenReturn("TestAppender");
        when(appender.isStarted()).thenReturn(true);
        getLoggerConfig().addAppender(appender, Level.ALL, null);
    }

    @After
    public void after(){
        getLoggerConfig().removeAppender(appender.getName());
    }

    @Test
    public void shouldLogAnnotatedMethodsOnly() {

        setLogLevel(Level.DEBUG);

        testServiceMethod.annotatedMethod(1, TEST_PARAMETER);

        ArgumentCaptor<LogEvent> argumentCaptor = ArgumentCaptor.forClass(LogEvent.class);
        Mockito.verify(appender, Mockito.times(1)).append(argumentCaptor.capture());

        assertThat(argumentCaptor.getAllValues().size()).isEqualTo(1);

        assertThat(argumentCaptor.getAllValues()).element(0)
                .hasFieldOrPropertyWithValue("level", Level.DEBUG)
                .extracting("message")
                .extractingResultOf("toString")
                .containsExactly("Finished annotatedMethod(1,Test parameter), returned Annotated!!!");
    }

    @Test
    public void shouldNotLogNotAnnotatedMethods() {

        setLogLevel(Level.DEBUG);

        testServiceMethod.ignoredMethod(1, TEST_PARAMETER);

        Mockito.verify(appender, Mockito.times(0)).append(any());
    }

    @EnableAutoConfiguration
    public static class LoggedTestConfiguration {
        @Bean
        public TestServiceMethod testServiceMethod() {
            return new TestServiceMethod();
        }
    }

    public static final String TEST_PARAMETER = "Test parameter";


    public static class TestServiceMethod {

        public String ignoredMethod(int intParam, String stringParam) {
            return "Ignored!!!";
        }

        @Logged
        public String annotatedMethod(int intParam, String stringParam) {
            return "Annotated!!!";
        }
    }

    private static void setLogLevel(Level logLevel) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        configuration.getLoggerConfig(TestServiceMethod.class.getName()).setLevel(logLevel);
        loggerContext.updateLoggers();
    }

    private static LoggerConfig getLoggerConfig() {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        return configuration.getLoggerConfig(TestServiceMethod.class.getName());
    }
}

