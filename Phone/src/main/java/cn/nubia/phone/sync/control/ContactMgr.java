package cn.nubia.phone.sync.control;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cn.nubia.wear.control.ControlApplication;
import cn.nubia.wear.control.sync.BaseMgr;
import cn.nubia.wear.control.sync.Constant;
import cn.nubia.wear.control.sync.ServerSyncManager;
import cn.nubia.wear.control.utils.LogUtils;

public class ContactMgr extends BaseMgr {
    private static final String TAG = "ContactMgr";
    private final String[] PROJECTION = new String[]{
            RawContacts._ID,
            "remote_raw_contact_id",
            "remote_version"
    };
    private final int RAW_ID = 0;
    private final int REMOTE_RAW_ID = 1;
    private final int REMOTE_VERSION = 2;

    final String[] PROJECTION_DATA = new String[]{
            Data._ID,
            Data.MIMETYPE,
            Data.DATA1,
            Data.DATA2,
            Data.RAW_CONTACT_ID
    };
    private final int DATA_ID = 0;
    private final int DATA_MIMETYPE = 1;
    private final int DATA1 = 2;
    private final int DATA2 = 3;
    private final int RAW_CONTACT_ID = 4;
    private StringBuilder mSelection;
    private StringBuilder mData;
    private HashMap<Long, Integer> mRemoteId_remoteVersion;//<remote_raw_contact_id, remote_version>
    private HashMap<Long, Long> mRemoteId_rawId;//<remote_raw_contact_id, raw_contact_id>
    private HashMap<Long, ContactData> mContacts = new HashMap<>();//<raw_contact_id, ContactData>

    public ContactMgr() {
        init();
    }

    public void init() {
    }

    @Override
    public String getCommand() {
        return Constant.COMMAND_CONTACT;
    }

    @Override
    public boolean parse(String jsonStr, int messageid) {
        return false;
    }

    @Override
    public boolean parse(byte[] data, int messageId) {
        sendReplyMessage(messageId);
        initIdVersion();
        initData(data);
    	data =null;
        new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				LogUtils.d(TAG, "parse start");
		        syncContacts();
		        LogUtils.d(TAG, "parse end");
			}
		}).start();
        return true;
    }

    private boolean sendReplyMessage(int messageId) {
        try {
            JSONObject datajson = new JSONObject();

            JSONObject myjson = new JSONObject();
            myjson.put(Constant.JSON_KEY_DATA, datajson);
            myjson.put(Constant.JSON_KEY_COMMAND, Constant.COMMAND_CONTACT);
            String message = myjson.toString();

            ServerSyncManager.getInstance().sendMsg(message, messageId);
            return true;
        } catch (Exception ex) {
            LogUtils.d(TAG, "sendReplyMessage Exception e = " + ex.getMessage());
            return false;

        }
    }

    private void initIdVersion() {
        if (mRemoteId_rawId != null) {
            mRemoteId_rawId.clear();
        } else {
            mRemoteId_rawId = new HashMap<>();
        }
        if (mRemoteId_remoteVersion != null) {
            mRemoteId_remoteVersion.clear();
        } else {
            mRemoteId_remoteVersion = new HashMap<>();
        }

        Cursor cursor = null;
        try {
            String selection = "deleted=0";
            cursor = ControlApplication.getInstance().getContentResolver().query(
                    RawContacts.CONTENT_URI, PROJECTION, selection, null, null);

            if (!validCursor(cursor)) return;
            mSelection = new StringBuilder();
            mSelection.append(Data.RAW_CONTACT_ID).append(" IN (");
            do {
                long rawId = cursor.getLong(RAW_ID);
                long remoteId = cursor.getLong(REMOTE_RAW_ID);
                int remoteVersion = cursor.getInt(REMOTE_VERSION);
                mSelection.append(rawId);
                if (!cursor.isLast()) {
                    mSelection.append(",");
                }
                mRemoteId_rawId.put(remoteId, rawId);
                mRemoteId_remoteVersion.put(remoteId, remoteVersion);
            } while (cursor.moveToNext());
            mSelection.append(")");
        } catch (Exception e) {
            LogUtils.w(TAG, "query RawContacts error", e.fillInStackTrace());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void initData(byte[] data) {
        Cursor cursor = null;
        try {
        	mData = new StringBuilder();
        	mData.append(new String(data));
            cursor = ControlApplication.getInstance().getContentResolver().query(Data.CONTENT_URI,
                    PROJECTION_DATA, mSelection.toString(), null, null);
            if (!validCursor(cursor)) return;
            ContactData contact = new ContactData();
            String mimeType = cursor.getString(DATA_MIMETYPE);
            long data_id = cursor.getLong(DATA_ID);
            long rawContactId = cursor.getLong(RAW_CONTACT_ID);
            setContactData(contact, cursor, mimeType, data_id);

            String nextMimeType;
            long nextDataId;
            long nextRawContactId;
            while (cursor.moveToNext()) {
                nextMimeType = cursor.getString(DATA_MIMETYPE);
                nextDataId = cursor.getLong(DATA_ID);
                nextRawContactId = cursor.getLong(RAW_CONTACT_ID);
                if (rawContactId == nextRawContactId) {
                    setContactData(contact, cursor, nextMimeType, nextDataId);
                } else {
                    mContacts.put(rawContactId, contact);
                    rawContactId = nextRawContactId;
                    contact = new ContactData();
                    setContactData(contact, cursor, nextMimeType, nextDataId);
                }
            }
            LogUtils.d(TAG, "input mContacts rawContactId is " + rawContactId);
            mContacts.put(rawContactId, contact);
        } catch (Exception e) {
            LogUtils.d(TAG, "query Data error");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setContactData(ContactData contact, Cursor cursor, String mimeType, long dataId) {
        if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
            contact.setName(dataId, cursor.getString(DATA1));
        } else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
            String number = cursor.getString(DATA1);
            if (!TextUtils.isEmpty(number)) {
                contact.addNumber(dataId, number, cursor.getString(DATA2));
            }
        }
    }

    private void syncContacts() {
        LogUtils.d(TAG, "jsonStr = " + mData);

        JSONObject jsonObject;
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(mData.toString());
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                jsonObject = new JSONObject(jsonArray.get(i).toString());
                long raw_contact_id = jsonObject.getLong("raw_contact_id");
                String name = jsonObject.getString("name");
                JSONArray numbers = jsonObject.getJSONArray("numbers");
                if (mRemoteId_remoteVersion.containsKey(raw_contact_id)) {
                    int remoteVersion = mRemoteId_remoteVersion.get(raw_contact_id);
                    int version = jsonObject.getInt("version");
                    if (remoteVersion != version) {
                        //changed,so need to update
                        update(operations, raw_contact_id, version, name, numbers);
                    }
                    mRemoteId_rawId.remove(raw_contact_id);
                } else {
                    // new contact, so need to insert
                    insert(operations, raw_contact_id, name, numbers);
                }
            }
            delete(operations);
        } catch (JSONException e) {
            LogUtils.w(TAG, "JSONException", e.fillInStackTrace());
        } catch (Exception e) {
            LogUtils.w(TAG, "Exception", e.fillInStackTrace());
        }
        try {
            ControlApplication.getInstance().getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (Exception e) {
            LogUtils.w(TAG, "Exception", e.fillInStackTrace());
        }
        if (mRemoteId_remoteVersion != null) {
            mRemoteId_remoteVersion.clear();
            mRemoteId_remoteVersion = null;
        }
        if (mRemoteId_rawId != null) {
            mRemoteId_rawId.clear();
            mRemoteId_rawId = null;
        }
        if (mContacts != null) {
            mContacts.clear();
            mContacts = null;
        }
        if (operations != null) {
        	operations.clear();
        	operations = null;
        } 
        mData = null;
    }

    private void update(ArrayList<ContentProviderOperation> operations, long remote_id, int version,
                        String name, JSONArray numbers) {
        LogUtils.d(TAG, "changed,so need to update");
        long raw_contact_id = mRemoteId_rawId.get(remote_id);
        //update version in raw_contacts table
        ContentProviderOperation.Builder builder;
        builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
                RawContacts.CONTENT_URI, raw_contact_id))
                .withValue("remote_version", version)
                .withYieldAllowed(true);
        operations.add(builder.build());

        //update name in data table
        ContactData contact = mContacts.get(raw_contact_id);
        LogUtils.w(TAG, "old name = " + contact.name + ", new name = " + name + ", name id =" + contact.data_id);
        if (!name.equals(contact.name)) {
            if (contact.data_id == -1 && !" ".equals(name)) {// create name
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValue(StructuredName.RAW_CONTACT_ID, raw_contact_id)
                        .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(StructuredName.DISPLAY_NAME, name)
                        .withYieldAllowed(true);
            } else if (" ".equals(name) || TextUtils.isEmpty(name)) {
                builder = ContentProviderOperation.newDelete(Data.CONTENT_URI)
                        .withSelection(Data._ID + "=" + contact.data_id, null)
                        .withYieldAllowed(true);
            } else {
                builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                        .withSelection(Data._ID + "=" + contact.data_id, null)
                        .withValue(StructuredName.DISPLAY_NAME, name)
                        .withYieldAllowed(true);
            }

            operations.add(builder.build());
        }

        // update numbers in data table
        updateNumber(operations, contact, raw_contact_id, numbers);
    }

    private void updateNumber(ArrayList<ContentProviderOperation> operations, ContactData contact,
                              long raw_contact_id, JSONArray numbersArray) {
        ContentProviderOperation.Builder builder;
        // first, delete all numbers
        long id;
        if (contact.id_numberType.size() > 0) {
            Set<Long> dataIdSet = contact.id_numberType.keySet();
            Iterator<Long> iterator = dataIdSet.iterator();
            while (iterator.hasNext()) {
                id = iterator.next();
                builder = ContentProviderOperation.newDelete(Data.CONTENT_URI)
                        .withSelection(Data._ID + "=" + id, null)
                        .withYieldAllowed(true);
                operations.add(builder.build());
            }
        }
        // second, insert new numbers
        try {
            for (int j = 0; j < numbersArray.length(); j++) {
                JSONObject numberJson = new JSONObject(numbersArray.get(j).toString());
                int phonetype = numberJson.getInt("phonetype");
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Data.RAW_CONTACT_ID, raw_contact_id)
                        .withValue(Data.DATA1, numberJson.getString("number"))
                        .withValue(Data.DATA2, phonetype);
                if (phonetype == Phone.TYPE_CUSTOM) {
                    builder.withValue(Data.DATA3, numberJson.getString("custom"));
                }
                builder.withYieldAllowed(true);
                operations.add(builder.build());
            }
        } catch (JSONException e) {
            LogUtils.w(TAG, "JSONException", e.fillInStackTrace());
        } catch (Exception e) {
            LogUtils.w(TAG, "Exception", e.fillInStackTrace());
        }
    }

    private void insert(ArrayList<ContentProviderOperation> operations, long remote_id,
                        String name, JSONArray numbersArray) {
        LogUtils.d(TAG, "new contact, so need to insert");
        int rawContactInsertIndex = operations.size();
        ContentProviderOperation.Builder builder;
        builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue("remote_raw_contact_id", remote_id)
                .withYieldAllowed(true);
        operations.add(builder.build());

        if (!TextUtils.isEmpty(name)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, rawContactInsertIndex);
            builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            builder.withValue(StructuredName.DISPLAY_NAME, name);
            builder.withYieldAllowed(true);
            operations.add(builder.build());
        }

        try {
            for (int j = 0; j < numbersArray.length(); j++) {
                JSONObject numberJson = new JSONObject(numbersArray.get(j).toString());
                int phonetype = numberJson.getInt("phonetype");
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Phone.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Data.DATA1, numberJson.getString("number"))
                        .withValue(Data.DATA2, phonetype);
                if (phonetype == Phone.TYPE_CUSTOM) {
                    builder.withValue(Data.DATA3, numberJson.getString("custom"));
                }
                builder.withYieldAllowed(true);
                operations.add(builder.build());
            }
        } catch (JSONException e) {
            LogUtils.w(TAG, "JSONException", e.fillInStackTrace());
        } catch (Exception e) {
            LogUtils.w(TAG, "Exception", e.fillInStackTrace());
        }
    }

    private void delete(ArrayList<ContentProviderOperation> operations) {
        LogUtils.d(TAG, "delete contact");
        if (mRemoteId_rawId.size() > 0) {
            Collection<Long> rawIds = mRemoteId_rawId.values();
            Cursor cursor = null;
            for (long id : rawIds) {
                try {
                    cursor = ControlApplication.getInstance().getContentResolver().query(
                            Contacts.CONTENT_URI, new String[]{"_id", Contacts.LOOKUP_KEY},
                            Contacts.NAME_RAW_CONTACT_ID + "=" + id, null, null);
                    if (!validCursor(cursor)) return;

                    long contactId = cursor.getLong(0);
                    String lookupKey = cursor.getString(1);
                    LogUtils.w(TAG, "rawId = " + id + "contactId = " + contactId + ", lookupKey = " + lookupKey);
                    Uri lookupUri = ContentUris.withAppendedId(Uri.withAppendedPath(
                            Contacts.CONTENT_LOOKUP_URI, lookupKey), contactId);
                    operations.add(ContentProviderOperation.newDelete(lookupUri).build());
                } catch (Exception e) {
                    LogUtils.d(TAG, "delete Data error");
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }
            }
        }
    }

    private static boolean validCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
            if (cursor != null) {
                cursor.close();
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getReplyData() {
        return Constant.EMPTY;
    }

    private class ContactData {
        long data_id = -1;
        private String name;
        //<data_id, <number, phone_type>>
        protected HashMap<Long, String[]> id_numberType = new HashMap<>();
        private String[] number_type;

        private void setName(long id, String name) {
            this.data_id = id;
            this.name = name;
        }

        private void addNumber(long data_id, String number, String phone_type) {
            number_type = new String[2];
            number_type[0] = number;
            number_type[1] = phone_type;
            id_numberType.put(data_id, number_type);
        }
    }
}
