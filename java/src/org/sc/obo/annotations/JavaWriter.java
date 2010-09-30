package org.sc.obo.annotations;

import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

public class JavaWriter {
	
	private static String tabString = "\t";
	
	public static final Integer CLASS = 0;
	public static final Integer INTERFACE = 1;
	public static final Integer METHOD = 2;
	
	private PrintWriter writer;
	private LinkedList<Integer> blockTypes;
	
	public JavaWriter(PrintWriter w) { 
		writer = w;
		blockTypes = new LinkedList<Integer>();
	}
	
	private boolean printIfPublic(int mod) { 
		boolean present = Modifier.isPublic(mod);
		if(present) { writer.print("public "); }
		return present;
	}
	
	private boolean printIfPrivate(int mod) { 
		boolean present = Modifier.isPrivate(mod);
		if(present) { writer.print("private "); }
		return present;
	}
	
	private boolean printIfProtected(int mod) { 
		boolean present = Modifier.isProtected(mod);
		if(present) { writer.print("protected "); }
		return present;
	}
	
	private boolean printIfAccess(int mod) { 
		return printIfPublic(mod) || printIfPrivate(mod) || printIfProtected(mod);
	}
	
	public String indent() { 
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < blockTypes.size(); i++) { 
			sb.append(tabString);
		}
		return sb.toString();
	}
	
	private boolean printIfStatic(int mod) { 
		boolean present = Modifier.isStatic(mod);
		if(present) { writer.print("static "); }
		return present;
	}
	
	private void doIndent() { 
		writer.print(indent());
	}
	
	public void beginInterface(int modifiers, String interfaceName, Class[] interfaces) {
		doIndent();
		printIfAccess(modifiers);
		printIfStatic(modifiers);
		writer.print("interface " + interfaceName + " ");
		
		if(interfaces != null && interfaces.length != 0) { 
			writer.print("extends ");
			for(int i = 0; i < interfaces.length; i++) { 
				if(i > 0) { writer.print(", "); }
				writer.print(interfaces[i].getSimpleName());
			}
			writer.print(" ");
		}
		
		writer.println("{");
		blockTypes.addFirst(INTERFACE);
	}
	
	public void endInterface() {
		endWithType(INTERFACE);
	}
	
	public void beginClass(int modifiers, String className, Class superClass, Class[] interfaces) { 
		doIndent();
		printIfAccess(modifiers);
		printIfStatic(modifiers);
		writer.print("class " + className + " ");
		
		if(superClass != null) { 
			writer.print(" extends " + superClass.getSimpleName() + " ");
		}
		
		if(interfaces != null && interfaces.length != 0) { 
			writer.print("implements ");
			for(int i = 0; i < interfaces.length; i++) { 
				if(i > 0) { writer.print(", "); }
				writer.print(interfaces[i].getSimpleName());
			}
			writer.print(" ");
		}
		
		writer.println("{");
		blockTypes.addFirst(CLASS);		
	}
	
	public void endClass() { 
		endWithType(CLASS);
	}
	
	private static boolean isSubclass(Class c1, Class c2) { 
		return c2.isAssignableFrom(c1);
	}
	
	public void field(int modifiers, Class type, String name, String init) { 
		doIndent();
		printIfAccess(modifiers);
		printIfStatic(modifiers);
		
		writer.print(type.getSimpleName() + " ");
		writer.print(name);
		
		if(init != null) { 
			if(isSubclass(type, String.class)) { 
				writer.print(String.format(" = \"%s\";", init.replace("\"", "\\\"")));
			} else { 
				writer.print(String.format(" = %s;", init));
			}
		}
		
		writer.println();
	}
	
	private void printMethodLine(int modifiers, Class returnType, String methodName, Class[] argTypes, String[] argNames, Class[] throwsList) { 
		doIndent();
		printIfAccess(modifiers);
		printIfStatic(modifiers);
		writer.print(returnType.getSimpleName() + " " + methodName);
		writer.print("(");
		
		if(argTypes != null && argTypes.length > 0) { 
			if(argNames == null || argNames.length != argTypes.length) { 
				throw new IllegalArgumentException();
			}
			
			for(int i = 0; i < argNames.length; i++) { 
				if(i > 0) { writer.print(", "); }
				writer.print(String.format("%s %s", argTypes[i].getSimpleName(), argNames[i]));
			}
		}
		
		writer.print(")");
		
		if(throwsList != null && throwsList.length > 0) { 
			writer.print(" throws ");
			for(int i = 0; i < throwsList.length; i++) { 
				if(i > 0) { writer.print(", "); }
				writer.print(throwsList[i].getSimpleName());
			}
		}
	}
		
	public void methodDeclaration(int modifiers, Class returnType, String methodName, Class[] argTypes, String[] argNames, Class[] throwsList) { 
		printMethodLine(modifiers, returnType, methodName, argTypes, argNames, throwsList);
		writer.println(";");
	}
	
	public void beginMethod(int modifiers, Class returnType, String methodName, Class[] argTypes, String[] argNames, Class[] throwsList) { 
		printMethodLine(modifiers, returnType, methodName, argTypes, argNames, throwsList);
		writer.println(" {");
		blockTypes.addFirst(METHOD);
	}
	
	private void endWithType(Integer type) { 
		if(blockTypes.isEmpty() || !blockTypes.removeFirst().equals(type)) { 
			throw new IllegalStateException();
		}
		doIndent();
		writer.println("}");						
	}
	
	public void endMethod() { 
		endWithType(METHOD);
	}
}
