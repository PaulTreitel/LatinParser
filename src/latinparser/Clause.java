package latinparser;

import java.util.ArrayList;
import latinparser.partsofspeech.*;
import latinparser.translationfinders.*;

public class Clause {
	private static final String SUM_FORM = "est                  V      5 1 IMPF"; //" ACTIVE  IND 3 S";
	
	private Utility u;
	private ArrayList<DictEntry> dict;
	private boolean[] revisitSubjs = new boolean[1];
	private boolean[] revisitObjs = new boolean[1];
	private boolean processed;
	private boolean revisitPreps;
	private boolean twoWordV;
	private int start;
	private int upTo;
	
	private ArrayList<DictEntry> preps;
	private ArrayList<Integer> verbIdx;
	private ArrayList<String> subjectForm;
	private ArrayList<String> objectForm;
	private ArrayList<DictEntry> objects;
	private ArrayList<DictEntry> ablatives;
	private ArrayList<DictEntry> subjects;
	
	public Clause(ArrayList<DictEntry> d, int startIndex, int uptoIndex, boolean checkCommas) {
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
	
	private ArrayList<Clause> process() {
		ArrayList<Clause> subclauses = new ArrayList<Clause>();
		int s = start;
		for (int i = start; i < upTo; i++)
			if (dict.get(i).getPunct().equals(",") && i+1 != upTo) {
				subclauses.add(new Clause(dict, s, i+1, true));
				s = i+1;
			} 
		if (s != start) {
			subclauses.add(new Clause(dict, s, upTo, true));
		}
		return subclauses;
	}
	
	private String translateSubclauses() {
		ArrayList<Clause> subclauses = process();
		String translation = "";
		for (Clause c: subclauses)
			translation += c.translate();
		return translation;
	}
	
	//TODO implement indirect statement, distinguish from complementary INF and NOM INF
	//TODO uses of the DAT
	//TODO uses of the GEN
	//TODO negations (non, ne, nonne, etc)
	//TODO proviso clauses (p264 Wheelock's) - add?
	public String translate() {
		System.out.println("CLAUSE "+ dict.subList(start, upTo));
		if (!processed) {
			String translation =  translateSubclauses();
			if (!translation.equals(""))
				return translation;
		}
		if (upTo-start == 1) {
			NotesGenerator NGen = new NotesGenerator(dict, start, upTo, u);
			String notes = NGen.getSingleNotes();
			return dict.get(start).getW(0).translate(notes);
		}
		findVocatives();
		
		PrepositionFinder PFinder = new PrepositionFinder(dict, start, upTo, u);
		preps = PFinder.findPrepositionPhrases();
		revisitPreps = PFinder.getRevisit();
		System.out.println("PREPS "+ preps);
		
		matchAdjectives();
		
		VerbFinder VFinder = new VerbFinder(dict, start, upTo, u);
		verbIdx = VFinder.getVerbInfo();
		System.out.println("VERB INDICES "+ verbIdx);
		subjectForm = VFinder.setSubjectForms(verbIdx);
		objectForm = VFinder.setObjectForms(verbIdx);
		
		findTwoWordVerbs();
		System.out.println("VERB INDICES "+ verbIdx);
		
		//char clauseType = VFinder.setClauseType(verbIdx);
		//System.out.println(clauseType);
		
		if (!objectForm.contains("NOM")) {
			objects = getWordsOfCase(objectForm, objectForm.toArray(new String[] {}), revisitObjs);
			System.out.println("OBJECTS "+ objects);
			System.out.println(objectForm);
		}
		
		AblativeFinder ABLFinder = new AblativeFinder(dict, start, upTo, u); 
		ablatives = ABLFinder.getAblativeUses(verbIdx, preps);
		System.out.println("ABLATIVES "+ ablatives);
		
		subjects = getWordsOfCase(subjectForm, new String[] {}, revisitSubjs);
		System.out.println("SUBJECTS "+ subjects);
		System.out.println(subjectForm);
		
		if (objectForm.contains("NOM")) {
			objects = getWordsOfCase(objectForm, objectForm.toArray(new String[] {}), revisitObjs);
			System.out.println("OBJECTS "+ objects);
			System.out.println(objectForm);
		}
		
		//TODO revisit PREPs, subjs, objs if necessary
		if (revisitPreps)
			System.out.println("revisit preps");
		if (revisitSubjs[0])
			System.out.println("revisit subjs");
		if (revisitObjs[0])
			System.out.println("revisit objs");
		
		for (int i = start; i < upTo; i++)
			if (!dict.get(i).isClaimed())
				System.out.print(dict.get(i) +" ");
		System.out.println();
		return " ";
	}
	
	/* TWO WORD VERB FORM METHODS
	 *  - void findTwoWordVerbs()
	 *  - String genFormType(DictEntry d)
	 *  - int checkForParticiple()
	 *  - int checkForSumPart()
	 */
	
	private void findTwoWordVerbs() {
		int ppl = checkForParticiple();
		int sum = checkForSumPart();
		w:
			while (ppl != -1 && sum != -1) {
				System.out.println(sum +" "+ ppl);
				for (String npart: new String[] {"PERF", "PLUP", "FUTP"})
					if (dict.get(sum).getW("V").canBe(npart) != -1)
						break w;
				twoWordV = true;
				dict.get(sum).getW("V").addPossForm(SUM_FORM+genFormType(dict.get(sum)));
				dict.get(sum).getW("V").setPart("IMPF");
				verbIdx.add(verbIdx.indexOf(sum)+1, ppl);
				dict.get(ppl).setClaimed();
				ppl = checkForParticiple();
				sum = checkForSumPart();
			}
	}
	
	private String genFormType(DictEntry d) {
		System.out.println(d);
		System.out.println(d.getWords());
		String f = " ";
		String existing = d.getW("V").getF(0);
		if (existing.contains(" A "))
			f += "ACTIVE ";
		else
			f += "PASSIVE";
		return f + existing.substring(existing.length()-12);
	}
	
	private int checkForParticiple() {
		for (int i = start; i < upTo; i++)
			if (dict.get(i).canBe("V") != -1 && dict.get(i).getW("V").canBe("PPL") != -1 && !dict.get(i).isClaimed())
				return i;
		return -1;
	}
	
	private int checkForSumPart() {
		for (int idx: verbIdx)
			if ((dict.get(idx).canBe("V") != -1 && dict.get(idx).getW("V").canBe("!VER") == -1 &&
			dict.get(idx).getW("V").toString().substring(0, 3).equals("be;")) || dict.get(idx).toString().equals("iri"))
				return idx;
		return -1;
	}
	
	/* ADJECTIVE MATCHING METHODS
	 *  - void matchAdjectives
	 *  - void addToArray(DictEntry add, DictEntry find)
	 *  - DictEntry locateNoun(String form)
	 *  - void findMatchForAdjective(int idx)
	 *  - void setMatchingAdj(int idx, String part)
	 *  - int getNumMatchingAdj(String part, String form, int originIdx) TODO merge with other getNum methods
	 * USES
	 *  - boolean checkIfNounAdjUsable(String toCheck, String checkAgainst)
	 */
	
	//TODO generalize for Word w such that !w.isClaimed()
	private void matchAdjectives() {
		for (int i = start; i < upTo; i++) {
			for (String part: new String[] {"ADJ", "NUM"})
				if (!dict.get(i).isClaimed() && dict.get(i).canBe(part) != -1) {
					findMatchForAdjective(i);
					String form = dict.get(i).getW(part).getF(0);
					DictEntry attached = locateNoun(form.substring(0, form.length()-4));
					if (attached != null)
						addToArray(dict.get(i), attached);
				}
		}
	}
	
	//TODO update addToArray as needed
	private void addToArray(DictEntry add, DictEntry find) {
		if (preps != null && preps.indexOf(find) != -1) {
			add.setClaimed();
			preps.add(preps.indexOf(find)+1, add);
		} else if (objects != null && objects.indexOf(find) != -1) {
			add.setClaimed();
			objects.add(objects.indexOf(find)+1, add);
		} else if (ablatives != null && ablatives.indexOf(find) != -1) {
			add.setClaimed();
			ablatives.add(ablatives.indexOf(find)+1, add);
		} else if (subjects != null && subjects.indexOf(find) != -1) {
			add.setClaimed();
			subjects.add(subjects.indexOf(find)+1, add);
		}
	}
	
	private DictEntry locateNoun(String form) {
		for (int i = start; i < upTo; i++)
			if (dict.get(i).canBe("N") != -1)
				for (String f: dict.get(i).getW("N").getForms())
					if (u.checkIfNounAdjUsable(f, form))
						return dict.get(i);
		return null;
	}
	
	private void findMatchForAdjective(int idx) {
		int numWords = 0;
		String part = "NUM";
		if (dict.get(idx).canBe("ADJ") != -1)
			part = "ADJ";
		for (String form: dict.get(idx).getW(part).getForms()) {
			String f = form.substring(0, form.length()-4);
			numWords += getNumMatchingAdj("N", f, idx) + getNumMatchingAdj("PRON", f, idx);
		}
		if (numWords != 1)
			return;
		setMatchingAdj(idx, part);
	}
	
	private void setMatchingAdj(int idx, String part) {
		for (String form: dict.get(idx).getW(part).getForms()) {
			String f = form.substring(0, form.length()-4);
			if (getNumMatchingAdj("N", f, idx) + getNumMatchingAdj("PRON", f, idx) == 1) {
				dict.get(idx).setPart(part);
				dict.get(idx).getW(part).setPart(f);
				break;
			}
		}
	}
	
	private int getNumMatchingAdj(String part, String form, int originIdx) {
		int n = 0;
		for (int i = start; i < upTo; i++)
			if (dict.get(i).canBe(part) != -1 && i != originIdx)
				for (String f2: dict.get(i).getW(part).getForms())
					if (u.checkIfNounAdjUsable(form, f2))
						n++;
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
	private ArrayList<DictEntry> getWordsOfCase(ArrayList<String> typeForm, String[] c, boolean[] revisit) {
		String[] cases = generateCaseList(c, typeForm.size());
		ArrayList<DictEntry> subjObjs = new ArrayList<DictEntry>();
		for (int f = 0; f < typeForm.size(); f++)
			for (int i = start; i < upTo; i++)
				canBeSubjObj(i, subjObjs, cases[f]+typeForm.get(f).substring(2), revisit);
		for (DictEntry d: subjObjs)
			d.setClaimed();
		return subjObjs;
	}
	
	private void canBeSubjObj(int i, ArrayList<DictEntry> subjObjs, String form, boolean[] revisit) {
		if (form.substring(2, 3).equals(form.substring(3, 4))) form = form.substring(0, 3);
		int n = u.getNumWordsOfForm("N", form) + u.getNumWordsOfForm("PRON", form);
		for (String part: new String[] {"N", "PRON"})
			if (!dict.get(i).isClaimed() && 
					n == 1 && dict.get(i).canBe(part) != -1 && dict.get(i).getW(part).canBe(form) != -1) {
				addWordOfCase(subjObjs, i, form);
				break;
			} else if (dict.get(i).canBe("!"+part) == -1 && dict.get(i).canBe(part) != -1 && 
					dict.get(i).getW(part).canBe("!"+form) == -1 && !dict.get(i).isClaimed()) {
				addWordOfCase(subjObjs, i, form);
			} else if (dict.get(i).canBe(part) != -1 && dict.get(i).getW(part).canBe("!"+form) == -1 && 
					!dict.get(i).isClaimed()) {
				if (adjectiveFormMatches(dict.get(i), part, form))
					addWordOfCase(subjObjs, i, form);
				else
					revisit[0] = true;
			}
	}
	
	private boolean adjectiveFormMatches(DictEntry d, String part, String form) {
		for (Word w: d.getWords())
			if (!w.getPart().equals(part) && !w.getPart().equals("ADJ"))
				return false;
		for (String f: d.getW("ADJ").getForms())
			if (!f.contains(form))
				return false;
		d.setPart(part);
		return true;
	}
	
	private void addWordOfCase(ArrayList<DictEntry> subjObjs, int i, String form) {
		int idx = subjObjs.size();
		subjObjs.add(dict.get(i));
		subjObjs.addAll(u.getAdjectivesFor(dict.get(i)));
		for (int x = idx; x < subjObjs.size(); x++) {
			subjObjs.get(x).setPart(subjObjs.get(x).getW(0).getPart());
			subjObjs.get(x).getW(0).setPart(form);
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

	private void findVocatives() {
		for (Word w: dict.get(start).getWords())
			if (w.getPart().equals("N") || w.getPart().equals("ADJ") || w.getPart().equals("PRON"))
				w.setPart("!VOC");
		for (int i = start+1; i < upTo; i++) {
			if (dict.get(i).getW(0).getPart().equals("N") && !dict.get(i-1).getW(0).toString().equals("Oh!"))
				for (Word w: dict.get(i).getWords()) {
					if (w.getPart().equals("N") || w.getPart().equals("ADJ") || w.getPart().equals("PRON"))
						w.setPart("!VOC");
				}
			else if (dict.get(i).getW(0).getPart().equals("N"))
				for (Word w: dict.get(i).getWords())
					if (w.getPart().equals("N") || w.getPart().equals("ADJ") || w.getPart().equals("PRON"))
						w.setPart("VOC");
		}
		cleanup();
	}
	
	private void cleanup() {
		for (DictEntry d: dict)
			for (int i = 0; i < d.getWords().size(); i++) {
				if (((d.getW(i).getForms().size() == 1 && d.getW(i).getF(0).trim().length() == 0) ||
						d.getW(i).getForms().size() == 0)
						&& !" CONJ/INTERJ PREP ".contains(" "+d.getW(i).getPart()+" ")) {
					d.removeForm(i);
					i--;
				}
			}
	}
	
}