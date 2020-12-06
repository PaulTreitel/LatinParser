package latinparser.words;


import java.util.ArrayList;

public class Preposition implements Word {
	private String meaning;
	private String codes;
	private String caseTaken;
	private int reduction = 0;
	
	public Preposition(String m, String c, String cas) {
		meaning = m;
		codes = c;
		caseTaken = cas;
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
		return meaning.split(";|,|/")[Integer.parseInt(notes)].trim();
	}
	
	public void setPart(String part) {}
	
	public int canBe(String f) {return -1;}
	public String getCase() {return caseTaken;}
	public void addMeaning(String m) {meaning += m;}
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public void addPossForm(String e) {}
	public String getPart() {return "PREP";}
	public ArrayList<String> getForms() {return new ArrayList<String>();}
	public String getF(int idx) {return new String();}
}
