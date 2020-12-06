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
	public ArrayList<DictEntry> findPrepositionPhrases() { 
		ArrayList<DictEntry> phrases = new ArrayList<DictEntry>();
		int n = 0, h = 0;
		for (int i = start; i < upTo; i++) {
			if (dict.get(i).canBe("PREP") != -1) {
				int s = phrases.size();
				for (String part: new String[] {"N", "ADJ", "PRON"}) {
					String objPart = ((Preposition) (dict.get(i).getW("PREP"))).getCase();
					if (dict.get(i+1).canBe(part) != -1 && 
							dict.get(i+1).getW(part).canBe(objPart) != -1) {
						phrases.add(dict.get(i));
						dict.get(i+1).setPart(part);
						phrases.addAll(getPrepObjects(i, objPart));
					}
				}
				if (s == phrases.size() && n < dict.get(i).getWords().size()) {
					dict.get(i).swapForm();
					n++;
					i--;
				} else if (s == phrases.size() && h < dict.get(i+1).getWords().size()) {
					dict.get(i+1).swapForm();
					n = 0;
					h++;
					i--;
				}
			}
		}
		for (DictEntry d: phrases) {
			d.setClaimed();
		}
		return phrases;
	}

	private ArrayList<DictEntry> getPrepObjects(int prepPos, String objPart) {
		int numNounsOfCase = u.getNumWordsOfForm("N", objPart) + u.getNumWordsOfForm("PRON", objPart);
		ArrayList<DictEntry> obj = new ArrayList<DictEntry>();
		if (dict.get(prepPos+1).getW(0).canBe(objPart) == -1) {
			return obj;
		} if (numNounsOfCase == 1) {
			obj.add(dict.get(prepPos+1));
			if (dict.get(prepPos+1).canBe("ADJ") == -1 && dict.get(prepPos+1).canBe("NUM") == -1)
				obj.addAll(u.getAdjectivesFor(obj.get(0)));
			for (DictEntry d: obj)
				d.getW(0).setPart(objPart);
		} else if (numNounsOfCase > 1) {
			obj.add(dict.get(prepPos+1));
			revisitPreps = true;
		}
		return obj;
	}
	
	public boolean getRevisit() {return revisitPreps;}
}
