package aspect.logger;

import aspect.logger.properties.LoggedInterceptorProperties;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CustomizableLoggedInterceptorTest {
    @Mock
    private Log logger;

    private LoggedInterceptorProperties properties = new LoggedInterceptorProperties();

    private CustomizableLoggedInterceptor interceptor = new CustomizableLoggedInterceptor(properties);

    @Mock
    MethodInvocation methodInvocation;

    @Before
    public void setupMocks() throws NoSuchMethodException {
        when(methodInvocation.getMethod()).thenReturn(Objects.class.getMethod("deepEquals", Object.class, Object.class));
        when(methodInvocation.getArguments()).thenReturn(new Object[]{1, "String argument"});
    }

    @Test
    public void shouldTraceEntranceAndDebugExit() throws Throwable {
        when(logger.isTraceEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);

        interceptor.invokeUnderTrace(methodInvocation, logger);

        verify(logger).isTraceEnabled();
        verify(logger).trace("Started deepEquals(1,String argument)", null);
        verify(logger).isDebugEnabled();
        verify(logger).debug("Finished deepEquals(1,String argument), returned null", null);
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldDebugExit() throws Throwable {
        when(logger.isTraceEnabled()).thenReturn(false);
        when(logger.isDebugEnabled()).thenReturn(true);

        interceptor.invokeUnderTrace(methodInvocation, logger);

        verify(logger).isTraceEnabled();
        verify(logger).isDebugEnabled();
        verify(logger).debug("Finished deepEquals(1,String argument), returned null", null);
        verifyNoMoreInteractions(logger);
    }

    @Test(expected = RuntimeException.class)
    public void shouldWriteToErrorLog() throws Throwable {
        when(logger.isErrorEnabled()).thenReturn(true);

        RuntimeException runtimeException = new RuntimeException("mocked error");
        when(methodInvocation.proceed()).thenThrow(runtimeException);

        try {
            interceptor.invokeUnderTrace(methodInvocation, logger);
        } finally {
            verify(logger).isTraceEnabled();
            verify(logger).isErrorEnabled();
            verify(logger).error("Exception in deepEquals(1,String argument)", runtimeException);
            verifyNoMoreInteractions(logger);
        }
    }

    @Test
    public void shouldReturnLogIsEnabled() {
        assertThat(interceptor.isLogEnabled(logger)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnEmptyEnterMessage() {
        interceptor.setEnterMessage("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullEnterMessage() {
        interceptor.setEnterMessage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnEnterMessageWithNotValidPlaceholder() {
        interceptor.setEnterMessage("enter $[foo]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnEnterMessageWithReturnValuePlaceholder() {
        interceptor.setEnterMessage("enter " + CustomizableLoggedInterceptor.PLACEHOLDER_RETURN_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnEnterMessageWithInvocationTimePlaceholder() {
        interceptor.setEnterMessage("enter " + CustomizableLoggedInterceptor.PLACEHOLDER_INVOCATION_TIME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnEmptyExitMessage() {
        interceptor.setExitMessage("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullExitMessage() {
        interceptor.setExitMessage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnExitMessageWithNotValidPlaceholder() {
        interceptor.setExitMessage("exit $[foo]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnEmptyExceptionMessage() {
        interceptor.setExceptionMessage("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullExeptionMessage() {
        interceptor.setExceptionMessage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnExceptionMessageWithNotValidPlaceholder() {
        interceptor.setExceptionMessage("exception $[foo]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnExceptionMessageWithReturnValuePlaceholder() {
        interceptor.setExceptionMessage("exception " + CustomizableLoggedInterceptor.PLACEHOLDER_RETURN_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnEnterLogLevelIsNull() {
        interceptor.setEnterMessageLogLevel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExitOnEnterLogLevelIsNull() {
        interceptor.setExitMessageLogLevel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnExceptionLogLevelIsNull() {
        interceptor.setExceptionMessageLogLevel(null);
    }
}