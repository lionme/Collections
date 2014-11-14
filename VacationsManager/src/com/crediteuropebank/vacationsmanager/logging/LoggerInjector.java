package com.crediteuropebank.vacationsmanager.logging;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * 
 * @author DIMAS
 *
 */
@Component(value="loggerInjector")
public class LoggerInjector implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {

		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, String beanName)
			throws BeansException {
		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {  
			public void doWith(Field field) throws IllegalArgumentException,  
			IllegalAccessException {  
				// make the field accessible if defined private  
				ReflectionUtils.makeAccessible(field);  
				if (field.getAnnotation(InjectLogger.class) != null) {  
					Logger log = LoggerFactory.getLogger(bean.getClass()); 
					field.set(bean, log);  
				}  
			}  
		});  
		return bean;  
	}  

}
