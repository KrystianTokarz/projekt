package server.message.chaingOfResposibility.employees;

import server.fasade.EmployeesFacade;
import server.message.Message;
import server.message.chaingOfResposibility.AbstractChainElement;
import server.model.employee.Employee;
import server.model.message.MessageType;

import java.util.List;

public class SaveEmployeeElement extends AbstractChainElement {

    public SaveEmployeeElement(MessageType messageType){
        this.messageType = messageType;
    }

    @Override
    public  Message execute(Object objectFromClient) {
        EmployeesFacade facade = EmployeesFacade.getInstance();
        List<Employee> employees = facade.saveNewEmployee(objectFromClient);
        Message message = new Message.MessageBuilder(messageType)
                .object(employees)
                .build();
        return message;
    }
}
