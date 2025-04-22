package ru.derendyaev.ideathesisUsersEtl.batch;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import ru.derendyaev.ideathesisUsersEtl.client.GraphQLClient;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentsResponse;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StudentReader implements ItemReader<StudentDTO>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(StudentReader.class);

    private final GraphQLClient graphQLClient;
    private List<StudentDTO> studentList = new ArrayList<>();
    private int nextIndex = 0;

    @Autowired
    public StudentReader(GraphQLClient graphQLClient) {
        this.graphQLClient = graphQLClient;
    }

    @Override
    public void afterPropertiesSet() {
        logger.info("Initializing StudentReader...");
        try {
            StudentsResponse response = graphQLClient.getStudents();
            if (response != null && response.getItems() != null) {
                studentList = response.getItems();
                logger.info("Fetched {} students from GraphQL", studentList.size());
            } else {
                logger.warn("Received empty or null response from GraphQL");
            }
        } catch (Exception e) {
            logger.error("Error while fetching students from GraphQL", e);
            throw new RuntimeException("Failed to fetch students", e);
        }
    }

    @Override
    public StudentDTO read() {
        if (nextIndex < studentList.size()) {
            StudentDTO student = studentList.get(nextIndex++);
            logger.debug("Reading student {}", student);
            return student;
        } else {
            return null;
        }
    }
}