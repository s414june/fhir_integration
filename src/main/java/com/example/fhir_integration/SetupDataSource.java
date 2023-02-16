package com.example.fhir_integration;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

public class SetupDataSource {

    public SQLServerDataSource setSQL(JsonNode dataObj) {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setUser(dataObj.findValue("username").asText());
        ds.setPassword(dataObj.findValue("password").asText());
        // ds.setServerName("localhost\\\\" + dataObj.findValue("servername").asText() +
        // ";");
        ds.setServerName(dataObj.findValue("servername").asText());
        ds.setPortNumber(Integer.parseInt(dataObj.findValue("portnumber").asText()));
        if (dataObj.findValue("databasename").asText() != "")
            ds.setDatabaseName(dataObj.findValue("databasename").asText());
        else
            ds.setDatabaseName("master");
        ds.setTrustServerCertificate(true);
        ds.setEncrypt(false);

        return ds;
    }

    public boolean setSession(JsonNode dataObj, HttpSession session) {
        try {
            String sourceName = dataObj.findValue("source").asText();
            session.setAttribute("dataObj_" + sourceName, dataObj.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public JsonNode getSession_JsonNode(HttpSession session, String sourceName)
            throws JsonMappingException, JsonProcessingException {
        String dataObj_Str = session.getAttribute("dataObj_" + sourceName).toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dataObj = mapper.readTree(dataObj_Str);
        return dataObj;
    }
}
