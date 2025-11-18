package org.acme.spy;

public class Agent {

    private String codeName;
    private String email;
    private String publicKeyArmored;

    public Agent() {
    }

    public Agent(String codeName, String email, String publicKeyArmored) {
        this.codeName = codeName;
        this.email = email;
        this.publicKeyArmored = publicKeyArmored;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPublicKeyArmored() {
        return publicKeyArmored;
    }

    public void setPublicKeyArmored(String publicKeyArmored) {
        this.publicKeyArmored = publicKeyArmored;
    }
}
