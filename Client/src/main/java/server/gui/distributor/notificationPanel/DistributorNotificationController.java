package server.gui.distributor.notificationPanel;

import com.sun.deploy.xml.XMLable;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import server.gui.administrator.institutionManagement.InstitutionForTable;
import server.gui.distributor.notificationPanel.decorator.*;
import server.gui.distributor.receivingPanel.CallerForTable;
import server.message.mediator.DistributorCommandMediator;
import server.model.employee.Employee;
import server.model.institution.Institution;
import server.model.institution.InstitutionType;
import server.model.localization.Locality;
import server.model.localization.Street;
import server.model.message.FirstMessageWithNotification;
import server.model.message.SecondMessageWithNotification;
import server.model.notification.AccidentType;


import java.net.URL;
import java.util.*;

public class DistributorNotificationController implements Initializable {

    private DistributorCommandMediator commandMediator = DistributorCommandMediator.getInstance();


    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField callerNumber;

    @FXML
    private ListView accidentListView;

    @FXML
    private ChoiceBox provinceChoiceBox;
    @FXML
    private ChoiceBox localityChoiceBox;
    @FXML
    private ChoiceBox streetChoiceBox;
    @FXML
    private ChoiceBox fireBrigadeChoiceBox;
    @FXML
    private ChoiceBox policeChoiceBox;
    @FXML
    private ChoiceBox emergencyChoiceBox;

    @FXML
    private TextField callerFirstNameTextField;
    @FXML
    private TextField callerLastNameTextField;

    @FXML
    private Button firstNotificationButton;

    @FXML
    private TextArea notationTextArea;
    @FXML
    private TextField numberOfVictimsTextField;

    @FXML
    private Button  secondNotificationButton;

    @FXML
    private CheckBox helicopterBox;

    @FXML
    private CheckBox boatBox;

    @FXML
    private Label notificationLabel;
    @FXML
    private Label fireBrigadeLabel;
    @FXML
    private Label policeLabel;
    @FXML
    private Label emergencyLabel;

    private ResourceBundle resourceBundle;





    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        loadInternationalizationNames();
        int statusOfController = commandMediator.getStatusOfController();
        commandMediator.sendForInstitutionDataToEdit();
        String localization;
        if(statusOfController==0) {
            CallerForTable callerData = commandMediator.getCallerData();
            callerNumber.setText(callerData.getCallerPhoneNumber());
            localization = callerData.getCallerLocalization();
        }else{
            localization = null;
        }
        AccidentType[] enumConstants = AccidentType.INNE.getDeclaringClass().getEnumConstants();
        accidentListView.getItems().setAll(enumConstants);

        Task< List<String>> notificationTask = new Task< List<String>>(){

            @Override
            protected  List<String> call() throws Exception {
                List<String> provinceLists = null;
                do {
                    Thread.sleep(1000);
                    provinceLists = commandMediator.returnAllProvince();

                }while (provinceLists == null);
                return  provinceLists;
            }
        };


        notificationTask.setOnSucceeded(e-> {

                provinceChoiceBox.getItems().addAll(notificationTask.getValue());
            if(statusOfController==0) {
                String provinceForLocality = commandMediator.getProvinceForLocality(localization);
                provinceChoiceBox.getSelectionModel().select(provinceForLocality);

                List<Locality> localityForSelectedProvince = commandMediator.getLocalityForSelectedProvince(provinceChoiceBox.getValue().toString());


                localityChoiceBox.getSelectionModel().select(localization);
            }else{
                provinceChoiceBox.getSelectionModel().select(0);
                //localityChoiceBox.getSelectionModel().select(0);
            }



        });

        new Thread(notificationTask).start();

            provinceChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(!newValue.equals(oldValue)){
                    if(localityChoiceBox.getItems()!=null)
                        localityChoiceBox.getItems().clear();
                    List<Locality> localityForSelectedProvince = commandMediator.getLocalityForSelectedProvince((String) newValue);
                    for (Locality locality: localityForSelectedProvince) {
                        localityChoiceBox.getItems().add(locality.getLocality());
                    }
                    streetChoiceBox.getItems().clear();
                    streetChoiceBox.setDisable(true);
                    fireBrigadeChoiceBox.setDisable(true);
                    emergencyChoiceBox.setDisable(true);
                    policeChoiceBox.setDisable(true);
                }
            });

            localityChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(provinceChoiceBox.getValue() != null && newValue != null) {
                    List<Street> streetForSelectedLocality = commandMediator.getStreetForSelectedLocality((String) provinceChoiceBox.getValue(), (String) newValue);
                    streetChoiceBox.getItems().clear();
                    streetChoiceBox.setDisable(false);
                    for (Street street : streetForSelectedLocality) {
                        streetChoiceBox.getItems().add(street.getStreet() + " " + street.getSpecialNumber());
                    }
                }
                fireBrigadeChoiceBox.getItems().clear();
                emergencyChoiceBox.getItems().clear();
                policeChoiceBox.getItems().clear();
                fireBrigadeChoiceBox.setDisable(false);
                emergencyChoiceBox.setDisable(false);
                policeChoiceBox.setDisable(false);

                List<Institution> rightInstitutionLForFireBrigade = commandMediator.getRightInstitutionListForProvinceAndLocality(InstitutionType.FIRE_BRIGADE,(String) newValue);
                List<Institution> rightInstitutionListForPolice = commandMediator.getRightInstitutionListForProvinceAndLocality(InstitutionType.POLICE,(String) newValue);
                List<Institution> rightInstitutionListForEmergency = commandMediator.getRightInstitutionListForProvinceAndLocality(InstitutionType.EMERGENCY,(String) newValue);
                fireBrigadeChoiceBox.getItems().add("No selected");
                emergencyChoiceBox.getItems().add("No selected");
                policeChoiceBox.getItems().add("No selected");

                for (Institution institution :rightInstitutionLForFireBrigade) {
                    fireBrigadeChoiceBox.getItems().add(institution.getName());
                }
                for (Institution institution :rightInstitutionListForEmergency) {
                    emergencyChoiceBox.getItems().add(institution.getName());
                }
                for (Institution institution :rightInstitutionListForPolice) {
                    policeChoiceBox.getItems().add(institution.getName());
                }
                fireBrigadeChoiceBox.getSelectionModel().select(0);
                emergencyChoiceBox.getSelectionModel().select(0);
                policeChoiceBox.getSelectionModel().select(0);
             });

        fireBrigadeChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(localityChoiceBox.getValue()!=null) {

            }
        });
    }

    public void loadInternationalizationNames(){
        notificationLabel.setText(resourceBundle.getString("notification"));
        fireBrigadeLabel.setText(resourceBundle.getString("fire_brigade"));
        policeLabel.setText(resourceBundle.getString("police"));
        emergencyLabel.setText(resourceBundle.getString("emergency"));
        firstNotificationButton.setText(resourceBundle.getString("send_first_notification"));
        secondNotificationButton.setText(resourceBundle.getString("send_second_notification"));

    }

    public void showEmptyFieldPopup(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("ERROR");
        alert.setContentText("You leave empty field     ");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void showErrorInput(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("ERROR");
        alert.setContentText("You add bad data into number of victims ");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void showAdditionInformation(String value){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("INFORMATION");
        alert.setContentText(value);
        alert.setHeaderText(null);
        alert.showAndWait();
    }



    @FXML
    public void sendSecondNotification(){
        String pattern = "\\d+";
        if(numberOfVictimsTextField.getText()!="" && accidentListView.getSelectionModel().getSelectedIndex()!=-1) {
            if(!numberOfVictimsTextField.getText().matches(pattern))
                showErrorInput();
            else {
                boolean selectedHelicopterBox = helicopterBox.isSelected();
                boolean selectedBoatBox = boatBox.isSelected();
                boolean reportBoat = false;
                boolean reportHelicopter = false;
                String street = null;
                if (streetChoiceBox.getSelectionModel().getSelectedItem() != null)
                    street = (String) streetChoiceBox.getSelectionModel().getSelectedItem();
                String province = (String) provinceChoiceBox.getSelectionModel().getSelectedItem();
                String locality = (String) localityChoiceBox.getSelectionModel().getSelectedItem();
                AccidentType accidentType = (AccidentType) accidentListView.getSelectionModel().getSelectedItem();
                AdditionAction addictionReport = null;
                if(selectedHelicopterBox == true && selectedBoatBox == false) {
                    addictionReport = new HelicopterDecorator(new AddictionReport(province, locality, street, accidentType));
                    reportHelicopter = true;
                }
                else if(selectedHelicopterBox == false && selectedBoatBox == true) {
                    addictionReport = new BoatDecorator(new AddictionReport(province, locality, street, accidentType));
                    reportBoat = true;
                }else if(selectedHelicopterBox == true && selectedBoatBox == true) {
                    addictionReport = new BoatDecorator(new HelicopterDecorator(new AddictionReport(province, locality, street, accidentType)));
                    reportHelicopter = true;
                    reportBoat = true;
                }
                if(addictionReport  != null) {
                    String valueToInformDistributor = addictionReport.report();
                    showAdditionInformation(valueToInformDistributor);
                }

                SecondMessageWithNotification secondMessageWithNotification = new SecondMessageWithNotification();
                secondMessageWithNotification.setNumberOfVictims(Integer.parseInt(numberOfVictimsTextField.getText()));
                secondMessageWithNotification.setAccidentType((AccidentType) accidentListView.getSelectionModel().getSelectedItem());
                secondMessageWithNotification.setNotations(notationTextArea.getText());
                secondMessageWithNotification.setReportBoat(reportBoat);
                secondMessageWithNotification.setReportHelicopter(reportHelicopter);
                commandMediator.saveSecondNotification(secondMessageWithNotification);
                commandMediator.startThread();
                Stage stage = (Stage) secondNotificationButton.getScene().getWindow();
                stage.close();
            }
        }
    }

    @FXML
    public void sendFirstNotification(){

        if(localityChoiceBox.getSelectionModel().getSelectedIndex()!=-1 && streetChoiceBox.getSelectionModel().getSelectedIndex()!=-1
                &&(fireBrigadeChoiceBox.getSelectionModel().getSelectedIndex()!=0
                    || policeChoiceBox.getSelectionModel().getSelectedIndex()!=0 || emergencyChoiceBox.getSelectionModel().getSelectedIndex()!=0 )) {
            commandMediator.setInstitutionChoiceBoxForService(policeChoiceBox, emergencyChoiceBox, fireBrigadeChoiceBox);

            FirstMessageWithNotification messageWithNotification = new FirstMessageWithNotification();

            List<String> institutionList = new ArrayList<>();

            String selectedEmergency =  (String) emergencyChoiceBox.getSelectionModel().getSelectedItem();
            if(!selectedEmergency.equals("No selected"))
                institutionList.add(selectedEmergency);
            String selectedFireBrigade =  (String) fireBrigadeChoiceBox.getSelectionModel().getSelectedItem();
            if(!selectedFireBrigade.equals("No selected"))
                institutionList.add(selectedFireBrigade);
            String selectedPolice =  (String) policeChoiceBox.getSelectionModel().getSelectedItem();
            if(!selectedPolice.equals("No selected"))
                institutionList.add(selectedPolice);

            String firstName = callerFirstNameTextField.getText();
            String lastName = callerLastNameTextField.getText();
            String number = callerNumber.getText();
            String province = (String) provinceChoiceBox.getSelectionModel().getSelectedItem();
            String locality = (String) localityChoiceBox.getSelectionModel().getSelectedItem();
            String street = null;
            if (streetChoiceBox.getSelectionModel().getSelectedItem() != null)
                street = (String) streetChoiceBox.getSelectionModel().getSelectedItem();

            String[] split = street.split("\\s+");
            String streetName = split[0];
            String streetNumber = split[1];
            Employee employee = new Employee();
            employee.setEmail(commandMediator.getUserEmail());
            employee.setFirstName(commandMediator.getUserFirstName());
            employee.setLastName(commandMediator.getUserLastName());


            messageWithNotification.setCallerFirstNameTextField(firstName);
            messageWithNotification.setCallerLastNameTextField(lastName);
            messageWithNotification.setEmployee(employee);
            messageWithNotification.setCallerNumber(number);
            messageWithNotification.setProvince(province);
            messageWithNotification.setLocality(locality);
            messageWithNotification.setStreetName(streetName);
            messageWithNotification.setStreetNumber(streetNumber);
            messageWithNotification.setInstitutionNotification(institutionList);

            commandMediator.saveFirstNotification(messageWithNotification);
            provinceChoiceBox.setDisable(true);
            localityChoiceBox.setDisable(true);
            streetChoiceBox.setDisable(true);
            callerFirstNameTextField.setDisable(true);
            callerLastNameTextField.setDisable(true);
            callerNumber.setDisable(true);
            emergencyChoiceBox.setDisable(true);
            policeChoiceBox.setDisable(true);
            fireBrigadeChoiceBox.setDisable(true);
            firstNotificationButton.setDisable(true);
            Stage stage = (Stage) firstNotificationButton.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                event.consume();
            });
        }else{
            showEmptyFieldPopup();
        }
    }
}
