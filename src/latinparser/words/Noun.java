package latinparser.words;


import java.util.ArrayList;

import latinparser.LatinParser;
import latinparser.Utility;

public class Noun extends Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private char gender;
	private static final int N_FORM_START = 32;
	private static final int N_FORM_END = 39;
	private static final int N_FORM_LENGTH = N_FORM_END - N_FORM_START;
	
	public Noun(char g, String m, String c) {
		meaning = m;
		codes = c;
		gender = g;
		if (gender == 'P')
			// P is special gender indicating IGNORE_UNKNOWN_NAME came up
			addAllForms();
	}
	
	/* addPossForm
	 * takes a String representation of a word form and adds it to the list of possible forms
	 * ignores Early Latin word forms
	 */
	public void addPossForm(String e) {
		if (e.contains("Early")) {
			return; // ignore Early Latin
		}
		if (e.contains("LOC")) {
			addLOCForm(e);
		} else if (e.length() >= N_FORM_END) {
			possForms.add(e.substring(N_FORM_START, N_FORM_END));
		} else if (e.length() == N_FORM_LENGTH) {
			possForms.add(e);
		}
	}
	
	public String translate(String notes) {
		if (gender == 'P')
			return meaning;
		return Noun.translate(meaning, possForms.get(Integer.parseInt(notes.split(" ")[1])), notes);
	}
	
	// static so that Pronoun can use it
	public static String translate(String m, String form, String notes) {
		notes = notes.replaceAll(" SUBST", "");
		String mean = m.split(";|,|/")[Integer.parseInt(notes.split(" ")[0])].trim();
		
		String prep = getPrep(form, notes);
		String article = getArticle(mean, notes);
		if (form.contains(" S"))
			return (prep + article + mean).trim();
		else
			return (prep + article + pluralize(mean)).trim();
	}
	
	public static String getArticle(String mean, String notes) {
		String article = notes.split(" ")[3];
		if (article.equals("*"))
			return "";
		if ("aeiouAEIOU".contains(mean.substring(0, 1)) && article.equals("a"))
			return "an ";
		return article +" "; 
	}
	
	private static String getPrep(String form, String notes) {
		if (notes.split(" ").length == 5 && !notes.split(" ")[4].equals("*"))
			return notes.split(" ")[4] +" ";
		else if (notes.split(" ").length == 5)
			return "";
		if (form.contains("NOM") || form.contains("VOC") || form.contains("ACC"))
			return " ";
		else if (form.contains("GEN"))
			return "of ";
		else if (form.contains("DAT"))
			return "for ";
		else
			return "by ";
	}
	
	private static String pluralize(String mean) {
		for (String word: LatinParser.getPluralsContents().split("\r\n"))
			if (word.split(" ")[0].equals(mean))
				return word.split(" ")[1];
		String two = mean.substring(mean.length()-2);
		String one = mean.substring(mean.length()-1);
		if (two.equals("ch") || two.equals("sh") || one.equals("s") || one.equals("x") || one.equals("z"))
			return mean +"es";
		else if (one.equals("y") && !"aeiou".contains(two.substring(0, 1)))
			return mean.substring(0, mean.length()-1) +"ies";
		else if (one.equals("f"))
			return mean.substring(0, mean.length()-1) +"ves";
		else if (two.equals("fe"))
			return mean.substring(0, mean.length()-2) +"ves";
		return mean +"s";
	}
	
	private void addLOCForm(String entry) {
		if (LatinParser.getLocsContents().contains(meaning))
			possForms.add(entry.substring(32, 37));
	}
	
	/* canBe
	 * takes a string and checks if it is in the list of possible word forms
	 * supports negation where the first character of the string is '!'
	 * returns the index of a possible matching form if there is one, -1 otherwise
	 */
	public int canBe(String f) {
		boolean negated = f.charAt(0) == '!';
		String absoluteForm = Utility.expandNounAdjForm(f, negated);
		
		for (int i = 0; i < possForms.size(); i++) {
			String currForm = possForms.get(i);
			boolean match = Utility.nounAdjMatch(currForm, absoluteForm);
			if (!negated && match) {
				return i;
			} else if (negated && !match) {
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
	
	
	/* addAllForms
	 * adds all noun forms to the list of possible forms
	 */
	private void addAllForms() {
		for (String c: new String[] {"NOM", "GEN", "DAT", "ACC", "ABL", "VOC"})
			for (String n: new String[] {"S", "P"})
					possForms.add(c +" "+ n +" X");
	}
	
	
	public char getGender() {return gender;}
	public void addMeaning(String m) {meaning += m;}
	public String getPart() {return "N";}
	public ArrayList<String> getForms() {return possForms;}
	public String getForm(int idx) {return possForms.get(idx);}
}
