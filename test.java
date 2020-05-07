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
import se.ParentChildIndexer;

public class test {
    public static void main(String[] args) {
        try{
            RocksDB.loadLibrary();
            Options options = new Options();
            options.setCreateIfMissing(true);
            RocksDB document = RocksDB.open(options, "db/document");
            RocksDB invertedDocument = RocksDB.open(options, "db/invertedDocument");
            RocksDB ParentChildIndex = RocksDB.open(options, "db/ParentChildIndex");
            RocksDB ParentChildIndexInverted = RocksDB.open(options, "db/ParentChildIndexInverted");
            
            // Crawl
            Crawler crawler = new Crawler("https://www.cse.ust.hk/");
            Vector<Crawler> crawlers = crawler.crawlers(30);

            DocumentIndexer documentIndexer = new DocumentIndexer(crawlers, document, invertedDocument);
            documentIndexer.index_pages();

            ParentChildIndexer parentChildIndexer = new ParentChildIndexer(crawlers, document, invertedDocument, ParentChildIndex, ParentChildIndexInverted );
            parentChildIndexer.index_pages();
            parentChildIndexer.PrintChildParent();

            // Output for PHASE I
            
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