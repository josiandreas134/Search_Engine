Notes:
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
*Main program is in SearchEngine.java

*The whole project is under package called se

*Make sure to download the Rocks DB library below and put them under lib/
- [rocksdbjni-6.9.0-linux64.jar] = https://hkustconnect-my.sharepoint.com/:u:/g/personal/raa_connect_ust_hk/EcRwjawFnE5BsU2P0FTtGtIB9OB_mRzh73n697i-Ztx6dA?e=QcxtUZ
- [librocksdbjni-linux64.so] = https://hkustconnect-my.sharepoint.com/:u:/g/personal/raa_connect_ust_hk/EVX7acboBvVOvWItvlHVQtQBkgPLWs4qVCIAaV_5Nzx94A?e=I70Q3K

*Make sure to download all the dependecies needed to compile and run SearchEngine.java and put the folder under the current directory
- [se] = https://hkustconnect-my.sharepoint.com/:f:/g/personal/jagunawan_connect_ust_hk/EvzodlX3g61DjJaVo6dSUHAB_h_fo4gmgPGj5pVwvLeh9g?e=hrwIZO

P.S. : Might need different version if used in the UST VM/different OS

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

Follow this step to compile and execute the code:

1. Create a directory and put the Crawler.java, SearchEngine.java, and stopwords.txt in that directory. (For an example we create a dir called ./SearchEng)

2. Download all the required files as mentioned above (RocksDB library and dependencies)
	RocksDB:	https://hkustconnect-my.sharepoint.com/:u:/g/personal/raa_connect_ust_hk/EcRwjawFnE5BsU2P0FTtGtIB9OB_mRzh73n697i-Ztx6dA?e=QcxtUZ
			https://hkustconnect-my.sharepoint.com/:u:/g/personal/raa_connect_ust_hk/EVX7acboBvVOvWItvlHVQtQBkgPLWs4qVCIAaV_5Nzx94A?e=I70Q3K
	Dependecies: 	https://hkustconnect-my.sharepoint.com/:f:/g/personal/jagunawan_connect_ust_hk/EvzodlX3g61DjJaVo6dSUHAB_h_fo4gmgPGj5pVwvLeh9g?e=hrwIZO

3. On the current directory, create a folder called 'lib' and put the RocksDB library files there. (Put it under /SearchEng/lib)
	
4. Still on the current directory (/SearchEng), put the downloaded dependencies folder (called 'se') there.

5. Then, use following command to compile **SearchEngine.java**
	javac -cp lib/rocksdbjni-6.9.0-linux64.jar:./ -d ./ SearchEngine.java

6. Before executing the code, create a folder named `db` to store rocksdb data. 
	mkdir db

7. Use the following command to execute "SearchEngine.class"
	java -cp lib/rocksdbjni-6.9.0-linux64.jar:lib/htmlparser.jar:. se.SearchEngine

8. The output of the code will be placed in a _.txt_ file called "spider_result.txt"