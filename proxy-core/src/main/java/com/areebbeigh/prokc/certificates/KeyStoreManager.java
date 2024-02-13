package com.areebbeigh.prokc.certificates;

import com.areebbeigh.prokc.certificates.pojo.CertificateAndKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

  private static final String X509_ALGORITHM = "SunX509";
  private static final char[] KEYSTORE_PWD = "".toCharArray();
  private static final PasswordProtection PROTECTION_PARAM = new PasswordProtection(KEYSTORE_PWD);
  private final KeyStore keyStore;
  private final Path keyStorePath;

  public KeyStoreManager(Path keyStorePath)
      throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
    this.keyStorePath = keyStorePath;
    this.keyStore = initKeyStore();
  }

  private KeyStore initKeyStore()
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

    if (keyStorePath != null && Files.exists(keyStorePath)) {
      var file = keyStorePath.toFile();
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

  public CertificateAndKey getCertificateAndKey(String alias)
      throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
    if (!keyStore.isKeyEntry(alias)) {
      return null;
    }

    var entry = (PrivateKeyEntry) keyStore.getEntry(alias, PROTECTION_PARAM);
    PrivateKey privateKey = entry.getPrivateKey();
    Certificate[] certChain = entry.getCertificateChain();
    return new CertificateAndKey(privateKey, (X509Certificate) certChain[0]);
  }

  public void addCertificateAndKey(String alias, CertificateAndKey cert)
      throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
    PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(cert.privateKey(),
                                                          new Certificate[]{cert.certificate()});
    keyStore.setEntry(alias, privateKeyEntry, PROTECTION_PARAM);
    saveKeyStore();
  }

  public KeyManagerFactory getX509KeyManagerFactory() {
    try {
      var factory = KeyManagerFactory.getInstance(X509_ALGORITHM);
      factory.init(keyStore, KEYSTORE_PWD);
      return factory;
    } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
      throw new RuntimeException(e);
    }
  }

  private void saveKeyStore()
      throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
    if (keyStorePath == null)
      return;

    try (var os = new FileOutputStream(keyStorePath.toFile())) {
      keyStore.store(os, KEYSTORE_PWD);
      os.flush();
    }
  }
}
