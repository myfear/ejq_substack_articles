package com.ibm.developer.quarkus.actuator.runtime.infoprovider;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SslInfoProvider extends AbstractInfoProvider {

    public Map<String, Object> getSslInfo() {
        Map<String, Object> ssl = newMap();
        List<Map<String, Object>> bundles = new ArrayList<>();

        try {
            // Try to load keystore from system properties
            String keystorePath = System.getProperty("javax.net.ssl.keyStore");
            String keystorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
            String keystoreType = System.getProperty("javax.net.ssl.keyStoreType", "JKS");

            if (keystorePath != null && !keystorePath.isEmpty()) {
                KeyStore keystore = loadKeystore(keystorePath, keystorePassword, keystoreType);
                if (keystore != null) {
                    Map<String, Object> bundle = createBundleFromKeystore(keystore, keystorePath);
                    if (bundle != null && !bundle.isEmpty()) {
                        bundles.add(bundle);
                    }
                }
            } else {
                // Try default keystore locations
                String defaultKeystore = System.getProperty("java.home") + "/lib/security/cacerts";
                KeyStore defaultKs = loadKeystore(defaultKeystore, "changeit", "JKS");
                if (defaultKs != null) {
                    Map<String, Object> bundle = createBundleFromKeystore(defaultKs, "default");
                    if (bundle != null && !bundle.isEmpty()) {
                        bundles.add(bundle);
                    }
                }
            }
        } catch (Exception e) {
            log.debugf("Failed to load SSL information: %s", e.getMessage());
        }

        ssl.put("bundles", bundles);
        return ssl;
    }

    private KeyStore loadKeystore(String path, String password, String type) {
        try {
            KeyStore keystore = KeyStore.getInstance(type);
            java.io.FileInputStream fis = new java.io.FileInputStream(path);
            keystore.load(fis, password != null ? password.toCharArray() : null);
            fis.close();
            return keystore;
        } catch (Exception e) {
            log.debugf("Failed to load keystore from %s: %s", path, e.getMessage());
            return null;
        }
    }

    private Map<String, Object> createBundleFromKeystore(KeyStore keystore, String bundleName) {
        Map<String, Object> bundle = newMap();
        bundle.put("name", bundleName);
        List<Map<String, Object>> certificateChains = new ArrayList<>();

        try {
            Enumeration<String> aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate[] chain = keystore.getCertificateChain(alias);
                if (chain == null || chain.length == 0) {
                    // Single certificate
                    Certificate cert = keystore.getCertificate(alias);
                    if (cert instanceof X509Certificate) {
                        Map<String, Object> chainMap = createCertificateChain(alias, new Certificate[] { cert });
                        if (chainMap != null) {
                            certificateChains.add(chainMap);
                        }
                    }
                } else {
                    // Certificate chain
                    Map<String, Object> chainMap = createCertificateChain(alias, chain);
                    if (chainMap != null) {
                        certificateChains.add(chainMap);
                    }
                }
            }
        } catch (KeyStoreException e) {
            log.debugf("Failed to enumerate keystore aliases: %s", e.getMessage());
        }

        bundle.put("certificateChains", certificateChains);
        return bundle;
    }

    private Map<String, Object> createCertificateChain(String alias, Certificate[] chain) {
        Map<String, Object> chainMap = newMap();
        chainMap.put("alias", alias);
        List<Map<String, Object>> certificates = new ArrayList<>();

        for (Certificate cert : chain) {
            if (cert instanceof X509Certificate) {
                X509Certificate x509Cert = (X509Certificate) cert;
                Map<String, Object> certMap = newMap();
                
                certMap.put("version", "V" + x509Cert.getVersion());
                certMap.put("issuer", x509Cert.getIssuerX500Principal().getName());
                certMap.put("subject", x509Cert.getSubjectX500Principal().getName());
                certMap.put("serialNumber", x509Cert.getSerialNumber().toString(16));
                certMap.put("signatureAlgorithmName", x509Cert.getSigAlgName());

                // Validity
                Map<String, Object> validity = newMap();
                try {
                    x509Cert.checkValidity();
                    validity.put("status", "VALID");
                } catch (Exception e) {
                    validity.put("status", "INVALID");
                }
                certMap.put("validity", validity);

                // Format dates
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withZone(ZoneOffset.UTC);
                certMap.put("validityStarts", formatter.format(Instant.ofEpochMilli(x509Cert.getNotBefore().getTime())));
                certMap.put("validityEnds", formatter.format(Instant.ofEpochMilli(x509Cert.getNotAfter().getTime())));

                certificates.add(certMap);
            }
        }

        chainMap.put("certificates", certificates);
        return chainMap;
    }
}

