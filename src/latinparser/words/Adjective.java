package latinparser.words;

import java.util.ArrayList;

import latinparser.Utility;

public class Adjective extends Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private static final int ADJ_FORM_START = 32;
	private static final int ADJ_FORM_END = 43;
	private static final int ADJ_FORM_LENGTH = ADJ_FORM_END - ADJ_FORM_START;
	
	public Adjective(String mean, String c) {
		meaning = mean;
		codes = c;
	}
	
	/* addPossForm
	 * takes a String representation of a word form/line from WORDS
	 * and adds it to the list of possible forms
	 * ignores malformed strings of less than 11 characters or from Early Latin
	 * does voodoo magic to long strings, presumably based on WORDS output
	 */
	public void addPossForm(String e) {
		// anything shorter than 11 characters is malformed
		if (e.length() < ADJ_FORM_LENGTH) {
			return;
		}
		// ignore Early Latin words
		if (e.contains("Early")) {
			return;
		}
		if (e.length() == ADJ_FORM_LENGTH) {
			possForms.add(e);
			return;
		}
		
		/* checks for spaces around the character-position of the form's number (S or P)
		used to filter out WORDS output lines that aren't actually forms */
		String beforeNumber = e.substring(ADJ_FORM_START + 3, ADJ_FORM_START + 4);
		String afterNumber = e.substring(ADJ_FORM_START + 5, ADJ_FORM_START + 6);
		if (beforeNumber.equals(" ") && afterNumber.equals(" ")) {
			possForms.add(e.substring(ADJ_FORM_START, ADJ_FORM_END));
		}
	}
	
	// static so that Pronoun can use it
	public static String translate(String mean, String form, String notes) {
		String transl = mean.split("; |, |/")[Integer.parseInt(notes.split(" ")[0])].trim();
		transl = Noun.getArticle(mean, notes) + transl;
		if (!notes.contains("SUBST"))
			return transl;
		else if (form.contains(" S")) {
			if (form.contains(" M"))
				return transl +" man";
			else if (form.contains(" F"))
				return transl +" woman";
			return transl +"thing ";
		} else {
			if (form.contains(" M"))
				return transl +" men";
			else if (form.contains(" F"))
				return transl +" women";
			return transl +"things ";
		}
	}
	
	public String translate(String notes) {
		return Adjective.translate(meaning, possForms.get(Integer.parseInt(notes.split(" ")[1])), notes);
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
	public String getPart() {return "ADJ";}
}
