package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

public class ElementFactory {
    protected String projectId;

    public ElementFactory(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public MmsClass createClass(String id, String name) {
        MmsClass mmsClass = new MmsClass(this, id, name);
        mmsClass.init();
        return mmsClass;
    }

}
