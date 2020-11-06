
import java.util.ArrayList;

public class Adverb implements Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String meaning;
	private String codes;
	private int reduction = 0;
	
	public Adverb(String m, String c) {
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
		String form = possForms.get(Integer.parseInt(notes.split(" ")[1]));
		String mean = meaning.split(";|,|/")[Integer.parseInt(notes.split(" ")[0])].trim();
		if (form.equals("POS"))
			return mean;
		else if (form.equals("COM"))
			return "more " + mean;
		return "most " + mean;
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
	
	public void addPossForm(String e) {
		if (!e.contains("Early"))
			possForms.add(e.substring(28, 31));
	}
	
	public void addMeaning(String m) {meaning += m;}
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public String getPart() {return "ADV";}
	public ArrayList<String> getForms() {return possForms;}
	public String getF(int idx) {return possForms.get(idx);}
}
