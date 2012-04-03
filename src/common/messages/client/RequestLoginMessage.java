package common.messages.client;

import java.nio.BufferUnderflowException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import common.ifaz.ClientLoginHandler;
import common.ifaz.ClientLoginMessage;
import common.messages.TaringaProtocolEncoder;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

/**
 * Mensaje que manda el cliente para pedir sumarse al juego
 */
public class RequestLoginMessage extends VariableLengthMessageAdapter implements
      ClientLoginMessage {

   private User usr;

   private String key;

   private long version;

   private int salon;

   /**
    * @param id
    *           el salon
    * @param usr
    *           el username
    * @param key
    *           md5
    */
   public RequestLoginMessage() {
   }

   public RequestLoginMessage(int salon, User usr, String key, long version) {
      this.salon = salon;
      this.usr = usr;
      this.key = key;
      this.version = version;
   }

   public void execute(IoSession session, ClientLoginHandler clh) {
      clh.login(session, salon, usr, key, version);
   }

   @Override
   public String toString() {
      return "Request Login";
   }

   @Override
   public ByteBuffer encodedContent() {
      CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();

      // User: name - puntos - guest
      // key
      // salon
      ByteBuffer ret = ByteBuffer.allocate(64);
      ret.setAutoExpand(true);

      User.writeTo(usr, ret);

      if (key == null) {
         ret.put(TaringaProtocolEncoder.NULL);
      }
      else {
         ret.put(TaringaProtocolEncoder.NON_NULL);

         // key
         try {
            ret.putPrefixedString(key, enc);
         }
         catch (CharacterCodingException e) {
            e.printStackTrace();
         }
      }

      ret.put((byte) salon);

      ret.putLong(version);

      ret.flip();

      return ret;
   }

   @Override
   public byte getMessageId() {
      return 0x01;
   }

   public void decode(ByteBuffer buff) {
      CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();

      usr = User.readFrom(buff);

      if (buff.get() == TaringaProtocolEncoder.NON_NULL) {
         try {
            key = buff.getPrefixedString(dec);
         }
         catch (CharacterCodingException e) {
            e.printStackTrace();
         }
      }

      salon = buff.get();

      try {
         version = buff.getLong();
      }
      catch (BufferUnderflowException bue) {
         // version muy vieja, dejamos version = 0, lo cual va a hacer que salte
         // el WrongMessage
      }
   }
}