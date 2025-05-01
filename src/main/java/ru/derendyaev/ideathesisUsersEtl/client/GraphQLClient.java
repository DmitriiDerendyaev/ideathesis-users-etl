package ru.derendyaev.ideathesisUsersEtl.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeeDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeesResponse;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentsResponse;

import java.beans.Introspector;
import java.time.Duration;
import java.util.*;
import java.util.function.BiFunction;

@Component
public class GraphQLClient {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLClient.class);
    private static final int PAGE_SIZE = 100;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GraphQLClient(@Value("${graphql.webclient.endpoint}") String endpoint,
                         WebClient.Builder webClientBuilder,
                         ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(endpoint).build();
        this.objectMapper = objectMapper;
    }

    public List<StudentDTO> getAllStudents() {
        return getAllItems(this::getStudentsPage, "students");
    }

    public List<EmployeeDTO> getAllEmployees() {
        return getAllItems(this::getEmployeesPage, "employees");
    }

    private <T> List<T> getAllItems(BiFunction<Integer, Integer, List<T>> pageFetcher, String entityName) {
        List<T> allItems = new ArrayList<>();
        int skip = 0;
        List<T> currentPage;

        do {
            currentPage = pageFetcher.apply(PAGE_SIZE, skip);
            allItems.addAll(currentPage);
            skip += PAGE_SIZE;
            logger.info("Fetched {} {} items (total: {})", currentPage.size(), entityName, allItems.size());
        } while (!currentPage.isEmpty());

        return allItems;
    }

    private List<StudentDTO> getStudentsPage(int take, int skip) {
        String query = String.format("{ students(take: %d, skip: %d) { items { fullName guid firstName surname middleName department group course startYear degreeLevel degreeForm } } }", take, skip);
        return executeQuery(query, "students", StudentsResponse.class).getItems();
    }

    private List<EmployeeDTO> getEmployeesPage(int take, int skip) {
        String query = String.format("{ employees(take: %d, skip: %d) { items { fullName guid surname mail dateOfBirth employeeEmployments { jobTitle staffCategory employmentType subDivision subDivisionGuid jobState } } } }", take, skip);
        return executeQuery(query, "employees", EmployeesResponse.class).getItems();
    }

    public List<StudentDTO> getStudentsByGroup(String groupName) {
        return getAllItems((take, skip) -> getStudentsPageByGroup(groupName, take, skip), "students");
    }

    private List<StudentDTO> getStudentsPageByGroup(String groupName, int take, int skip) {
        String query = String.format("{ students(take: %d, skip: %d, where: { group: { eq: \"%s\" } }) { items { fullName guid firstName surname middleName department group course startYear degreeLevel degreeForm } } }",
                take, skip, groupName);
        return executeQuery(query, "students", StudentsResponse.class).getItems();
    }

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