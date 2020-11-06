


import java.util.ArrayList;

public class DictEntry {
	
	private ArrayList<Word> possible = new ArrayList<Word>();
	private String word; //no punctuation or line breaks
	private char punctuation;
	private boolean claimed;
	
	//TODO deal with enclitics (like -que)
	/* DictEntry constructor
	 * initialized punctuation and word
	 * converts entries into the Word list possible and orders it by frequency
	 */
	public DictEntry(String entries, String origin) {
		punctuation = '\0';
		if (".,;:?!".contains(origin.substring(origin.length()-1)))
			punctuation = origin.charAt(origin.length()-1);
		word = origin.toLowerCase().replaceAll(",|[.]|;|:|[?]|!", "");
		
		String[] b = entries.split(";\r\n");
		for (String eInput : b) {
			eInput = eInput.strip();
			if (!eInput.equals("")) {

				if (eInput.charAt(eInput.length()-1) != ';')
					dictToWord((eInput+";").split("\n"));
				else
					dictToWord(eInput.split("\n"));
				
			}
		}
		orderEntries();
	}
	
	/* dictToWord
	 * takes in an array of lines for a single word-output from WORDS
	 * turns it into a Word entry and adds it to the list of possible words
	 */
	private void dictToWord(String[] entry) {
		// handles cases with multiple lines of meanings
		if (entry.length == 1) {
			Word lastWord = possible.get(possible.size()-1);
			if (!lastWord.toString().contains(entry[0])) {
				possible.get(possible.size()-1).addMeaning(entry[0]);
				return;
			}
		}
		
		//grabs the lines containing the meaning and the principal parts
		// also grabs the dictionary codes from the principal parts line
		String mean = entry[entry.length-1];
		String partsLine = entry[entry.length-2];
		partsLine = partsLine.substring(0, partsLine.length()-1);
		String[] parts = partsLine.replaceAll(",", "").split("   |  | ");
		String dictCodes = parts[parts.length-1];
		
		if (addExceptionWordsToEntry(entry, mean, dictCodes))
			return;
		
		int i = addWordToEntry(parts, mean, dictCodes);
		addOtherForms(i, parts, entry);
		
		// culls any possible words that have no valid forms
		for (int x = 0; x < possible.size(); x++) {
			if (possible.get(x).getForms().size() == 0 &&
				!"CONJ/INTERJPREP".contains(possible.get(x).getPart())) {
				possible.remove(x);
			}
		}
	}
	
	/* addOtherForms
	 * adds any extra possible forms to the latest possible Word addition
	 */
	private void addOtherForms(int i, String[] parts, String[] entry) {
		if (i != -1 && "N ADJ V ADV NUM".contains(parts[i])) {
			for (int x = 0; x < entry.length-2; x++) {
				possible.get(possible.size()-1).addPossForm(entry[x]);
			}

			// for when WORDS sometimes lists a 2nd word after the 1st
			// with no forms in between
			if (possible.get(possible.size()-1).getForms().size() == 0 && 
					possible.size() != 1) {
				copyPossForms();
			}
		}

		// since (most) number words are indeclinable
		// TODO handle declinable number words
		if (i != -1 && parts[i].equals("NUM")) {
			possible.get(possible.size()-1).addPossForm("1");
		}
	}
	
	/* addWordToEntry
	 * adds a new Word to the list of possible Words 
	 */
	private int addWordToEntry(String[] parts, String mean, String dictCodes) {
		int x = possible.size();
		boolean decrease = false;
		for (int i = 0; i < parts.length; i++) {
			
			switch (parts[i]) {
			case "N":
				possible.add(new Noun(parts[4].charAt(0), mean, dictCodes));
				break;
			case "ADJ":
				possible.add(new Adjective(mean, dictCodes));
				break;
			case "V":
				possible.add(new Verb(mean, dictCodes));
				break;
			case "ADV":
				possible.add(new Adverb(mean, dictCodes));
				break;
			case "PREP":
				possible.add(new Preposition(mean, dictCodes, parts[i+1]));
				break;
			case "INTERJ":
			case "CONJ":
				possible.add(new ConjInterj(mean, dictCodes));
				break;
			case "NUM":
				possible.add(new Numeral(mean, dictCodes));
				break;
			}
			
			if ("PACKTACKONPREFIXSUFFIX".contains(parts[i]))
				decrease = true;
			
			if (possible.size() > x) {
				if (decrease)
					possible.get(possible.size()-1).reduce();
				return i;
			}
		}
		return -1;
	}

	/* addExceptionWordsToEntry
	 * checks if the new dictionary entry is a special word and, if so, adds it
	 * to the list of possible Words
	 * special word = ignored capital word, pronoun, numerals, suffixes,
	 *     prefixes, and packons
	 */
	private boolean addExceptionWordsToEntry(String[] entry, String mean, 
			String dictCodes) {
		if (entry[0].contains("SUFFIX") || entry[0].contains("PACK") ||
				entry[0].contains("PREFIX")) {
			return true;
			
		} else if (mean.contains("MODE IGNORE_UNKNOWN_NAME")) {
			possible.add(new Noun('P', word, dictCodes));
			return true;
			
		} else if (entry[0].contains("PRON")) {
			for (int i = 0; i < possible.size(); i++)
				if (possible.get(i).getPart().equals("PRON")) {
					return true;
				}
			possible.add(new Pronoun(word, dictCodes));
			return true;
			
		} else if (mean.contains("ROMAN NUMERAL")) {
			possible.add(new Numeral(mean, dictCodes));
			possible.get(possible.size()-1).addPossForm("1");
			return true;
		}
		return false;
	}
	
	/* orderEntries
	 * orders the possible Words by their frequency
	 */
	private void orderEntries() {
		for (int i = 1; i < possible.size(); i++)
			for (int x = i; x < possible.size(); x++)
				if (!possible.get(x).getPart().equals("NUM") && 
					possible.get(x).getFreq() > possible.get(x-1).getFreq())
					
					possible.add(x-1, possible.remove(x));
	}
	
	/* canBe
	 * determines if the the DictEntry can be a certain part of speech,
	 * returns -1 if not
	 * allows for negation by "!" prefix - if the DictEntry can't be
	 * that part of speech
	 */
	public int canBe(String part) {
		for (int a = 0; a < possible.size(); a++)
			if (part.charAt(0) != '!' && possible.get(a).getPart().equals(part))
				return a;
		//allows for negation (ie canNotBe) 
			else if (part.charAt(0) == '!' &&
					!possible.get(a).getPart().equals(part.substring(1)))
				return a;
		return -1;
	}
	
	/* setPart
	 * sets the DictEntry to a certain part of speech by removing every 
	 * possibility that is not that part of speech
	 */
	public void setPart(String part) {
		for (int i = possible.size()-1; i >= 0; i--) {
			if (part.charAt(0) != '!' && 
					!possible.get(i).getPart().equals(part)) {
				possible.remove(i);
		// allows for negation (ie setPartToNotBe)
			} else if (possible.get(i).getPart().equals(part.substring(1)) &&
					part.charAt(0) == '!' ) {
				possible.remove(i);
			}
		}
	}
	
	/* copyPossForms
	 * copies all form from the 2nd-to-last Word to the last Word
	 */
	private void copyPossForms() {
		for (String form: possible.get(possible.size()-2).getForms())
			possible.get(possible.size()-1).addPossForm(form);
	}
	
	/* getW
	 * get a particular word by index or by part of speech
	 */
	public Word getW(int idx) {return possible.get(idx);}
	public Word getW(String part) {
		return possible.get(canBe(part));
	}
	
	/* removeForm
	 * removes a possible Word by index
	 */
	public void removeForm(int idx) {possible.remove(idx);}
	
	/* swapForm
	 * removes a Word from the start of the list, then adds it to the end
	 */
	public void swapForm() {possible.add(possible.remove(0));}
	
	public void setClaimed() {claimed = true;}
	public boolean isClaimed() {return claimed;}
	
	public char getPunct() {return punctuation;}
	public String toString() {return word;}
	public ArrayList<Word> getWords() {return possible;}

	public void addPart(String part, String mean, ArrayList<String> forms) {
		addWordToEntry(new String[] {part}, mean, "[XXXXX]");
		for (String f: forms) {
			String toAdd = f.substring(0, f.length()-10) + "POS";
			possible.get(possible.size()-1).addPossForm(toAdd);
		}
	}
}


