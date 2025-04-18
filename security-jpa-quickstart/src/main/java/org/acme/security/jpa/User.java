package org.acme.security.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.PasswordType;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
//import io.quarkus.elytron.security.common.BcryptUtil;

@Entity
@Table(name = "test_user")
@UserDefinition
public class User extends PanacheEntity {

    @Username
    public String username;

    @Password(PasswordType.CLEAR)
    public String password;

    @Roles
    public String role;

    public static void add(String username, String password, String role) {
        User user = new User();
        user.username = username;
        // For testing I am using clear text passwords. 
        // YOU should use Bcrypt!
        // user.password = BcryptUtil.bcryptHash(password);
        user.password = password;
        user.role = role;
        user.persist();
    }
}