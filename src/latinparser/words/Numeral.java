package latinparser.words;

import java.util.ArrayList;

public class Numeral implements Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String meaning;
	private String codes;
	private int reduction = 0;
	
	public Numeral(String m, String c) {
		meaning = m.replace("as a ROMAN NUMERAL", "").replace("1th", "1st");
		meaning = meaning.replace("2th", "2nd").replace("3th", "3rd").trim();
		codes = c;
	}
	
	public void addRemainingForms() {
		if (possForms.size() > 1 || (possForms.size() == 1 && !possForms.get(0).contains(" X ")))
			return;
		if (possForms.size() == 1)
			possForms.remove(0);
		for (String c: new String[] {"NOM", "GEN", "DAT", "ACC", "ABL", "VOC"})
			for (String n: new String[] {" S", " P"})
				for (String g: new String[] {" M", " F", " N"})
					possForms.add(c+n+g);
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
			addRemainingForms();
			return;
		}
		possForms.add(e.substring(32, 43));
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
	public String getPart() {return "NUM";}
	public ArrayList<String> getForms() {return possForms;}
	public String getF(int idx) {return possForms.get(idx);}
}