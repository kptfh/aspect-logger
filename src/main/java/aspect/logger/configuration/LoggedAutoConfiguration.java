package aspect.logger.configuration;

import aspect.logger.CustomizableLoggedInterceptor;
import aspect.logger.properties.LoggedInterceptorProperties;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "logging.interceptor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggedAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "logging.interceptor")
    public LoggedInterceptorProperties loggedInterceptorProperties() {
        return new LoggedInterceptorProperties();
    }

    @Bean
    public CustomizableLoggedInterceptor loggedInterceptor(LoggedInterceptorProperties properties) {
        return new CustomizableLoggedInterceptor(properties);
    }

    @Bean
    public Advisor loggedAdvisor(CustomizableLoggedInterceptor loggedInterceptor) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        String expresion = "(execution(public * *(..)) && !execution(* java.lang.Object.*(..))" +
                "&& within(@aspect.logger.Logged *))" +
                "|| @annotation(aspect.logger.Logged)";
        pointcut.setExpression(expresion);
        return new DefaultPointcutAdvisor(pointcut, loggedInterceptor);
    }
}
