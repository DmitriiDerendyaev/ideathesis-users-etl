package ru.derendyaev.ideathesisUsersEtl.batch;

import ru.derendyaev.ideathesisUsersEtl.dto.StudentDTO;
import ru.derendyaev.ideathesisUsersEtl.mapper.StudentMapper;
import ru.derendyaev.ideathesisUsersEtl.model.Student;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class StudentProcessor implements ItemProcessor<StudentDTO, Student> {

    private final StudentMapper studentMapper;

    @Autowired
    public StudentProcessor(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public Student process(StudentDTO item) throws Exception {
        return studentMapper.map(item);
    }
}
