package latinparser.words;


import java.util.ArrayList;

public abstract class Word {
	protected String codes;
	protected int reduction = 0;
	protected String meaning;
	
	
	public abstract void addPossForm(String e);
	
	public abstract String translate(String notes);
	
	/**
	 * Returns the frequency of the word, reduced by `reduction`. Frequency
	 * codes are specified by the WORDS program. Codes are converted into
	 * integers, then `reduction` is subtracted off.
	 * @return the adjusted frequency of the word
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
	
	/**
	 * Sets the Word to a certain form by removing every possible form that is
	 * not that part of speech. Allows for negation by the "!" prefix - to set 
	 * the Word to not be the given part of speech.
	 * @param form - the part of speech to set the Word to
	 */
	public abstract void setForm(String form);
	
	/**
	 * Takes a word form and checks if matches an item the list of possible word
	 * forms. Supports negation where the first character of the string is '!' -
	 * if the Word can't be that form.
	 * @param f - the word form to search for
	 * @return true if a match is found, false otherwise
	 */
	public abstract boolean canBe(String f);
	
	/** 
	 * Reduces the adjusted frequency of the word by 1.
	 */
	public void reduce() {reduction++;}
	
	public abstract String getForm(String formSearch);
	public abstract String getForm(int idx);
	public abstract ArrayList<String> getForms();
	
	public abstract String getPart();
	public abstract void addMeaning(String m);
	public String toString() {return meaning;}
}
