package org.mcadminToolkit;

public class configBuilder {

    static String buildLoggingField (String command, boolean shouldLog, boolean shouldSendPush) {
        String field = "";

        field += "        \"" + command + "\": {";
        field += "            \"log\": \"" + (shouldLog ? "true" : "false") + "\", ";
        field += "            \"push\": " + (shouldSendPush ? "true" : "false") + "}";

        return field;
    }

    public static String build () {
        String config = "";

        config += "{\n";
        config += "    \"port\": 4096,\n";
        config += "    \"commandLogging\": {\n";
        config += buildLoggingField ("ban", true, true) + ",\n";
        config += buildLoggingField ("ban-ip", true, true) + ",\n";
        config += buildLoggingField ("pardon", true, true) + ",\n";
        config += buildLoggingField ("pardon-ip", true, true) + ",\n";
        config += buildLoggingField ("kick", true, false) + ",\n";
        config += buildLoggingField ("whitelist", true, true) + "\n";
        config += "    },\n";
        config += "    \"appLogging\": {\n";
        config += buildLoggingField("ban", true, true) + ",\n";
        config += buildLoggingField("banIp", true, true) + ",\n";
        config += buildLoggingField("kick", true, false) + ",\n";
        config += buildLoggingField("unban", true, true) + ",\n";
        config += buildLoggingField("unbanIp", true, true) + ",\n";
        config += buildLoggingField("whitelistOnOff", true, true) + ",\n";
        config += buildLoggingField("whitelistAddRemovePlayer", true, true) + "\n";
        config += "    }\n";
        config += "}";

        return config;
    }
}
