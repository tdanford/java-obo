package org.sc.obo;

import java.io.*;
import java.util.*;

public class OBOOntology extends OBOStanza {

	private Map<String,OBOStanza> stanzas;
	
	public OBOOntology() { 
		super("ontology");
		stanzas = new TreeMap<String,OBOStanza>();
	}
	
	public void addOBOStanza(OBOStanza s) {
		assert s != null;
		String id = s.id();
		if(id == null || stanzas.containsKey(id)) { 
			throw new IllegalArgumentException(String.format("ID: \"%s\"", id));
		}
		stanzas.put(id, s);
	}
	
	public void add(OBOOntology ont) { 
		super.add(ont);
		for(String key : ont.stanzas.keySet()) { 
			if(!stanzas.containsKey(key)) { 
				stanzas.put(key, (OBOStanza)ont.stanzas.get(key).clone());
			} else { 
				stanzas.get(key).add(ont.stanzas.get(key));
			}
		}
	}
	
	public void print(PrintWriter w) { 
		for(String id : stanzas.keySet()) { 
			stanzas.get(id).print(w);
		}
	}

	public Collection<OBOStanza> getStanzas() {
		return stanzas.values();
	}
}
