package se;

import java.util.Arrays;
import java.util.Vector;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;

import se.Crawler;
import se.StopWords;

public class WordIndexer {
    private Vector<Crawler> crawlers;
    private RocksDB word;
    private RocksDB invertedWord;
    private Options options;

    WordIndexer(Vector<Crawler> crawlers, RocksDB word, RocksDB invertedWord) throws RocksDBException
    {
        this.crawlers = crawlers;
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.word = word;
        this.invertedWord = invertedWord;
        // this.word = RocksDB.open(options, "db/word");
        // this.invertedWord = RocksDB.open(options, "db/invertedWord");
    }

    WordIndexer(RocksDB word, RocksDB invertedWord) throws RocksDBException
    {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.word = word;
        this.invertedWord = invertedWord;
    }

    // pageID starts from 1

    public void addEntry(String new_word) throws RocksDBException
    {
        // Get the latest wordID
        Integer lastID = 0;
        RocksIterator iter = word.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            lastID++;
        }
    
        byte[] ID = word.get(new_word.getBytes());
        if (ID == null) {
            lastID++;
            ID = (Integer.toString(lastID)).getBytes();
            // System.out.println("Added " + new_word + " -> " + lastID);
            word.put(new_word.getBytes(), ID);
            invertedWord.put(ID, new_word.getBytes());
        }
    }

    public void addEntry(Crawler c) throws RocksDBException
    {
        // Get the latest wordID
        Integer lastID = 0;
        RocksIterator iter = word.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            lastID++;
        }

        Vector p = c.parser();

        // For title
        String[] titleWords = ((Vector<String>) p.get(0)).get(0).split("\\s+");
        Vector<String> title = new Vector<String>(Arrays.asList(titleWords));
        title = StopWords.clean(title);
        for(int j=0; j<title.size(); j++) {
            String w = title.get(j);
            byte[] ID = word.get(w.getBytes());
            if (ID == null) {
                lastID++;
                ID = (Integer.toString(lastID)).getBytes();
                // System.out.println("Added " + w + " -> " + lastID);
                word.put(w.getBytes(), ID);
                invertedWord.put(ID, w.getBytes());
            }
        }
        
        
        // For content
        Vector<String> content = StopWords.clean((Vector<String>) p.get(3));
        for(int j=0; j<content.size(); j++) {
            String w = content.get(j);
            byte[] ID = word.get(w.getBytes());
            if (ID == null) {
                lastID++;
                ID = (Integer.toString(lastID)).getBytes();
                // System.out.println("Added " + w + " -> " + lastID);
                word.put(w.getBytes(), ID);
                invertedWord.put(ID, w.getBytes());
            }
        }
    }

    public void index_words() throws RocksDBException
    {
        // Get the latest pageID
        Integer lastID = 0;
        RocksIterator iter = word.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            lastID++;
        }
        System.out.println("No. of words indexed = " + lastID);

        for(int i=0; i<crawlers.size(); i++) {
            Crawler c = crawlers.get(i);
            Vector p = c.parser();

            // For title
            String[] titleWords = ((Vector<String>) p.get(0)).get(0).split("\\s+");
            Vector<String> title = new Vector<String>(Arrays.asList(titleWords));
            title = StopWords.clean(title);
            for(int j=0; j<title.size(); j++) {
                String w = title.get(j);
                byte[] ID = word.get(w.getBytes());
                if (ID == null) {
                    lastID++;
                    ID = (Integer.toString(lastID)).getBytes();
                    // System.out.println("Added " + w + " -> " + lastID);
                    word.put(w.getBytes(), ID);
                    invertedWord.put(ID, w.getBytes());
                }
            }
            
            
            // For content
            Vector<String> content = StopWords.clean((Vector<String>) p.get(3));
            for(int j=0; j<content.size(); j++) {
                String w = content.get(j);
                byte[] ID = word.get(w.getBytes());
                if (ID == null) {
                    lastID++;
                    ID = (Integer.toString(lastID)).getBytes();
                    // System.out.println("Added " + w + " -> " + lastID);
                    word.put(w.getBytes(), ID);
                    invertedWord.put(ID, w.getBytes());
                }
            }
        }
    }

    public void print_wordIndex() throws RocksDBException
    {
        RocksIterator iter = word.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            System.out.println(new String(iter.key()) + " -> " + new String(iter.value()));
        }
    }

    public void print_invertedWordIndex() throws RocksDBException
    {
        RocksIterator iter = invertedWord.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            System.out.println(new String(iter.key()) + " -> " + new String(iter.value()));
        }
    }

    public void close_db() throws RocksDBException
    {
        this.invertedWord.closeE();
        this.word.closeE();
        // invertedWord.closeE();
    }
}