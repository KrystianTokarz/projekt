package server.message.chaingOfResponsibility.employees;

import server.fasade.EmployeesFacade;
import server.message.Message;
import server.message.chaingOfResponsibility.AbstractChainElement;
import server.model.employee.Employee;
import server.model.message.MessageType;

/**
 * Chain of responsibility pattern element
 */
public class OneEmployeeElement extends AbstractChainElement {

    public OneEmployeeElement(MessageType messageType){
        this.messageType = messageType;
    }

    @Override
    public Message execute(Object objectFromClient) {
        EmployeesFacade facade = EmployeesFacade.getInstance();
        Employee employeeByFirstAndLastNameAndMail = facade.findEmployeeByFirstAndLastNameAndMail(objectFromClient);
        Message message = new Message.MessageBuilder(messageType)
                .object(employeeByFirstAndLastNameAndMail)
                .build();
        return message;
    }
}
