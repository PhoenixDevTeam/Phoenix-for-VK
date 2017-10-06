package biz.dealnote.messenger.model;

/**
 * Created by Ruslan Kolbasa on 21.06.2017.
 * phoenix
 */
public class ContactInfo {

    private final int userId;

    private String descriprion;

    private String phone;

    private String email;

    public ContactInfo(int userId) {
        this.userId = userId;
    }

    public ContactInfo setDescriprion(String descriprion) {
        this.descriprion = descriprion;
        return this;
    }

    public ContactInfo setEmail(String email) {
        this.email = email;
        return this;
    }

    public ContactInfo setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDescriprion() {
        return descriprion;
    }

    public String getPhone() {
        return phone;
    }
}