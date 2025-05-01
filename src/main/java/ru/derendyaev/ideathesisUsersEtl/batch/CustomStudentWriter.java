package ru.derendyaev.ideathesisUsersEtl.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.derendyaev.ideathesisUsersEtl.model.Student;
import ru.derendyaev.ideathesisUsersEtl.repository.StudentRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomStudentWriter implements ItemWriter<Student> {

    private final StudentRepository studentRepository;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void write(Chunk<? extends Student> chunk) throws Exception {
        new TransactionTemplate(transactionManager).execute(status -> {
            chunk.forEach(studentRepository::save);
            return null;
        });
    }
}