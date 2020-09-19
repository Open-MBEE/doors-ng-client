package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

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

    public MmsClass createClass(String id, String name) {
        MmsClass mmsClass = new MmsClass(this, id, name);
        mmsClass.init();
        return mmsClass;
    }

    public String getAuthority() {
        return authority;
    }

}
