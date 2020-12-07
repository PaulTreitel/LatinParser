package latinparser.words;

import java.util.ArrayList;

public class Verb extends Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String form;
	private String chosenMeaning;
	private char gender;
	private static final int VERB_TYPE_START = 21;
	private static final int VERB_FORM_START = 32;
	private static final int CONJUGATED_FORM_END = 38;
	private static final int PARTICIPLE_FORM_END = 46;
	private static final int SUPINE_FORM_END = 39;
	
	public Verb(String m, String co) {
		meaning = m;
		codes = co;
	}
	
	/* addPossForm
	 * takes a String representation of a word form and adds it to the list of possible forms
	 * ignores Early Latin word forms and forms under 30 characters
	 */
	public void addPossForm(String form) {
		// TODO magic number galore
		if (form.length() < 30  || form.contains("Early")) {
			return;
		}
		String addition = "";
		switch (form.substring(VERB_TYPE_START, VERB_TYPE_START + 6)) {
		case "V     ":
			// of the form "PRES P IND 2 S"
			addition = form.substring(VERB_FORM_START, CONJUGATED_FORM_END) 
			+ form.substring(44, 52) +" VER";
			break;
		case "VPAR  ":
			// of the form "NOM S M PERF P PPL"
			addition = form.substring(VERB_FORM_START, PARTICIPLE_FORM_END) 
			+ form.substring(52);
			break;
		case "SUPINE":
			// of the form "ACC S N"
			addition = form.substring(VERB_FORM_START, SUPINE_FORM_END) +" SUP";
			break;
		default:
			return;
		}
		possForms.add(addition.replaceAll("   ", " A "));
	}
	
	public String translate(String notes) {
		if (notes.split(" ").length == 5)
			gender = notes.split(" ")[4].charAt(0);
		form = possForms.get(Integer.parseInt(notes.split(" ")[1]));
		chosenMeaning = meaning.split(";|,|/")[Integer.parseInt(notes.split(" ")[0])].trim();
		if (chosenMeaning.equals("be") && notes.split(" ")[3].equals("true"))
			return addPersonAndNumber(' ') + getSumForm();
		else if (chosenMeaning.equals("be"))
			return getSumForm();
		if (form.contains("VER")) {
			String transl = "";
			if (notes.split(" ")[3].equals("true"))
				transl = addPersonAndNumber(' ');
			if (form.contains("IND") && form.contains(" A "))
				return AindicTranslate(transl);
			else if (form.contains("IND") && form.contains(" P "))
				return PindicTranslate(transl);
			else
				return subjTranslate(transl, notes.split(" ")[2].charAt(0));
		} else if (form.contains("IMP")) {
			return imperativeTranslate();
		} else if (form.contains("PPL"))
			return participleTranslate();
		else if (form.contains("INF"))
			return infinitiveTranslate(form);
		else
			return infinitiveTranslate("PRES A ");
	}
	
	private String AindicTranslate(String transl) {
		if (form.contains("PRES")) {
			if (form.contains("3 S") && form.contains("IND"))
				return transl + chosenMeaning +"s";
			else
				return transl + chosenMeaning;
		} else if (form.contains("FUT "))
			return transl + "will " + chosenMeaning; 
		else if (form.contains("IMPF"))
			return transl + getSumForm() +" "+ changeEending("ing");
		else if (form.contains("PERF"))
			return transl + changeEending("ed");
		else if (form.contains("PLUP"))
			return transl + "had " + changeEending("ed");
		else
			return transl +"will have "+ changeEending("ed");
	}
	
	private String PindicTranslate(String transl) {
		if (form.contains("PRES"))
			return transl + getSumForm() +" "+ changeEending("ed");
		else if (form.contains("FUT "))
			return transl +"will be "+ changeEending("ed");
		else if (form.contains("IMPF"))
			return transl + getSumForm() +" "+ changeEending("ed");
		else if (form.contains("PERF")) {
			if (form.contains("3 S"))
				return transl +"has been "+ changeEending("ed");
			return transl + "have been "+ changeEending("ed");
		} else if (form.contains("PLUP"))
			return transl +"had been "+ changeEending("ed");
		else
			return transl +"will have been "+ changeEending("ed");
	}
	
	private String subjTranslate(String transl, char clause) {
		if (form.contains(" A ")) {
		String r = exceptionAsubj(transl, clause);
		if (r != "")
			return r;
		else if (form.contains("PRES"))
			return presAsubj(transl, clause);
		else
			return pastAsubj(transl, clause);
		} else {
			String r = exceptionPsubj(transl, clause);
			if (r != "")
				return r;
			else if (form.contains("PRES"))
				return presPsubj(transl, clause);
			else
				return pastPsubj(transl, clause);
		}
	}
	
	private String exceptionPsubj(String transl, char clause) {
		if (clause == 'J') {
			if (form.contains("2"))
				return chosenMeaning;
			return "Let "+ addPersonAndNumber('J') +"be "+ changeEending("ed");
		} else if (clause == '1')
			return transl +"has been "+ changeEending("ed");
		else if (clause == '2' || clause == '7' || (clause == 'T' && form.contains("PLUP")))
			return transl +"had been "+ changeEending("ed");
		else if (clause == 'R' || clause == 'I' || clause == 'C' || clause == 'O')
			return PindicTranslate(transl);
		else if (form.contains("PLUP"))
			return transl +"would have been "+ changeEending("ed");
		return "";
	}
	
	private String presPsubj(String transl, char clause) {
		if (clause == 'V')
			return "may "+ transl +"be "+ changeEending("ed"); 
		else if (clause == 'P' || clause == 'U' || clause == 'T')
			return transl +"may be "+ changeEending("ed");
		else if (clause == 'H')
			return "would be "+ changeEending("ed");
		else if (clause == 'D')
			return "should "+ transl +"be "+ changeEending("ed");
		else if (clause == 'F')
			return transl +"will be "+ changeEending("ed");
		else if (clause == '3')
			return transl +"should be "+ changeEending("ed");
		else //clause == '4'
			return transl +"would be "+ changeEending("ed");
	}
	
	private String pastPsubj(String transl, char clause) {
		if (clause == 'V')
			return "may "+ transl +"have been "+ changeEending("ed");
		else if (clause == 'P')
			return transl +"may " +"have been "+ changeEending("ed");
		else if (clause == 'U' || clause == 'F' || clause == '6')
			 return transl +"would be "+ changeEending("ed");
		else if (clause == 'H')
			return "would have been "+ changeEending("ed");
		else if (clause == 'D')
			return "could "+ transl +"have been "+ changeEending("ed");
		else if (clause == 'T')
			return transl +"were being "+ changeEending("ed");
		else //clause === '5'
			return transl +"were being "+ changeEending("ed");
	}
	
	private String exceptionAsubj(String transl, char clause) {
		if (clause == 'J') {
			if (form.contains("2"))
				return chosenMeaning;
			return "let "+ addPersonAndNumber('J') + chosenMeaning;
		} else if (clause == '1')
			return transl +"has "+ changeEending("ed");
		else if (clause == '2' || clause == '7' || (clause == 'T' && form.contains("PLUP")))
			return transl +"had "+ changeEending("ed");
		else if (clause == 'R' || clause == 'I' || clause == 'C' || clause == 'O')
			return AindicTranslate(transl);
		else if (form.contains("PLUP"))
			return transl +"would have "+ changeEending("ed");
		return "";
	}
	
	private String presAsubj(String transl, char clause) {
		if (clause == 'V')
			return "may "+ transl + chosenMeaning; 
		else if (clause == 'P' || clause == 'U' || clause == 'T')
			return transl +"may "+ chosenMeaning;
		else if (clause == 'H')
			return "would "+ chosenMeaning;
		else if (clause == 'D')
			return "should "+ transl + chosenMeaning;
		else if (clause == 'F')
			return transl +"will "+ chosenMeaning;
		else if (clause == '3')
			return transl +"should "+ chosenMeaning;
		else //clause == '4'
			return transl +"would "+ chosenMeaning;
	}
	
	private String pastAsubj(String transl, char clause) {
		if (clause == 'V')
			return "may "+ transl +"have "+ changeEending("ed");
		else if (clause == 'P')
			return transl +"may " +"have "+ changeEending("ed");
		else if (clause == 'U' || clause == 'F' || clause == '6')
			 return transl +"would "+ chosenMeaning;
		else if (clause == 'H')
			return "would have "+ changeEending("ed");
		else if (clause == 'D')
			return "could "+ transl +"have "+ changeEending("ed");
		else if (clause == 'T')
			return transl +"were "+ changeEending("ing");
		else //clause === '5'
			return transl +changeEending("ing");
	}

	private String participleTranslate() {
		if (form.contains("PRES"))
			return changeEending("ing");
		else if (form.contains("PERF"))
			return changeEending("ed");
		else {
			if (form.contains(" A "))
				return "about to "+ chosenMeaning;
			else
				return "to be "+ changeEending("ed");
		}
	}
	
	private String infinitiveTranslate(String form) {
		if (form.contains(" A ")) {
			if (form.contains("PRES"))
				return "to "+ chosenMeaning;
			if (form.contains("FUT"))
				return "to be about to "+ chosenMeaning;
			else
				return "to have "+ changeEending("ed");
		} else {
			if (form.contains("PRES"))
				return "to be "+ changeEending("ed");
			if (form.contains("FUT"))
				return "to be about to be "+ changeEending("ed");
			else
				return "to have been "+ changeEending("ed");
		}
	}
	
	private String imperativeTranslate() {
		if (form.contains(" A "))
			return chosenMeaning;
		return "be "+ changeEending("ed");
	}
	
	private String changeEending(String end) {
		if (chosenMeaning.charAt(chosenMeaning.length()-1) == 'e')
			return chosenMeaning.substring(0, chosenMeaning.length()-1) + end;
		return chosenMeaning + end;
	}
	
	private String addPersonAndNumber(char type) {
		if (type != 'J') {
			if (form.contains("2"))
				return "you ";
			else if (form.contains("1 S"))
				return "I ";
			else if (form.contains("1 P"))
				return "we ";
			else if (form.contains("3 S") && gender == 'm')
				return "he ";
			else if (form.contains("3 S") && gender == 'f')
				return "she ";
			else if (form.contains("3 S") && gender == 'n')
				return "it ";
			else
				return "they ";
		} else {
			if (form.contains("2"))
				return "";
			if (form.contains("1 S"))
				return "me ";
			else if (form.contains("1 P"))
				return "us ";
			else if (form.contains("3 S") && gender == 'n')
				return "it ";
			else if (form.contains("3 S") && gender == 'f')
				return "her ";
			else if (form.contains("3 S") && gender == 'm')
				return "him ";
			else
				return "them ";
		}
	}
	
	private String getSumForm() {
		if (form.contains("INF"))
			return "be";
		if (form.contains("FUT"))
			return "will be";
		else if (form.contains("FUTP"))
			return "will have been";
		else if (form.contains("PLUP"))
			return "had been";
		else if (form.contains("1 S")) {
			if (form.contains("PRES"))
				return "am";
			return "was";
		} else if (form.contains("3 S")) {
			if (form.contains("PRES"))
				return "is";
			return "was";
		} else if (form.contains("PRES"))
			return "are";
		return "were";
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
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public String getPart() {return "V";}
	public ArrayList<String> getForms() {return possForms;}
	public String getForm(int idx) {return possForms.get(idx);}
}
