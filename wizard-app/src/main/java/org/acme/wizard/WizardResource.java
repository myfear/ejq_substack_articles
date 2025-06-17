package org.acme.wizard;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

import org.acme.wizard.forms.AdditionalInfoForm;
import org.acme.wizard.forms.AddressForm;
import org.acme.wizard.forms.OrderForm;
import org.acme.wizard.model.WizardState;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/wizard")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Important for form submissions
@SessionScoped
public class WizardResource {

    @Inject
    WizardState wizardState; // Our session-scoped state

    @Inject
    Validator validator; // For bean validation



    /**
     * Define Qute templates. Qute will automatically find these in
     * src/main/resources/templates/wizard/.
     */
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance step1(AddressForm form, Set<String> errors, int currentStep,
                int totalSteps);

        public static native TemplateInstance step2(OrderForm form, Set<String> errors, int currentStep,
                int totalSteps);

        public static native TemplateInstance step3(AdditionalInfoForm form, Set<String> errors, int currentStep,
                int totalSteps);

        public static native TemplateInstance step4(WizardState state, int currentStep, int totalSteps);

        public static native TemplateInstance success();
    }

    private static final int TOTAL_STEPS = 4;

    @GET
    @Path("/start")
    public TemplateInstance startWizard() {
    
        wizardState.reset(); // Reset state for a new wizard flow
        return Templates.step1(wizardState.getAddressForm(), null, 1, TOTAL_STEPS);
    }

    @GET
    @Path("/step/{stepNumber}")
    public Response showStep(@PathParam("stepNumber") int stepNumber) { // Changed return type to Response
        if (stepNumber < 1 || stepNumber > TOTAL_STEPS) {
            // Invalid step number, redirect to start
            return Response.temporaryRedirect(URI.create("/wizard/start")).build();
        }

        wizardState.setCurrentStep(stepNumber);
        // Depending on the step number, return the appropriate template
        switch (stepNumber) {
            case 1:
                return Response.ok(Templates.step1(wizardState.getAddressForm(), null, 1, TOTAL_STEPS)).build();
            case 2:
                return Response.ok(Templates.step2(wizardState.getOrderForm(), null, 2, TOTAL_STEPS)).build();
            case 3:
                return Response.ok(Templates.step3(wizardState.getAdditionalInfoForm(), null, 3, TOTAL_STEPS)).build();
            case 4:
                return Response.ok(Templates.step4(wizardState, 4, TOTAL_STEPS)).build();
            default:
                // This case should ideally be caught by the initial if condition,
                // but as a fallback, redirecting is safe.
                return Response.temporaryRedirect(URI.create("/wizard/start")).build();
        }
    }

    @POST
    @Path("/step1")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance processStep1(@BeanParam AddressForm form) {
        Set<ConstraintViolation<AddressForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            Set<String> errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            return Templates.step1(form, errors, 1, TOTAL_STEPS);
        }
        wizardState.setAddressForm(form);
        wizardState.setCurrentStep(2);
        return Templates.step2(wizardState.getOrderForm(), null, 2, TOTAL_STEPS);
    }

    @POST
    @Path("/step2")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance processStep2(@BeanParam OrderForm form) {
        Set<ConstraintViolation<OrderForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            Set<String> errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            return Templates.step2(form, errors, 2, TOTAL_STEPS);
        }
        wizardState.setOrderForm(form);
        wizardState.setCurrentStep(3);
        return Templates.step3(wizardState.getAdditionalInfoForm(), null, 3, TOTAL_STEPS);
    }

    @POST
    @Path("/step3")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance processStep3(@BeanParam AdditionalInfoForm form) {
        Set<ConstraintViolation<AdditionalInfoForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            Set<String> errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            return Templates.step3(form, errors, 3, TOTAL_STEPS);
        }
        wizardState.setAdditionalInfoForm(form);
        wizardState.setCurrentStep(4);
        return Templates.step4(wizardState, 4, TOTAL_STEPS);
    }

    @POST
    @Path("/submit")
    public TemplateInstance submitWizard() {
        // Here you would typically process the complete wizardState
        // e.g., save to database, send email, etc.
        System.out.println("Wizard data submitted:");
        System.out.println(
                "Address: " + wizardState.getAddressForm().getStreet() + ", " + wizardState.getAddressForm().getCity());
        System.out.println("Order: " + wizardState.getOrderForm().getQuantity() + " x "
                + wizardState.getOrderForm().getProductName());
        System.out.println("Comments: " + wizardState.getAdditionalInfoForm().getComments());

        wizardState.reset(); // Clear session data after successful submission
        return Templates.success();
    }
}