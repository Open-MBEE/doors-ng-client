package gov.nasa.jpl.mbee.doorsng.clients;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;

public class UpdateRequirement {

	static DoorsClient doors;
	
	public static void main(String[] args) {
		String sourceRequirementURL = "https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_Hsy3AWsFEeafeva6bOHaJA";		// req 8
		String targetRequirementURL = "https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_srvQMWsfEeafeva6bOHaJA";		// req 9
		String customLinkURL = "https://doors-ng-dev.jpl.nasa.gov:9443/rm/web/mylink";		// before update, req8 points to req2
		
		
		try {
			// set up dng client
			doors = new DoorsClient("cae.integ", "GT42NYmOj-W3", "https://doors-ng-uat.jpl.nasa.gov:9443/rm/", "myMagicDrawProject");
		
			doors.addCustomLinkToExistingRequirement(sourceRequirementURL, targetRequirementURL, customLinkURL);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

//	private static void addCustomLinkToExistingRequirement(String sourceRequirementURL, String targetRequirementURL,
//			String customLinkURL) {
//		
//		// first do a GET to check if source requirement exists, and also to get its RDF representation
//		String srcReqAsRDF = doors.getRequirementAsRDF(sourceRequirementURL);
//		
//		if(srcReqAsRDF == null){
//			System.err.println("Resource does not exist and cannot be updated: " + sourceRequirementURL);
//			return;
//		}
//		
//		// parse the RDF representation of the resource as RDF model		
//		InputStream is = new ByteArrayInputStream(srcReqAsRDF.getBytes());
//		Model rdfModel = ModelFactory.createDefaultModel();
//		rdfModel.read(is, sourceRequirementURL);
//		
//		// print RDF model to console for verification
//		OutputStream outputStream = new ByteArrayOutputStream();
//		rdfModel.write(outputStream);
//		String content = outputStream.toString();
//		System.out.println(content);
//		
//		// set up RDF resources
//		Resource sourceRequirementResource = rdfModel.getResource(sourceRequirementURL);
//		Resource targetRequirementResource = rdfModel.getResource(targetRequirementURL);
//		Property customLinkProperty = rdfModel.createProperty(customLinkURL);
//		
//		// check if the requirement already has custom link value(s)
//		// if yes, delete them
//		sourceRequirementResource.removeAll(customLinkProperty);
//		
//		// add the triple describing the new custom link value		
//		sourceRequirementResource.addProperty(customLinkProperty, targetRequirementResource);
//		
//		// transform RDF model describing requirement into RDF
//		OutputStream outputStream2 = new ByteArrayOutputStream();
//		rdfModel.write(outputStream2);
//		String updatedSrcReqAsRDF = outputStream2.toString();
//		
//		// perform the update
//		int statusCode = doors.updateRequirementFromRDF(updatedSrcReqAsRDF, sourceRequirementURL);
//		System.out.println("Update statusCode:" + statusCode);
//	}

}
