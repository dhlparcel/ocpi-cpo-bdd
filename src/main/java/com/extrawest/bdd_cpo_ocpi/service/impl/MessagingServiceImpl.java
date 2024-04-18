package com.extrawest.bdd_cpo_ocpi.service.impl;


import com.extrawest.bdd_cpo_ocpi.models.enums.ImplementedMessageType;
import com.extrawest.bdd_cpo_ocpi.service.MessageService;
import com.extrawest.bdd_cpo_ocpi.validation.AssertionAndValidationService;
import com.extrawest.bdd_cpo_ocpi.validation.RequestMessageFactory;
import com.extrawest.bdd_cpo_ocpi.validation.ResponseMessageFactory;
import com.extrawest.ocpi.model.markers.OcpiRequestData;
import com.extrawest.ocpi.model.markers.OcpiResponseData;
import io.restassured.response.Response;
import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
public class MessagingServiceImpl implements MessageService {
    private final AssertionAndValidationService factories;

    public MessagingServiceImpl(AssertionAndValidationService factories) {
        this.factories = factories;
    }

    @Override
    public OcpiRequestData createRequestBody(ImplementedMessageType type, Map<String, String> fields) {
        RequestMessageFactory<? extends OcpiRequestData> messageFactory = factories.getOutgoingRequestFactory(type);
        return messageFactory.createMessageWithValidatedParams(fields);
    }

    @Override
    public OcpiResponseData createResponseBody(ImplementedMessageType type, Response response) {
        ResponseMessageFactory<? extends OcpiResponseData> messageFactory = factories.getIncomingResponseFactory(type);
        Class<? extends OcpiResponseData> clazz = messageFactory.getClazz();
        return ResponseParsingService.parseObject(response, clazz);
    }

    @Override
    public void validateResponseBody(Map<String, String> parameters, OcpiResponseData responseBody) {
        factories.validateAndAssertFields(responseBody, parameters);
    }

}
