package com.example.fhir_etl_test;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.support.DefaultRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.fhir_etl_test.RouteBuilderTool.thisRouteBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

@RestController
public class SearchMultiTableController extends HttpServlet {
    HttpSession session = null;
    String useTable = "";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        session = request.getSession();
    }

    @PostMapping("/api/searchtable_multi")
    public ResponseEntity<?> getSearchResultViaAjax(HttpServletRequest request, HttpServletResponse response,
            @Validated @RequestBody String dbdatas, Errors errors)
            throws Exception {
        doGet(request, response);
        JsonNode data = new ObjectMapper().readTree("{}");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dbObj = mapper.readTree(dbdatas);

        for (JsonNode source : dbObj) {
            Iterator<String> itr = source.fieldNames();
        }

        // String sourceName = dbObj.findValue("source").asText();

        // SetupDataSource setupDataSource = new SetupDataSource();
        // JsonNode dataObj = setupDataSource.getSession_JsonNode(session, sourceName);
        // useTable = dbObj.findValue("selectdata").asText();
        // ObjectNode objectNode = (ObjectNode) dataObj;
        // // objectNode.put("databasetable", useTable);
        // dataObj = objectNode;
        // if (dataObj.findValue("driver").asText().equals("sqlserver")) {
        //     SQLServerDataSource dbSource = setupDataSource.setSQL(dataObj);
        //     DefaultRegistry reg = new DefaultRegistry();
        //     reg.bind("dbSource", dbSource);
        //     CamelContext context = new DefaultCamelContext(reg);
        //     RouteBuilderTool buildtool = new RouteBuilderTool();
        //     buildtool.getSelectdataFloor("table");
        //     buildtool.getUseTable(useTable);
        //     thisRouteBuilder build = buildtool.thisRouteBuilder();
        //     context.addRoutes(build);
        //     context.start();
        //     Thread.sleep(5000);
        //     context.stop();
        //     context.close();
        //     data = buildtool.getDatabases();
        // }

        // if (errors.hasErrors()) {
        //     return ResponseEntity.badRequest().body(
        //             errors.getAllErrors().stream().map(x -> x.getDefaultMessage())
        //                     .collect(Collectors.joining(",")));
        // }

        return ResponseEntity.ok(data.toString());
    }

    class MyRouteBuilder extends RouteBuilder {
        JsonNode database = null;
        String errorMsg = "";
        boolean hasErrors = false;

        public void configure() throws Exception {
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
    }

}
