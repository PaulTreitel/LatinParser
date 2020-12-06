package latinparser.wordassistant;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import latinparser.DictEntry;
import latinparser.Utility;


/* ABLATIVES TODO update
 * USES
 *  - ArrayList<DictEntry> getAdjectivesFor(DictEntry d)
 */
public class AblativeFinder {
	private final String ABL_WORDS = System.getProperty("user.dir") +"/src/ablativewords.txt"; 
	
	private Utility u;
	private ArrayList<DictEntry> dict;
	private ArrayList<DictEntry> ablatives;
	private int start;
	private int upTo;
	
	public AblativeFinder(ArrayList<DictEntry> d, int startIndex, int toIndex, Utility util) {
		dict = d;
		start = startIndex;
		upTo = toIndex;
		u = util;
	}
	
	//TODO implement getAblativeUses()
	//TODO overhaul to ArrayList<ArrayList<DictEntry>> (to distinguish uses)
	public ArrayList<DictEntry> getAblativeUses(ArrayList<Integer> vIdx, ArrayList<DictEntry> preps) {
		if (ablatives != null)
			return ablatives;
		ablatives = new ArrayList<DictEntry>();
		
		//ABL of Comparison has noun associated with COMP ADJ, ABL of degree of difference is adverbial
		ArrayList<DictEntry> comp = getAblativeOfComparison(); 
		ablatives.addAll(comp.subList(0, comp.size()/2));
		//System.out.println("A COMP: "+ ablatives);
		
		ablatives.addAll(getAblativeAbsolute());
		//System.out.println("A ABSO: "+ ablatives);
		
		ablatives.addAll(getAblativeOfAgent(vIdx, preps));
		//System.out.println("A AGNT: "+ ablatives);
		
		ablatives.addAll(getAblativeOfPlaceTime("PLACE"));
		//System.out.println("A PLCE: "+ ablatives);
		
		ablatives.addAll(getAblativeOfPlaceTime("TIME"));
		//System.out.println("A TIME: "+ ablatives);
		
		//TODO ABL of separation
		//TODO ABL of cause
		//TODO ABL of means
		//TODO ABL of attendant circumstances
		//TODO ABL of degree of difference
		//TODO ABL of specification
		//TODO ABL of description/quality
		
		//TODO revisit adjectives (most slip by)
		for (DictEntry d: ablatives)
			d.setClaimed();
		for (int i = start; i < upTo; i++)
			for (String part: new String[] {"N", "ADJ", "NUM"})
			if (dict.get(i).canBe(part) != -1 && dict.get(i).getW(part).canBe("!ABL") != -1)
				dict.get(i).getW(part).setPart("!ABL");
		return ablatives;
	}
		
	/* ABLATIVE OF PLACE/ABLATIVE OF TIME */
	
	private ArrayList<DictEntry> getAblativeOfPlaceTime(String use) {
		ArrayList<DictEntry> placeTime = new ArrayList<DictEntry>();
		String[] ablativeWords = getAblativeWords();
		for (int i = start; i < upTo; i++)
			if (dict.get(i).canBe("N") != -1 && dict.get(i).getW("N").canBe("ABL") != -1) {
				String meaning = dict.get(i).getW("N").toString();
				if (isAblativeWord(meaning, ablativeWords, use)) {
					dict.get(i).setPart("N");
					dict.get(i).getW(0).setPart("ABL");
					placeTime.add(dict.get(i));
					placeTime.addAll(u.getAdjectivesFor(dict.get(i)));
				}
			}
		return placeTime;
	}
	
	private boolean isAblativeWord(String mean, String[] abls, String construction) {
		for (String use: abls)
			if (use.contains(construction)) {
				String[] meanings = use.split("\r\n");
				for (String m: meanings)
					if (mean.contains(m))
						return true;
			}
		return false;
	}
	
	private String[] getAblativeWords() {
		try {
			Scanner sc = new Scanner(new File(ABL_WORDS));
			sc.useDelimiter("$^");
			String lines = sc.next();
			sc.close();
			return lines.split("\r\n\r\n");
		} catch (FileNotFoundException e) {
			System.out.println("Ablative file "+ ABL_WORDS +" does not exist. Certain ablatives have been disabled.");
			e.printStackTrace();
			return new String[] {};
		}
	}
	
	/* ABLATIVE OF AGENT */
		
	private ArrayList<DictEntry> getAblativeOfAgent(ArrayList<Integer> vIdx, ArrayList<DictEntry> preps) {
		ArrayList<DictEntry> agents = new ArrayList<DictEntry>();
		for (DictEntry d: preps)
			if (d.toString().equals("a") || d.toString().equals("ab"))
				return agents;
		for (int idx: vIdx)
			for (String f: dict.get(idx).getW(0).getForms())
				if (f.substring(4, 7).equals(" P ")) {
					agents.addAll(findCaseFromEnd("ABL"));
					break;
				}
		return agents;
	}
	
	/* ABLATIVE ABSOLUTE */
	
	private ArrayList<DictEntry> getAblativeAbsolute() {
		ArrayList<DictEntry> absolutes = new ArrayList<DictEntry>();
		if (containsSumForm())
			return absolutes;
		for (int i = start; i < upTo; i++)
			if (dict.get(i).canBe("V") != -1 && dict.get(i).getW("V").canBe("ABL") != -1) {
				dict.get(i).setPart("V");
				dict.get(i).getW(0).setPart("PPL");
				dict.get(i).getW(0).setPart("ABL");
				absolutes.add(dict.get(i));
				String cng = dict.get(i).getW(0).getF(0).substring(0, 7);
				findAbsolutes(absolutes, cng);
			}
		return absolutes;
	}
	
	private void findAbsolutes(ArrayList<DictEntry> absolutes, String cng) {
		for (int a = start; a < upTo; a++) {
			p:
				for (String part: new String[] {"N", "PRON"}) {
					if (dict.get(a).canBe(part) != -1) {
						for (String f: dict.get(a).getW(part).getForms())
							if (!u.checkIfNounAdjUsable(f,cng))
								break p;
						dict.get(a).setPart(part);
						dict.get(a).getW(0).setPart(cng.substring(0, cng.length()-2));
						absolutes.add(dict.get(a));
						absolutes.addAll(u.getAdjectivesFor(dict.get(a)));
					}
				}
		}
	}
	
	/* ABLATIVE OF COMPARISON */
	
	//TODO distinguish from ABL of degree of difference
	private ArrayList<DictEntry> getAblativeOfComparison() {
		ArrayList<DictEntry> ablComp = new ArrayList<DictEntry>();
		int idx = canBeComp();
		while (idx != -1) {
			ArrayList<DictEntry> add = findCaseFromEnd("ABL");
			ablComp.add(dict.get(idx));
			dict.get(idx).getW(0).setPart("COM");
			ablComp.addAll(u.getAdjectivesFor(dict.get(idx)));
			ablComp.addAll(add);
			for (int i = 1; i < ablComp.size(); i++)
				ablComp.get(i).getW(0).setPart("ABL");
			idx = canBeComp();
		}
		return ablComp;
	}
	
	private boolean containsSumForm() {
		for (int i = start; i < upTo; i++) {
			if (dict.get(i).getW(0).toString().contains("also used to form"))
				return true;
		}
		return false;
	}
	
	private int canBeComp() {
		for (int i = start; i < upTo; i++)
			if (dict.get(i).canBe("ADJ") != -1)
				for (String f: dict.get(i).getW("ADJ").getForms())
					if (f.contains("COM") && (f.contains("NOM") || f.contains("ACC") && findNoun(f)))
						return i;
		return -1;
	}
	
	private boolean findNoun(String form) {
		for (int i = start; i < upTo; i++)
			if (dict.get(i).canBe("N") != -1)
				for (String f: dict.get(i).getW("N").getForms())
					if (u.checkIfNounAdjUsable(f, form))
						return true;
		return false;
	}
	
	private ArrayList<DictEntry> findCaseFromEnd(String c) {
		ArrayList<DictEntry> objects = new ArrayList<DictEntry>();
		for (int i = upTo-1; i >= start; i--) {
			if (dict.get(i).canBe("N") != -1 && dict.get(i).getW("N").canBe(c) != -1 && !dict.get(i).isClaimed()) {
				objects.add(dict.get(i));
				objects.addAll(u.getAdjectivesFor(objects.get(0)));
				return objects;
			}
		}
		return objects;
	}
}
