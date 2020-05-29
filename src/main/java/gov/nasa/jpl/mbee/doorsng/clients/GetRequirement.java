package gov.nasa.jpl.mbee.doorsng.clients;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.wink.client.ClientResponse;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import gov.nasa.jpl.mbee.doorsng.model.Folder;
import gov.nasa.jpl.mbee.doorsng.model.Requirement;

public class GetRequirement {

	public static void main(String[] args) {
		DoorsClient doors;
		try {
			doors = new DoorsClient("cae.integ", "GT42NYmOj-W3", "https://doors-ng-uat.jpl.nasa.gov:9443/rm/", "myMagicDrawProject");
			
			for (Requirement requirement : doors.getRequirements()) {
				System.out.println("***********************");
				System.out.println(requirement.getTitle());
				
				ClientResponse response = doors.client.getResource(requirement.getResourceUrl());
				
				InputStream is = response.getEntity(InputStream.class);
				Model rdfModel = ModelFactory.createDefaultModel();
				rdfModel.read(is,requirement.getResourceUrl());
				rdfModel.write(new FileOutputStream(new File(requirement.getTitle() + ".rdf")));
				
				// list the statements in the Model
				StmtIterator iter = rdfModel.listStatements();

				// print out the predicate, subject and object of each statement
				while (iter.hasNext()) {
				    Statement stmt      = iter.nextStatement();  // get next statement
				    Resource subject   = stmt.getSubject();     // get the subject
				    Property predicate = stmt.getPredicate();   // get the predicate
				    RDFNode object    = stmt.getObject();      // get the object

				    System.out.print(subject.toString());
				    System.out.print(" " + predicate.toString() + " ");
				    if (object instanceof Resource) {
				       System.out.print(object.toString());
				    } else {
				        // object is a literal
				        System.out.print(" \"" + object.toString() + "\"");
				    }

				    System.out.println(" .");
				} 
				
				
//				System.out.println(requirement.getAbout());
//				System.out.println(requirement.getTitle());
//				System.out.println(requirement.getPrimaryText());
//				System.out.println(requirement.getServiceProvider());
//				System.out.println(requirement.getParent());
			
			};
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
