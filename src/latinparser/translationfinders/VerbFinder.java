package latinparser.translationfinders;

import java.util.ArrayList;

import latinparser.DictEntry;
import latinparser.LatinParser;
import latinparser.Utility;

/* VerbFinder
 *  - ArrayList<Integer> getVerbInfo()
 *  - ArrayList<Integer> findVerb()
 *  - ArrayList<Integer> stealVerb()
 *  - ArrayList<Integer> stealLaterVerb()
 *  - int getStartOfLastClause()
 *  - void setSubjectForms(ArrayList<Integer> vIndices)
 *  - void setClauseType(ArrayList<Integer> vIndices)
 *  - void setObjectForms(ArrayList<Integer> vIndices)
 */

//overhaul with .getW() that isn't 0?
public class VerbFinder {
	private Utility u;
	private ArrayList<DictEntry> dict;
	private int start;
	private int upTo;
	
	public VerbFinder(ArrayList<DictEntry> d, int sIdx, int tIdx, Utility util) {
		dict = d;
		start = sIdx;
		upTo = tIdx;
		u = util;
	}
	
	//TODO test getVerbInfo()
	//TODO infinitives (indirect statement)?
	//TODO make verb-borrowing conditional on there being a subj/obj not part of a list
	public ArrayList<Integer> getVerbInfo() {
		ArrayList<Integer> indices = findVerb();
		if (indices.size() == 0 && !u.isInList()) {
			indices = stealVerb();
		}
		for (int idx: indices) {
			dict.get(idx).setPart("V");
			dict.get(idx).setClaimed();
		}
		return indices;
	}
	
	public ArrayList<String> setSubjectForms(ArrayList<Integer> vIndices) {
		ArrayList<String> subjectForm = new ArrayList<String>();
		for (int idx: vIndices) {
			int formidx = dict.get(idx).getW(0).canBe(" VER");
			String form = dict.get(idx).getW(0).getF(formidx);
			subjectForm.add(form.substring(form.length()-8, form.length()-4));
		}
		return subjectForm;
	}
	
	public ArrayList<String> setObjectForms(ArrayList<Integer> vIndices) {
		ArrayList<String> objectForm = new ArrayList<String>();
		for (int idx: vIndices)
			cases:
				for (String c: new String[] {"GEN", "DAT", "ABL", "ACC"})
					if (dict.get(idx).getW(0).toString().toUpperCase().contains(c)) {
						objectForm.add(c);
						break cases;
					} else if (c.equals("ACC"))
						objectForm.add("ACC");
		for (int i = 0; i < objectForm.size(); i++)
			if (dict.get(vIndices.get(i)).getW(0).toString().substring(0, 3).equals("be;"))
				objectForm.set(i, "NOM");
		return objectForm;
	}
	
	public char setClauseType(ArrayList<Integer> vIndices) {
		//TODO ID type of clause
		return ' ';
	}
	
	private ArrayList<Integer> findVerb() {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		int conj = 0;
		int vcount = 0;
		for (int i = start; i < upTo; i++) {
			if (dict.get(i).getW(0).getPart().equals("V")) {
				perV:
					for (String form: dict.get(i).getW(0).getForms()) {
						if (form.contains(" VER") && vcount == conj) {
							indices.add(i);
							if (vcount == 0)
								vcount++;
							break perV;
						} else if (form.contains(" VER") && vcount < conj)
							vcount++;
						else if (dict.get(i).getW(0).getPart().equals("CONJ/INTERJ"))
							conj++;
					}
			}
		}
		return indices;
	}
	
	private ArrayList<Integer> stealVerb() {
		if (start == 0)
			return stealLaterVerb();
		ArrayList<Integer> indices = new ArrayList<Integer>();
		int e = getStartOfLastClause();
		for (int i = start-1; i > e; i--) {
			if (dict.get(i).getW(0).getPart().equals("V"))
				for (String form: dict.get(i).getW(0).getForms())
					if (form.contains(" VER")) {
						indices.add(i);
						return indices;
					}
		}
		System.out.println("HELP stealVerb()");
		return indices;
	}
	
	private ArrayList<Integer> stealLaterVerb() {
		int keywords = 0;
		int verbs = 0;
		ArrayList<Integer> indices = new ArrayList<Integer>();
		all:
		for (int i = upTo; i < dict.size(); i++) {
			if (LatinParser.isKeyword(i))
				keywords++;
			else if (dict.get(i).getW(0).getPart().equals("V") && keywords == verbs)
				for (String form: dict.get(i).getW(0).getForms()) {
					if (form.contains(" VER")) {
						indices.add(i);
						break all;
					}
				}
			else if (dict.get(i).getW(0).getPart().equals("V") && verbs < keywords)
				verbs++;
		}
		return indices;
	}
	
	private int getStartOfLastClause() {
		for (int i = start-1; i >= 0; i--) {
			if (LatinParser.isKeyword(i) || dict.get(i).getPunct().equals(";"))
				return i;
		}
		return 0;
	}
}
