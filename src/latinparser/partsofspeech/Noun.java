package latinparser.partsofspeech;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import latinparser.LatinParser;

public class Noun implements Word {
	private ArrayList<String> possForms = new ArrayList<String>();
	private String meaning;
	private String codes;
	private int reduction = 0;
	private char gender;
	
	public Noun(char g, String m, String c) {
		meaning = m;
		codes = c;
		gender = g;
		if (gender == 'P')
			addUnknownForms();
	}
	
	public void addPossForm(String e) {
		if (e.contains("LOC") && LatinParser.isLOCFile && !e.contains("Early")) {
			addLOCForm(e);
			return;
		} if (e.length() > 38 && !e.contains("Early"))
			possForms.add(e.substring(32, 39));
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
	
	public static String translate(String m, String form, String notes) {
		notes = notes.replaceAll(" SUBST", "");
		String mean = m.split(";|,|/")[Integer.parseInt(notes.split(" ")[0])].trim();
		if (form.contains(" S"))
			return (getPrep(form, notes) + getArticle(mean, notes) + mean).trim();
		else
			return (getPrep(form, notes) + getArticle(mean, notes) + pluralize(mean)).trim();
	}
	
	public static String getArticle(String mean, String notes) {
		String article = notes.split(" ")[3];
		if (article.equals("*"))
			return "";
		if ("aeiouAEIOU".contains(mean.substring(0, 1)) && article.equals("a"))
			return "an ";
		return article +" "; 
	}
	
	private static String getPrep(String form, String notes) {
		if (notes.split(" ").length == 5 && !notes.split(" ")[4].equals("*"))
			return notes.split(" ")[4] +" ";
		else if (notes.split(" ").length == 5)
			return "";
		if (form.contains("NOM") || form.contains("VOC") || form.contains("ACC"))
			return " ";
		else if (form.contains("GEN"))
			return "of ";
		else if (form.contains("DAT"))
			return "for ";
		else
			return "by ";
	}
	
	private static String pluralize(String mean) {
		if (LatinParser.isPluralFile) {
			try {
				Scanner sc = new Scanner(new File(LatinParser.PLURALS));
				sc.useDelimiter("$^");
				String plurals = sc.next();
				sc.close();
				for (String word: plurals.split("\r\n"))
					if (word.split(" ")[0].equals(mean))
						return word.split(" ")[1];
			} catch (FileNotFoundException e) {
				System.out.println("Plurals file " + LatinParser.PLURALS +"does not exist.");
				e.printStackTrace();
			}
		}
		String two = mean.substring(mean.length()-2);
		String one = mean.substring(mean.length()-1);
		if (two.equals("ch") || two.equals("sh") || one.equals("s") || one.equals("x") || one.equals("z"))
			return mean +"es";
		else if (one.equals("y") && !"aeiou".contains(two.substring(0, 1)))
			return mean.substring(0, mean.length()-1) +"ies";
		else if (one.equals("f"))
			return mean.substring(0, mean.length()-1) +"ves";
		else if (two.equals("fe"))
			return mean.substring(0, mean.length()-2) +"ves";
		return mean +"s";
	}
	
	public String translate(String notes) {
		if (gender == 'P')
			return meaning;
		return Noun.translate(meaning, possForms.get(Integer.parseInt(notes.split(" ")[1])), notes);
	}
	
	private void addLOCForm(String entry) {
		try {
			Scanner sc = new Scanner(new File(LatinParser.LOCS));
			sc.useDelimiter("$^");
			String locs = sc.next();
			sc.close();
			if (locs.contains(meaning))
				possForms.add(entry.substring(32, 37));
		} catch (FileNotFoundException e) {
			System.out.println("Locative file "+ LatinParser.LOCS +" does not exist.");
			e.printStackTrace();
		}
	}
	
	public int canBe(String f) {
		for (int i = 0; i < possForms.size(); i++)
			if (f.charAt(0) != '!' && possForms.get(i).contains(f)) {
				return i;
			} else if (f.charAt(0) == '!' && !possForms.get(i).contains(f.substring(1))) {
				return i;
			}
		return -1;
	}
		
	public void setPart(String part) {
		for (int i = possForms.size()-1; i >= 0; i--)
			if (part.charAt(0) != '!' && !possForms.get(i).contains(part))
				possForms.remove(i);
			else if (part.charAt(0) == '!' && possForms.get(i).contains(part.substring(1)))
				possForms.remove(i);
	}
	
	private void addUnknownForms() {
		String one = meaning.substring(meaning.length()-1);
		String two = meaning.substring(meaning.length()-2);
		String four = meaning.substring(meaning.length()-4);
		if (four.equals("ibus"))
			addForms(new String[] {"DAT", "ABL"}, new String[] {"P"}, new String[] {"X"});
		else if (four.equals("orum"))
			addForms(new String[] {"GEN"}, new String[] {"P"},new String[] {"M", "N"});
		else if (four.equals("arum"))
			addForms(new String[] {"GEN"}, new String[] {"P"},new String[] {"F"});
		else if (two.equals("is")) {
			addForms(new String[] {"GEN"}, new String[] {"S"},new String[] {"X"});
			addForms(new String[] {"DAT", "ABL"}, new String[] {"P"},new String[] {"X"});
		} else if (two.equals("ae")) {
			addForms(new String[] {"NOM", "VOC"}, new String[] {"P"},new String[] {"F"});
			addForms(new String[] {"GEN", "DAT"}, new String[] {"S"},new String[] {"F"});
		} else if (two.equals("am"))
			addForms(new String[] {"ACC"}, new String[] {"S"},new String[] {"F"});
		else if (two.equals("as"))
			addForms(new String[] {"ACC"}, new String[] {"P"},new String[] {"F"});
		else if (two.equals("os"))
			addForms(new String[] {"ACC"}, new String[] {"P"},new String[] {"M"});
		else if (two.equals("us") || one.equals("r"))
			addForms(new String[] {"NOM"}, new String[] {"S"},new String[] {"M"});
		else if (one.equals("o"))
			addForms(new String[] {"DAT", "ABL"}, new String[] {"S"},new String[] {"M", "N"});
		else if (two.equals("um")) {
			addForms(new String[] {"ACC"}, new String[] {"S"},new String[] {"M", "N"});
			addForms(new String[] {"NOM", "VOC"}, new String[] {"S"},new String[] {"N"});
			addForms(new String[] {"GEN"}, new String[] {"P"},new String[] {"X"});
		} else if (one.equals("a")) {
			addForms(new String[] {"NOM", "ABL", "VOC"}, new String[] {"S"},new String[] {"F"});
			addForms(new String[] {"NOM", "ACC", "VOC"}, new String[] {"P"},new String[] {"N"});
		} else if (one.equals("i")) {
			addForms(new String[] {"NOM", "VOC"}, new String[] {"P"},new String[] {"M"});
			addForms(new String[] {"GEN"}, new String[] {"S"},new String[] {"M", "N"});
			addForms(new String[] {"DAT"}, new String[] {"S"},new String[] {"X"});
		} else if (two.equals("es"))
			addForms(new String[] {"NOM", "ACC", "VOC"}, new String[] {"P"},new String[] {"X"});
		else if (two.equals("em"))
			addForms(new String[] {"ACC"}, new String[] {"S"},new String[] {"X"});
		else if (one.equals("e"))
			addForms(new String[] {"ABL"}, new String[] {"S"},new String[] {"X"});
		else {
			addForms(new String[] {"NOM", "ACC", "VOC"}, new String[] {"S"},new String[] {"N"});
			addForms(new String[] {"NOM", "VOC"}, new String[] {"S"},new String[] {"C"});
		}
	}
	
	private void addForms(String[] cases, String[] numbers, String[] genders) {
		for (String c: cases)
			for (String n: numbers)
				for (String g: genders)
					possForms.add(c +" "+ n +" "+ g);
	}
	
	public char getGender() {return gender;}
	public void addMeaning(String m) {meaning += m;}
	public String toString() {return meaning;}
	public void reduce() {reduction++;}
	public String getPart() {return "N";}
	public ArrayList<String> getForms() {return possForms;}
	public String getF(int idx) {return possForms.get(idx);}
}
