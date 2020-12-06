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
N: "MEANINGNUM FORMNUM GENDER ARTICLE [PREP = preposition to use]"
	"0 1 F the by"
ADJ: "MEANINGNUM FORMNUM GENDER ARTICLE [PREP] [SUBST = use noun translation]"
	"2 3 M a for SUBST"
ADV: "MEANINGNUM FORMNUM" "
	"1 0"
CONJ/INTERJ:"MEANINGNUM"
	"0"
NUM: ""
	""
PREP: "MEANINGNUM"
	"2"
PRON: "MEANINGNUM FORMNUM GENDER ARTICLE [PREP] [SUBST]"
	"1 1 N a from SUBST"
V: "MEANINGNUM FORMNUM SUBCLAUSE PRON [GENDER]"
	"1 2 . true F"

FINDING NOMINATIVES
0: rule out words preceded by prepositions
1: if an N can only be NOM or VOC, set to NOM; 
2: if only one N can be NOM, set that to NOM;
3: if zero N can be NOM, repeat process with ADJ
4: if multiple N can be NOM, narrow by matching PERS and NUMB with V
4.1: if multiple N, find ADJ that can only be NOM or VOC
	and narrow by matching NUMB and GENDER
5: if all else fails, imply from verb

KEYWORDS
ut; ne; quod; qui, quae, quod; ubi; cum (as a CONJ); postquam; si; nisi;
sive; seu; quo; unde; quoniam; quia; simul; etsi; tamen; quam
*/

//assumes that ablativess fall closer to the end of the sentence
	//and searches there first.
//will not use word forms marked 'Early'
//point of improvement: numeral translation?
public class LatinParser {
	// sets constants for file names used by the parser
	// either for dictionary program or to supplement dictionary
	private static final String WORDSIN = System.getProperty("user.dir")+
			"/bin/autoin.txt";
	private static final String WORDSOUT = System.getProperty("user.dir")+
			"/bin/wordsout.txt";
	private static final String PRONOUNS = System.getProperty("user.dir")+
			"/bin/pronouns.txt";
	private static final String LOCS = System.getProperty("user.dir")+
			"/bin/locatives.txt";
	private static final String PLURALS = System.getProperty("user.dir")+
			"/bin/plurals.txt";
	private final static String KEYWORDS = " ut ne quod ubi postquam si cur "
			+ "nisi sive seu unde quoniam quia simul etsi tamen qui quam quo ";
	
	public static String LOCS_CONTENTS;
	public static String PLURALS_CONTENTS;
	public static String PRONOUNS_CONTENTS;
	
	private static ArrayList<DictEntry> dict = new ArrayList<DictEntry>();
	private static ArrayList<Clause> clauses;
	private static String originalText; //retains punctuation w/o line breaks
	
	// TODO enable full translation
	/* translate
	 * divides the dictionary entries into clauses, then translates each clause
	 */
	public static String translate() {
		String translation = "";
		//translation += clauses.get(0).translate();
		for (Clause c: clauses) {
			translation += c.translate();
		}
		return translation;
	}

	/* parse
	 * takes in an input filepath and converts it into a set of clauses
	 * each containing dictionary entires
	 */
	public static void parse(String rawInputFile) {
		loadNeededFiles();
		try {
			originalText = getFile(rawInputFile).replaceAll("\r\n", " ");
			originalText = originalText.replaceAll("  ", " ");
		} catch (FileNotFoundException e) {
			System.out.println("Fatal FileNotFoundException: "+ rawInputFile 
					+" does not exist.");
			e.printStackTrace();
			System.exit(1);
		}
		
		for (String x: originalText.split("[.] |[?] |! |[.]|[?]|!")) {
			stringsToDicts(x +".");
			handleSpecialFutPassInf();
			System.out.println(dict);
		}
		divideClauses();
	}
	
	/* loadNeededFiles
	 * ensures that the pronoun, locatives, and plurals text files exist
	 * exits the program if any of them do not
	 */
	private static void loadNeededFiles() {
		try {
			PRONOUNS_CONTENTS = getFile(PRONOUNS);
		} catch (FileNotFoundException e) {
			System.out.println("Error: Pronoun file "+ PRONOUNS 
					+" does not exist");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			LOCS_CONTENTS = getFile(LOCS);
		} catch (FileNotFoundException e) {
			System.out.println("Error: Locative file "+ LOCS 
					+" does not exist");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			PLURALS_CONTENTS = getFile(PLURALS);
		} catch (FileNotFoundException e) {
			System.out.println("Error: Plural file "+ PRONOUNS 
					+" does not exist");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/* getFile
	 * takes an absolute filepath, scans the file into a string, and returns it
	 */
	private static String getFile(String in) throws FileNotFoundException {
		File f = new File(in);
		Scanner sc = new Scanner(f);
		sc.useDelimiter("$^");
		String r = sc.next();
		sc.close();
		return r;
	}
	
	/* stringsToDicts
	 * takes a string, splits it into words, puts it through WORDS,
	 * and generates dictionary entries for them
	 */
	private static void stringsToDicts(String input) {
		String[] words = input.split(" ");
		callWORDS(input);
		try {
			String dictgen = getFile(WORDSOUT).replaceAll("[*]", "");
			String[] dictResult = dictgen.split("\r\n\r\n");
			createDictEntries(dictResult, words);
		} catch (FileNotFoundException e) {
			System.out.println("Fatal FileNotFoundException: " + WORDSOUT + 
					"does not exist.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/* callWORDS
	 * takes an input string, writes it to the WORDSIN file, then
	 * runs the dictionary program on that file, outputting it into WORDSOUT
	 */
	private static void callWORDS(String w) {
		writeToIn(w);
		ProcessBuilder builder = assembleWORDSBuilder(); 
		try {
			Process process = builder.start();
			process.waitFor();
			process.destroy();
		} catch (InterruptedException e1) {
			System.out.println("Fatal InterruptedException while running "
					+"WORDS.exe");
			e1.printStackTrace();
			System.exit(1);
		} catch (IOException e2) {
			System.out.println("Fatal IOException: unable to run WORDS.exe");
			e2.printStackTrace();
			System.exit(1);
		}
	}
	
	/* assembleWORDSBuilder
	 * assembles the ProcessBuilder for running the WORDS program
	 */
	private static ProcessBuilder assembleWORDSBuilder() {
		ProcessBuilder builder = new ProcessBuilder();
		String f = System.getProperty("user.dir");
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
		    builder.command("cmd.exe", "/c", "words", WORDSIN, WORDSOUT);
		    builder.directory(new File(f + "/bin/"));
		} else {
		    builder.command("sh", "-c", "words", WORDSIN, WORDSOUT);
		    builder.directory(new File(f + "/bin/"));
		}
		return builder;
	}
	
	/* writeToIn
	 * takes in a string and writes it to the WORDSIN file
	 */
	private static void writeToIn(String w) {
		try (Writer writer = new BufferedWriter(new FileWriter(WORDSIN))) {
			writer.write(w);
		} catch (IOException e) {
			System.out.println("Fatal IOException: unable to access " +WORDSIN);
			e.printStackTrace();
			System.exit(1);
		}
	}
		
	/* createDictEntries
	 * takes a list of dictionary results and a corresponding list of words
	 * and turns them into DictEntries
	 */
	private static void createDictEntries(String[] dictResult, String[] words) {
		int change = 0;
		for (int i = 0; i < words.length; i++) {
			// handles special case for fut pass inf - reruns word through WORDS
			if ((dictResult[i-change].contains("verb TO_BE") ||
					dictResult[i-change].contains("+esse") || 
				dictResult[i-change].contains("+ iri"))) {
				stringsToDicts(words[i]);
				change++;
				continue;
			}
			
			dict.add(new DictEntry(dictResult[i-change], words[i]));
		}
	}
	
	/* handleSpecialFutPassInf
	 * handles the special future passive infinitive form
	 * when generating DictEntries
	 * "[perf pass participle] iri"
	 */
	private static void handleSpecialFutPassInf() {
		for (int i = 1;  i < dict.size(); i++) {
			boolean isFPI = dict.get(i).toString().equals("iri");
			boolean isVerb = dict.get(i-1).canBe("V") != -1; 
			if (isVerb && isFPI && dict.get(i-1).getW("V").canBe("PPL") != -1) {
				
				dict.get(i).setPart("ADJ");
				dict.get(i).addPart("ADJ", "to be about to be",
						dict.get(i-1).getW("V").getForms());
			}
		}
	}
	
	/* divideClauses
	 * takes the list of dictionary entries and splits it into "clauses"
	 * a clauses is distinguished by a keyword (ie conjunction)
	 * or an ending semicolon
	 */
	private static void divideClauses() {
		clauses = new ArrayList<Clause>();
		int start = 0;
		for (int i = 0; i < dict.size(); i++) {
			if (isKeyword(i) || dict.get(i).getPunct() == ';') {
				clauses.add(new Clause(dict, start, i, false));
				start = i;
			} else if (i == dict.size()-1) {
				clauses.add(new Clause(dict, start, i+1, false));
			}
		}
	}
		
	/* isKeyword
	 * checks if the dictionary entry at the given index matches exactly a word
	 * in the KEYWORDS string; also checks (in a very simplistic way)
	 * for the conjunction use of the preposition cum
	 * TODO fix bug: other pronouns like 'se' are acting as keywords
	 */
	public static boolean isKeyword(int idx) {
		if (dict.get(idx).toString().equals("cum")) {
			int isN = dict.get(idx+1).canBe("N");
			if (isN == -1 || dict.get(idx+1).getW(isN).canBe("ABL") == -1)
				return true;
			return false;
		// spaces added to protect against compound keywords
		} else if (KEYWORDS.contains(" "+ dict.get(idx).toString() +" ")) {
			return true;
		}
		String toCheck = "\r\n"+dict.get(idx).toString()+" ";
		return LatinParser.PRONOUNS_CONTENTS.contains(toCheck);
	}
	
	public static String getLocsContents() {return LOCS_CONTENTS;}
	public static String getPluralsContents() {return PLURALS_CONTENTS;}
	public static String getPronounsContents() {return PRONOUNS_CONTENTS;}
	
}
