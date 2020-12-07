package latinparser.words;


import java.util.ArrayList;

public abstract class Word {
	protected String codes;
	protected int reduction = 0;
	protected String meaning;
	
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
	
	public void reduce() {reduction++;}
	public String toString() {return meaning;}
	
	public abstract String translate(String notes);
	public abstract void addPossForm(String e);
	public abstract void addMeaning(String m);
	public abstract String getPart();
	public abstract ArrayList<String> getForms();
	public abstract String getF(int idx);
	public abstract int canBe(String f);
	public abstract void setPart(String part);
}
