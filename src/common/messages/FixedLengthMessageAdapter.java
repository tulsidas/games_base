package common.messages;

import org.apache.mina.common.ByteBuffer;

public abstract class FixedLengthMessageAdapter implements FixedLengthMessage {

   @Override
   public boolean isFixedLength() {
      return true;
   }

   // /////////////////////////////
   // por default no agregan nada
   // /////////////////////////////

   public abstract int getContentLength();

   @Override
   public void decode(ByteBuffer buff) {
      // nada que decodificar
   }

   @Override
   public final ByteBuffer encode() {
      // sin contenido
      ByteBuffer ret = ByteBuffer.allocate(getContentLength() + 1).put(
            getMessageId());

      encodeContent(ret);

      return ret.flip();
   }

   /**
    * Dado el ByteBuffer ya creado, pongo el contenido específico
    * 
    * @param ret
    */
   protected void encodeContent(ByteBuffer buff) {
      // nada por default
   }
}
