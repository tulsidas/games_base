package common.messages.server;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.BasicGameHandler;
import common.ifaz.BasicGameMessage;
import common.messages.FixedLengthMessageAdapter;

public class UpdatedPointsMessage extends FixedLengthMessageAdapter implements
      BasicGameMessage {

   private int puntos;

   public UpdatedPointsMessage() {
   }

   public UpdatedPointsMessage(int puntos) {
      this.puntos = puntos;
   }

   public void execute(BasicGameHandler game) {
      game.updatePoints(puntos);
   }

   @Override
   public String toString() {
      return "Update points (" + puntos + ")";
   }

   @Override
   public int getContentLength() {
      return 4;
   }

   @Override
   public void decode(ByteBuffer buff) {
      // nada que decodificar
      puntos = buff.getInt();
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.putInt(puntos);
   }

   @Override
   public byte getMessageId() {
      return 0x14;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + puntos;
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final UpdatedPointsMessage other = (UpdatedPointsMessage) obj;
      if (puntos != other.puntos) {
         return false;
      }
      return true;
   }
}
