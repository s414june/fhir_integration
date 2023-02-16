package com.example.fhir_integration;

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

import com.example.fhir_integration.RouteBuilderTool.thisRouteBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

@RestController
public class ConnectDbController extends HttpServlet {
    HttpSession session = null;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        session = request.getSession();
    }

    @PostMapping("/api/connectdb")
    // @GetMapping("/api/connectdb")
    public ResponseEntity<?> getConnectResultViaAjax(HttpServletRequest request, HttpServletResponse response,
            @Validated @RequestBody String connectdatas, Errors errors)
            throws Exception {
                response.setHeader("Access-Control-Allow-Origin", "*");
        doGet(request, response);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dataObj = mapper.readTree(connectdatas);
        // String database = "";

        JsonNode data = new ObjectMapper().readTree("{}");

        if (dataObj.findValue("driver").asText().equals("sqlserver")) {
            SetupDataSource setupDataSource = new SetupDataSource();
            SQLServerDataSource dbSource = setupDataSource.setSQL(dataObj);
            DefaultRegistry reg = new DefaultRegistry();
            reg.bind("dbSource", dbSource);
            CamelContext context = new DefaultCamelContext(reg);
            RouteBuilderTool buildtool = new RouteBuilderTool();
            buildtool.getSelectdataFloor("source");
            thisRouteBuilder build = buildtool.thisRouteBuilder();
            context.addRoutes(build);
            context.start();
            Thread.sleep(5000);
            context.stop();
            context.close();
            data = buildtool.getDatabases();
            // data = new ObjectMapper().readTree(database);
            if (buildtool.hasErrors()) {
                return ResponseEntity.badRequest().body("連線錯誤");
            }
            // 若連線成功，將連線資訊存進session
            setupDataSource.setSession(dataObj, session);
        }

        // If error, just return a 400 bad request, along with the error message
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
