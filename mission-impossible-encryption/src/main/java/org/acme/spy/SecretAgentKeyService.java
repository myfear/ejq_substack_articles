package org.acme.spy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service for generating PGP key pairs for secret agents.
 * 
 * This class generates RSA 4096-bit key pairs and converts them to PGP format,
 * producing both public and private key rings. The private key is encrypted
 * with AES-256 using a passphrase, and both keys are returned in ASCII-armored
 * (base64) format suitable for storage or transmission.
 */
@ApplicationScoped
public class SecretAgentKeyService {

    /**
     * Generates PGP key pairs for an agent with the given code name and email.
     * Returns both public and private keys in ASCII-armored format.
     */
    public AgentEnrollmentResponse generateAgentKeys(String codeName, String email, char[] passphrase) {
        try {
            String identity = codeName + " <" + email + ">";
            // Create the PGP key ring generator with RSA keys and encryption settings
            PGPKeyRingGenerator keyRingGen = createKeyRingGenerator(identity, passphrase);

            // Generate separate public and private key rings
            PGPPublicKeyRing publicKeyRing = keyRingGen.generatePublicKeyRing();
            PGPSecretKeyRing secretKeyRing = keyRingGen.generateSecretKeyRing();

            // Convert binary key data to ASCII-armored (base64) format
            String publicKeyArmored = armor(publicKeyRing.getEncoded());
            String privateKeyArmored = armor(secretKeyRing.getEncoded());

            return new AgentEnrollmentResponse(codeName, email, publicKeyArmored, privateKeyArmored);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PGP keys for " + codeName, e);
        }
    }

    /**
     * Creates a PGP key ring generator with RSA 4096-bit keys.
     * The private key will be encrypted with AES-256 using the provided passphrase.
     */
    private PGPKeyRingGenerator createKeyRingGenerator(String identity, char[] passphrase)
            throws NoSuchAlgorithmException, NoSuchProviderException, PGPException {

        // Generate RSA 4096-bit key pair using BouncyCastle provider
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(4096);
        KeyPair kp = kpg.generateKeyPair();

        // Configure SHA1 for key checksum (required by PGPKeyRingGenerator)
        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder()
                .build()
                .get(HashAlgorithmTags.SHA1);

        // Configure SHA-256 for secret key encryption
        PGPDigestCalculator sha256Calc = new JcaPGPDigestCalculatorProviderBuilder()
                .build()
                .get(HashAlgorithmTags.SHA256);

        // Convert JCA key pair to PGP format (version 4)
        PGPKeyPair pgpKeyPair = new JcaPGPKeyPair(4, PGPPublicKey.RSA_GENERAL, kp, new Date());

        // Configure signer for key certification using SHA-256
        JcaPGPContentSignerBuilder signerBuilder = new JcaPGPContentSignerBuilder(
                pgpKeyPair.getPublicKey().getAlgorithm(),
                HashAlgorithmTags.SHA256);

        // Configure private key encryption with AES-256 and the provided passphrase
        JcePBESecretKeyEncryptorBuilder encryptorBuilder = new JcePBESecretKeyEncryptorBuilder(
                PGPEncryptedData.AES_256, sha256Calc)
                .setProvider("BC");

        // Create the key ring generator with all configured components
        // Note: SHA1 is required for key checksum calculations (4th parameter)
        PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION,
                pgpKeyPair,
                identity,
                sha1Calc,
                null,
                null,
                signerBuilder,
                encryptorBuilder.build(passphrase));

        return keyRingGen;
    }

    /**
     * Converts binary PGP key data to ASCII-armored format (base64 encoded text).
     * This format is human-readable and safe for email or text file storage.
     */
    private String armor(byte[] bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ArmoredOutputStream aos = new ArmoredOutputStream(baos)) {
            aos.write(bytes);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }
}
