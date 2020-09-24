package gov.nasa.jpl.mbee.doorsng.model;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import gov.nasa.jpl.mbee.doorsng.MmsAdapter.ElementFactory;
import gov.nasa.jpl.mbee.doorsng.MmsAdapter.MmsClass;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.eclipse.lyo.client.oslc.resources.RmConstants;
import org.eclipse.lyo.oslc4j.core.annotation.*;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.eclipse.lyo.oslc4j.core.model.ValueType;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@OslcNamespace(RmConstants.REQUIREMENTS_MANAGEMENT_NAMESPACE)
@OslcResourceShape(title = "Requirement Resource Shape", describes = RmConstants.TYPE_REQUIREMENT)
public class Requirement extends org.eclipse.lyo.client.oslc.resources.Requirement
{
    private static final String DC_TITLE = "http://purl.org/dc/terms/title";
    private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static final String FOAF_PERSON = "http://xmlns.com/foaf/0.1/Person";
    private static final String FOAF_NAME = "http://xmlns.com/foaf/0.1/name";
    private static final String NULL_TITLE_POINTER = "";

    private String parseSimpleTitle(Model model, Resource subject) {
        Statement dcTitle = model.getProperty(subject, model.createProperty(DC_TITLE));
        if (dcTitle != null) {
            return dcTitle.getString();
        }
        else {
            Statement rdfsLabel = model.getProperty(subject, model.createProperty(RDFS_LABEL));
            if (rdfsLabel != null) {
                return rdfsLabel.getString();
            }
        }

        return null;
    }

    private String fetchResourceTitle(DoorsClient doors, ElementFactory factory, URI resource) {
        // remote URI or non-fetcahble local URI
        if(!resource.getAuthority().equals(factory.getAuthority()) || !resource.getPath().startsWith("/rm/")) {
            return resource.getPath().replaceFirst("^(.*?)([^/#]*)$", "$2");
        }

        Model model = doors.prepareModel(resource);
        Resource subject = model.createResource(resource.toString());

        // typed thing
        Statement typed = model.getProperty(subject, model.createProperty(RDF_TYPE));
        if (typed != null) {
            String type = typed.getObject().asResource().toString();

            // person
            if (type.equals(FOAF_PERSON)) {
                // TODO: make nested attributes?
                Statement name = model.getProperty(subject, model.createProperty(FOAF_NAME));
                if(name != null) {
                    return name.getString();
                }

                // continue onto title
            }
        }

        String title = parseSimpleTitle(model, subject);

        if (title != null) {
            System.err.println("\t - titled thing: \"" + title + "\"");
            return title;
        } else {
            System.err.println("\tNo titles for <"+resource.toString()+">: \"" + doors.getResponse(resource.toString()).getEntity(String.class) + "\"");
            return NULL_TITLE_POINTER;
        }
    }

    private final Set<URI> rdfTypes = new TreeSet<URI>();
    private Map<QName, Object> extended = new HashMap<QName, Object>();
    private String resourceUrl;
    private String eTag;

    private String primaryText;

    public Requirement()
    {
        super();

        // Only add the type if Requirement is the created object
        if ( ! ( this instanceof RequirementCollection ) ) {
            rdfTypes.add(URI.create(RmConstants.TYPE_REQUIREMENT));
        }
    }

    public Requirement(final URI about)
    {
        super(about);

        // Only add the type if Requirement is the created object
        if ( ! ( this instanceof RequirementCollection ) ) {
            rdfTypes.add(URI.create(RmConstants.TYPE_REQUIREMENT));
        }
    }

    public String getCustomField(URI property) {
        try {
            return this.getExtendedProperties().get(convertUriToQname(property)).toString();
        } catch (Exception e) {
            return null;
        }
    }

    public Object getCustomFieldObject(URI property) {
        try {
            return this.getExtendedProperties().get(convertUriToQname(property));
        } catch (Exception e) {
            return null;
        }
    }
    public void setCustomField(URI property, Object value) {
        extended.put(convertUriToQname(property), value);
        this.setExtendedProperties(extended);
    }

    public String getParent() {
        Object parent = this.getExtendedProperties().get(RmConstants.PROPERTY_PARENT_FOLDER);
        if (parent != null) {
            return parent.toString();
        }
        return null;
    }

    public void setParent(final URI parent) {
        extended.put(RmConstants.PROPERTY_PARENT_FOLDER, parent);
        this.setExtendedProperties(extended);
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(final String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getEtag() {
        return eTag;
    }

    public void setEtag(final String eTag) {
        this.eTag = eTag;
    }

    private static QName convertUriToQname(URI uri) {
        String path = uri.getPath();
        String last = path.substring(path.lastIndexOf('/') + 1);
        String url = uri.toString().replace(last, "");
        return new QName(url, last);
    }

    @OslcDescription("Primary Text field.")
    @OslcPropertyDefinition("http://jazz.net/ns/rm#primaryText")
    @OslcTitle("Primary Text")
    @OslcValueType(ValueType.String)
    public String getPrimaryText() {
        return this.primaryText;
    }

    public void setPrimaryText(String primaryText) {
        this.primaryText = primaryText;
    }


    public List<JSONObject> export(DoorsClient doors, ElementFactory factory, Map<URI, ResourceShape> resourceShapeCache) throws URISyntaxException, RuntimeException {
        MmsClass requirement = factory.createClass(factory.localResourceUriToElementId(getAbout()), getTitle());

        System.err.println("<"+getAbout()+">");

        // basic properties
        requirement
            .addStringProperty("about", "About", getAbout().toString())
            .addStringProperty("shape", "Shape", fetchResourceTitle(doors, factory, getInstanceShape()))
            .addStringProperty("identifier", "Identifier", getIdentifier())
            .addStringProperty("description", "Description", getDescription())
            .addStringProperty("primaryText", "Primary Text", getPrimaryText())
            .addStringProperty("created", "Date Created", getCreated().toString())
            .addStringProperty("modified", "Date Last Modified", getModified().toString())
            .addLinks("affectedBy", "Affected By", getAffectedBy())
            .addLinks("constrainedBy", "Constrained By", getConstrainedBy())
            .addLinks("constrains", "Constrains", getConstrains())
            .addLinks("decomposedBy", "Decomposed By", getDecomposedBy())
            .addLinks("decomposes", "Decomposes", getDecomposes())
            .addLinks("elaboratedBy", "Elaborated By", getElaboratedBy())
            .addLinks("elaborates", "Elaborates", getElaborates())
            .addLinks("implementedBy", "Implemented By", getImplementedBy())
            .addLinks("satisfiedBy", "Satisfied By", getSatisfiedBy())
            .addLinks("satisfies", "Satisfies", getSatisfies())
            .addLinks("specifiedBy", "Specified By", getSpecifiedBy())
            .addLinks("specifies", "Specifies", getSpecifies())
            .addLinks("trackedBy", "Tracked By", getTrackedBy())
            .addLinks("validatedBy", "Validated By", getValidatedBy())
            ;

        // add subjects
        requirement.addStringArrayProperty("subjects", "Subjects", Arrays.stream(getSubjects()).collect(Collectors.toList()));

        // add types (use fragment part of IRI for name)
        requirement.addStringArrayProperty("types", "Types", getTypes().stream()
            .map(uri -> fetchResourceTitle(doors, factory, uri))
            .collect(Collectors.toList()));

        // add creators
        requirement.addStringArrayProperty("creators", "Creators", Arrays.stream(getCreators()).map(uri -> {
            String path = uri.getPath();
            return path.substring(path.lastIndexOf("/")+1);
        }).collect(Collectors.toList()));


        URI shapeUri = getInstanceShape();
        ResourceShape resourceShape = resourceShapeCache.get(shapeUri);
        if(resourceShape == null) {
            resourceShape = doors.getResource(ResourceShape.class, shapeUri);
            resourceShapeCache.put(shapeUri, resourceShape);
        }

        // each extended property
        Map<QName, Object> properties = this.getExtendedProperties();
        for(Map.Entry<QName, Object> property: properties.entrySet()) {
            // uri of the property
            QName qname = property.getKey();
            URI propertyUri = new URI(qname.getNamespaceURI()+qname.getLocalPart());

            // create deterministic id of propertyUri for it's JSON key
            String propertyId = propertyUri.toString();

            // fetch property's label
            Property propertyResource = resourceShape.getProperty(propertyUri);
            String propertyLabel;
            if(propertyResource == null || propertyResource.getTitle() == null) {
                propertyLabel = qname.getPrefix()+":"+qname.getLocalPart();
            }
            else {
                propertyLabel = propertyResource.getTitle();
            }

            // ref value
            Object valueObject = property.getValue();

            // verbose debugging
//            System.err.println("["+propertyUri.toString()+" ==> "+valueObject.toString()+"]");
            System.err.printf("~~ (%s) \"%s\" <%s>%n", valueObject == null? "null": valueObject.getClass().getName(), propertyLabel, propertyUri);

            if(valueObject instanceof String) {
                requirement.addStringProperty(propertyId, propertyLabel, (String) valueObject);
            }
            else if(valueObject instanceof URI) {
                URI valueUri = (URI) valueObject;
                System.err.println("\tTesting <"+valueUri.toString()+">");
                // local
                if(valueUri.getAuthority().equals(factory.getAuthority())) {
                    String path = valueUri.getPath();

                    // artifact
                    if(path.startsWith("/rm/resources/")) {
                        Requirement link = doors.getResource(Requirement.class, valueUri);

                        System.err.println("\t - requirement: " + valueUri.toString());

                        String targetId = factory.localResourceUriToElementId(valueUri);
                        requirement.addRelation(propertyId, propertyLabel, targetId);
                    }
                    // these URLs don't support RDF, can't get any titles for them
                    else if(path.startsWith("/rm/process/") || path.startsWith("/rm/cm/") || path.startsWith("/rm/accessControl/")) {
                        System.err.println("Skipping <"+valueUri.toString()+">");
                        continue;
                    }
                    // other
                    else {
                        String title = fetchResourceTitle(doors, factory, valueUri);

                        // skip
                        if(title == null) {
                            continue;
                        }
                        // add as null property
                        else if(title == NULL_TITLE_POINTER) {
                            requirement.addNullProperty(propertyId, propertyLabel);
                        }
                        // add as string property
                        else {
                            requirement.addStringProperty(propertyId, propertyLabel, title);
                        }
                    }
                }
                // remote
                else {
                    String title = fetchResourceTitle(doors, factory, valueUri);
                    requirement.addStringProperty(propertyId, propertyLabel, title);
                }
            }
            else if(valueObject instanceof Integer) {
                requirement.addIntegerProperty(propertyId, valueObject.toString(), (Integer) valueObject);
            }
            else if(valueObject instanceof Double) {
                requirement.addRealProperty(propertyId, propertyLabel, (Double) valueObject);
            }
            else if(valueObject instanceof Date) {
                requirement.addStringProperty(propertyId, propertyLabel, ((Date) valueObject).toString());
            }
            else if(valueObject instanceof ArrayList) {
                ArrayList<Object> itemObjects = (ArrayList) valueObject;

                // empty list
                if(itemObjects.size() == 0) {
                    requirement.addStringArrayProperty(propertyId, propertyLabel, Collections.EMPTY_LIST);
                    continue;
                }

                // URIs
                if(itemObjects.get(0) instanceof URI) {
                    ArrayList<URI> itemUris = (ArrayList) itemObjects;
                    ArrayList<String> itemStrings = new ArrayList<>();

                    // each item
                    for (URI valueUri : itemUris) {
                        // local
                        if (valueUri.getAuthority().equals(factory.getAuthority())) {
                            // requirement
                            if(valueUri.getPath().startsWith("/rm/resources/")) {
                                Requirement link = doors.getResource(Requirement.class, valueUri);

                                System.err.println("\t - requirement: " + valueUri.toString());

                                String targetId = factory.localResourceUriToElementId(valueUri);
                                itemStrings.add(targetId);
                                continue;
                            }
                            // other
                            else {
                                String title = fetchResourceTitle(doors, factory, valueUri);

                                itemStrings.add(title);
                                continue;
                            }
                        }

                        itemStrings.add(valueUri.toString());
                    }

                    requirement.addStringArrayProperty(propertyId, propertyLabel, itemStrings);
                }
                else {
                    throw new RuntimeException(String.format("Encountered ArrayList<%s> value type for property \"%s\" <%s>", itemObjects.get(0).getClass().getName(), propertyLabel, propertyUri));
                }
            }
            else if(valueObject == null) {
                throw new RuntimeException(String.format("Encountered null value type for property \"%s\" <%s>", propertyLabel, propertyUri));
            }
            else {
                throw new RuntimeException(String.format("Value type of custom property \"%s\" (%s) does not match known datatypes", propertyLabel, valueObject.getClass().getName()));
            }
        }

        return requirement.getClassSerialization();
    }
}
