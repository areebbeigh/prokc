package com.areebbeigh.prokc.certificates;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.lang3.NotImplementedException;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class CertificateGenerator {

  public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

  public X509Certificate createCARootCertificate(KeyPair keyPair)
      throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
    X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

    X500Principal issuer = new X500Principal(
        "CN=Prokc CA,OU=Prokc Certification Authority,O=prokc.io,C=US");

    // DN and Serial Number
    generator.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
    generator.setIssuerDN(issuer);
    generator.setSubjectDN(issuer);

    // Validity
    long monthsBefore = 12L;
    long monthsAfter = 24L;
    generator.setNotBefore(
        new Date(System.currentTimeMillis() - (monthsBefore * 30 * 24 * 60 * 60 * 1000)));
    generator.setNotAfter(
        new Date(System.currentTimeMillis() + (monthsAfter * 30 * 24 * 60 * 60 * 1000)));

    // Public key, algorithm
    generator.setPublicKey(keyPair.getPublic());
    generator.setSignatureAlgorithm(SIGNATURE_ALGORITHM);

    // Extensions
    generator.addExtension(
        Extension.subjectKeyIdentifier,
        false,
        SubjectKeyIdentifier.getInstance(keyPair.getPublic().getEncoded())
    );
    generator.addExtension(
        Extension.basicConstraints,
        true,
        new BasicConstraints(0)
    );
    generator.addExtension(
        Extension.keyUsage,
        false,
        new KeyUsage(KeyUsage.cRLSign | KeyUsage.keyCertSign)
    );

    // Extended usage
    ASN1EncodableVector purposes = new ASN1EncodableVector();
    purposes.add(KeyPurposeId.id_kp_serverAuth);
    purposes.add(KeyPurposeId.id_kp_clientAuth);
    purposes.add(KeyPurposeId.anyExtendedKeyUsage);
    generator.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));

    X509Certificate certificate = generator.generate(keyPair.getPrivate());
    certificate.checkValidity(new Date());
    certificate.verify(keyPair.getPublic());

    return certificate;
  }

  public X509Certificate createServerCertificate(X509Certificate caCert, PrivateKey privateKey) {
    throw new NotImplementedException();
  }
}
