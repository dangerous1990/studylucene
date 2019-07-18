package org.study;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过索引字段来读取文档
 */
public class SearchTest {

    public static List<String> search(String indexDir, String query, String searchField, String resultFiled, int num) throws Exception {
        // 通过dir得到的路径下的所有的文件
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
        // 建立索引查询器
        IndexSearcher searcher = new IndexSearcher(reader);
        // 设置为TF/IDF 排序
        searcher.setSimilarity(new ClassicSimilarity());
        // 计算索引开始时间
        long start = System.currentTimeMillis();
        // 开始查询
        /**
         * 第一个参数是通过传过来的参数来查找得到的query；
         * 第二个参数是要出查询的行数
         * */
        Query q = new QueryParser(searchField, new SmartChineseAnalyzer()).parse(query);
        TopDocs hits = searcher.search(q, num);
        // 计算索引结束时间
        long end = System.currentTimeMillis();
        System.out.println("匹配 " + query + " ，总共花费" + (end - start) + "毫秒" + "查询到" + hits.totalHits + "个记录");
        List<String> result = new ArrayList<>();
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            result.add(doc.get(resultFiled));
        }
        // 关闭reader
        reader.close();
        return result;
    }

    public static String[] spellCheck(String queryWord) {
        //新索引目录
        String spellIndexPath = "/Users/limeng/code/lucene/spell";
        //已有索引目录
        String oriIndexPath = "/Users/limeng/code/lucene/index";
        // 字典目录
        String dicPath = "/Users/limeng/code/lucene/dict/spell.dic";

        //拼写检查
        try {
            //目录
            Directory directory = FSDirectory.open((new File(spellIndexPath)).toPath());
            SpellChecker spellChecker = new SpellChecker(directory);
            //以下几步用来初始化索引
            IndexReader reader = DirectoryReader.open(FSDirectory.open((new File(oriIndexPath)).toPath()));
            //利用已有索引
            Dictionary dictionary = new PlainTextDictionary(new File(dicPath).toPath());

            IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());
            spellChecker.indexDictionary(dictionary, config, true);

            String[] suggestions = spellChecker.suggestSimilar(queryWord, 5);
            System.out.println("拼写检查：");
            for (String suggestion : suggestions) {
                System.out.println("    " + suggestion);
            }
            System.out.println();
            reader.close();
            spellChecker.close();
            directory.close();
            return suggestions;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //测试
    public static void main(String[] args) throws Exception {
        String indexDir = "/Users/limeng/code/lucene/index";
        // 处理输入
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String str = null;
        System.out.println("请输入你要搜索的关键词:");
        try {
            str = br.readLine();
            System.out.println();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        // 拼写检查
        spellCheck(str);
        String temp = str;
        // 匹配问题
        List<String> suggestions = search(indexDir, str, Constants.QUESTION, Constants.QUESTION, 5);
        if (suggestions != null && suggestions.size() != 0) {
            System.out.println("你可能想输入的是:");
            for (int i = 0; i < suggestions.size(); i++) {
                System.out.println((i + 1) + " : " + suggestions.get(i));
            }

            System.out.println("请选择上面的一个正确的关键词(输入 1 ~ 5)，或继续原词(输入0)进行搜索:");
            str = br.readLine();
            System.out.println();
            if (!str.equals("0")) {
                str = suggestions.get(str.charAt(0) - '1');
            } else {
                str = temp;
            }
        }
        // 查询答案
        try {
            suggestions = search(indexDir, str, Constants.QUESTION, Constants.ANSWER, 1);
            for (int i = 0; i < suggestions.size(); i++) {
                System.out.println((i + 1) + " : " + suggestions.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}