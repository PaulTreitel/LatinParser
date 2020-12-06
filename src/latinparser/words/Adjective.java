package latinparser.words;

import java.util.ArrayList;

public class Adjective implements Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String meaning;
	private String codes;
	private int reduction = 0;
	
	public Adjective(String mean, String c) {
		meaning = mean;
		codes = c;
	}
	
	/* addPossForm
	 * takes a String representation of a word form and adds it to the list of possible forms
	 * ignores malformed strings of less than 11 characters
	 * does voodoo magic to long strings, presumably based on WORDS output
	 */
	public void addPossForm(String e) {
		if (e.length() < 11) { // anything shorter than 11 characters is malformed
			return;
		}
		if (e.length() == 11) {
			possForms.add(e);
			return;
		}
		
		// TODO I have no idea why this is here
		if (e.contains("3 2 ACC P C POS") && !e.contains("es")) {
			System.out.println("Adjective.java weird if clause");
			System.out.println(e);
			return;
		}
		// TODO sort out this magic number bullshit
		if (e.substring(35, 36).equals(" ") && e.substring(37, 38).equals(" ") && !e.contains("Early")) {
			System.out.println("Adjective.java magic number bullshit");
			System.out.println(e);
			possForms.add(e.substring(32, 43));
		}
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
		return 1 - reduction;
	}
	
	// TODO should this be static?
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
	
	public void addMeaning(String m) {meaning += m;}
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public String getPart() {return "ADJ";}
	public ArrayList<String> getForms() {return possForms;}
	public String getF(int idx) {return possForms.get(idx);}
}
