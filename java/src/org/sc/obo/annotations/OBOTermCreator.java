package org.sc.obo.annotations;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

public class OBOTermCreator {
	
	public static void main(String[] args) { 
		try {
			Class cls = createTerm("request_0010",
					"foo bar", 
					"a test term", null, 
					new Class[] { ProteinSite.class });
			
			OBOAnnotationParser obo = new OBOAnnotationParser();
			System.out.println(obo.stanza(cls));
			
		} catch (CannotCompileException e) {
			e.printStackTrace(System.err);
		}
	}

	public static Class createTerm(
			String id, 
			String name, 
			String def, 
			String[] comments, 
			Class[] is_a) throws CannotCompileException { 

		OBOAnnotationParser obo = new OBOAnnotationParser();
		
		ClassPool cp = ClassPool.getDefault();
		
		CtClass stringClass = null, stringArrayClass = null;
		try {
			stringClass = cp.get("java.lang.String");
			stringArrayClass = cp.get("java.lang.String[]");
		} catch (NotFoundException e) {
			throw new IllegalStateException(e);
		}
		
        CtClass cc = cp.makeInterface(OntologyAnnotationParser.camelCase(name, "\\s+"));
        cc.setModifiers(javassist.Modifier.INTERFACE | javassist.Modifier.PUBLIC);
        
        CtField idField = new CtField(stringClass, "id", cc);
        idField.setModifiers(javassist.Modifier.PUBLIC | javassist.Modifier.STATIC | javassist.Modifier.FINAL);

        CtField nameField = new CtField(stringClass, "name", cc);
        nameField.setModifiers(javassist.Modifier.PUBLIC | javassist.Modifier.STATIC | javassist.Modifier.FINAL);

        CtField defField = new CtField(stringClass, "def", cc);
        defField.setModifiers(javassist.Modifier.PUBLIC | javassist.Modifier.STATIC | javassist.Modifier.FINAL);

        cc.addField(idField, CtField.Initializer.constant(id));
        cc.addField(nameField, CtField.Initializer.constant(name));
        cc.addField(defField, CtField.Initializer.constant(def));
        
        if(is_a != null) { 
        	for(Class superClass : is_a) { 
        		if(!obo.isTerm(superClass)) { 
        			throw new IllegalArgumentException(superClass.getCanonicalName());
        		}
        		try {
        			CtClass superCtClass = cp.get(superClass.getCanonicalName());
        			cc.addInterface(superCtClass);

        		} catch (NotFoundException e) {
        			throw new IllegalArgumentException(e);
        		}
        	}
        }

        ClassFile ccFile = cc.getClassFile();
        ConstPool constpool = ccFile.getConstPool();

        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation annot = new Annotation("org.sc.obo.annotations.Term", constpool);

        //annot.addMemberValue("value", new IntegerMemberValue(ccFile.getConstPool(), 0));
        
        attr.addAnnotation(annot);
        ccFile.addAttribute(attr);
        
        Class c = cc.toClass();
		return c;
	}
}
