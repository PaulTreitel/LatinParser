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
	 */
	public void addPossForm(String entry) {
		if (!getFormsFromFile(word)) {
			// tries re-running it with an ending (eg -met, -nam, etc)
			getFormsFromFile(word.substring(0, word.length()-3));
		}
	}
	
	private boolean getFormsFromFile(String w) {
		String[][] allLines = LatinParser.getPronounLines();
		boolean foundForms = false;
		for (String[] currLines: allLines) {
			int startingForms = possForms.size();
			for (int i = 0; i < currLines.length-2; i++) {
				String[] elements = currLines[i].split(" "); // [word, case, number, gender]
				if (elements[0].equals(w)) {
					String form = elements[1] +" "+ elements[2] +" "+ elements[3];
					possForms.add(form);
				}
			}
			
			if (possForms.size() > startingForms) {
				meaning = currLines[currLines.length-1];
				String[] parts = currLines[currLines.length-2].split(", ");
				part = parts[parts.length-1];
				foundForms = true;
			}	
		}
		return foundForms;
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
