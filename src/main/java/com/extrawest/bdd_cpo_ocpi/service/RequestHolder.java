package com.extrawest.bdd_cpo_ocpi.service;

import com.extrawest.ocpi.model.markers.OcpiRequestData;
import io.restassured.http.Method;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
//@ScenarioScope(proxyMode = ScopedProxyMode.NO)
public class RequestHolder {
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, String> pathParams = new HashMap<>();
    private Method httpMethod;
    private String requestAddress;

    private OcpiRequestData body;

    private static void addNewAndReplaceExisted(Map<String, String> existed, Map<String, String> additional) {
        existed.putAll(
                additional.entrySet().stream()
                        .filter(entry -> !existed.containsKey(entry.getKey())
                                         || !existed.get(entry.getKey()).equals(entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    public void addHeaders(Map<String, String> requestHeaders) {
        addNewAndReplaceExisted(headers, requestHeaders);
    }

    public void addQueryParameters(Map<String, String> params) {
        addNewAndReplaceExisted(queryParams, params);
    }

    public void addPathParameters(Map<String, String> params) {
        addNewAndReplaceExisted(pathParams, params);
    }

    public Method getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(Method httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestAddress() {
        return requestAddress;
    }

    public void setRequestAddress(String requestAddress) {
        this.requestAddress = requestAddress;
    }

    public OcpiRequestData getBody() {
        return body;
    }

    public <T extends OcpiRequestData> void setBody(T body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }
}
