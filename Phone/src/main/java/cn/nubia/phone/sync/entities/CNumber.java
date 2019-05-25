package cn.nubia.phone.sync.entities;

public class CNumber {
    private long rawContactId; // Local
    private long dataId; // Local
    private long remoteDataId;

    private int version; // Local
    private boolean isRemoteVersionUpdated;
    private int remoteVersion;


    private boolean isNumberUpdated;
    private String number;
    private boolean isPhoneTypeUpdated;
    private int phoneType;
    private boolean isCustomUpdated;
    private String custom;

    private boolean isNewNumber;
    private boolean isSameNumber;

    public long getDataId() {
        return dataId;
    }

    public void setDataId(long dataId) {
        this.dataId = dataId;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public boolean isCustomUpdated() {
        return isCustomUpdated;
    }

    public void setCustomUpdated(boolean customUpdated) {
        isCustomUpdated = customUpdated;
    }

    public int getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(int phoneType) {
        this.phoneType = phoneType;
    }

    public boolean isPhoneTypeUpdated() {
        return isPhoneTypeUpdated;
    }

    public void setPhoneTypeUpdated(boolean phoneTypeUpdated) {
        isPhoneTypeUpdated = phoneTypeUpdated;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isNumberUpdated() {
        return isNumberUpdated;
    }

    public void setNumberUpdated(boolean numberUpdated) {
        isNumberUpdated = numberUpdated;
    }

    public long getRawContactId() {
        return rawContactId;
    }

    public void setRawContactId(long rawContactId) {
        this.rawContactId = rawContactId;
    }

    public int getRemoteVersion() {
        return remoteVersion;
    }

    public void setRemoteVersion(int remoteVersion) {
        this.remoteVersion = remoteVersion;
    }

    public boolean isRemoteVersionUpdated() {
        return isRemoteVersionUpdated;
    }

    public void setRemoteVersionUpdated(boolean remoteVersionUpdated) {
        isRemoteVersionUpdated = remoteVersionUpdated;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getRemoteDataId() {
        return remoteDataId;
    }

    public void setRemoteDataId(long remoteDataId) {
        this.remoteDataId = remoteDataId;
    }

    public boolean isNewNumber() {
        return isNewNumber;
    }

    public void setNewNumber(boolean newNumber) {
        isNewNumber = newNumber;
    }


    public void setSameNumber(boolean sameNumber) {
        isSameNumber = sameNumber;
    }

    public boolean isCanDelete() {
        return !(isNewNumber || isSameNumber || isRemoteVersionUpdated);
    }
}
