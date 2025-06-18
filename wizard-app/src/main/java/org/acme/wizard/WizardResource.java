package org.acme.wizard;

import java.util.Set;
import java.util.stream.Collectors;

import org.acme.wizard.forms.AdditionalInfoForm;
import org.acme.wizard.forms.AddressForm;
import org.acme.wizard.forms.OrderForm;
import org.acme.wizard.model.WizardState;
import org.acme.wizard.store.WizardStateStore;
import org.jboss.resteasy.reactive.RestForm;

import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
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

@Path("/wizard")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Important for form submissions
public class WizardResource {

    @Inject
    WizardStateStore wizardStateStore; // Our DB-backed state store

    @Inject
    Validator validator; // For bean validation

    /**
     * Define Qute templates. Qute will automatically find these in
     * src/main/resources/templates/WizardResource/.
     */
    @CheckedTemplate
    public static class Templates {
        // Now passing the wizardId string to templates for the hidden field
        public static native TemplateInstance step1(AddressForm form, Set<String> errors, int currentStep,
                int totalSteps, String wizardId);

        public static native TemplateInstance step2(OrderForm form, Set<String> errors, int currentStep, int totalSteps,
                String wizardId);

        public static native TemplateInstance step3(AdditionalInfoForm form, Set<String> errors, int currentStep,
                int totalSteps, String wizardId);

        public static native TemplateInstance step4(WizardState state, int currentStep, int totalSteps,
                String wizardId);

        public static native TemplateInstance success();

        public static native TemplateInstance error();
    }

    private static final int TOTAL_STEPS = 4;

    @GET
    @Path("/start")
    public Uni<TemplateInstance> startWizard() {
        // Create a new empty state object
        WizardState newState = new WizardState();
        // Persist the new WizardState and return the first step template
        return wizardStateStore.saveWizardState(null, newState)
                .onItem().transform(wizardId -> {
                    newState.setCurrentStep(1); // Set current step for display
                    return Templates.step1(newState.getAddressForm(), null, 1, TOTAL_STEPS, wizardId);
                });
    }

    @GET
    @Path("/step/{stepNumber}/{wizardId}")
    public Uni<TemplateInstance> showStep(@PathParam("stepNumber") int stepNumber,
            @PathParam("wizardId") String wizardId) {
        return wizardStateStore.getWizardState(wizardId)
                .onItem().transformToUni(optionalState -> {
                    if (optionalState.isEmpty()) {
                        // If state not found (expired or invalid ID), redirect to an error page
                        return Uni.createFrom().item(Templates.error());
                    }
                    WizardState state = optionalState.get();
                    state.setCurrentStep(stepNumber); // Update current step

                    // Re-save state to update currentStep and refresh expiration
                    return wizardStateStore.saveWizardState(wizardId, state)
                            .onItem().transform(savedWizardId -> {
                                // Render the appropriate template based on the current step
                                switch (stepNumber) {
                                    case 1:
                                        return Templates.step1(state.getAddressForm(), null, 1, TOTAL_STEPS,
                                                savedWizardId);
                                    case 2:
                                        return Templates.step2(state.getOrderForm(), null, 2, TOTAL_STEPS,
                                                savedWizardId);
                                    case 3:
                                        return Templates.step3(state.getAdditionalInfoForm(), null, 3, TOTAL_STEPS,
                                                savedWizardId);
                                    case 4:
                                        return Templates.step4(state, 4, TOTAL_STEPS, savedWizardId);
                                    default:
                                        // Fallback for invalid step numbers
                                        return Templates.error();
                                }
                            });
                });
    }

    @POST
    @Path("/step1")
    public Uni<TemplateInstance> processStep1(@BeanParam AddressForm form, @RestForm("wizardId") String wizardId) {
        // Retrieve existing state from DB
        return wizardStateStore.getWizardState(wizardId)
                .onItem().transformToUni(optionalState -> {
                    if (optionalState.isEmpty()) {
                        // Redirect to error if wizard state is missing
                        return Uni.createFrom().item(Templates.error());
                    }
                    WizardState state = optionalState.get();

                    // Validate the submitted form data
                    Set<ConstraintViolation<AddressForm>> violations = validator.validate(form);
                    if (!violations.isEmpty()) {
                        Set<String> errors = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.toSet());
                        // If validation fails, re-render the current step with errors
                        return Uni.createFrom().item(Templates.step1(form, errors, 1, TOTAL_STEPS, wizardId));
                    }

                    // If validation passes, update state, increment step, and save
                    state.setAddressForm(form);
                    state.setCurrentStep(2);
                    return wizardStateStore.saveWizardState(wizardId, state)
                            .onItem().transform(savedWizardId -> Templates.step2(state.getOrderForm(), null, 2,
                                    TOTAL_STEPS, savedWizardId));
                });
    }

    @POST
    @Path("/step2")
    public Uni<TemplateInstance> processStep2(@BeanParam OrderForm form, @RestForm("wizardId") String wizardId) {
        return wizardStateStore.getWizardState(wizardId)
                .onItem().transformToUni(optionalState -> {
                    if (optionalState.isEmpty()) {
                        // Redirect to error if wizard state is missing
                        return Uni.createFrom().item(Templates.error());
                    }
                    WizardState state = optionalState.get();

                    Set<ConstraintViolation<OrderForm>> violations = validator.validate(form);
                    if (!violations.isEmpty()) {
                        Set<String> errors = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.toSet());
                        return Uni.createFrom().item(Templates.step2(form, errors, 2, TOTAL_STEPS, wizardId));
                    }
                    state.setOrderForm(form);
                    state.setCurrentStep(3);
                    return wizardStateStore.saveWizardState(wizardId, state)
                            .onItem().transform(savedWizardId -> Templates.step3(state.getAdditionalInfoForm(), null, 3,
                                    TOTAL_STEPS, savedWizardId));
                });
    }

    @POST
    @Path("/step3")
    public Uni<TemplateInstance> processStep3(@BeanParam AdditionalInfoForm form,
            @RestForm("wizardId") String wizardId) {
        return wizardStateStore.getWizardState(wizardId)
                .onItem().transformToUni(optionalState -> {
                    if (optionalState.isEmpty()) {
                        // Redirect to error if wizard state is missing
                        return Uni.createFrom().item(Templates.error());
                    }
                    WizardState state = optionalState.get();

                    Set<ConstraintViolation<AdditionalInfoForm>> violations = validator.validate(form);
                    if (!violations.isEmpty()) {
                        Set<String> errors = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.toSet());
                        return Uni.createFrom().item(Templates.step3(form, errors, 3, TOTAL_STEPS, wizardId));
                    }
                    state.setAdditionalInfoForm(form);
                    state.setCurrentStep(4);
                    return wizardStateStore.saveWizardState(wizardId, state)
                            .onItem().transform(savedWizardId -> Templates.step4(state, 4, TOTAL_STEPS, savedWizardId));
                });
    }

    @POST
    @Path("/submit")
    public Uni<TemplateInstance> submitWizard(@RestForm("wizardId") String wizardId) {
        return wizardStateStore.getWizardState(wizardId)
                .onItem().transformToUni(optionalState -> {
                    if (optionalState.isEmpty()) {
                        // Redirect to start if wizard state is missing
                        return Uni.createFrom().item(Templates.error());
                    }
                    WizardState state = optionalState.get();

                    // Perform final processing of the complete wizardState
                    Log.infof("Wizard data submitted (from DB):");
                    Log.infof("Wizard ID: " + wizardId);
                    Log.infof(
                            "Address: " + state.getAddressForm().getStreet() + ", " + state.getAddressForm().getCity());
                    Log.infof("Order: " + state.getOrderForm().getQuantity() + " x "
                            + state.getOrderForm().getProductName());
                    Log.infof("Comments: " + state.getAdditionalInfoForm().getComments());

                    // Delete the state from the database after successful submission
                    return wizardStateStore.deleteWizardState(wizardId)
                            .onItem().transform(v -> Templates.success());
                });
    }
}