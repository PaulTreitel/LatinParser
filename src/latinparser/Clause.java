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
	private boolean revisitPreps = false;
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
	
	/**
	 * creates a Clause object
	 * @param d - a list representing the dictionary entries for each word in the clause
	 * @param sIdx - the start index of the clause within d
	 * @param eIdx - the end index of the clause within d (the clause goes up to,
	 * but does not include eIdx)
	 * @param checkCommas - whether there are commas within the clause or not
	 */
	public Clause(ArrayList<DictEntry> d, int sIdx, int eIdx,
			boolean checkCommas) {
		dict = d;
		start = sIdx;
		upTo = eIdx;
		processed = checkCommas;
		u = new Utility(dict, start, upTo);
	}
	
	/* CLAUSING METHODS
	 *  - ArrayList<Clause> process()
	 *  - String translateSubclauses()
	 *  - String translate()
	 */
	
	/**
	 * Generates an arraylist of subclauses, separated by commas in the text
	 * @return the list of subclauses
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
	
	/**
	 * combines and returns the translations of each subclause
	 * @return the combined translation of all subclauses
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
		
		/*
		// TODO figure out what the hell this is
		if (upTo-start == 1) {
			NotesGenerator NGen = new NotesGenerator(dict, start, upTo);
			String notes = NGen.getSingleNotes();
			return dict.get(start).getWord(0).translate(notes);
		}
		*/
		
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
		
		String[] objectForms = objectForm.toArray(new String[] {});
		if (!objectForm.contains("NOM")) {
			objects = getSubjectsObjects(objectForms, revisitObjs);
		}
		
		AblativeFinder ABLFinder = new AblativeFinder(dict, start, upTo, u); 
		ablatives = ABLFinder.getAblativeUses(verbIdx, preps);
		
		matchAdjectives();
		
		String[] subjectForms = generateSubjForms();
		subjects = getSubjectsObjects(subjectForms, revisitSubjs);
		
		if (objectForm.contains("NOM")) {
			objects = getSubjectsObjects(objectForms, revisitObjs);
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
			if (!dict.get(i).isClaimed()) {
				System.out.print(dict.get(i) +" ");
			}
		System.out.println("\n");
		
		return " ";
	}
	
	
	/* ADJECTIVE MATCHING METHODS
	 *  - void matchAdjectives
	 *  - void addToList(DictEntry search, DictEntry add)
	 *  - boolean findMatchForAdjective(int idx)
	 *  - void setMatchingNoun(int idx, String part)
	 *  TODO merge with other getNum methods
	 *  TODO cleanup & generalize noun-adjective matching methods
	 */
	
	
	/**
	 * Goes through the clause and attempts to pair each Adjective and Numeral
	 * with exactly one Noun. If there is a pair and the adjective cannot be
	 * another part of speech, it will restrict the adjective to just that
	 * matching form.
	 */
	private void matchAdjectives() {
		String[] parts = new String[] {"ADJ", "NUM"};
		for (int i = start; i < upTo; i++) {
			DictEntry currEntry = dict.get(i);
			if (currEntry.isClaimed())
				continue;

			for (String part: parts) {
				if (!currEntry.canBe(part))
					continue;
				if (!findMatchForAdj(i)) // TODO why does this work?
					continue;
				
				String form = currEntry.getWord(part).getForm(0);
				form = form.substring(0, form.length()-4);
				DictEntry attached = u.getWordByForm("N", form);
				if (attached == null)
					attached = u.getWordByForm("PRON", form);
				
				if (attached != null && !currEntry.canBe("!"+part)) {
					addToList(attached, currEntry);
				}
			}
		}
	}
	
	/**
	 * Looks for `search` in the set of lists (preps, objects, subjects,
	 * ablatives). If found, it will add `add` to that list immediately after `search`
	 * @param search - the entry to search for in the lists
	 * @param add - the entry to add right after the search entry
	 */
	// TODO update addToList as needed
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addToList(DictEntry search, DictEntry add) {
		ArrayList lists[] = {preps, objects, ablatives, subjects};
		for (ArrayList l : lists) {
			if (l != null && l.indexOf(search) != -1) {
				add.claim();
				l.add(l.indexOf(search)+1, add);
				return;
			}
		}
	}

	/**
	 * Takes the index of an Adjective or Numeral and determines how many Nouns
	 * and Pronouns it is compatible with. If it is only compatible with one
	 * other word, then it will pair those two together.
	 * @param idx - the index of the Adjective or Numberal to pair
	 * @return true if it was able to pair the adjective, false otherwise
	 */
	private boolean findMatchForAdj(int idx) {
		int numWords = 0;
		ArrayList<String> matchingForms = new ArrayList<String>();
		DictEntry d = dict.get(idx);
		String part = (d.canBe("ADJ")) ? "ADJ" : "NUM";
		
		
		for (String form: d.getWord(part).getForms()) {
			// removes type (positive, comparative, superlative, cardinal, etc)
			if (part.equals("ADJ"))
				form = form.substring(0, form.length()-4);
			int delta = u.getNumWordsOfForm("N", form) + u.getNumWordsOfForm("PRON", form);
			// don't count the current word if it could be a Noun or Pronoun
			if (d.canBe("N")) {
				delta--;
			}
			if (d.canBe("PRON")) {
				delta--;
			}
			numWords += delta;
			if (delta > 0)
				matchingForms.add(form);
		}
				
		if (numWords == 1) {
			d.setPart(part);
			// TODO why 0?
			d.getWord(part).setForm(matchingForms.get(0));
			setMatchingNoun(matchingForms.get(0));
			return true;
		}
		return false;
	}
	
	/**
	 * Sets the first Noun or Pronoun that can match the given form
	 * so that it can only be that form.
	 * @param form - the noun form to use
	 */
	private void setMatchingNoun(String form) {
		DictEntry matchingNoun = null;
		for (String part: new String[] {"N", "PRON"}) {
			matchingNoun = u.getWordByForm(part, form);
			if (matchingNoun != null) {
				// TODO also set part?
				matchingNoun.getWord(part).setForm(form);
				return;
			}
		}
	}
	
	
	/* SUBJECT/OBJECT METHODS
	 *  - ArrayList<DictEntry> getSubjectsObjects(String[] cases, boolean[] revisit)
	 *  - boolean isSubjObjMatch(DictEntry d, String form, boolean[] revisit)
	 *  - boolean adjFormMatches(DictEntry d, String part, String form)
	 *  - void addWordsByForm(ArrayList<DictEntry> subjObjs, DictEntry d, String form)
	 *  - String[] generateSubjForms()
	 */
	
	/**
	 * Takes a list of noun forms. Gathers all Nouns and Pronouns that *must*
	 * fit those forms, as well as any Adjectives or Numerals that accompany
	 * them.
	 * @param forms - the list of cases that the nouns must match
	 * @param revisit - reference for a flag to revisit the current operation
	 * @return list of nouns that must fit the given cases, along with their
	 * adjectives
	 */
	//TODO handle multiple-N subjects/objects with CONJ separator
	//TODO handle NOM INFs, distinguish from complementary INFs and indirect statement
	//TODO ensure there are no double-additions to typeForm
	//TODO test getSubjects()
	private ArrayList<DictEntry> getSubjectsObjects(String[] forms, boolean[] revisit) {
		ArrayList<DictEntry> subjObjs = new ArrayList<DictEntry>();
		for (int f = 0; f < forms.length; f++) {
			for (int i = start; i < upTo; i++) {
				DictEntry currEntry = dict.get(i);
				if (isSubjObjMatch(currEntry, forms[f], revisit)) {
					addWordsByForm(subjObjs, currEntry, forms[f]);
				}
			}
		}
		return subjObjs;
	}
	
	/**
	 * Determines if the given DictEntry has take the given form. If the result
	 * is inconclusive, it will set revisit[0] to be true.
	 * @param d - the DictEntry to look at
	 * @param form - the form to check for
	 * @param revisit - reference for a flag to revisit the current operation
	 * @return true if d has to take the form, false otherwise
	 */
	private boolean isSubjObjMatch(DictEntry d, String form, boolean[] revisit) {	
		int numNouns = u.getNumWordsOfForm("N", form) + 
				u.getNumWordsOfForm("PRON", form);
		
		for (String part: new String[] {"N", "PRON"}) {
			if (d.isClaimed() || !d.canBe(part))
				continue;
			
			boolean canBeForm = d.getWord(part).canBe(form);
			boolean mustBeForm = !d.getWord(part).canBe("!"+form);
			boolean adjVariantOfNoun = d.canBe("ADJ") && !adjFormsMatchNoun(d, part, form);
			if (mustBeForm && adjVariantOfNoun) {
				revisit[0] = true;
				return false;
			}
			return (numNouns == 1 && canBeForm) || mustBeForm;
		}
		return false;
	}
	
	/**
	 *If the DictEntry can only be an adjective or Noun and all adjective
	 * forms match the given form, it sets the word to be Noun
	 * and returns true; otherwise it does nothing and returns false.
	 * This is useful for cases where a word is an adjective but it also has a
	 * substantive noun counterpart. NOTE: this assumes we want the adjective
	 * version and not the noun version (which may be distinct from an adjective
	 * used substantively).
	 * @param d - a DictEntry that can be a Noun/Pronoun or Adjective
	 * @param part - either "N" for Noun or "PRON" for pronoun
	 * @param form - the form of the word to match against
	 * @return true if it is able to remove the Adjective part, leaving only the
	 * Noun/Pronoun part; false otherwise
	 */
	private boolean adjFormsMatchNoun(DictEntry d, String part, String form) {
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
	
	/**
	 * Adds the given DictEntry to the list, then adds all adjectives for that
	 * word to the list. Sets the part of speech of all the new words to the
	 * first part in their lists, then sets the form of all the new words to the
	 * given form. Sets the claimed flag for all words added this way.
	 * @param subjObjs - the list to add words to
	 * @param d - the DictEntry to add first
	 * @param form - the form that all added words must conform to.
	 */
	private void addWordsByForm(ArrayList<DictEntry> subjObjs, DictEntry d, String form) {
		int newWordsIdx = subjObjs.size();
		subjObjs.add(d);
		subjObjs.addAll(u.getAdjectivesFor(d));
		for (int x = newWordsIdx; x < subjObjs.size(); x++) {
			DictEntry currEntry = subjObjs.get(x);
			currEntry.setPart(currEntry.getWord(0).getPart());
			currEntry.getWord(0).setForm(form);
			currEntry.claim();
		}
	}
	
	/**
	 * Uses the existing subjectForm list to generate an array of Noun forms for
	 * the subjects of the clause's verbs. subjectForm includes information
	 * about the person of the subject which is not represented in a Noun form;
	 * this is why we don't overwrite the subjectForm list.
	 * @return an array of Noun forms for the subjects of the clause
	 */
	private String[] generateSubjForms() {
		String[] a = new String[subjectForm.size()];
		for (int i = 0; i < subjectForm.size(); i++) {
			String currForm = subjectForm.get(i);
			char number = currForm.charAt(currForm.length() - 1);
			a[i] = Utility.expandNounAdjForm("NOM " + number, false);
		}
		return a;
	}
	
	
	/* UTILITY METHODS
	 *  - String toString()
	 *  - void findVocatives()
	 *  - void cleanup()
	 */
	
	/**
	 * generates a string representing the clause
	 * @return the string representation of the clause
	 */
	public String toString() {
		String str = "\"";
		for (int i = start; i < upTo; i++)
			str += dict.get(i).toString() + dict.get(i).getPunct() +" ";
		return str + "\"";
	}

	/**
	 * Finds any vocatives and declares them to be so, then declares all other
	 * Nouns, Adjectives, and Pronouns to be not vocatives. A vocative is
	 * presumed to be preceded with "O".
	 */
	//TODO find a more conservative way to do this
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
				if (isNoun || isPron || isAdj) {
					if (isVoc) {
						w.setForm("VOC");
					} else
						w.setForm("!VOC");
				}
			}
		}
		cleanup();
	}
	
	/**
	 * Looks through the clause and removes any possible words that are not 
	 * conjunctions, interjections, or prepositions with no valid forms remaining.
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
					dict.get(i).removeWord(j);
					j--;
				}
			}
		}
	}
	
}