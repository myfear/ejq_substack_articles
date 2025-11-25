package com.ibm.developer.quarkus.actuator.runtime.infoprovider;

import java.util.Map;

public interface BuildInfoProvider {
    Map<String, Object> getBuildInfo();
}
