package com.kmong.aop.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class RequestFlowLogger {

    private static final ThreadLocal<Deque<String>> callStack = ThreadLocal.withInitial(LinkedList::new);
    private static final ThreadLocal<String> requestUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString().substring(0, 8));

    @Around("execution(* com.example.bobivet.interfaces.api..*Controller.*(..)) || " +
            "execution(* com.example.bobivet.application..*Facade.*(..)) || " +
            "execution(* com.example.bobivet.domain..*Service.*(..)) || " +
            "execution(* com.example.bobivet.domain..*Repository.*(..)) || " +
            "execution(* com.example.bobivet.domain..*RepositoryImpl.*(..))")
    public Object logFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        Boolean exceptionThrown = false;

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);

        String layer = getLayer(simpleClassName);
        String callInfo = layer + " " + simpleClassName + "." + methodName + "()";

        callStack.get().addLast(callInfo);
        log.info(currentIndent() + "->" + callInfo);

        try {
            Object result = joinPoint.proceed();
            log.info(currentIndent() + "<-" + callInfo);
            return result;
        } catch (Throwable ex) {
            exceptionThrown = true;
            log.error(currentIndent() + "X ERROR IN " + callInfo + ": " + ex.getMessage());
            throw ex;
        } finally {
            callStack.get().removeLast();
            if (callStack.get().isEmpty()) {
                callStack.remove();
            }
        }
    }

    private String getLayer(String simpleClassName) {
        if (simpleClassName.endsWith("Controller")) return "[CONTROLLER]";
        if (simpleClassName.endsWith("Facade")) return "[FACADE]";
        if (simpleClassName.endsWith("Service")) return "[SERVICE]";
        if (simpleClassName.endsWith("Repository") || simpleClassName.endsWith("RepositoryImpl")) return "[REPOSITORY]";
        return "[UNKNOWN]";
    }

    public static String currentIndent() {
        String uuidPrefix = "[" + requestUUID.get() + "] ";
        String indent = "  ".repeat(Math.max(0, callStack.get().size() - 1));
        return uuidPrefix + indent;
    }

    public static String getIndent() {
        return "  ".repeat(Math.max(0, callStack.get().size()));
    }

    public static String getCurrentUUID() {
        return requestUUID.get();
    }

    public static void clearUUID() {
        requestUUID.remove();
    }
}
