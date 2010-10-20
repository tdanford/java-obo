package org.sc.obo;

import java.io.IOException;
import java.io.StringReader;

import org.junit.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class OBOParserTest { 
	
	@Test 
	public void testParseIndividual() throws IOException {
		OBOParser parser = new OBOParser();
		String term = 
			"[Individual]\n" + 
			"id: foo\n" +
			"name: foo\n";
		
		parser.parse(new StringReader(term));
		
		OBOOntology ontology = parser.getOntology();
		
		assertThat(ontology, is(not(nullValue())));
		assertThat(ontology.getStanzas().size(), is(1));
		
		OBOStanza stanza = ontology.getStanza("foo");
		
		assertThat(stanza, is(not(nullValue())));
		assertThat(stanza.getType(), is("Individual"));
		assertThat(stanza.getClass(), is(OBOIndividual.class));
		assertThat(stanza.values("name").size(), is(1));
		assertThat(stanza.values("name").get(0).getValue(), is("foo"));
	}
}
