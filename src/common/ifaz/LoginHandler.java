package common.ifaz;

import common.model.User;

public interface LoginHandler {
   public void loggedIn(User user);

   public void duplicatedLogin();

   public void disconnected();

   public void banned();

   public void kicked();

   public void wrongVersion();
}
