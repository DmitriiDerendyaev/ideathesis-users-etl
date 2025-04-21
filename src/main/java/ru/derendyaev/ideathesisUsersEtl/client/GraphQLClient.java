package ru.derendyaev.ideathesisUsersEtl.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeesResponse;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentsResponse;

import java.util.HashMap;
import java.util.Map;

@Component
public class GraphQLClient {

    @Value("${graphql.webclient.endpoint}")
    private String endpoint;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GraphQLClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(endpoint).build();
        this.objectMapper = objectMapper;
    }

    public StudentsResponse getStudents() {
        String query = "{ students { items { fullName guid firstName surname middleName department group course startYear degreeLevel degreeForm } } }";
        Map<String, String> request = new HashMap<>();
        request.put("query", query);

        Mono<String> responseMono = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);

        String response = responseMono.block();

        try {
            Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
            return objectMapper.convertValue(jsonResponse.get("data"), StudentsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GraphQL response", e);
        }
    }

    public EmployeesResponse getEmployees() {
        String query = "{ employees { items { fullName guid surname mail dateOfBirth employeeEmployments { jobTitle staffCategory employmentType subDivision subDivisionGuid jobState } } } }";
        Map<String, String> request = new HashMap<>();
        request.put("query", query);

        Mono<String> responseMono = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);

        String response = responseMono.block();

        try {
            Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
            return objectMapper.convertValue(jsonResponse.get("data"), EmployeesResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GraphQL response", e);
        }
    }
}