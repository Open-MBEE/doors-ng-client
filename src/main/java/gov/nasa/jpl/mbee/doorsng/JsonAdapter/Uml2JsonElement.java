package gov.nasa.jpl.mbee.doorsng.JsonAdapter;

public abstract class Uml2JsonElement extends Uml2JsonEntity {

    public Uml2JsonElement(ElementFactory factory, String id, String ownerId) {
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
