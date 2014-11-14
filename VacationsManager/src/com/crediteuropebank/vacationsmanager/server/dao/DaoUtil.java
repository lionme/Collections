package com.crediteuropebank.vacationsmanager.server.dao;

import java.lang.reflect.Method;

import com.crediteuropebank.vacationsmanager.shared.domain.BaseDomain;

/**
 * This class contains static methods for setting id and version of the domain object 
 * using reflection. It should be used for setting id and version properties of all 
 * domain objects because they don't have public setters. This is done for securing purposes 
 * so nobody can change version and id outside DAO package (as you can see this class
 * has package visibility).
 * 
 * @author DIMAS
 *
 */
class DaoUtil {
	
	/**
	 * The name of the setter for id.
	 */
	private static final String ID_SETTER_NAME = "setId";
	
	/**
	 * The name of the setter for version.
	 */
	private static final String VERSION_SETTER_NAME = "setVersion";

	/**
	 * <p>This method sets id property for specified domain object.</p> 
	 * 
	 * <i><p>Note that domain object should have private setter for id.</p></i>
	 * 
	 * @param domainObj - domain object for which id property should be set.
	 * @param id - id that should be set for the domain object.
	 * @throws IllegalStateException if some exception occurs during setting of the id.
	 */
	public static void setObjectId(BaseDomain domainObj, long id) {
		try {
			
			//Method method = domainObj.getClass().getMethod(ID_SETTER_NAME, long.class);
			Method method = getMethod(ID_SETTER_NAME, domainObj.getClass());
			method.setAccessible(true);
			
			method.invoke(domainObj, id);
			
		} catch (Exception e) {
			throw new IllegalStateException("Colud not set id of the object.", e);
		} 
	}
	
	/**
	 * <p>This method sets version property for specified domain object.</p>
	 * 
	 * <i><p>Note that domain object should have private setter for version.</p></i>
	 * 
	 * @param domainObj - domain object for which version property should be set.
	 * @param version - version that should be set for the domain object.
	 * @throws IllegalStateException if some exception occurs during setting of the version.
	 */
	public static void setObjectVersion(BaseDomain domainObj, int version) {
		try {
			//Method method = domainObj.getClass().getMethod(VERSION_SETTER_NAME, int.class);
			Method method = getMethod(VERSION_SETTER_NAME, domainObj.getClass());
			method.setAccessible(true);
			
			method.invoke(domainObj, version);
			
		} catch (Exception e) {
			throw new IllegalStateException("Colud not set version of the object.", e);
		} 
	}
	
	/**
	 * Returns method object from class by its name. If method with specified name doesn't exist 
	 * then return null.
	 * 
	 * @param methodName - the name of the method to search for
	 * @param objectClass - the class of the object where to search method in
	 * @return the {@link Method} object from the class that have specified name
	 */
	private static Method getMethod(String methodName, Class<?> objectClass) {
		Method[] classMethods = objectClass.getDeclaredMethods();
		
		for (Method method: classMethods) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		
		/*Class<?> superclass = objectClass.getSuperclass();
		while (superclass != null) {
			getMethod(methodName, superclass);
			
			superclass = superclass.getSuperclass();
		}*/
		
		Class<?> superclass = objectClass.getSuperclass();
		if (superclass != null) {
			return getMethod(methodName, superclass);
		}
		
		// At last, if there is no such method - throw RuntimeException.
		throw new RuntimeException(methodName + " method no found");
	}
	
}
