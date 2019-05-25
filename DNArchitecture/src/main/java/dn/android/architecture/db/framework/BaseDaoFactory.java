package dn.android.architecture.db.framework;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;

public class BaseDaoFactory {
//    private String dbPath;
//    private SQLiteDatabase db;
//    private static BaseDaoFactory instance = new BaseDaoFactory();
//
//    public BaseDaoFactory() {
//        dbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/teacher.db";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
//        } else {
//
//        }
//    }
//
//    public synchronized <T extends BaseDao<M>, M> T getDatabaseHelper(Class<T> clazz, Class<M> entityClz) {
//        BaseDao dao = null;
//        try {
//            dao = clazz.newInstance();
//            dao.init(entityClz, db);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        }
//        return (T)dao;
//    }
//
//    public static BaseDaoFactory getInstance(){
//        return instance;
//    }
}
