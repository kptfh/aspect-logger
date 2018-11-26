package aspect.logger;

import aspect.logger.properties.LoggedInterceptorProperties;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.aop.interceptor.AbstractTraceInterceptor;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.core.Constants;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static aspect.logger.LogHelper.isLogLevelEnabled;
import static aspect.logger.LogHelper.log;

public class CustomizableLoggedInterceptor extends AbstractTraceInterceptor {

    public static final String PLACEHOLDER_METHOD_NAME = "$[methodName]";
    public static final String PLACEHOLDER_ARGUMENTS = "$[arguments]";
    public static final String PLACEHOLDER_RETURN_VALUE = "$[returnValue]";
    public static final String PLACEHOLDER_INVOCATION_TIME = "$[invocationTime]";


    public static final String DEFAULT_ENTER_MESSAGE = "Started " + PLACEHOLDER_METHOD_NAME + "(" + PLACEHOLDER_ARGUMENTS + ")";
    public static final String DEFAULT_EXIT_MESSAGE = "Finished " + PLACEHOLDER_METHOD_NAME + "(" + PLACEHOLDER_ARGUMENTS + "), " +
            "returned " + PLACEHOLDER_RETURN_VALUE;
    public static final String DEFAULT_EXCEPTION_MESSAGE = "Exception in " + PLACEHOLDER_METHOD_NAME + "(" + PLACEHOLDER_ARGUMENTS + ")";

    protected Function<TraceData, String> enterMessageLambda;
    protected LogLevel enterMessageLogLevel;

    protected Function<TraceData, String> exitMessageLambda;
    protected LogLevel exitMessageLogLevel;

    protected Function<TraceData, String> exceptionMessageLambda;
    protected LogLevel exceptionMessageLogLevel;

    public CustomizableLoggedInterceptor(LoggedInterceptorProperties properties){
        setEnterMessage(properties.getEnterMessage());
        setEnterMessageLogLevel(properties.getEnterMessageLogLevel());
        setExitMessage(properties.getExitMessage());
        setExitMessageLogLevel(properties.getExitMessageLogLevel());
        setExceptionMessage(properties.getExceptionMessage());
        setExceptionMessageLogLevel(properties.getExceptionMessageLogLevel());
        setUseDynamicLogger(properties.isUseDynamicLogger());
    }

    public void setEnterMessage(String enterMessage) {
        checkEnterMessage(enterMessage);
        this.enterMessageLambda = buildLogMessageLambda(enterMessage);
    }

    public void setExitMessage(String exitMessage) {
        checkExitMessage(exitMessage);
        this.exitMessageLambda = buildLogMessageLambda(exitMessage);
    }

    public void setExceptionMessage(String exceptionMessage) {
        checkExceptionMessage(exceptionMessage);
        this.exceptionMessageLambda = buildLogMessageLambda(exceptionMessage);
    }

    public void setEnterMessageLogLevel(LogLevel enterMessageLogLevel) {
        validateLogLevel(enterMessageLogLevel);
        this.enterMessageLogLevel = enterMessageLogLevel;
    }

    public void setExitMessageLogLevel(LogLevel exitMessageLogLevel) {
        validateLogLevel(exitMessageLogLevel);
        this.exitMessageLogLevel = exitMessageLogLevel;
    }

    public void setExceptionMessageLogLevel(LogLevel exceptionMessageLogLevel) {
        validateLogLevel(exceptionMessageLogLevel);
        this.exceptionMessageLogLevel = exceptionMessageLogLevel;
    }

    @Override
    protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            logEnterMessage(invocation, logger);

            Object returnValue = invocation.proceed();
            logExitMessage(invocation, logger, returnValue, startTime);

            return returnValue;
        } catch (Throwable ex) {
            logExceptionMessage(invocation, logger, ex, startTime);

            throw ex;
        }
    }

    protected void logEnterMessage(MethodInvocation invocation, Log logger) {
        if (isLogLevelEnabled(logger, enterMessageLogLevel)) {
            String enterMessage = enterMessageLambda.apply(new TraceData(invocation, null, -1));
            log(logger, enterMessageLogLevel, enterMessage, null);
        }
    }

    protected void logExitMessage(MethodInvocation invocation, Log logger, Object returnValue, long startTime) {
        if (isLogLevelEnabled(logger, exitMessageLogLevel)) {
            long executionTime = System.currentTimeMillis() - startTime;
            TraceData exitTraceData = new TraceData(invocation, returnValue, executionTime);
            String exitMessage = exitMessageLambda.apply(exitTraceData);
            log(logger, exitMessageLogLevel, exitMessage, null);
        }
    }

    protected void logExceptionMessage(MethodInvocation invocation, Log logger, Throwable throwable, long startTime) {
        if (isLogLevelEnabled(logger, exceptionMessageLogLevel)) {
            long executionTime = System.currentTimeMillis() - startTime;
            TraceData exceptionTraceData = new TraceData(invocation, null, executionTime);
            String exceptionMessage = exceptionMessageLambda.apply(exceptionTraceData);
            log(logger, exceptionMessageLogLevel, exceptionMessage, throwable);
        }
    }

    @Override
    protected boolean isLogEnabled(Log logger) {
        return true;
    }


    private static Function<TraceData, String> buildLogMessageLambda(String message) {
        List<Function<TraceData, String>> chunks = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(message);
        int previousMatchEnd = 0;
        while (matcher.find()) {
            String textChunk = message.substring(previousMatchEnd, matcher.start());
            chunks.add(data -> textChunk);

            String placeholder = matcher.group();
            if (PLACEHOLDER_METHOD_NAME.equals(placeholder)) {
                chunks.add(data -> data.methodInvocation.getMethod().getName());
            } else if (PLACEHOLDER_ARGUMENTS.equals(placeholder)) {
                chunks.add(data -> StringUtils.arrayToCommaDelimitedString(data.methodInvocation.getArguments()));
            } else if (PLACEHOLDER_RETURN_VALUE.equals(placeholder)) {
                chunks.add(data -> {
                    if (data.methodInvocation.getMethod().getReturnType() == void.class) {
                        return "void";
                    } else if (data.returnValue == null) {
                        return "null";
                    } else {
                        return data.returnValue.toString();
                    }
                });
            } else if (PLACEHOLDER_INVOCATION_TIME.equals(placeholder)) {
                chunks.add(data -> Long.toString(data.executionTime));
            } else {
                throw new IllegalArgumentException("Unknown placeholder [" + placeholder + "]");
            }
            previousMatchEnd = matcher.end();
        }
        String textChunk = message.substring(previousMatchEnd, message.length());
        chunks.add(data -> textChunk);

        return traceData -> chunks.stream().map(chunk -> chunk.apply(traceData)).collect(Collectors.joining());
    }

    private static class TraceData {

        public final MethodInvocation methodInvocation;
        public final Object returnValue;
        public final long executionTime;

        public TraceData(MethodInvocation methodInvocation, Object returnValue, long executionTime) {
            this.methodInvocation = methodInvocation;
            this.returnValue = returnValue;
            this.executionTime = executionTime;
        }
    }

    protected void validateLogLevel(LogLevel logLevel) {
        Assert.notNull(logLevel,"Log level is null.");
    }

    protected void checkEnterMessage(String enterMessage) throws IllegalArgumentException {
        Assert.hasText(enterMessage, "enterMessage must not be empty");
        checkForInvalidPlaceholders(enterMessage);
        Assert.doesNotContain(enterMessage, PLACEHOLDER_RETURN_VALUE,
                "enterMessage cannot contain placeholder " + PLACEHOLDER_RETURN_VALUE);
        Assert.doesNotContain(enterMessage, PLACEHOLDER_INVOCATION_TIME,
                "enterMessage cannot contain placeholder " + PLACEHOLDER_INVOCATION_TIME);
    }

    protected void checkExitMessage(String exitMessage) {
        Assert.hasText(exitMessage, "exitMessage must not be empty");
        checkForInvalidPlaceholders(exitMessage);
    }

    protected void checkExceptionMessage(String exceptionMessage) {
        Assert.hasText(exceptionMessage, "exceptionMessage must not be empty");
        checkForInvalidPlaceholders(exceptionMessage);
        Assert.doesNotContain(exceptionMessage, PLACEHOLDER_RETURN_VALUE,
                "exceptionMessage cannot contain placeholder " + PLACEHOLDER_RETURN_VALUE);
    }

    private static final Pattern PATTERN = Pattern.compile("\\$\\[\\p{Alpha}+\\]");
    private static final Set<Object> ALLOWED_PLACEHOLDERS =
            new Constants(CustomizableTraceInterceptor.class).getValues("PLACEHOLDER_");

    private static void checkForInvalidPlaceholders(String message) throws IllegalArgumentException {
        Matcher matcher = PATTERN.matcher(message);
        while (matcher.find()) {
            String match = matcher.group();
            if (!ALLOWED_PLACEHOLDERS.contains(match)) {
                throw new IllegalArgumentException("Placeholder [" + match + "] is not valid");
            }
        }
    }
}
