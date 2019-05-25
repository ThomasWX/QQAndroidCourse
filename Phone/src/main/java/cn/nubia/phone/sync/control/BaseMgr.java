package cn.nubia.phone.sync.control;

public abstract class BaseMgr {
    public abstract String getCommand();
    public abstract boolean parse(String jsonStr, int messageid);
    public abstract boolean parse(byte[] data, int messageId);
}
