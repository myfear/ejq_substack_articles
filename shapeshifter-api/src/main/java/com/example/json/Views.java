package com.example.json;

public class Views {
    // 1. The Building Blocks
    public interface Public {
    }

    public interface PremiumFeature {
    } // The "Aspect"

    // 2. The Hierarchy
    public interface Authenticated extends Public {
    }

    // 2. The Hierarchy
    public interface AdminFeature extends Authenticated {
    }

    // 3. The "Bridge" View
    // This is the Magic: It combines standard access with premium features
    // WITHOUT giving them Admin privileges.
    public interface Subscriber extends Authenticated, PremiumFeature {
    }

    // 4. The Admin (who sees everything)
    public interface Admin extends PremiumFeature, AdminFeature {
    }
}