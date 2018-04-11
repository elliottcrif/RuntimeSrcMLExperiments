import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class CommandLine {

    public static void main(String[] args) {
        String reponame = downloadRepo();
        FileSearch fileSearch = new FileSearch();
        fileSearch.searchDirectory(new File(reponame), ".cpp");
        fileSearch.searchDirectory(new File(reponame), ".hpp");
        String outputFolder = runSrcML(fileSearch.getResult(), reponame);
        fileSearch.resetResult();
        fileSearch.searchDirectory(new File(outputFolder), ".xml");
        HashMap<String, Integer> totalSafeMap = new HashMap<>();
        HashMap<String, Integer> totalUnsafeMap = new HashMap<>();
        for (String result : fileSearch.getResult()) {
            // parse the file into the doc
            Document doc = null;
            try {
                doc = XMLParser.getXMLDoc(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (doc != null) {
                // scan the file for safe functions
                HashMap<String, Integer> safeMap=  XMLParser.scanForFunctions(doc, "safe.txt");
                for (Map.Entry<String, Integer> entry : safeMap.entrySet()) {
                    Integer numberOfCalls = totalSafeMap.get(entry.getKey());
                    if ( numberOfCalls != null) {
                        totalSafeMap.put(entry.getKey(), numberOfCalls+entry.getValue());
                    } else {
                        totalSafeMap.put(entry.getKey(), entry.getValue());
                    }
                }
                // scan the file for unsafe functions
                HashMap<String, Integer> unsafeMap=XMLParser.scanForFunctions(doc, "unsafe.txt");
                for (Map.Entry<String, Integer> entry : unsafeMap.entrySet()) {
                    Integer numberOfCalls = totalUnsafeMap.get(entry.getKey());
                    if ( numberOfCalls != null) {
                        totalUnsafeMap.put(entry.getKey(), numberOfCalls+entry.getValue());
                    } else {
                        totalUnsafeMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        totalSafeMap.values().removeIf(val -> 0 == val);
        totalUnsafeMap.values().removeIf(val -> 0 == val);
        System.out.println("Safe Functions");
        for (Map.Entry<String, Integer> entry : totalSafeMap.entrySet()) {
            System.out.println(entry.getKey()+": "+entry.getValue());
        }
        System.out.println("Unsafe Functions");
        for (Map.Entry<String, Integer> entry : totalUnsafeMap.entrySet()) {
            System.out.println(entry.getKey()+": "+entry.getValue());
        }
    }

    private static String downloadRepo() {
        String reponame ="";
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("What is the name of the repo: ");
                Runtime rt = Runtime.getRuntime();
                reponame = scanner.next();
                String repourl = "https://github.com/"+ reponame;
                Process pr = rt.exec("git clone "+repourl);
                System.out.println("Downloading repo...");
                pr.waitFor();
                System.out.println("DownloadFinished");
            } catch (Exception e) {
            e.printStackTrace();
        }
        reponame = reponame.substring(reponame.lastIndexOf("/")+1, reponame.length());
        reponame = reponame.substring(0,reponame.lastIndexOf("."));
        System.out.println(reponame);
        return reponame;
}

    private static String runSrcML(List<String> filesToConvert, String outputFolder) {
        String outputFolderName = "";
        try {
            outputFolderName = outputFolder+"Output";
            System.out.print("Converting " + filesToConvert.size() + "C++ files to XML");
            File output = new File(outputFolderName);
            // if the directory does not exist, create it
            if (!output.exists()) {
                try {
                    output.mkdirs();
                } catch (SecurityException se) {
                    //handle it
                }
            }
                for (String nameOfFile : filesToConvert) {
                String filename = nameOfFile.substring(nameOfFile.lastIndexOf("/"), nameOfFile.length());
                String outputfilename = filename.substring(0,filename.lastIndexOf("."));
                outputfilename = outputFolderName+ outputfilename + ".xml";
                String command = "srcml " + nameOfFile;
                Runtime rt = Runtime.getRuntime();
                Process pr = rt.exec(command);
                FileWriter fileWriter = new FileWriter(outputfilename);
                BufferedReader srcMLOutput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                BufferedWriter writer = new BufferedWriter(fileWriter);
                String outputLine = srcMLOutput.readLine();
                while (outputLine != null) {
                    writer.write(outputLine);
                    outputLine = srcMLOutput.readLine();
                }
                if (pr.waitFor() != 0) {
                    System.out.println("Error");

                }
                writer.close();
                srcMLOutput.close();
            }

        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return outputFolderName;
    }
}
