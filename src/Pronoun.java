

import java.util.ArrayList;

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
	
	public void addPossForm(String entry) {
		String[] pronouns = LatinParser.getPronounsContents().split("\r\n\r\n");
		if (!getForm(pronouns, word)) {
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
	
	public int canBe(String f) {
		for (int i = 0; i < possForms.size(); i++)
			if (f.charAt(0) != '!' && possForms.get(i).contains(f))
				return i;
			else if (f.charAt(0) == '!' && !possForms.get(i).contains(f.substring(1)))
				return i;
		return -1;
	}
	
	public void setPart(String part) {
		for (int i = possForms.size()-1; i >= 0; i--)
			if (part.charAt(0) != '!' && !possForms.get(i).contains(part))
				possForms.remove(i);
			else if (part.charAt(0) == '!' && possForms.get(i).contains(part.substring(1)))
				possForms.remove(i);
	}
	
	public int getNumber() {
		if (meaning.equals("you"))
			return 2;
		if (meaning.equals("I, me"))
			return 1;
		return 3;
	}
	
	public void addMeaning(String m) {}
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public String getPart() {return "PRON";}
	public ArrayList<String> getForms() {return possForms;}
	public String getF(int idx) {return possForms.get(idx);}
}
