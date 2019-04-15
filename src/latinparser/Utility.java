package latinparser;

import java.util.ArrayList;

/* UTILITY
 *  - ArrayList<DictEntry> getAdjectivesFor(DictEntry d)
 *  - boolean checkIfNounAdjUsable(String toCheck, String checkAgainst)
 *  - boolean isGenderMatch(String toCheck, String checkAgainst)
 *  - boolean isCaseMatch(String toCheck, String checkAgainst)
 *  - boolean isInList()
 */
public class Utility {
	private ArrayList<DictEntry> dict;
	private int start;
	private int upTo;
	
	public Utility(ArrayList<DictEntry> d, int startIndex, int toIndex) {
		dict = d;
		start = startIndex;
		upTo = toIndex;
	}
	
	public ArrayList<DictEntry> getAdjectivesFor(DictEntry d) {
		ArrayList<DictEntry> adjectives = new ArrayList<DictEntry>();
		String form = d.getW(0).getF(0);
		for (int i = start; i < upTo; i++) {
			for (String type: new String[] {"ADJ", "NUM"})
				if (dict.get(i).canBe(type) != -1) {
					boolean isUsable = true;
					perADJ:
						for (String x: dict.get(i).getW(type).getForms()) {
							if (!checkIfNounAdjUsable(x, form)) {
								isUsable = false;
								break perADJ;
							}
						}
					if (isUsable && !dict.get(i).isClaimed())
						adjectives.add(dict.get(i));
				}
		}
		return adjectives;
	}
	
	public boolean checkIfNounAdjUsable(String toCheck, String checkAgainst) {
		if (toCheck.charAt(4) != checkAgainst.charAt(4) || !isGenderMatch(toCheck, checkAgainst) ||
				!isCaseMatch(toCheck, checkAgainst))
			return false;
		return true;
	}
	
	public boolean isGenderMatch(String toCheck, String checkAgainst) {
		char toG = toCheck.charAt(6);
		char agG = checkAgainst.charAt(6);
		if (toG == 'X' || agG == 'X')
			return true;
		else if ((toG == 'C' && agG == 'N') || (toG == 'N' && agG == 'C')) {
			return false;
		} else if (toG == 'C' || agG == 'C')
			return true;
		else if (toG != agG) {
			return false;
		}
		return true;
	}

	public boolean isCaseMatch(String toCheck, String checkAgainst) { 
		String toC = toCheck.substring(0, 3);
		String agC = checkAgainst.substring(0, 3);
		if (toC.equals(agC))
			return true;
		if ((toC.equals("ABL") || agC.equals("ABL")) && (toC.equals("DAT") || agC.equals("DAT")))
			return true;
		return false;
	}
	
	public boolean isInList() {
		//TODO implement isInList()
		return false;
	}
	
	public int getNumWordsOfForm(String part, String form) {
		int n = 0;
		for (int i = start; i < upTo; i++)
			if (dict.get(i).canBe(part) != -1 && 
					dict.get(i).getW(part).canBe(form) != -1) {
				n++;
			}
		return n;
	}
}
