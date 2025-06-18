package org.acme.wizard.model;

import org.acme.wizard.forms.AddressForm;
import org.acme.wizard.forms.AdditionalInfoForm;
import org.acme.wizard.forms.OrderForm;
import java.io.Serializable;

public class WizardState implements Serializable {

    private AddressForm addressForm;
    private OrderForm orderForm;
    private AdditionalInfoForm additionalInfoForm;
    private int currentStep;

    public WizardState() {
        // Initialize with empty forms to avoid NullPointerExceptions
        this.addressForm = new AddressForm();
        this.orderForm = new OrderForm();
        this.additionalInfoForm = new AdditionalInfoForm();
        this.currentStep = 1;
    }

    // Getters and Setters
    public AddressForm getAddressForm() {
        return addressForm;
    }

    public void setAddressForm(AddressForm addressForm) {
        this.addressForm = addressForm;
    }

    public OrderForm getOrderForm() {
        return orderForm;
    }

    public void setOrderForm(OrderForm orderForm) {
        this.orderForm = orderForm;
    }

    public AdditionalInfoForm getAdditionalInfoForm() {
        return additionalInfoForm;
    }

    public void setAdditionalInfoForm(AdditionalInfoForm additionalInfoForm) {
        this.additionalInfoForm = additionalInfoForm;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

}