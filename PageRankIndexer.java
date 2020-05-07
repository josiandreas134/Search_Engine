package se;

import java.util.Vector;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;

public class PageRankIndexer {
    private RocksDB invertedDocument;
    private RocksDB PageRankIndex;
    private RocksDB ParentChildIndex;
    private RocksDB ParentChildIndexInverted;
    private Options options;


    PageRankIndexer(RocksDB PageRankIndex, RocksDB invertedDocument, RocksDB ParentChildIndex, RocksDB ParentChildIndexInverted) throws RocksDBException
    {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.PageRankIndex = PageRankIndex;
        this.invertedDocument = invertedDocument;
        this.ParentChildIndex = ParentChildIndex;
        this.ParentChildIndexInverted = ParentChildIndexInverted;
    }

    public void Initialize() throws RocksDBException{
        RocksIterator iter = invertedDocument.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
           PageRankIndex.put((iter.key()),"1".getBytes());
        }
    }

    public Double computePageRank(String ParentList) throws RocksDBException
    {
        String rankS = new String(PageRankIndex.get(ParentList.getBytes()));
        Double rankD = Double.valueOf(rankS);
        String[] Child = (new String(ParentChildIndex.get(ParentList.getBytes()))).split(";;");
        rankD = rankD / Child.length;
        return rankD;
    }

    public void index_ranks() throws RocksDBException
    {
        Boolean Converge = false;
        int total = 0;
        int convergenum = 0;
        RocksIterator iter2 = ParentChildIndexInverted.newIterator();
        for(iter2.seekToFirst(); iter2.isValid(); iter2.next()){
            ++total;
        }
        do{
            RocksIterator iter = ParentChildIndexInverted.newIterator();
            for(iter.seekToFirst(); iter.isValid(); iter.next()){
                Double rank=0.0;
                String[] ParentList = ((new String(iter.value())).split(";;"));
                for(int i=0;i<ParentList.length ;i++){
                    rank += computePageRank(ParentList[i]);
            }
            rank = (0.15) + (0.85*rank);
            String rankS = new String(PageRankIndex.get(iter.key()));
            Double rankD = Double.valueOf(rankS);
            if(rank.equals(rankD)){
                ++convergenum;
                if(convergenum == total){
                    Converge = true;
                }
            }
            PageRankIndex.put(iter.key(), (Double.toString(rank)).getBytes()); 
            }
        } while(Converge == false);
    }


    public void print_pageRankIndex() throws RocksDBException
    {
        RocksIterator iter = PageRankIndex.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            System.out.println(new String(iter.key()) + " -> " + new String(iter.value()));
        }
    }


    public void close_db() throws RocksDBException
    {
        this.invertedDocument.closeE();
        this.PageRankIndex.closeE();
    }
}