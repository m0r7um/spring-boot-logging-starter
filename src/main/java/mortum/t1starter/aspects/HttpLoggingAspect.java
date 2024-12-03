package mortum.t1starter.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

@Aspect
public class HttpLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(HttpLoggingAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Level loggingLevel;

    public HttpLoggingAspect(Level loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerPointcut() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void httpRequestMethodsPointcut() {
    }

    @Before("restControllerPointcut() && httpRequestMethodsPointcut()")
    public void logRequestDetails(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();

        Map<String, Object> requestBodyArgs = extractRequestBodyArguments(parameterAnnotations, args);

        log("Handling HTTP Request: Method={}, Args={}",
                signature.getMethod().getName(),
                requestBodyArgs);
    }

    @AfterReturning(value = "restControllerPointcut() && httpRequestMethodsPointcut()", returning = "response")
    public void logResponseDetails(JoinPoint joinPoint, Object response) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log("HTTP Response for Method={}: {}", signature.getMethod().getName(), formatResponse(response));
    }

    private Map<String, Object> extractRequestBodyArguments(Annotation[][] parameterAnnotations, Object[] args) {
        Map<String, Object> requestBodyArgs = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().equals(RequestBody.class)) {
                    requestBodyArgs.put("arg" + i, args[i]);
                }
            }
        }
        return requestBodyArgs;
    }

    private String formatResponse(Object response) {
        try {
            if (response instanceof ResponseEntity<?> responseEntity) {
                return String.format(
                        "Status=%d, Headers=%s, Body=%s",
                        responseEntity.getStatusCode().value(),
                        responseEntity.getHeaders(),
                        objectMapper.writeValueAsString(responseEntity.getBody())
                );
            }
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "Error serializing response: " + e.getMessage();
        }
    }

    private void log(String message, Object... args) {
        logger.atLevel(loggingLevel).log(message, args);
    }
}
