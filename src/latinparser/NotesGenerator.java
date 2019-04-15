package latinparser;

import java.util.ArrayList;

import latinparser.translationfinders.VerbFinder;

/* NOTES GENERATOR METHODS
 *  - String getSingleNotes()
 *  TODO expand to general notes-generating methods
 */
public class NotesGenerator {
	private Utility u;
	private ArrayList<DictEntry> dict;
	private int start;
	private int upTo;
	
	public NotesGenerator(ArrayList<DictEntry> d, int startIndex, int toIndex, Utility util) {
		dict = d;
		start = startIndex;
		upTo = toIndex;
		u = util;
	}
	
	public String getSingleNotes() {
		String prep = "";
		if (dict.get(start).canBe(dict.get(start-1).getW(0).getPart()) != -1) {
			dict.get(start).setPart(dict.get(start-1).getW(0).getPart());
			ArrayList<String> removes = new ArrayList<String>();
			for (String form: dict.get(start).getW(0).getForms()) {
				boolean r = true;
				for (String f: dict.get(start-1).getW(0).getForms())
					if (f.substring(0, f.length()-4).equals(form.substring(0, form.length()-4)))
						r = false;
				if (r)
					removes.add(form);
			}
			for (String f: removes)
				dict.get(start).getW(0).setPart("!"+f);
			prep = " *";
		}
		String part = dict.get(start).getW(0).getPart();
		return getBasicNotesForm(dict.get(start), part, prep);
	}
	
	private String getBasicNotesForm(DictEntry d, String part, String prep) {
		if (part.equals("NUM")) return "";
		else if (part.equals("CONJ/INTERJ") || part.equals("PREP")) return "0";
		else if (part.equals("ADV")) return "0 0";
		else if (part.equals("ADJ") || part.equals("PRON"))
			return "0 0 "+ d.getW(0).getF(0).substring(6,7)+" * SUBST";
		else if (part.equals("V")) {
			ArrayList<Integer> idx = new ArrayList<Integer>();
			idx.add(start);
			VerbFinder VFinder = new VerbFinder(dict, start, upTo, u);
			char clauseType = VFinder.setClauseType(idx);
			return "0 0 "+ clauseType +" true";
		} else {
			if (d.getW(0).getF(0).contains(" S"))
				return "0 0 "+((latinparser.partsofspeech.Noun) d.getW(0)).getGender()+" a"+ prep;
			else
				return "0 0 "+((latinparser.partsofspeech.Noun) d.getW(0)).getGender()+" the"+ prep;
		}
	}
}
