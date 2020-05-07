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

public class ParentChildIndexer
{
    protected RocksDB db;
    protected RocksDB dbinverted;
    protected RocksDB document;
    protected RocksDB invertedDocument;
    protected Options options;
    protected Vector<Crawler> crawlers;

    public ParentChildIndexer(Vector<Crawler> crawlers, RocksDB document, RocksDB invertedDocument, RocksDB ParentChildIndex, RocksDB ParentChildIndexInverted) throws RocksDBException
    {
        this.crawlers = crawlers;
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.db = ParentChildIndex;
        this.dbinverted = ParentChildIndexInverted;
        // open document to get pageID
        this.document = document;
        this.invertedDocument = invertedDocument;
    }

    public ParentChildIndexer(RocksDB document, RocksDB invertedDocument, RocksDB ParentChildIndex, RocksDB ParentChildIndexInverted) throws RocksDBException
    {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.db = ParentChildIndex;
        this.dbinverted = ParentChildIndexInverted;
        // open document to get pageID
        this.document = document;
        this.invertedDocument = invertedDocument;
    }

    public void addEntry(Crawler c) throws RocksDBException
    {
        Vector parsed = c.parser();
        Vector<String> links = (Vector<String>) parsed.get(4);

        byte[] pageID = document.get((c.getURL()).getBytes());
        // If pageID is not available
        if(pageID == null) {
            DocumentIndexer documentIndexer = new DocumentIndexer(document, invertedDocument);
            documentIndexer.addEntry(c);
            pageID = document.get((c.getURL()).getBytes());
        }

        String asu = " ";
        /*if (links.size() > 0){
            byte[] pageIDChild = document.get(links.get(0).getBytes());
            if(pageIDChild == null) {
                DocumentIndexer documentIndexer = new DocumentIndexer(document, invertedDocument);
                documentIndexer.addEntry(links.get(0));
                pageIDChild = document.get((links.get(0)).getBytes());
            }

            for(int i=0; i<links.size(); i++) {
                System.out.println(i);
                System.out.println(links.get(i));
            }
            
        }
        */
        if (links.size() > 0){
            for(int i=0; i<links.size(); i++) {
                byte[] pageIDChild = document.get((links.get(i)).getBytes());
                if(pageIDChild == null) {
                    DocumentIndexer documentIndexer = new DocumentIndexer(document, invertedDocument);
                    documentIndexer.addEntry(links.get(i));
                    pageIDChild = document.get((links.get(i)).getBytes());

                    if(dbinverted.get(pageIDChild) == null){
                        dbinverted.put(pageIDChild,pageID);
                    }
                    else{
                        byte[] temp = dbinverted.get(pageIDChild);
                        String existedString = new String(temp);
                        String pageIDString = new String(pageID);
                        String put = existedString +"||"+ pageIDString;
                        dbinverted.put(pageIDChild,put.getBytes());
                    }

                    String string = new String(pageIDChild);
                    asu += string + "||";
                    string = null;
                } 
                else{
                    String string3 = new String(pageIDChild);
                    asu += string3 + "||";
                    string3 = null;

                    if(dbinverted.get(pageIDChild) == null){
                        dbinverted.put(pageIDChild,pageID);
                    }
                    else{
                        byte[] temp = dbinverted.get(pageIDChild);
                        String existedString = new String(temp);
                        String pageIDString = new String(pageID);
                        String put = existedString +"||"+ pageIDString;
                        dbinverted.put(pageIDChild,put.getBytes());
                    }

                }               
            }
            db.put(pageID, asu.getBytes());
        }
    }

    public void printParentChild() throws RocksDBException
    {
        // Print all the data in the hashtable
        // ADD YOUR CODES HERE
        RocksIterator iter = db.newIterator();                
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println(new String(iter.key()) + " = \n" + new String(iter.value()));
        }

    }    

    public void PrintChildParent() throws RocksDBException{
            // Print all the data in the hashtable
            // ADD YOUR CODES HERE
            RocksIterator iter = dbinverted.newIterator();                
            for(iter.seekToFirst(); iter.isValid(); iter.next()) {
                System.out.println(new String(iter.key()) + " = \n" + new String(iter.value()));
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
        this.db.close();
        this.document.close();
    }
}