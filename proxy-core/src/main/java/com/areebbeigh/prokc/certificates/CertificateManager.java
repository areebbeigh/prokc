package com.areebbeigh.prokc.certificates;

import static com.areebbeigh.prokc.certificates.util.KeyUtils.generateKeyPair;

import com.areebbeigh.prokc.certificates.pojo.CertificateAndKey;
import com.areebbeigh.prokc.proxy.ProxyConfiguration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.net.ssl.KeyManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

@Slf4j
@RequiredArgsConstructor
public class CertificateManager {

  private static final String ROOT_CA_ALIAS = "ROOT_CA";
  private final CertificateGenerator certificateGenerator;
  private final KeyStoreManager keyStoreManager;
  private final ProxyConfiguration configuration;

  public void addX509CertToTrustStore(String host)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, IOException, UnrecoverableEntryException {
    Certificate certificate = keyStoreManager.getCertificate(host);
    if (certificate == null || !isValid(certificate)) {
      log.debug("No certificate/invalid certificate found for host {}. Generating.", host);
      CertificateAndKey caCert = getRootCACert();
      createServerCertificate(host, caCert);
    }
  }

  public KeyManagerFactory getX509KeyManagerFactory() {
    return keyStoreManager.getX509KeyManagerFactory();
  }

  private boolean isValid(Certificate certificate) {
    try {
      ((X509Certificate) certificate).checkValidity(new Date());
      return true;
    } catch (CertificateExpiredException | CertificateNotYetValidException e) {
      return false;
    }
  }

  private CertificateAndKey getRootCACert()
      throws NoSuchAlgorithmException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException, KeyStoreException, IOException, UnrecoverableEntryException {
    CertificateAndKey cert = keyStoreManager.getCertificateAndKey(ROOT_CA_ALIAS);
    if (cert == null) {
      log.debug("No root CA certificate found. Generating.");
      KeyPair keyPair = generateKeyPair();
      X509Certificate certificate = certificateGenerator.createCARootCertificate(keyPair);
      cert = new CertificateAndKey(keyPair.getPrivate(), certificate);
      keyStoreManager.addCertificateAndKey(ROOT_CA_ALIAS, cert);
      writeToDisk(certificate);
    }

    return cert;
  }

  private void writeToDisk(X509Certificate certificate) throws IOException {
    File file = new File(configuration.getRootCAPath().toString());
    log.debug("Saving root CA certificate to {}", file);
    try (var f = new FileWriter(file)) {
      PemWriter writer = new PemWriter(f);
      writer.writeObject(new JcaMiscPEMGenerator(certificate));
      writer.flush();
    }
  }

  private X509Certificate createServerCertificate(String host, CertificateAndKey caCert)
      throws IOException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, KeyStoreException {
    KeyPair keyPair = generateKeyPair();
    X509Certificate certificate = certificateGenerator.createServerCertificate(caCert, host,
                                                                               keyPair.getPublic());
    keyStoreManager.addCertificateAndKey(host,
                                         new CertificateAndKey(keyPair.getPrivate(), certificate));
    return certificate;
  }
}
