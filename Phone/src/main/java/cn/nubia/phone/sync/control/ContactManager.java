package cn.nubia.phone.sync.control;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.nubia.wear.control.sync.BaseMgr;
import cn.nubia.wear.control.sync.Constant;
import cn.nubia.wear.control.sync.ServerSyncManager;
import cn.nubia.wear.control.utils.LogUtils;

/**
 * a manager of sync contacts.
 */
public class ContactManager extends BaseMgr implements ISyncProgressListener {
    private static final String TAG = "ContactManager";
    private ExecutorService mThreadPool;
    private ContactHelper mContactHelper;
    private List<RemoteContact> mCachedRemoteContacts;

    public ContactManager() {
        mContactHelper = new ContactHelper(this);
        mThreadPool = Executors.newCachedThreadPool();
    }

    private List<RemoteContact> mNewContacts = new ArrayList<>();
    private List<LocalContact> mUpdateContacts = new ArrayList<>();

    @Override
    public String getCommand() {
        return Constant.COMMAND_CONTACT;
    }

    @Override
    public String getReplyData() {
        return Constant.EMPTY;
    }

    @Override
    public boolean parse(String jsonStr, int message) {
        return false;
    }

    @Override
    public boolean parse(byte[] data, int id) {
        sendReplyMessage(id);
        Gson gson = new GsonBuilder().registerTypeAdapter(RemoteContact.class, new ContactTypeAdapter()).create();
        Type type = new TypeToken<List<RemoteContact>>() {
        }.getType();
        try {
            mCachedRemoteContacts = gson.fromJson(new String(data), type);
            LogUtils.d(TAG, "[Receive] remoteContacts size: " + (mCachedRemoteContacts != null ? mCachedRemoteContacts.size() : "null"));
            if (mCachedRemoteContacts != null) {
                syncContacts();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return true;
    }

    private void syncContacts() throws Exception {
        LogUtils.d(TAG, "[syncContacts] isTaskFinished: " + mContactHelper.isTaskFinished());
        if (mContactHelper.isTaskFinished()) {
            LogUtils.d(TAG, "[syncContacts] Begin.");
            reset();
            onInsertProgressUpdate(2);
            List<LocalContact> localContacts = mContactHelper.queryContacts();
            onInsertProgressUpdate(3);
            if (localContacts == null) return;
            compare(mCachedRemoteContacts, localContacts);
            onInsertProgressUpdate(4);
            mUpdateContacts = localContacts;
            save();
        }
    }

    private void compare(List<RemoteContact> remoteContacts, List<LocalContact> localContacts) {
        RemoteContact remoteContact;
        LocalContact localContact;

        for (int i = 0; i < remoteContacts.size(); i++) {
            remoteContact = remoteContacts.get(i);
            localContact = matchContact(remoteContact.getRawContactId(), localContacts);

            if (localContact == null) { // new contacts, need insert.
                mNewContacts.add(remoteContact);
            } else {
                // if raw_contacts version is different, compare and update
                if (localContact.getRemoteVersion() != remoteContact.getVersion()) {
                    compareContact(remoteContact, localContact);
                } else {
                    localContact.setSameContact(true);
                }
            }
        }
    }

    private void compareContact(RemoteContact remoteContact, LocalContact localContact) {
        // version
        localContact.setRemoteVersion(remoteContact.getVersion());
        localContact.setRemoteVersionUpdated(true);
        // name
        if (!TextUtils.equals(localContact.getName(), remoteContact.getName())) {
            localContact.setName(remoteContact.getName());
            localContact.setNameUpdated(true);
        }
        // numbers
        List<Number> remoteNumbers = remoteContact.getNumbers();
        List<Number> localNumbers = localContact.getNumbers();
        Number remoteNumber, localNumber;
        for (int i = 0; i < remoteNumbers.size(); i++) {
            remoteNumber = remoteNumbers.get(i);

            localNumber = matchNumber(remoteNumber.getRemoteDataId(), localNumbers);
            if (localNumber == null) { // new
                remoteNumber.setNewNumber(true);
                remoteNumber.setRawContactId(localContact.getRawContactId());
                localContact.addNumber(remoteNumber);
            } else {
                // if data version is different, compare and update
                if (localNumber.getRemoteVersion() != remoteNumber.getRemoteVersion()) {
                    compareNumber(remoteNumber, localNumber);
                } else {
                    localNumber.setSameNumber(true);
                }
            }
        }
    }

    private void compareNumber(Number remoteNumber, Number localNumber) {
        localNumber.setRemoteVersion(remoteNumber.getVersion());
        localNumber.setRemoteVersionUpdated(true);

        if (!TextUtils.equals(localNumber.getNumber(), remoteNumber.getNumber())) {
            localNumber.setNumber(remoteNumber.getNumber());
            localNumber.setNumberUpdated(true);

        }
        if (!TextUtils.equals(localNumber.getCustom(), remoteNumber.getCustom())) {
            localNumber.setCustom(remoteNumber.getCustom());
            localNumber.setCustomUpdated(true);

        }
        if (localNumber.getPhoneType() != remoteNumber.getPhoneType()) {
            localNumber.setPhoneType(remoteNumber.getPhoneType());
            localNumber.setPhoneTypeUpdated(true);
        }

    }

    private LocalContact matchContact(long remoteRawContactId, List<LocalContact> localContacts) {
        for (Iterator<LocalContact> it = localContacts.iterator(); it.hasNext(); ) {
            LocalContact contact = it.next();
            if (contact.getRemoteRawContactId() == remoteRawContactId)
                return contact;
        }
        return null;
    }

    private Number matchNumber(long remoteDataId, List<Number> localNumbers) {
        for (int i = 0; i < localNumbers.size(); i++) {
            Number localNumber = localNumbers.get(i);
            if (localNumber.getRemoteDataId() == remoteDataId) {
                return localNumber;
            }
        }
        return null;
    }

    private void save() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                mContactHelper.updateContacts(mUpdateContacts);
                LogUtils.d(TAG, "[syncContacts] update End.");
            }
        });
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                mContactHelper.insertContacts(mNewContacts);
                LogUtils.d(TAG, "[syncContacts] insert End.");
            }
        });
    }

    private void reset() {
        mNewContacts.clear();
        mUpdateContacts.clear();
        mContactHelper.reset();
    }

    private boolean sendReplyMessage(int messageId) {
        try {
            JSONObject dataJson = new JSONObject();
            JSONObject mJson = new JSONObject();
            mJson.put(Constant.JSON_KEY_DATA, dataJson);
            mJson.put(Constant.JSON_KEY_COMMAND, Constant.COMMAND_CONTACT);
            String message = mJson.toString();
            ServerSyncManager.getInstance().sendMsg(message, messageId);
            return true;
        } catch (Exception ex) {
            LogUtils.d(TAG, "sendReplyMessage Exception e = " + ex.getMessage());
            return false;
        }
    }

    @Override
    public void onInsertProgressUpdate(int percent) {
        try {
            if (ServerSyncManager.getInstance().isConnected()) {
                LogUtils.d(TAG, "[onInsertProgressUpdate] Progress: " + percent);
                ServerSyncManager.getInstance().sendMsg(buildProgressMsg(percent), 0);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildProgressMsg(int progress) throws JSONException {
        JSONObject jsonMsg = new JSONObject();
        jsonMsg.put(Constant.JSON_KEY_PROGRESS, progress);
        jsonMsg.put(Constant.JSON_KEY_COMMAND, Constant.COMMAND_CONTACT);
        return jsonMsg.toString();
    }

}
