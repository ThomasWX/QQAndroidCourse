package dn.android.open.ipc;

public class Person {
    String name;
    String password;

    public Person(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public Person() {
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
