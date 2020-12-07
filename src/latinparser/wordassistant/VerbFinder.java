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
	
	private ArrayList<DictEntry> dict;
	private int start;
	private int upTo;
	private boolean isTwoWordVerb = false;
	private ArrayList<Integer> vIndices = new ArrayList<Integer>();
	private ArrayList<String> subjectForm = new ArrayList<String>();
	private ArrayList<String> objectForm = new ArrayList<String>();
	
	public VerbFinder(ArrayList<DictEntry> d, int sIdx, int tIdx) {
		dict = d;
		start = sIdx;
		upTo = tIdx;
	}
	
	public ArrayList<Integer> getVerbIndices() {return vIndices;}
	
	public ArrayList<String> getSubjectForm() {return subjectForm;}
	
	public ArrayList<String> getObjectForm() {return objectForm;}
	
	//TODO test getVerbInfo()
	//TODO infinitives (indirect statement)?
	//TODO make verb-borrowing conditional on there being a subj/obj not part of a list
	/**
	 * Attempts to find verb; if successful, it claims those verb words.
	 * If unsuccessful, it attempts to copy a verb from another clause.
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
	
	/**
	 * Finds the required person and number of each verb and constructs an
	 * arraylist of those strings, subjectForm
	 */
	private void setSubjectForms() {
		for (int i = 0; i < vIndices.size(); i++) {
			if (isTwoWordVerb && i % 2 == 1) {
				continue;
			}
			int vIdx = vIndices.get(i);
			int formidx = dict.get(vIdx).getWord(0).canBe(" VER");
			String verbForm = dict.get(vIdx).getWord(0).getForm(formidx);
			String personNum = verbForm.substring(verbForm.length()-8,
					verbForm.length()-4);
			subjectForm.add(personNum);
		}
	}
	
	/**
	 * Assembles an arraylist of the required cases for the objects of the verb.
	 * Attempts to find nonstandard object cases in the meaning line of the verb,
	 * otherwise it defaults to accusative.
	 * Exception: For the verb 'to be' it uses nominative.
	 * NOTE: assumes that all non-standard objects are listed in verb meaning.
	 * NOTE: assumes all uses of cases in verb meaning indicate non-standard object cases.
	 */
	private void setObjectForms() {
		for (int idx: vIndices) {
			// find a nonstandard object case
			for (String c: new String[] {"GEN", "DAT", "ABL"}) {
				String verb = dict.get(idx).getWord(0).toString(); 
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
			String word = dict.get(vIndices.get(i)).getWord(0).toString(); 
			if (word.substring(0, 3).equals("be;")) {
				objectForm.set(i, "NOM");
			}
		}
	}

	// TODO implement
	public char setClauseType() {
		return ' ';
	}
	
	/**
	 * Looks for all words in the clause whose most likely possibility is a verb
	 * and can be a conjugated verb. Adds their indices to vIndices.
	 */
	private void findVerb() {
		for (int i = start; i < upTo; i++) {
			if (dict.get(i).getWord(0).getPart().equals("V")) {
				
				for (String form: dict.get(i).getWord(0).getForms()) {
					if (form.contains(" VER")) {
						vIndices.add(i);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Finds the verb from a previous or later clause and adds it to vIndices.
	 * If this is the first clause, it steals the verb from the next clause.
	 * If this is a later clause, it will first attempt to steal a verb from the previous clause,
	 * then the next clause.
	 */
	private void stealVerb() {
		if (start == 0) {
			stealLaterVerb();
			return;
		}
		int prevStart = getStartOfLastClause();
		
		for (int i = start-1; i >= prevStart; i--) {
			if (dict.get(i).getWord(0).getPart().equals("V")) {
				
				for (String form: dict.get(i).getWord(0).getForms()) {
					if (form.contains(" VER")) {
						vIndices.add(i);
						return;
					}
				}
			}
		}
		// TODO more sophisticated system for stealing verbs from other clauses
		stealLaterVerb();
	}
	
	/**
	 * Steals a verb from the following clause to be used in this one.
	 * If a verb is found, its index is added to vIndices.
	 */
	private void stealLaterVerb() {
		int keywords = 0;
		int verbs = 0;
		verbStealing:
		for (int i = upTo; i < dict.size(); i++) {
			Word w = dict.get(i).getWord(0);
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
	
	/**
	 * Finds the starting index of the previous clause, or 0 if this is the first clause.
	 * @return The index of the start of the previous clause in the sentence
	 * or the start of the first clause if there are no previous ones.
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
	
	
	/**
	 * Detects if there is a two-word verb (such as "amatus est") in the clause.
	 * If so, adds the participle index to vIndices and claims the participle.
	 * Also sets the isTwoWordVerb flag.
	 */
	public void findTwoWordVerbs() {
		int ppl = checkForParticiple();
		int sum = checkForSumPart();
		while (ppl != -1 && sum != -1) {
			// ensures that the sum form has a valid tense
			for (String npart: new String[] {"PERF", "PLUP", "FUTP"}) {
				if (dict.get(sum).getWord("V").canBe(npart) != -1) {
					isTwoWordVerb = false;
					return;
				}
			}
			isTwoWordVerb = true;
			
			// adds participle index to verbs right after index of sum form
			vIndices.add(vIndices.indexOf(sum)+1, ppl);
			
			// claims the participle for Verb
			dict.get(ppl).setClaimed();
			
			ppl = checkForParticiple();
			sum = checkForSumPart();
		}
	}
	
	/**
	 * Returns the index of the first unclaimed Word that can be a participle,
	 * returns -1 if there are none.
	 * @return index of first Word that could be a participle, -1 if it doesn't exist
	 */
	private int checkForParticiple() {
		for (int i = start; i < upTo; i++) {
			if (dict.get(i).isClaimed())
				continue;
			if (dict.get(i).canBe("V") == -1)
				continue;
			if (dict.get(i).getWord("V").canBe("PPL") == -1)
				continue;
			return i;
		}
		return -1;
	}
	
	/**
	 * Checks for a form of the word sum (also succeeds on 'iri').
	 * Returns its index in the list, or -1 if it doesn't exist.
	 * @return index of form of sum or 'iri', or -1 if it doesn't exist
	 */
	private int checkForSumPart() {
		for (int idx: vIndices) {
			if (dict.get(idx).toString().equals("iri")) {
				return idx;
			}
			
			if (dict.get(idx).canBe("V") == -1) {
				continue;
			}
			Word d = dict.get(idx).getWord("V");
			if (d.canBe("!VER") == -1 && d.toString().substring(0, 3).equals("be;")) {
					return idx;
			}
		}
		return -1;
	}
}
