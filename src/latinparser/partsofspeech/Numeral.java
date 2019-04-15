package latinparser.partsofspeech;
import java.util.ArrayList;

public class Numeral implements Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String meaning;
	private String codes;
	private int reduction = 0;
	
	public Numeral(String m, String c) {
		meaning = m.replace("as a ROMAN NUMERAL", "").replace("1th", "1st");
		meaning = meaning.replace("2th", "2nd").replace("3th", "3rd").trim();
		codes = c;
	}
	
	public void addRemainingForms() {
		if (possForms.size() > 1 || (possForms.size() == 1 && !possForms.get(0).contains(" X ")))
			return;
		if (possForms.size() == 1)
			possForms.remove(0);
		for (String c: new String[] {"NOM", "GEN", "DAT", "ACC", "ABL", "VOC"})
			for (String n: new String[] {" S", " P"})
				for (String g: new String[] {" M", " F", " N"})
					possForms.add(c+n+g);
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
		return meaning.split(";|,|/|-")[0].trim();
	}
	
	public void addPossForm(String e) {
		if (e.contains("Early"))
			return;
		if (e.equals("1")) {
			addRemainingForms();
			return;
		}
		possForms.add(e.substring(32, 43));
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
	
	public void addMeaning(String m) {meaning += m;}
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public String getPart() {return "NUM";}
	public ArrayList<String> getForms() {return possForms;}
	public String getF(int idx) {return possForms.get(idx);}
}