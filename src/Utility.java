


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
	
	/* getAdjectivesFor
	 * takes in a DictEntry that can be a (pro)noun and generates a list
	 * of adjectives that can match with its (first) form only
	 */
	public ArrayList<DictEntry> getAdjectivesFor(DictEntry d) {
		String form;
		ArrayList<DictEntry> adjectives = new ArrayList<DictEntry>();
		//assumes words & forms are already ordered/narrowed down
		if (d.canBe("N") != -1)
			form = d.getW("N").getF(0);
		else
			form = d.getW("PRON").getF(0);
		
		for (int i = start; i < upTo; i++) {
			for (String type: new String[] {"ADJ", "NUM"}) {
				if (dict.get(i).canBe(type) == -1 || d == dict.get(i))
					continue;
				boolean isUsable = true;
				for (String x: dict.get(i).getW(type).getForms()) {
					if (!checkIfNounAdjUsable(x, form)) {
						isUsable = false;
						break;
					}
				}
				// isUsable should be true if the matching form is the ONLY form
				if (isUsable && !dict.get(i).isClaimed())
					adjectives.add(dict.get(i));
			}
		}
		return adjectives;
	}
	
	/* checkIfNounAdjUsable
	 * takes in two strings, both of forms like "NOM S F",
	 * returns true if the two forms are compatible, false otherwise
	 */
	public boolean checkIfNounAdjUsable(String toCheck, String checkAgainst) {
		// 4 is position of plural ("S" or "P") in form
		boolean samePlurals = toCheck.charAt(4) == checkAgainst.charAt(4);
		boolean genders = isGenderMatch(toCheck, checkAgainst);
		boolean cases = isCaseMatch(toCheck, checkAgainst);
		return samePlurals && genders && cases;
	}
	
	/* isGenderMatch
	 * takes in two strings, both of forms like "NOM S F"
	 * returns true if the two forms have compatible genders, false otherwise
	 * M, F, and N are male, female, and neuter; C is M or F and X is any
	 */
	public boolean isGenderMatch(String toCheck, String checkAgainst) {
		// 6 is position of gender ("F", "M", "N", "C", or "X") in form
		char toG = toCheck.charAt(6);
		char agG = checkAgainst.charAt(6);
		if (toG == 'X' || agG == 'X')
			return true;
		else if ((toG == 'C' && agG == 'N') || (toG == 'N' && agG == 'C')) {
			return false;
		} else if (toG == 'C' || agG == 'C')
			return true;
		return toG == agG;
	}

	/* isCaseMatch
	 * takes in two strings, both of forms like "NOM S F"
	 * returns true if the two have the same case
	 */
	public boolean isCaseMatch(String toCheck, String checkAgainst) {
		// case is indicated by the first 3 letters of the string
		String toC = toCheck.substring(0, 3);
		String agC = checkAgainst.substring(0, 3);
		return toC.equals(agC);
	}
	
	/* getNumWordsOfForm
	 * returns the number of DictEntries in the clause that match both
	 * the given part of speech and the given form
	 */
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
