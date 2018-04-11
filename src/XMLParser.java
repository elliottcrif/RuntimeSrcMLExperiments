import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by elliottcrifasi on 9/20/17.
 * A class to parse an XML file of a C program and output the number of unsafe function
 * calls
 */
public class XMLParser {
    public static void main(String[] args) {
        try {

            // parse the file into the doc
            Document doc = getXMLDoc("bitcoin-0.14.xml");

            // scan the file for safe functions
            scanForFunctions(doc, "safe.txt");

            // scan the file for unsafe functions
            scanForFunctions(doc, "unsafe.txt");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Scans the methods specified in the file and adds them to a Hashmap for
     * O(1) lookups
     *
     * @return The map of unsafe functions
     */
    public static HashMap<String, Integer> getMethodMap(String filename) {
        HashMap<String, Integer> unsafeCommandsMap = new HashMap<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNext()) {
                String methodName = scanner.next();
                unsafeCommandsMap.put(methodName, 0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return unsafeCommandsMap;
    }

    /**
     * Grabs an xml doc and makes it available to parse easily utilizing Document
     * Builder
     *
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Document getXMLDoc(String filename) throws ParserConfigurationException, IOException, SAXException {

        File xmlFile = new File(filename);

        // create instance of the document builder factory to get the builder
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        // get the document builder from the factory
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // parse the file into the doc
        Document doc = dBuilder.parse(xmlFile);

        // normalize doc
        doc.getDocumentElement().normalize();

        return doc;
    }

    /**
     * Checks the file against the methdd map and
     * counts the occurrences of the unsafe functions
     *
     * @param doc
     */
    public static HashMap<String, Integer> scanForFunctions(Document doc, String filename) {

        long startTime = System.currentTimeMillis();
        // grab all function nodes
        NodeList list = doc.getElementsByTagName("call");

        // get map of all the unsafeCommands to check against
        HashMap<String, Integer> unsafeCommandsMap = getMethodMap(filename);

        // loop through functions and find the name of the function
        for (int i = 0; i < list.getLength(); i++) {

            // Grab all the children of the current function call
            NodeList childrenNodesCall = list.item(i).getChildNodes();

            // loop through the children trying to grab their names
            for (int k = 0; k < childrenNodesCall.getLength(); k++) {

                // temp for the current child node
                Node currNode = childrenNodesCall.item(k);

                // temp for the current child node tag name
                String currNodeName = currNode.getNodeName();

                // temp for the current child node text content
                String currMethodName = currNode.getTextContent();
                // if the current node is the name tag and the function is unsafe put it into the map
                if (currNodeName.equals("name") && unsafeCommandsMap.containsKey(currMethodName)/* && !wrappedWithIf.contains(currNode)*/) {

                    int currValue = unsafeCommandsMap.get(currMethodName);
                    unsafeCommandsMap.put(currMethodName, currValue + 1);
                }
            }

        }
        return unsafeCommandsMap;
    }
}
