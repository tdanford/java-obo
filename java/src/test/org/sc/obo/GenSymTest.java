package org.sc.obo;

import org.junit.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class GenSymTest { 
	
	@Test 
	public void testUnique() { 
		GenSym sym = new GenSym("foo");
		String s1 = sym.nextSymbol();
		String s2 = sym.nextSymbol();
		assertThat(s1, not(s2));
	}
}
