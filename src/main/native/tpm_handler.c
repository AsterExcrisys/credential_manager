#include <tss2/tss2_esys.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// TPM context
ESYS_CONTEXT *esys_ctx;

// Initialize TPM ESAPI context
int init_tpm() {
    TSS2_RC rc = Esys_Initialize(&esys_ctx, NULL, NULL);
    return rc == TSS2_RC_SUCCESS ? 0 : -1;
}

// Finalize TPM context
void cleanup_tpm() {
    if (esys_ctx) {
        Esys_Finalize(&esys_ctx);
    }
}

// Seal a key into TPM
int seal_key(const uint8_t *keyData, size_t keyLen,
             TPM2B_PRIVATE **outPrivate, TPM2B_PUBLIC **outPublic) {
    TSS2_RC rc;
    ESYS_TR primaryHandle;

    TPM2B_SENSITIVE_CREATE inSensitive = {
        .sensitive = {
            .userAuth = {.size = 0},
            .data = {.size = keyLen}
        }
    };
    memcpy(inSensitive.sensitive.data.buffer, keyData, keyLen);

    TPM2B_PUBLIC inPublic = {
        .publicArea = {
            .type = TPM2_ALG_KEYEDHASH,
            .nameAlg = TPM2_ALG_SHA256,
            .objectAttributes = (TPMA_OBJECT_USERWITHAUTH | TPMA_OBJECT_FIXEDTPM |
                                 TPMA_OBJECT_FIXEDPARENT | TPMA_OBJECT_SENSITIVEDATAORIGIN),
            .authPolicy = {.size = 0},
            .parameters.keyedHashDetail = {
                .scheme = {.scheme = TPM2_ALG_NULL}
            },
            .unique.keyedHash = {.size = 0}
        }
    };

    TPM2B_DATA outsideInfo = {.size = 0};
    TPML_PCR_SELECTION creationPCR = {.count = 0};

    TPM2B_CREATION_DATA *creationData;
    TPM2B_DIGEST *creationHash;
    TPMT_TK_CREATION *creationTicket;
    TPM2B_PUBLIC *primaryPub;

    TPM2B_PUBLIC primaryTemplate = {
        .publicArea = {
            .type = TPM2_ALG_RSA,
            .nameAlg = TPM2_ALG_SHA256,
            .objectAttributes = (TPMA_OBJECT_RESTRICTED | TPMA_OBJECT_DECRYPT |
                                 TPMA_OBJECT_FIXEDTPM | TPMA_OBJECT_FIXEDPARENT |
                                 TPMA_OBJECT_SENSITIVEDATAORIGIN | TPMA_OBJECT_USERWITHAUTH),
            .parameters.rsaDetail = {
                .symmetric = {.algorithm = TPM2_ALG_AES, .keyBits.aes = 128, .mode.aes = TPM2_ALG_CFB},
                .scheme = {.scheme = TPM2_ALG_NULL},
                .keyBits = 2048,
                .exponent = 0
            },
            .unique.rsa = {.size = 0}
        }
    };

    TPM2B_SENSITIVE_CREATE emptySensitive = {.sensitive = {.userAuth = {.size = 0}, .data = {.size = 0}}};

    rc = Esys_CreatePrimary(esys_ctx, ESYS_TR_RH_OWNER,
                            ESYS_TR_PASSWORD, ESYS_TR_NONE, ESYS_TR_NONE,
                            &emptySensitive, &primaryTemplate,
                            &outsideInfo, &creationPCR,
                            &primaryHandle, &primaryPub,
                            &creationData, &creationHash, &creationTicket);
    if (rc != TSS2_RC_SUCCESS) return -1;

    TPM2B_PRIVATE *createdPrivate;
    TPM2B_PUBLIC *createdPublic;

    rc = Esys_Create(esys_ctx, primaryHandle,
                     ESYS_TR_PASSWORD, ESYS_TR_NONE, ESYS_TR_NONE,
                     &inSensitive, &inPublic, &outsideInfo, &creationPCR,
                     &createdPrivate, &createdPublic,
                     &creationData, &creationHash, &creationTicket);
    if (rc != TSS2_RC_SUCCESS) return -2;

    *outPrivate = createdPrivate;
    *outPublic = createdPublic;

    Esys_FlushContext(esys_ctx, primaryHandle);
    return 0;
}

// Unseal key from TPM
int unseal_key(const TPM2B_PRIVATE *sealedPrivate, const TPM2B_PUBLIC *sealedPublic,
               uint8_t *outKeyData, size_t *outLen) {
    TSS2_RC rc;
    ESYS_TR primaryHandle;
    ESYS_TR sealedHandle;

    TPM2B_DATA outsideInfo = {.size = 0};
    TPML_PCR_SELECTION creationPCR = {.count = 0};
    TPM2B_CREATION_DATA *creationData;
    TPM2B_DIGEST *creationHash;
    TPMT_TK_CREATION *creationTicket;
    TPM2B_PUBLIC *primaryPub;

    TPM2B_PUBLIC primaryTemplate = {
        .publicArea = {
            .type = TPM2_ALG_RSA,
            .nameAlg = TPM2_ALG_SHA256,
            .objectAttributes = (TPMA_OBJECT_RESTRICTED | TPMA_OBJECT_DECRYPT |
                                 TPMA_OBJECT_FIXEDTPM | TPMA_OBJECT_FIXEDPARENT |
                                 TPMA_OBJECT_SENSITIVEDATAORIGIN | TPMA_OBJECT_USERWITHAUTH),
            .parameters.rsaDetail = {
                .symmetric = {.algorithm = TPM2_ALG_AES, .keyBits.aes = 128, .mode.aes = TPM2_ALG_CFB},
                .scheme = {.scheme = TPM2_ALG_NULL},
                .keyBits = 2048,
                .exponent = 0
            },
            .unique.rsa = {.size = 0}
        }
    };

    TPM2B_SENSITIVE_CREATE emptySensitive = {.sensitive = {.userAuth = {.size = 0}, .data = {.size = 0}}};

    rc = Esys_CreatePrimary(esys_ctx, ESYS_TR_RH_OWNER,
                            ESYS_TR_PASSWORD, ESYS_TR_NONE, ESYS_TR_NONE,
                            &emptySensitive, &primaryTemplate,
                            &outsideInfo, &creationPCR,
                            &primaryHandle, &primaryPub,
                            &creationData, &creationHash, &creationTicket);
    if (rc != TSS2_RC_SUCCESS) return -1;

    rc = Esys_Load(esys_ctx, primaryHandle,
                   ESYS_TR_PASSWORD, ESYS_TR_NONE, ESYS_TR_NONE,
                   sealedPrivate, sealedPublic, &sealedHandle);
    if (rc != TSS2_RC_SUCCESS) return -2;

    TPM2B_SENSITIVE_DATA *unsealed;
    rc = Esys_Unseal(esys_ctx, sealedHandle,
                     ESYS_TR_PASSWORD, ESYS_TR_NONE, ESYS_TR_NONE,
                     &unsealed);
    if (rc != TSS2_RC_SUCCESS) return -3;

    memcpy(outKeyData, unsealed->buffer, unsealed->size);
    *outLen = unsealed->size;

    Esys_FlushContext(esys_ctx, sealedHandle);
    Esys_FlushContext(esys_ctx, primaryHandle);
    return 0;
}