package common.ifaz;

import common.model.User;

public interface POSTHandler {
    /**
     * Hace transferencia de puntos entre los usuarios
     * 
     * @param u1
     *            el usuario ganador
     * @param u2
     *            el usuario perdedor
     * @param puntos
     *            los puntos apostados
     * 
     * @return un array con [ptos1, ptos2] los puntos actualizados de los users
     */
    int[] transferPoints(User u1, User u2, int puntos);

    /**
     * Obtiene los puntos del usuario (registrado)
     * 
     * @param username
     * @param hash
     *            la clave
     * @return lo que devuelve el POST (se asume entero o algo fallo)
     */
    int validateUser(String username, String hash);

    void kickPlayer(String origen, String name, int min);

    void banPlayer(String origen, String user);

    void unbanPlayer(String origen, String user);
    
    boolean isOp(String name);
}
