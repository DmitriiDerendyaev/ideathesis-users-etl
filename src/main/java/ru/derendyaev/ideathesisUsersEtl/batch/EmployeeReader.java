package ru.derendyaev.ideathesisUsersEtl.batch;

import ru.derendyaev.ideathesisUsersEtl.client.GraphQLClient;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeeDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeesResponse;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeReader implements ItemReader<EmployeeDTO> {

    private final GraphQLClient graphQLClient;
    private List<EmployeeDTO> employeeList;
    private int nextIndex;

    @Autowired
    public EmployeeReader(GraphQLClient graphQLClient) {
        this.graphQLClient = graphQLClient;
        EmployeesResponse response = graphQLClient.getEmployees();
        this.employeeList = response.getItems();
        this.nextIndex = 0;
    }

    @Override
    public EmployeeDTO read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (nextIndex < employeeList.size()) {
            return employeeList.get(nextIndex++);
        } else {
            return null;
        }
    }
}
