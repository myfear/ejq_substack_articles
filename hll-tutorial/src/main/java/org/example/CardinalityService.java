package org.example;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CardinalityService {
    // p=10 for ~2.4% error, uses 1KB memory
    private final HyperLogLog dailyUsers = new HyperLogLog(10);
    // p=12 for ~1.2% error, uses 4KB memory
    private final HyperLogLog weeklyRepos = new HyperLogLog(12);

    public void trackUser(String userId) {
        if (userId != null) {
            dailyUsers.add(userId);
        }
    }

    public void trackRepo(String repoName) {
        if (repoName != null) {
            weeklyRepos.add(repoName);
        }
    }

    public double getDailyUserEstimate() {
        return dailyUsers.estimate();
    }

    public double getWeeklyRepoEstimate() {
        return weeklyRepos.estimate();
    }

    public long getMemoryUsageBytes() {
        return dailyUsers.getMemoryUsageBytes() + weeklyRepos.getMemoryUsageBytes();
    }
}
