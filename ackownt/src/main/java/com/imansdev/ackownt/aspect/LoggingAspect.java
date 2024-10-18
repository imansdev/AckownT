package com.imansdev.ackownt.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Controller Layer Logging
    @Before("execution(* com.imansdev.ackownt.controller..*(..))")
    public void logControllerAccess(JoinPoint joinPoint) {
        logger.info("Entering: {} with arguments: {}", joinPoint.getSignature(),
                joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* com.imansdev.ackownt.controller..*(..))",
            returning = "result")
    public void logControllerResponse(JoinPoint joinPoint, Object result) {
        logger.info("Exiting: {} with result: {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(pointcut = "execution(* com.imansdev.ackownt.controller..*(..))",
            throwing = "ex")
    public void logControllerError(JoinPoint joinPoint, Throwable ex) {
        logger.error("Error in: {} with message: {}", joinPoint.getSignature(), ex.getMessage());
    }

    // Service Layer Logging
    @Before("execution(* com.imansdev.ackownt.service..*(..))")
    public void logServiceAccess(JoinPoint joinPoint) {
        logger.debug("Service method called: {}", joinPoint.getSignature());
    }

    @AfterReturning(pointcut = "execution(* com.imansdev.ackownt.service..*(..))",
            returning = "result")
    public void logServiceResponse(JoinPoint joinPoint, Object result) {
        logger.debug("Service method {} returned: {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(pointcut = "execution(* com.imansdev.ackownt.service..*(..))", throwing = "ex")
    public void logServiceError(JoinPoint joinPoint, Throwable ex) {
        logger.error("Service method {} threw an exception: {}", joinPoint.getSignature(),
                ex.getMessage(), ex);
    }
}
