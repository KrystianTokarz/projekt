package server.repository;

import server.model.employee.Employee;
import server.model.institution.Institution;
import server.model.localization.Locality;
import server.model.localization.Province;
import server.model.localization.Street;
import server.model.message.MessageWithNotification;
import server.model.notification.Notification;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class NotificationRepository for CRUD operation on Notification Entity (singleton)
 */
public class NotificationRepository extends Repository {

    private NotificationRepository() {
    }

    private static NotificationRepository instance = null;

    public static NotificationRepository getInstance() {
        if (instance == null) {
            instance = new NotificationRepository();
        }
        return instance;
    }


    public Notification findById(Long id) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Notification> criteriaQuery = criteriaBuilder.createQuery(Notification.class);
        Root<Notification> notification = criteriaQuery.from(Notification.class);
        criteriaQuery.select(notification).where(notification.get("id").in(id));
        Notification singleResult= entityManager.createQuery(criteriaQuery).getSingleResult();

        return  singleResult;
    }

    public List<Notification> findNotificationForDistributor(Employee employee) {
        EmployeeRepository employeeRepository = EmployeeRepository.getInstance();
        Employee employeeInDatabase = employeeRepository.findEmployeeByFirstAndLastNameAndMail(employee);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Notification> criteriaQuery = criteriaBuilder.createQuery(Notification.class);
        Root<Notification> notificationInDatabase = criteriaQuery.from(Notification.class);
        criteriaQuery.select(notificationInDatabase).where(notificationInDatabase.get("employee").in(employeeInDatabase));
        List<Notification> notificationListInDatabase;
        try {
            List<Notification> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            notificationListInDatabase = resultList;
        }
        catch (NoResultException e){
            notificationListInDatabase = null;
        }
        entityManager.clear();
        return  notificationListInDatabase;
    }

    public Map<String,List<Notification>> findAllNotification(Employee employee) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Notification> criteriaQuery = criteriaBuilder.createQuery(Notification.class);
        Root<Notification> notificationInDatabase = criteriaQuery.from(Notification.class);
        criteriaQuery.select(notificationInDatabase);
        List<Notification> resultList = entityManager.createQuery(criteriaQuery).getResultList();
        entityManager.clear();
        Map<String, List<Notification>> mapsForClient = new HashMap<>();
        mapsForClient.put("forUser", findNotificationForDistributor(employee));
        mapsForClient.put("forAll", resultList);

        return mapsForClient;

    }

    public Long saveFirstNotification(MessageWithNotification messageWithNotification) {
        EmployeeRepository employeeRepository = EmployeeRepository.getInstance();
        Employee employeeInDatabase = employeeRepository.findEmployeeByFirstAndLastNameAndMail(messageWithNotification.getEmployee());
        LocalizationRepository localizationRepository = LocalizationRepository.getInstance();
        Locality localityInDatabase = localizationRepository.findLocality(messageWithNotification.getLocality());
        Street streetInDatabase = localizationRepository.findStreet(messageWithNotification.getStreetName(),messageWithNotification.getStreetNumber());
        Province provinceInDatabase = localizationRepository.findProvince(messageWithNotification.getProvince());



        InstitutionRepository institutionRepository = InstitutionRepository.getInstance();
        List<String> institutions = messageWithNotification.getInstitutionNotification();

        List<Institution> institutionsList = new ArrayList<>();

        for (String institutionName:institutions) {
            Institution institution = institutionRepository.findInstitutionByName(institutionName);
            institutionsList.add(institution);

        }

//        for (Institution institution:institutionsList) {
//            institution.set
//        }

        Notification notification = new Notification();
        notification.setStatus(1);
        notification.setInstitutions(institutionsList);
        notification.setCallerFirstName(messageWithNotification.getCallerFirstNameTextField());
        notification.setCallerLastName(messageWithNotification.getCallerLastNameTextField());
        notification.setCallerPhoneNumber(messageWithNotification.getCallerNumber());
        notification.setEmployee(employeeInDatabase);
        notification.setStreet(streetInDatabase);
        notification.setLocality(localityInDatabase);
        notification.setProvince(provinceInDatabase);


        entityManager.getTransaction().begin();
        entityManager.merge(notification);
        entityManager.getTransaction().commit();
        entityManager.clear();


        return 1L;
    }
}