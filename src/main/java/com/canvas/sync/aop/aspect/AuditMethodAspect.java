package com.canvas.sync.aop.aspect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
@Slf4j
@Component
public class AuditMethodAspect {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Around("execution(* *(..)) && @annotation(com.canvas.sync.aop.annotation.Audit)")
    public Object logMethods(ProceedingJoinPoint jp) throws Throwable {
        var method = ((MethodSignature) jp.getSignature()).getMethod();
        String methodName = "[" + method.getDeclaringClass().getSimpleName() + "#" + method.getName() + "]";
        logMethodInvocationAndParameters(jp, methodName);

        var startTime = Instant.now();
        Object result;

        try {
            result = jp.proceed(jp.getArgs());
        } catch (Throwable t) {
            logExceptions(t, methodName);
            throw t;
        }
        var endTime = Instant.now();

        log.debug("{} execution time: {} ms", methodName, startTime.until(endTime, ChronoUnit.MILLIS));
        log.debug("{} return: {}\n", methodName, gson.toJson(result));

        return result;
    }

    @Around("execution(* com.canvas.sync.dao.store.BaseStore.save(..))")
    public void logSaveEntity(ProceedingJoinPoint jp) throws Throwable {
        log.debug("saving entity: {}", jp.getArgs());
        jp.proceed();
    }

    // aspect for monitoring all save-operations
    @Around("execution(* com.canvas.sync.dao.store.BaseStore.saveAll(..))")
    public void logBatchSaveEntity(ProceedingJoinPoint jp) throws Throwable {
        log.debug("saving batch of entities: {}", jp.getArgs());
        jp.proceed();
    }

    private void logMethodInvocationAndParameters(ProceedingJoinPoint jp, String methodName) {
        String[] argNames = ((MethodSignature) jp.getSignature()).getParameterNames();
        Object[] values = jp.getArgs();
        Map<String, Object> params = IntStream.range(0, argNames.length).boxed()
                .collect(Collectors.toMap(i -> argNames[i], i -> values[i]));

        log.debug("{} invocation with params: {}", methodName, !params.isEmpty() ? gson.toJson(params) : "no params");
    }

    private void logExceptions(Throwable t, String methodName) {
        log.debug("{} exception caught message: {} ; stacktrace: {}", methodName, t.getMessage(),
                 Arrays.stream(t.getStackTrace()).limit(5).map(el -> el.toString() + "\n").collect(Collectors.joining()));
    }

}