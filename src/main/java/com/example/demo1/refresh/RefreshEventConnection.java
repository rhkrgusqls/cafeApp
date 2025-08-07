package com.example.demo1.refresh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.websocket.*;

@ClientEndpoint
public class RefreshEventConnection implements RefreshObserver {

    private Session session;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void connect(String wsUrl, String affiliationCode) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI(wsUrl + "?affiliation_code=" + affiliationCode);
        System.out.println("Connecting to WebSocket URL: " + uri);
        container.connectToServer(this, uri);
    }

    @Override
    public void onRefresh(RefreshEvent event) {
        List<String> pages = event.getPagesToRefresh();
        refreshPages(pages);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("WebSocket opened: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            Map<String, Object> json = parseJson(message);
            if ("refresh".equals(json.get("command"))) {
                List<String> pages = (List<String>) json.get("pages");
                refreshPages(pages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket closed: " + closeReason);
    }

    private void refreshPages(List<String> pages) {
        for (String page : pages) {
            System.out.println("Refresh page: " + page);
            switch (page) {
                case "storeManagement":
                    StoreManagementRefresh.refresh();
                    break;
                case "requestList":
                    RequestListRefresh.refresh();
                    break;
                case "itemList":
                    ItemListRefresh.refresh();
                    StoreManagementRefresh.refresh();
                    break;
                case "affiliationRequestList":
                    AffiliationRequestListRefresh.refresh();
                    break;
                default:
                    System.out.println("Unknown page: " + page);
                    break;
            }
        }
    }


    public void sendRefreshRequest(List<String> pages) throws Exception {
        String json = createJsonMessage("refreshRequest", pages);
        session.getBasicRemote().sendText(json);
    }

    private Map<String, Object> parseJson(String json) throws Exception {
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    private String createJsonMessage(String action, List<String> pages) throws Exception {
        Map<String, Object> map = Map.of(
                "action", action,
                "pages", pages
        );
        return objectMapper.writeValueAsString(map);
    }
}

