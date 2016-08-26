package gov.nasa.jpl.mbee.doorsng;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.Date;

import org.eclipse.lyo.oslc4j.core.model.Link;

import gov.nasa.jpl.mbee.doorsng.model.Requirement;

public class Driver {
    
    
    public static void main(String[] args) {
        
        
        String path = System.getProperty("user.dir").replace('\\','\\');
        System.out.println(System.getProperty("user.dir"));
        
        /*try {
        RandomAccessFile raf = new RandomAccessFile("ReqTest.java", "rw");
        long length = raf.length();
        System.out.println("File Length="+raf.length());
        //supposing that last line is of 8 
        raf.setLength(length - 8);
        System.out.println("File Length="+raf.length());
        raf.close();
        }catch(Exception ex){
        ex.printStackTrace();
        }
        */
        
        
        /*BufferedWriter bw = null;

        try {
           // APPEND MODE SET HERE
           bw = new BufferedWriter(new FileWriter("ReqTest.java", true));
       bw.write("400:08311998:Inprise Corporation:249.95");
       bw.newLine();
       bw.flush();
        } catch (IOException ioe) {
       ioe.printStackTrace();
        } finally {                       // always close the file
       if (bw != null) try {
          bw.close();
       } catch (IOException ioe2) {
          // just ignore it
       }
        } // end try/catch/finally
        
        */
        
        try {
            
            /*DoorsClient client = new DoorsClient( "cae.integ",
                                                  "GT42NYmOj-W3",
                                                  "https://doors-ng-uat.jpl.nasa.gov:9443/rm",
                                                  "smallVandV",
                                                  "vandv:verificationItem");
                        
            
            
            boolean a = client.doesAttributeExist("vandv:verificationItem","verifiedByInheritance");
            
            System.out.println(a);
            */
            
            
            /*Requirement doorsReq = new Requirement();
            doorsReq.setTitle( "ReqG" );
            doorsReq.setDescription( "TBD ..." );
            Date d = new Date("AUG 11 2016 11:33:33");
            doorsReq.setModified( d );
            
            doorsReq.setCustomField( client.getField( "sysmlid" ), "_23C_" );

            
            String uri1 = client.create(doorsReq);
            
            System.out.println(uri1);
            
            Requirement doorsReq2 = new Requirement();
            doorsReq2.setTitle( "ReqH" );
            doorsReq2.setDescription( "TBD ......" );
            Date d2 = new Date("AUG 11 2016 33:33:33");
            doorsReq2.setModified( d2 );
            
            doorsReq2.setCustomField( client.getField( "sysmlid" ), "_23D_" );

            String uri2 = client.create(doorsReq2);
            
            System.out.println(uri2);*/
            
            /*Requirement r1 = client.getRequirement("https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_f36fEWAAEeaItOc2ksOh0A");
            
            r1.setResourceUrl("https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_f36fEWAAEeaItOc2ksOh0A");
            
            Requirement r2 = client.getRequirement("https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_jbxAUWAAEeaItOc2ksOh0A");

            r2.setResourceUrl("https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_jbxAUWAAEeaItOc2ksOh0A");
            
            
            r2.addConstrainedBy(
                            new Link(new URI("https://doors-ng-uat.jpl.nasa.gov:9443/rm/resources/_f36fEWAAEeaItOc2ksOh0A")));
            
            System.out.println("test");*/
            
            
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        
            
           // boolean p = client.doesProjectExists( "myMagicDr" );
            
            //boolean a = client.doesArtifactTypeExist( "vandv1" );
            
            
            //boolean s = client.doesSysmlIdExist( "VandV", "sysmlid");
            
            //boolean att = client.doorsAttributeExists("customTypesTest", "Requirement", "AFID");
            
            //Requirement doorsReq = new Requirement();
            
           /* URI uri = null; //uri of existing requirement in DNG
            
            doorsReq = new Requirement();
            Link l = new Link(uri,"Generalizes"); //link label that I created
            doorsReq.addElaboratedBy(l); //test if using elaborated by with a custom link declaration will create an elaborated By link or custom link
            
            //boolean a = client.do
            
            System.out.println( s );*/
            
            /*Requirement reqs[] = client.getRequirements();
            
            int k = reqs.length;
            
            System.out.println(k);
            
            Requirement doorsReq = new Requirement();

            doorsReq.setTitle( "CUSTOM TTYPE HOPEFULLY" );
            doorsReq.setDescription( "description" );
            Date d = new Date("AUG 08 2016 11:24:30");
            doorsReq.setModified( d );
            
            //URI u = client.getField( "sysmlid" );

            doorsReq.setCustomField( client.getField( "sysmlid" ), "XYZ" );
            
            String s = doorsReq.getCustomField( client.getField("sysmlid"));

            System.out.println( s );

            //doorsReq.setParent( URI.create( null ) );
            
            client.create(doorsReq);*/
            
            
       /* } catch ( Exception e ) {
            e.printStackTrace();
        }*/
        
    }
    

}
