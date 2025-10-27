package com.asterexcrisys.acm.services.storage;

import java.util.Optional;

@SuppressWarnings("unused")
public sealed interface Store<T> permits SoftwareStore, HardwareStore {

    Optional<T> retrieve(String identifier);

    boolean save(String identifier, T data);

    boolean clear(String identifier);

}