package ru.derendyaev.ideathesisUsersEtl.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentDTO;
import ru.derendyaev.ideathesisUsersEtl.mapper.StudentMapper;
import ru.derendyaev.ideathesisUsersEtl.model.Student;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class StudentProcessor implements ItemProcessor<StudentDTO, Student> {

    private static final Logger logger = LoggerFactory.getLogger(StudentProcessor.class);
    private final StudentMapper studentMapper;

    @Autowired
    public StudentProcessor(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public Student process(StudentDTO item) {
        logger.info("Processing student DTO: {}", item);
        Student student = studentMapper.map(item);
        logger.info("Mapped to entity: {}", student);
        return student;
    }
}