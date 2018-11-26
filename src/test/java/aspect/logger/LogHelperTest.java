package aspect.logger;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static aspect.logger.LogHelper.log;
import static aspect.logger.LogLevel.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LogHelperTest {

    @Mock
    private Log logger;

    private String message = "Test message";

    private Throwable throwable = new RuntimeException(message);

    @Test
    public void shouldLogTrace() {
        log(logger, TRACE, message, throwable);
        verify(logger).trace(message, throwable);
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldLogDebug() {
        log(logger, DEBUG, message, throwable);
        verify(logger).debug(message, throwable);
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldLogInfo() {
        log(logger, INFO, message, throwable);
        verify(logger).info(message, throwable);
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldLogWarn() {
        log(logger, WARN, message, throwable);
        verify(logger).warn(message, throwable);
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldLogError() {
        log(logger, ERROR, message, throwable);
        verify(logger).error(message, throwable);
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldLogFatal() {
        log(logger, FATAL, message, throwable);
        verify(logger).fatal(message, throwable);
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldNotLogOnLevelOFF() {
        log(logger, OFF, message, throwable);
        verifyZeroInteractions(logger);
    }

    @Test
    public void shouldGetIsTraceEnabled() {
        when(logger.isTraceEnabled()).thenReturn(true);
        assertThat(LogHelper.isLogLevelEnabled(logger, TRACE)).isTrue();
    }

    @Test
    public void shouldGetIsDebugEnabled() {
        when(logger.isDebugEnabled()).thenReturn(true);
        assertThat(LogHelper.isLogLevelEnabled(logger, DEBUG)).isTrue();
    }

    @Test
    public void shouldGetIsInfoEnabled() {
        when(logger.isInfoEnabled()).thenReturn(true);
        assertThat(LogHelper.isLogLevelEnabled(logger, INFO)).isTrue();
    }

    @Test
    public void shouldGetIsWarnEnabled() {
        when(logger.isWarnEnabled()).thenReturn(true);
        assertThat(LogHelper.isLogLevelEnabled(logger, WARN)).isTrue();
    }

    @Test
    public void shouldGetIsErrorEnabled() {
        when(logger.isErrorEnabled()).thenReturn(true);
        assertThat(LogHelper.isLogLevelEnabled(logger, ERROR)).isTrue();
    }

    @Test
    public void shouldGetIsFatalEnabled() {
        when(logger.isFatalEnabled()).thenReturn(true);
        assertThat(LogHelper.isLogLevelEnabled(logger, FATAL)).isTrue();
    }

    @Test
    public void shouldGetFalseWhenLevelOFF() {
        assertThat(LogHelper.isLogLevelEnabled(logger, OFF)).isFalse();
        verifyZeroInteractions(logger);
    }
}