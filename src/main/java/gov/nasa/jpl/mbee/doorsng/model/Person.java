package gov.nasa.jpl.mbee.doorsng.model;

import org.eclipse.lyo.client.oslc.resources.RmConstants;
import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcName;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcReadOnly;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;

/**
 * A person.
 * @author han
 */
@OslcNamespace("http://xmlns.com/foaf/0.1/")
@OslcResourceShape(title = "Person Resource Shape", describes = RmConstants.TYPE_PERSON)
public class Person extends AbstractResource {

  private String name;

  @OslcDescription("Tag or keyword for a resource. Each occurrence of a dcterms:name property denotes an additional tag for the resource.")
  @OslcPropertyDefinition("http://xmlns.com/foaf/0.1/name")
  @OslcReadOnly
  @OslcTitle("Name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}