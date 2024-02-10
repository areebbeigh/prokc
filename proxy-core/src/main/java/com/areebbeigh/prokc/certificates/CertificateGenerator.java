package com.areebbeigh.prokc.certificates;

import com.areebbeigh.prokc.certificates.pojo.RootCACertificate;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
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
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class CertificateGenerator {

  public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

  public X509Certificate createCARootCertificate(KeyPair keyPair)
      throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, IOException {
    var generator = new X509V3CertificateGenerator();

    var issuer = new X500Principal(
        "CN=Prokc Root CA,OU=Prokc Certification Authority,O=Prokc,C=US");

    // DN and Serial Number
    generator.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
    generator.setIssuerDN(issuer);
    generator.setSubjectDN(issuer);

    // Validity
    var monthsBefore = 12L;
    var monthsAfter = 24L;
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
        createSubjectKeyIdentifier(keyPair.getPublic())
    );
    generator.addExtension(
        Extension.basicConstraints,
        true,
        new BasicConstraints(0)
    );
    generator.addExtension(
        Extension.keyUsage,
        true,
        new KeyUsage(KeyUsage.cRLSign | KeyUsage.keyCertSign | KeyUsage.digitalSignature)
    );

    // Extended usage
    var purposes = new ASN1EncodableVector();
    purposes.add(KeyPurposeId.id_kp_serverAuth);
    purposes.add(KeyPurposeId.id_kp_clientAuth);
    purposes.add(KeyPurposeId.anyExtendedKeyUsage);
    generator.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));

    X509Certificate certificate = generator.generate(keyPair.getPrivate());
    certificate.checkValidity(new Date());
    certificate.verify(keyPair.getPublic());

    return certificate;
  }

  private SubjectPublicKeyInfo createSubjectKeyIdentifier(PublicKey publicKey) throws IOException {
    return SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(
        PublicKeyFactory.createKey(publicKey.getEncoded()));
  }

  public X509Certificate createServerCertificate(RootCACertificate caCert, String host) {
    throw new NotImplementedException();
  }
}
