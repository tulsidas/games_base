package common.ifaz;

import org.apache.mina.common.IoSession;

import common.model.User;

public interface ClientLoginHandler {
   public void login(IoSession session, int salon, User usr, String key,
         long version);
}
