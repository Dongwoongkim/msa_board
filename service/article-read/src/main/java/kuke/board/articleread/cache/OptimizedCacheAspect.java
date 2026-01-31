package kuke.board.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OptimizedCacheAspect {

    private final OptimizedCacheManager optimizedCacheManager;

    @Around("@annotation(OptimizedCacheable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        OptimizedCacheable optimizedCacheable = findAnnotation(joinPoint);
        return optimizedCacheManager.process(
            optimizedCacheable.type(),
            optimizedCacheable.ttlSeconds(),
            joinPoint.getArgs(),
            findReturnType(joinPoint),
            () -> joinPoint.proceed()
        );
    }

    private OptimizedCacheable findAnnotation(ProceedingJoinPoint joinPoint) {
        Signature sign = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) sign;
        return methodSignature.getMethod().getAnnotation(OptimizedCacheable.class);
    }

    private Class<?> findReturnType(ProceedingJoinPoint joinPoint) {
        Signature sign = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) sign;
        return methodSignature.getReturnType();
    }
}
