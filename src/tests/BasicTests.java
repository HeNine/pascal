package tests;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import compiler.lexanal.PascalLex;
import compiler.lexanal.PascalSym;
import compiler.report.Report;
import compiler.report.XML;
import compiler.synanal.PascalTok;

public class BasicTests {

	public static String[] pascalTermNames;

	@BeforeClass
	public static void setupClass() {
		PascalTok pascalTok = new PascalTok();
		Field[] pascalToks = pascalTok.getClass().getDeclaredFields();
		pascalTermNames = new String[pascalToks.length + 5];
		for (int f = 0; f < pascalToks.length; f++) {
			try {
				int tok = pascalToks[f].getInt(pascalTok);
				String lex = pascalToks[f].toString().replaceAll("^.*\\.", "");
				pascalTermNames[tok] = lex;
			} catch (IllegalAccessException _) {
			}
		}
	}

	@Rule
	public TestName testName = new TestName();

	private FileReader srcFile;
	private PrintStream xml;

	@Before
	public void setupTest() {
		String srcName = "testcases/" + testName.getMethodName() + ".pas";
		try {
			srcFile = new FileReader(srcName);
		} catch (FileNotFoundException _) {
			Report.error("Source file '" + srcName + "' cannot be opened.", 1);
		}
		xml = XML.open("lexanal", "testresults/" + testName.getMethodName());
	}

	@Test
	public void ProgramTest() {
		doTest();
	}

	@Test
	public void IdentifierAndConstTest() {
		doTest();
	}

	@Test
	public void VarTest() {
		doTest();
	}

	@Test
	public void SimpleTest() {
		doTest();
	}

	private void doTest() {
		PascalLex lexer = new PascalLex(srcFile);
		PascalSym symbol;
		try {
			while ((symbol = lexer.next_token()).sym != PascalTok.EOF) {
				symbol.toXML(xml);
			}
		} catch (IOException _) {
			Report.error("Error while testing lexical analyzer.", 1);
		}

		XML.close("lexanal", xml);
		try {
			srcFile.close();
		} catch (IOException _) {
		}

		assertTrue(compareXml());
	}

	private boolean compareXml() {

		List<Token> testTokens = xmlToTokenList("testresults/" + testName.getMethodName() + ".xml");
		List<Token> sampleTokens = xmlToTokenList("testresults/" + testName.getMethodName() + "Sample.xml");

		return testTokens.equals(sampleTokens);
	}

	private List<Token> xmlToTokenList(String xmlFilename) {
		final List<Token> tokens = new LinkedList<>();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) {
					if (qName.equals("terminal")) {
						tokens.add(new Token(attributes.getValue("token"), attributes.getValue("lexeme")));
					}
				}

			};

			saxParser.parse(xmlFilename, handler);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tokens;
	}

	private class Token {
		final public String token;
		final public String lexeme;

		public Token(String token, String lexeme) {
			this.token = token;
			this.lexeme = lexeme;
		}

		@Override
		public boolean equals(Object that) {
			if (that.getClass() != Token.class) {
				return false;
			}
			assert that.getClass() == Token.class;

			if (!this.token.equals(((Token) that).token)) {
				return false;
			}
			if (this.lexeme == null && ((Token) that).lexeme != null || this.lexeme != null
					&& ((Token) that).lexeme == null) {
				return false;
			}
			if (this.lexeme != null && ((Token) that).lexeme != null && !this.lexeme.equals(((Token) that).lexeme)) {
				return false;
			}

			return true;
		}
	}
}
