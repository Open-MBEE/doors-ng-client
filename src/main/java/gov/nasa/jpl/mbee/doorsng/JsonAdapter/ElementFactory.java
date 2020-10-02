package gov.nasa.jpl.mbee.doorsng.JsonAdapter;

import java.net.URI;
import java.net.URISyntaxException;

public class ElementFactory {
    protected String projectId;
    protected String authority;

    public ElementFactory(String projectId, String doorsUrl) throws URISyntaxException {
        this.projectId = projectId;
        this.authority = (new URI(doorsUrl)).getAuthority();
    }

    public String getProjectId() {
        return projectId;
    }

    public Uml2JsonClass createClass(String id, String name) {
        Uml2JsonClass uml2JsonClass = new Uml2JsonClass(this, id, name);
        uml2JsonClass.init();
        return uml2JsonClass;
    }

    public String getAuthority() {
        return authority;
    }


    public String localResourceUriToElementId(URI requirement) throws RuntimeException {
        if(!requirement.getAuthority().equals(getAuthority())) {
            throw new RuntimeException(String.format("Cannot convert URI <%s> to element id; resource is not on same authority", requirement.toString()));
        }

        return requirement.getPath().replace('/', '_');
//        String path = requirement.getPath();
//        return path.substring(path.lastIndexOf("/")+1);
    }

}
