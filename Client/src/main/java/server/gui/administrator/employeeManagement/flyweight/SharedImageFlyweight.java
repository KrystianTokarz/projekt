package server.gui.administrator.employeeManagement.flyweight;

import server.model.employee.EmployeeImage;

public class SharedImageFlyweight  implements ImageFlyweight{

    private byte[] employeeImage;

    public SharedImageFlyweight(byte[] employeeImage){
        this.employeeImage = employeeImage;
    }

    @Override
    public byte[] getCorrectEmployeeImage() {
        return employeeImage;
    }
}
