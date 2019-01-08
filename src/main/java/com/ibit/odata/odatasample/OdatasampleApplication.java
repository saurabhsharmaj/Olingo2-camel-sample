package com.ibit.odata.odatasample;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.olingo2.Olingo2Component;
import org.apache.camel.component.olingo2.Olingo2Configuration;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OdatasampleApplication {
//http://services.odata.org/OData/OData.svc
	static SSLContext sc;
	public static void allowHttps() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

        } };

        sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) { return true; }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        /* End of the fix*/
		
	}
	
	static String auth = "Authorization" ;
	static String credentials = "";
	static String encodedCredentials ="Basic " + Base64.encodeBase64String(credentials.getBytes());
	
	protected CamelContext createCamelContext() throws Exception {

        final CamelContext context = new DefaultCamelContext();
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("serviceUri", "https://services.odata.org/V2/(S(vo4m1jhe5cwdqcnquhrbgx1z))/OData/OData.svc/");
        options.put("contentType","application/json;charset=utf-8");
        final Olingo2Configuration configuration = new Olingo2Configuration();
        IntrospectionSupport.setProperties(configuration, options);

        // add OlingoComponent to Camel context
        final Olingo2Component component = new Olingo2Component(context);
        component.setConfiguration(configuration);
        component.setUseGlobalSslContextParameters(false);
        context.addComponent("olingo2", component);

        return context;
    }

	
	public static void main(String[] args) throws Exception {
		//SpringApplication.run(OdatasampleApplication.class, args);
		 allowHttps();
		CamelContext camelContext = new OdatasampleApplication().createCamelContext();
		final Map<String, Object> headers = new HashMap<String, Object>();		
		try {
			camelContext.addRoutes(new RouteBuilder() {
	            public void configure() {
	                // test routes for read
	                from("direct://READENTRY")
	               // .setHeader(auth, simple(encodedCredentials))
	                    .to("olingo2://read/Products");

	                from("timer://Products?delay=2000")
	                .to("direct://READENTRY")	               
//			    	.split(body())
			    	
					.log(body().toString());
	            }
	        });
			
			camelContext.start();
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}


}

