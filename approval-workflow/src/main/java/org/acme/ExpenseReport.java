package org.acme;

import java.util.ArrayList;
import java.util.List;

public class ExpenseReport {
    private double amount;
    private String department;
    private String submittedBy;
    private List<String> approvalChain = new ArrayList<>();

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public List<String> getApprovalChain() {
        return approvalChain;
    }

    public void setApprovalChain(List<String> approvalChain) {
        this.approvalChain = approvalChain;
    }

    public void addApprovalStep(String approver) {
        this.approvalChain.add(approver);
    }
}