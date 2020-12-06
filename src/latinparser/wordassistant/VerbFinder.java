package latinparser.wordassistant;


import java.util.ArrayList;

import latinparser.DictEntry;
import latinparser.LatinParser;
import latinparser.words.Word;

/* VerbFinder
 *  - ArrayList<Integer> getVerbInfo()
 *  - ArrayList<Integer> findVerb()
 *  - ArrayList<Integer> stealVerb()
 *  - ArrayList<Integer> stealLaterVerb()
 *  - int getStartOfLastClause()
 *  - void setSubjectForms()
 *  - void setObjectForms
 *  - ArrayList<String> getSubjectForm()
 *  - ArrayList<String> getObjectForm()
 *  - char setClauseType()
 */

//TODO overhaul with .getW() that isn't 0?
public class VerbFinder {
	
	private static final String SUM_FORM = 
			"est                  V      5 1 IMPF"; //" ACTIVE  IND 3 S";
	
	private ArrayList<DictEntry> dict;
	private int start;
	private int upTo;
	private ArrayList<Integer> vIndices = new ArrayList<Integer>();
	private ArrayList<String> subjectForm = new ArrayList<String>();
	private ArrayList<String> objectForm = new ArrayList<String>();
	
	public VerbFinder(ArrayList<DictEntry> d, int sIdx, int tIdx) {
		dict = d;
		start = sIdx;
		upTo = tIdx;
	}
	
	public ArrayList<Integer> getVerbIndices() {
		return vIndices;
	}
	
	public ArrayList<String> getSubjectForm() {
		return subjectForm;
	}
	
	public ArrayList<String> getObjectForm() {
		return objectForm;
	}
	
	//TODO test getVerbInfo()
	//TODO infinitives (indirect statement)?
	//TODO make verb-borrowing conditional on there being a subj/obj not part of a list
	/* getVerbInfo
	 * attempts to find verb; if successful, it claims those verb words
	 * if unsuccessful, it attempts to copy a verb from another clause
	 * returns ArrayList of all verb indices in the clause
	 */
	public void getVerbInfo() {
		findVerb();
		if (vIndices.size() == 0) {
			stealVerb();
		}
		for (int idx: vIndices) {
			dict.get(idx).setPart("V");
			dict.get(idx).setClaimed();
		}
		findTwoWordVerbs();
		setSubjectForms();
		setObjectForms();
	}
	
	/* setSubjectForms
	 * finds the required person and number of each very and constructs an
	 * arraylist of those strings
	 */
	private void setSubjectForms() {
		for (int idx: vIndices) {
			int formidx = dict.get(idx).getW(0).canBe(" VER");
			String verbForm = dict.get(idx).getW(0).getF(formidx);
			String personNum = verbForm.substring(verbForm.length()-8,
					verbForm.length()-4);
			subjectForm.add(personNum);
		}
	}
	
	/* setObjectForms
	 * attempts to find nonstandard object cases in the meaning line of the verb
	 * otherwise defaults to accusative
	 * for the verb 'to be' it uses nominative
	 * assembles an arraylist of the required cases for the objects of the verb
	 * NOTE assumes that all non-standard objects are listed in verb meaning
	 * NOTE assumes all uses of cases in verb meaning indicate non-standard object cases
	 */
	private void setObjectForms() {
		for (int idx: vIndices) {
			for (String c: new String[] {"GEN", "DAT", "ABL"}) {
				String verb = dict.get(idx).getW(0).toString(); 
				if (verb.toUpperCase().contains(c)) {
					objectForm.add(c);
					break;
				}
			}
			if (objectForm.size() == 0) {
				objectForm.add("ACC");
			}
		}
		
		for (int i = 0; i < objectForm.size(); i++) {
			String word = dict.get(vIndices.get(i)).getW(0).toString(); 
			if (word.substring(0, 3).equals("be;")) {
				objectForm.set(i, "NOM");
			}
		}
	}

	// TODO implement
	public char setClauseType() {
		return ' ';
	}
	
	/* findVerb
	 * looks for all words in the clause whose most likely possibility is a verb
	 * and can be a conjugated verb
	 * adds their indices to vIndices
	 */
	private void findVerb() {
		for (int i = start; i < upTo; i++) {
			if (dict.get(i).getW(0).getPart().equals("V")) {
				
				for (String form: dict.get(i).getW(0).getForms()) {
					if (form.contains(" VER")) {
						vIndices.add(i);
						break;
					}
				}
			}
		}
	}
	
	/* stealVerb
	 * finds verb from a previous clause and adds it to vIndices
	 */
	private void stealVerb() {
		if (start == 0) {
			stealLaterVerb();
			return;
		}
		int e = getStartOfLastClause();
		
		for (int i = start-1; i > e; i--) {
			if (dict.get(i).getW(0).getPart().equals("V")) {
				
				for (String form: dict.get(i).getW(0).getForms()) {
					if (form.contains(" VER")) {
						vIndices.add(i);
						return;
					}
				}
			}
		}
		System.out.println("HELP stealVerb()");
	}
	
	/* stealLaterVerb
	 * sub-process of stealVerb, but for when there is no preceding clause
	 */
	private void stealLaterVerb() {
		int keywords = 0;
		int verbs = 0;
		verbStealing:
		for (int i = upTo; i < dict.size(); i++) {
			Word w = dict.get(i).getW(0);
			if (LatinParser.isKeyword(i)) {
				keywords++;
			} else if (w.getPart().equals("V") && keywords == verbs) {
				for (String form: w.getForms()) {
					if (form.contains(" VER")) {
						vIndices.add(i);
						break verbStealing;
					}
				}
			} else if (w.getPart().equals("V") && verbs < keywords) {
				verbs++;
			}
		}
	}
	
	/* getStartofLastClause
	 * returns the index of the start of the previous clause in the sentence
	 * or the start of the first clause if there are no previous ones
	 */
	private int getStartOfLastClause() {
		for (int i = start-1; i > 0; i--) {
			if (LatinParser.isKeyword(i) || dict.get(i).getPunct() == ';')
				return i;
		}
		return 0;
	}
	
	
	
	/* TWO WORD VERB FORM METHODS
	 *  - void findTwoWordVerbs()
	 *  - String genFormType(DictEntry d)
	 *  - int checkForParticiple()
	 *  - int checkForSumPart()
	 */
	
	
	
	public void findTwoWordVerbs() {
		int ppl = checkForParticiple();
		int sum = checkForSumPart();
		while (ppl != -1 && sum != -1) {
			// ensures that the sum form has a valid tense
			for (String npart: new String[] {"PERF", "PLUP", "FUTP"})
				if (dict.get(sum).getW("V").canBe(npart) != -1)
					return;
			//twoWordV = true;
			// TODO figure out what this does
			dict.get(sum).getW("V").addPossForm(SUM_FORM+genFormType(dict.get(sum)));
			dict.get(sum).getW("V").setPart("IMPF");
			
			// adds participle index to verbs right after index of sum form
			vIndices.add(vIndices.indexOf(sum)+1, ppl);
			
			// claims the participle for Verb
			dict.get(ppl).setClaimed();
			
			ppl = checkForParticiple();
			sum = checkForSumPart();
		}
	}

	/* genFormType
	 * takes a DictEntry, gets the first verb sense of the word, then gets
	 * the first possible form of that word
	 * removes the tense and fully writes out the voice, then returns
	 * the resulting string
	 */
	private String genFormType(DictEntry d) {
		String f;
		String existing = d.getW("V").getF(0);
		if (existing.contains(" A "))
			f = "ACTIVE" + existing.substring(existing.length()-12);
		else
			f = "PASSIVE" + existing.substring(existing.length()-12);
		return f;
	}
	
	/* checkForParticiple
	 * returns the index of the first unclaimed Word that can be a participle
	 * returns -1 if there are none
	 */
	private int checkForParticiple() {
		for (int i = start; i < upTo; i++) {
			if (dict.get(i).isClaimed())
				continue;
			if (dict.get(i).canBe("V") == -1)
				continue;
			if (dict.get(i).getW("V").canBe("PPL") == -1)
				continue;
			return i;
		}
		return -1;
	}
	
	/* checkForSumPart
	 * checks for a form of the word sum (also succeeds on 'iri')
	 * returns its index in the list, or -1 if it doesn't exist
	 */
	private int checkForSumPart() {
		for (int idx: vIndices) {
			if (dict.get(idx).canBe("V") != -1) {
				
				Word d = dict.get(idx).getW("V");
				if (d.canBe("!VER") == -1)
					if (d.toString().substring(0, 3).equals("be;"))
						return idx;
			}
			if (dict.get(idx).toString().equals("iri"))
				return idx;
		}
		return -1;
	}
}
