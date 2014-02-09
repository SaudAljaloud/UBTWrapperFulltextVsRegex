package de.l3s.ubt.wrapper.jena2_11Regex.Test2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test3 {

	public static void main(String[] args) {
		Test3 test = new Test3();
		test.run();
	}

	public void run() {

		String ngram = "^smith$";
		Pattern pattern2 = Pattern.compile("\\.|\\*|\\+|\\^|\\$|\\[|\\]|\\s|f");
		Matcher matcher = pattern2.matcher(ngram);
		if (matcher.find()) {
			System.out.println("yes");
		}

	}

}
