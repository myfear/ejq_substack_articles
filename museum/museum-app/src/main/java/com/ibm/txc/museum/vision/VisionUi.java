package com.ibm.txc.museum.vision;

import java.util.List;

import com.ibm.txc.museum.domain.Art;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class VisionUi {

    public List<Art> getList() {
        return Art.listAll();
    }
}
