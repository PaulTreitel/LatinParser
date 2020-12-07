package latinparser;



import java.util.ArrayList;

import latinparser.wordassistant.VerbFinder;
import latinparser.words.Noun;

/* NOTES GENERATOR METHODS
 *  - String getSingleNotes()
 *  TODO expand to general notes-generating methods
 */
public class NotesGenerator {
	private ArrayList<DictEntry> dict;
	private int start;
	private int upTo;
	
	public NotesGenerator(ArrayList<DictEntry> d, int startIndex, int toIndex) {
		dict = d;
		start = startIndex;
		upTo = toIndex;
	}
	
	public String getSingleNotes() {
		String prep = "";
		if (dict.get(start).canBe(dict.get(start-1).getWord(0).getPart()) != -1) {
			// if it can be same part of speech as last word, it is
			dict.get(start).setPart(dict.get(start-1).getWord(0).getPart());
			
			ArrayList<String> removes = new ArrayList<String>();
			for (String form: dict.get(start).getWord(0).getForms()) {
				boolean r = true;
				for (String f: dict.get(start-1).getWord(0).getForms())
					if (f.substring(0, f.length()-4).equals(form.substring(0, form.length()-4)))
						r = false;
				if (r)
					removes.add(form);
			}
			for (String f: removes)
				dict.get(start).getWord(0).setForm("!"+f);
			prep = " *";
		}
		String part = dict.get(start).getWord(0).getPart();
		return getBasicNotesForm(dict.get(start), part, prep);
	}
	
	private String getBasicNotesForm(DictEntry d, String part, String prep) {
		if (part.equals("NUM")) return "";
		else if (part.equals("CONJ/INTERJ") || part.equals("PREP")) return "0";
		else if (part.equals("ADV")) return "0 0";
		else if (part.equals("ADJ") || part.equals("PRON"))
			return "0 0 "+ d.getWord(0).getForm(0).substring(6,7)+" * SUBST";
		else if (part.equals("V")) {
			VerbFinder VFinder = new VerbFinder(dict, start, upTo);
			char clauseType = VFinder.setClauseType();
			return "0 0 "+ clauseType +" true";
		} else {
			if (d.getWord(0).getForm(0).contains(" S"))
				return "0 0 "+((Noun) d.getWord(0)).getGender()+" a"+ prep;
			else
				return "0 0 "+((Noun) d.getWord(0)).getGender()+" the"+ prep;
		}
	}
}
