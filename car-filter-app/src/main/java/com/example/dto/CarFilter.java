package com.example.dto;

import java.math.BigDecimal;
import java.util.Set;

public class CarFilter {
    public Set<Long> brandIds;
    public Set<Long> dealershipIds;
    public Set<String> colors;
    public Set<String> features;
    public Integer minYear;
    public Integer maxYear;
    public BigDecimal minPrice;
    public BigDecimal maxPrice;
}