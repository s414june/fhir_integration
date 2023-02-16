package com.example.fhir_etl_test;

import java.io.IOException;
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
public class SearchDbController extends HttpServlet {
    HttpSession session = null;
    String useDB = "";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        session = request.getSession();
    }

    @PostMapping("/api/searchdb")
    // @GetMapping("/api/searchdb")
    public ResponseEntity<?> getSearchResultViaAjax(HttpServletRequest request, HttpServletResponse response,
            @Validated @RequestBody String dbdatas, Errors errors)
            throws Exception {
        doGet(request, response);
        JsonNode data = new ObjectMapper().readTree("{}");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dbObj = mapper.readTree(dbdatas);
        String sourceName = dbObj.findValue("source").asText();

        SetupDataSource setupDataSource = new SetupDataSource();
        JsonNode dataObj = setupDataSource.getSession_JsonNode(session,sourceName);
        useDB = dbObj.findValue("selectdata").asText();
        ObjectNode objectNode = (ObjectNode)dataObj;
        objectNode.put("databasename", useDB);
        dataObj = objectNode;
        setupDataSource.setSession(dataObj, session);
        if (dataObj.findValue("driver").asText().equals("sqlserver")) {
            SQLServerDataSource dbSource = setupDataSource.setSQL(dataObj);
            DefaultRegistry reg = new DefaultRegistry();
            reg.bind("dbSource", dbSource);
            CamelContext context = new DefaultCamelContext(reg);
            RouteBuilderTool buildtool = new RouteBuilderTool();
            buildtool.getSelectdataFloor("db");
            buildtool.getUseDB(useDB);
            thisRouteBuilder build = buildtool.thisRouteBuilder();
            context.addRoutes(build);
            context.start();
            Thread.sleep(5000);
            context.stop();
            context.close();
            data = buildtool.getDatabases();
        }

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    errors.getAllErrors().stream().map(x -> x.getDefaultMessage())
                            .collect(Collectors.joining(",")));
        }

        return ResponseEntity.ok(data.toString());
    }

    class MyRouteBuilder extends RouteBuilder {
        JsonNode database = null;
        String errorMsg = "";
        boolean hasErrors = false;

        public void configure() throws Exception {
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
