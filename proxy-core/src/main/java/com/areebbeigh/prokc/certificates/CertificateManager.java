package com.areebbeigh.prokc.certificates;

import com.areebbeigh.prokc.certificates.pojo.RootCACertificate;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
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

public class CertificateManager {

  private final CertificateGenerator certificateGenerator;
  private final KeyStoreManager keyStoreManager;

  public CertificateManager(CertificateGenerator certificateGenerator,
                            KeyStoreManager keyStoreManager) {
    this.certificateGenerator = certificateGenerator;
    this.keyStoreManager = keyStoreManager;
  }

  public void addX509CertToTrustStore(String host)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, IOException, UnrecoverableEntryException {
    Certificate certificate = keyStoreManager.getCertificate(host);
    if (certificate == null || !isValid(certificate)) {
      RootCACertificate caCert = getRootCACert();
      X509Certificate serverCert = certificateGenerator.createServerCertificate(caCert, host);
      keyStoreManager.addServerCert(host, serverCert);
    }
  }

  private boolean isValid(Certificate certificate) {
    try {
      ((X509Certificate) certificate).checkValidity(new Date());
      return true;
    } catch (CertificateExpiredException | CertificateNotYetValidException e) {
      return false;
    }
  }

  private RootCACertificate getRootCACert()
      throws NoSuchAlgorithmException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException, KeyStoreException, IOException, UnrecoverableEntryException {
    RootCACertificate cert = keyStoreManager.getRootCACertificate();
    if (cert == null) {
      KeyPair keyPair = generateKeyPair();
      X509Certificate certificate = certificateGenerator.createCARootCertificate(keyPair);
      cert = new RootCACertificate(keyPair.getPrivate(), certificate);
      keyStoreManager.addRootCACert(cert);
    }

    return cert;
  }

  private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    return generator.generateKeyPair();
  }

  public KeyManagerFactory getX509KeyManagerFactory() {
    return keyStoreManager.getX509KeyManagerFactory();
  }
}
