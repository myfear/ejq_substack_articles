package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AssociationRuleService {

    // A simple record to hold our final rule and its metrics
    public record AssociationRule(Set<String> antecedent, Set<String> consequent, double confidence, double lift,
            double support) {
    }

    public List<AssociationRule> findAssociationRules(double minSupport, double minConfidence) {
        List<Set<String>> transactions = Transaction.<Transaction>listAll().stream()
                .map(t -> t.items)
                .collect(Collectors.toList());

        int numTransactions = transactions.size();

        Map<Set<String>, Integer> itemsetSupportCount = new HashMap<>();
        Map<String, Integer> singleItemSupportCount = new HashMap<>();

        // Calculate support counts for single items and pairs
        for (Set<String> transaction : transactions) {
            List<String> items = new ArrayList<>(transaction);
            for (int i = 0; i < items.size(); i++) {
                String itemA = items.get(i);
                singleItemSupportCount.put(itemA, singleItemSupportCount.getOrDefault(itemA, 0) + 1);
                for (int j = i + 1; j < items.size(); j++) {
                    String itemB = items.get(j);
                    Set<String> pair = new TreeSet<>(Arrays.asList(itemA, itemB)); // Use TreeSet for consistent
                                                                                   // ordering
                    itemsetSupportCount.put(pair, itemsetSupportCount.getOrDefault(pair, 0) + 1);
                }
            }
        }

        List<AssociationRule> allRules = new ArrayList<>();

        // Generate rules from frequent itemsets
        for (Map.Entry<Set<String>, Integer> entry : itemsetSupportCount.entrySet()) {
            Set<String> itemset = entry.getKey();
            int itemsetCount = entry.getValue();
            double itemsetSupport = (double) itemsetCount / numTransactions;

            if (itemsetSupport >= minSupport) {
                // For each item in the pair, create a rule
                for (String item : itemset) {
                    Set<String> antecedent = Set.of(item);
                    Set<String> consequent = new HashSet<>(itemset);
                    consequent.remove(item);

                    int antecedentCount = singleItemSupportCount.get(item);
                    double confidence = (double) itemsetCount / antecedentCount;

                    String consequentItem = consequent.iterator().next();
                    int consequentCount = singleItemSupportCount.get(consequentItem);
                    double consequentSupport = (double) consequentCount / numTransactions;

                    double lift = confidence / consequentSupport;

                    if (confidence >= minConfidence) {
                        allRules.add(new AssociationRule(antecedent, consequent, confidence, lift, itemsetSupport));
                    }
                }
            }
        }

        allRules.sort(Comparator.comparing(AssociationRule::confidence).reversed());
        return allRules;
    }
}