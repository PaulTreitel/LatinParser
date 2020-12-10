package latinparser.wordassistant;


import java.util.ArrayList;
import latinparser.words.Preposition;
import latinparser.DictEntry;
import latinparser.Utility;

/* PREPOSITION METHODS
 *  - ArrayList<DictEntry> findPrepositionPhrases()
 *  - ArrayList<DictEnty> getPrepObjects(int prepPos, String objPart)
 *  - boolean getRevisit()
 * USES
 *  - ArrayList<DictEntry> getAdjectivesFor(DictEntry d)
 *  - int getNumWordsOfForm(String part, String form)
 *  - boolean checkIfNounAdjUsable(String toCheck, String checkAgainst)
 */
public class PrepositionFinder {
	private Utility u;
	private ArrayList<DictEntry> dict;
	private int start;
	private int upTo;
	private boolean revisitPreps;
	
	public PrepositionFinder(ArrayList<DictEntry> d, int sIdx, int tIdx, Utility util) {
		dict = d;
		start = sIdx;
		upTo = tIdx;
		u = util;
	}
	
	//TODO test findPrepositionPhrases()
	/**
	 * Finds prepositional phrases and adds them to a list of all prepositional
	 * phrases and associated Words.
	 * @return list of all Words in prepositional phrases
	 */
	public ArrayList<DictEntry> findPrepositionPhrases() { 
		ArrayList<DictEntry> phrases = new ArrayList<DictEntry>();
		int numPartsTested = 0;
		for (int idx = start; idx < upTo; idx++) {
			if (dict.get(idx).canBe("PREP") == -1) {
				continue;
			}
			int s = phrases.size();
			testPrepositionPhrase(idx, phrases);
			if (s == phrases.size()) {
				// should test all possible parts of speech for subsequent word
				if (numPartsTested < dict.get(idx+1).getWords().size()) {
					dict.get(idx+1).swapForm();
					numPartsTested++;
					idx--;
				// this Word can't be prep phrase but it could be under a different Word
				} else {
					dict.get(idx).removeForm(0);
					idx--;
				}
			}
		}
		for (DictEntry d: phrases) {
			d.claim();
		}
		return phrases;
	}
	
	/**
	 * Determines if a given index is actually a prepositional phrase.
	 * If so, it adds it to the list of phrases and attempts to find all related words.
	 * @param idx - the starting index of the prepositional phrase
	 * @param phrases - the current list of prepositional phrases
	 */
	private void testPrepositionPhrase(int idx, ArrayList<DictEntry> phrases) {
		String objCase = ((Preposition) (dict.get(idx).getWord("PREP"))).getCase();
		for (String part: new String[] {"N", "ADJ", "PRON"}) {
			if (dict.get(idx+1).canBe(part) == -1) {
				continue;
			}
			if (dict.get(idx+1).getWord(part).canBe(objCase) == -1) {
				continue;
			}
			dict.get(idx+1).setPart(part);
			dict.get(idx+1).getWord(part).setForm(objCase);
			phrases.add(dict.get(idx));
			phrases.add(dict.get(idx + 1));
			phrases.addAll(getPrepObjects(idx, objCase));
		}
	}

	/**
	 * Attempts to find all the Words associated with the object of the prepositional phrase.
	 * May not be very successful. It will also set the revisitPreps flag if it totally fails.
	 * Sets the part of speech of all such Words so that they are part of the object phrase.
	 * @param idx - the starting index of the prepositional phrase
	 * @param objCase - the case of the object phrase
	 * @return the list of entries associated with the object of the prepositional phrase 
	 */
	private ArrayList<DictEntry> getPrepObjects(int idx, String objCase) {
		int numNounsOfCase = u.getNumWordsOfForm("N", objCase) 
				+ u.getNumWordsOfForm("PRON", objCase);
		ArrayList<DictEntry> obj = new ArrayList<DictEntry>();
		if (numNounsOfCase == 1) {
			DictEntry sourceWord = dict.get(idx + 1);
			if (sourceWord.canBe("N") != -1) {
				obj.addAll(u.getAdjectivesFor(sourceWord));
			}
		} else if (numNounsOfCase > 1) {
			revisitPreps = true;
		}
		for (DictEntry d: obj)
			d.getWord(0).setForm(objCase);
		return obj;
	}
	
	/**
	 * returns revisitPreps
	 * @return revisitPreps
	 */
	public boolean getRevisit() {return revisitPreps;}
}
