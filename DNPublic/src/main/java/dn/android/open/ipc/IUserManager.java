package dn.android.open.ipc;

import dn.android.open.ipc.lib.ClassId;

/**
 * 对应需要暴露的API
 */
@ClassId("dn.android.open.ipc.UserManager")
public interface IUserManager {
    public Person getPerson();
    public void setPerson(Person person);
}
