package latinparser.words;

import java.util.ArrayList;

public class Adverb implements Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String meaning;
	private String codes;
	private int reduction = 0;
	
	public Adverb(String m, String c) {
		meaning = m;
		codes = c;
	}
	
	/* getFreq
	 * returns the frequency of the word, reduced by `reduction`
	 * frequency codes are specified by the WORDS program
	 * codes are converted into integers, then `reduction` is subtracted off
	 */
	public int getFreq() {
		char x = codes.charAt(4);
		if (x == 'A') return 6 - reduction;
		if (x == 'B') return 5 - reduction;
		if (x == 'C') return 4 - reduction;
		if (x == 'D') return 3 - reduction;
		if (x == 'E') return 2 - reduction;
		else return 1 - reduction;
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
	
	/* canBe
	 * takes a string and checks if it is in the list of possible word forms
	 * supports negation where the first character of the string is '!'
	 * returns the index of a possible matching form if there is one, -1 otherwise
	 */
	public int canBe(String f) {
		boolean negated = f.charAt(0) == '!';
		String absolute_form = f.substring(1); // form without '!'
		
		for (int i = 0; i < possForms.size(); i++) {
			String curr_form = possForms.get(i);
			if (!negated && curr_form.contains(f)) {
				return i;
			} else if (negated && !curr_form.contains(absolute_form)) {
				return i;
			}
		}
		return -1;
	}
	
	/* setPart
	 * takes a string and removes any possible word forms that do not match
	 * supports negation where the first character of the string is '!'
	 */
	public void setPart(String part) {
		boolean negated = part.charAt(0) == '!';
		String absolute_part = part.substring(1); // part without '!'
		
		for (int i = possForms.size()-1; i >= 0; i--) {
			String curr_form = possForms.get(i);
			if (!negated && !curr_form.contains(part)) {
				possForms.remove(i);
			} else if (negated && curr_form.contains(absolute_part)) {
				possForms.remove(i);
			}
		}
	}
	
	/* addPossForm
	 * takes a String representation of a word form and adds it to the list of possible forms
	 * ignores Early Latin word forms
	 */
	public void addPossForm(String e) {
		if (!e.contains("Early")) {
			// TODO more magic constants
			possForms.add(e.substring(28, 31));
		}
	}
	
	public void addMeaning(String m) {meaning += m;}
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public String getPart() {return "ADV";}
	public ArrayList<String> getForms() {return possForms;}
	public String getF(int idx) {return possForms.get(idx);}
}
