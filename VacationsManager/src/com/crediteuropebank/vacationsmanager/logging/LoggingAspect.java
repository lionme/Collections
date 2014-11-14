package com.crediteuropebank.vacationsmanager.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomValidationException;

/**
 * This class represents aspect that is used for logging each method call of the application.
 * It Executes logging with TRACE level.
 * 
 * @author dimas
 *
 */
@Component
@Aspect
@Order(value=2)
public class LoggingAspect {

	/**
	 * This method represents around advice that executes each method call logging.
	 * 
	 * @param joinPoint
	 * @return the {@link Object} that represents return value of the method wrapped in 
	 * 					around advice.
	 * @throws Throwable
	 */
	@Around("execution(* com.crediteuropebank.vacationsmanager.server..*.*(..)) " +
			"&& !execution(* com.crediteuropebank.vacationsmanager.server.service.*.*(..))")// && @annotation(com.raulraja.util.aop.profile.Profile)
	public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable{
		final Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass().getName());
		Object retVal = null;

		try {
			StringBuffer startMessageStringBuffer = new StringBuffer();

			startMessageStringBuffer.append("Start method ");
			startMessageStringBuffer.append('"').append(joinPoint.getSignature().getName()).append('"');
			startMessageStringBuffer.append("(");

			Object[] args = joinPoint.getArgs();
			for (int i = 0; i < args.length; i++) {
				startMessageStringBuffer.append(args[i]).append(",");
			}
			if (args.length > 0) {
				startMessageStringBuffer.deleteCharAt(startMessageStringBuffer.length() - 1);
			}

			startMessageStringBuffer.append(")");

			logger.trace(startMessageStringBuffer.toString());

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			retVal = joinPoint.proceed();

			stopWatch.stop();

			StringBuffer endMessageStringBuffer = new StringBuffer();
			endMessageStringBuffer.append("Finish method ");
			endMessageStringBuffer.append('"').append(joinPoint.getSignature().getName()).append('"');
			endMessageStringBuffer.append("(..); execution time: ");
			endMessageStringBuffer.append(stopWatch.getTotalTimeMillis());
			endMessageStringBuffer.append(" ms;");

			logger.trace(endMessageStringBuffer.toString());
		} catch (Throwable ex) {
			logError(ex, logger, joinPoint);
			
			throw ex;
		}

		return retVal;
	}
	
	/**
	 * This advice is used for logging remote method calls.
	 * 
	 * @param joinPoint
	 * @return the {@link Object} that represents return value of the method wrapped in 
	 * 					around advice.
	 * @throws Throwable
	 */
	@Around("execution(* com.crediteuropebank.vacationsmanager.server.service.*.*(..))")
	public Object logManagerPackageMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		final Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass().getName());
		Object retVal = null;
		
		try {
			StringBuffer startMessageStringBuffer = new StringBuffer();

			startMessageStringBuffer.append("\n\t");
			startMessageStringBuffer.append("Remote method call from client.");
			startMessageStringBuffer.append("\n\t");
			startMessageStringBuffer.append("Remote service method name: ");
			startMessageStringBuffer.append('"').append(joinPoint.getSignature().getName()).append('"');
			startMessageStringBuffer.append("\n\t");
			startMessageStringBuffer.append("Method arguments: \n\t");

			Object[] args = joinPoint.getArgs();
			for (int i = 0; i < args.length; i++) {
				startMessageStringBuffer.append(args[i]).append(",").append('\n').append('\t').append('\t');
			}
			if (args.length > 0) {
				startMessageStringBuffer.delete(startMessageStringBuffer.length() - 4, startMessageStringBuffer.length());
			} else {
				startMessageStringBuffer.append("No arguments");
			}

			logger.info(startMessageStringBuffer.toString());

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			retVal = joinPoint.proceed();

			stopWatch.stop();

			StringBuffer endMessageStringBuffer = new StringBuffer();
			endMessageStringBuffer.append("\n\t");
			endMessageStringBuffer.append("Remote method ");
			endMessageStringBuffer.append('"').append(joinPoint.getSignature().getName()).append('"');
			endMessageStringBuffer.append(" call is successfully finished; ");
			endMessageStringBuffer.append("\n\t");
			endMessageStringBuffer.append("execution time: ");
			endMessageStringBuffer.append(stopWatch.getTotalTimeMillis());
			endMessageStringBuffer.append(" ms;");

			logger.info(endMessageStringBuffer.toString());
		} catch (Throwable ex) {
			logError(ex, logger, joinPoint);
			
			throw ex;
		}
		
		return retVal;
	}
	
	/**
	 * This method is used for logging info about exception.
	 * 
	 * @param ex - {@link Throwable} subclass.
	 */
	private void logError(final Throwable ex, final Logger logger, 
			final ProceedingJoinPoint joinPoint) {
		StringBuffer errorMessageStringBuffer = new StringBuffer();
		if (ex instanceof CustomValidationException) {
			errorMessageStringBuffer.append("Validation failed.");
			errorMessageStringBuffer.append("Validation errors: \n");
			CustomValidationException exception = (CustomValidationException)ex;
			int count = 1;
			for (String errorMessage: exception.getErrorMessages()) {
				errorMessageStringBuffer.append("       ");
				errorMessageStringBuffer.append(count);
				errorMessageStringBuffer.append(") "); 
				errorMessageStringBuffer.append(errorMessage);
				errorMessageStringBuffer.append(";\n");
				
				count ++;
			}
			logger.warn(errorMessageStringBuffer.toString());
		} else if (ex instanceof CustomMessageException) {
			errorMessageStringBuffer.append("Server warning message: ");
			
			CustomMessageException exception = (CustomMessageException)ex;
			errorMessageStringBuffer.append(exception.getMessage());
			logger.warn(errorMessageStringBuffer.toString());
		} else {		
			errorMessageStringBuffer.append("Error in method ");
			errorMessageStringBuffer.append(joinPoint.getSignature().getName());
			errorMessageStringBuffer.append("() occured:"); 
			logger.error(errorMessageStringBuffer.toString(), ex);
		}
	}
}
