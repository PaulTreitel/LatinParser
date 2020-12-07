package latinparser;



import java.util.ArrayList;

/* UTILITY
 *  - ArrayList<DictEntry> getAdjectivesFor(DictEntry d)
 *  - boolean nounAdjUsable(String toCheck, String checkAgainst)
 *  - boolean isGenderMatch(String toCheck, String checkAgainst)
 *  - int getNumWordsOfForm(String part, String form)
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
	
	/**
	 * Takes in a DictEntry that can be a (pro)noun and generates a list
	 * of adjectives in the clause that can match with its first form only.
	 * This assumes that the first form is the one you want to match against.
	 * @param d - the DictEntry to match against
	 * @return list of DictEntries that match the given entry
	 */
	public ArrayList<DictEntry> getAdjectivesFor(DictEntry d) {
		String form;
		String[] types = new String[] {"ADJ", "NUM"};
		ArrayList<DictEntry> adjectives = new ArrayList<DictEntry>();
		//assumes words & forms are already ordered/narrowed down
		if (d.canBe("N") != -1)
			form = d.getWord("N").getForm(0);
		else
			form = d.getWord("PRON").getForm(0);
		
		for (int i = start; i < upTo; i++) {
			DictEntry currEntry = dict.get(i);
			if (currEntry.isClaimed()) {
				continue;
			}
			for (String type: types) {
				if (currEntry.canBe(type) == -1 || d == currEntry)
					continue;
				boolean exactFormMatch = true;
				for (String x: currEntry.getWord(type).getForms()) {
					// ensures that there are no possible forms that don't match
					if (!nounAdjUsable(x, form)) {
						exactFormMatch = false;
						break;
					}
				}
				if (exactFormMatch) {
					adjectives.add(currEntry);
				}
			}
		}
		return adjectives;
	}
	
	/**
	 * Takes in two strings, both noun forms like "NOM S F",
	 * returns true if the two forms are compatible, false otherwise
	 * @param first - the first noun form
	 * @param second - the second noun form
	 * @return true if the two noun forms are compatible, false otherwise
	 */
	public boolean nounAdjUsable(String first, String second) {
		// 4 is position of plural ("S" or "P") in form
		boolean plurals = first.charAt(4) == second.charAt(4);
		// 6 is position of gender ("F", "M", "N", "C", or "X") in form
		boolean genders = isGenderMatch(first.charAt(6), second.charAt(6));
		// case is indicated by the first 3 letters of the string
		boolean cases = first.substring(0, 3).equals(second.substring(0, 3));
		return plurals && genders && cases;
	}
	
	/**
	 * Takes in two strings, both noun forms like "NOM S F", and
	 * returns true if the two forms have compatible genders, false otherwise.
	 * M, F, and N are male, female, and neuter; C is M or F; and X is any.
	 * @param first - the first noun form
	 * @param second - the second noun form
	 * @return true if the genders are compatible, false otherwise
	 */
	public boolean isGenderMatch(char first, char second) {
		if (first == 'X' || second == 'X') {
			return true;
		} else if ((first == 'C' && second == 'N') || (first == 'N' && second == 'C')) {
			return false;
		} else if (first == 'C' || second == 'C') {
			return true;
		}
		return first == second;
	}
	
	/**
	 * Returns the number of DictEntries in the clause that match both
	 * the given part of speech and the given form
	 * @param part - the part of speech to match
	 * @param form - the word form to match
	 * @return number of matching DictEntries
	 */
	public int getNumWordsOfForm(String part, String form) {
		int n = 0;
		for (int i = start; i < upTo; i++) {
			DictEntry d = dict.get(i);
			if (d.canBe(part) != -1 && d.getWord(part).canBe(form) != -1) {
				n++;
			}
		}
		return n;
	}
}
