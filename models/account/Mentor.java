package models.account;


public class Mentor extends Codecooler {

    private Integer classId;

    public Menotr(Integer id, Login login, Password password, Email email, String name, String surname) {
        super(id, login, password, email, name, surname);
        this.classId = null;
    }

    public Menotr(Integer id, Login login, Password password, Email email, String name, String surname. Integer classId) {
        super(id, login, password, email, name, surname);
        this.classId = classId;
    }

    public Integer getClassId() {
        return this.classId;
    }


}
