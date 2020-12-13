package latinparser;


import java.util.ArrayList;
import latinparser.words.*;

public class DictEntry {
	
	private ArrayList<Word> possible = new ArrayList<Word>();
	private String word; //no punctuation or line breaks
	private char punctuation;
	private boolean claimed;
	
	/**
	 * Creates a DictEntry object.
	 * @param entries - string representing all of the WORDS output for for the
	 * DictEntry
	 * @param origin - the original word
	 */
	//TODO deal with enclitics (like -que)
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
					eInput = eInput + ";";
				dictToWord(eInput.split("\n"));
			}
		}
		orderEntries();
	}
	
	/**
	 * Takes in an array of lines for a single word-output from WORDS and turns
	 * it into a Word entry and adds it to the list of possible words.
	 * @param entry - the lines of a single dictionary result
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
		if (i != -1) {
			addOtherForms(parts[i], entry);
		}
		
		// culls any possible words that have no valid forms
		for (int x = 0; x < possible.size(); x++) {
			if (possible.get(x).getForms() == null)
				continue;
			if (possible.get(x).getForms().size() > 0)
				continue;
			if ("CONJ/INTERJPREP".contains(possible.get(x).getPart()))
				continue;
			possible.remove(x);
		}
	}
	
	/**
	 * Adds any extra possible forms to the last Word in the list of possible
	 * Words.
	 * @param partOfSpeech - the part of speech of the Word
	 * @param entry - the lines of a single dictionary result
	 */
	private void addOtherForms(String partOfSpeech, String[] entry) {
		Word w = possible.get(possible.size() - 1);
		if ("N ADJ V ADV NUM".contains(partOfSpeech)) {
			for (int x = 0; x < entry.length-2; x++) {
				w.addPossForm(entry[x]);
			}
			// for when WORDS sometimes lists a 2nd word after the 1st with no forms in between
			if (w.getForms().size() == 0 && possible.size() != 1) {
				copyPossForms();
			}
		}

		/* if there's 1 form and it starts with X, the number is indeclinable
		 * and it could be any possible form */
		if (partOfSpeech.equals("NUM")) {
			if (w.getForms().size() == 1 && w.getForm(0).charAt(0) == 'X') {
				w.addPossForm("1");
			}
		}
	}
	
	/**
	 * Adds a new Word to the list of possible Words.
	 * @param parts - the list of parts of speech of the Words
	 * @param mean - the meaning of the Word
	 * @param dictCodes - the WORDS codes for word frequency, type, etc
	 * @return the index of the part of speech in the list of parts
	 * corresponding to the Word that was added
	 */
	// TODO find a way to call this function with only actual parts of speech
	private int addWordToEntry(String[] parts, String mean, String dictCodes) {
		int x = possible.size();
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
			
			if (possible.size() > x) {
				if ("PACKTACKONPREFIXSUFFIX".contains(parts[i])) {
					possible.get(possible.size()-1).reduce();
				}
				return i;
			}
		}
		return -1;
	}

	/**
	 * Checks if the newest dictionary entry is a special entry and, if so, adds
	 * it to the list of possible Words.
	 * @param entry - the lines of a single dictionary result
	 * @param mean - the meaning of the Word
	 * @param dictCodes - the WORDS codes for word frequency, type, etc
	 * @return true if a new word is added, false otherwise
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
	
	/**
	 * Orders the possible Words based on their frequency.
	 */
	private void orderEntries() {
		for (int i = 1; i < possible.size(); i++)
			for (int x = i; x < possible.size(); x++)
				if (!possible.get(x).getPart().equals("NUM") && 
					possible.get(x).getFreq() > possible.get(x-1).getFreq())
					
					possible.add(x-1, possible.remove(x));
	}
	
	/**
	 * Determines if the the DictEntry can be a certain part of speech. Allows
	 * for negation by the "!" prefix - if the DictEntry can't be that part of
	 * speech.
	 * @param part - the part of speech to search for
	 * @return true if there is a word matching the given part of speech, false
	 * otherwise
	 */
	public boolean canBe(String part) {
		return getWordIdx(part) != -1;
	}
	
	/**
	 * Gets the index of the first Word that matches the given part of speech.
	 * Allows for negation by the "!" prefix - if the DictEntry can't be that
	 * part of speech.
	 * @param part - the part of speech to search for
	 * @return the index of the first Word that matches the given part of
	 * speech, -1 otherwise
	 */
	public int getWordIdx(String part) {
		boolean negated = part.charAt(0) == '!';
		for (int a = 0; a < possible.size(); a++) {
			String currPart = possible.get(a).getPart();
			if (!negated && currPart.equals(part)) {
				return a;
			//allows for negation (ie canNotBe) 
			} else if (negated && !currPart.equals(part.substring(1))) {
				return a;
			}
		}
		return -1;
	}
	
	/**
	 * Sets the DictEntry to a certain part of speech by removing every
	 * possible Word that is not that part of speech. Allows for negation by the
	 * "!" prefix - to set the DictEntry to not be the given part of speech.
	 * @param part - the part of speech to set the Word to
	 */
	public void setPart(String part) {
		boolean negated = part.charAt(0) == '!';
		String absolutePart = part.substring(1);
		for (int i = possible.size()-1; i >= 0; i--) {
			String currPart = possible.get(i).getPart();
			if (!negated &&  !currPart.equals(part)) {
				possible.remove(i);
			} else if (negated && currPart.equals(absolutePart)) {
				possible.remove(i);
			}
		}
	}
	
	/**
	 * Copies all forms from the 2nd to last Word to the last Word.
	 */
	private void copyPossForms() {
		for (String form: possible.get(possible.size()-2).getForms())
			possible.get(possible.size()-1).addPossForm(form);
	}
	
	/**
	 * Adds a new Word to the list of possible Words.
	 * @param part - the part of speech of the new Word
	 * @param mean - the meaning of the Word
	 * @param forms - list of possible forms of the Word
	 */
	// TODO figure out what this does and why it does so
	public void addPart(String part, String mean, ArrayList<String> forms) {
		addWordToEntry(new String[] {part}, mean, "[XXXXX]");
		for (String f: forms) {
			String toAdd = f.substring(0, f.length()-10) + "POS";
			possible.get(possible.size()-1).addPossForm(toAdd);
		}
	}
	
	/**
	 * Removes a Word from the start of the list of possible Words, then adds it
	 * to the end.
	 */
	public void swapForm() {possible.add(possible.remove(0));}
	
	/**
	 * Gets a Word by its part of speech in the list of possible Words.
	 * @param part - the part of speech of the desired Word
	 * @return the first Word with the given part of speech
	 */
	public Word getWord(String part) {
		return possible.get(getWordIdx(part));
	}
	
	public Word getWord(int idx) {return possible.get(idx);}
	public void removeWord(int idx) {possible.remove(idx);}
	
	public void claim() {claimed = true;}
	public boolean isClaimed() {return claimed;}
	
	public char getPunct() {return punctuation;}
	public String toString() {return word;}
	public ArrayList<Word> getWords() {return possible;}
}


