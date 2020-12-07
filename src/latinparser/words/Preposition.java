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
	
	public void setForm(String part) {}
	public int canBe(String f) {return -1;}
	public String getCase() {return caseTaken;}
	public void addMeaning(String m) {meaning += m;}
	public void addPossForm(String e) {}
	public String getPart() {return "PREP";}
	public ArrayList<String> getForms() {return new ArrayList<String>();}
	public String getForm(int idx) {return new String();}
}
