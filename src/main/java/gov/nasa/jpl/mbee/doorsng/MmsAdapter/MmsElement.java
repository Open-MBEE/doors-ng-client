package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

public abstract class MmsElement extends MmsEntity {

    public MmsElement(ElementFactory factory, String id, String ownerId) {
        super(factory, id, ownerId);
    }


    @Override
    public void init() {
        super.init();
        this
            .put("isLeaf", false)
            ;
    }

    @Override
    public String getVisibility() {
        return null;
    }
}