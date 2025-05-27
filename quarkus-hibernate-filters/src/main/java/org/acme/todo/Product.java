package org.acme.todo;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
@FilterDef(name = "deletedProductFilter", parameters = @ParamDef(name = "isDeleted", type = boolean.class))
@Filter(name = "deletedProductFilter", condition = "deleted = :isDeleted")
public class Product extends PanacheEntity {
    public String name;
    public double price;
    public boolean deleted = false;

    public Product() {}
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
}