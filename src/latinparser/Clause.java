package latinparser;



import java.util.ArrayList;
import latinparser.wordassistant.AblativeFinder;
import latinparser.wordassistant.PrepositionFinder;
import latinparser.wordassistant.VerbFinder;
import latinparser.words.Word;

public class Clause {
	
	private Utility u;
	private ArrayList<DictEntry> dict;
	private boolean[] revisitSubjs = new boolean[1];
	private boolean[] revisitObjs = new boolean[1];
	private boolean processed;
	private boolean revisitPreps;
	//private boolean twoWordV;
	private int start;
	private int upTo;
	
	private ArrayList<DictEntry> preps;
	private ArrayList<Integer> verbIdx;
	private ArrayList<String> subjectForm;
	private ArrayList<String> objectForm;
	private ArrayList<DictEntry> objects;
	private ArrayList<DictEntry> ablatives;
	private ArrayList<DictEntry> subjects;
	
	public Clause(ArrayList<DictEntry> d, int startIndex, int uptoIndex,
			boolean checkCommas) {
		dict = d;
		start = startIndex;
		upTo = uptoIndex;
		processed = checkCommas;
		revisitPreps = false;
		u = new Utility(dict, start, upTo);
	}
	
	/* CLAUSING METHODS
	 *  - ArrayList<Clause> process()
	 *  - String translateSubclauses()
	 *  - String translate()
	 */
	
	/* process
	 * generates an arraylist of subclauses, separated by commas in the text
	 */
	private ArrayList<Clause> process() {
		ArrayList<Clause> subclauses = new ArrayList<Clause>();
		int s = start;
		for (int i = start; i < upTo; i++)
			if (dict.get(i).getPunct() == ',' && i+1 != upTo) {
				subclauses.add(new Clause(dict, s, i+1, true));
				s = i+1;
			} 
		if (s != start) {
			subclauses.add(new Clause(dict, s, upTo, true));
		}
		return subclauses;
	}
	
	/* translateSubclauses
	 * combines and returns the translations of each subclause
	 */
	private String translateSubclauses() {
		ArrayList<Clause> subclauses = process();
		String translation = "";
		for (Clause c: subclauses)
			translation += c.translate();
		return translation;
	}
	
	//TODO add indirect statement, distinguish from complementary INF & NOM INF
	//TODO uses of the DAT
	//TODO uses of the GEN
	//TODO negations (non, ne, nonne, etc)
	//TODO proviso clauses (p264 Wheelock's) - add?
	//TODO refactor
	public String translate() {
		System.out.println("CLAUSE "+ dict.subList(start, upTo));
		// splits semi-colon clauses into comma sub-clauses then translates them
		if (!processed) {
			String translation =  translateSubclauses();
			if (!translation.equals(""))
				return translation;
		}
		
		// TODO figure out what the hell this is
		if (upTo-start == 1) {
			NotesGenerator NGen = new NotesGenerator(dict, start, upTo);
			String notes = NGen.getSingleNotes();
			return dict.get(start).getWord(0).translate(notes);
		}
		
		findVocatives();
		
		PrepositionFinder PFinder = new PrepositionFinder(dict, start, upTo, u);
		preps = PFinder.findPrepositionPhrases();
		revisitPreps = PFinder.getRevisit();
		
		VerbFinder VFinder = new VerbFinder(dict, start, upTo);
		VFinder.getVerbInfo();
		subjectForm = VFinder.getSubjectForm();
		objectForm = VFinder.getObjectForm();
		verbIdx = VFinder.getVerbIndices();
		
		//char clauseType = VFinder.setClauseType(verbIdx);
		//System.out.println(clauseType);
		
		if (!objectForm.contains("NOM")) {
			objects = getWordsOfCase(objectForm, 
					objectForm.toArray(new String[] {}), revisitObjs);
		}
		
		AblativeFinder ABLFinder = new AblativeFinder(dict, start, upTo, u); 
		ablatives = ABLFinder.getAblativeUses(verbIdx, preps);
		
		matchAdjectives();
		
		subjects = getWordsOfCase(subjectForm, new String[] {}, revisitSubjs);
		
		if (objectForm.contains("NOM")) {
			objects = getWordsOfCase(objectForm, 
					objectForm.toArray(new String[] {}), revisitObjs);
		}
		
		System.out.println("SUBJECTS "+ subjects);
		System.out.println(subjectForm);
		System.out.println("VERB INDICES "+ verbIdx);
		System.out.println("OBJECTS "+ objects);
		System.out.println(objectForm);
		System.out.println("PREPS "+ preps);
		System.out.println("ABLATIVES "+ ablatives);
		
		//TODO revisit PREPs, subjs, objs if necessary
		if (revisitPreps)
			System.out.println("revisit preps");
		if (revisitSubjs[0])
			System.out.println("revisit subjs");
		if (revisitObjs[0])
			System.out.println("revisit objs");
		
		System.out.print("UNCLAIMED: ");
		for (int i = start; i < upTo; i++)
			if (!dict.get(i).isClaimed())
				System.out.print(dict.get(i) +" ");
		System.out.println("\n");
		
		return " ";
	}
	
	
	/* ADJECTIVE MATCHING METHODS
	 *  - void matchAdjectives
	 *  - void addToArray(DictEntry add, DictEntry find)
	 *  - DictEntry locateNoun(String form)
	 *  - void findMatchForAdjective(int idx)
	 *  - void setMatchingAdj(int idx, String part)
	 *  - int getNumMatchingAdj(String part, String form, int originIdx)
	 *  TODO merge with other getNum methods
	 *  TODO cleanup & generalize noun-adjective matching methods
	 * USES
	 *  - boolean checkIfNounAdjUsable(String toCheck, String checkAgainst)
	 */
	
	
	/* matchAdjectives
	 * goes through clause and attempts to pair each adjective and number
	 * with exactly one noun
	 * if there is a pair and the adjective cannot be another part of speech,
	 * it will restrict the adjective to just that matching form
	 */
	private void matchAdjectives() {
		for (int i = start; i < upTo; i++) {
			if (dict.get(i).isClaimed())
				continue;

			for (String part: new String[] {"ADJ", "NUM"}) {
				if (dict.get(i).canBe(part) == -1)
					continue;
				if (!findMatchForAdjective(i))
					continue;
				
				String form = dict.get(i).getWord(part).getForm(0);
				form = form.substring(0, form.length()-4);
				DictEntry attached = locateNoun(form);
				
				if (attached != null && dict.get(i).canBe("!"+part) == -1) {
					addToList(dict.get(i), attached);
				}
			}

		}
	}
	
	/* addToList
	 * checks if 'find' is in any of the parsing lists
	 * if so, it adds 'add' to that list
	 * TODO update addToList as needed
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addToList(DictEntry add, DictEntry find) {
		ArrayList lists[] = {preps, objects, ablatives, subjects};
		for (ArrayList l : lists) {
			if (l != null && l.indexOf(find) != -1) {
				add.setClaimed();
				l.add(l.indexOf(find)+1, add);
				return;
			}
		}
	}
	
	/* locateNoun
	 * searches through clause for a noun/pronoun that is compatible with
	 * the given form
	 * returns the word if it finds one, returns null otherwise
	 */
	private DictEntry locateNoun(String form) {
		for (int i = start; i < upTo; i++) {
			for (String part : new String[] {"N", "PRON"}) {
				if (dict.get(i).canBe(part) == -1)
					continue;
				for (String f: dict.get(i).getWord(part).getForms())
					if (u.nounAdjUsable(f, form))
						return dict.get(i);
			}
	}
		return null;
	}
	
	/* findMatchForAdjective
	 * takes in the index of an adjective (or numeral) and checks how many nouns
	 * and pronouns in the clause it is compatible with
	 */
	private boolean  findMatchForAdjective(int idx) {
		int numWords = 0;
		String part;
		ArrayList<String> matchingForms = new ArrayList<String>();
		if (dict.get(idx).canBe("ADJ") != -1)
			part = "ADJ";
		else
			part = "NUM";
		
		for (String form: dict.get(idx).getWord(part).getForms()) {
			// removes type (positive, comparative, superlative, cardinal, etc)
			if (part.equals("ADJ"))
				form = form.substring(0, form.length()-4);
			int delta = getNumMatchingAdj("N", form, idx);
			delta += getNumMatchingAdj("PRON", form, idx);
			numWords += delta;
			if (delta > 0)
				matchingForms.add(form);
		}
				
		if (numWords == 1) {
			dict.get(idx).setPart(part);
			dict.get(idx).getWord(part).setForm(matchingForms.get(0));
			setMatchingNoun(idx, matchingForms.get(0));
			return true;
		}
		return false;
	}
	
	/* setMatchingNoun
	 * takes an index and a form
	 * sets the noun which matches the form and does not match the index so that
	 * it only has the matching form
	 */
	private void setMatchingNoun(int originIdx, String form) {
		for (int i = start; i < upTo; i++) {
			if (i == originIdx)
				continue;
			for (String part : new String[] {"NOUN", "PRONOUN"}) {
				if (dict.get(i).canBe(part) == -1)
					continue;
				for (String f2: dict.get(i).getWord(part).getForms())
					if (u.nounAdjUsable(form, f2)) {
						dict.get(i).getWord(part).setForm(f2);
						return;
					}
			}
		}
	}
	
	/* getNumMatchingAdj
	 * returns the number of words that can match a given part of speech and
	 * are compatible with a given form
	 */
	private int getNumMatchingAdj(String part, String form, int originIdx) {
		int n = 0;
		for (int i = start; i < upTo; i++) {
			if (dict.get(i).canBe(part) == -1)
				continue;
			if (i == originIdx)
				continue;
			for (String f2: dict.get(i).getWord(part).getForms())
				if (u.nounAdjUsable(form, f2))
					n++;
		}
		return n;
	}
	
	/* SUBJECT/OBJECT METHODS
	 *  - ArrayList<DictEntry> getWordsOfCase(ArrayList<String> typeForm, String[] c, boolean[] revisit)
	 *  - void canBeSubjObj(int i, ArrayList<DictEntry> subjObjs, String form, boolean[] revisit)
	 *  - boolean adjectiveFormMatches(DictEntry d, String part, String form)
	 *  - void addWordOfCase(ArrayList<DictEntry> subjObjs, int i, String form)
	 *  - String[] generateCaseList(String[] c, int formsLength)
	 * USES
	 *  - ArrayList<DictEntry> getAdjectivesFor(DictEntry d)
	 */
	
	//TODO handle multiple-N subjects/objects with CONJ separator
	//TODO handle NOM INFs, distinguish from complementary INFs and indirect statement
	//TODO ensure there are no double-additions to typeForm
	//TODO test getSubjects()
	private ArrayList<DictEntry> getWordsOfCase(ArrayList<String> typeForm,
			String[] c, boolean[] revisit) {
		// typeForm is subjectForm or objectForm from VerbFinder
		// c is 
		String[] cases = generateCaseList(c, typeForm.size());
		ArrayList<DictEntry> subjObjs = new ArrayList<DictEntry>();
		for (int f = 0; f < typeForm.size(); f++)
			for (int i = start; i < upTo; i++)
				canBeSubjObj(dict.get(i), subjObjs, 
						cases[f]+typeForm.get(f).substring(2), revisit);
		for (DictEntry d: subjObjs)
			d.setClaimed();
		return subjObjs;
	}
	
	private void canBeSubjObj(DictEntry d, ArrayList<DictEntry> subjObjs,
			String form, boolean[] revisit) {
		// TODO figure out what this does
		if (form.substring(2, 3).equals(form.substring(3, 4)))
			form = form.substring(0, 3);
		
		int numNouns = u.getNumWordsOfForm("N", form) + 
				u.getNumWordsOfForm("PRON", form);
		
		for (String part: new String[] {"N", "PRON"}) {
			
			boolean canBePart = d.canBe(part) != -1;
			if (d.isClaimed() || !canBePart)
				continue;
			
			boolean mustBeForm = d.getWord(part).canBe("!"+form) == -1;
			
			if (numNouns == 1 && d.getWord(part).canBe(form) != -1) {
				addWordOfCase(subjObjs, d, form);
				return;
			} else if (d.canBe("!"+part) == -1 && mustBeForm) {
				addWordOfCase(subjObjs, d, form);
			} else if (mustBeForm) {
				if (formsMatchADJN(d, part, form))
					addWordOfCase(subjObjs, d, form);
				else
					revisit[0] = true;
			}
		}
	}
	
	/* formsMatchADJN
	 * if the DictEntry can only be an adjective or N/PRON and all adjective
	 * forms match the given form, it sets the word to be N/PRON
	 * and returns true; otherwise it does nothing and returns false
	 * 
	 * used for cases where meaning has identical N and ADJ forms
	 * in the use-cases, the variable part is always "N" or "PRON"
	 */
	private boolean formsMatchADJN(DictEntry d, String part, String form) {
		for (Word w: d.getWords())
			if (!w.getPart().equals(part) && !w.getPart().equals("ADJ"))
				return false;

		for (String f: d.getWord("ADJ").getForms()) {
			if (!f.contains(form))
				return false;
		}
		d.setPart(part);
		return true;
	}
	
	private void addWordOfCase(ArrayList<DictEntry> subjObjs, DictEntry d, String form) {
		int idx = subjObjs.size();
		subjObjs.add(d);
		subjObjs.addAll(u.getAdjectivesFor(d));
		for (int x = idx; x < subjObjs.size(); x++) {
			subjObjs.get(x).setPart(subjObjs.get(x).getWord(0).getPart());
			subjObjs.get(x).getWord(0).setForm(form);
			subjObjs.get(x).setClaimed();
		}
	}
	
	private String[] generateCaseList(String[] c, int formsLength) {
		if (c.length != 0)
			return c;
		String[] a = new String[formsLength];
		for (int i = 0; i < formsLength; i++)
			a[i] = "NOM";
		return a;
	}
	
	/* UTILITY METHODS
	 *  - String toString()
	 *  - void findVocatives()
	 *  - void cleanup()
	 */
	
	
	public String toString() {
		String str = "";
		for (int i = start; i < upTo; i++)
			str += dict.get(i).toString() + dict.get(i).getPunct() +" ";
		return str;
	}

	/* findVocatives
	 * finds any vocatives (presumed to be nouns preceded by "O") and declares
	 * them to be so. Declares all other nouns & adjectives to be not vocatives
	 */
	private void findVocatives() {
		// first word of a clause can't be a vocative (we assume)
		for (Word w: dict.get(start).getWords())
			if (w.getPart().equals("N") || w.getPart().equals("ADJ") ||
					w.getPart().equals("PRON"))
				w.setForm("!VOC");
		
		for (int i = start+1; i < upTo; i++) {
			boolean isVoc = dict.get(i-1).getWord(0).toString().equals("O");
			for (Word w: dict.get(i).getWords()) {
				boolean isNoun = w.getPart().equals("N");
				boolean isPron = w.getPart().equals("PRON");
				boolean isAdj = w.getPart().equals("ADJ");
				if (isNoun && isPron && isAdj) {
					if (isVoc) {
						w.setForm("VOC");
					} else
						w.setForm("!VOC");
				}
			}
		}
		cleanup();
	}
	
	/* cleanup
	 * looks through the clause and removes any possible words that are not 
	 * conjunctions, interjections, or prepositions 
	 * with no valid forms remaining
	 */
	private void cleanup() {
		String Conj = " CONJ/INTERJ PREP ";
		for (int i = start; i < upTo; i++) {
			
			for (int j = 0; j < dict.get(i).getWords().size(); j++) {
				
				String cip = " "+ dict.get(i).getWord(j).getPart() +" ";
				boolean isCIP = Conj.contains(cip);
				// remove words only if they have no valid forms and
				// are not conjunction, interjection, or preposition
				if (dict.get(i).getWord(j).getForms() == null) {
					continue;
				}
				if (dict.get(i).getWord(j).getForms().size() == 0 && !isCIP) {
					dict.get(i).removeForm(j);
					j--;
				}
			}
		}
	}
	
}