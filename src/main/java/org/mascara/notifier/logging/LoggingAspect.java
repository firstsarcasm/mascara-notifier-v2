package org.mascara.notifier.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

	@Pointcut("@annotation(org.mascara.notifier.logging.LogEntryAndExit)")
	public void executeLogging() {
	}

	@Before("executeLogging()")
	public void logMethodCall(JoinPoint pjp) {
		String className = pjp.getTarget().getClass().getSimpleName();
		Signature signature = pjp.getSignature();

		log.info("Call method '{}.{}', with arguments '{}'", className, signature.getName(), pjp.getArgs());
	}

	@After("executeLogging()")
	public void logAfterMethodCall(JoinPoint pjp) {

		String className = pjp.getTarget().getClass().getSimpleName();
		Signature signature = pjp.getSignature();
		log.info("Exit method '{}.{}' ", className, signature.getName());
	}

}