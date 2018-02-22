package com.xmpp.teacher.history;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.adapter.teacher.Childbeans;
import com.db.teacher.DatabaseHelper;
import com.xmpp.teacher.Constant;
import com.xmpp.teacher.XMPPMethod;


import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.mam.element.MamFinIQ;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by etech on 22/8/16.
 */
public class MamHistory {


    private static MamHistory instance;
    private static MamManager mamManager;
    static Context context;
    List<Childbeans> newMessage = null;
    //List<Childbeans> newMessage1 = new ArrayList<Childbeans>();
    private String withJid;
    private boolean external;

    public static MamHistory init(Context context, XMPPConnection xmppcon) {
        if (instance == null) {
            instance = new MamHistory();
        }
        instance.context = context;
        if (xmppcon != null && xmppcon.isConnected() && xmppcon.isAuthenticated()) {
            mamManager = MamManager.getInstanceFor(xmppcon);
        }

        return instance;
    }

    public List<Childbeans> loadHistory(String withJid, boolean external) {
        this.withJid = withJid;
        this.external = external;
        try {
            if (mamManager != null) {
                if (mamManager.isSupportedByServer()) {
                    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                    Jid receiverjid = JidCreate.bareFrom(withJid);
                    String ts = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
                    //pass query to fetch history
                    MamManager.MamQueryResult mamQueryResult = mamManager.pageBefore(receiverjid, ts, 10);

                    //set result of history
                    List<Forwarded> forwardedMessages = mamQueryResult.forwardedMessages;
                    MamFinIQ finsetval = mamQueryResult.mamFin;
                    Constant.iscomplete = finsetval.isComplete();
                    Constant.rmssetTime = finsetval.getRSMSet().getFirst();
                    if (newMessage != null && newMessage.size() > 0)
                        newMessage.clear();

                    newMessage = new ArrayList<Childbeans>();

                    //fetch message from forwardedmessage and add into array list
                    for (int i = 0; i < forwardedMessages.size(); i++) {
                        Stanza packet = forwardedMessages.get(i).getForwardedStanza();
                        Date stamp = forwardedMessages.get(i).getDelayInformation().getStamp();
                        Message msghistory = (Message) packet;
                        Log.d("MamHistory",msghistory.toString());
                        newMessage.add(getmessagefromstanza(msghistory, packet, stamp, external));
                    }

                }
            }
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newMessage;
    }

    public List<Childbeans> loadoldHistory(String withJid, boolean external) {
        try {
            Jid receiverjid = JidCreate.bareFrom(withJid);

            //pass query to fetch history
            MamManager.MamQueryResult mamQueryResult = mamManager.pageBefore(receiverjid, Constant.rmssetTime, 10);

            //set result of history
            List<Forwarded> forwardedMessages = mamQueryResult.forwardedMessages;
            MamFinIQ finsetval = mamQueryResult.mamFin;
            Constant.iscomplete = finsetval.isComplete();
            Constant.rmssetTime = finsetval.getRSMSet().getFirst();
            if (newMessage != null && newMessage.size() > 0)
                newMessage.clear();

            newMessage = new ArrayList<Childbeans>();
            //fetch message from forwardedmessage and add into array list
            for (int i = 0; i < forwardedMessages.size(); i++) {
                Stanza packet = forwardedMessages.get(i).getForwardedStanza();
                Date stamp = forwardedMessages.get(i).getDelayInformation().getStamp();

                Message msghistory = (Message) packet;
                newMessage.add(getmessagefromstanza(msghistory, packet, stamp, external));
            }
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return newMessage;
    }

    private Childbeans getmessagefromstanza(Message msgreceived, Stanza packet, Date stamp, boolean external) {
        DatabaseHelper db = DatabaseHelper.getDBAdapterInstance(context);
        Childbeans newMessage = new Childbeans();
        StandardExtensionElement extTimestamp = (StandardExtensionElement) msgreceived.getExtension("urn:xmpp:extra");
        if (external)
            newMessage.parenttype = extTimestamp != null ? extTimestamp.getAttributeValue("parent") : "";

        newMessage.message_body = msgreceived.getBody();
        newMessage.messageTimeMilliseconds = 00;
        newMessage.messageType = Constant.MESSAGE_TYPE_RECEIVED;
        newMessage.message_id = packet.getPacketID();
        //  newMessage.setGroupChat(false);
        newMessage.username = Constant.getUserName(msgreceived.getFrom().toString()).toUpperCase();
        newMessage.receiver_id_jid = Constant.getUserName(msgreceived.getTo().toString()).toUpperCase();
        newMessage.sender_id_jid = Constant.getUserName(msgreceived.getFrom().toString()).toUpperCase();
        newMessage.typing = false;
        newMessage.message_status = db.getmessagestatus(packet.getStanzaId());
        newMessage.user_id = "0";
        newMessage.name = "0";
        newMessage.image = "";
        SimpleDateFormat ddf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            newMessage.created_at = ddf.format(stamp);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
       // newMessage.created_at = convertlocalize(stamp.toString());
        newMessage.sender = getsenderinfo(Constant.getUserName(msgreceived.getTo().toString()).toUpperCase());
        newMessage.sender_id = "";
        newMessage.parent_id = "";
        newMessage.receiver_name = "";
        newMessage.receiver_image = "";
        newMessage.receiver_name = Constant.getUserName(msgreceived.getTo().toString()).toUpperCase();
        newMessage.child_moblie = "-------";
        newMessage.parenttype = getparentno(msgreceived);
        return newMessage;
    }

    private String getsenderinfo(String tojid) {
        String sender = "me";
        SharedPreferences prefer = context.getSharedPreferences(Constant.USER_FILENAME, Context.MODE_PRIVATE);
        String userjid = Constant.getUserName(prefer.getString("jid", ""));
        if (userjid.equalsIgnoreCase(tojid))
            sender = "from";
        else
            sender = "me";
        return sender;
    }


    private String convertlocalize(String created_at) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat ddf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//("EEE MMM dd HH:mm:ss z yyyy");//, Locale.ENGLISH);
        try {
            created_at = sdf.format(ddf.parse(created_at));
        } catch (ParseException e1) {
            e1.printStackTrace();

        }
        return created_at;
    }

    private String getparentno(Message msgreceived) {
        String parentno = "";
        StandardExtensionElement extTimestamp = (StandardExtensionElement) msgreceived.getExtension("urn:xmpp:extra");
        if (extTimestamp != null) {
            List<StandardExtensionElement> element = extTimestamp.getElements();
            if (element.size() > 0) {
                parentno = element.get(0).getText() != null ? element.get(0).getText() : "";
            }
        }
        return parentno;
    }


}
