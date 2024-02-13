package com.areebbeigh.prokc.certificates;

import static com.areebbeigh.prokc.certificates.util.KeyUtils.generateKeyPair;

import com.areebbeigh.prokc.certificates.pojo.CertificateAndKey;
import com.areebbeigh.prokc.proxy.ProxyConfiguration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

@Slf4j
public class CertificateManager {

  private static final String ROOT_CA_ALIAS = "ROOT_CA";
  private static final String CA_KEYSTORE_FILENAME = "prokc.keystore";
  private static final String ROOT_CA_FILENAME = "ProkcRootCA.pem";
  private static final String ROOT_CA_KEY_FILENAME = "ProkcRootCA.key";
  private final CertificateGenerator certificateGenerator;
  private final ProxyConfiguration configuration;
  private final Map<String, KeyStoreManager> keyStoresByHost;

  public CertificateManager(CertificateGenerator certificateGenerator,
                            ProxyConfiguration configuration) {
    this.certificateGenerator = certificateGenerator;
    this.configuration = configuration;
    this.keyStoresByHost = new HashMap<>();
  }

  public void addX509CertToTrustStore(String host)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, IOException, UnrecoverableEntryException {
    KeyStoreManager keyStoreManager = getKeyStore(host);
    Certificate certificate = keyStoreManager.getCertificate(host);
    if (certificate == null || !isValid(certificate)) {
      log.debug("No certificate/invalid certificate found for host {}. Generating.", host);
      CertificateAndKey caCert = getRootCACert();
      createServerCertificate(host, caCert);
    }
  }

  public KeyManagerFactory getX509KeyManagerFactory(String host) {
    KeyStoreManager keyStoreManager = getKeyStore(host);
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

  @SneakyThrows
  private CertificateAndKey getRootCACert() {
    KeyStoreManager keyStoreManager = getKeyStore(ROOT_CA_ALIAS);
    CertificateAndKey cert = keyStoreManager.getCertificateAndKey(ROOT_CA_ALIAS);
    if (cert == null) {
      log.debug("No root CA certificate found. Generating.");
      KeyPair keyPair = generateKeyPair();
      X509Certificate certificate = certificateGenerator.createCARootCertificate(keyPair);
      cert = new CertificateAndKey(keyPair.getPrivate(), certificate);
      keyStoreManager.addCertificateAndKey(ROOT_CA_ALIAS, cert);
      writeToDisk(certificate, ROOT_CA_FILENAME);
      writeToDisk(keyPair.getPrivate(), ROOT_CA_KEY_FILENAME);
    } else {
      log.debug("Found existing root CA certificate: {}", configuration.getRootCAFilePath());
    }

    return cert;
  }

  private void writeToDisk(X509Certificate certificate, String filename) throws IOException {
    File file = Paths.get(configuration.getKeyStoresDir().toString(), filename).toFile();
    log.debug("Saving root CA certificate to {}", file);
    try (var f = new FileWriter(file)) {
      PemWriter writer = new PemWriter(f);
      writer.writeObject(new JcaMiscPEMGenerator(certificate));
      writer.flush();
    }
  }

  private void writeToDisk(PrivateKey key, String filename) throws IOException {
    File file = Paths.get(configuration.getKeyStoresDir().toString(), filename).toFile();
    log.debug("Saving key to {}", file);
    try (var f = new FileWriter(file)) {
      PemWriter writer = new PemWriter(f);
      writer.writeObject(new JcaMiscPEMGenerator(key));
      writer.flush();
    }
  }

  private void createServerCertificate(String host, CertificateAndKey caCert)
      throws IOException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, KeyStoreException {
    KeyPair keyPair = generateKeyPair();
    X509Certificate certificate = certificateGenerator.createServerCertificate(caCert, host,
                                                                               keyPair.getPublic());
    KeyStoreManager keyStoreManager = getKeyStore(host);
    keyStoreManager.addCertificateAndKey(host,
                                         new CertificateAndKey(keyPair.getPrivate(), certificate));
    writeToDisk(certificate, "%s.crt".formatted(host));
    writeToDisk(keyPair.getPrivate(), "%s.key".formatted(host));
  }

  private KeyStoreManager getKeyStore(String host) {
    if (keyStoresByHost.containsKey(host)) {
      return keyStoresByHost.get(host);
    }

    Path path;
    if (StringUtils.equals(host, ROOT_CA_ALIAS)) {
      path = Paths.get(configuration.getKeyStoresDir().toString(), CA_KEYSTORE_FILENAME);
    } else {
      path = null;
    }

    try {
      keyStoresByHost.put(host, new KeyStoreManager(path));
    } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    return keyStoresByHost.get(host);
  }
}
