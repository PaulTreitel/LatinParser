package latinparser.words;


import java.util.ArrayList;

import latinparser.LatinParser;
import latinparser.Utility;

public class Pronoun extends Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String word;
	private String part;
	
	public Pronoun(String origin, String c) {
		word = origin;
		codes = c;
		meaning = "";
		part = "";
		addPossForm("");
	}
	
	/* addPossForm
	 * loads pronoun contents from LatinParser file, then adds relevant ones
	 * TODO how exactly does this work?
	 */
	public void addPossForm(String entry) {
		String[] pronouns = LatinParser.getPronounsContents().split("\r\n\r\n");
		if (!getForm(pronouns, word)) {
			getForm(pronouns, word.substring(0, word.length()-3));
		}
	}
	
	private boolean getForm(String[] pronouns, String w) {
		for (String p: pronouns) {
			String[] lines = p.split("\r\n");
			for (int i = 0; i < lines.length-2; i++) {
				if (lines[i].split(" ")[0].equals(w)) {
					possForms.add(lines[i].substring(lines[i].length()-7, lines[i].length()));
					if (meaning.equals(""))
						meaning = lines[lines.length-1];
					if (part.equals("")) {
						String[] parts = lines[lines.length-2].split(", ");
						part = parts[parts.length-1];
					}
				}
			}
			if (!meaning.equals(""))
				return true;
		}
		return false;
	}
	
	public String translate(String notes) {
		String form = possForms.get(Integer.parseInt(notes.split(" ")[1]));
		if (part.equals("N"))
			return Noun.translate(meaning, form, notes);
		return Adjective.translate(meaning, form, notes);
	}
	
	public void setForm(String form) {
		boolean negated = form.charAt(0) == '!';
		String absoluteForm = Utility.expandNounAdjForm(form, negated);
		
		for (int i = possForms.size()-1; i >= 0; i--) {
			String currForm = possForms.get(i);
			boolean match = Utility.nounAdjMatch(currForm, absoluteForm);
			if (!negated && !match) {
				possForms.remove(i);
			} else if (negated && match) {
				possForms.remove(i);
			}
		}
	}
	
	public boolean canBe(String f) {
		return getForm(f) != null;
	}
	
	public String getForm(String formSearch) {
		boolean negated = formSearch.charAt(0) == '!';
		String absoluteForm = Utility.expandNounAdjForm(formSearch, negated);
		
		for (int i = 0; i < possForms.size(); i++) {
			String currForm = possForms.get(i);
			boolean match = Utility.nounAdjMatch(currForm, absoluteForm);
			if (!negated && match) {
				return currForm;
			} else if (negated && !match) {
				return currForm;
			}
		}
		return null;
	}
	
	public void addMeaning(String m) {}
	public String getPart() {return "PRON";}
	public ArrayList<String> getForms() {return possForms;}
	public String getForm(int idx) {return possForms.get(idx);}
}
