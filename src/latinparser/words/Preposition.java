package latinparser.words;


import java.util.ArrayList;

public class Preposition extends Word {
	private String caseTaken;
	
	public Preposition(String m, String c, String cas) {
		meaning = m;
		codes = c;
		caseTaken = cas;
	}
	
	public String translate(String notes) {
		return meaning.split(";|,|/")[Integer.parseInt(notes)].trim();
	}
	
	public boolean canBe(String f) {return false;}
	public void addPossForm(String e) {}
	public String getCase() {return caseTaken;}
	
	public void setForm(String part) {}
	public String getForm(String formSearch) {return null;}
	public String getForm(int idx) {return null;}
	public ArrayList<String> getForms() {return null;}
	
	public void addMeaning(String m) {meaning += m;}
	public String getPart() {return "PREP";}
}
