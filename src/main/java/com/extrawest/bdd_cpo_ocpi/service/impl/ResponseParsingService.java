package com.extrawest.bdd_cpo_ocpi.service.impl;

import com.extrawest.bdd_cpo_ocpi.exception.AssertionException;
import com.extrawest.bdd_cpo_ocpi.exception.BddTestingException;
import com.extrawest.bdd_cpo_ocpi.models.enums.ImplementedMessageType;
import com.extrawest.bdd_cpo_ocpi.validation.AssertionAndValidationService;
import com.extrawest.bdd_cpo_ocpi.validation.RequestMessageFactory;
import com.extrawest.ocpi.model.markers.OcpiRequestData;
import com.extrawest.ocpi.model.markers.OcpiResponseData;
import io.restassured.response.Response;
import jakarta.inject.Singleton;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.extrawest.bdd_cpo_ocpi.exception.ApiErrorMessage.ANY_NOT_ALLOWED;
import static com.extrawest.bdd_cpo_ocpi.exception.ApiErrorMessage.EMPTY_RESPONSE_DATA_RECEIVED;
import static com.extrawest.bdd_cpo_ocpi.exception.ApiErrorMessage.EMPTY_RESPONSE_RECEIVED;
import static com.extrawest.bdd_cpo_ocpi.exception.ApiErrorMessage.EXPECTED_AND_ACTUAL_LISTS_NOT_EQUALS;
import static com.extrawest.bdd_cpo_ocpi.exception.ApiErrorMessage.INVALID_RESPONSE_RECEIVED;
import static com.extrawest.bdd_cpo_ocpi.exception.ApiErrorMessage.MODEL_CANT_BE_INSTANTIATED;
import static com.extrawest.bdd_cpo_ocpi.exception.ApiErrorMessage.NON_MATCH_RESPONSE_LIST_SIZE;
import static com.extrawest.bdd_cpo_ocpi.exception.ApiErrorMessage.NON_MATCH_ROW;

@Singleton
public class ResponseParsingService {
    private static final Logger log = LoggerFactory.getLogger(ResponseParsingService.class);

    private static final String DATA_FIELD = "data";
    private final AssertionAndValidationService factories;
    //    @Value("${wildcard:any}")
    protected String wildCard;

    public ResponseParsingService(AssertionAndValidationService factories) {
        this.factories = factories;
    }

    public static <T> List<T> parseList(Response response, Class<T> clazz) {
        checkNotEmpty(response);

        List<T> data;
        try {
            data = response.jsonPath().getList(DATA_FIELD, clazz);
        } catch (RuntimeException e) {
            throw new BddTestingException(String.format(MODEL_CANT_BE_INSTANTIATED.getValue(),
                    clazz.getSimpleName(), e.getMessage()));
        }

        if (data == null || data.isEmpty()) {
            throw new BddTestingException(EMPTY_RESPONSE_DATA_RECEIVED.getValue());
        }
        if (data.contains(null)) {
            throw new BddTestingException(INVALID_RESPONSE_RECEIVED.getValue());
        }
        return data;
    }


    public static <T> T parseObject(Response response, Class<T> clazz) {
        checkNotEmpty(response);

        T data;
        try {
            data = response.jsonPath().getObject(DATA_FIELD, clazz);
        } catch (RuntimeException e) {
            throw new BddTestingException(String.format(MODEL_CANT_BE_INSTANTIATED.getValue(),
                    clazz.getSimpleName(), e.getMessage()));
        }

        if (data == null) {
            throw new BddTestingException(String.format(EMPTY_RESPONSE_DATA_RECEIVED.getValue()));
        }
        return data;
    }

    private static <T extends OcpiRequestData> void validateEquals(List<T> actualList, List<T> expectedList) {
        boolean isEqualCollection = CollectionUtils.isEqualCollection(expectedList, actualList);
        if (!isEqualCollection) {
            List<T> differences = new ArrayList<>(CollectionUtils.subtract(actualList, expectedList));

            log.warn("Actual list has no expected objects:\n {}", differences.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n")));

            log.warn(EXPECTED_AND_ACTUAL_LISTS_NOT_EQUALS.getValue());
            throw new AssertionException(EXPECTED_AND_ACTUAL_LISTS_NOT_EQUALS.getValue());
        }
    }

    private static <T extends OcpiRequestData> void validateListSize(List<Map<String, String>> rows,
                                                                     List<T> actualList) {
        if (rows.size() != actualList.size()) {
            throw new AssertionException(NON_MATCH_RESPONSE_LIST_SIZE.getValue());
        }
    }

    private static void checkNotEmpty(Response response) {
        if (response.body().asString().isEmpty()) {
            throw new BddTestingException(String.format(EMPTY_RESPONSE_RECEIVED.getValue()));
        }
    }

    public <T extends OcpiRequestData> void validateResponseListEquals(List<Map<String, String>> rows,
                                                                       Response response,
                                                                       Class<T> clazz,
                                                                       ImplementedMessageType implementedMessageType) {
        checkNotEmpty(response);
        validateHasNoAny(rows);

        List<T> actualList = parseList(response, clazz);
        List<T> expectedList = mapRowsToType(rows, implementedMessageType, clazz);

        validateListSize(rows, actualList);
        validateEquals(actualList, expectedList);
    }

    public <T extends OcpiResponseData> void validateResponseListContains(List<Map<String, String>> rows,
                                                                          Response response,
                                                                          Class<T> clazz) {
        List<T> actualResponseList = ResponseParsingService.parseList(response, clazz);
        List<Map<String, String>> notFoundRows = findNotFoundRows(rows, actualResponseList);

        if (!notFoundRows.isEmpty()) {
            log.warn("Non-matching rows: " + notFoundRows);
            throw new AssertionException(String.format(NON_MATCH_ROW.getValue(), notFoundRows));
        }
    }

    private void validateHasNoAny(List<Map<String, String>> rows) {
        boolean hasAny = rows.stream().anyMatch(map -> map.values().stream().anyMatch(wildCard::equals));
        if (hasAny) {
            throw new AssertionException(ANY_NOT_ALLOWED.getValue());
        }
    }

    @NotNull
    private <T extends OcpiRequestData> List<T> mapRowsToType(List<Map<String, String>> rows,
                                                              ImplementedMessageType implementedType,
                                                              Class<T> clazz) {
        RequestMessageFactory<? extends OcpiRequestData> messageFactory =
                factories.getOutgoingRequestFactory(implementedType);


        return rows.stream().map(row -> {
            try {
                return clazz.cast(messageFactory.createMessageWithValidatedParams(row));
            } catch (ClassCastException e) {
                log.warn("Can not create type {} from row {}: ", clazz.getSimpleName(), row);
                throw new BddTestingException(String.format(MODEL_CANT_BE_INSTANTIATED.getValue(),
                        clazz.getSimpleName(), e.getMessage()));
            }
        }).collect(Collectors.toList());
    }

    private <T extends OcpiResponseData> List<Map<String, String>> findNotFoundRows(List<Map<String, String>> rows,
                                                                                    List<T> actualResponseList) {
        return rows.stream()
                .filter(row -> actualResponseList.stream().noneMatch(el -> isRowFound(el, row)))
                .collect(Collectors.toList());
    }

    private <T extends OcpiResponseData> boolean isRowFound(T element, Map<String, String> row) {
        try {
            factories.validateAndAssertFields(element, row);
            return true;
        } catch (AssertionException e) {
            return false;
        }
    }

}
