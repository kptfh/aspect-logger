package aspect.logger.properties;

import aspect.logger.LogLevel;

import static aspect.logger.CustomizableLoggedInterceptor.DEFAULT_ENTER_MESSAGE;
import static aspect.logger.CustomizableLoggedInterceptor.DEFAULT_EXCEPTION_MESSAGE;
import static aspect.logger.CustomizableLoggedInterceptor.DEFAULT_EXIT_MESSAGE;
import static aspect.logger.LogLevel.DEBUG;
import static aspect.logger.LogLevel.TRACE;

public class LoggedInterceptorProperties {

    private String enterMessage = DEFAULT_ENTER_MESSAGE;
    private LogLevel enterMessageLogLevel = TRACE;

    private String exitMessage = DEFAULT_EXIT_MESSAGE;
    private LogLevel exitMessageLogLevel = DEBUG;

    private String exceptionMessage = DEFAULT_EXCEPTION_MESSAGE;
    private LogLevel exceptionMessageLogLevel = LogLevel.ERROR;

    private boolean useDynamicLogger = true;

    public LoggedInterceptorProperties(){
    }

    public String getEnterMessage() {
        return enterMessage;
    }

    public void setEnterMessage(String enterMessage) {
        this.enterMessage = enterMessage;
    }

    public LogLevel getEnterMessageLogLevel() {
        return enterMessageLogLevel;
    }

    public void setEnterMessageLogLevel(LogLevel enterMessageLogLevel) {
        this.enterMessageLogLevel = enterMessageLogLevel;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    public LogLevel getExitMessageLogLevel() {
        return exitMessageLogLevel;
    }

    public void setExitMessageLogLevel(LogLevel exitMessageLogLevel) {
        this.exitMessageLogLevel = exitMessageLogLevel;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public LogLevel getExceptionMessageLogLevel() {
        return exceptionMessageLogLevel;
    }

    public void setExceptionMessageLogLevel(LogLevel exceptionMessageLogLevel) {
        this.exceptionMessageLogLevel = exceptionMessageLogLevel;
    }

    public boolean isUseDynamicLogger() {
        return useDynamicLogger;
    }

    public void setUseDynamicLogger(boolean useDynamicLogger) {
        this.useDynamicLogger = useDynamicLogger;
    }
}
