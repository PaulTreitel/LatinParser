

import java.util.ArrayList;

public interface Word {
	public String translate(String notes);
	public int getFreq();
	public void addPossForm(String e);
	public void addMeaning(String m);
	public void reduce();
	public String getPart();
	public ArrayList<String> getForms();
	public String getF(int idx);
	public int canBe(String f);
	public void setPart(String part);
}
