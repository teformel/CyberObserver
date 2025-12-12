package com.cyber.common.model;

/**
 * Envelope for all WebSocket communications.
 */
public class CyberMessage {
    public enum Type {
        AUTH,
        STATUS_UPDATE,
        CONTROL_CMD,
        ERROR
    }

    private Type type;
    private String payloadJson; // Serialized inner object (ID, Status, or Cmd)

    public CyberMessage() {}

    public CyberMessage(Type type, String payloadJson) {
        this.type = type;
        this.payloadJson = payloadJson;
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}
