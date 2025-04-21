package ru.derendyaev.ideathesisUsersEtl.batch;

import ru.derendyaev.ideathesisUsersEtl.dto.EmployeeDTO;
import ru.derendyaev.ideathesisUsersEtl.mapper.EmployeeMapper;
import ru.derendyaev.ideathesisUsersEtl.model.Employee;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class EmployeeProcessor implements ItemProcessor<EmployeeDTO, Employee> {

    private final EmployeeMapper employeeMapper;

    @Autowired
    public EmployeeProcessor(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public Employee process(EmployeeDTO item) throws Exception {
        return employeeMapper.map(item);
    }
}
