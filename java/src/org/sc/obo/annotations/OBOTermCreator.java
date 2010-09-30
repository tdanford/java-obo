package org.sc.obo.annotations;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sc.obo.OBOOntology;
import org.sc.obo.OBOStanza;
import org.sc.obo.OBOTerm;
import org.sc.obo.OBOValue;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

public class OBOTermCreator {
	
	public static void main(String[] args) { 
		try {
			OBOTermCreator creator = new OBOTermCreator();
			Class cls = creator.createTerm("request_0010",
					"foo bar", 
					"a test term", null, 
					new Class[] { ProteinSite.class }, null, null);
			
			OBOAnnotationParser obo = new OBOAnnotationParser();
			System.out.println(obo.stanza(cls));
			
			System.out.println(new JavaExporter().export(cls));
			
		} catch (CannotCompileException e) {
			e.printStackTrace(System.err);
		}
	}
	
	private Map<String,Class> created;
	
	public OBOTermCreator() {
		created = new TreeMap<String,Class>();
	}
	
	public Class createTerm(OBOOntology ontology, OBOTerm termStanza) throws CannotCompileException {

		String id = termStanza.id();
		if(created.containsKey(id)) { 
			return created.get(id);
		}

		String name = termStanza.getName();
		String def = termStanza.values("def").get(0).getValue();
		
		List<OBOValue> isaValues = termStanza.values("is_a");
		Class[] is_a = new Class[isaValues.size()];
		for(int i = 0; i < isaValues.size(); i++) { 
			OBOValue value = isaValues.get(i);
			String valueStr = value.getValue();
			String isaID = valueStr.split("!")[0].trim();
			OBOTerm isaTerm = (OBOTerm)ontology.getStanza(isaID);
			is_a[i] = createTerm(ontology, isaTerm);
		}
		
		Pattern relPattern = Pattern.compile("^\\s*([^\\s]+)\\s+(.*)\\s*!\\s*(.*)$");
		
		List<OBOValue> relValues = termStanza.values("relationship");
		Class[] relTypes = new Class[relValues.size()];
		String[] relTypedefs = new String[relValues.size()];
		for(int i = 0; i < relValues.size(); i++) { 
			OBOValue relValue = relValues.get(i);
			Matcher relMatcher = relPattern.matcher(relValue.getValue());
			if(relMatcher.matches()) { 
				OBOTerm relTerm = (OBOTerm)ontology.getStanza(relMatcher.group(2));
				relTypes[i] = createTerm(ontology, relTerm);
				relTypedefs[i] = relMatcher.group(1);
			}
		}

		return createTerm(id, name, def, null, is_a, relTypes, relTypedefs);
	}

	public Class createTerm(
			String id, 
			String name, 
			String def, 
			String[] comments, 
			Class[] is_a, 
			Class[] relTypes, 
			String[] relTypedefs) throws CannotCompileException {
		
		if(created.containsKey(id)) { 
			return created.get(id);
		}

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

        ClassFile ccFile = cc.getClassFile();
        ConstPool constpool = ccFile.getConstPool();

        Annotation termAnnotation = new Annotation("org.sc.obo.annotations.Term", constpool);
        Annotation relAnnotation = new Annotation("org.sc.obo.annotations.Relates", constpool);
        //annot.addMemberValue("value", new IntegerMemberValue(ccFile.getConstPool(), 0));

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
        
        if(relTypes != null && relTypedefs != null) { 
        	if(relTypes.length != relTypedefs.length) { throw new IllegalArgumentException(); }
        	for(int i = 0; i < relTypes.length; i++) { 
        		try {
					CtClass relTypeClass = cp.get(relTypes[i].getCanonicalName());
					CtMethod relMethod = new CtMethod(relTypeClass, relTypedefs[i], new CtClass[]{}, cc);
					relMethod.setModifiers(Modifier.PUBLIC);
					
					AnnotationsAttribute annotations = 
						(AnnotationsAttribute) relMethod.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
					annotations.addAnnotation(relAnnotation);
			        
			        cc.addMethod(relMethod);
			        
				} catch (NotFoundException e) {
        			throw new IllegalArgumentException(e);
				}
        	}
        }

        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        attr.addAnnotation(termAnnotation);
        ccFile.addAttribute(attr);
        
        Class c = cc.toClass();
        created.put(id, c);
        
		return c;
	}
}
