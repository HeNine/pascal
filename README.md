pascal
======

Test cases for simplified Pascal compiler.

    public static PrintStream open(String phase, String filename) {
		PrintStream stream = null;
		try {
			stream = new PrintStream(filename + ".xml");
			stream.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			try {
				String xslDir = System.getenv("PASCALXSL");
				if (xslDir != null) {
					stream.println("<?xml-stylesheet type=\"text/xsl\" href=\"" + xslDir + "/" + phase + ".xsl\"?>");
				}
			} catch (Exception _) {
			}
			stream.println("<" + phase + ">");
		} catch (IOException _) {
			Report.error("Cannot open XML file '" + phase + ".xml'.", 1);
		}
		return stream;
	}