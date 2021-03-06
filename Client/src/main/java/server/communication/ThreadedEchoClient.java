package server.communication;

import javafx.application.Platform;
import server.message.command.*;
import server.message.Message;
import server.message.command.distributor.*;
//import server.message.command.distributor.NotificationForDistributorListCommand;
import server.message.command.employees.AuthorizationCommand;
import server.message.command.employees.EmployeeListCommand;
import server.message.command.employees.OneEmployeeForAdministratorCommand;
import server.message.command.institutions.AllInstitutionListCommand;
import server.message.command.institutions.InstitutionsForNotificationCommand;
import server.message.command.institutions.LocalizationForNotificationCommand;
import server.message.command.institutions.OneInstitutionCommand;
import server.message.mediator.CommandMediator;
import server.message.mediator.DistributorCommandMediator;
import server.model.message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Main class (thread) for receive data from server and register all of command elements
 */
public class ThreadedEchoClient extends Thread {

    private Socket socket;
    private final int port = 6789;
    private ObjectInputStream streamInput;
    private ObjectOutputStream streamOutput;
    private CommandRegister commandRegister;
    private CommandInvoker commandInvoker;
    private Message messageFromServer;
    private DataStream dataSocket;
//    Logger logger = Logger.getLogger(this.getClass().getName());

    public Socket getSocket() {
        return socket;
    }

    public ThreadedEchoClient() {
        try {
            dataSocket = DataStream.getInstance();
            InetAddress address = InetAddress.getLocalHost();
            socket = new Socket(address, port);
            streamOutput = new ObjectOutputStream(socket.getOutputStream());
            streamInput = new ObjectInputStream(socket.getInputStream());
            dataSocket.setSocket(socket);
            dataSocket.setInputStream(streamInput);
            dataSocket.setOutputStream(streamOutput);
            createCommandRegister();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    private void createCommandRegister(){
        Command authorizationCommand = new AuthorizationCommand();
        Command employeeListCommand = new EmployeeListCommand();
        Command oneEmployeeCommand = new OneEmployeeForAdministratorCommand();

        Command allInstitutionListCommand = new AllInstitutionListCommand();
        Command oneInstitutionCommand = new OneInstitutionCommand();
        Command oneEmployeeForDistributor = new OneEmployeeForDistributorCommand();
        Command editOneEmployeeData = new EditEmployeeDataCommand();
        Command localizationForNotificationCommand = new LocalizationForNotificationCommand();
        Command notificationListForAllApplicationCommand = new NotificationForAllApplicationListCommand();
        Command institutionForNotificationCommand = new InstitutionsForNotificationCommand();
        Command saveFirstNotificationCommand = new SaveFirstNotificationCommand();
        Command saveSecondNotificationCommand = new SaveSecondNotificationCommand();


        commandRegister = new CommandRegister();
        commandRegister.addCommand(MessageType.AUTHORIZATION,authorizationCommand);
        commandRegister.addCommand(MessageType.EMPLOYEE_LIST,employeeListCommand);
        commandRegister.addCommand(MessageType.SEND_EMPLOYEE,oneEmployeeCommand);
        commandRegister.addCommand(MessageType.ALL_INSTITUTION_LIST,allInstitutionListCommand);
        commandRegister.addCommand(MessageType.SEND_INSTITUTION,oneInstitutionCommand);
        commandRegister.addCommand(MessageType.SEND_EMPLOYEE_DISTRIBUTOR,oneEmployeeForDistributor);
        commandRegister.addCommand(MessageType.EDIT_EMPLOYEE_DISTRIBUTOR,editOneEmployeeData);
        commandRegister.addCommand(MessageType.SEND_FOR_LOCALIZATION_FOR_NOTIFICATION,localizationForNotificationCommand);
        commandRegister.addCommand(MessageType.SEND_NOTIFICATION,notificationListForAllApplicationCommand);
        commandRegister.addCommand(MessageType.SEND_FOR_INSTITUTION_FOR_LOCALIZATION,institutionForNotificationCommand);
        commandRegister.addCommand(MessageType.SAVE_NEW_FIRST_NOTIFICATION,saveFirstNotificationCommand);
        commandRegister.addCommand(MessageType.SAVE_NEW_SECOND_NOTIFICATION,saveSecondNotificationCommand);
    }

    @Override
        public void run()  {
        commandInvoker = new CommandInvoker(commandRegister);
        try {
            while ((messageFromServer = (Message) streamInput.readObject()) != null) {

                processMessage(messageFromServer);
            }
        } catch (ClassNotFoundException e ) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            this.interrupt();
        } catch (IOException e) {
            try {
                streamInput.close();
                streamInput.close();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
            Platform.exit();
    }

    private void processMessage(Message messageFromServer) {
        commandInvoker.takeCommand(messageFromServer);
    }
}
