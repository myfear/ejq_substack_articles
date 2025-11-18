package org.acme.spy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPMarker;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PgpCryptoService {

    public String encrypt(String plainText, String armoredPublicKey) {
        try {
            PGPPublicKey publicKey = readEncryptionKey(armoredPublicKey);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (org.bouncycastle.bcpg.ArmoredOutputStream armoredOut = new org.bouncycastle.bcpg.ArmoredOutputStream(
                    out)) {

                PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                        new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                                .setWithIntegrityPacket(true)
                                .setSecureRandom(new java.security.SecureRandom())
                                .setProvider("BC"));

                encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey)
                        .setProvider("BC"));

                OutputStream encOut = encGen.open(armoredOut, new byte[4096]);

                PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

                try (OutputStream compressedOut = comData.open(encOut)) {
                    PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
                    byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);

                    try (OutputStream literalOut = lGen.open(compressedOut,
                            PGPLiteralData.TEXT,
                            PGPLiteralData.CONSOLE,
                            bytes.length,
                            new Date())) {
                        literalOut.write(bytes);
                    }
                }
                encOut.close();
            }

            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt message", e);
        }
    }

    public String decrypt(String armoredCipherText,
            String armoredPrivateKey,
            char[] passphrase) {

        try (InputStream in = PGPUtil.getDecoderStream(
                new ByteArrayInputStream(
                        armoredCipherText.getBytes(StandardCharsets.UTF_8)))) {

            PGPObjectFactory pgpFact = new PGPObjectFactory(in, new JcaKeyFingerprintCalculator());
            Object obj = pgpFact.nextObject();

            if (obj instanceof PGPEncryptedDataList encryptedDataList) {
                return decryptEncryptedDataList(encryptedDataList, armoredPrivateKey, passphrase);
            } else if (obj instanceof PGPMarker) {
                obj = pgpFact.nextObject();
                PGPEncryptedDataList encList = (PGPEncryptedDataList) obj;
                return decryptEncryptedDataList(encList, armoredPrivateKey, passphrase);
            } else {
                throw new IllegalStateException("Unexpected PGP object: " + obj);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt message", e);
        }
    }

    private String decryptEncryptedDataList(PGPEncryptedDataList encList,
            String armoredPrivateKey,
            char[] passphrase) throws Exception {

        PGPSecretKeyRingCollection secretKeyRings = readSecretKeyRings(armoredPrivateKey);

        PGPPublicKeyEncryptedData encryptedData = null;
        PGPPrivateKey privateKey = null;

        Iterator<PGPEncryptedData> it = encList.getEncryptedDataObjects();
        while (it.hasNext() && privateKey == null) {
            PGPPublicKeyEncryptedData pked = (PGPPublicKeyEncryptedData) it.next();
            PGPSecretKey secretKey = secretKeyRings.getSecretKey(pked.getKeyIdentifier().getKeyId());
            if (secretKey != null) {
                privateKey = secretKey.extractPrivateKey(
                        new JcePBESecretKeyDecryptorBuilder()
                                .setProvider("BC")
                                .build(passphrase));
                encryptedData = pked;
            }
        }

        if (privateKey == null || encryptedData == null) {
            throw new IllegalStateException("No suitable private key found for decryption");
        }

        InputStream clear = encryptedData.getDataStream(
                new JcePublicKeyDataDecryptorFactoryBuilder()
                        .setProvider("BC")
                        .build(privateKey));

        PGPObjectFactory plainFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator());

        Object message = plainFact.nextObject();
        if (message instanceof PGPCompressedData compressedData) {
            PGPObjectFactory innerFact = new PGPObjectFactory(
                    compressedData.getDataStream(),
                    new JcaKeyFingerprintCalculator());
            message = innerFact.nextObject();
        }

        if (message instanceof PGPLiteralData literalData) {
            InputStream literalStream = literalData.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            literalStream.transferTo(out);
            return out.toString(StandardCharsets.UTF_8);
        }

        throw new IllegalStateException("Unsupported PGP message type: " + message);
    }

    private PGPPublicKey readEncryptionKey(String armoredPublicKey) throws Exception {
        try (InputStream keyIn = PGPUtil.getDecoderStream(
                new ByteArrayInputStream(
                        armoredPublicKey.getBytes(StandardCharsets.UTF_8)))) {

            PGPObjectFactory pgpFact = new PGPObjectFactory(keyIn, new JcaKeyFingerprintCalculator());
            Object obj = pgpFact.nextObject();
            if (!(obj instanceof PGPPublicKeyRing)) {
                throw new IllegalStateException("Not a public key ring");
            }

            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) obj;

            Iterator<PGPPublicKey> it = keyRing.getPublicKeys();
            while (it.hasNext()) {
                PGPPublicKey key = it.next();
                if (key.isEncryptionKey()) {
                    return key;
                }
            }
        }
        throw new IllegalStateException("No encryption key found in public key ring");
    }

    private PGPSecretKeyRingCollection readSecretKeyRings(String armoredPrivateKey)
            throws Exception {

        try (InputStream keyIn = PGPUtil.getDecoderStream(
                new ByteArrayInputStream(
                        armoredPrivateKey.getBytes(StandardCharsets.UTF_8)))) {

            return new PGPSecretKeyRingCollection(
                    keyIn,
                    new JcaKeyFingerprintCalculator());
        }
    }
}
