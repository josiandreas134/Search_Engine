# COMP 4321

Main program is in **SearchEngine.java**

The whole project is under package called `se`

Make sure to download the Rocks DB library below and put them under `lib/`
- [rocksdbjni-6.9.0-linux64.jar](https://hkustconnect-my.sharepoint.com/:u:/g/personal/raa_connect_ust_hk/EcRwjawFnE5BsU2P0FTtGtIB9OB_mRzh73n697i-Ztx6dA?e=QcxtUZ)
- [librocksdbjni-linux64.so](https://hkustconnect-my.sharepoint.com/:u:/g/personal/raa_connect_ust_hk/EVX7acboBvVOvWItvlHVQtQBkgPLWs4qVCIAaV_5Nzx94A?e=I70Q3K)

*P.S. : Might need different version if  used in the UST VM/different OS*

## Follow this step to compile and execute the code:

**_Compiling_**
1. use following command to compile **SearchEngine.java**
```bash
javac -cp lib/rocksdbjni-6.9.0-linux64.jar:./ -d ./ SearchEngine.java
```

**_Executing_**

2. Before executing the code, create a folder named _db_ to store rocksdb data.
```bash
mkdir db
```

3. Use the following command to execute **SearchEngine.class**
```bash
java -cp lib/rocksdbjni-6.9.0-linux64.jar:lib/htmlparser.jar:. se.SearchEngine
```
**_Output File_**

4. The output of the code will be placed in a _.txt_ file called **spider_result.txt**
