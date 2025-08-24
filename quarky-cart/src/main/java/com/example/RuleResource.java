package com.example;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/rules")
public class RuleResource {

    @Inject
    AssociationRuleService ruleService;

    @GET
    @Path("/discover")
    public List<AssociationRuleService.AssociationRule> discoverRules(
            @QueryParam("support") Double support,
            @QueryParam("confidence") Double confidence) {

        // Set reasonable defaults if parameters are not provided
        double minSupport = (support != null) ? support : 0.1; // Default: itemset must appear in at least 10% of
                                                               // transactions
        double minConfidence = (confidence != null) ? confidence : 0.5; // Default: rule must be correct at least 50% of
                                                                        // the time

        return ruleService.findAssociationRules(minSupport, minConfidence);
    }
}