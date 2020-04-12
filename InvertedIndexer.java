package se;

import org.rocksdb.RocksDB;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Arrays;

import javax.swing.text.Document;

import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;

import se.Crawler;
import se.DocumentIndexer;
import se.StopWords;
import se.WordIndexer;


public class InvertedIndexer
{
    protected RocksDB titleInverted;
    protected RocksDB contentInverted;
    protected RocksDB document;
    protected RocksDB word;
    protected RocksDB invertedDocument;
    protected RocksDB invertedWord;
    protected Options options;
    protected Vector<Crawler> crawlers;

    public InvertedIndexer(Vector<Crawler> crawlers, RocksDB word, RocksDB invertedWord, RocksDB document, RocksDB invertedDocument, RocksDB titleInverted, RocksDB contentInverted) throws RocksDBException
    {
        this.crawlers = crawlers;
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.titleInverted = titleInverted;
        this.contentInverted = contentInverted;
        // open document to get pageID
        this.document = document;
        this.invertedDocument = invertedDocument;
        // open word to get wordID
        this.word = word;
        this.invertedWord = invertedWord;
    }

    public InvertedIndexer(RocksDB word, RocksDB invertedWord, RocksDB document, RocksDB invertedDocument, RocksDB titleInverted, RocksDB contentInverted) throws RocksDBException
    {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.titleInverted = titleInverted;
        this.contentInverted = contentInverted;
        // open document to get pageID
        this.document = document;
        this.invertedDocument = invertedDocument;
        // open word to get wordID
        this.word = word;
        this.invertedWord = invertedWord;
    }

    // !!!!!!!!!!!!!!!!!!!! Remove Duplicate if rerun
    public void addEntry(Crawler c) throws RocksDBException
    {
        // Location start from 1
        Vector p = c.parser();
        byte[] pageID = document.get((c.getURL()).getBytes());
        // If pageID is not available
        if(pageID == null) {
            DocumentIndexer documentIndexer = new DocumentIndexer(document, invertedDocument);
            documentIndexer.addEntry(c);
            pageID = document.get((c.getURL()).getBytes());
        }

        // For title
        String[] titleWords = ((Vector<String>) p.get(0)).get(0).split("\\s+");
        Vector<String> title = new Vector<String>(Arrays.asList(titleWords));
        title = StopWords.clean(title);

        for(int i=0; i<title.size(); i++) {
            byte[] wordID = word.get((title.get(i)).getBytes());
            // If wordID is not available
            if(wordID == null) {
                WordIndexer wordIndexer = new WordIndexer(word, invertedWord);
                wordIndexer.addEntry(title.get(i));
                wordID = word.get((title.get(i)).getBytes());
            }

            byte[] value = titleInverted.get(wordID);
            if(value == null) {
                value = ("doc" + new String(pageID) + " " + i+1).getBytes();
            }
            else {
                value = (new String(value) + " doc" + new String(pageID) + " " + i+1).getBytes();
            }
            titleInverted.put(wordID, value);
        }

        // For content
        Vector<String> content = StopWords.clean((Vector<String>) p.get(3));
        for(int i=0; i<content.size(); i++) {
            byte[] wordID = word.get((content.get(i)).getBytes());
            // If wordID is not available
            if(wordID == null) {
                WordIndexer wordIndexer = new WordIndexer(word, invertedWord);
                wordIndexer.addEntry(content.get(i));
                wordID = word.get((content.get(i)).getBytes());
            }
            
            byte[] value = contentInverted.get(wordID);
            if(value == null) {
                value = ("doc" + new String(pageID) + " " + i+1).getBytes();
            }
            else {
                value = (new String(value) + " doc" + new String(pageID) + " " + i+1).getBytes();
            }
            contentInverted.put(wordID, value);
        }
    }
    
    public void delEntry(String word) throws RocksDBException
    {
        // Delete the word and its list from the hashtable
        // ADD YOUR CODES HERE
        // c.remove(word.getBytes());
    } 
    public void print_contentInvIndex() throws RocksDBException
    {
        RocksIterator iter = contentInverted.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            System.out.println(new String(iter.key()) + " -> " + new String(iter.value()));
        }
    }

    public void print_titleInvIndex() throws RocksDBException
    {
        RocksIterator iter = titleInverted.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            System.out.println(new String(iter.key()) + " -> " + new String(iter.value()));
        }
    }

    public void index_pages() throws RocksDBException
    {
        for(int i=0; i<crawlers.size(); i++) {
            addEntry(crawlers.get(i));
        }
    }

    public void close_db() throws RocksDBException
    {
        this.titleInverted.close();
        this.contentInverted.close();
        this.document.close();
        this.word.close();
    }
}