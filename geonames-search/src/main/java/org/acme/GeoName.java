package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "geoname")
public class GeoName extends PanacheEntityBase {

    @Id
    public Integer id;

    @Column(length = 200)
    public String name;
    
    @Column(length = 200)
    public String asciiname;
    
    @Column(columnDefinition = "TEXT")
    public String alternatenames;
    
    @Column(precision = 10, scale = 7)
    public BigDecimal latitude;
    
    @Column(precision = 10, scale = 7)
    public BigDecimal longitude;
    
    @Column(name = "feature_class", length = 1)
    public String featureClass;
    
    @Column(name = "feature_code", length = 10)
    public String featureCode;
    
    @Column(name = "country_code", length = 2)
    public String countryCode;
    
    @Column(length = 200)
    public String cc2;
    
    @Column(name = "admin1_code", length = 20)
    public String admin1Code;
    
    @Column(name = "admin2_code", length = 80)
    public String admin2Code;
    
    @Column(name = "admin3_code", length = 20)
    public String admin3Code;
    
    @Column(name = "admin4_code", length = 20)
    public String admin4Code;
    
    public Long population;
    
    public Integer elevation;
    
    public Integer dem;
    
    @Column(length = 40)
    public String timezone;
    
    @Column(name = "modification_date")
    public LocalDate modificationDate;

    // Panache provides find(), list(), etc. methods for free!
}