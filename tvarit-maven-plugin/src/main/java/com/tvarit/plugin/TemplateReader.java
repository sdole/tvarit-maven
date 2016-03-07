package com.tvarit.plugin;

import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by sachi_000 on 3/6/2016.
 */
public class TemplateReader {
    public String readTemplate(String templateFileName) throws MojoFailureException {
        final BufferedReader templateStream = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(templateFileName)));
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = templateStream.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new MojoFailureException("Could not read vpc infra template.", e);
        }
        return sb.toString();
    }
}
