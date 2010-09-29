package org.sc.obo.annotations;

import java.util.*;
import java.lang.reflect.*;

public class OntologyAnnotationParser {

	public static String unCamelCase(String str) { 
		return unCamelCase(str, " ");
	}
	
	public static String unCamelCase(String str, String spacer) { 
		Set<Integer> wordBoundaries = new TreeSet<Integer>();
		if(str.length() > 0) {
			StringBuilder sb =new StringBuilder();
			wordBoundaries.add(0);
			for(int i = 1; i < str.length(); i++) { 
				if(Character.isUpperCase(str.charAt(i)) && 
					!Character.isUpperCase(str.charAt(i-1))) { 
					
					wordBoundaries.add(i);
				}
			}

			wordBoundaries.add(str.length());
			Integer[] array = wordBoundaries.toArray(new Integer[0]);
			for(int i = 0; i < array.length-1; i++) { 
				String word = str.substring(array[i], array[i+1]).toLowerCase();
				if(sb.length() > 0) { sb.append(spacer); }
				sb.append(word);
			}
			
			return sb.toString();
			
		} else { 
			return str;
		}
	}
	
	public static boolean isSubclass(Class c1, Class c2) { 
		return c2.isAssignableFrom(c1);
	}
	
	/**
	 * 
	 * @param cls
	 * @param fieldName
	 * @param type
	 * @return A static, public field with the given name.
	 * @throws NoSuchFieldException
	 */
	protected Field findPublicField(Class cls, String fieldName, Class type) throws NoSuchFieldException { 
		Field field = cls.getField(fieldName);
		int mod = field.getModifiers();
		if(!Modifier.isPublic(mod) || !Modifier.isStatic(mod)) { 
			throw new NoSuchFieldException(fieldName);
		}
		if(!isSubclass(field.getType(), type)) { 
			throw new NoSuchFieldException(field.getType().getCanonicalName());			
		}
		return field;
	}
	
	/**
	 * 
	 * @param cls
	 * @param methodName
	 * @param type
	 * @return A static, public accessor method with the given name.
	 * @throws NoSuchMethodException
	 */
	protected Method findPublicAccessor(Class cls, String methodName, Class type) throws NoSuchMethodException { 
		Method method = cls.getDeclaredMethod(methodName);
		int mod = method.getModifiers();
		if(!Modifier.isPublic(mod) || !Modifier.isStatic(mod)) { 
			throw new NoSuchMethodException(methodName);
		}
		if(!isSubclass(method.getReturnType(), type)) { 
			throw new NoSuchMethodException(method.getReturnType().getCanonicalName());			
		}
		return method;
	}
	
	protected <T> T[] getClassValueSet(Class cls, String name, Class<T> type) { 
		T[] array = (T[])Array.newInstance(type, 0);
		Class arrayType = array.getClass();
		try {
			Field field = findPublicField(cls, name, arrayType);
			return (T[])field.get(cls);

		} catch (NoSuchFieldException e) {
			try {
				Method method = findPublicAccessor(cls, name, arrayType);
				return (T[])method.invoke(cls);
				
			} catch (NoSuchMethodException e1) {
				return array;
				
			} catch (InvocationTargetException e1) {
				throw new IllegalArgumentException(e1);
			} catch (IllegalAccessException e1) {
				throw new IllegalArgumentException(e1);
			}
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	protected <T> T getClassValue(Class cls, String name, Class<T> type) {
		try {
			Field field = findPublicField(cls, name, type);
			return (T)field.get(cls);

		} catch (NoSuchFieldException e) {
			try {
				Method method = findPublicAccessor(cls, name, type);
				return (T)method.invoke(cls);
				
			} catch (NoSuchMethodException e1) {
				throw new OBOTermValueException(name);
				
			} catch (InvocationTargetException e1) {
				throw new IllegalArgumentException(e1);
			} catch (IllegalAccessException e1) {
				throw new IllegalArgumentException(e1);
			}
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	protected String getTermField(Class cls, String fieldName) {
		if(!cls.isAnnotationPresent(org.sc.obo.annotations.Term.class)) { 
			throw new IllegalArgumentException("Annotations for " + cls.getCanonicalName() + ": " + Arrays.asList(cls.getAnnotations())); 			
		}
		Term termAnnotation = (Term)cls.getAnnotation(org.sc.obo.annotations.Term.class);
		
		try {
			Method termAccessor = termAnnotation.getClass().getMethod(fieldName);
			if(!isSubclass(termAccessor.getReturnType(), String.class)) { 
				throw new IllegalArgumentException(fieldName);
			}
			
			return (String)termAccessor.invoke(termAnnotation);
			
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
