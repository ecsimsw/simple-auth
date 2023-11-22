package ecsimsw.auth.sample;

import ecsimsw.auth.anotations.TokenKey;

public class MyLoginPayload {

    @TokenKey
    private String name;
    private String email;

    public MyLoginPayload() {
    }

    public MyLoginPayload(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
