package org.sc.obo.annotations;

import java.util.*;
import java.lang.reflect.*;

public class OBOAnnotationParser extends OntologyAnnotationParser {
	
	public static void main(String[] args) {		
		OBOAnnotationParser obo = new OBOAnnotationParser();
		
		Class c= ModifiedSite.class;
		System.out.println(obo.stanza(c));
	}
	
	public Method[] findRelationships(Class cls) {
		ArrayList<Method> ms = new ArrayList<Method>();
		for(Method m : cls.getDeclaredMethods()) { 
			if(m.isAnnotationPresent(Relates.class)) { 
				ms.add(m);
			}
		}
		return ms.toArray(new Method[0]);
	}
	
	public String relationshipTypedef(Method m) { 
		if(m.isAnnotationPresent(Relates.class)) {
			Relates rel = m.getAnnotation(Relates.class);
			String typedef = rel.value();
			if(typedef == null || typedef.length() == 0) { 
				typedef = unCamelCase(m.getName(), "_");
			}
			return typedef;
		} else { 
			throw new IllegalArgumentException(String.format("Method %s in class %s has annotations %s",
					m.getName(), m.getDeclaringClass().getSimpleName(), 
					Arrays.asList(m.getAnnotations())));
		}
	}
	
	public Class relationshipType(Method m) { 
		if(m.isAnnotationPresent(Relates.class)) {
			Relates rel = m.getAnnotation(Relates.class);
			Class type = m.getReturnType();
			if(type.isArray()) { 
				type = type.getComponentType();
			}
			
			if(!isTerm(type)) { 
				throw new IllegalArgumentException(String.format("Type class %s has annotations %s",
						type.getSimpleName(), 
						Arrays.asList(type.getAnnotations())));
			}
			
			return type; 
			
		} else { 
			throw new IllegalArgumentException(String.format("Method %s in class %s has annotations %s",
					m.getName(), m.getDeclaringClass().getSimpleName(), 
					Arrays.asList(m.getAnnotations())));
		}
	}
	
	public Class[] findImmediateSuperClasses(Class cls) { 
		Set<Class> supers = new LinkedHashSet<Class>();
		if(isTerm(cls)) { 
			Class superClass = cls.getSuperclass();
			if(superClass != null && isTerm(superClass)) {
				supers.add(superClass);
			}
			
			Class[] interfaces = cls.getInterfaces();
			for(int i = 0; i < interfaces.length; i++) { 
				if(isTerm(interfaces[i])) { 
					supers.add(interfaces[i]);
				}
			}
		}
		return supers.toArray(new Class[0]);		
	}
	
	public Class[] findAllSuperClasses(Class cls) { 
		Set<Class> supers = new LinkedHashSet<Class>();
		if(isTerm(cls)) { 
			supers.add(cls);
			for(Class superClass : findImmediateSuperClasses(cls)) { 
				supers.addAll(Arrays.asList(findAllSuperClasses(superClass)));
			}
		}
		return supers.toArray(new Class[0]);
	}
	
	public String name(Class value) {
		try { 
			return getClassValue(value, getTermField(value, "name"), String.class);
		} catch(OBOTermValueException e) { 
			return unCamelCase(value.getSimpleName());
		}
	}

	public String def(Class value) { 
		return getClassValue(value, getTermField(value, "def"), String.class);
	}
	
	public String id(Class value) { 
		return getClassValue(value, getTermField(value, "id"), String.class);
	} 
	
	public String[] comments(Class value) { 
		return getClassValueSet(value, getTermField(value, "comment"), String.class);
	}

	public boolean isTerm(Class t) { 
		return t.isAnnotationPresent(org.sc.obo.annotations.Term.class); 		
	}
	
	private boolean isRelationship(Method m) { 
		return m.isAnnotationPresent(org.sc.obo.annotations.Relates.class);
	}
	
	
	public String oboTag(Class cls) { 
		return String.format("%s ! %s", id(cls), name(cls));
	}
	
	public String stanza(Class cls) { 
		if(!isTerm(cls)) { 
			throw new IllegalArgumentException(cls.getCanonicalName());
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append("[Term]\n");
		sb.append(String.format("id: %s\n", id(cls)));
		sb.append(String.format("name: %s\n", name(cls)));
		sb.append(String.format("def: \"%s\"\n", def(cls)));

		for(String comment : comments(cls)) { 
			sb.append(String.format("comment: \"%s\"\n", comment));
		}
		
		for(Class superClass : findImmediateSuperClasses(cls)) { 
			sb.append(String.format("is_a: %s\n", oboTag(superClass)));
		}
		
		for(Method m : findRelationships(cls)) { 
			sb.append(String.format("relationship: %s %s\n", 
					relationshipTypedef(m),
					oboTag(relationshipType(m))));
		}
		
		return sb.toString();
	}
}
