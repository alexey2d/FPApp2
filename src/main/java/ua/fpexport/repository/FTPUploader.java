package ua.fpexport.repository;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import ua.fpexport.model.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;

/**
 * Created by al on 23.06.2016.
 */
public class FTPUploader {
    final static Logger logger = Logger.getLogger(FTPUploader.class);

    private String ftpUrl;
    private String ftpFolder;
    private String ftpUser;
    private String ftpPassword;

    private String filename;


    public FTPUploader() {
        ftpUrl = Properties.getFtpUrl();
        ftpFolder = Properties.getFtpFolder();
        ftpUser = Properties.getFtpUser();
        ftpPassword = Properties.getFtpPassword();

        logger.debug("ftpUrl: " + ftpUrl);
        logger.debug("ftpFolder: " + ftpFolder);
        logger.debug("ftpUser: " + ftpUser);
        logger.debug("ftpPassword: " + ftpPassword);
    }

    public FTPUploader(String filename) {
        this();
        this.filename = filename;
    }

    public void upload() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding("windows-1251");

        ftpClient.connect(ftpUrl, FTP.DEFAULT_PORT);
        ftpClient.login(ftpUser, ftpPassword);
        ftpClient.enterLocalPassiveMode();

        ftpClient.setFileType(BINARY_FILE_TYPE);

        File localFilePath = new File(filename);
        InputStream inputStream = new FileInputStream(localFilePath);
        String localFilename = filename.substring(filename.lastIndexOf('\\') + 1);
        String remoteFile = ftpFolder + "/" + localFilename;

        logger.info("Start uploading " + localFilename);
        boolean done = ftpClient.storeFile(remoteFile, inputStream);
        inputStream.close();
        if (done) {
            logger.info("The file is uploaded successfully.");
        } else {
            IOException ioEx = new IOException("FTP upload failed.");
            logger.error("FTP", ioEx);
            throw ioEx;
        }

        if (ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }

    } // end upload()

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
