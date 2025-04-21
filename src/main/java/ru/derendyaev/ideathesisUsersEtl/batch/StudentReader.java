package ru.derendyaev.ideathesisUsersEtl.batch;

import ru.derendyaev.ideathesisUsersEtl.client.GraphQLClient;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentsResponse;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class StudentReader implements ItemReader<StudentDTO> {

    private final GraphQLClient graphQLClient;
    private List<StudentDTO> studentList;
    private int nextIndex;

    @Autowired
    public StudentReader(GraphQLClient graphQLClient) {
        this.graphQLClient = graphQLClient;
        StudentsResponse response = graphQLClient.getStudents();
        this.studentList = response.getItems();
        this.nextIndex = 0;
    }

    @Override
    public StudentDTO read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (nextIndex < studentList.size()) {
            return studentList.get(nextIndex++);
        } else {
            return null;
        }
    }
}
