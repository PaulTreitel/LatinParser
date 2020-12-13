package latinparser.words;

import java.util.ArrayList;

import latinparser.Utility;

public class Adverb extends Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private static final int ADV_COMPARISON_START = 28;
	private static final int ADV_COMPARISON_END = 31;
	
	public Adverb(String m, String c) {
		meaning = m;
		codes = c;
	}
		
	public String translate(String notes) {
		String form = possForms.get(Integer.parseInt(notes.split(" ")[1]));
		String mean = meaning.split(";|,|/")[Integer.parseInt(notes.split(" ")[0])].trim();
		if (form.equals("POS"))
			return mean;
		else if (form.equals("COM"))
			return "more " + mean;
		return "most " + mean;
	}
	
	/* addPossForm
	 * takes a String representation of a word form and adds it to the list of possible forms
	 * ignores Early Latin word forms
	 */
	public void addPossForm(String e) {
		if (!e.contains("Early")) {
			possForms.add(e.substring(ADV_COMPARISON_START, ADV_COMPARISON_END));
		}
	}
	
	public void setForm(String form) {
		boolean negated = form.charAt(0) == '!';
		String absoluteForm = form.substring(1); // part without '!'
		
		for (int i = possForms.size()-1; i >= 0; i--) {
			String currForm = possForms.get(i);
			if (!negated && !currForm.contains(form)) {
				possForms.remove(i);
			} else if (negated && currForm.contains(absoluteForm)) {
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
	
	public void addMeaning(String m) {meaning += m;}
	public String getPart() {return "ADV";}
	public ArrayList<String> getForms() {return possForms;}
	public String getForm(int idx) {return possForms.get(idx);}
}
