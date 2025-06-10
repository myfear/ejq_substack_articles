package com.example.demo;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Workflow;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;

@Workflow(dependents = @Dependent(type = ConfigMapDR.class))
public class GreetingReconciler implements Reconciler<Greeting> {
    @Override
    public UpdateControl<Greeting> reconcile(Greeting resource, Context<Greeting> context) {
        resource.setStatus(new GreetingStatus(resource.getSpec().message()));
        return UpdateControl.patchStatus(resource);
    }
}