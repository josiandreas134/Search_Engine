package se;

import java.util.Vector;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;

import se.Crawler;

public class DocumentIndexer {
    private Vector<Crawler> crawlers;
    private RocksDB document;
    private RocksDB invertedDocument;
    private Options options;

    DocumentIndexer(Vector<Crawler> crawlers, RocksDB document, RocksDB invertedDocument) throws RocksDBException
    {
        this.crawlers = crawlers;
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.document = document;
        this.invertedDocument = invertedDocument;
        // this.document = RocksDB.open(options, "db/document");
        // this.invertedDocument = RocksDB.open(options, "db/invertedDocument");
    }

    DocumentIndexer(RocksDB document, RocksDB invertedDocument) throws RocksDBException
    {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.document = document;
        this.invertedDocument = invertedDocument;
    }

    // pageID starts from 1

    public void addEntry(Crawler c) throws RocksDBException
    {
        // Get the latest pageID
        Integer lastID = 0;
        RocksIterator iter = document.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            lastID++;
        }

        byte[] ID = document.get(c.getURL().getBytes());

        if (ID == null) {
            lastID++;
            ID = (Integer.toString(lastID)).getBytes();
            // System.out.println("Added " + c.getURL() + " -> " + lastID);
            document.put((c.getURL()).getBytes(), ID);
            invertedDocument.put(ID, (c.getURL()).getBytes());
        }
    }

    public void addEntry(String s) throws RocksDBException
    {
        // Get the latest pageID
        Integer lastID = 0;
        RocksIterator iter = document.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            lastID++;
        }

        byte[] ID = document.get(s.getBytes());
        
        if (ID == null) {
            lastID++;
            ID = (Integer.toString(lastID)).getBytes();
            // System.out.println("Added " + c.getURL() + " -> " + lastID);
            document.put(s.getBytes(), ID);
            invertedDocument.put(ID, s.getBytes());
        }
    }

    public void index_pages() throws RocksDBException
    {
        // Get the latest pageID
        Integer lastID = 0;
        RocksIterator iter = document.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            lastID++;
        }
        System.out.println("No. of pages indexed = " + lastID);

        for(int i=0; i<crawlers.size(); i++) {
            Crawler c = crawlers.get(i);
            byte[] ID = document.get(c.getURL().getBytes());

            if (ID == null) {
                lastID++;
                ID = (Integer.toString(lastID)).getBytes();
                // System.out.println("Added " + c.getURL() + " -> " + lastID);
                document.put((c.getURL()).getBytes(), ID);
                invertedDocument.put(ID, (c.getURL()).getBytes());
            }

        }
    }

    public void print_docIndex() throws RocksDBException
    {
        RocksIterator iter = document.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            System.out.println(new String(iter.key()) + " -> " + new String(iter.value()));
        }
    }

    public void print_invertedDocIndex() throws RocksDBException
    {
        RocksIterator iter = invertedDocument.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            System.out.println(new String(iter.key()) + " -> " + new String(iter.value()));
        }
    }

    public void close_db() throws RocksDBException
    {
        this.invertedDocument.closeE();
        this.document.closeE();
    }
}