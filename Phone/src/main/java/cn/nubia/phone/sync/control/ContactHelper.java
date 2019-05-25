package cn.nubia.phone.sync.control;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.nubia.wear.control.ControlApplication;
import cn.nubia.wear.control.utils.LogUtils;

public class ContactHelper {
    private static final String TAG = "ContactHelper";
    private PowerManager.WakeLock mWakeLock;
    private static final int APPLY_LIMIT = 100;
    public boolean isInsertFinished = true;
    public boolean isUpdateFinished = true;
    private final ArrayList<ContentProviderOperation> mInsertContactOperations;
    private final ArrayList<ContentProviderOperation> mUpdateContactOperations;

    private ContentResolver mResolver;
    private ISyncProgressListener mProgressListener;
    private final Uri CONTENT_URI =
            Uri.parse("content://" + ContactsContract.AUTHORITY + "/profile/sync");
    private final Uri CONTACTS_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts");

    // raw_contacts
    private final int COL_CONTACT_ID = 0; // contact_id
    private final int COL_RAW_CONTACT_ID = 1; // raw_contact_id
    private final int COL_REMOTE_RAW_CONTACT_ID = 2; // "remote_raw_contact_id"
    private final int COL_REMOTE_VERSION = 3; // "remote_version"
    // data
    private final int COL_DATA_ID = 4; // data_id
    private final int COL_REMOTE_DATA_ID = 5; // "remote_data_id"
    private final int COL_REMOTE_DATA_VERSION = 6; // "remote_data_version"

    private final int COL_DATA_DATA1 = 7; // data1,number
    private final int COL_DATA_DATA2 = 8; // data2,phoneType
    private final int COL_DATA_DATA3 = 9; // data3,custom
    private final int COL_MIME_TYPE = 10; // mimetype

    public ContactHelper(ISyncProgressListener listener) {
        Context context = ControlApplication.getInstance();
        mResolver = context.getContentResolver();
        mProgressListener = listener;
        mInsertContactOperations = new ArrayList<>();
        mUpdateContactOperations = new ArrayList<>();

        PowerManager powerManager =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    // Query All Contacts
    public List<LocalContact> queryContacts() {
        Cursor cursor = mResolver.query(CONTENT_URI, null, null, null, null);
        if (cursor == null) {
            LogUtils.e(TAG, "[queryContacts] error!");
            return null;
        }

        try {
            final LinkedList<LocalContact> localContacts = new LinkedList<>();
            LocalContact localContact = null;
            long currentRawContactId = -1;

            while (cursor.moveToNext()) {
                long rawContactId = cursor.getLong(COL_RAW_CONTACT_ID);
                if (rawContactId != currentRawContactId) {
                    // First time to see this raw contact id, so create a new entity, and add it to list.
                    currentRawContactId = rawContactId;
                    localContact = new LocalContact();
                    localContact.setContactId(cursor.getLong(COL_CONTACT_ID));
                    localContact.setRawContactId(currentRawContactId);
                    localContact.setRemoteRawContactId(cursor.getLong(COL_REMOTE_RAW_CONTACT_ID));
                    localContact.setRemoteVersion(cursor.getInt(COL_REMOTE_VERSION));
                    localContacts.add(localContact);
                }
                if (localContact != null)
                    addDataItemValues(localContact, currentRawContactId, cursor);
            }
            return localContacts;
        } finally {
            cursor.close();
        }
    }

    private void addDataItemValues(LocalContact contact, long rawContactId, Cursor cursor) {
        String mimeType = cursor.getString(COL_MIME_TYPE);
        if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
            contact.setName(cursor.getString(COL_DATA_DATA1));
            contact.setNameId(cursor.getLong(COL_DATA_ID));
        } else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
            // add Number Entity
            Number number = new Number();
            number.setDataId(cursor.getLong(COL_DATA_ID));
            number.setRemoteDataId(cursor.getLong(COL_REMOTE_DATA_ID));
            number.setRemoteVersion(cursor.getInt(COL_REMOTE_DATA_VERSION));
            number.setRawContactId(rawContactId);
            number.setNumber(cursor.getString(COL_DATA_DATA1));
            number.setPhoneType(cursor.getInt(COL_DATA_DATA2));
            number.setCustom(cursor.getString(COL_DATA_DATA3));
            contact.addNumber(number);
        }
    }

    // Insert New Contact
    public void insertContacts(List<RemoteContact> contacts) {
        try {
            mWakeLock.acquire();
            mProgressListener.onInsertProgressUpdate(5);
            long beginTime = System.currentTimeMillis();
            RemoteContact newContact;
            int rawContactInsertIndex;
            int size = contacts.size();
            int count = 0;
            for (int i = 0; i < size; i++) {
                count = i;
                rawContactInsertIndex = mInsertContactOperations.size();
                newContact = contacts.get(i);

                buildInsertContactOperation(newContact, rawContactInsertIndex);

                // when applyBatch, ContentProviderOperation has a limit of 500;
                // we commit at 100 is OK.
                if (mInsertContactOperations.size() >= APPLY_LIMIT) {
                    // apply(mInsertContactOperations);
                    LogUtils.d("WTest","applyBatch begin.");
                    mResolver.applyBatch(ContactsContract.AUTHORITY, mInsertContactOperations);
                    LogUtils.d("WTest","applyBatch end.");
                    mInsertContactOperations.clear();
                    updateProgress(count * 100 / size);
                }
            }
            if (mInsertContactOperations.size() > 0) {
                // apply(mInsertContactOperations);
                LogUtils.d("WTest","applyBatch begin.");
                mResolver.applyBatch(ContactsContract.AUTHORITY, mInsertContactOperations);
                LogUtils.d("WTest","applyBatch end.");
                updateProgress(count * 100 / size);
            }
            printLongLog("insertContacts", contacts.size(), beginTime);
        } catch (Exception e) {
            LogUtils.e(TAG, "insert Contacts", e);
        } finally {
            isInsertFinished = true;
            updateProgress(100);
            mWakeLock.release();
        }

    }

    private void buildInsertContactOperation(RemoteContact newContact, int rawId) {
        mInsertContactOperations.add(buildRawContacts(newContact));
        mInsertContactOperations.add(buildDisplayName(newContact, rawId));
        insertNumbers(newContact.getNumbers(), rawId);
    }

    private void insertNumbers(List<Number> numbers, int rawContactInsertIndex) {
        if (numbers == null) return;
        for (int i = 0; i < numbers.size(); i++) {
            mInsertContactOperations.add(buildNumber(numbers.get(i), rawContactInsertIndex));
        }
    }

    private ContentProviderOperation buildNumber(Number number, int rawContactInsertIndex) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Phone.RAW_CONTACT_ID, rawContactInsertIndex)

                .withValue("remote_data_id", number.getRemoteDataId())
                .withValue("remote_data_version", number.getRemoteVersion())

                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Data.DATA1, number.getNumber())
                .withValue(Data.DATA2, number.getPhoneType());
        if (number.getPhoneType() == Phone.TYPE_CUSTOM) {
            builder.withValue(Data.DATA3, number.getCustom());
        }
        builder.withYieldAllowed(false);
        return builder.build();
    }

    private ContentProviderOperation buildRawContacts(RemoteContact contact) {
        return ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue("remote_version", contact.getVersion())
                .withValue("remote_raw_contact_id", contact.getRawContactId())
                .withYieldAllowed(false).build();
    }

    private ContentProviderOperation buildDisplayName(RemoteContact contact, int rawContactsIndex) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(StructuredName.RAW_CONTACT_ID, rawContactsIndex)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, contact.getName())
                .withYieldAllowed(false).build();
    }

    // Update Exists Contact
    public void updateContacts(List<LocalContact> localContacts) {
        try {
            long beginTime = System.currentTimeMillis();
            for (int i = 0; i < localContacts.size(); i++) {
                buildUpdateContactOperation(localContacts.get(i));
                if (mUpdateContactOperations.size() >= APPLY_LIMIT) {
                    // apply(mUpdateContactOperations);
                    mResolver.applyBatch(ContactsContract.AUTHORITY, mUpdateContactOperations);
                    mUpdateContactOperations.clear();
                }
            }
            if (mUpdateContactOperations.size() > 0) {
                // apply(mUpdateContactOperations);
                mResolver.applyBatch(ContactsContract.AUTHORITY, mUpdateContactOperations);
            }
            printLongLog("updateContacts", localContacts.size(), beginTime);
        } catch (Exception e) {
            LogUtils.e(TAG, "update Contacts", e);
        } finally {
            isUpdateFinished = true;
        }
    }

    private void buildUpdateContactOperation(LocalContact contact) {
        if (contact.isRemoteVersionUpdated()) {
            updateContactVersion(contact.getContactId(), contact.getRemoteVersion());
            if (contact.isNameUpdated()) {
                addContactNameUpdateOperation(contact.getNameId(), contact.getName());
            }
            updateNumbers(contact.getNumbers());
        } else if (contact.isCanDelete()) {
            LogUtils.d(TAG, "[buildUpdateContactOperation] delete: " + contact.getName());
            addContactDeleteOperation(contact.getContactId());
        }
    }

    private void updateContactVersion(long contactId, int version) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI)
                .withValue(RawContacts.VERSION, version).withSelection(RawContacts.CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)});
        builder.withYieldAllowed(true);
        mUpdateContactOperations.add(builder.build());
    }

    private void updateNumbers(List<Number> numbers) {
        for (Number entity : numbers) {
            if (entity.isNewNumber()) {
                addNumberInsertOperation(entity);
            } else if (entity.isCanDelete()) {
                addNumberDeleteOperation(entity.getDataId());
            } else if (entity.isRemoteVersionUpdated()) {
                addNumberUpdateOperation(entity);
            }
        }
    }

    private void addContactDeleteOperation(Long contactId) {
        Uri deleteUri = ContentUris.withAppendedId(CONTACTS_URI, contactId)
                .buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
        mUpdateContactOperations.add(ContentProviderOperation.newDelete(deleteUri).build());
    }

    private void addContactNameUpdateOperation(long nameDataId, String name) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI).
                withValue(Data.DATA1, name).withSelection(Data._ID + " = ?", new String[]{String.valueOf(nameDataId)});
        builder.withYieldAllowed(true);
        mUpdateContactOperations.add(builder.build());
    }

    private void addNumberInsertOperation(Number number) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValue(Data.RAW_CONTACT_ID, number.getRawContactId())
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Data.DATA1, number.getNumber())
                .withValue(Data.DATA2, number.getPhoneType());
        if (number.getPhoneType() == Phone.TYPE_CUSTOM) {
            builder.withValue(Data.DATA3, number.getCustom());
        }
        builder.withYieldAllowed(true);
        mUpdateContactOperations.add(builder.build());
    }

    private void addNumberDeleteOperation(long id) {
        mUpdateContactOperations.add(ContentProviderOperation.newDelete(Data.CONTENT_URI).
                withSelection(Data._ID + " = ?", new String[]{String.valueOf(id)})
                .withYieldAllowed(true).build());
    }

    private void addNumberUpdateOperation(Number number) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI).
                withValue("remote_data_version", number.getRemoteVersion());
        if (number.isNumberUpdated()) builder.withValue(Data.DATA1, number.getNumber());
        if (number.isPhoneTypeUpdated()) builder.withValue(Data.DATA2, number.getPhoneType());
        if (number.isCustomUpdated()) builder.withValue(Data.DATA3, number.getCustom());
        builder.withSelection(Data._ID + " = ?", new String[]{String.valueOf(number.getDataId())});
        builder.withYieldAllowed(true);
        mUpdateContactOperations.add(builder.build());
    }

    public boolean isTaskFinished() {
        return isInsertFinished && isUpdateFinished;
    }

    public void reset() {
        isUpdateFinished = false;
        isInsertFinished = false;
        mInsertContactOperations.clear();
        mUpdateContactOperations.clear();
    }

    private void printLongLog(String methodName, int size, long beginTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(methodName).append("] size: ").append(size).append(" | ");
        sb.append("time: ").append((System.currentTimeMillis() - beginTime));

        LogUtils.d(TAG, sb.toString());
    }

    private static final long PROGRESS_TIME_INTERVAL = 50;

    private void updateProgress(int percent) {
        if (mProgressListener != null && percent >= 5) {
            try {
                Thread.sleep(PROGRESS_TIME_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mProgressListener.onInsertProgressUpdate(percent);
        }
    }
}