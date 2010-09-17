package org.sc.obo;

public class OBOTerm extends OBOStanza {
	
	private String name;
	
	public OBOTerm() { 
		super("Term");
		name = null;
	}
	
	public Object clone() { 
		return super.clone(OBOTerm.class);
	}
	
	public String getName() { return name; }
	
	public void addValue(String k, OBOValue v) { 
		super.addValue(k, v);
		
		if(k.equals("name")) { 
			if(name != null) { 
				throw new OBOException(String.format("Term %s given duplicate name %s", 
						name, v.getValue()));
			} else { 
				name = v.getValue();
			}
		}
	}
}
