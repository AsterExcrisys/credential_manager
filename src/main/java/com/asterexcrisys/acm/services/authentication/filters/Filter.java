package com.asterexcrisys.acm.services.authentication.filters;

import com.asterexcrisys.acm.types.authentication.FilterResult;

@SuppressWarnings("unused")
public sealed interface Filter<T> permits StoreFilter, DatabaseFilter {

    FilterResult filter(T data) throws Exception;

}