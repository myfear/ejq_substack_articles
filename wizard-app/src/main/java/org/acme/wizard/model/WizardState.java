package org.acme.wizard.model;

import io.quarkus.logging.Log;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.SessionScoped;
import org.acme.wizard.forms.AddressForm;
import org.acme.wizard.forms.AdditionalInfoForm;
import org.acme.wizard.forms.OrderForm;

import java.io.Serializable;

@SessionScoped
@DefaultBean
public class WizardState implements Serializable {

    private AddressForm addressForm = new AddressForm();
    private OrderForm orderForm = new OrderForm();
    private AdditionalInfoForm additionalInfoForm = new AdditionalInfoForm();
    private int currentStep = 1;

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

    public void reset() {
        this.addressForm = new AddressForm();
        this.orderForm = new OrderForm();
        this.additionalInfoForm = new AdditionalInfoForm();
        this.currentStep = 1;
        Log.info("Wizard state reset.");
    }
}