package org.mcadminToolkit;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.json.JSONObject;

import java.io.IOException;

public class CommandListener implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocessEvent (PlayerCommandPreprocessEvent event) {

        if (expressServer.client == null) return;

        String message = event.getMessage();

        String[] array = message.split(" ");

        if (!event.getPlayer().isOp()) return;

        if (array.length >= 2) {
            if (array[0].equalsIgnoreCase("/ban") ||
                    array[0].equalsIgnoreCase("/ban-ip") ||
                    array[0].equalsIgnoreCase("/pardon") ||
                    array[0].equalsIgnoreCase("/kick")) {
                MediaType JSON = MediaType.get ("application/json; charset=utf-8");

                JSONObject jsonObject = new JSONObject();

                String player = event.getPlayer().getName();

                jsonObject.put("user", player);
                jsonObject.put("log", "Attempt to execute command \"" + array[0] + "\" on player " + array[1]);
                jsonObject.put("sendPush", true);

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                Request request = new Request.Builder()
                        .url (expressServer.baseUrl + "/addLog")
                        .post(body)
                        .build();

                try {
                    expressServer.client.newCall(request).execute();
                } catch (IOException e) {
                    return;
                }
            } else if (array[0].equalsIgnoreCase("/whitelist") && (array[1].equalsIgnoreCase("on") || array[1].equalsIgnoreCase("off"))) {
                MediaType JSON = MediaType.get ("application/json; charset=utf-8");

                JSONObject jsonObject = new JSONObject();

                String player = event.getPlayer().getName();

                jsonObject.put("user", player);
                jsonObject.put("log", "Whitelist status changed to " + array[1]);
                jsonObject.put("sendPush", true);

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                Request request = new Request.Builder()
                        .url (expressServer.baseUrl + "/addLog")
                        .post(body)
                        .build();

                try {
                    expressServer.client.newCall(request).execute();
                } catch (IOException e) {
                    return;
                }
            } else if (array[0].equalsIgnoreCase("/whitelist") && (array[1].equalsIgnoreCase("add") || array[1].equalsIgnoreCase("remove"))) {
                MediaType JSON = MediaType.get ("application/json; charset=utf-8");

                JSONObject jsonObject = new JSONObject();

                String player = event.getPlayer().getName();

                jsonObject.put("user", player);
                jsonObject.put("log", "Attempt to " + array[1] + " player " + array[2] + " to/from whitelist");
                jsonObject.put("sendPush", true);

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                Request request = new Request.Builder()
                        .url (expressServer.baseUrl + "/addLog")
                        .post(body)
                        .build();

                try {
                    expressServer.client.newCall(request).execute();
                } catch (IOException e) {
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onServerCommandPreprocessEvent (ServerCommandEvent event) {

        if (expressServer.client == null) return;

        String message = event.getCommand();

        String[] array = message.split(" ");

        if (array.length >= 2) {
            if (array[0].equalsIgnoreCase("ban") ||
                    array[0].equalsIgnoreCase("ban-ip") ||
                    array[0].equalsIgnoreCase("pardon-ip") ||
                    array[0].equalsIgnoreCase("kick")) {
                MediaType JSON = MediaType.get ("application/json; charset=utf-8");

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("user", "SERVER");
                jsonObject.put("log", "Attempt to execute command \"" + array[0] + "\" on player " + array[1]);
                jsonObject.put("sendPush", true);

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                Request request = new Request.Builder()
                        .url (expressServer.baseUrl + "/addLog")
                        .post(body)
                        .build();

                try {
                    expressServer.client.newCall(request).execute();
                } catch (IOException e) {
                    return;
                }
            } else if (array[0].equalsIgnoreCase("whitelist") && (array[1].equalsIgnoreCase("on") || array[1].equalsIgnoreCase("off"))) {
                MediaType JSON = MediaType.get ("application/json; charset=utf-8");

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("user", "SERVER");
                jsonObject.put("log", "Whitelist status changed to " + array[1]);
                jsonObject.put("sendPush", true);

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                Request request = new Request.Builder()
                        .url (expressServer.baseUrl + "/addLog")
                        .post(body)
                        .build();

                try {
                    expressServer.client.newCall(request).execute();
                } catch (IOException e) {
                    return;
                }
            } else if (array[0].equalsIgnoreCase("whitelist") && (array[1].equalsIgnoreCase("add") || array[1].equalsIgnoreCase("remove"))) {
                MediaType JSON = MediaType.get ("application/json; charset=utf-8");

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("user", "SERVER");
                jsonObject.put("log", "Attempt to " + array[1] + " player " + array[2] + " to/from whitelist");
                jsonObject.put("sendPush", true);

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                Request request = new Request.Builder()
                        .url (expressServer.baseUrl + "/addLog")
                        .post(body)
                        .build();

                try {
                    expressServer.client.newCall(request).execute();
                } catch (IOException e) {
                    return;
                }
            }
        }
    }
}
