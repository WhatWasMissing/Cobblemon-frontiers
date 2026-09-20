package com.whatwasmissing.cobblemongacha.core;

import java.util.ArrayList;
import java.util.List;

public final class GachaBanner {
    public String id;
    public String title;
    public String description;
    public List<GachaEntry> entries = new ArrayList<>();

    public GachaBanner() {}

    public GachaBanner(String id, String title, String description, List<GachaEntry> entries) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.entries = entries;
    }
}
