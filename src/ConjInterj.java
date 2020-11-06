

import java.util.ArrayList;

public class ConjInterj implements Word {
	private String meaning;
	private String codes;
	private int reduction = 0;
	
	public ConjInterj(String m, String c) {
		meaning = m;
		codes = c;
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
		return meaning.split(";|,|/")[Integer.parseInt(notes)].trim();
	}
	
	public void setPart(String part) {}
	public int canBe(String f) {return -1;}
	public void addPossForm(String e) {}
	public ArrayList<String> getForms() {return new ArrayList<String>();}
	public String getF(int idx) {return new String();}
	
	public void addMeaning(String m) {meaning += m;}
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public String getPart() {return "CONJ/INTERJ";}
}
