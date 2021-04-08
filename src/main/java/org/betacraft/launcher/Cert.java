package org.betacraft.launcher;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.betacraft.launcher.Logger;
import org.betacraft.launcher.Util;

import pl.betacraft.auth.DownloadRequest;
import pl.betacraft.json.cert.CertInfo;
import pl.betacraft.json.cert.CertRequest;

public class Cert {
	public static SSLSocketFactory sslSocketFactory = null;

	public static void download() {
		try {
			CertInfo cert_info = new CertRequest().perform().cert_info;
			if (Util.cert_file.exists()) {
				if (Util.getSHA1(Util.cert_file).equals(cert_info.sha1)) {
					return;
				}
			}
			if (new DownloadRequest(cert_info.url, Util.cert_file.toPath().toString(), cert_info.sha1, false).perform().result != DownloadResult.OK) {
				Logger.a("Failed to download certificate!");
			}

		} catch (Throwable t) {
			t.printStackTrace();
			Logger.printException(t);
		}
	}

	public static void prepare() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			// Create a new trust store, use getDefaultType for .jks files or "pkcs12" for .p12 files
			trustStore.load(null, null);


			// If you comment out the following, the request will fail
			trustStore.setCertificateEntry(
					"betacraft.pl",
					// To test, download the certificate from stackoverflow.com with your browser
					loadCertificate(new FileInputStream(Util.cert_file))
					);
			// Uncomment to following to add the installed certificates to the keystore as well
			addCACerts(trustStore);

			sslSocketFactory = createSSlSocketFactory(trustStore);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static SSLSocketFactory createSSlSocketFactory(KeyStore trustStore) throws GeneralSecurityException {
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);
		TrustManager[] trustManagers = tmf.getTrustManagers();

		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustManagers, null);
		return sslContext.getSocketFactory();
	}

	private static X509Certificate loadCertificate(InputStream certificateFile) throws IOException, CertificateException {
		return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(certificateFile);
	}

	private static void addCACerts(KeyStore trustStore) throws GeneralSecurityException {
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		// Loads default Root CA certificates (generally, from JAVA_HOME/lib/cacerts)
		trustManagerFactory.init((KeyStore)null);
		for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
			if (trustManager instanceof X509TrustManager) {
				for (X509Certificate acceptedIssuer : ((X509TrustManager) trustManager).getAcceptedIssuers()) {
					trustStore.setCertificateEntry(acceptedIssuer.getSubjectDN().getName(), acceptedIssuer);
				}
			}
		}
	}
}
