package gov.nasa.jpl.mbee.doorsng.examples;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;

public class CreateRequirement {

	public static void main(String[] args) {
		DoorsClient doors;
		try {
			doors = new DoorsClient("cae.integ", "password", "https://doors-ng-uat.jpl.nasa.gov:9443/rm/",
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

			doors.createRequirementFromRDF(content);


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
