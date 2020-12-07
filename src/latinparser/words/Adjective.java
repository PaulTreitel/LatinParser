package latinparser.words;

import java.util.ArrayList;

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
		
		// checks for spaces around the character-position of the form's number (S or P)
		// used to filter out WORDS output lines that aren't actually forms
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
	
	/* canBe
	 * takes a string and checks if it is in the list of possible word forms
	 * supports negation where the first character of the string is '!'
	 * returns the index of a possible matching form if there is one, -1 otherwise
	 */
	public int canBe(String f) {
		boolean negated = f.charAt(0) == '!';
		String absoluteForm = f.substring(1); // form without '!'
		
		for (int i = 0; i < possForms.size(); i++) {
			String currForm = possForms.get(i);
			if (!negated && currForm.contains(f)) {
				return i;
			} else if (negated && !currForm.contains(absoluteForm)) {
				return i;
			}
		}
		return -1;
	}
	
	/* setForm
	 * takes a string and removes any possible word forms that do not match
	 * supports negation where the first character of the string is '!'
	 */
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
	
	public void addMeaning(String m) {meaning += m;}
	public String getPart() {return "ADJ";}
	public ArrayList<String> getForms() {return possForms;}
	public String getForm(int idx) {return possForms.get(idx);}
}
