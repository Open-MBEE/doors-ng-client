package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

import java.util.Collections;

public class MmsAssociation extends MmsClass {
    protected String targetId;
    public MmsAssociation(ElementFactory factory, String id, String ownerId, String targetId) {
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
