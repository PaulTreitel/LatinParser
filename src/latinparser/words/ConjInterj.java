package latinparser.words;


import java.util.ArrayList;

public class ConjInterj extends Word {
	
	public ConjInterj(String m, String c) {
		meaning = m;
		codes = c;
	}

	public String translate(String notes) {
		return meaning.split(";|,|/")[Integer.parseInt(notes)].trim();
	}
	
	public void setForm(String part) {}
	public void addPossForm(String e) {}
	
	public int canBe(String f) {return -1;}
	public ArrayList<String> getForms() {return null;}
	public String getForm(int idx) {return new String();}
	
	public void addMeaning(String m) {meaning += m;}
	public String getPart() {return "CONJ/INTERJ";}
}
