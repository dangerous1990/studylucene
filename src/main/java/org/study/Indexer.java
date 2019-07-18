package org.study;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

/**
 * create index
 */
public class Indexer {
    // 数据的目录
    private String dataDirPath;
    // 创建索引的目录
    private String indexDirPath;

    public Indexer(String dataDirPath, String indexDirPath) throws IOException {
        this.indexDirPath = indexDirPath;
        this.dataDirPath = dataDirPath;
    }

    /**
     * 创建索引
     *
     * @param filter
     * @return
     * @throws IOException
     */
    public int createIndex(FileFilter filter)
            throws IOException {
        Directory indexDirectory = FSDirectory.open(FileSystems.getDefault().getPath(indexDirPath));
        IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());

        File[] files = new File(dataDirPath).listFiles();
        int count = 0;
        try (IndexWriter writer = new IndexWriter(indexDirectory, config)) {
            for (File file : files) {
                if (!file.isDirectory()
                        && !file.isHidden()
                        && file.exists()
                        && file.canRead()
                        && filter.accept(file)
                ) {
                    count++;
                    Document document = new Document();
                    // read file
                    List<String> lines = new ArrayList<>();
                    StringBuffer buffer = new StringBuffer();
                    BufferedReader bf = new BufferedReader(new FileReader(file));
                    String s = null;
                    while ((s = bf.readLine()) != null) {
                        String temp = s.trim();
                        if (!temp.equals("")) {
                            lines.add(temp);
                            buffer.append(temp);
                        }
                    }
                    bf.close();
                    String xml = buffer.toString();
                    // filed type
                    FieldType fieldType = new FieldType();
                    fieldType.setStored(true);
                    fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
                    //index file contents
                    Field contentField = new Field(Constants.CONTENTS, xml, fieldType);
                    Field fileNameField = new Field(Constants.FILE_NAME, file.getName(), fieldType);
                    Field filePathField = new Field(Constants.FILE_PATH, file.getCanonicalPath(), fieldType);
                    Field questionField = new Field(Constants.QUESTION, lines.get(0), fieldType);
                    Field answerField = new Field(Constants.ANSWER, lines.get(1), fieldType);

                    document.add(contentField);
                    document.add(fileNameField);
                    document.add(filePathField);
                    document.add(questionField);
                    document.add(answerField);
                    writer.addDocument(document);
                }
            }
        }
        return count;
    }

    public static void main(String[] args) {
        try {
            final String indexDir = "/Users/limeng/code/lucene/index";
            final String dataDir = "/Users/limeng/code/lucene/data";
            Indexer indexer = new Indexer(dataDir, indexDir);
            long startTime = System.currentTimeMillis();
            int numIndexed = indexer.createIndex(new TextFileFilter());
            long endTime = System.currentTimeMillis();
            System.out.println(numIndexed + " File indexed, time taken: " + (endTime - startTime) + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

