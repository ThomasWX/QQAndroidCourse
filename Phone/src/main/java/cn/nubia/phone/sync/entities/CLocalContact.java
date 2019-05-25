package cn.nubia.phone.sync.entities;

import java.util.ArrayList;
import java.util.List;

public class CLocalContact {
    private long contactId;
    private long rawContactId;
    private long remoteRawContactId;
    private boolean isRemoteVersionUpdated;
    private int remoteVersion;

    private long nameId;
    private boolean isNameUpdated;
    private String name;

    private List<Number> numbers;

    private boolean isSameContact;

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public long getRawContactId() {
        return rawContactId;
    }

    public void setRawContactId(long rawContactId) {
        this.rawContactId = rawContactId;
    }

    public long getRemoteRawContactId() {
        return remoteRawContactId;
    }

    public void setRemoteRawContactId(long remoteRawContactId) {
        this.remoteRawContactId = remoteRawContactId;
    }

    public boolean isRemoteVersionUpdated() {
        return isRemoteVersionUpdated;
    }

    public void setRemoteVersionUpdated(boolean remoteVersionUpdated) {
        isRemoteVersionUpdated = remoteVersionUpdated;
    }

    public int getRemoteVersion() {
        return remoteVersion;
    }

    public void setRemoteVersion(int remoteVersion) {
        this.remoteVersion = remoteVersion;
    }

    public long getNameId() {
        return nameId;
    }

    public void setNameId(long nameId) {
        this.nameId = nameId;
    }

    public boolean isNameUpdated() {
        return isNameUpdated;
    }

    public void setNameUpdated(boolean nameUpdated) {
        isNameUpdated = nameUpdated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Number> getNumbers() {
        if (numbers == null) {
            numbers = new ArrayList<>();
        }
        return numbers;
    }

    public void addNumber(Number number) {
        if (this.numbers == null) {
            this.numbers = new ArrayList<>();
        }
        this.numbers.add(number);
    }

    public void setSameContact(boolean sameContact) {
        isSameContact = sameContact;
    }

    public boolean isCanDelete() {
        return !(isSameContact || isRemoteVersionUpdated);
    }
}
