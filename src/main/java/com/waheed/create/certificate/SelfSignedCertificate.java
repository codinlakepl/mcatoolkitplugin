/**
 *
 */
package com.waheed.create.certificate;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;


/**
 * This class uses the Bouncycastle lightweight API to generate X.509 certificates programmatically.
 *
 * @author abdul
 *
 * orginal file: https://github.com/abdulwaheed18/Self-Signed-Certificate/blob/master/src/com/waheed/create/certificate/SelfSignedCertificate.java
 *
 */
public class SelfSignedCertificate {

    private String CERTIFICATE_ALGORITHM = "RSA";
    private String CERTIFICATE_DN = "CN=cn, O=o, L=L, ST=il, C=c";
    private int CERTIFICATE_BITS = 1024;

    public X509Certificate cert = null;
    public PrivateKey privateKey = null;

    static {
        // adds the Bouncy castle provider to java security
        Security.addProvider(new BouncyCastleProvider());
    }

    public SelfSignedCertificate(String CERTIFICATE_ALGORITHM, String CERTIFICATE_DN, int CERTIFICATE_BITS) throws Exception {
        this.CERTIFICATE_ALGORITHM = CERTIFICATE_ALGORITHM;
        this.CERTIFICATE_DN = CERTIFICATE_DN;
        this.CERTIFICATE_BITS = CERTIFICATE_BITS;

        createCertificate();
    }

    @SuppressWarnings("deprecation")
    private void createCertificate() throws Exception{
        X509Certificate cert = null;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(CERTIFICATE_ALGORITHM);
        keyPairGenerator.initialize(CERTIFICATE_BITS, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // GENERATE THE X509 CERTIFICATE
        X509V3CertificateGenerator v3CertGen =  new X509V3CertificateGenerator();
        v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        v3CertGen.setIssuerDN(new X509Principal(CERTIFICATE_DN));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)));
        v3CertGen.setSubjectDN(new X509Principal(CERTIFICATE_DN));
        v3CertGen.setPublicKey(keyPair.getPublic());
        v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        cert = v3CertGen.generateX509Certificate(keyPair.getPrivate());

        this.cert = cert;
        this.privateKey = keyPair.getPrivate();
    }
}