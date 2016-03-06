package com.tvarit.plugin;

import java.util.Base64;

/**
 * Created by sachi_000 on 3/6/2016.
 */
public abstract class Base64EncodedTemplateBody implements HasTemplateBody {

    @Override
    public String decode() {
        return new String(Base64.getDecoder().decode(this.getEncodedTemplate()));
    }

    protected abstract String getEncodedTemplate();
}
