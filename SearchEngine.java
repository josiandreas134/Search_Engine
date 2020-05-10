package se;

import java.util.Arrays;
import java.util.Vector;
import java.io.FileWriter; 
import java.io.IOException;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Options;

import se.ForwardIndexer;
import se.InvertedIndexer;
import se.StopWords;
import se.WordIndexer;

public class SearchEngine {
    public static void main(String[] args) {
        try{
            RocksDB.loadLibrary();
            Options options = new Options();
            options.setCreateIfMissing(true);
            RocksDB document = RocksDB.open(options, "db/document");
            RocksDB invertedDocument = RocksDB.open(options, "db/invertedDocument");
            RocksDB word = RocksDB.open(options, "db/word");
            RocksDB invertedWord = RocksDB.open(options, "db/invertedWord");
            RocksDB forwardIndex = RocksDB.open(options, "db/forwardIndex");
            RocksDB titleInverted = RocksDB.open(options, "db/titleInverted");
            RocksDB contentInverted = RocksDB.open(options, "db/contentInverted");
            
            // Crawl
            System.out.println("Start crawling");
            Crawler crawler = new Crawler("https://www.cse.ust.hk/");
            Vector<Crawler> crawlers = crawler.crawlers(2, forwardIndex);
            System.out.println("Finished crawling");

            DocumentIndexer documentIndexer = new DocumentIndexer(crawlers, document, invertedDocument);
            documentIndexer.index_pages();
            System.out.println("Finished making document index");
            
            WordIndexer wordIndexer = new WordIndexer(crawlers, word, invertedWord);
            wordIndexer.index_words();
            System.out.println("Finished making word index");
            
            ForwardIndexer forwardIndexer = new ForwardIndexer(crawlers, word, invertedWord, document, invertedDocument, forwardIndex);
            forwardIndexer.index_pages();
            System.out.println("Finished making forward index");
            
            InvertedIndexer invertedIndexer = new InvertedIndexer(crawlers, word, invertedWord, document, invertedDocument, titleInverted, contentInverted);
            invertedIndexer.index_pages();
            System.out.println("Finished making inverted index");

            // Output for PHASE I
            try {
                FileWriter myWriter = new FileWriter("spider_result.txt");
                RocksIterator iter = forwardIndex.newIterator();
                    
                int num = 0;
                System.out.println("Saving to txt");
                for(iter.seekToFirst(); iter.isValid(); iter.next()) {
                    num++;
                    System.out.println(num);
                    myWriter.write("Page "+num+"\n");
                    String[] page = ((new String(iter.value())).split(" ;;; "));
                    if(page.length != 5) {
                        // System.out.println("Array length" + page.length);
                        // for(int x=0; x<page.length; x++) {
                        //     System.out.println(page[x]);
                        // }
                    }
                    if(page[0] != null) {
                        myWriter.write(page[0] + "\n");                                        // Title
                    }
                    else {
                        myWriter.write("No Title\n");                                        // Title
                    }
                    myWriter.write(new String(invertedDocument.get(iter.key())) + "\n");   // URL
                    myWriter.write(page[1] + ", " + page[2] + "\n");                       // Date, Size
                    // List
                    if(page.length > 3) {
                        String[] list = (page[3]).split("\\s+");
                        for(int i=0; i<list.length; i++) {
                            // docID
                            if(i%2 == 0) {
                                // System.out.println(new String(inverte));
                                myWriter.write(new String(invertedWord.get(list[i].getBytes())) + " ");
                            }
                            // freq
                            else {
                                if(i != list.length-1) {
                                    myWriter.write(list[i] + ", ");
                                }
                                else {
                                    myWriter.write(list[i] + "\n");
                                }
                            }
                        }
                    }

                    // Links
                    if(page.length > 4) {
                        String[] links = (page[4]).split("\\s+");
                        for(int i=0; i<links.length; i++) {
                            myWriter.write(links[i] + "\n");
                        }
                    }
                    myWriter.write("\n\n");
                }   
                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
            }
            
            // invertedIndexer.print_titleInvIndex();
            // System.out.println("----------------------------------------");
            // invertedIndexer.print_contentInvIndex();
            
            // forwardIndexer.printAll();

            // wordIndexer.print_wordIndex();
            // System.out.println("----------------------------------------");
            // wordIndexer.print_invertedWordIndex();

            // documentIndexer.print_docIndex();
            // System.out.println("----------------------------------------");
            // documentIndexer.print_invertedDocIndex();

            // // Add page to index
            // for(int i=0; i<crawlers.size(); i++) {
            //     System.out.println(crawlers.get(i).getURL());
            //     index.addEntry(crawlers.get(i));
            // }
            // index.printAll();
        }
        catch(RocksDBException e)
        {
            System.err.println(e.toString());
        }
    }
}
