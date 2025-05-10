package com.asterexcrisys.cman.services;

import java.util.Optional;

public interface Encryptor {

    Optional<String> encrypt(String data);

    Optional<String> decrypt(String data);

}