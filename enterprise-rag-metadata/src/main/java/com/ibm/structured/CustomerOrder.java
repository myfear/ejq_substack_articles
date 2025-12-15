package com.ibm.structured;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class CustomerOrder extends PanacheEntity {

    public String userId;
    public String orderNumber;
    public String status;
    public Double totalAmount;

    public static CustomerOrder findByOrderAndUser(String orderNumber, String userId) {
        return find("orderNumber = ?1 and userId = ?2", orderNumber, userId).firstResult();
    }
}