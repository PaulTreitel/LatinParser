package latinparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

/* TODO Dictionary Settings (#)

DO_UNKNOWNS_ONLY
	Returns only unknown terms
	Possibly use in first run to deal with names, etc
DO_FIXES
	adds prefixes and suffixes if unable to find normal version
		(may be spurious)
	change depending on whether some really weird stuff turns up
DO_TRICKS
	tries slight modifications to the query in case of failure
		(possibly spurious)
	change depending on whether some really weird stuff turns up

formatting for notes to be passed to Words for translation:
N: "MEANINGNUM FORMNUM GENDER ARTICLE [PREP = preposition to use]" | "0 1 F the by"
ADJ: "MEANINGNUM FORMNUM GENDER ARTICLE [PREP] [SUBST = use noun translation]" | "2 3 M a for SUBST"
ADV: "MEANINGNUM FORMNUM" " | "1 0"
CONJ/INTERJ:"MEANINGNUM" | "0"
NUM: "" | ""
PREP: "MEANINGNUM" | "2"
PRON: "MEANINGNUM FORMNUM GENDER ARTICLE [PREP] [SUBST]" | "1 1 N a from SUBST"
V: "MEANINGNUM FORMNUM SUBCLAUSE PRON [GENDER]" | "1 2 . true F"

FINDING NOMINATIVES
0: rule out words preceded by prepositions
1: if an N can only be NOM or VOC, set to NOM; 
2: if only one N can be NOM, set that to NOM;
3: if zero N can be NOM, repeat process with ADJ
4: if multiple N can be NOM, narrow by matching PERS and NUMB with V
  4.1: if multiple N, find ADJ that can only be NOM or VOC and narrow by matching NUMB and GENDER
5: if all else fails, imply from verb

KEYWORDS
ut; ne; quod; qui, quae, quod; ubi; cum (as a CONJ); postquam; si; nisi;
sive; seu; quo; unde; quoniam; quia; simul; etsi; tamen; quam
*/

//assumes that ablativess fall closer to the end of the sentence and searches there first.
//will not use word forms marked 'Early'
//point of improvement: numeral translation?
public class LatinParser {
	private static final String WORDSIN = System.getProperty("user.dir")+"/src/autoin.txt";
	private static final String WORDSOUT = System.getProperty("user.dir")+"/src/wordsout.txt";
	public static final String PRONOUNS = System.getProperty("user.dir")+"/src/pronouns.txt";
	public static final String LOCS = System.getProperty("user.dir")+"/src/locatives.txt";
	public static final String PLURALS = System.getProperty("user.dir")+"/src/plurals.txt";
	//extra case: cum (as CONJ)
	private final static String KEYWORDS = " ut ne quod ubi postquam si cur "
			+ "nisi sive seu unde quoniam quia simul etsi tamen qui quam quo ";
	
	private static ArrayList<DictEntry> dict;
	private static ArrayList<Clause> clauses;
	//retains punctuation, but not line breaks
	private static String originalText;
	private static boolean brk;
	public static boolean isPronounFile;
	public static boolean isLOCFile;
	public static boolean isPluralFile;
	
	private String translation;
	private String origin;
	
	public String getTranslation() {return translation;}
	public String getOrigin() {return origin;}
	public String translate(String input) {
		origin = input;
		translation = LatinParser.parse(input);
		return translation;
	}
	
	public static String parse(String rawInput) {
		loadNeededFiles();
		try {
			originalText = getFile(rawInput).replaceAll("\r\n", " ").replaceAll("  ", " ");
		} catch (FileNotFoundException e) {
			System.out.println("Fatal FileNotFoundException: "+ rawInput +" does not exist.");
			e.printStackTrace();
			return null;
		}
		String transl = "";
		for (String x: originalText.split("[.] |[?] |! |[.]|[?]|!")) {
			dict = new ArrayList<DictEntry>();
			stringsToDicts(x +".");
			for (int i = 1;  i < dict.size(); i++)
				if (dict.get(i-1).canBe("V") != -1 && 
						dict.get(i-1).getW("V").canBe("PPL") != -1 && dict.get(i).toString().equals("iri")) {
					dict.get(i).setPart("ADJ");
					dict.get(i).addPart("ADJ", "to be about to be", dict.get(i-1).getW("V").getForms());
				}
			if (brk)
				return null;
			System.out.println(dict);
			transl += translate();
		}
		return transl;
	}
	
	private static void callWORDS(String w) {
		writeToIn(w);
		ProcessBuilder builder = new ProcessBuilder();
		if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
		    builder.command("cmd.exe", "/c", "WORDS", WORDSIN, WORDSOUT);
		else
		    builder.command("sh", "-c", "WORDS", WORDSIN, WORDSOUT);
		builder.directory(new File(System.getProperty("user.dir") +"/src/WORDS"));
		try {
			Process process = builder.start();
			process.waitFor();
			process.destroy();
		} catch (InterruptedException e1) {
			System.out.println("Fatal InterruptedException while running WORDS.exe");
			brk = true;
			e1.printStackTrace();
		} catch (IOException e2) {
			System.out.println("Fatal IOException: unable to run WORDS.exe");
			brk = true;
			e2.printStackTrace();
		}
	}
	
	private static void writeToIn(String w) {
		try (Writer writer = new BufferedWriter(new FileWriter(WORDSIN))) {
			writer.write(w);
		} catch (IOException e) {
			System.out.println("Fatal IOException: unable to access " +WORDSIN);
			e.printStackTrace();
			brk = true;
			return;
		}
	}
	
	private static void loadNeededFiles() {
		File f = new File(PRONOUNS);
		isPronounFile = f.exists() && !f.isDirectory();
		if (!isPronounFile)
			System.out.println("Pronoun file "+ PRONOUNS +" does not exist. Pronouns have been disabled.");
		f = new File(LOCS);
		isLOCFile = f.exists() && !f.isDirectory();
		if (!isLOCFile)
			System.out.println("Locative file "+ LOCS +" does not exist. Locatives have been disabled.");
		f = new File(PLURALS);
		isPluralFile = f.exists() && !f.isDirectory();
		if (!isPluralFile)
			System.out.println("Plural file "+ PLURALS +" does not exist. Certain pluralizations may be incorrect.");
	}
	
	private static String getFile(String in) throws FileNotFoundException {
		if (in.substring(1, 3).equals(":/") || in.substring(1, 3).equals(":\\")) {
			File f = new File(in);
			Scanner sc = new Scanner(f);
			sc.useDelimiter("$^");
			String r = sc.next();
			sc.close();
			return r;
		} else
			return in;
	}
	
	private static void stringsToDicts(String input) {
		String[] words = input.split(" ");
		callWORDS(input);
		if (brk)
			return;
		try {
			String dictgen = getFile(WORDSOUT).replaceAll("[*]", "");
			String[] dictResult = dictgen.split("\r\n\r\n");
			createDictEntries(dictResult, words);
		} catch (FileNotFoundException e) {
			System.out.println("Fatal FileNotFoundException: " + WORDSOUT + "does not exist.");
			brk = true;
			e.printStackTrace();
			return;
		}
	}
	
	private static void createDictEntries(String[] dictResult, String[] words) {
		int change = 0;
		boolean cooloff = false;
		//System.out.println(Arrays.toString(dictResult));
		for (int i = 0; i < words.length; i++) {
			//System.out.println(i +" "+ cooloff +" "+ change +" "+ words[i]);
			if (!cooloff && (dictResult[i-change].contains("verb TO_BE")||dictResult[i-change].contains("+esse") || 
				dictResult[i-change].contains("+ iri"))) {
				stringsToDicts(words[i]);
				change++; cooloff = true;
				continue;
			} else if (cooloff) {
				cooloff = !cooloff;
				//System.out.println(i +"|"+ change +" "+ words[i]);
			}
			dict.add(new DictEntry(dictResult[i-change], words[i]));
		}
	}
	
	private static String translate() {
		divideClauses();
		String translation = "";
		translation += clauses.get(0).translate();
		//for (Clause c: clauses) TODO enable full translation
			//translation += c.translate();
		return translation;
	}
	
	private static void divideClauses() {
		clauses = new ArrayList<Clause>();
		int start = 0;
		for (int i = 0; i < dict.size(); i++) {
			if (isKeyword(i) || dict.get(i).getPunct().equals(";")) {
				clauses.add(new Clause(dict, start, i, false));
				start = i;
			} else if (i == dict.size()-1) {
				clauses.add(new Clause(dict, start, i+1, false));
			}
		}
	}
	
	public static boolean isKeyword(int idx) {
		if (dict.get(idx).toString().equals("cum")) {
			int isN = dict.get(idx+1).canBe("N");
			if (isN == -1 || dict.get(idx+1).getW(isN).canBe("ABL") == -1)
				return true;
			return false;
		} else if (KEYWORDS.contains(" "+ dict.get(idx).toString() +" "))
			return true;
		else if (LatinParser.isPronounFile && checkPronouns(idx))
			return true;
		return false;
	}
	
	private static boolean checkPronouns(int idx) {
		try {
			Scanner sc = new Scanner(new File(LatinParser.PRONOUNS));
			sc.useDelimiter("\r\n\r\n\r\n");
			String qui = sc.next();
			sc.close();
			if (qui.contains("\r\n"+ dict.get(idx).toString() +" "))
				return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
}
