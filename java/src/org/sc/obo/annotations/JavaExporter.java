package org.sc.obo.annotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JavaExporter extends OBOAnnotationParser implements Exporter {
	
	public String export(Class cls) {
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		JavaWriter java = new JavaWriter(pw);
		
		pw.println("@Term");
		java.beginInterface(0, camelCase(name(cls), "\\s+"), cls.getInterfaces());
		
		java.field(Modifier.PUBLIC | Modifier.STATIC, String.class, "id", id(cls));
		java.field(Modifier.PUBLIC | Modifier.STATIC, String.class, "name", name(cls));
		java.field(Modifier.PUBLIC | Modifier.STATIC, String.class, "def", def(cls));
		
		for(Method method : findRelationships(cls)) {
			pw.println("@Property");
			java.methodDeclaration(method.getModifiers(), method.getReturnType(), method.getName(),
					null, null, null);
		}
		
		java.endInterface();
		
		//return String.format("%s\n%s", java.getImports(), writer.toString());
		return writer.toString();
	} 
}