package latinparser;

import java.util.ArrayList;


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
	 * Takes in a DictEntry that can be a Noun or Pronoun and generates a list
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
		if (d.canBe("N"))
			form = d.getWord("N").getForm(0);
		else
			form = d.getWord("PRON").getForm(0);
		
		for (int i = start; i < upTo; i++) {
			DictEntry currEntry = dict.get(i);
			if (currEntry.isClaimed()) {
				continue;
			}
			for (String type: types) {
				if (!currEntry.canBe(type) || d == currEntry)
					continue;
				boolean exactFormMatch = true;
				for (String x: currEntry.getWord(type).getForms()) {
					// ensures that there are no possible forms that don't match
					if (!nounAdjMatch(x, form)) {
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
	 * Takes a partial form for a Noun or Adjective (Pronoun and Numeral also work)
	 * and expands it into a full form, using '_' as a filler character.
	 * @param f - the partial form
	 * @param negated - whether or not the form is negated (starts with a '!')
	 * @return the expanded form, without a negation and with filler characters
	 */
	public static String expandNounAdjForm(String f, boolean negated) {
		if (negated) {
			f = f.substring(1);
		}
		if (f.length() == 3) {
			if (f.charAt(0) == 'S' || f.charAt(0) == 'P') {
				f = "___ " + f;
			} else {
				f = f + " _ _";
			}
		} else if (f.length() == 1) {
			if (f.equals("S") || f.equals("P")) {
				f = "___ " + f + " _";
			} else {
				f = "___ _ " + f;
			}
		} else if (f.length() == 5) {
			if (f.charAt(4) == 'S' || f.charAt(4) == 'P') {
				f = f + " _";
			} else {
				f = f.substring(0, 3) + " _ " + f.substring(4);
			}
		}
		return f;
	}
	
	/**
	 * Takes in two strings, both noun forms like "NOM S F",
	 * returns true if the two forms are compatible, false otherwise
	 * @param first - the first noun form
	 * @param second - the second noun form
	 * @return true if the two noun forms are compatible, false otherwise
	 */
	public static boolean nounAdjMatch(String first, String second) {
		// 4 is position of plural ("S" or "P") in form
		char[] plural = {first.charAt(4), second.charAt(4)};
		// 6 is position of gender ("F", "M", "N", "C", or "X") in form
		char[] gender = {first.charAt(4), second.charAt(4)};
		// case is indicated by the first 3 letters of the string
		String[] cases = {first.substring(0, 3), second.substring(0, 3)};
		
		boolean pluralMatch = plural[0] == plural[1] || plural[0] == '_' || plural[1] == '_';
		boolean genderMatch = isGenderMatch(gender[0], gender[1]) || 
				gender[0] == '_' || gender[1] == '_';
		boolean caseMatch = cases[0].equals(cases[1]) || 
				cases[0].equals("___") || cases[1].equals("___");
		return pluralMatch && genderMatch && caseMatch;
	}
	
	/**
	 * Takes in two strings, both noun forms like "NOM S F", and
	 * returns true if the two forms have compatible genders, false otherwise.
	 * M, F, and N are male, female, and neuter; C is M or F; and X is any.
	 * @param first - the first noun form
	 * @param second - the second noun form
	 * @return true if the genders are compatible, false otherwise
	 */
	private static boolean isGenderMatch(char first, char second) {
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
	 * Returns the number of DictEntries in the clause that can match both
	 * the given part of speech and the given form
	 * @param part - the part of speech to match
	 * @param form - the word form to match
	 * @return number of matching DictEntries
	 */
	public int getNumWordsOfForm(String part, String form) {
		int n = 0;
		for (int i = start; i < upTo; i++) {
			DictEntry d = dict.get(i);
			if (!d.canBe(part)) {
				continue;
			}
			if (!d.getWord(part).canBe(form)) {
				continue;
			}
			n++;
		}
		return n;
	}
	
	/**
	 * Searches through the clause for an entry which can match both the given
	 * part of speech and the given form.
	 * @param part - the part of speech to match
	 * @param form - the word form to match
	 * @return the first entry to meet both criteria, or null if there are none
	 */
	public DictEntry getWordByForm(String part, String form) {
		for (int i = start; i < upTo; i++) {
			DictEntry currEntry = dict.get(i);
			if (!currEntry.canBe(part)) {
				continue;
			}
			if (!currEntry.getWord(part).canBe(form)) {
				continue;
			}
			return currEntry;
		}
		return null;
	}
}
