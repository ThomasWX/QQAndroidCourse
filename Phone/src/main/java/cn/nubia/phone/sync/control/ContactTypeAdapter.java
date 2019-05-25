package cn.nubia.phone.sync.control;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactTypeAdapter extends TypeAdapter<RemoteContact> {
    private static final String RAW_CONTACT_ID_TAG = "raw_contact_id";
    private static final String VERSION_TAG = "version";
    private static final String NAME_TAG = "name";
    private static final String NUMBERS_TAG = "numbers";

    private static final String NUMBERS_DATA_ID_TAG = "data_id";
    private static final String NUMBERS_DATA_VERSION_TAG = "version";

    private static final String NUMBERS_NUMBER_TAG = "number";
    private static final String NUMBERS_PHONE_TYPE_TAG = "phonetype";
    private static final String NUMBERS_CUSTOM_TAG = "custom";

    @Override
    public void write(JsonWriter jsonWriter, RemoteContact remoteContact) throws IOException {

    }

    @Override
    public RemoteContact read(JsonReader in) throws IOException {
        RemoteContact contact = new RemoteContact();
        in.beginObject();
        String tag;
        while (in.hasNext()) {
            tag = in.nextName();
            if (RAW_CONTACT_ID_TAG.equals(tag)) {
                contact.setRawContactId(in.nextLong());
            } else if (VERSION_TAG.equals(tag)) {
                contact.setVersion(in.nextInt());
            } else if (NAME_TAG.equals(tag)) {
                contact.setName(in.nextString());
            } else if (NUMBERS_TAG.equals(tag)) {
                contact.setNumbers(readNumbers(in));
            }
        }
        in.endObject();
        return contact;
    }

    public List<Number> readNumbers(JsonReader in) throws IOException {
        final List<Number> numbers = new ArrayList<>();
        Number number;
        in.beginArray();
        while (in.hasNext()) {
            number = new Number();
            numbers.add(readNumber(in, number));
        }
        in.endArray();
        return numbers;
    }

    public Number readNumber(JsonReader in, Number number) throws IOException {
        in.beginObject();
        String tag;
        while (in.hasNext()) {
            tag = in.nextName();
            if (NUMBERS_DATA_ID_TAG.equals(tag)) {
                number.setRemoteDataId(in.nextLong());
            } else if (NUMBERS_DATA_VERSION_TAG.equals(tag)) {
                number.setRemoteVersion(in.nextInt());
            } else if (NUMBERS_NUMBER_TAG.equals(tag)) {
                number.setNumber(in.nextString());
            } else if (NUMBERS_PHONE_TYPE_TAG.equals(tag)) {
                number.setPhoneType(in.nextInt());
            } else if (NUMBERS_CUSTOM_TAG.equals(tag)) {
                number.setCustom(in.nextString());
            }
        }
        in.endObject();
        return number;
    }
}
