package latinparser.words;


import java.util.ArrayList;

import latinparser.LatinParser;

public class Pronoun implements Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String meaning;
	private String codes;
	private String word;
	private String part;
	private int reduction = 0;
	
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
		String[] pronouns = LatinParser.getPronounsContents().split("\r\n\r\n");
		if (!getForm(pronouns, word)) {
			// TODO magic number
			getForm(pronouns, word.substring(0, word.length()-3));
		}
	}
	
	private boolean getForm(String[] pronouns, String w) {
		for (String p: pronouns) {
			String[] lines = p.split("\r\n");
			for (int i = 0; i < lines.length-2; i++) {
				if (lines[i].split(" ")[0].equals(w)) {
					possForms.add(lines[i].substring(lines[i].length()-7, lines[i].length()));
					if (meaning.equals(""))
						meaning = lines[lines.length-1];
					if (part.equals("")) {
						String[] parts = lines[lines.length-2].split(", ");
						part = parts[parts.length-1];
					}
				}
			}
			if (!meaning.equals(""))
				return true;
		}
		return false;
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
		if (part.equals("N"))
			return Noun.translate(meaning, form, notes);
		return Adjective.translate(meaning, form, notes);
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
	
	public void addMeaning(String m) {}
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public String getPart() {return "PRON";}
	public ArrayList<String> getForms() {return possForms;}
	public String getF(int idx) {return possForms.get(idx);}
}
