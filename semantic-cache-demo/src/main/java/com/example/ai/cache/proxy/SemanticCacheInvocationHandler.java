package com.example.ai.cache.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.example.ai.cache.SemanticCacheConfig;
import com.example.ai.cache.store.SemanticCacheStore;

import io.quarkus.logging.Log;

public class SemanticCacheInvocationHandler implements InvocationHandler {

    private final Object target;
    private final SemanticCacheStore cacheStore;
    private final SemanticCacheConfig config;

    public SemanticCacheInvocationHandler(
            Object target,
            SemanticCacheStore cacheStore,
            SemanticCacheConfig config) {
        this.target = target;
        this.cacheStore = cacheStore;
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Skip caching for Object methods
        if (method.getDeclaringClass() == Object.class) {
            Method targetMethod = findTargetMethod(method);
            return targetMethod.invoke(target, args);
        }

        // Extract prompt and build LLM string
        String prompt = extractPrompt(method, args);
        String llmString = buildLlmString(target, method);

        Log.debugf("Cache lookup for method %s with prompt: %s",
                method.getName(), prompt);

        // Try cache lookup
        var cached = cacheStore.lookup(prompt, llmString, config);
        if (cached.isPresent()) {
            Log.infof("Cache hit (similarity: %.3f, exact: %b) for method %s",
                    cached.get().getSimilarityScore(),
                    cached.get().isExactMatch(),
                    method.getName());
            return cached.get().getResponse();
        }

        Log.infof("Cache miss for method %s, invoking AI service", method.getName());

        // Execute actual method
        // Find the corresponding method on the target class to avoid
        // IllegalAccessException
        Method targetMethod = findTargetMethod(method);
        Object response = targetMethod.invoke(target, args);

        // Store in cache
        cacheStore.store(prompt, llmString, response, config);

        return response;
    }

    private String extractPrompt(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        // Check for @UserMessage or @SystemMessage annotations
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (isPromptParameter(parameters[i])) {
                return String.valueOf(args[i]);
            }
        }

        // Fallback: concatenate all string arguments
        return Arrays.stream(args)
                .filter(arg -> arg instanceof String)
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
    }

    private boolean isPromptParameter(Parameter parameter) {
        // Check for langchain4j annotations
        return Arrays.stream(parameter.getAnnotations())
                .anyMatch(ann -> ann.annotationType().getName().contains("UserMessage") ||
                        ann.annotationType().getName().contains("SystemMessage"));
    }

    private String buildLlmString(Object target, Method method) {
        // Build a string that uniquely identifies the LLM configuration
        // Include: service class, method name, model configuration
        return String.format("%s.%s",
                target.getClass().getSimpleName(),
                method.getName());
    }

    /**
     * Find the corresponding method on the target class.
     * This is necessary because the method parameter is from the interface,
     * but we need to invoke it on the concrete target implementation.
     * 
     * If the target is a proxy (like LangChain4j AI services), we need to
     * find the actual method on the target class, not use the interface method
     * directly.
     */
    private Method findTargetMethod(Method interfaceMethod) throws NoSuchMethodException {
        Class<?> targetClass = target.getClass();
        Class<?>[] parameterTypes = interfaceMethod.getParameterTypes();

        // Check if target is a proxy (common for Quarkus/LangChain4j services)
        if (Proxy.isProxyClass(targetClass)) {
            // For proxies, we can use the interface method directly
            // but we need to ensure it's accessible
            try {
                interfaceMethod.setAccessible(true);
                return interfaceMethod;
            } catch (Exception e) {
                // If that fails, try to find it through the proxy's interfaces
                for (Class<?> iface : targetClass.getInterfaces()) {
                    try {
                        Method m = iface.getMethod(interfaceMethod.getName(), parameterTypes);
                        m.setAccessible(true);
                        return m;
                    } catch (NoSuchMethodException ignored) {
                        // Continue
                    }
                }
            }
        }

        try {
            // Try to find the method directly on the target class
            Method targetMethod = targetClass.getMethod(
                    interfaceMethod.getName(),
                    parameterTypes);
            targetMethod.setAccessible(true);
            return targetMethod;
        } catch (NoSuchMethodException e) {
            // If not found, try to find it in the class hierarchy
            // This handles cases where the target has a different structure
            Class<?> currentClass = targetClass;
            while (currentClass != null && currentClass != Object.class) {
                try {
                    Method targetMethod = currentClass.getDeclaredMethod(
                            interfaceMethod.getName(),
                            parameterTypes);
                    targetMethod.setAccessible(true);
                    return targetMethod;
                } catch (NoSuchMethodException ignored) {
                    // Continue searching up the hierarchy
                }
                currentClass = currentClass.getSuperclass();
            }

            // If still not found, try to find by name and parameter count only
            // (less strict matching for proxy classes)
            for (Method m : targetClass.getMethods()) {
                if (m.getName().equals(interfaceMethod.getName()) &&
                        m.getParameterCount() == parameterTypes.length) {
                    m.setAccessible(true);
                    return m;
                }
            }

            // Last resort: use the interface method directly
            // This should work if the target implements the interface
            interfaceMethod.setAccessible(true);
            return interfaceMethod;
        }
    }
}