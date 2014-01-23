package de.unipassau.mics.termviewer;

/**
 * Created by wmb on 23.01.14.
 */
public class ActivityDescriptor {

    private String name;
    private String contentUri;
    private String field;


    public ActivityDescriptor(String name, String contentUriString, String field) {
        this.name = name;
        this.contentUri = contentUriString;
        this.field = field;
    }


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getContentUri() {
        return contentUri;
    }

    public void setContentUri(String contentUri) {
        this.contentUri = contentUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
