import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ReadCustomXML {
    public static void main(String[] args) {
        Map<String, String> properties = new HashMap<>();

        try {
            File file = new File("config.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("entry");
            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String key = element.getAttribute("key");
                    String value = element.getTextContent();
                    properties.put(key, value);
                }
            }

            // Print out the properties
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
