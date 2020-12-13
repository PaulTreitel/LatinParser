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
	
	public boolean canBe(String f) {return false;}
	public String getForm(String formSearch) {return null;}
	public String getForm(int idx) {return null;}
	public ArrayList<String> getForms() {return null;}
	
	public void addMeaning(String m) {meaning += m;}
	public String getPart() {return "CONJ/INTERJ";}
}
