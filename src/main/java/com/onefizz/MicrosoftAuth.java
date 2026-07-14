package com.onefizz;

import com.google.gson.*;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class MicrosoftAuth {

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    private static final Gson GSON = new Gson();

    private static final String CLIENT_ID = "00000000402b5328";
    private static final String SCOPE = "XboxLive.signin offline_access";

    public static class AuthResult {
        public String username;
        public UUID uuid;
        public String accessToken;
        public String refreshToken;
    }

    public static class DeviceCode {
        public String userCode;
        public String verificationUri;
        public int expiresIn;
        public int interval;
        public String deviceCode;
    }

    public static CompletableFuture<DeviceCode> startDeviceCode() {
        return CompletableFuture.supplyAsync(() -> {
            String body = "client_id=" + urlEncode(CLIENT_ID)
                + "&scope=" + urlEncode(SCOPE);
            String json = postForm("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode", body);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            if (obj == null || !obj.has("device_code")) {
                throw new RuntimeException("Invalid device code response: " + json);
            }
            DeviceCode dc = new DeviceCode();
            dc.userCode = obj.get("user_code").getAsString();
            dc.verificationUri = obj.get("verification_uri").getAsString();
            dc.expiresIn = obj.get("expires_in").getAsInt();
            dc.interval = obj.has("interval") ? obj.get("interval").getAsInt() : 5;
            dc.deviceCode = obj.get("device_code").getAsString();
            return dc;
        });
    }

    public static CompletableFuture<AuthResult> pollToken(DeviceCode dc) {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            String msAccessToken = null;
            String refreshToken = null;

            while (System.currentTimeMillis() - start < dc.expiresIn * 1000L) {
                String body = "grant_type=urn:ietf:params:oauth:grant-type:device_code"
                    + "&client_id=" + urlEncode(CLIENT_ID)
                    + "&device_code=" + urlEncode(dc.deviceCode);
                String json = postForm("https://login.microsoftonline.com/consumers/oauth2/v2.0/token", body);
                JsonObject obj = GSON.fromJson(json, JsonObject.class);

                if (obj.has("error")) {
                    String error = obj.get("error").getAsString();
                    if ("authorization_pending".equals(error) || "slow_down".equals(error)) {
                        sleep(dc.interval * 1000L);
                        continue;
                    }
                    throw new RuntimeException("Microsoft auth error: " + error);
                }

                msAccessToken = obj.get("access_token").getAsString();
                refreshToken = obj.has("refresh_token") ? obj.get("refresh_token").getAsString() : null;
                break;
            }

            if (msAccessToken == null) {
                throw new RuntimeException("Device code expired");
            }

            JsonObject xblReq = new JsonObject();
            JsonObject xblProps = new JsonObject();
            xblProps.addProperty("AuthMethod", "RPS");
            xblProps.addProperty("SiteName", "user.auth.xboxlive.com");
            xblProps.addProperty("RpsTicket", "d=" + msAccessToken);
            xblReq.add("Properties", xblProps);
            xblReq.addProperty("RelyingParty", "http://auth.xboxlive.com");
            xblReq.addProperty("TokenType", "JWT");
            String xblJson = postJson("https://user.auth.xboxlive.com/user/authenticate", xblReq.toString());
            JsonObject xblResp = GSON.fromJson(xblJson, JsonObject.class);
            if (xblResp == null || !xblResp.has("Token")) {
                throw new RuntimeException("Xbox Live auth failed: " + xblJson);
            }
            String xblToken = xblResp.get("Token").getAsString();
            String uhs = xblResp.getAsJsonObject("DisplayClaims")
                .getAsJsonArray("xui").get(0).getAsJsonObject()
                .get("uhs").getAsString();

            JsonObject xstsReq = new JsonObject();
            JsonObject xstsProps = new JsonObject();
            xstsProps.addProperty("SandboxId", "RETAIL");
            JsonArray userTokens = new JsonArray();
            userTokens.add(xblToken);
            xstsProps.add("UserTokens", userTokens);
            xstsReq.add("Properties", xstsProps);
            xstsReq.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
            xstsReq.addProperty("TokenType", "JWT");
            String xstsJson = postJson("https://xsts.auth.xboxlive.com/xsts/authorize", xstsReq.toString());
            JsonObject xstsResp = GSON.fromJson(xstsJson, JsonObject.class);
            if (xstsResp == null || !xstsResp.has("Token")) {
                throw new RuntimeException("XSTS auth failed: " + xstsJson);
            }
            String xstsToken = xstsResp.get("Token").getAsString();

            JsonObject mcReq = new JsonObject();
            mcReq.addProperty("identityToken", "XBL3.0 x=" + uhs + ";" + xstsToken);
            String mcJson = postJson("https://api.minecraftservices.com/authentication/login_with_xbox", mcReq.toString());
            JsonObject mcResp = GSON.fromJson(mcJson, JsonObject.class);
            if (mcResp == null || !mcResp.has("access_token")) {
                throw new RuntimeException("Minecraft auth failed: " + mcJson);
            }
            String mcAccessToken = mcResp.get("access_token").getAsString();

            String profileJson = getAuth("https://api.minecraftservices.com/minecraft/profile", mcAccessToken);
            JsonObject profile = GSON.fromJson(profileJson, JsonObject.class);
            if (profile == null || profile.has("error")) {
                throw new RuntimeException("No Minecraft profile linked to this Microsoft account");
            }
            String username = profile.get("name").getAsString();
            UUID uuid = UUID.fromString(profile.get("id").getAsString()
                .replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));

            AuthResult res = new AuthResult();
            res.username = username;
            res.uuid = uuid;
            res.accessToken = mcAccessToken;
            res.refreshToken = refreshToken;
            return res;
        });
    }

    private static String postForm(String url, String body) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.body();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String postJson(String url, String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.body();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String getAuth(String url, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.body();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}