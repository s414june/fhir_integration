package com.example.fhir_integration;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import com.fasterxml.jackson.databind.JsonNode;

public class RouteBuilderTool {
    // private static final Logger log = LoggerFactory.getLogger(Camel.class);
    JsonNode database = null;
    String useDB = "";
    String useTable = "";
    ToolErrors errors = new ToolErrors();
    
    String selectdataFloor = "";
    boolean hasToolErrors = false;

    public thisRouteBuilder thisRouteBuilder() {
        thisRouteBuilder b = new thisRouteBuilder();
        return b;
    }

    public class thisRouteBuilder extends RouteBuilder {
        public void configure() throws Exception {
            switch (selectdataFloor) {
                case "source":
                    ConnectRouteBuilder();
                    break;
                case "db":
                    DBRouteBuilder();
                    break;
                case "table":
                    TableRouteBuilder();
                    break;
            }
        }

        public void ConnectRouteBuilder() {
            from("timer://foo?repeatCount=1")
                    .setBody(constant(
                            "SELECT name, database_id, create_date FROM sys.databases where database_id>4;"))
                    .doTry()
                    .to("jdbc:dbSource")
                    .marshal().json(JsonLibrary.Jackson)
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    database = exchange.getIn().getBody(JsonNode.class);
                                }
                            })
                    .doCatch(Exception.class)
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                                    System.out.print(cause.getMessage());
                                    log.error(cause.getMessage());
                                    errors.setLogMsg(cause.getMessage());
                                    errors.setShowMsg("連線錯誤");
                                    hasToolErrors = true;
                                }
                            })
                    .end();
        }

        public void DBRouteBuilder() {
            from("timer://foo?repeatCount=1")
                    .setBody(constant(
                            "use " + useDB + ";SELECT DISTINCT TABLE_NAME AS 'name' FROM INFORMATION_SCHEMA.COLUMNS"))
                    .doTry()
                    .to("jdbc:dbSource")
                    .marshal().json(JsonLibrary.Jackson)
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    database = exchange.getIn().getBody(JsonNode.class);
                                }
                            })
                    .doCatch(Exception.class)
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    hasToolErrors = true;
                                }
                            });
        }

        public void TableRouteBuilder() {
            String sqlStr = "";
            sqlStr += "SELECT COLUMN_NAME,DATA_TYPE,IS_NULLABLE,NUMERIC_PRECISION,NUMERIC_SCALE";
            sqlStr += " FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + useTable + "';";
            from("timer://foo?repeatCount=1")
                    .setBody(constant(sqlStr))
                    .doTry()
                    .to("jdbc:dbSource")
                    .marshal().json(JsonLibrary.Jackson)
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    database = exchange.getIn().getBody(JsonNode.class);
                                }
                            })
                    .doCatch(Exception.class)
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    hasToolErrors = true;
                                }
                            });
        }

    }

    public JsonNode getDatabases() {
        return database;
    }

    public ToolErrors getToolErrors() {
        return errors;
    }

    public boolean hasToolErrors() {
        return hasToolErrors;
    }

    public void getUseDB(String usedb) {
        useDB = usedb;
    }

    public void getUseTable(String usetable) {
        useTable = usetable;
    }

    public void getSelectdataFloor(String floor) {
        selectdataFloor = floor;
    }

    public class ToolErrors{
        private String logMsg = "";
        private String showMsg = "";
        public String getLogMsg(){
            return logMsg;
        }
        public String getShowMsg(){
            return showMsg;
        }
        public void setLogMsg(String msg)
        {
            this.logMsg = msg;
        }
        public void setShowMsg(String msg)
        {
            this.showMsg = msg;
        }
    }
}
