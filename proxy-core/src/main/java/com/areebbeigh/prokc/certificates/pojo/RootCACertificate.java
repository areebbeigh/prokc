package com.areebbeigh.prokc.certificates.pojo;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public record RootCACertificate(PrivateKey privateKey, X509Certificate certificate) {

}
