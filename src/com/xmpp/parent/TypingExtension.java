package com.xmpp.parent;

import org.jivesoftware.smack.packet.PacketExtension;

public class TypingExtension implements PacketExtension {
    public static final String MAMHISTORY = "urn:xmpp:mam:1";
    public static final String CARBONHISTORY="urn:xmpp:carbons:2";
    public static final String  NAMESPACE = "http://jabber.org/protocol/chatstates";
    public static final String ELEMENT_COMPOSING = "composing";
    public static final String ELEMENT_GONE = "inactive";
    public static final String ElEMENT_PAUSE = "paused";

	boolean isTyping; // / original ID of the delivered message

	public TypingExtension(boolean isTyping) {
		this.isTyping = isTyping;
	}

	@Override
	public String getElementName() {
		if (isTyping) {
			return "composing";
		} else {
			return "inactive";
		}
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		if (isTyping) {
			return "<composing xmlns='" + NAMESPACE + "'/>";
		} else {
			return "<inactive xmlns='" + NAMESPACE + "'/>";
		}

	}
}