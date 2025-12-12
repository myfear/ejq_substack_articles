package com.pixel.service;

import java.awt.image.BufferedImage;
import java.util.List;

import com.pixel.filter.ImageFilter;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FilterChainService {

    public BufferedImage applyChain(BufferedImage input, List<ImageFilter> filters) {
        BufferedImage current = input;
        for (ImageFilter f : filters) {
            current = f.apply(current);
        }
        return current;
    }
}