package se;

// import IRUtilities.*;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;
import java.util.Iterator;

import java.io.FileWriter;

public class StopWords
{
	private Porter porter;
	private java.util.HashSet stopWords;
	public boolean isStopWord(String str)
	{
			return stopWords.contains(str);
	}
	public StopWords(String str)
	{
			super();
			porter = new Porter();
			stopWords = new java.util.HashSet();

			try {
					BufferedReader reader = new BufferedReader(new FileReader(str));
					String line;
					while ((line = reader.readLine()) != null) {
							stopWords.add(line);
					}
					reader.close();
			} catch (IOException e) {
					e.printStackTrace();
			}

	}
	public String stem(String str)
	{
		return porter.stripAffixes(str);
	}

	public static Vector<String> clean(Vector<String> input) {
		StopWords stopStem = new StopWords("stopwords.txt");
		Iterator it = input.iterator();

		Vector<String> result = new Vector<String>();

		while(it.hasNext()){
			String str = it.next().toString();
			if(str.length() > 4 && !stopStem.isStopWord(str)){
				result.add(stopStem.stem(str));
			}	
		}
		return result;
	}
	// public static void main(String[] arg)
	// {
	// 	StopWords stopStem = new StopWords();
	// 	Scanner in = null;

	// 	try{
	// 		in = new Scanner(new File("parser_output.txt"));
	// 		PrintStream out = new PrintStream(new FileOutputStream("StopWord.txt"));
	// 		System.setOut(out);			
	// 		//String input="";
	// 		while(in.hasNext()){
	// 			String input=in.next();
	// 			if(input.length() < 4){
	// 				System.out.print("");
	// 			}
	// 			else if (stopStem.isStopWord(input)){
	// 				System.out.print("");
	// 			}
	// 			else{
	// 				System.out.println(stopStem.stem(input)+" ");
	// 			}	
	// 		}
	// 	}
	// 	catch(IOException ioe)
	// 	{
	// 		System.err.println(ioe.toString());
	// 	}
	// }
}