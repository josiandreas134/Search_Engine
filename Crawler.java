package se;

import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.FilterBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.filters.StringFilter;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.net.URL;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Options;

public class Crawler
{
    private String url;

    Crawler(String _url) {
            url = _url;
    }

    public String getURL() {
        return this.url;
    }

    public Vector<String> extractTitle() throws ParserException {
        // extract title in url and return it
        Vector<String> result = new Vector<String>();
        FilterBean bean = new FilterBean();

        TagNameFilter title = new TagNameFilter("title");
        TagNameFilter[] filter = {title};

        bean.setURL(url);
        bean.setFilters(filter);
        
	if (bean.getText() != null && bean.getText() != "")
		result.add(bean.getText());
	else
		result.add("No Title");

        return result; 
    }

    public Vector<String> extractLastModDate() throws ParserException, MalformedURLException, IOException {
        // extract last modification date in url and return it
        Vector<String> result = new Vector<String>();
        FilterBean bean = new FilterBean();

        // if the website is under cse domain and has the footer
        StringFilter date = new StringFilter("Last updated on ");
        StringFilter[] filter = {date};

        bean.setURL(url);
        bean.setFilters(filter);
        if (bean.getText() != "") {
            result.add(bean.getText().substring(16, 26));
            return result;
        }

        // when the link is out of cse doamin
        HttpURLConnection content = (HttpURLConnection) new URL(url.replaceFirst("s", "")).openConnection();

        if (content.getLastModified() != 0)
            result.add(String.valueOf(content.getLastModified()));
        else {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date today = new Date();
            result.add(format.format(today));
        }

        return result; 
    }

    public Vector<String> extractSize() throws MalformedURLException, IOException {
        // extract the size of the page and return it
        Vector<String> result = new Vector<String>();
        HttpURLConnection content = (HttpURLConnection) new URL(url.replaceFirst("s", "")).openConnection();

        result.add(String.valueOf(content.getContentLength()));

        return result;
    }

    public Vector<String> extractWords() throws ParserException {
        // extract words in url and return them
        // use StringTokenizer to tokenize the result from StringBean
        Vector<String> result = new Vector<String>();
        StringBean bean = new StringBean();

        bean.setURL(url);
        bean.setLinks(false);
        String contents = bean.getStrings();
        StringTokenizer st = new StringTokenizer(contents);
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }

        return result;
    }

    public Vector<String> extractLinks() throws ParserException {
        // extract links in url and return them
        Vector<String> result = new Vector<String>();
        LinkBean bean = new LinkBean();

        bean.setURL(url);
        Set<URL> urls = new HashSet<URL>();
        urls.addAll(Arrays.asList(bean.getLinks()));
	for (URL s : urls) {
            result.add(s.toString());
        }

        return result;
    }

    public Vector parser() {
        // output the result of the parser
        Vector result = new Vector();

        try {
            // write to txt file
            // BufferedWriter writer = new BufferedWriter(new FileWriter("parser_output.txt"));

            // output title
            Vector<String> title = this.extractTitle();
            // System.out.println("Title of " + this.url + ":");
            // System.out.println(title.get(0));
            // System.out.println("");
            // writer.write(title.get(0));
            // writer.write(", ");
            result.addElement(title);

            // output last modification date
            Vector<String> lastModDate = this.extractLastModDate();
            // System.out.println("Last modification date of " + this.url + ":");
            // System.out.println(lastModDate.get(0));
            // System.out.println("");
            // writer.write(lastModDate.get(0));
            // writer.write(", ");
            result.addElement(lastModDate);

            // output size
            Vector<String> size = this.extractSize();
            // System.out.println("The size of " + this.url + ":");
            // System.out.println(size.get(0));
            // System.out.println("");
            // writer.write(size.get(0));
            // writer.write(", ");
            result.addElement(size);

            // output content
            Vector<String> words = this.extractWords();
            // System.out.println("Words in " + this.url + ":");
            // for (int i = 0; i < words.size(); i++) {
            //     // System.out.print(words.get(i) + " ");
            //     writer.write(words.get(i) + " ");
            // }
            // System.out.println("\n\n");
            result.addElement(words);

            // output links
            Vector<String> links = this.extractLinks();
            // System.out.println("Links in " + this.url + ":");
            // for (int i = 0; i < links.size(); i++) {
            //     // System.out.println(links.get(i));
            //     writer.write(links.get(i) + ", ");
            // }
            // System.out.println("");
            result.addElement(links);
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public Vector<Crawler> crawlers(int num, RocksDB document) throws RocksDBException {
	
	num -= 1;

        // loop in the child links 
        Vector<Crawler> crawlers = new Vector<Crawler>();
        crawlers.add(this);
        for (int i = 0, k = 0; i < num; i++, k++) {
            Vector<String> links_k = (Vector<String>) crawlers.get(k).parser().get(4);
            for (int j = 0; j < links_k.size() && i < num; j++, i++) {
                if (document.get(links_k.get(j).getBytes()) != null && links_k.get(j).contains("cse.ust.hk")) {
                    String data = new String(document.get(links_k.get(j).getBytes()));
                    Crawler temp = new Crawler(links_k.get(j));
                    Vector<String> date = (Vector<String>) temp.parser().get(1);
		    if (data.substring(data.indexOf(";;;", 0), 10) != date.get(0))
                        crawlers.add(temp);
                    else 
                        i--;
                }
                else 
                    crawlers.add(new Crawler(links_k.get(j)));
            }
        }

        return crawlers;
    }

    public Vector<Crawler> crawl(int limit, RocksDB document) throws RocksDBException  {
        Vector<Crawler> crawlers = new Vector<Crawler>();
        crawlers.add(new Crawler(this.url));
        for(int i=0; i<crawlers.size() && i<limit-1;) {
            Vector result = crawlers.get(i).parser();
            Vector<String> links = (Vector<String>) result.get(4);
            // System.out.println(links.get(i));

<<<<<<< HEAD
            for (int j = 0; j<links.size() || i<limit-1; j++, i++) {
                if(links.get(i).contains("cse.ust.hk")){
                    crawlers.add(new Crawler(links.get(i)));
                }
=======
            for (int j = 0; j<links.size() && i<limit-1; j++) {
                if(links.get(j).contains("cse.ust.hk") && document.get((links.get(j)).getBytes()) == null){
                    // System.out.println("include : "+links.get(j));
                    crawlers.add(new Crawler(links.get(j)));
                    i++;
                }
                // else {
                //     System.out.println("Do not include : "+links.get(j));
                // }
>>>>>>> a158753692f8fa1661f1647965d59d797514141e
            }
        }
        return crawlers;
    }

    // public static void main(String[] args)
    // {
    //     Crawler crawler = new Crawler("https://www.cse.ust.hk/");
    //     Vector<Crawler> crawlers = crawler.crawlers(30);

    //     for (int i = 0; i < 30; i++) {
    //         // test the retrival method
    //         Vector result = crawlers.get(i).parser();
    //         // output title
    //         Vector<String> title = (Vector<String>) result.get(0);
    //         System.out.println("Title of " + crawlers.get(i).url + ":");
    //         System.out.println(title.get(0));
    //         System.out.println("");

    //         // output last modification date
    //         Vector<String> lastModDate = (Vector<String>) result.get(1);
    //         System.out.println("Last modification date of " + crawlers.get(i).url + ":");
    //         System.out.println(lastModDate.get(0));
    //         System.out.println("");

    //         // output size
    //         Vector<String> size = (Vector<String>) result.get(2);
    //         System.out.println("The size of " + crawlers.get(i).url + ":");
    //         System.out.println(size.get(0));
    //         System.out.println("");

    //         // output content
    //         Vector<String> words = (Vector<String>) result.get(3);
    //         System.out.println("Words in " + crawlers.get(i).url + ":");
    //         for (int j = 0; j < words.size(); j++) {
    //             System.out.print(words.get(j) + " ");
    //         }
    //         System.out.println("\n\n");

    //         // output links
    //         Vector<String> links = (Vector<String>) result.get(4);
    //         System.out.println("Links in " + crawlers.get(i).url + ":");
    //         for (int j = 0; j < links.size(); j++) {
    //             System.out.println(links.get(j));
    //         }
    //         System.out.println("-------------------------------");
    //     }

    //     // // test the retrival method
    //     // Vector result = crawlers.get(20).parser();
    //     // // output title
    //     // Vector<String> title = (Vector<String>) result.get(0);
    //     // System.out.println("Title of " + crawlers.get(20).url + ":");
    //     // System.out.println(title.get(0));
    //     // System.out.println("");

    //     // // output last modification date
    //     // Vector<String> lastModDate = (Vector<String>) result.get(1);
    //     // System.out.println("Last modification date of " + crawlers.get(20).url + ":");
    //     // System.out.println(lastModDate.get(0));
    //     // System.out.println("");

    //     // // output size
    //     // Vector<String> size = (Vector<String>) result.get(2);
    //     // System.out.println("The size of " + crawlers.get(20).url + ":");
    //     // System.out.println(size.get(0));
    //     // System.out.println("");

    //     // // output content
    //     // Vector<String> words = (Vector<String>) result.get(3);
    //     // System.out.println("Words in " + crawlers.get(20).url + ":");
    //     // for (int i = 0; i < words.size(); i++) {
    //     //     System.out.print(words.get(i) + " ");
    //     // }
    //     // System.out.println("\n\n");

    //     // // output links
    //     // Vector<String> links = (Vector<String>) result.get(4);
    //     // System.out.println("Links in " + crawlers.get(20).url + ":");
    //     // for (int i = 0; i < links.size(); i++) {
    //     //     System.out.println(links.get(i));
    //     // }
    //     // System.out.println("-------------------------------");
    // }
}

