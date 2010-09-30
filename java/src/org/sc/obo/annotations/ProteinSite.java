package org.sc.obo.annotations;

@Term interface Site { 
	public static String id = "SNAP:site";
}

@Term interface ExonPart extends Site { 
	
	public static String id = "request_00003";
	public static String def = "A site which is a part of a protein transcribed from a single exon in a transcript.";

}

public @Term interface ProteinSite extends Site {

	public static String id = "request_00001";
	public static String def = "A site on a protein.";
	public static String comment = "This is an example of how to use the OBO annotations.";
	
	public @Relates("part_of") ExonPart exon();
}

@Term interface Protein { 
	
	public static String id = "PRO:000000001";
	public static String def = "";
	public static String comment = "";
}

@Term interface Isoform extends Protein { 
	public static String id = "request:00004";
	public static String def = "an exon part of a protein.";

	public @Relates("has_part") ExonPart[] hasParts();
	public @Relates("lacks_part") ExonPart[] lacksParts();
}

@Term interface ModifiedSite extends ProteinSite { 
	
	public static String id = "request_00002";
	public static String def = "This is a protein site which contains a modified residue.";
	public static String comment = "This is an example of sub-classing using the OBO annotations.";
	
	public @Relates Residue[] contains();
}

@Term interface Residue { 
	public static String id = "CHEBI:something";
}

