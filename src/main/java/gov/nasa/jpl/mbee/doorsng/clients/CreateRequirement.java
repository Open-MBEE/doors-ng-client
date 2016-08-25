package gov.nasa.jpl.mbee.doorsng.clients;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.HttpContext;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.httpclient.ApacheHttpClientConfig;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;
import org.eclipse.lyo.oslc4j.provider.json4j.Json4JProvidersRegistry;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import gov.nasa.jpl.mbee.doorsng.model.Folder;
import gov.nasa.jpl.mbee.doorsng.model.Requirement;

public class CreateRequirement {

	public static void main(String[] args) {
		DoorsClient doors;
		try {
			doors = new DoorsClient("cae.integ", "GT42NYmOj-W3", "https://doors-ng-uat.jpl.nasa.gov:9443/rm/",
					"myMagicDrawProject");

			Model rdfModel = ModelFactory.createDefaultModel();

			// String inputFileName =
			// "file:C:/Users/Axel/git/ecore2oslc/EcoreMetamodel2OSLCSpecification/RDF
			// Vocabulary/sysmlRDFVocabulary.rdf";
			String inputFileName = "test.rdf";
			InputStream in = FileManager.get().open(inputFileName);
			if (in == null) {
				throw new IllegalArgumentException("File: " + inputFileName + " not found");
			}

			// read the RDF/XML file as RDF model
			rdfModel.read(in, null);

			// add custom link to requirement resouce
			Resource sourceRequirementResource = rdfModel.getResource("https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_vag30WmkEeauX4BB8_MMMM");
			Resource targetRequirementResource = rdfModel.getResource("https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_v50qAWmkEeauX4BB8_HMfQ");
			Property customLinkProperty = rdfModel.createProperty("https://doors-ng-dev.jpl.nasa.gov:9443/rm/web/mylink");
			sourceRequirementResource.addProperty(customLinkProperty, targetRequirementResource);
			

			// print for verification RDF in console
			OutputStream outputStream = new ByteArrayOutputStream();
			rdfModel.write(outputStream);
			String content = outputStream.toString();

			System.out.println(content);

			// for test
			// doors.create(requirement);

			doors.createRequirementFromRDF(content);

//			// create httpclient
//			DefaultHttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
//			httpClient.setRedirectStrategy(new RedirectStrategy() {
//				@Override
//				public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
//					return null;
//				}
//
//				@Override
//				public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
//					return false;
//				}
//			});
//
//			// create clientConfig
//			ClientConfig clientConfig = new ApacheHttpClientConfig(httpClient);
//			javax.ws.rs.core.Application app = new javax.ws.rs.core.Application() {
//				@Override
//				public Set<Class<?>> getClasses() {
//					Set<Class<?>> classes = new HashSet<Class<?>>();
//					classes.addAll(JenaProvidersRegistry.getProviders());
//					classes.addAll(Json4JProvidersRegistry.getProviders());
//					return classes;
//				}
//			};
//			clientConfig = clientConfig.applications(app);
//
//			RestClient restClient = new RestClient(clientConfig);
//
//			String requirementFactory = "https://doors-ng-uat.jpl.nasa.gov:9443/rm/requirementFactory?projectURL=https://doors-ng-uat.jpl.nasa.gov:9443/rm/process/project-areas/_xYndoOZaEeWvYbfYe0sscg";
//
//			 String response =
//			 restClient.resource(requirementFactory).contentType("application/rdf+xml").accept("application/rdf+xml").header(OSLCConstants.OSLC_CORE_VERSION,"2.0").post(String.class,
//					 content);
//			 System.out.println("Test");
			
			
			
			
			// response.consumeContent();
			// if (response.getStatusCode() == HttpStatus.SC_CREATED) {
			//
			// System.out.println("POST successful");;
			//
			// }

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
