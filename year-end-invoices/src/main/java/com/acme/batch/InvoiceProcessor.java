package com.acme.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.acme.domain.Invoice;
import com.acme.domain.Policy;

import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("com.acme.batch.InvoiceProcessor")
public class InvoiceProcessor implements ItemProcessor {

    @Inject
    JobContext jobCtx;

    @Override
    public Object processItem(Object item) {
        Policy p = (Policy) item;

        int year = Integer.parseInt(jobCtx.getProperties().getProperty("year"));
        BigDecimal taxRate = new BigDecimal(jobCtx.getProperties().getProperty("billing.tax.default-rate", "0.19"));

        BigDecimal annualNet = premiumFor(p);
        BigDecimal tax = annualNet.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gross = annualNet.add(tax);

        Invoice inv = new Invoice();
        inv.policy = p;
        inv.year = year;
        inv.netAmount = annualNet;
        inv.insuranceTax = tax;
        inv.grossAmount = gross;
        inv.dueDate = LocalDate.of(year + 1, 1, 15);
        inv.paymentFrequency = "ANNUAL";

        return inv;
    }

    private BigDecimal premiumFor(Policy p) {
        BigDecimal base = p.baseAnnualPremium;
        // Heuristics to mimic Regionalklassen/Typklassen/SF-Klasse:
        BigDecimal regionFactor = switch (String.valueOf(p.vehicle.regionalklasse)) {
            case "HH", "BE" -> new BigDecimal("1.10");
            case "BY", "BW" -> new BigDecimal("1.05");
            default -> BigDecimal.ONE;
        };
        BigDecimal typeFactor = switch (String.valueOf(p.vehicle.typklasse)) {
            case "SPORT" -> new BigDecimal("1.15");
            case "SUV" -> new BigDecimal("1.08");
            default -> BigDecimal.ONE;
        };
        BigDecimal sfBonus = new BigDecimal("0.90"); // 10% bonus for example
        return base.multiply(regionFactor).multiply(typeFactor).multiply(sfBonus).setScale(2, RoundingMode.HALF_UP);
    }
}
