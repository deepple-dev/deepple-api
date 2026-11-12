package kr.co.deepple.common.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class EventLogContextAspect {

  @Around(
      "@annotation(org.springframework.context.event.EventListener)"
          + " || @annotation(org.springframework.transaction.event.TransactionalEventListener)")
  public Object putEventAction(ProceedingJoinPoint joinPoint) throws Throwable {
    String action =
        LogContext.resolveEventAction(joinPoint.getSignature().getName(), joinPoint.getArgs());
    return proceedWithAction(joinPoint, action);
  }

  @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
  public Object putScheduledAction(ProceedingJoinPoint joinPoint) throws Throwable {
    String action =
        joinPoint.getSignature().getDeclaringType().getSimpleName()
            + "."
            + joinPoint.getSignature().getName();
    return proceedWithAction(joinPoint, action);
  }

  private Object proceedWithAction(ProceedingJoinPoint joinPoint, String action) throws Throwable {
    String previous = LogContext.putEventAction(action);
    try {
      return joinPoint.proceed();
    } finally {
      LogContext.restoreEventAction(previous);
    }
  }
}
