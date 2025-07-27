#include <tss2/tss2_esys.h>
#include <tss2/tss2_rc.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define TPM2_SEAL_HANDLE ESYS_TR_RH_OWNER
#define AUTHENTICATION_NULL {.size = 0, .buffer = {}}

// TODO: gcc -fPIC -shared -o tpm_handler.so tpm_handler.c -ltss2-esys

ESYS_CONTEXT* initialise_context() {
    TSS2_RC response_code;
    ESYS_CONTEXT* context = NULL;
    response_code = Esys_Initialize(&context, NULL, NULL);
    if (response_code != TSS2_RC_SUCCESS) {
        return NULL;
    }
    return context;
}

void finalise_context(ESYS_CONTEXT* context) {
    if (context != NULL) {
        Esys_Finalize(&context);
    }
}

bool seal_key_to_file(ESYS_CONTEXT* context, const uint8_t* key, size_t key_length, const char* file_name) {
    TSS2_RC response_code;
    TPM2B_SENSITIVE_CREATE input_sensitive = {
        .size = 0,
        .sensitive = {
            .userAuth = AUTHENTICATION_NULL,
            .data = {
                .size = key_length,
            }
        }
    };
    memcpy(input_sensitive.sensitive.data.buffer, key, key_length);
    TPM2B_PUBLIC input_public = {
        .size = 0,
        .publicArea = {
            .type = TPM2_ALG_KEYEDHASH,
            .nameAlg = TPM2_ALG_SHA256,
            .objectAttributes =
                TPMA_OBJECT_USERWITHAUTH |
                TPMA_OBJECT_FIXEDTPM |
                TPMA_OBJECT_FIXEDPARENT |
                TPMA_OBJECT_SENSITIVEDATAORIGIN,
            .authPolicy = {
                .size = 0,
            },
            .parameters.keyedHashDetail = {
                .scheme.scheme = TPM2_ALG_NULL,
            },
            .unique.keyedHash.size = 0,
        }
    };
    ESYS_TR object_handle = ESYS_TR_NONE;
    TPM2B_PRIVATE* output_private = NULL;
    TPM2B_PUBLIC* output_public = NULL;
    response_code = Esys_Create(
        context,
        TPM2_SEAL_HANDLE,
        ESYS_TR_PASSWORD,
        ESYS_TR_NONE,
        ESYS_TR_NONE,
        &input_sensitive,
        &input_public,
        NULL, NULL, NULL,
        &output_private,
        &output_public,
        NULL, NULL, NULL
    );
    if (response_code != TSS2_RC_SUCCESS) {
        return false;
    }
    FILE* file = fopen(file_name, "wb");
    if (file == NULL) {
        Esys_Free(output_private);
        Esys_Free(output_public);
        return false;
    }
    fwrite(&output_private->size, sizeof(UINT16), 1, file);
    fwrite(output_private->buffer, 1, output_private->size, file);
    fwrite(&output_public->size, sizeof(UINT16), 1, file);
    fwrite(output_public->publicArea.unique.keyedHash.buffer, 1, output_public->publicArea.unique.keyedHash.size, file);
    fclose(file);
    Esys_Free(output_private);
    Esys_Free(output_public);
    return true;
}

uint8_t* unseal_key_from_file(ESYS_CONTEXT* context, const char* file_name, size_t* key_length) {
    FILE* file = fopen(file_name, "rb");
    if (file == NULL) {
        return NULL;
    }
    TPM2B_PRIVATE input_private = {0};
    TPM2B_PUBLIC input_public = {
        .publicArea = {
            .type = TPM2_ALG_KEYEDHASH,
            .nameAlg = TPM2_ALG_SHA256,
            .objectAttributes =
                TPMA_OBJECT_USERWITHAUTH |
                TPMA_OBJECT_FIXEDTPM |
                TPMA_OBJECT_FIXEDPARENT |
                TPMA_OBJECT_SENSITIVEDATAORIGIN,
            .authPolicy = {
                .size = 0,
            },
            .parameters.keyedHashDetail = {
                .scheme.scheme = TPM2_ALG_NULL,
            },
            .unique.keyedHash.size = 32,
        }
    };
    fread(&input_private.size, sizeof(UINT16), 1, file);
    fread(input_private.buffer, 1, input_private.size, file);
    UINT16 public_size;
    fread(&public_size, sizeof(UINT16), 1, file);
    fread(input_public.publicArea.unique.keyedHash.buffer, 1, public_size, file);
    input_public.publicArea.unique.keyedHash.size = public_size;
    fclose(file);
    ESYS_TR loaded_object = ESYS_TR_NONE;
    TSS2_RC response_code = Esys_Load(
        context,
        TPM2_SEAL_HANDLE,
        ESYS_TR_PASSWORD,
        ESYS_TR_NONE,
        ESYS_TR_NONE,
        &input_private,
        &input_public,
        &loaded_object
    );
    if (response_code != TSS2_RC_SUCCESS) {
        return NULL;
    }
    TPM2B_SENSITIVE_DATA* output_sensitive;
    response_code = Esys_Unseal(
        context,
        loaded_object,
        ESYS_TR_PASSWORD,
        ESYS_TR_NONE,
        ESYS_TR_NONE,
        &output_sensitive
    );
    if (response_code != TSS2_RC_SUCCESS) {
        Esys_FlushContext(context, loaded_object);
        return NULL;
    }
    uint8_t* key = malloc(output_sensitive->size);
    memcpy(key, output_sensitive->buffer, output_sensitive->size);
    *key_length = output_sensitive->size;
    Esys_FlushContext(context, loaded_object);
    Esys_Free(output_sensitive);
    return key;
}