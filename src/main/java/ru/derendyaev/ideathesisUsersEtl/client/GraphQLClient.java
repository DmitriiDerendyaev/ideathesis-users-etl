package ru.derendyaev.ideathesisUsersEtl.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeesResponse;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentsResponse;

import java.beans.Introspector;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class GraphQLClient {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GraphQLClient(@Value("${graphql.webclient.endpoint}") String endpoint,
                         WebClient.Builder webClientBuilder,
                         ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(endpoint).build();
        this.objectMapper = objectMapper;
    }

    public StudentsResponse getStudents() {
        String query = "{ students { items { fullName guid firstName surname middleName department group course startYear degreeLevel degreeForm } } }";
        return executeQuery(query, "students", StudentsResponse.class);
    }

    public EmployeesResponse getEmployees() {
        String query = "{ employees { items { fullName guid surname mail dateOfBirth employeeEmployments { jobTitle staffCategory employmentType subDivision subDivisionGuid jobState } } } }";
        return executeQuery(query, "employees", EmployeesResponse.class);
    }

    /**
     * Выполняет GraphQL-запрос и возвращает нужную часть ответа в указанном типе.
     */
    private <T> T executeQuery(String query, String dataKey, Class<T> responseType) {
        Map<String, String> request = Collections.singletonMap("query", query);

        String response = webClient.post()
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp -> {
                    logger.error("GraphQL server responded with error status code: {}", resp.statusCode());
                    return Mono.error(new RuntimeException("GraphQL request failed with HTTP error"));
                })
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        if (response == null || response.isEmpty()) {
            throw new RuntimeException("Empty response from GraphQL server");
        }

        try {
            Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
            Map<String, Object> data = (Map<String, Object>) jsonResponse.get("data");

            if (data == null) {
                throw new RuntimeException("Missing 'data' field in GraphQL response");
            }

            Object resultData = data.get(dataKey);

            if (resultData == null) {
                throw new RuntimeException("Missing '" + dataKey + "' field in GraphQL response");
            }

            return objectMapper.convertValue(resultData, responseType);
        } catch (Exception e) {
            logger.error("Failed to parse GraphQL response for key '{}': {}", dataKey, response, e);
            throw new RuntimeException("Failed to parse GraphQL response for key: " + dataKey, e);
        }
    }
}