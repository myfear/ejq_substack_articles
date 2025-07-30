package com.example.dto;

import java.util.List;

import com.example.entity.Brand;
import com.example.entity.Dealership;

public record FilterOptionsDto(
        List<Brand> brands,
        List<Dealership> dealerships,
        List<String> colors,
        List<String> features) {
}