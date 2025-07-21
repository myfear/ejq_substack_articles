package com.example;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChoreAssignmentService {

    public Map<String, String> findStableAssignment(ChoreAssignmentRequest request) {
        Map<String, List<String>> memberPrefs = new HashMap<>(request.familyPreferences());
        Map<String, List<String>> chorePrefs = request.choreSuitability();

        Map<String, String> assignments = new HashMap<>();
        Queue<String> unassignedMembers = new LinkedList<>(memberPrefs.keySet());

        Map<String, Map<String, Integer>> choreRankings = new HashMap<>();
        chorePrefs.forEach((chore, prefs) -> {
            Map<String, Integer> ranks = new HashMap<>();
            for (int i = 0; i < prefs.size(); i++) {
                ranks.put(prefs.get(i), i);
            }
            choreRankings.put(chore, ranks);
        });

        while (!unassignedMembers.isEmpty()) {
            String currentMember = unassignedMembers.poll();
            List<String> memberChorePrefs = memberPrefs.get(currentMember);

            if (memberChorePrefs.isEmpty())
                continue;

            String preferredChore = memberChorePrefs.remove(0);

            if (!assignments.containsKey(preferredChore)) {
                assignments.put(preferredChore, currentMember);
            } else {
                String currentAssignee = assignments.get(preferredChore);
                Map<String, Integer> currentChoreRanks = choreRankings.get(preferredChore);

                if (currentChoreRanks.get(currentMember) < currentChoreRanks.get(currentAssignee)) {
                    assignments.put(preferredChore, currentMember);
                    unassignedMembers.add(currentAssignee);
                } else {
                    unassignedMembers.add(currentMember);
                }
            }
        }

        Map<String, String> finalAssignments = new HashMap<>();
        assignments.forEach((chore, member) -> finalAssignments.put(member, chore));

        return finalAssignments;
    }
}