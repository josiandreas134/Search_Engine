package se;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.Scanner;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;

public class CosSim {
    private String query;
    private RocksDB word;
    private RocksDB titleInverted;
    private RocksDB contentInverted;
    private RocksDB forwardIndex;

    CosSim(String query, RocksDB word, RocksDB titleInverted, RocksDB contentInverted, RocksDB forwardIndex) 
    {
        this.query = query;
        this.word = word;
        this.titleInverted = titleInverted;
        this.contentInverted = contentInverted;
        this.forwardIndex = forwardIndex;
    }

    public HashMap<String, Double> getPages(double thresh) throws RocksDBException
    {
        // Get all wordIDs in Query
        HashMap<String, Double> cossim = new HashMap<String, Double>();
        HashMap<String, Integer> cossim_u = new HashMap<String, Integer>();
        HashMap<String, Integer> cossim_d = new HashMap<String, Integer>();

        Vector<String> q = new Vector<String>(Arrays.asList(query.split("\\s+")));
        q = StopWords.clean(q);
        boolean no_match = true;
        for(int i=0; i<q.size(); i++) {
            byte[] wID = word.get(q.get(i).getBytes());
            if(wID != null){
                no_match = false;
                q.set(i, new String(wID));
            }
            else {
                q.remove(i);
            }
        }
        if(no_match) {
            return null;
        }

        
        for(int i=0; i<q.size(); i++) {
            // HashMap<String, Integer> freq = new HashMap<String, Integer>();
            // List pages with title that contains word(s) from query
            
            String lastDoc = "";
            if(titleInverted.get((q.get(i)).getBytes()) != null) {
                String title_pagelist_str = new String(titleInverted.get((q.get(i)).getBytes()));
                Vector<String> title_pagelist = new Vector<String>(Arrays.asList(title_pagelist_str.split("\\s+")));
                lastDoc = "";
                for(int j=0; j<title_pagelist.size(); j++) {
                    if(title_pagelist.get(j).length() > 3 && title_pagelist.get(j).substring(0,3).equals("doc")) {
                        lastDoc = title_pagelist.get(j).substring(3);
                        cossim_u.putIfAbsent(lastDoc, 0);
                    }
                    else {
                        cossim_u.put(lastDoc, cossim_u.get(lastDoc)+2);
                    }
                }
            }
            
            // List pages with content that contains word(s) from query
            if(contentInverted.get((q.get(i)).getBytes()) != null) {
                String content_pagelist_str = new String(contentInverted.get((q.get(i)).getBytes()));
                Vector<String> content_pagelist = new Vector<String>(Arrays.asList(content_pagelist_str.split("\\s+")));
                lastDoc = "";
                for(int j=0; j<content_pagelist.size(); j++) {
                    // System.out.println(content_pagelist.get(j));
                    if(content_pagelist.get(j).length() > 3 && content_pagelist.get(j).substring(0,3).equals("doc")) {
                        lastDoc = content_pagelist. get(j).substring(3);
                        cossim_u.putIfAbsent(lastDoc, 0);
                    }
                    else {
                        cossim_u.put(lastDoc, cossim_u.get(lastDoc)+1);
                    }
                }
            }
        }
        for (String key : cossim_u.keySet()) {
            String fwd_str = new String(forwardIndex.get(key.getBytes()));
            Vector<String> fwd = new Vector<String>(Arrays.asList(fwd_str.split(" ;;; ")));
            int freq_sq = Integer.parseInt(fwd.get(5));
            cossim_d.putIfAbsent(key, freq_sq);
            double res = cossim_u.get(key) / ( Math.sqrt(freq_sq) * Math.sqrt(q.size()) );
            if(res>thresh) {
                cossim.put(key, res);
            }
        }
        return cossim;
    }

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

            System.out.println(new String(titleInverted.get("1".getBytes())));
            System.out.println(new String(titleInverted.get("2".getBytes())));
            System.out.println(new String(titleInverted.get("3".getBytes())));
            System.out.println(new String(contentInverted.get("1".getBytes())));
            System.out.println(new String(contentInverted.get("2".getBytes())));
            System.out.println(new String(contentInverted.get("3".getBytes())));
            System.out.println(new String(forwardIndex.get("8".getBytes())));
            // System.out.println(new String(word.get("HKUST".getBytes())));
            // System.out.println(new String(word.get("SCHOOL".getBytes())));

            String s;
            Scanner sc = new Scanner(System.in);
            System.out.println("Insert a string:");
            s = sc.nextLine();
            CosSim c= new CosSim( s, word, titleInverted, contentInverted, forwardIndex);
            System.out.println(c.getPages(0.2));
        }
        catch(RocksDBException e)
        {
            System.err.println(e.toString());
        }
    }
}