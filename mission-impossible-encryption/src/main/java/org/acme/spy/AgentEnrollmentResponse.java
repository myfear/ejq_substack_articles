package org.acme.spy;

public class AgentEnrollmentResponse {
    public String codeName;
    public String email;
    public String publicKeyArmored;
    public String privateKeyArmored;

    public AgentEnrollmentResponse() {
    }

    public AgentEnrollmentResponse(String codeName,
            String email,
            String publicKeyArmored,
            String privateKeyArmored) {
        this.codeName = codeName;
        this.email = email;
        this.publicKeyArmored = publicKeyArmored;
        this.privateKeyArmored = privateKeyArmored;
    }
}
