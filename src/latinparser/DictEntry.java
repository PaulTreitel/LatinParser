package latinparser;

import java.util.ArrayList;
import latinparser.partsofspeech.*;

public class DictEntry {
	private ArrayList<Word> possible = new ArrayList<Word>();
	//no punctuation or line breaks
	private String word;
	private String punctuation;
	private boolean decrease;
	private boolean claimed;
	
	//TODO deal with additions (like -que)
	public DictEntry(String entries, String origin) {
		punctuation = "";
		if (".,;:?!".contains(origin.substring(origin.length()-1)))
			punctuation = origin.substring(origin.length()-1);
		word = origin.toLowerCase().replaceAll(",|[.]|;|:|[?]|!", "");
		String eInput = "";
		int n = 0;
		for (int a = 0; a < entries.length(); a++) {
			eInput += entries.charAt(a);
			if (entries.charAt(a) == ';')
				n = 1;
			if (entries.charAt(a) == '\n')
				n = 0;
			if (n == 1 && entries.charAt(a) == '\r') {
				dictToWord(eInput.split("\n"));
				eInput = "";
				n = 0;
				while (entries.charAt(a) != '\n' && a < entries.length()-1)
					a++;
			}
		}
		if (!eInput.equals(""))	
			dictToWord(eInput.split("\n"));
		orderEntries();
	}
	
	private void dictToWord(String[] entry) {
		if (entry.length == 1 && !possible.get(possible.size()-1).toString().contains(entry[0])) {
			possible.get(possible.size()-1).addMeaning(entry[0]);
			return;
		}
		String mean = entry[entry.length-1].substring(0, entry[entry.length-1].length()-1);
		int mline;
		if (entry.length > 2) {
			mline = entry.length-2;
			while ((int) (entry[mline].charAt(entry[mline].length()-2)) == 59) {
				mean = entry[mline].substring(0, entry[mline].length()-1) + mean;
				mline--;
			}
		} else
			mline = entry.length-1;
		String[] parts = entry[mline].substring(0, entry[mline].length()-1).replaceAll(",", "").split("   |  | ");
		String dictCodes = "";
		for (String a: entry)
			if (a.contains("[")) {
				dictCodes = a.substring(a.indexOf("["), a.indexOf("]")+1);
				break;
			}
		if (addExceptionWordsToEntry(entry, mean, dictCodes))
			return;
		int i = addWordToEntry(parts, mean, dictCodes);
		if (i != -1 && "NADJVADVNUM".contains(parts[i])) {
			for (int x = 0; x < entry.length-2; x++)
				if (!entry[x].equals("\r"))
					possible.get(possible.size()-1).addPossForm(entry[x]);
			if (possible.get(possible.size()-1).getForms().size() == 0 && possible.size() != 1)
				copyPossForms();
		}
		if (i != -1 && parts[i].equals("NUM")) {
			possible.get(possible.size()-1).addPossForm("1");
		} for (int x = 0; x < possible.size(); x++)
			if (possible.get(x).getForms().size() == 0 &&
				!"CONJ/INTERJPREP".contains(possible.get(x).getPart())) {
				possible.remove(x);
			}
	}
	
	private int addWordToEntry(String[] parts, String mean, String dictCodes) {
		int x = possible.size();
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].equals("N"))
				possible.add(new Noun(parts[4].charAt(0), mean, dictCodes));
			else if (parts[i].equals("ADJ"))
				possible.add(new Adjective(mean, dictCodes));
			else if (parts[i].equals("V"))
				possible.add(new Verb(mean, dictCodes));
			else if (parts[i].equals("ADV"))
				possible.add(new Adverb(mean, dictCodes));
			else if (parts[i].equals("PREP"))
				possible.add(new Preposition(mean, dictCodes, parts[i+1]));
			else if (parts[i].equals("CONJ") || parts[i].equals("INTERJ"))
				possible.add(new ConjInterj(mean, dictCodes));
			else if (parts[i].equals("NUM"))
				possible.add(new Numeral(mean, dictCodes));
			else if ("PACKTACKONPREFIXSUFFIX".contains(parts[i]))
				decrease = true;
			if (possible.size() > x) {
				if (decrease)
					possible.get(possible.size()-1).reduce();
				return i;
			}
		}
		return -1;
	}
	
	private boolean addExceptionWordsToEntry(String[] e, String mean, String dictCodes) {
		if (e[0].contains("SUFFIX") || e[0].contains("PACK") || e[0].contains("PREFIX"))
			return true;
		if (mean.contains("Assume this is capitalized proper name/abbr, under MODE IGNORE_UNKNOWN_NAM")) {
			possible.add(new Noun('P', word, dictCodes));
			return true;
		} else if (e[0].contains("PRON") && LatinParser.isPronounFile) {
			for (int i = 0; i < possible.size(); i++)
				if (possible.get(i).getPart().equals("PRON")) {
					return true;
				}
			possible.add(new Pronoun(word, dictCodes));
			return true;
		} else if (mean.contains("ROMAN NUMERAL")) {
			possible.add(new Numeral(mean, dictCodes));
			possible.get(possible.size()-1).addPossForm("1");
		}
		return false;
	}
	
	private void orderEntries() {
		for (int i = 1; i < possible.size(); i++)
			for (int x = i; x < possible.size(); x++)
				if (!possible.get(x).getPart().equals("NUM") && 
					possible.get(x).getFreq() > possible.get(x-1).getFreq())
					possible.add(x-1, possible.remove(x));
	}
	
	public int canBe(String part) {
		for (int a = 0; a < possible.size(); a++)
			if (part.charAt(0) != '!' && possible.get(a).getPart().equals(part))
				return a;
			else if (part.charAt(0) == '!' && !possible.get(a).getPart().equals(part.substring(1)))
				return a;
		return -1;
	}
	
	public void setPart(String part) {
		for (int i = possible.size()-1; i >= 0; i--)
			if (part.charAt(0) != '!' && !possible.get(i).getPart().equals(part))
				possible.remove(i);
			else if (part.charAt(0) == '!' && possible.get(i).getPart().equals(part.substring(1)))
				possible.remove(i);
	}
	
	private void copyPossForms() {
		for (String form: possible.get(possible.size()-2).getForms())
			possible.get(possible.size()-1).addPossForm(form);
	}
	
	public Word getW(int idx) {return possible.get(idx);}
	public Word getW(String part) {
		return possible.get(canBe(part));
	}
	
	public void removeForm(int idx) {possible.remove(idx);}
	
	public void swapForm() {possible.add(possible.remove(0));}
	
	public void setClaimed() {claimed = true;}
	public boolean isClaimed() {return claimed;}
	
	public boolean getDecrease() {return decrease;}
	public String getPunct() {return punctuation;}
	public String toString() {return word;}
	public ArrayList<Word> getWords() {return possible;}

	public void addPart(String part, String mean, ArrayList<String> forms) {
		addWordToEntry(new String[] {part}, mean, "[XXXXX]");
		for (String f: forms)
			possible.get(possible.size()-1).addPossForm(f.substring(0, f.length()-10) +"POS");
	}
}
