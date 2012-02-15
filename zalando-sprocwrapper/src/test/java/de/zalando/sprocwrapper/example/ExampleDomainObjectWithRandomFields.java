package de.zalando.sprocwrapper.example;

import java.util.List;

import com.typemapper.annotations.DatabaseField;

public class ExampleDomainObjectWithRandomFields {
    @DatabaseField
    private String z;

    @DatabaseField
    private String y;

    @DatabaseField
    private String x;

    @DatabaseField
    private ExampleDomainObjectWithRandomFieldsInner innerObject;

    @DatabaseField
    private List<ExampleDomainObjectWithRandomFieldsInner> list;

    public ExampleDomainObjectWithRandomFields() { }

    public ExampleDomainObjectWithRandomFields(final String x, final String y, final String z) {
        this.x = x;
        this.y = y;
        this.z = z;

    }

    public ExampleDomainObjectWithRandomFieldsInner getInnerObject() {
        return innerObject;
    }

    public void setInnerObject(final ExampleDomainObjectWithRandomFieldsInner innerObject) {
        this.innerObject = innerObject;
    }

    public String getX() {
        return x;
    }

    public void setX(final String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(final String y) {
        this.y = y;
    }

    public String getZ() {
        return z;
    }

    public void setZ(final String z) {
        this.z = z;
    }

    public List<ExampleDomainObjectWithRandomFieldsInner> getList() {
        return list;
    }

    public void setList(final List<ExampleDomainObjectWithRandomFieldsInner> list) {
        this.list = list;
    }

}
