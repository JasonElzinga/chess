package client.websocket;

//import webSocketMessages.Notification;

import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage notification);
}
