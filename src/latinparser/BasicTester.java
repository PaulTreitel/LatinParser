package latinparser;

//import java.util.ArrayList;
//import latinparser.partsofspeech.Noun;

public class BasicTester {
	public static void main(String[] args) {
		System.out.println(LatinParser.parse(System.getProperty("user.dir") + "/src/in.txt"));
	}
}