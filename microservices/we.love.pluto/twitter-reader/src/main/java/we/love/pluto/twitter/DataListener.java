package we.love.pluto.twitter;

/**
 * @author Michal Gajdos
 */
interface DataListener {

    void onNext(String message, String username);
}
