package dn.android.architecture.db.framework;

import dn.android.architecture.db.framework.annotation.DbField;
import dn.android.architecture.db.framework.annotation.DbTable;

@DbTable("t_user")
public class User {
    @DbField("user_name")
    private String name;
}
