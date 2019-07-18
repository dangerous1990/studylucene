package org.study;

import java.io.*;

public class PrepareData {

    public static void main(String[] args) {
        segFile();
    }

    public static void segFile(){
        String dir = "/Users/limeng/code/lucene/data/qa.txt";
        String out = "/Users/limeng/code/lucene/data";
        try ( BufferedReader br = new BufferedReader(new FileReader(dir))){
            int count = 0;
            int fileCount = 0;
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                count++;
                sb.append(line);
                sb.append("\n");
                if (count % 2 == 0) {
                    File file = new File(out + "/" + fileCount++ + ".txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                        bw.write(sb.toString());
                        bw.flush();
                        sb.delete(0, sb.length() - 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
