package com.extrawest.bdd_cpo_ocpi.cucumber;

import com.extrawest.bdd_cpo_ocpi.ServerEndpoints;
import com.extrawest.bdd_cpo_ocpi.config.CpoConfig;
import com.extrawest.bdd_cpo_ocpi.config.EmspConfig;
import com.extrawest.bdd_cpo_ocpi.containers.ContainerBase;
import com.extrawest.bdd_cpo_ocpi.exception.BddTestingException;
import com.extrawest.bdd_cpo_ocpi.models.enums.ImplementedMessageType;
import com.extrawest.bdd_cpo_ocpi.repository.CredentialsRepository;
import com.extrawest.bdd_cpo_ocpi.repository.VersionDetailsRepository;
import com.extrawest.bdd_cpo_ocpi.repository.VersionsRepository;
import com.extrawest.bdd_cpo_ocpi.service.MessageService;
import com.extrawest.bdd_cpo_ocpi.service.RequestHolder;
import com.extrawest.bdd_cpo_ocpi.service.RequestService;
import com.extrawest.bdd_cpo_ocpi.service.impl.ResponseParsingService;
import com.extrawest.ocpi.model.dto.BusinessDetails;
import com.extrawest.ocpi.model.dto.CredentialsDto;
import com.extrawest.ocpi.model.dto.CredentialsRole;
import com.extrawest.ocpi.model.dto.VersionDetailsDto;
import com.extrawest.ocpi.model.dto.VersionDto;
import com.extrawest.ocpi.model.dto.tariff.TariffDto;
import com.extrawest.ocpi.model.enums.Role;
import com.extrawest.ocpi.model.markers.OcpiRequestData;
import com.extrawest.ocpi.model.markers.OcpiResponseData;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.Method;
import io.restassured.response.Response;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.extrawest.bdd_cpo_ocpi.exception.ApiErrorMessage.EMPTY_EXPECTED_VALUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;
//import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

//@CucumberContextConfiguration
//@SpringBootTest
//@RunWith(Cucumber.class)
//@CucumberOptions(features = "src/test/resources")
//@Testcontainers
@Singleton
public class StepsDefinitionTest extends ContainerBase {
    private static final Logger log = LoggerFactory.getLogger(StepsDefinitionTest.class);

    private final static String AUTHORIZATION_TOKEN = "Token %s";
    private final static String STATUS_CODE = "status_code";
    //    private static final List<String> mongoCollections = new ArrayList<>();
    private static Response response;
    private final VersionDetailsRepository versionDetailsRepository;
    private final VersionsRepository versionsRepository;
    private final CredentialsRepository credentialsRepository;
    private final ServerEndpoints serverEndpoints;
    private final MessageService messagingService;
    private final ResponseParsingService responseListService;
    private final RequestService httpStepsService;
    private final RequestHolder requestHolder;
    private final EmspConfig emspConfig;
    private final CpoConfig cpoConfig;
    //    private final MongoTemplate mongoTemplate;
//    @Autowired
//    @Qualifier("mongoDatabaseFactory")
//    private CodecRegistryProvider codecRegistryProvider;
    private int stepNumber;

    public StepsDefinitionTest(CpoConfig cpoConfig,
                               VersionDetailsRepository versionDetailsRepository,
                               VersionsRepository versionsRepository,
                               CredentialsRepository credentialsRepository,
                               ServerEndpoints serverEndpoints,
                               MessageService messagingService,
                               ResponseParsingService responseListService,
                               RequestService httpStepsService,
                               RequestHolder requestHolder,
                               EmspConfig emspConfig) {
        this.cpoConfig = cpoConfig;
        this.versionDetailsRepository = versionDetailsRepository;
        this.versionsRepository = versionsRepository;
        this.credentialsRepository = credentialsRepository;
        this.serverEndpoints = serverEndpoints;
        this.messagingService = messagingService;
        this.responseListService = responseListService;
        this.httpStepsService = httpStepsService;
        this.requestHolder = requestHolder;
        this.emspConfig = emspConfig;
    }

    private static int getOcpiStatusCode(Response response) {
        return response.jsonPath().getInt(STATUS_CODE);
    }

    @Before
    public void scenarioIncrease(Scenario scenario) {
        int scenarioLine = scenario.getLine();
        String scenarioName = scenario.getName();

        // werkt niet want scenario file in andere module dus uri is 'classpath:bdd-test/versions.feature'
//        String scenarioFileName = Paths.get(scenario.getUri()).getFileName().toString();
        String scenarioFileName = scenario.getUri().getSchemeSpecificPart();
        log.info(String.format("\nNew Scenario: %s (%s, line %s)", scenarioName, scenarioFileName, scenarioLine));

        stepNumber = 0;

        requestHolder.addHeaders(Map.of(
                "Content-Type", "application/json",
                "Authorization", String.format(AUTHORIZATION_TOKEN, emspConfig.getTokenA())
        ));

        getVersions();
        getVersionDetails();
    }

    @BeforeStep
    public void stepIncrease() {
        stepNumber++;
        requestHolder.setBody(null);
    }

    @After
    public void tearDown() {
//        mongoCollections.forEach(mongoCollection -> RepositoryUtils.remove(mongoTemplate,
//                new org.springframework.data.mongodb.core.query.Query(),
//                mongoCollection));
//        mongoCollections.clear();

        versionsRepository.clear();
        versionDetailsRepository.clear();
    }

    @Given("CPO has {string} data {string}")
    public void createMongoCollectionWithData(String collectionName, String filePath) {
//        mongoCollections.add(collectionName);
//        importToCollection(codecRegistryProvider, mongoTemplate,
//                readJson(filePath), collectionName);
        log.info("STEP {}: added {} into DB collection {}", stepNumber, filePath, collectionName);
    }

    @Given("eMSP is registered in CPO system")
    public void registerCpo() {
        CredentialsDto credentials = getCredentials();
        requestHolder.addHeaders(Map.of(
                "Authorization", String.format(AUTHORIZATION_TOKEN, credentials.getToken()))
        );
    }

    ////////////////////////////////////////////////    Query params   /////////////////////////////////////////////////
    @And("{string} query param is {string}")
    public void addQueryParams(String parameterName, String parameterValue) {
        Map<String, String> requestParameters = Map.of(parameterName, parameterValue);
        requestHolder.addQueryParameters(requestParameters);
    }

    @And("{string} path param is {string}")
    public void addPathParams(String parameterName, String parameterValue) {
        Map<String, String> pathParameters = Map.of(parameterName, parameterValue);
        requestHolder.addPathParameters(pathParameters);
    }

    ////////////////////////////////////////////////////    GET   //////////////////////////////////////////////////////
    @When("eMSP with {string} {string} and {string} {string} gets {string}s")
    public void get(String paramName1, String paramValue1,
                    String paramName2, String paramValue2,
                    String messageType) {
        ImplementedMessageType implementedType = getImplementedMessageType(messageType);
        Map<String, String> parameters = Map.of(
                paramName1, paramValue1,
                paramName2, paramValue2);
        requestHolder.addQueryParameters(parameters);

        response = sendRequest(Method.GET, implementedType);

        log.info("STEP {}: CPO gets his {} from eMSP system, {} exists", stepNumber, messageType, messageType);
    }

    @When("CPO checks {string} in eMSP system")
    public void get(String messageType) {
        ImplementedMessageType implementedType = getImplementedMessageType(messageType);
        response = sendRequest(Method.GET, implementedType);

        log.info("STEP {}: CPO checks his {} in eMSP system, {} exists", stepNumber, messageType, messageType);
    }

    ////////////////////////////////////////////////////   POST   //////////////////////////////////////////////////////
    @Given("eMSP post {string} in CPO system with data")
    public void post(String messageType, DataTable body) {
        ImplementedMessageType implementedType = getImplementedMessageType(messageType);

        if (nonNull(body) && !body.isEmpty()) {
            OcpiRequestData ocpiData = messagingService.createRequestBody(implementedType, body.asMap());
            requestHolder.setBody(ocpiData);
        }
        response = sendRequest(Method.POST, implementedType);
        log.info("STEP {}: CPO send PUT {} into eMSP system", stepNumber, messageType);
    }

    public CredentialsDto getCredentials() {
        CredentialsDto emspCredentials = new CredentialsDto();
        emspCredentials.setUrl(emspConfig.getVersionUrl());
        emspCredentials.setToken(emspConfig.getTokenA());
        CredentialsRole credentialsRole = CredentialsRole.builder()
                .role(Role.valueOf(emspConfig.getRole()))
                .businessDetails(new BusinessDetails())
                .countryCode(emspConfig.getCountryCode())
                .partyId(emspConfig.getPartyId())
                .build();
        emspCredentials.setRoles(List.of(credentialsRole));

        requestHolder.setBody(emspCredentials);

        ImplementedMessageType implementedType = ImplementedMessageType.CREDENTIALS;
        response = sendRequest(Method.POST, implementedType);

        checkResponseIsSuccess();

        CredentialsDto cpoCredentials = (CredentialsDto)
                messagingService.createResponseBody(implementedType, response);

        credentialsRepository.setCpoCredentials(cpoCredentials);

        log.info("Before tests: CPO received emsp credentials");
        return cpoCredentials;
    }

    @When("eMSP checks \"version\" in CPO system")
    public void getVersions() {
        createMongoCollectionWithData("tokens-a", "db/credentials.json");
        ImplementedMessageType implementedType = ImplementedMessageType.VERSION;
        response = sendRequest(Method.GET, implementedType);
        checkResponseIsSuccess();

        List<VersionDto> versionsList = ResponseParsingService.parseList(response, VersionDto.class);
        versionsRepository.addAll(versionsList);

        log.info("Before tests: CPO received list of versions from eMSP");
    }

    public void getVersionDetails() {
        ImplementedMessageType implementedType = ImplementedMessageType.VERSION_DETAILS;
        response = sendRequest(Method.GET, implementedType);
        checkResponseIsSuccess();

        VersionDetailsDto details = (VersionDetailsDto)
                messagingService.createResponseBody(implementedType, response);
        versionDetailsRepository.addAll(details);

        log.info("Before tests: CPO received 2.2.1 version details from eMSP");
    }


    /////////////////////////////////////////////////    Check response   //////////////////////////////////////////////
    @And("response is success")
    public void checkResponseIsSuccess() {
        checkResponseIsSuccess(response);
        log.info("STEP {}: response is success", stepNumber);
    }

    @Then("eMSP responded with HTTP status {int}")
    public void checkHttpResponseStatusCode(int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.statusCode());
        log.info("STEP {}: eMSP responded with HTTP status {}", stepNumber, expectedStatusCode);
    }

    @Then("eMSP responded with OCPI status {int}")
    public void checkOcpiResponseStatusCode(int expectedOcpiStatusCode) {
        int actualOcpiStatusCode = getOcpiStatusCode(response);
        assertEquals(expectedOcpiStatusCode, actualOcpiStatusCode);

        log.info("STEP {}: eMSP responded with OCPI status {}", stepNumber, expectedOcpiStatusCode);
    }

    @Then("{string} response is valid and has data")
    public void validateResponse(String messageType, DataTable table) {
        checkResponseIsSuccess(response);
        Map<String, String> parameters = isNull(table) || table.isEmpty() ? Collections.emptyMap() : table.asMap();

        ImplementedMessageType type = getImplementedMessageType(messageType);
        OcpiResponseData responseBody = messagingService.createResponseBody(type, response);
        messagingService.validateResponseBody(parameters, responseBody);

        log.info("STEP {}: Response is valid and fields are asserted", stepNumber);
    }

    @And("list of versions response is valid and contains values")
    public void validateListResponseContains(DataTable table) {
        checkResponseIsSuccess(response);
        if (isNull(table) || table.isEmpty()) {
            log.info("STEP {}: Expected values was not provided", stepNumber);
            throw new BddTestingException(EMPTY_EXPECTED_VALUE.getValue());
        }
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        responseListService.validateResponseListContains(rows, response, VersionDto.class);
        log.info("STEP {}: Response is valid and fields are asserted", stepNumber);
    }

    @And("list of tariffs response is valid and is")
    public void validateListResponseIs(DataTable table) {
        checkResponseIsSuccess(response);
        if (isNull(table) || table.isEmpty()) {
            log.info("STEP {}: Expected values was not provided", stepNumber);
            throw new BddTestingException(EMPTY_EXPECTED_VALUE.getValue());
        }
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        responseListService.validateResponseListEquals(rows, response, TariffDto.class, ImplementedMessageType.TARIFF);
        log.info("STEP {}: Response is valid and fields are asserted", stepNumber);
    }


    private Response sendRequest(Method httpMethod, ImplementedMessageType messageType) {
        String url = serverEndpoints.getUrl(messageType);
        requestHolder.setRequestAddress(url);
        requestHolder.setHttpMethod(httpMethod);

        try {
            return httpStepsService.sendRequest(requestHolder);
        } catch (Exception e) {
            throw new BddTestingException(String.format("STEP %s: server responded with error: %s",
                    stepNumber, e.getMessage()));
        }
    }

    private ImplementedMessageType getImplementedMessageType(String messageType) {
        if (ImplementedMessageType.contains(messageType)) {
            return ImplementedMessageType.fromValue(messageType);
        } else {
            throw new BddTestingException(
                    String.format("STEP %s: wrong message request type or %s is not implemented.",
                            stepNumber, messageType));
        }
    }

    private void checkResponseIsSuccess(Response response) {
        if (!(response.statusCode() >= 200 && response.statusCode() <= 299)) {
            throw new BddTestingException(String.format("STEP %s: Server responded with http status code %s: %s",
                    stepNumber,
                    response.getStatusCode(),
                    response.asPrettyString()));
        }
    }
}
