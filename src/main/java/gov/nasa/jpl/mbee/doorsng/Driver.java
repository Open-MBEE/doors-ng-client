package gov.nasa.jpl.mbee.doorsng;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.lyo.oslc4j.core.model.Link;

import gov.nasa.jpl.mbee.doorsng.model.Requirement;

public class Driver {
    
    
    
    public static void main (String[] args) {
        
        
        try {
            
            DoorsClient doors = new DoorsClient("cae.integ", "GT42NYmOj-W3", "https://doors-ng-uat.jpl.nasa.gov:9443/rm",
                            "customTypesTest");
            
            Requirement[] rs = doors.getRequirements();

            for(int u=0; u<rs.length;u++) {
            
                if( rs[u].getTitle().contains("SPACE")) {
                    
                    int g = doors.artifactHasLinks(rs[u].getResourceUrl(),"customTypesTest");
                    
                    //HashMap<String,HashMap<String,ArrayList<String>>> links =  doors.getArtifactLinkTarget(rs[u].getResourceUrl(),"customTypesTest");
                    System.out.println(g);

                }
            

            }
            
            /*URI u = new URI("http://open-services.net/ns/rm#optimizes");
            
            Requirement[] rs = doors.getRequirements();
            
            Requirement r = null;
            
            for(int i=0; i < rs.length; i++) {
                r = rs[i];
                String n = r.getTitle();
               String s3 = doors.getRequirementAsRDF(r.getResourceUrl());
                System.out.println();
            }
            
            Link l = new Link(u);
            
            String labelName = l.getLabel();
            
            r.getExtendedProperties();*/
            //boolean hasLinks = doors.artifactHasLinks(r.getResourceUrl(),"customTypesTest");
            
            //System.out.println("dnk");
            
            //String s = doors.getCustomLinkRDF(rs[0].getInstanceShape().toString())
                            
            //String s2 = doors.getRequirementAsRDF("https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_zIRkgXIfEeaRI-62F4SlNA");
            
            
            
            /*rs[0].getInstanceShape();
            
            rs[0].getE
            
            Link l = new Link(u);
            
            URI lu = l.getValue();
            
            String s = doors.getCustomLinkRDF(rs[0].getInstanceShape().toString());
            */
            
            
            /*URI rdf_uri = l.getValue();
            
            String k = rdf_uri.toString();
            
            l.getValue();*/
            
            /*
            boolean doesArtifactTypeExists = doors.doesArtifactTypeExist("command");
        Requirement[] r = doors.getRequirements();
        
        String sysmlId = r[1].getCustomField(doors.getField("sysmlid"));
        
        System.out.println(sysmlId);
        
        
        */
        
        
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        
        
    }
    

}
