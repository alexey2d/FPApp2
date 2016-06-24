package ua.fpexport.repository;

import org.apache.log4j.Logger;
import ua.fpexport.FPApp2;
import ua.fpexport.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by al on 18.06.2016.
 */
public class SQLWorker {
    private static Connection connection = null;
    private String dbUrl;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    final static Logger logger = Logger.getLogger(SQLWorker.class);


    public SQLWorker() {
/*        dbUrl = System.getenv("dbUrl");
        dbName = System.getenv("dbName");
        dbUser = System.getenv("dbUser");
        dbPassword = System.getenv("dbPassword");*/
        dbUrl = Properties.getDbUrl();
        dbName = Properties.getDbName();
        dbUser = Properties.getDbUser();
        dbPassword = Properties.getDbPassword();

        logger.debug(dbUrl);
        logger.debug(dbName);
        logger.debug(dbUser);
        logger.debug(dbPassword);

    }

    public void work() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        // load driver
//         Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
//        DriverManager.registerDriver(new net.sourceforge.jtds.jdbc.Driver());
        // connecting
        connection = DriverManager.getConnection(dbUrl + dbName, dbUser, dbPassword);


        if (connection != null) logger.info("Connection to \"" + dbUrl + "\" successful");
        if (connection == null) {
            logger.error("Can't connect to " + dbUrl);
            System.exit(0);
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        logger.info("Auto commit: " + connection.getAutoCommit());

        String query = "SELECT db_name() AS CurrentDataBase";
        preparedStatement = connection.prepareStatement(query);
        resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            logger.info("Current database: " + resultSet.getString(1));
        }
        closeResultSet(resultSet);
        closePreparedStatement(preparedStatement);

        getDocuments(FPApp2.documents);
        fillDocuments(FPApp2.documents);

//        release in functions
//        close connection
//        if(resultSet != null) resultSet.close();
//        if(preparedStatement != null) preparedStatement.close();
//        if(connection != null) connection.close();
    } // end work()

    private void getDocuments(ArrayList<Document> documents) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String query = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED \n" +
                "SELECT ORDERSTRADE.ID " +
                ",ORDERSTRADE.FDATE " +
                ",ORDERSTRADE.TOID " +
                ",ORDERSTRADE.MONEYID " +
                ",ORDERSTRADE.STATE " +
                ",ISNULL(ORDERSTRADEMARKS.TEXT, '') AS TEXT " +
                ",ORDERSTRADESALE.CLIENTID " +
                ",a1.agentname " +
                ",ORDERSTRADE.SHIFTID " +
                ",ORDERSTRADE.PUBLISHER" +
                ",ORDERSTRADESALE.EDISTATUS" +
                ",ORDERSTRADESALE.GROUPORDERID " +
                "FROM ORDERSTRADE " +
                "LEFT JOIN ORDERSTRADEMARKS ON ORDERSTRADE.ID  = ORDERSTRADEMARKS.ITEMID " +
                "LEFT JOIN ORDERSTRADESALE  ON ORDERSTRADE.ID  = ORDERSTRADESALE.ITEMID " +
                "INNER JOIN Agents as A1 on ORDERSTRADESALE.CLIENTID = A1.agentid " +
                "WHERE ORDERSTRADE.FDATE BETWEEN  CONVERT(date, getdate()) AND DATEADD(day, 5, GETDATE()) " +
//                "WHERE ORDERSTRADE.FDATE BETWEEN  '2016-06-22' AND '2016-06-23' " +  //debug condition
                "AND DIRECTID = 0  " +
                "AND ORDERSTRADE.PUBLISHER IN (1,11) " +
                "and a1.agentid = 44728 " +
                "AND EDISTATUS = 0 " +
                "ORDER BY ORDERSTRADE.ID";

        try {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            logger.debug("getDocuments() resulSet fetch size: " + resultSet.getFetchSize());

            while (resultSet.next()) {
                long id = resultSet.getLong("ID");

                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                Date dateFromSQL = resultSet.getDate("FDATE");
                String date = dateFormat.format(dateFromSQL);

                String customer = resultSet.getString("agentname");

                String comment = resultSet.getString("TEXT");
                Currency currency;
                switch (resultSet.getInt("MONEYID")) {
                    case 0:
                        currency = Currency.CASH;
                        break;
                    default:
                        currency = Currency.CASHLESS;
                        break;
                }

                // add prepared document's headers
                documents.add(new Document(id, date, customer, currency, comment));
            }
            closeResultSet(resultSet);
            closePreparedStatement(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (documents != null & documents.size() != 0) {
            logger.info("Documents count: " + documents.size());
        } else {
            logger.info("There aren't documents for proceed");
            System.exit(0);
        }


    } // getDocuments()

    private void fillDocuments(ArrayList<Document> documents) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        for (Document doc : documents) { // 4 each document
            String query = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED\n" +
                    "SELECT ITEMID " +
                        ",OTI.TOVARID " +
                        ",KTI.kievId " +
                        ",Tovar.Name " +
                        ",QTYSTR " +
                        ",QTY " +
                        ",PRICE " +
                        ",FFACTOREDDATE " +
                        ",MEASUREID " +
                        ",Measure.AgentName as measureName " +
                        ",CAST(QTYI as nvarchar(10)) AS QTYI " +
                        ",PRICEI " +
                    "FROM ORDERSTRADEITEMS AS OTI " +
                        "INNER JOIN Tovar AS Tovar ON Tovar.TovarID = OTI.TOVARID " +
                        "INNER JOIN Agents AS Measure ON Measure.AgentID = MEASUREID " +
                        "LEFT JOIN KievTovarsIds  KTI ON OTI.TOVARID = KTI.tovarId " +
                    "WHERE ItemId = " + doc.getId();
            ArrayList<Product> products = new ArrayList<>();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            logger.debug("fillDocuments() " + doc.getId() + " resulSet fetch size: " + resultSet.getFetchSize());


            while (resultSet.next()) { // fill products array
                long id;
                String name;
                Measure measure; // кг или шт
                BigDecimal quantity = null; //целое или вещественное (3 знака после запятой)


                id = resultSet.getLong("kievId");
                name = resultSet.getString("Name");

                switch (resultSet.getString("measureName")) {
                    case "кг":
                        measure = Measure.KILO;
                        break;
                    default:
                        measure = Measure.PC;
                        break;
                }
// in case CAST(QTYI as nvarchar(10)) AS QTYI " -> "QTYI"
                    /*switch (measure) {
                        case KILO:
                            quantity = new BigDecimal(resultSet.getString("QTYI")).setScale(3, RoundingMode.HALF_UP);
                            break;
                        case PC:
                            quantity = new BigDecimal(resultSet.getString("QTYI")).setScale(0, RoundingMode.HALF_UP);
                            break;
                    }*/
                quantity = new BigDecimal(resultSet.getString("QTYI"));

                logger.trace("Product # " + (products.size() + 1) + "\t" + "weight" + resultSet.getString("QTYI"));
//                logger.trace(quantity);
                //saving to array
                Product product = new Product(id, name, measure, quantity);
                products.add(product);
            }
            doc.setProducts(products);

            closeResultSet(resultSet);
            closePreparedStatement(preparedStatement);
        }

    } // end fillDocuments()

    public void updateDocumentsStatuses(ArrayList<Document> documents) throws SQLException { // after all actions with db, xml, upload
        PreparedStatement preparedStatement = null;

//        1. make string with documents numbers
        String documentsNumbers = makeDocumentsNumbersString(documents);

        if (documentsNumbers == null) {
            return;
        }
//        2. update
        String query = "UPDATE OrdersTradeSale SET EdiStatus = 1 WHERE ItemId IN " + documentsNumbers;
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.executeUpdate();

        logger.debug("updateDocumentsStatuses() success");
        closePreparedStatement(preparedStatement);
    } // end updateDocumentsStatuses()

    private void closeConnection() throws SQLException {
        if (connection != null) connection.close();
    }

    private void closeResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet != null) resultSet.close();
    }

    private void closePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        if (preparedStatement != null) preparedStatement.close();
    }

    @org.jetbrains.annotations.Nullable
    private String makeDocumentsNumbersString(ArrayList<Document> documents) {
        StringBuilder documentsNumbers = new StringBuilder("("); // (num1, num2, ..., numN)

        for (Document doc : documents) {
            documentsNumbers.append(doc.getId() + ", ");
        }
        if (documentsNumbers.length() > 1) {
            documentsNumbers = documentsNumbers.replace(documentsNumbers.lastIndexOf(", "), documentsNumbers.length(), ")");
            logger.debug("makeDocumentsNumbersString() : " + documentsNumbers);
        } else {
            logger.debug("makeDocumentsNumbersString() is null");
            return null;
        }
        return documentsNumbers.toString();
    } // end makeDocumentsNumbersString()
}
