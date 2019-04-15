package latinparser.partsofspeech;
import java.util.ArrayList;

public class Adjective implements Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String meaning;
	private String codes;
	private int reduction = 0;
	
	public Adjective(String mean, String c) {
		meaning = mean;
		codes = c;
	}
	
	public void addPossForm(String e) {
		if (e.length() == 11) {
			possForms.add(e);
			return;
		} else if (e.length() < 11)
			return;
		if (e.contains("3 2 ACC P C POS") && !e.contains("es"))
			return;
		if (e.substring(35, 36).equals(" ") && e.substring(37, 38).equals(" ") && !e.contains("Early"))
			possForms.add(e.substring(32, 43));
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
	
	public static String translate(String mean, String form, String notes) {
		String transl = mean.split("; |, |/")[Integer.parseInt(notes.split(" ")[0])].trim();
		transl = Noun.getArticle(mean, notes) + transl;
		if (!notes.contains("SUBST"))
			return transl;
		else if (form.contains(" S")) {
			if (form.contains(" M"))
				return transl +" man";
			else if (form.contains(" F"))
				return transl +" woman";
			return transl +"thing ";
		} else {
			if (form.contains(" M"))
				return transl +" men";
			else if (form.contains(" F"))
				return transl +" women";
			return transl +"things ";
		}
	}
	
	public String translate(String notes) {
		return Adjective.translate(meaning, possForms.get(Integer.parseInt(notes.split(" ")[1])), notes);
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
	public String getPart() {return "ADJ";}
	public ArrayList<String> getForms() {return possForms;}
	public String getF(int idx) {return possForms.get(idx);}
}
