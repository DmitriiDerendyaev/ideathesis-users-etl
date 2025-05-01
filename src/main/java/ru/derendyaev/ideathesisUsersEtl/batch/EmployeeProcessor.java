package ru.derendyaev.ideathesisUsersEtl.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeeDTO;
import ru.derendyaev.ideathesisUsersEtl.mapper.EmployeeMapper;
import ru.derendyaev.ideathesisUsersEtl.model.Employee;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class EmployeeProcessor implements ItemProcessor<EmployeeDTO, Employee> {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeProcessor.class);
    private final EmployeeMapper employeeMapper;

    @Autowired
    public EmployeeProcessor(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public Employee process(EmployeeDTO item) {
        logger.info("Processing employee DTO: {}", item);
        Employee employee = employeeMapper.map(item);
        logger.info("Mapped to entity: {}", employee);
        return employee;
    }
}
