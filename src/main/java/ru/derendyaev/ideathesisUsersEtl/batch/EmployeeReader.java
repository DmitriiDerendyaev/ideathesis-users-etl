package ru.derendyaev.ideathesisUsersEtl.batch;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import ru.derendyaev.ideathesisUsersEtl.client.GraphQLClient;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeeDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeesResponse;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class EmployeeReader implements ItemReader<EmployeeDTO>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeReader.class);

    private final GraphQLClient graphQLClient;
    private List<EmployeeDTO> employeeList = new ArrayList<>();
    private int nextIndex = 0;

    @Autowired
    public EmployeeReader(GraphQLClient graphQLClient) {
        this.graphQLClient = graphQLClient;
    }

    @Override
    public void afterPropertiesSet() {
        logger.info("Initializing EmployeeReader...");
        try {
            EmployeesResponse response = graphQLClient.getEmployees();
            if (response != null && response.getItems() != null) {
                employeeList = response.getItems();
                logger.info("Fetched {} employees from GraphQL", employeeList.size());
            } else {
                logger.warn("Received empty or null employee list from GraphQL");
            }
        } catch (Exception e) {
            logger.error("Error while fetching employees from GraphQL", e);
            throw new RuntimeException("Failed to fetch employees", e);
        }
    }

    @Override
    public EmployeeDTO read() {
        if (nextIndex < employeeList.size()) {
            EmployeeDTO employee = employeeList.get(nextIndex++);
            logger.debug("Reading employee {}", employee);
            return employee;
        } else {
            return null;
        }
    }
}
