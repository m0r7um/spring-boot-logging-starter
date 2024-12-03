package mortum.t1starter.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Aspect
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("@annotation(mortum.t1starter.aspects.annotations.IncomingPutRequestLogging)")
    public void logPutMethod(JoinPoint joinPoint) {
        List<Object> incomingRequestParams = List.of(joinPoint.getArgs());
        logger.info("""
                        Incoming put request processing by {}
                        Passed arguments: {}""",
                joinPoint.getTarget().getClass(),
                incomingRequestParams
        );
    }

    @Around(value = "@annotation(mortum.t1starter.aspects.annotations.IncomingPostRequestLogging)")
    public Object logPostMethod(ProceedingJoinPoint pjp) {
        List<Object> incomingRequestParams = List.of(pjp.getArgs());
        logger.info("""
                        Incoming post request processing by {}
                        Passed arguments: {}""",
                pjp.getTarget(),
                incomingRequestParams
        );

        Object result;

        try {
            result = pjp.proceed();
            logger.info("Added resource: {}", result);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @AfterThrowing(pointcut = "@annotation(mortum.t1starter.aspects.annotations.ExceptionLogging)", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        logger.info(ex.getClass().toString());
        logger.error("""
                        Exception at {} while executing {} with arguments: {}
                        Sql constraint violation:  {}""",
                joinPoint.getTarget().getClass(),
                joinPoint.getSignature(),
                Arrays.toString(joinPoint.getArgs()),
                ex.getMessage()
        );
    }

    @AfterReturning(value = "@annotation(mortum.t1starter.aspects.annotations.GetMethodLogging)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("""
                        Get method is processed by {}, method {}, with arguments {} successfully.
                        Response will include {}""",
                joinPoint.getTarget().getClass(),
                joinPoint.getSignature(),
                Arrays.toString(joinPoint.getArgs()),
                result == null ? "nothing" : result
        );
    }

    @AfterReturning(value = "@annotation(mortum.t1starter.aspects.annotations.ModifyingOperationLogging)", returning = "result")
    public void modifyingOperationCounter(JoinPoint joinPoint, Object result) {
        logger.info("""
                        Modifying was processed by {}, method {}, with arguments {} successfully.
                        Rows affected: {}""",
                joinPoint.getTarget().getClass(),
                joinPoint.getSignature(),
                Arrays.toString(joinPoint.getArgs()),
                result);
    }
}
