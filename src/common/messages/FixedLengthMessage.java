package common.messages;

public interface FixedLengthMessage extends Message {
   /**
    * El tamaño (fijo) del mensaje (sin contar el id)
    * 
    * @return
    */
   public int getContentLength();
}
