package pws.main;

import pws.lib.PasswordManager;

public class Main {

    public static void main(String[] args) {
        PasswordManager passwordManager = new PasswordManager();
        passwordManager.addPassword("test1", "Google");
        passwordManager.addPassword("test2", "Apple");
        passwordManager.addPassword("test3", "Microsoft");
        System.out.println(passwordManager.getPassword("Google").orElseThrow());
        System.out.println();
        for (String sealedObject : passwordManager.getSealedObjects().orElseThrow()) {
            System.out.println(sealedObject);
        }
    }

}