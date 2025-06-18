/**
 * Holds the form data and wizard progress for the application.
 * <p>
 * This class is used to store the state of the wizard, including the data entered
 * in each form step and the current step the user is on. It must implement
 * {@link Serializable} to ensure compatibility with Jackson serialization and caching mechanisms.
 * </p>
 *
 * <p>
 * The state includes:
 * <ul>
 *   <li>{@link AddressForm} - Data for the address step.</li>
 *   <li>{@link OrderForm} - Data for the order step.</li>
 *   <li>{@link AdditionalInfoForm} - Data for the additional information step.</li>
 *   <li>currentStep - The current step in the wizard process.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Forms are initialized to empty instances in the default constructor to avoid
 * {@link NullPointerException}s during usage.
 * </p>
 *
 * <p>
 * Note: There is no {@code reset()} method; state management is handled by creating or loading entities.
 * </p>
 */
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

    // No `reset()` here, as state is managed by creating/loading entities.
}