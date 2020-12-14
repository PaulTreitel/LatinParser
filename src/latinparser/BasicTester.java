package latinparser;



public class BasicTester {
	public static void main(String[] args) {
		LatinParser.parse(System.getProperty("user.dir") + "/src/in.txt");
		System.out.println("translation: " + LatinParser.translate());
	}
}