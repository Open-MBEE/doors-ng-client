package gov.nasa.jpl.mbee.doorsng.JsonAdapter;

import java.util.Collections;

public class Uml2JsonAssociation extends Uml2JsonClass {
    protected String targetId;
    public Uml2JsonAssociation(ElementFactory factory, String id, String ownerId, String targetId) {
        super(factory, id, ownerId);
        this.targetId = targetId;
    }


    public void init() {
        super.init();
        this
            .put("isDerived", false)
            .put("memberEndIds", new String[] {ownerId, targetId})
            .put("ownedEndIds", Collections.EMPTY_LIST)
            .put("navigableOwnedEndIds", Collections.EMPTY_LIST)
            ;
    }

    @Override
    public String getType() {
        return "Association";
    }

}
