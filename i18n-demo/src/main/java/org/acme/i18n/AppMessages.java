package org.acme.i18n;

import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle
public interface AppMessages {
    @Message("Welcome!")
    String welcome();

    @Message("Goodbye, {0}!")
    String goodbye(String name);
}