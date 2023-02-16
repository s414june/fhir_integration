package com.example.fhir_integration;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteBuilderTool {
    private static final Logger log = LoggerFactory.getLogger(Camel.class);
    JsonNode database = null;
    String useDB = "";
    String useTable = "";
    String errorMsg = "";
    String selectdataFloor = "";
    boolean hasErrors = false;

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
                    // .split(body())
                    // .marshal().json(JsonLibrary.Jackson)
                    .marshal().json(JsonLibrary.Jackson)
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    database = exchange.getIn().getBody(JsonNode.class);
                                }
                            })
                    .doCatch(Exception.class)
                    // .log("error")
                    // .process(
                    // new Processor() {
                    // public void process(Exchange exchange) throws Exception {
                    // hasErrors = true;
                    // }
                    // });
                    // .setBody(exceptionMessage())
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                                    System.out.print(cause);
                                    log.error(cause.getMessage());
                                    hasErrors = true;
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
                    // .split(body())
                    // .marshal().json(JsonLibrary.Jackson)
                    .marshal().json(JsonLibrary.Jackson)
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    database = exchange.getIn().getBody(JsonNode.class);
                                }
                            })
                    .doCatch(Exception.class)
                    // .log("error")
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    hasErrors = true;
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
                    // .split(body())
                    // .marshal().json(JsonLibrary.Jackson)
                    .marshal().json(JsonLibrary.Jackson)
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    database = exchange.getIn().getBody(JsonNode.class);
                                }
                            })
                    .doCatch(Exception.class)
                    // .log("error")
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    hasErrors = true;
                                }
                            });
        }

    }

    public JsonNode getDatabases() {
        // List<String> databases = new ArrayList<String>();
        // return databases;
        return database;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean hasErrors() {
        return hasErrors;
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
}
