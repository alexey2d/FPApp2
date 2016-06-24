package ua.fpexport.repository;


import ua.fpexport.model.Document;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import ua.fpexport.model.Product;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
 * Created by al on 23.06.2016.
*/

public class XMLCreator {
    final static Logger logger = Logger.getLogger(XMLCreator.class);
    private ArrayList<Document> documents;

    public XMLCreator(ArrayList<Document> documents) {
        this.documents = documents;
    }

    public String createXmlFromDocuments() throws ParserConfigurationException, TransformerException, FileNotFoundException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        documentBuilder = factory.newDocumentBuilder();

        org.w3c.dom.Document xmlDocument = documentBuilder.newDocument();

        Element supplyRoot = xmlDocument.createElement("supplies");
        xmlDocument.appendChild(supplyRoot);
        for (Document doc : documents) {
            // Header
            Element supplyElement = xmlDocument.createElement("supply");
            Element supplyId = xmlDocument.createElement("id");
            Element supplyDate = xmlDocument.createElement("date");
            Element supplyCustomerId = xmlDocument.createElement("customerId");
            Element supplyCustomerName = xmlDocument.createElement("customerName");
            Element supplyCurrency = xmlDocument.createElement("currency");
            Element supplyComment = xmlDocument.createElement("comment");

            // append to ...
            supplyRoot.appendChild(supplyElement);
            supplyElement.appendChild(supplyId);
            supplyElement.appendChild(supplyDate);
            supplyElement.appendChild(supplyCustomerId);
            supplyElement.appendChild(supplyCustomerName);
            supplyElement.appendChild(supplyCurrency);
            supplyElement.appendChild(supplyComment);

            // set values
            supplyId.appendChild(xmlDocument.createTextNode(String.valueOf(doc.getId())));
            supplyDate.appendChild(xmlDocument.createTextNode(doc.getDate()));
            supplyCustomerId.appendChild(xmlDocument.createTextNode("1"));
            supplyCustomerName.appendChild(xmlDocument.createTextNode(doc.getCustomerName()));
            supplyCurrency.appendChild(xmlDocument.createTextNode("0")); //CASH
            supplyComment.appendChild(xmlDocument.createTextNode(doc.getComment()));

            // Goods
            // Goods header
            Element supplyGoods = xmlDocument.createElement("products");
            supplyElement.appendChild(supplyGoods);
            // Goods content
            for (Product product : doc.getProducts()) {
                Element goodElement = xmlDocument.createElement("product");
                Element goodId = xmlDocument.createElement("id");
                Element goodName = xmlDocument.createElement("name");
                Element goodMeasure = xmlDocument.createElement("m");
                Element goodQuantity = xmlDocument.createElement("qty");

                supplyGoods.appendChild(goodElement);
                goodElement.appendChild(goodId);
                goodElement.appendChild(goodName);
                goodElement.appendChild(goodMeasure);
                goodElement.appendChild(goodQuantity);

                // set values
                goodId.appendChild(xmlDocument.createTextNode(String.valueOf(product.getId())));
                goodName.appendChild(xmlDocument.createTextNode(product.getName()));
                goodMeasure.appendChild(xmlDocument.createTextNode(product.getMeasure().getValue()));
                String qty = product.getQuantity().toString().replace('.', ',');
                goodQuantity.appendChild(xmlDocument.createTextNode(qty));
            } // end foreach goods
        } //end foreach documents

        TransformerFactory trFactory = TransformerFactory.newInstance();

        String filename = setLocalFileName("supplies.xml");
        filename = "out\\logs\\" + filename;
        Transformer transformer = trFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//        xmlDocument.setXmlStandalone(true);
        DOMSource domSource = new DOMSource(xmlDocument);
        StreamResult streamFile = new StreamResult(new FileOutputStream(filename));
        transformer.transform(domSource, streamFile);
        logger.info("File " + filename + " created");

        return filename;
    }

    private String setLocalFileName(String fileName) {
        // 1 - get file in name and extension
        int fileNameLength = fileName.length();
        String fileNameOnly = fileName.substring(0, fileNameLength - 4);
        String fileExtension = fileName.substring(fileNameLength - 4, fileNameLength);

        // 2 - get current date and time
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();

        // 3 - out fileNameOnly + date + fileExtension
        String localFileName = fileNameOnly + "_" + dateFormat.format(date) + fileExtension;

        return localFileName;
    }
}
