package gov.nasa.jpl.mbee.doorsng.MmsAdapter;

import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;

public abstract class MmsEntity {
    private boolean initialized = false;

    protected JSONObject serialization = new JSONObject();
    protected ElementFactory factory;
    protected String id;
    protected String ownerId;

    public MmsEntity(ElementFactory factory, String id, String ownerId) {
        this.factory = factory;
        this.id = id;
        this.ownerId = ownerId;
    }

    // this is needed in order to allow overriding subclasses to implement certain getters and write `null` instead of
    // deleting the property when a String is expected
    protected MmsEntity put(String key, String nullableValue) {
        if(nullableValue == null) {
            serialization.put(key, JSONObject.NULL);
        }
        else {
            serialization.put(key, nullableValue);
        }
        return this;
    }

    protected MmsEntity put(String key, Collection collection) {
        serialization.put(key, collection);
        return this;
    }

    protected MmsEntity put(String key, Object value) {
        serialization.put(key, value);
        return this;
    }

    public void init() {
        this.initialized = true;
        this
            .put("_appliedStereotypeIds", Collections.EMPTY_LIST)
            .put("documentation", "")
            .put("type", this.getType())
            .put("id", this.getId())
            .put("mdExtensionsIds", Collections.EMPTY_LIST)
            .put("ownerId", this.getOwnerId())
            .put("syncElementId", JSONObject.NULL)
            .put("appliedStereotypeInstanceId", JSONObject.NULL)
            .put("clientDependencyIds", Collections.EMPTY_LIST)
            .put("supplierDependencyIds", Collections.EMPTY_LIST)
            .put("name", this.getName())
            .put("nameExpression", JSONObject.NULL)
            .put("visibility", this.getVisibility())
            .put("templateParameterId", JSONObject.NULL)
            ;
    }

    public JSONObject getSerialization() throws IllegalStateException {
        if(!this.initialized) {
            throw new IllegalStateException("MmsEntity#init() was never called on "+this.getClass().getName());
        }
        return serialization;
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public abstract String getType();
    public abstract String getVisibility();
    public abstract String getName();

}
