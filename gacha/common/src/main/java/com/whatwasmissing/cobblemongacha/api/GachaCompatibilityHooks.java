package com.whatwasmissing.cobblemongacha.api;

import com.whatwasmissing.cobblemongacha.core.ItemValueService;
import com.whatwasmissing.cobblemongacha.core.UpgradeCatalog;

/** Small optional-integration surface that does not create hard mod dependencies. */
public final class GachaCompatibilityHooks {
    private GachaCompatibilityHooks() {}

    public static void registerOptionalNamespace(String namespace) {
        ItemValueService.registerOptionalNamespace(namespace);
        UpgradeCatalog.registerOptionalNamespace(namespace);
    }
}
