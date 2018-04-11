import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSearch {

    private String fileNameToSearch;
    private List<String> result = new ArrayList<String>();

    public String getFileNameToSearch() {
        return fileNameToSearch;
    }

    public void setFileNameToSearch(String fileNameToSearch) {
        this.fileNameToSearch = fileNameToSearch;
    }

    public void resetResult() {
        result = new ArrayList<String>();
    }
    public List<String> getResult() {
        return result;
    }
    public void printResults() {
        System.out.println("Found " +this.getResult().size() + "C++ files");
    }
    public static void main(String[] args) {

        FileSearch fileSearch = new FileSearch();

        //try different directory and filename :)
        fileSearch.searchDirectory(new File("src"), ".cpp");
        fileSearch.searchDirectory(new File("src"), ".hpp");

        int count = fileSearch.getResult().size();
        if(count ==0){
            System.out.println("\nNo result found!");
        }else{
            System.out.println("\nFound " + count + " result!\n");
            for (String matched : fileSearch.getResult()){
                System.out.println("Found : " + matched);
            }
        }
    }

    public void searchDirectory(File directory, String fileNameToSearch) {

        setFileNameToSearch(fileNameToSearch);

        if (directory.isDirectory()) {
            search(directory);
        } else {
            System.out.println(directory.getAbsoluteFile() + " is not a directory!");
        }

    }

    private void search(File file) {
        if (file.isDirectory()) {
            //do you have permission to read this directory?	
            if (file.canRead()) {
                for (File temp : file.listFiles()) {
                    if (temp.isDirectory()) {
                        search(temp);
                    } else {
                        String suffix = getFileNameToSearch().substring(getFileNameToSearch().lastIndexOf("."), getFileNameToSearch().length());
                        String tempFileName = temp.getName();
                        String tempSuffix = "";
                        if (tempFileName.contains(".")) {
                            tempSuffix = tempFileName.substring(tempFileName.lastIndexOf("."), tempFileName.length());
                        }
                        if (suffix.equals(tempSuffix)) {
                            result.add(temp.getAbsoluteFile().toString());
                        }

                    }
                }

            } else {
                System.out.println(file.getAbsoluteFile() + "Permission Denied");
            }
        }

    }

}