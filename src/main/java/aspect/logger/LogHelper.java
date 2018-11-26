package aspect.logger;

import org.apache.commons.logging.Log;

public class LogHelper {

    public static void log(Log logger, LogLevel logLevel, Object message, Throwable t) {
        switch (logLevel) {
            case TRACE:
                logger.trace(message, t);
                break;
            case DEBUG:
                logger.debug(message, t);
                break;
            case INFO:
                logger.info(message, t);
                break;
            case WARN:
                logger.warn(message, t);
                break;
            case ERROR:
                logger.error(message, t);
                break;
            case FATAL:
                logger.fatal(message, t);
                break;
        }
    }

    public static boolean isLogLevelEnabled(Log logger, LogLevel logLevel) {
        switch (logLevel) {
            case TRACE:
                return logger.isTraceEnabled();
            case DEBUG:
                return logger.isDebugEnabled();
            case INFO:
                return logger.isInfoEnabled();
            case WARN:
                return logger.isWarnEnabled();
            case ERROR:
                return logger.isErrorEnabled();
            case FATAL:
                return logger.isFatalEnabled();
        }
        return false;
    }
}
