package com.areebbeigh.prokc.certificates;

import static com.areebbeigh.prokc.certificates.util.KeyUtils.toAsymmetricKeyParameter;

import com.areebbeigh.prokc.certificates.pojo.CertificateAndKey;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.x509.X509V3CertificateGenerator;

@RequiredArgsConstructor
public class CertificateGenerator {

  public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
  private static final Long MONTHS_BEFORE = 12L;
  private static final Date NOT_BEFORE = new Date(
      System.currentTimeMillis() - (MONTHS_BEFORE * 30 * 24 * 60 * 60 * 1000));
  private static final Long MONTHS_AFTER = 24L;
  public static final Date NOT_AFTER = new Date(
      System.currentTimeMillis() + (MONTHS_AFTER * 30 * 24 * 60 * 60 * 1000));

  private final BcX509ExtensionUtils x509ExtensionUtils;

  public X509Certificate createCARootCertificate(KeyPair keyPair)
      throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, IOException {
    var generator = new X509V3CertificateGenerator();

    var issuer = new X500Principal(
        "CN=Prokc Root CA,OU=Prokc Certification Authority,O=Prokc,C=US");

    // DN and Serial Number
    generator.setSerialNumber(randomSerialNumber());
    generator.setIssuerDN(issuer);
    generator.setSubjectDN(issuer);

    // Validity
    generator.setNotBefore(NOT_BEFORE);
    generator.setNotAfter(NOT_AFTER);

    // Public key, algorithm
    generator.setPublicKey(keyPair.getPublic());
    generator.setSignatureAlgorithm(SIGNATURE_ALGORITHM);

    // Extensions
    generator.addExtension(Extension.subjectKeyIdentifier, false,
                           x509ExtensionUtils.createSubjectKeyIdentifier(
                               toAsymmetricKeyParameter(keyPair.getPublic())));
    generator.addExtension(Extension.basicConstraints, true, new BasicConstraints(0));
    generator.addExtension(Extension.keyUsage, true, new KeyUsage(
        KeyUsage.cRLSign | KeyUsage.keyCertSign | KeyUsage.digitalSignature));

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

  public X509Certificate createServerCertificate(CertificateAndKey caCert, String host,
                                                 PublicKey serverPublicKey)
      throws IOException, CertificateEncodingException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, CertificateNotYetValidException, CertificateExpiredException {
    var generator = new X509V3CertificateGenerator();

    // Subject, Issuer DN and Serial no.
    generator.setSerialNumber(randomSerialNumber());
    generator.setSubjectDN(new X500Principal("CN=%s".formatted(host)));
    generator.setIssuerDN(caCert.certificate().getSubjectX500Principal());

    // Public key, sign algorithm
    generator.setSignatureAlgorithm(SIGNATURE_ALGORITHM);
    generator.setPublicKey(serverPublicKey);

    // Validity
    generator.setNotAfter(NOT_AFTER);
    generator.setNotBefore(NOT_BEFORE);

    // Extensions
    generator.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
    generator.addExtension(Extension.subjectKeyIdentifier, false,
                           SubjectPublicKeyInfo.getInstance(serverPublicKey.getEncoded()));
    generator.addExtension(Extension.authorityKeyIdentifier, false,
                           x509ExtensionUtils.createAuthorityKeyIdentifier(
                               toAsymmetricKeyParameter(caCert.certificate().getPublicKey())));

    // Extended usage
    var purposes = new ASN1EncodableVector();
    purposes.add(KeyPurposeId.id_kp_serverAuth);
    purposes.add(KeyPurposeId.id_kp_clientAuth);
    purposes.add(KeyPurposeId.id_kp_nsSGC);
    purposes.add(KeyPurposeId.id_kp_msSGC);
    generator.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));

    X509Certificate certificate = generator.generate(caCert.privateKey());
    certificate.checkValidity(new Date());
    return certificate;
  }

  private static BigInteger randomSerialNumber() {
    return BigInteger.valueOf(System.currentTimeMillis());
  }
}
