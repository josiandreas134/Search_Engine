package se;

import org.rocksdb.RocksDB;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.text.Document;

import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;

import se.Crawler;
import se.DocumentIndexer;
import se.StopWords;
import se.WordIndexer;


public class ForwardIndexer
{
    protected RocksDB db;
    protected RocksDB document;
    protected RocksDB word;
    protected RocksDB invertedDocument;
    protected RocksDB invertedWord;
    protected Options options;
    protected Vector<Crawler> crawlers;

    public ForwardIndexer(Vector<Crawler> crawlers, RocksDB word, RocksDB invertedWord, RocksDB document, RocksDB invertedDocument, RocksDB forwardIndex) throws RocksDBException
    {
        this.crawlers = crawlers;
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.db = forwardIndex;
        // open document to get pageID
        this.document = document;
        this.invertedDocument = invertedDocument;
        // open word to get wordID
        this.word = word;
        this.invertedWord = invertedWord;
    }

    public ForwardIndexer(RocksDB word, RocksDB invertedWord, RocksDB document, RocksDB invertedDocument, RocksDB forwardIndex) throws RocksDBException
    {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.db = forwardIndex;
        // open document to get pageID
        this.document = document;
        this.invertedDocument = invertedDocument;
        // open word to get wordID
        this.word = word;
        this.invertedWord = invertedWord;
    }

    public void addEntry(Crawler c) throws RocksDBException
    {
        Vector parsed = c.parser();
        Vector<String> content = (Vector<String>) parsed.get(3);
        Vector<String> links = (Vector<String>) parsed.get(4);

        byte[] pageID = document.get((c.getURL()).getBytes());
        // If pageID is not available
        if(pageID == null) {
            DocumentIndexer documentIndexer = new DocumentIndexer(document, invertedDocument);
            documentIndexer.addEntry(c);
            pageID = document.get((c.getURL()).getBytes());
        }

        String data = ((Vector<String>)parsed.get(0)).get(0) + " ;;; " + ((Vector<String>)parsed.get(1)).get(0) + " ;;; " + ((Vector<String>)parsed.get(2)).get(0) + " ;;; ";

        
        content = StopWords.clean(content);
        // HashMap
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for(int i=0; i<content.size(); i++) {
            String key = content.get(i);

            // Get wordID
            byte[] ID = word.get(key.getBytes());
            if(ID == null) {
                WordIndexer wordIndexer = new WordIndexer(word, invertedWord);
                wordIndexer.addEntry(content.get(i));
                ID = word.get(key.getBytes());
            }

            Integer count = (Integer) map.get(new String(ID));
            if(count == null || count == 0){
                map.put(new String(ID), 1);
            }
            else {
                map.put(new String(ID), (count+1));
            }
        }

        int word_count = 0;

        for (Map.Entry<String, Integer> e : map.entrySet()) {
            data += e.getKey();
            data += " ";
            data += e.getValue();
            data += " ";
            word_count += e.getValue() * e.getValue();
        }

        data += " ;;; ";
        for(int i=0; i<links.size(); i++) {
            data += links.get(i) + " ";
        }

        data += " ;;; " + word_count;
        db.put(pageID, data.getBytes());
    }
    
    public void printAll() throws RocksDBException
    {
        RocksIterator iter = db.newIterator();
                    
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println(new String(iter.key()) + "=" + new String(iter.value()));
        }
    }    

    public void index_pages() throws RocksDBException
    {
        for(int i=0; i<crawlers.size(); i++) {
            addEntry(crawlers.get(i));
        }
    }
}