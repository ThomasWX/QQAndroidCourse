package dn.android.architecture.db.framework;

import android.database.sqlite.SQLiteDatabase;

public class BaseDao {
    public <M> void init(Class<M> entityClz, SQLiteDatabase db) {
    }
}
