package latinparser.words;

import java.util.ArrayList;

import latinparser.Utility;

public class Numeral extends Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private static final int NUM_FORM_START = 32;
	private static final int NUM_FORM_END = 43;
	
	public Numeral(String m, String c) {
		meaning = m.replace("as a ROMAN NUMERAL", "").replace("1th", "1st");
		meaning = meaning.replace("2th", "2nd").replace("3th", "3rd").trim();
		codes = c;
	}
	
	/* addRemainingForms
	 * adds all word forms to the list of possible forms
	 */
	private void addRemainingForms() {
		if (possForms.size() == 1)
			possForms.remove(0);
		for (String c: new String[] {"NOM", "GEN", "DAT", "ACC", "ABL", "VOC"})
			for (String n: new String[] {"S", "P"})
					possForms.add(c +" "+ n +" X");
	}
	
	public String translate(String notes) {
		return meaning.split(";|,|/|-")[0].trim();
	}
	
	/* addPossForm
	 * takes a String representation of a word form and adds it to the list of possible forms
	 * ignores Early Latin word forms
	 */
	public void addPossForm(String e) {
		if (e.contains("Early"))
			return;
		if (e.equals("1")) {
			boolean hasUniqueForm = possForms.size() == 1 && !possForms.get(0).contains(" X ");
			if (possForms.size() == 0 || !hasUniqueForm) {
				addRemainingForms();
			}
			return;
		}
		possForms.add(e.substring(NUM_FORM_START, NUM_FORM_END));
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
	
	public String getForm(int idx) {return possForms.get(idx);}
	public ArrayList<String> getForms() {return possForms;}
	
	public void addMeaning(String m) {meaning += m;}
	public String getPart() {return "NUM";}
}