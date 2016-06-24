package ua.fpexport;

import org.apache.log4j.Logger;
import ua.fpexport.model.Document;
import ua.fpexport.repository.FTPUploader;
import ua.fpexport.repository.SQLWorker;
import ua.fpexport.repository.XMLCreator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by al on 18.06.2016.
 */
public class FPApp2 {
    public static ArrayList<Document> documents = new ArrayList<>();

    final static Logger logger = Logger.getLogger(FPApp2.class);
    public static void main(String[] args) throws Exception {
        logger.info("FPApp2 started............................");
        try {
            SQLWorker sqlWorker = new SQLWorker();
            sqlWorker.work();

            XMLCreator xmlCreator = new XMLCreator(documents);
            String xmlFileName = null;
            xmlFileName = xmlCreator.createXmlFromDocuments();

            FTPUploader ftpUploader = new FTPUploader(xmlFileName);
            ftpUploader.upload();

            sqlWorker.updateDocumentsStatuses(documents);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e);
        }
        logger.info("FPApp2 ended..............................");
    }


}
