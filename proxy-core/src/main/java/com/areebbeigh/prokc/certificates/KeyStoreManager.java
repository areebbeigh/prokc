package com.areebbeigh.prokc.certificates;

import com.areebbeigh.prokc.certificates.pojo.RootCACertificate;
import com.areebbeigh.prokc.proxy.ProxyConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;

public class KeyStoreManager {

  private static final String ROOT_CA_ALIAS = "ROOT_CA";
  private static final String X509_ALGORITHM = "X509";
  private static final char[] KEYSTORE_PWD = "".toCharArray();
  private static final PasswordProtection PROTECTION_PARAM = new PasswordProtection(KEYSTORE_PWD);
  private final ProxyConfiguration config;
  private final KeyStore keyStore;

  public KeyStoreManager(ProxyConfiguration config)
      throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
    this.config = config;
    this.keyStore = initKeyStore();
  }

  private KeyStore initKeyStore()
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    String keyStorePath = config.getKeyStorePath().toString();
    File file = new File(keyStorePath);

    if (file.exists()) {
      try (var storeInputStream = new FileInputStream(file)) {
        keyStore.load(storeInputStream, KEYSTORE_PWD);
      }
    } else {
      keyStore.load(null, KEYSTORE_PWD);
    }

    return keyStore;
  }

  public Certificate getCertificate(String alias) throws KeyStoreException {
    return keyStore.getCertificate(alias);
  }

  public RootCACertificate getRootCACertificate()
      throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
    if (!keyStore.isKeyEntry(ROOT_CA_ALIAS)) {
      return null;
    }

    PrivateKeyEntry entry = (PrivateKeyEntry) keyStore.getEntry(ROOT_CA_ALIAS, PROTECTION_PARAM);
    PrivateKey privateKey = entry.getPrivateKey();
    Certificate[] certChain = entry.getCertificateChain();
    return new RootCACertificate(privateKey, (X509Certificate) certChain[0]);
  }

  public void addRootCACert(RootCACertificate cert)
      throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
    PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(cert.privateKey(),
                                                          new Certificate[]{cert.certificate()});
    keyStore.setEntry(ROOT_CA_ALIAS, privateKeyEntry, PROTECTION_PARAM);
    saveKeyStore();
  }

  public void addServerCert(String alias, X509Certificate serverCert)
      throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
    keyStore.setCertificateEntry(alias, serverCert);
    saveKeyStore();
  }

  private void saveKeyStore()
      throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
    try (var os = new FileOutputStream(config.getKeyStorePath().toString())) {
      keyStore.store(os, KEYSTORE_PWD);
    }
  }

  public KeyManagerFactory getX509KeyManagerFactory() {
    try {
      KeyManagerFactory factory = KeyManagerFactory.getInstance(X509_ALGORITHM);
      factory.init(keyStore, KEYSTORE_PWD);
      return factory;
    } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
      throw new RuntimeException(e);
    }
  }
}
